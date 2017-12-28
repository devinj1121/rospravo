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
        if(document != null){
            // Add document to Solr
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
                String[] cdataArray = null;
                String cdataString = null;
                String category;
                String location;
                String date;
                String plaintiff;
                String defendant;
                String thirdp;
                String sought;
                String awarded;
                String winloss;

                // Store CData
                NodeList bodyNodes = xmlDoc.getElementsByTagName("body");
                for (int i = 0; i < bodyNodes.getLength(); i++) {
                    Element e = (Element)bodyNodes.item(i);
                    cdataString = e.getTextContent().trim();
                    cdataArray  = e.getTextContent().trim().split(" ");
                }

                // Category
                if(cdataString.toLowerCase().contains("о взыскании задолженности")){
                    category = "О взыскании задолженности";
                }
                else if(cdataString.toLowerCase().contains("о взыскании обязательных платежей")){
                    category = "О взыскании обязательных платежей";
                }
                else{
                    return null;
                }

                System.out.println(root.getName() + " in category of interest!");
                System.out.println(category);

                // Location
                if(cdataString.contains("г.")){
                    String temp = cdataString.substring(cdataString.indexOf("г.") + 3);
                    location = temp.substring(0, temp.indexOf(" "));
                    System.out.println(location);
                }

                // Date
                NodeList dateNodes = xmlDoc.getElementsByTagName("date");
                for(int i = 0; i < dateNodes.getLength(); i++){
                    date = dateNodes.item(i).getTextContent().trim();
                    System.out.println(date);
                    break;
                }

                // Plaintiff
                if(cdataString.contains("от истца – ")){
                    String temp = cdataString.substring(cdataString.indexOf("от истца – ") + 11);
                    plaintiff = temp.substring(0, temp.indexOf(",")).trim();
                    if(plaintiff.toLowerCase().contains("не явился")){
                        plaintiff = "не явился";
                    }
                    System.out.println(plaintiff);
                }

                // Defendant
                if(cdataString.contains("от ответчика – ")){
                    String temp = cdataString.substring(cdataString.indexOf("от ответчика – ") + 15);
                    defendant = temp.substring(0, temp.indexOf(",")).trim();
                    if(defendant.toLowerCase().contains("не явился")){
                        defendant = "не явился";
                    }
                    System.out.println(defendant);
                }

                // Third Party
                if(cdataString.contains("от 3-его лица – ")){
                    String temp = cdataString.substring(cdataString.indexOf("от 3-его лица – ") + 16);
                    thirdp = temp.substring(0, temp.indexOf(",")).trim();
                    if(thirdp.toLowerCase().contains("не явился")){
                        thirdp = "не явился";
                    }
                    System.out.println(thirdp);
                }

                // Amount Sought
                // TODO Handle all cases
                if(cdataString.contains("о взыскании")){
                    String temp;
                    if(cdataString.contains("о взыскании - ")){
                        temp = cdataString.substring(cdataString.indexOf("о взыскании - ") + 14);
                    }
                    else{
                        temp = cdataString.substring(cdataString.indexOf("о взыскании") + 13);
                    }
                    sought = temp.substring(0, temp.indexOf(".")).trim();
                    System.out.println(sought);
                }

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

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }
}
