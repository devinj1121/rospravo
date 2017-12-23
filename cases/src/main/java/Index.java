import org.apache.solr.common.SolrInputDocument;
import java.io.File;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.util.Scanner;
import javax.xml.parsers.*;


public class Index {

    static Scanner stdin = new Scanner(System.in);

    public static void main(String args[]){

        // Build document
        SolrInputDocument document = buildSolrDoc();

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
    public static SolrInputDocument buildSolrDoc() {
        System.out.print("Please enter the FULL path of the root directory: ");
        return buildSolrDocHelper(new File(stdin.nextLine()));

    }
    private static SolrInputDocument buildSolrDocHelper(File root) {
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                buildSolrDocHelper(child);
            }
        } else {
            try {

                // XML Document Built
                Document xmlDoc = buildXMLDoc(root.getAbsolutePath());

                // Fields to grab
                String category;
                String location;
                String date;
                String plaintiff;
                String defendant;
                String thirdp;
                String yavil;
                String sought;
                String awarded;
                String winloss;

                // Category
                NodeList categoryNodes = xmlDoc.getElementsByTagName("category");

                // Location

                // Date

                // Plaintiff

                // Defendant

                // Third Party

                // Yavilsa?

                // Amount Sought

                // Amount Awarded

                // Win or Loss


//                }
//                SolrInputDocument ret = new SolrInputDocument();
//                ret.addField("Category", category);
//                return ret;
//            }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }
    private static Document buildXMLDoc(String docString) {
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
//            factory.setValidating(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(docString));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
