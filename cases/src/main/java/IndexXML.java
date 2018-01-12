import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import java.io.File;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.util.ArrayList;
import javax.xml.parsers.*;

// Class to index documents into Solr
public class IndexXML implements Runnable {

    private File file;

    public IndexXML(File file) {
        this.file = file;
    }

    public void run() {
        try {
//            String urlString = "http://localhost:8983/solr/test";
//            SolrClient solr = new HttpSolrClient.Builder(urlString).build();

            // XML Document Built
            Document xmlDoc = buildXMLDoc(file.getAbsolutePath());
            SolrInputDocument ret = new SolrInputDocument();
            String cdata = null;

            // Store CData
            NodeList bodyNodes = xmlDoc.getElementsByTagName("body");
            for (int i = 0; i < bodyNodes.getLength(); i++) {
                Element e = (Element) bodyNodes.item(i);
                cdata = e.getTextContent().trim();
            }

            // Category
            NodeList categoryNodes = xmlDoc.getElementsByTagName("category");
            String category = "";
            for (int i = 0; i < categoryNodes.getLength(); i++) {
                category = categoryNodes.item(i).getTextContent().trim();
                break;
            }
            switch(category) {
                case "о взыскании задолженности":
                    ret.addField("Category", "О взыскании задолженности");
                    break;

                case "о взыскании обязательных платежей":
                    ret.addField("Category", "О взыскании обязательных платежей");
                    break;

                case "":
                    if (cdata.toLowerCase().contains("о взыскании задолженности")) {
                        ret.addField("Category", "О взыскании задолженности");
                    } else if (cdata.toLowerCase().contains("о взыскании обязательных платежей")) {
                        ret.addField("Category", "О взыскании обязательных платежей");
                    } else {
                        return;
                    }
                    break;
                default:
                    return;
            }

            System.out.println(file.getName() + " in category of interest!");

            // Location
            ret.addField("Location", getLocation(cdata));

            // Date
            NodeList dateNodes = xmlDoc.getElementsByTagName("date");
            for (int i = 0; i < dateNodes.getLength(); i++) {
                ret.addField("Date", dateNodes.item(i).getTextContent().trim());
                break;
            }
            // Parties
            ret.addField("Plaintiff", getParty(cdata, "истца – "));
            ret.addField("Defendant", getParty(cdata, "ответчика – "));
            ret.addField("Third Party", getParty(cdata, "3-его лица – "));

            // Amount sought, interest, penalties
            ret.addField("Amount Sought", getRubles(cdata, "о взыскании"));
            ret.addField("Interest and Penalties", getRubles(cdata, "взыскании штраф"));

            // Amount Awarded

            // Win or Loss

            // Add Solr doc
//            solr.add(ret);
//            solr.commit();
//            System.out.println(ret.getFieldValue("Date"));
//            System.out.println(ret.getFieldValue("Location"));
            System.out.println(ret.getFieldValue("Plaintiff"));
//            System.out.println(ret.getFieldValue("Amount Sought"));
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // A method to use DOM parser to build XML doc tree
    private static Document buildXMLDoc(String docString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setValidating(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(docString));
        } catch (Exception e) {
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

        // Four different possibilities
        String temp = "";
        if(string.contains("г</span><span>.")) temp = string.substring(string.indexOf("г</span><span>.") + 15);
        else if(string.contains("г.")) temp = string.substring(string.indexOf("г.") + 2);

        // General cleanup before return
        temp = temp.trim(); // Must trim before substring or else deletes string
        temp = temp.substring(0, temp.indexOf(" ")).replace(",", "");
        if(temp.contains("&nbsp;")) temp = temp.substring(0, temp.indexOf("&nbsp;"));
        if(temp.contains("</")) temp = temp.substring(0, temp.indexOf("</"));
        return temp;
    }

    // A method to get the parties of the court case
    public static String getParty(String string, String party){
        String temp;
        if (string.contains(party)) {
            temp = string.substring(string.indexOf(party) + party.length());
            if (temp.toLowerCase().contains("не явился") || temp.toLowerCase().contains("не явились")) {
                return "не явился";
            }
            else{
                // TODO Split, regex to find element with initials "1.2" (finish)
                String[] temparr = temp.split(" ");
                for(int i = 0; i < temparr.length; i++){
                    if(temparr[i].matches("(^([А-Я]\\.)+$)")){
                        // Finish here
                    }
                }
                return temp;
            }
        }
        return "";
    }
}
