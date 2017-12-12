import jdk.internal.util.xml.impl.Input;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;


// A class with various methods for handling the xml in this project
public class XML {

    static Scanner stdin = new Scanner(System.in);

    // Recursive method which unzips a file or file tree
    public static void unzip(){
        System.out.print("Please enter the FULL path of the root directory: ");
        unzipHelper(new File(stdin.nextLine()));
    }
    private static void unzipHelper(File root){

        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                unzipHelper(child);
            }
        }
        else {
            byte[] buffer = new byte[1024];
            try{
                GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(root));
                FileOutputStream out = new FileOutputStream(new File(root.getAbsolutePath().replace(".gz", "")));
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                gzis.close();
                out.close();
                root.delete();
                System.out.println(root.getName() + " unzipped.");
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    // Recursively adds root elements to xml files (if missing)
    public static void addRoot() {
        System.out.print("Please enter the FULL path of the root directory: ");
        addRootHelper(new File(stdin.nextLine()));

    }

    private static void addRootHelper(File root){
        FileInputStream fis;
        List<InputStream> streams;
        InputStream is;
        OutputStream os;

        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                addRootHelper(child);
            }
        }
        else {
            // Read file into scanner
            final List<String> lines = new ArrayList<>();
            try (Scanner in = new Scanner(root)) {
                while (in.hasNextLine())
                    lines.add(in.nextLine());
            }
            catch(IOException ex){
                ex.printStackTrace();
            }

            // Add root to file
            try (PrintStream out = new PrintStream(root)) {
                out.println("<root>");
                for (String line : lines) {
                    out.print("    ");
                    out.println(line);
                }
                out.println("</root>");
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    // A method to parse a court XML file and get the category tag
    public static String getCategory(File file) {
        try {
            InputStream in = new FileInputStream(file);
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
                        continue;
                    }
                }
            }
            return curr;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
