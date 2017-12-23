import org.apache.solr.common.SolrInputDocument;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class Index {


    public static void main(String args[]){



        // Build document
        SolrInputDocument document = buildDoc();

//        // Add document to Solr
//        String urlString = "http://localhost:8983/solr/test";
//        SolrClient solr = new HttpSolrClient.Builder(urlString).build();
//        try{
//            solr.add(document);
//            solr.commit();
//            solr.deleteById("123456");
//            solr.commit();
//        }
//        catch(Exception e){
//            System.out.println(e.getMessage());
//        }
    }

    // A method to parse a court XML file and builds a Solr document
    // TODO Get fields
    public static SolrInputDocument buildDoc() {
        System.out.print("Please enter the FULL path of the root directory: ");
        return buildDocHelper(new File(stdin.nextLine()));

    }
    public static SolrInputDocument buildDocHelper(File root){
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                buildDocHelper(child);
            }
        }
        else {
            try {
                InputStream in = new FileInputStream(root);
                // Create a new XMLInputFactory and event reader
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

                // Traverse the XML document
                String curr = null;
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals("category")) {
                            event = eventReader.nextEvent();
                            curr = event.asCharacters().getData();
                        }
                    }
                }
                return curr;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
