/**
 * 
 */
package org.askw.link_based_fusion.pagination;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * A light method for an exhaustive retrieval of triples i.e., are the result 
 * of a CONSTRUCT query, from a SPARQL endpoint.
 * The result list is paginated as its size may become very large, and each
 * page is written into an .nt file. 
 * 
 * 
 * @author Amit Kirschenbaum
 *
 */
public class ConstructQueryPaginator {

	
	
	private Query query;
	private File outDir;
	private String resultsFilePrefix;
	private ParameterizedSparqlString pQuery;
	private long limit;
	private long initialOffset;
	private URI sparqlEndPoint;
	
	
	/**
	 * @param args command line arguments are specified under <code>getCLIParser()</code>
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		
		OptionParser parser =  getCLIParser();
		OptionSet options = parser.parse(args);
		
		File queryFile = (File)options.valueOf("query");
		long limit = (Long)options.valueOf("limit");
		File outDir = (File)options.valueOf("outdir");
		URI sparqlEndPoint = (URI)options.valueOf("sparqlendpoint");
		String prefix = (String)options.valueOf("outfileprefix");
		long initialOffset = (Long)options.valueOf("initialoffset");
		
		ConstructQueryPaginator cqp = new ConstructQueryPaginator(queryFile,limit,outDir,sparqlEndPoint,prefix,initialOffset);
		
		final long startTime = System.currentTimeMillis();
		cqp.execute();
		//cqp.executePaginatedQuery(limit, initialOffset);
		final long endTime = System.currentTimeMillis();
		//System.out.println("Processing time " +(endTime-startTime));
		
		
		
	}
	
	public ConstructQueryPaginator(File queryFile, long limit, File outDir, URI sparqlEndPoint, String resultsFilePrefix,long initialOffset)
	{
		query = QueryFactory.read(queryFile.getAbsolutePath()); 
		
		this.limit = (limit>0)? limit:10000;	
		
				
		StringBuffer parameterizedSparqlStringBuffer = new StringBuffer(query.toString());
		parameterizedSparqlStringBuffer.append("\nLIMIT ?limit")
										.append("\nOFFSET ?offset");
					
		pQuery  = new ParameterizedSparqlString(parameterizedSparqlStringBuffer.toString());
		
	    
		 
		this.outDir = outDir; 
		this.resultsFilePrefix = resultsFilePrefix; 
		 
		if(!(outDir.exists() && outDir.isDirectory()))
		 {
			  outDir.mkdir();
			 
		 }
		
		 this.sparqlEndPoint = sparqlEndPoint;
	
		 this.initialOffset = initialOffset;
	}
	
	
	
	public void execute() throws FileNotFoundException
	{
		
		
		long offset = initialOffset;//0L;
		pQuery.setLiteral("limit", limit);
        
		QueryEngineHTTP httpQuery;
		do
	    {
			pQuery.setLiteral("offset", offset);
	        query = QueryFactory.create(pQuery.toString(), Syntax.syntaxARQ) ;
	    
	        httpQuery = new QueryEngineHTTP(sparqlEndPoint.toString(),query);
    	
	        
	        Model m = httpQuery.execConstruct();
	       
	        
	        //stop when there are no more elements 
	        if(m.isEmpty())
	        {
	        	
	        	break; 
	        }
	        OutputStream os = new FileOutputStream(new File(outDir.getAbsolutePath()+"/"+resultsFilePrefix+offset+".nt")); 
	        RDFDataMgr.write(os,m,RDFFormat.NTRIPLES_UTF8);
	        
	        offset +=limit;
	        
	        httpQuery.close();
	    }while (true); 
    	
		
	}
	
	
	public Model executePaginatedQuery(long limit, long offset) throws FileNotFoundException
	{
		
		pQuery.setLiteral("limit", limit);
        pQuery.setLiteral("offset", offset);
	    query = QueryFactory.create(pQuery.toString(), Syntax.syntaxARQ) ;
	    System.out.println(query.toString());
	    QueryEngineHTTP httpQuery = new QueryEngineHTTP(sparqlEndPoint.toString(),query);
	    Model m = httpQuery.execConstruct(); //qex.execConstruct();
	    if(!m.isEmpty())
	    {
	    	OutputStream os = new FileOutputStream(new File(outDir.getAbsolutePath()+"/"+resultsFilePrefix+offset+".nt"));
	    	RDFDataMgr.write(os,m,RDFFormat.NTRIPLES_UTF8);
	    }
	    else
	    {
	    	System.err.println("resulting RDF model is empty " + m.isEmpty());
	    }
	    httpQuery.close();
		return m;
	}
	
	private static OptionParser getCLIParser()
	{
		OptionParser parser = new OptionParser();
		parser.accepts("query","SPARQL query file")
			  .withRequiredArg()
			  .ofType( File.class );
		parser.accepts("limit","Size of each page; Defaults to 10000, should be verified against the endpoint)")
			  .withRequiredArg()
			  .ofType(Long.class)
			  .defaultsTo(10000L);					;
		parser.accepts("outdir","Output directory; Defaults to 'out' under home directory  ")
			  .withRequiredArg()
		  	  .ofType( File.class )
		  	  .defaultsTo( new File(System.getProperty("user.dir")+File.separator +"out"));
		parser.accepts("sparqlendpoint","SPARQL service endpoint")
			  .withRequiredArg()
			  .ofType(URI.class);
		parser.accepts("outfileprefix","Prefix string for output files; Defaults to 'p'")
			  .withRequiredArg().ofType(String.class)
			  .defaultsTo("p");
		parser.accepts("initialoffset","Initial offset from the beginning og the resulting set; Defaults to 0")
				.withRequiredArg()
				.ofType(Long.class)
				.defaultsTo(0L);
		
				
	
		return parser;
	}
	
	
	

}
