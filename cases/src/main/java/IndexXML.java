import org.apache.solr.common.SolrInputDocument;

import java.io.File;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.Arrays;
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
            Entry entry = new Entry();

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
            category = categoryNodes.item(0).getTextContent().trim();

            // Determine if of interest
            // TODO excluding bankruptcy?
            switch(category) {
                case "о взыскании задолженности":
                    entry.setCategory("О взыскании задолженности");
                    break;

                case "о взыскании обязательных платежей":
                    entry.setCategory("О взыскании обязательных платежей");
                    break;

                case "":
                    if (cdata.toLowerCase().contains("о взыскании задолженности") && !cdata.toLowerCase().contains("банкрот")) {
                        entry.setCategory("О взыскании задолженности");
                    } else if (cdata.toLowerCase().contains("о взыскании обязательных платежей") && !cdata.toLowerCase().contains("банкрот")) {
                        entry.setCategory("О взыскании обязательных платежей");
                    } else {
                        return;
                    }
                    break;
                default:
                    return;
            }

            // Region, case number, judge, date, court, result
            NodeList regionNodes = xmlDoc.getElementsByTagName("region");
            NodeList caseNumNodes = xmlDoc.getElementsByTagName("CaseNumber");
            NodeList judgeNodes = xmlDoc.getElementsByTagName("judge");
            NodeList dateNodes = xmlDoc.getElementsByTagName("date");
            NodeList courtNodes = xmlDoc.getElementsByTagName("court");
            NodeList resultNodes = xmlDoc.getElementsByTagName("result");
            entry.setResult(resultNodes.item(0).getTextContent().trim());
            entry.setCourt(courtNodes.item(0).getTextContent().trim());
            entry.setRegion(regionNodes.item(0).getTextContent().trim());
            entry.setJudge(judgeNodes.item(0).getTextContent().trim());
            entry.setDate(dateNodes.item(0).getTextContent().trim());
            entry.setCasenumber(caseNumNodes.item(0).getTextContent().trim());

            // Expedited proceedings
            String proceedings = "";
            if(cdata.contains("упрощенного производства") || cdata.contains("упрощенное производство")){
                proceedings = "True";
            }
            else{
                proceedings = "False";
            }
            entry.setExpedited(proceedings);

            // Reps
            entry.setPlaintiffreps(getReps(cdata, new String[] {"истца","заявителя", "истец", "заявитель"}));
            entry.setDefendantreps(getReps(cdata, new String[] {"ответчика", "ответчик"}));

            // Parties
            String[] parties = getParties(cdata, new String[]{"по иск", "заявлен"});
            if(parties != null){
                entry.setPlaintiff(stringCleanup(parties[0]));
                entry.setDefendant(stringCleanup(parties[1]));
            }

            // Financials
            entry.setAmountsought(getRubles(cdata, new String[] {"взыскании","сумме", "размере"}));
            entry.setInterest(getRubles(cdata, new String[] {"процент"}));
            entry.setPenalties(getRubles(cdata, new String[] {"штраф", "пени", "пеней", "неустойк"}));
            entry.setAmountawarded(getRubles(cdata, new String[] {"взыскании штраф"}));


            // Printout
            System.out.println("Date: " + entry.getDate());
            System.out.println("Case Number: " + entry.getCasenumber());
            System.out.println("Result: " +entry.getResult());
            System.out.println("Region: " + entry.getRegion());
            System.out.println("Court: " + entry.getCourt());
            System.out.println("Judge: " + entry.getJudge());
            System.out.println("Plaintiff: " + entry.getPlaintiff());
            System.out.println("Plaintiff Reps: " + entry.getPlaintiffreps());
            System.out.println("Defendant: " + entry.getDefendant());
            System.out.println("Defendant Reps: " + entry.getDefendantreps());
            System.out.println("Total amount sought: " + entry.getAmountsought());
            System.out.println("Interest: " + entry.getInterest());
            System.out.println("Penalties: " + entry.getPenalties());
            System.out.println("Amount Awarded: " + entry.getAmountawarded());
            System.out.println("Expedited Proceedings: " + entry.getExpedited());
            System.out.println(file.getName() + " in category of interest!");
            System.out.println();

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
        for(int i = 0; i < chunkIdentifiers.length; i++){
            // Grab first chunk of text containing word and rubles
            String chunk = "";
            int currOcc = string.toLowerCase().indexOf(chunkIdentifiers[i]);
            while(true){
                if(currOcc >= 0) {
                    // If found stop
                    if (string.substring(currOcc, currOcc + 201).contains("руб")){
                        chunk = string.substring(currOcc, currOcc + 201);
                        break;
                    }
                    // If not, keep going
                    else{
                        currOcc = string.toLowerCase().indexOf(chunkIdentifiers[i], currOcc + chunkIdentifiers[i].length());
                    }
                }
                else{
                    break;
                }
            }
            String[] split = chunk.split("\\s+|\\h+");
            String toReturn = "";
            for(int j = 0; j < split.length; j++){
                if(!chunkIdentifiers[i].contains(split[j])){
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
            // If something found, return it. If not, go on to next chunk identifier
            if(!toReturn.equals("")){
                return stringCleanup(toReturn).replaceAll("[^\\d.]", "");
            }
        }
        return "";
    }

    private String[] getParties(String string, String[] possibleNames){
        // Search with all names, stop once found
        String parties = "";
        string = string.toLowerCase();
        for(String name : possibleNames){
            if(string.contains(name)){
                String temp = string.substring(string.indexOf(name), string.indexOf(name) + 600);
                if(temp.contains("взыскании")){
                    parties = temp.substring(0, temp.indexOf("взыскании"));
                    break;
                }
                else if(temp.contains("признании")){
                    parties = temp.substring(0, temp.indexOf("признании"));
                    break;
                }

                else{
                    parties = temp;
                    break;
                }
            }
        }
        // Split the string before "k" and after "k"
        String[] split = null;
        Pattern pattern2 = Pattern.compile("[\\s\\xA0]к[\\s\\xA0]|>к<|<к[\\s\\xA0]|[\\s\\xA0]sк>|>к[\\s\\xA0]|[\\s\\xA0]к<");
        Matcher matcher2 = pattern2.matcher(parties);
        if(matcher2.find()){
           split = parties.split("[\\s\\xA0]к[\\s\\xA0]|>к<|<к[\\s\\xA0]|[\\s\\xA0]к>|>к[\\s\\xA0]|[\\s\\xA0]к<");
        }
        return split;
    }

    // A method to get the parties of the court case
    private ArrayList<String> getReps(String string, String[] possibleNames){
        // TODO
        if(file.getName().contains("305734700")){
            System.out.println();
        }
        // List for storing multiples names found
        ArrayList<String> people = new ArrayList<>();

        // For each possible name of the party (until found)
        for(int x = 0; x < possibleNames.length && people.size() == 0; x++){

            // Create target area and clean up
            String temp = string;
            String party = possibleNames[x];
            temp = stringCleanup(temp);
            if(temp.toLowerCase().contains("при участии")){
                temp = temp.substring(temp.toLowerCase().indexOf("при участии"), temp.toLowerCase().indexOf("при участии") + 500);
            }

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

    // A method to cleanup XML before indexing. Handles edge cases, etc.
    private String stringCleanup(String temp){
        // Dashes, brackets, etc.
        temp = temp.replaceAll(",", "");
        temp = temp.replaceAll("&nbsp;", "");
        temp = temp.replaceAll("–", "");
        temp = temp.replaceAll("-", "");
        temp = temp.replaceAll("_", "");
        temp = temp.replaceAll("<[^>]+>|</[^>]+>|/[^>]+>|<. style[^>]+>|<.+", "");
        temp = temp.replaceAll("[\\s\\xA0]+", " ");
        // Check for ending "o"
        String[] tempSplit = temp.split("[\\s\\xA0]");
        if(tempSplit[tempSplit.length -1].equals("о")){
            temp = "";
            for(int i = 0; i < tempSplit.length - 1; i++){
                temp += tempSplit[i] + " ";
            }
        }
        // Cutoff ustanovil
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
