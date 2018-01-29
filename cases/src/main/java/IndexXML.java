import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import java.io.BufferedWriter;
import java.io.File;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

            // Region
            NodeList regionNodes = xmlDoc.getElementsByTagName("region");
            for (int i = 0; i < regionNodes.getLength(); i++) {
                ret.addField("Region", regionNodes.item(i).getTextContent().trim());
                break;
            }

            // Court
            NodeList courtNodes = xmlDoc.getElementsByTagName("court");
            for (int i = 0; i < courtNodes.getLength(); i++) {
                ret.addField("Court", courtNodes.item(i).getTextContent().trim());
                break;
            }

            // Judge
            NodeList judgeNodes = xmlDoc.getElementsByTagName("judge");
            for (int i = 0; i < judgeNodes.getLength(); i++) {
                ret.addField("Judge", judgeNodes.item(i).getTextContent().trim());
                break;
            }

            // Date
            NodeList dateNodes = xmlDoc.getElementsByTagName("date");
            for (int i = 0; i < dateNodes.getLength(); i++) {
                ret.addField("Date", dateNodes.item(i).getTextContent().trim());
                break;
            }

            // Parties
            ret.addField("Plaintiff", getParty(cdata, new String[] {"истца", "заявителя"}));
            ret.addField("Defendant", getParty(cdata, new String[] {"ответчика"}));
            ret.addField("Third Party", getParty(cdata, new String[] {"3-его лица", "от третьего лица"}));

            // TODO Amount sought, interest, penalties
            ret.addField("Amount Sought", getRubles(cdata, "о взыскании"));
            ret.addField("Interest and Penalties", getRubles(cdata, "взыскании штраф"));

            // TODO Amount Awarded

            // TODO Win or Loss (account for amount won/lost)
            NodeList resultNodes = xmlDoc.getElementsByTagName("result");
            String result = "";
            for (int i = 0; i < resultNodes.getLength(); i++) {
                result = resultNodes.item(i).getTextContent().trim();
                break;
            }
            ret.addField("Result", result);

            // Add Solr doc
//            solr.add(ret);
//            solr.commit();
//            System.out.println("Date: " + ret.getFieldValue("Date"));
//            System.out.println("Result: " + ret.getFieldValue("Result"));
//            System.out.println("Region: " + ret.getFieldValue("Region"));
//            System.out.println("Court: " + ret.getFieldValue("Court"));
//            System.out.println("Judge: " + ret.getFieldValue("Judge"));
//            System.out.println("Plaintiff: " + ret.getFieldValue("Plaintiff"));
//            System.out.println("Defendant: " + ret.getFieldValue("Defendant"));
//            System.out.println("Third Party: " + ret.getFieldValue("Third Party"));
//            System.out.println("Amount sought: " + ret.getFieldValue("Amount Sought"));
//            System.out.println("Penalties: ");
//            System.out.println("Amount awarded: ");
//            System.out.println("Win/Loss: ");
              System.out.println();

//            File file = new File("/home/dj1121/Desktop/test.txt");
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
//            writer.append(' ');
//            writer.append(ret.getFieldValue("Result") + "\n");
//            writer.close();


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
    private static String getRubles(String string, String chunkIdentifier){
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
                    if(split[j-2].matches("-?\\d+")){
                        return stringCleanup(split[j-2] + "" + split[j-1]);
                    }
                    return stringCleanup(split[j - 1]);
                }
            }
        }
        return "";
    }

    // A method to get the location of a court case if not listed in header
    private static String getLocation(String string){
        String temp = "";
        if(string.contains("г</span><span>.")) temp = string.substring(string.indexOf("г</span><span>.") + 15);
        else if(string.contains("г.")) temp = string.substring(string.indexOf("г.") + 2);
        else return "";
        temp = stringCleanup(temp);
        if(temp.contains(" ")) temp = temp.substring(0,temp.indexOf(" "));
        return temp;
    }

    // A method to get the result of a court case
    private static String getResult(String string){
        return null;
    }

    // A method to get the parties of the court case
    private static String getParty(String string, String[] possibleNames){
        // For each possible name of the party
        for(int x = 0; x < possibleNames.length; x++){
            String temp = "";
            String party = possibleNames[x];
            // If the cdata contains the party name try to grab the first occurrence, otherwise, go to next possible name
            if (string.toLowerCase().contains(party)){
                // Clean up the string first, make sure to cut before another party is mentioned
                temp = string.substring(string.toLowerCase().indexOf(party) + party.length());
                temp = stringCleanup(temp);
                if(temp.contains(" от ")) temp = temp.substring(0, temp.indexOf(" от "));

                // Look for initials signifying a party
                String[] tempArr = temp.split(" ");
                for(int i = 0; i < tempArr.length; i++){
                    if(tempArr[i].matches("^[А-Я]\\.[А-Я]\\.$")){
                        temp = tempArr[i-1] + " " + tempArr[i];
                        return temp;
                    }
                }
                // If no initials found, look for full name
                Pattern pattern = Pattern.compile("([А-Я]+[а-я]+)\\s([А-Я]+[а-я]+)\\s([А-Я]+[а-я]+)");
                Matcher matcher = pattern.matcher(temp);
                if (matcher.find()){
                    return matcher.group(0);
                }

                // If not found, look for signifier for not showing up
                if (temp.toLowerCase().contains("не явился") || temp.toLowerCase().contains("не явились")) {
                    return "не явился";
                }
            }
        }
        // If loop finishes, none of the information was found
        return "NOT LISTED";
    }

    // A method to cleanup XML before indexing
    private static String stringCleanup(String temp){
        if(temp.contains(",")) temp = temp.replaceAll(",", "");
        if(temp.contains("&nbsp;")) temp = temp.replaceAll("&nbsp;", "");
        if(temp.contains("–")) temp = temp.replaceAll("–", "");
        if(temp.contains("-")) temp = temp.replaceAll("-", "");
        if(temp.contains("</")) temp = temp.substring(0, temp.indexOf("</"));
        temp = temp.trim();
        return temp;
    }
}
