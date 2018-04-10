package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.jena.rdf.model.*;

import com.hp.hpl.jena.util.FileManager;

import util.ConfigManager;

/**
 * Main class of the ShaefflerPEPMain project
 * @author Irlan
 */

public class RDF2CSV {

	Model jenaModel = null;

	/**
	 * Entry point method of the application
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		RDF2CSV main = new RDF2CSV();
		ConfigManager.loadConfig();
		main.readOntology();
	}
	
	/**
	 * Read the ontology file
	 * @throws FileNotFoundException 
	 */
	public void readOntology() throws FileNotFoundException{
		InputStream inputStream = FileManager.get().open(ConfigManager.getFilePath());
		jenaModel = ModelFactory.createDefaultModel();
		jenaModel.read(new InputStreamReader(inputStream), null, "TURTLE");
		
		StmtIterator iter = jenaModel.listStatements();
		
		PrintWriter pw = new PrintWriter(new File("test.csv"));
        StringBuilder sb = new StringBuilder();
        
        sb.append("s");
        sb.append(',');
        sb.append('p');
        sb.append(',');
        sb.append('o');
        sb.append('\n');
        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();         // get next statement
            Resource  subject   = stmt.getSubject();   // get the subject
            Property  predicate = stmt.getPredicate(); // get the predicate
            RDFNode   object    = stmt.getObject();    // get the object
            
            String subjectToWrite = stmt.getSubject().getLocalName();
            String predicateToWrite = stmt.getPredicate().getLocalName();
            String objectToWrite = "";
            
            if (object.isURIResource()) {
				object = jenaModel.getResource(object.as(Resource.class).getURI());
				objectToWrite = object.asNode().getLocalName();
			} else {
				if (object.isLiteral()) {
					if(object.toString().length()>100){
						objectToWrite = object.asLiteral().getLexicalForm().substring(0, 20);
					}else  objectToWrite = object.asLiteral().getLexicalForm();
				} 
			}
            
            sb.append(subjectToWrite);
            sb.append(',');
            sb.append(predicateToWrite);
            sb.append(',');
            sb.append(objectToWrite);
            sb.append('\n');
            
            System.out.println("Subject   " + subjectToWrite);
            System.out.println("predicate   " + predicateToWrite);
            System.out.println("Object   " + objectToWrite);
            System.out.println("\n   " );
        }
        
        //jenaModel.write( System.out, "N-TRIPLE");

    
        pw.write(sb.toString());
        pw.close();
	}
	
	

}
