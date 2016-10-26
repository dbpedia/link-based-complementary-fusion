# link-based-complementary-fusion
A light-weight tool to fuse complementary facts to DBpedia identifiers


Usage: 

Under the directory created from downloading/cloning, build the project and 
produce an executable jar using

``$ mvn package``

The resulting target directory contains jar file JRCNamesJena-0.0.2-SNAPSHOT.jar that include
the project and dependency files. 
  
Use the following format to run it  

``$ java -jar ./target/JRCNamesJena-0.0.2-SNAPSHOT.jar --query  <path/to/query.rq> [--limit <page size>]  [--outdir <output files directory>] [--outfileprefix <prefix>] --sparqlendpoint <URI to SPRAQL endpoint>``
 
 where the arguments are:
 
 + `query`     A SPARQL query of CONSTRUCT type, which is used to to match data against DBPedia.   
 	
 + ``sparqlendpoint`` 	URI to the SPARQL endpoint  
 
 + `limit`     Required page size; Defaults to 10000, actual page size should be checked against the SPARQL endpoint.
 + ``outdir``    Directory where output (paged) files are written, defaults to _out_ under the user home directory
 + ``outputfileprefix``	Prefix for the output files. Each file will additionally have a number which corresponds to the offset of records from the SPARQL endpoint; defaults to _p_	.
 
 To link DBPedia resources of persons and organizations to their corresponding entries in JRC-Names use:
 
``$ java -jar ./target/JRCNamesJena-0.0.2-SNAPSHOT.jar --query  ./resources/jrcnames-fuse.rq --limit 10000 --outdir ./out --outfileprefix p --sparqlendpoint http://data.europa.eu/euodp/sparqlep``
 
 
 (Values of outdir and outfileprefix can be of course different than those suggested above)