package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;

import com.hp.hpl.jena.util.FileManager;

import util.ConfigManager;
import util.StringUtil;

/**
 * Main class of the RDF2CSV project
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
		String subjectToWrite = "";
		String objectToWrite = "";
		String srcDataset = "sto";
		String dstDataset = "sto";
		
		InputStream inputStream = FileManager.get().open(ConfigManager.getOntologyPath());
		jenaModel = ModelFactory.createDefaultModel();
		jenaModel.read(new InputStreamReader(inputStream), null, "TURTLE");

		StmtIterator iter = jenaModel.listStatements();

		PrintWriter pw = new PrintWriter(new File(ConfigManager.getCSVPath()));
		StringBuilder sb = new StringBuilder();

		sb.append("s");
		sb.append(',');
		sb.append('p');
		sb.append(',');
		sb.append('o');
		sb.append(',');
		sb.append("srcDataset");
		sb.append(',');
		sb.append("dstDataset");
		sb.append('\n');
		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
			Statement stmt      = iter.nextStatement(); // get next statement
			Resource  subject   = stmt.getSubject();    // get the subject
			Property  predicate = stmt.getPredicate();  // get the predicate
			RDFNode   object    = stmt.getObject();     // get the object
			
			subjectToWrite = stmt.getSubject().getLocalName();
			if(subjectToWrite == null){
				continue;
			}
			
			subjectToWrite = jenaModel.shortForm(subject.asResource().getURI());
			
			if(subjectToWrite.equals("owl:Thing")){
				continue;
			}
			
			String predicateToWrite = jenaModel.shortForm(predicate.asResource().getURI());
			
			if (predicate.asNode().getLocalName().toString().equals("type")) {
				String objectType = jenaModel.shortForm(object.asResource().getURI()).toString();
				
				if(objectType.equals("owl:AnnotationProperty")){
					continue;
				}
				
				if(objectType.equals("owl:Thing")){
					continue;
				}
				
			}
			
			if (predicate.asNode().getLocalName().toString().equals("domain")) {
				String objectType = jenaModel.shortForm(object.asResource().getURI()).toString();
				
				if(objectType.equals("owl:Thing")){
					continue;
				}
			}
			
			if (object.isURIResource()) {
				object = jenaModel.getResource(object.as(Resource.class).getURI());
				objectToWrite = jenaModel.shortForm(object.asResource().getURI());
				
				String tmpDestinyDataset = objectToWrite.split(":")[0];
				if(!tmpDestinyDataset.equals("sto") && 
				   !tmpDestinyDataset.equals("https") &&
				   !tmpDestinyDataset.equals("http") &&
				   !tmpDestinyDataset.equals("owl") &&
				   !tmpDestinyDataset.equals("dul")
				   ){
					dstDataset = tmpDestinyDataset;
				}
				
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
			sb.append(',');
			sb.append(srcDataset);
			sb.append(',');
			sb.append(dstDataset);
			sb.append('\n');
		}

		pw.write(sb.toString());
		pw.close();
	}

}
