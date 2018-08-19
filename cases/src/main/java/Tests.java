import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Scanner;

public class Tests {
    static Scanner stdin = new Scanner(System.in);
    static int count = 0;

    public static void main(String[] args){
        recurseTree();
    }

    public static void recurseTree() {
        System.out.print("Please enter the FULL path of the root directory: ");
        recurseTreeHelper(new File(stdin.nextLine()));
    }

    private static void recurseTreeHelper(File root) {
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                recurseTreeHelper(child);
            }
        }
        else{
            try{
                Document xmlDoc = buildXMLDoc(root.getAbsolutePath());
                String cdata = null;

                // Store CData
                NodeList bodyNodes = xmlDoc.getElementsByTagName("body");
                for (int i = 0; i < bodyNodes.getLength(); i++) {
                    Element e = (Element) bodyNodes.item(i);
                    cdata = e.getTextContent().trim();
                }
                NodeList categoryNodes = xmlDoc.getElementsByTagName("category");
                String category = "";
                category = categoryNodes.item(0).getTextContent().trim();


                if(cdata.toLowerCase().contains("энергоснабжения") || category.toLowerCase().contains("энергоснабжения")){
                    count++;
                    System.out.println(count);
                }
            }
            catch (Exception e) {

            }
        }

    }

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
}
