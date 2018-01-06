import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import java.io.File;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.*;

// Class to index documents into Solr
public class Index {

    // Global values
    public static Scanner stdin = new Scanner(System.in);
    public static List<SolrInputDocument> docs = new ArrayList<>();

    public static void main(String args[]){

        // Build documents, setup server
        buildSolrDocs();
//        String urlString = "http://localhost:8983/solr/test";
//        SolrClient solr = new HttpSolrClient.Builder(urlString).build();

        // Add documents to Solr
//        for(SolrInputDocument doc : docs){
//            try{
//                solr.add(doc);
//                solr.commit();
//            }
//            catch(Exception e){
//                System.out.println(e.getMessage());
//            }
//        }
    }

    // A method to parse a court XML file and build a Solr document
    public static void buildSolrDocs() {
        System.out.print("Please enter the FULL path of the root directory: ");
        buildSolrDocsHelper(new File(stdin.nextLine()));

    }

    // A recursive helper method for building SolrDocs
    private static void buildSolrDocsHelper(File root) {
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                buildSolrDocsHelper(child);
            }
        } else {
            try {
                // XML Document Built
                Document xmlDoc = buildXMLDoc(root.getAbsolutePath());
                SolrInputDocument ret = new SolrInputDocument();
                String cdata = null;

                // Store CData
                NodeList bodyNodes = xmlDoc.getElementsByTagName("body");
                for (int i = 0; i < bodyNodes.getLength(); i++) {
                    Element e = (Element) bodyNodes.item(i);
                    cdata = e.getTextContent().trim();
                }

                // Category
                if (cdata.toLowerCase().contains("о взыскании задолженности")) {
                    ret.addField("Category", "О взыскании задолженности");
                } else if (cdata.toLowerCase().contains("о взыскании обязательных платежей")) {
                    ret.addField("Category", "О взыскании обязательных платежей");
                }
                else{
                    return;
                }

                System.out.println(root.getName() + " in category of interest!");

                // Location
                ret.addField("Location", getLocation(cdata));

                // Date
                NodeList dateNodes = xmlDoc.getElementsByTagName("date");
                for (int i = 0; i < dateNodes.getLength(); i++) {
                    ret.addField("Date", dateNodes.item(i).getTextContent().trim());
                    break;
                }
                // Parties
                ret.addField("Plaintiff", getParty(cdata, "от истца – "));
                ret.addField("Defendant", getParty(cdata, "от ответчика – "));
                ret.addField("Third Party", getParty(cdata, "от 3-его лица – "));

                // Amount sought, interest, penalties
                ret.addField("Amount Sought", getRubles(cdata, "о взыскании"));
                ret.addField("Interest and Penalties", getRubles(cdata, "взыскании штраф"));

                // Amount Awarded

                // Win or Loss

                // Add Solr doc
                docs.add(ret);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // A method to use DOM parser to build XML doc tree
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

    // A method to get ruble value from a string
    public static String getRubles(String string, String chunkIdentifier){
        // Grab chunks of text containing it, use 200 character buffer
        ArrayList<String> chunks = new ArrayList<>();
        // While occurrences found, keep adding to chunks
        int currOcc = string.indexOf(chunkIdentifier);
        while (currOcc >= 0) {
            chunks.add(string.substring(currOcc + 1, currOcc + 201));
            currOcc = string.indexOf(chunkIdentifier, currOcc + 1);
        }
        // Loop through chunks, find first occurrence of ruble value
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            String[] split = chunk.split(" ");
            for (int j = 0; j < split.length; j++) {
                if (split[j].equals("руб.")) {
                    return split[j - 1];
                }
            }
        }
        return "";
    }

    // A method to get the location of a court case
    public static String getLocation(String string){
        if (string.contains("г.")) {
            String temp = string.substring(string.indexOf("г.") + 3);
            // Deal with nbsp
            return temp.substring(0, temp.indexOf(" ")).replace(String.valueOf((char) 160), "").trim();
        }
        return "";
    }

    // A method to get the parties of the court case
    public static String getParty(String string, String party){
        String temp;
        if (string.contains(party)) {
             temp = string.substring(string.indexOf(party) + party.length());
             temp = temp.substring(0, temp.indexOf(",")).trim();
             if (temp.toLowerCase().contains("не явился")) {
                 return "не явился";
             }
             else{
                 return temp;
             }
        }
        return "";
    }
}
