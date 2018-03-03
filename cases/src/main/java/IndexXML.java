import org.apache.solr.common.SolrInputDocument;

import java.io.File;

import org.apache.solr.common.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.*;

// Class to index documents into Solr
public class IndexXML implements Runnable {

    private File file;
    SolrInputDocument ret = new SolrInputDocument();

    public IndexXML(File file) {
        this.file = file;
    }

    public void run() {
        try {
//            String urlString = "http://localhost:8983/solr/test";
//            SolrClient solr = new HttpSolrClient.Builder(urlString).build();

            // XML Document Built
            Document xmlDoc = buildXMLDoc(file.getAbsolutePath());
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
            ret.addField("Plaintiff", getParty(cdata, new String[] {"истца","заявителя", "истец", "заявитель"}));
            ret.addField("Defendant", getParty(cdata, new String[] {"ответчика", "ответчик"}));
            ret.addField("Third Party", getParty(cdata, new String[] {"3-его лица","от третьего лица", "3-ое лицо", "третье лицо"}));

            // Financials
            ret.addField("Total amount sought", getRubles(cdata, new String[] {"o взыскании","сумме"}));
            ret.addField("Interest and Penalties", getRubles(cdata, new String[] {"взыскании штраф"}));
//            ret.addField("Amount Awarded", getRubles(cdata, "взыскании штраф"));

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
            System.out.println("Date: " + ret.getFieldValue("Date"));
            System.out.println("Result: " + ret.getFieldValue("Result"));
            System.out.println("Region: " + ret.getFieldValue("Region"));
            System.out.println("Court: " + ret.getFieldValue("Court"));
            System.out.println("Judge: " + ret.getFieldValue("Judge"));
            System.out.println("Plaintiff: " + ret.getFieldValues("Plaintiff"));
            System.out.println("Defendant: " + ret.getFieldValues("Defendant"));
            System.out.println("Third Party: " + ret.getFieldValues("Third Party"));
            System.out.println("Total amount sought: " + ret.getFieldValue("Total amount sought"));
            System.out.println("Penalties: ");
//            System.out.println("Amount awarded: ");
//            System.out.println("Win/Loss: ");
            System.out.println(file.getName() + " in category of interest!");
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
    private Document buildXMLDoc(String docString) {
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
    private String getRubles(String string, String[] chunkIdentifiers){
        for(int a = 0; a < chunkIdentifiers.length; a++){
            // Grab chunks of text containing string, use 200 character buffer
            ArrayList<String> chunks = new ArrayList<>();
            int currOcc = string.toLowerCase().indexOf(chunkIdentifiers[a]);
            while (currOcc >= 0) {
                chunks.add(string.substring(currOcc, currOcc + 201));
                currOcc = string.toLowerCase().indexOf(chunkIdentifiers[a], currOcc + chunkIdentifiers[a].length());
            }
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                String[] split = chunk.split("\\s+|\\h+");
                String toReturn = "";
                for(int j = 0; j < split.length; j++){
                    if(!chunkIdentifiers[a].contains(split[j])){
                        if(split[j].contains("руб") && split[j].matches("\\d+.+")){
                            toReturn += split[j].substring(0, split[j].indexOf("р"));
                            break;
                        }
                        else if(split[j].contains("руб")){
                            break;
                        }
                        else{
                            toReturn += split[j];
                        }
                    }
                }
                return stringCleanup(toReturn).replaceAll("[^\\d.]", "");
            }
        }
        return "";
    }

    // A method to get the result of a court case
    private String getResult(String string){
        return null;
    }

    // A method to get the parties of the court case
    private ArrayList<String> getParty(String string, String[] possibleNames){

        // List for storing multiples names found
        ArrayList<String> people = new ArrayList<>();

        // For each possible name of the party (until found)
        for(int x = 0; x < possibleNames.length && people.size() == 0; x++){

            // Create target area and clean up
            String temp = string;
            String party = possibleNames[x];
            temp = stringCleanup(temp);

            // If the cdata contains the party name try to grab the first occurrence, otherwise, go to next possible name
            if (temp.toLowerCase().contains(party)){

                // Make sure to cut before another party is mentioned
                temp = string.substring(string.toLowerCase().indexOf(party) + party.length(), string.toLowerCase().indexOf(party) + 500);
                temp = stringCleanup(temp);
                temp = removeOthers(temp);

                // For each line in search region
                String[] lines = temp.split("\\r?\\n");
                for(String line:lines){

                    // Look for initials signifying a party, add each one
                    String[] tempArr = line.split(" ");
                    Pattern pattern1 = Pattern.compile("([А-Я]\\s*\\.\\s*[А-Я]\\s*\\.)\\s*.*");
                    for(int i = 0; i < tempArr.length; i++){
                        Matcher matcher = pattern1.matcher(tempArr[i]);
                        if(matcher.find()){
                            people.add(tempArr[i-1] + " " + matcher.group(1));
                        }
                    }

                    // Look for full name
                    Pattern pattern2 = Pattern.compile("([А-Я]+[а-я]+\\s[А-Я]+[а-я]+\\s[А-Я]+[а-я]+)");
                    Matcher matcher2 = pattern2.matcher(line);
                    while(matcher2.find()){
                        people.add(matcher2.group(1));
                    }

                    // Look for signifier for not showing up
                    Pattern pattern3 = Pattern.compile("не явился|не явились");
                    Matcher matcher3 = pattern3.matcher(line);
                    while(matcher3.find()){
                        people.add(matcher3.group(0));
                    }
                }
            }
        }
        return people;
    }

    // A method to cleanup XML before indexing
    private String stringCleanup(String temp){
        if(temp.contains(",")) temp = temp.replaceAll(",", "");
        if(temp.contains("&nbsp;")) temp = temp.replaceAll("&nbsp;", "");
        if(temp.contains("–")) temp = temp.replaceAll("–", "");
        if(temp.contains("-")) temp = temp.replaceAll("-", "");
        if(temp.contains("<u>")) temp = temp.replaceAll("<u>", "");
        if(temp.contains("_")) temp = temp.replaceAll("_", "");
        if(temp.contains("<span>")) temp = temp.replaceAll("<span>", "");
        if(temp.contains("</span>")) temp = temp.replaceAll("</span>", "");
        if(temp.contains("у  с  т  а  н  о  в  и  л :")) temp = temp.substring(0, temp.indexOf("у  с  т  а  н  о  в  и  л :"));
        if(temp.contains("у  с  т  а  н  о  в  и  л  :")) temp = temp.substring(0, temp.indexOf("у  с  т  а  н  о  в  и  л  :"));
        if(temp.contains("У  С  Т  А  Н  О  В  И  Л :")) temp = temp.substring(0, temp.indexOf("У  С  Т  А  Н  О  В  И  Л :"));
        temp = temp.trim();
        return temp;
    }

    // Remove other names
    private String removeOthers(String temp){
        if(temp.toLowerCase().contains("ответчик")) temp = temp.substring(0, temp.toLowerCase().indexOf("ответчик"));
        if(temp.toLowerCase().contains("истец")) temp = temp.substring(0, temp.toLowerCase().indexOf("истец"));
        if(temp.toLowerCase().contains("истца")) temp = temp.substring(0, temp.toLowerCase().indexOf("истца"));
        if(temp.toLowerCase().contains("от трет")) temp = temp.substring(0, temp.toLowerCase().indexOf("от трет"));
        if(temp.toLowerCase().contains("от 3-его")) temp = temp.substring(0, temp.toLowerCase().indexOf("3-его"));
        if(temp.toLowerCase().contains("судь")) temp = temp.substring(0, temp.toLowerCase().indexOf("судь"));
        return temp;
    }
}
