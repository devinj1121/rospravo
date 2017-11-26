import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

// To print whole input stream
//            java.util.Scanner s = new java.util.Scanner(cntr).useDelimiter("\\A");
//            String hello =  s.hasNext() ? s.next() : "";
//            System.out.println(hello);

public class Filter {


    static ArrayList<File> xmlFiles = new ArrayList<>();

    public static void main(String[] args) {

        // For each file create file and add root to it
        File root;
        String category;

        // Assign root file of directory
        try {
            root = new File("/media/dj1121/Seagate Expansion Drive/arb_sud/4-arbitrazhnyj-apellyacionnyj-sud-40026");
            unzip(root);
//            getFiles(root);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        // For each file add root
    }

    // Method to unzip files in tree
    public static void unzip(File root){
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                unzip(child);
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
    // Method to iterate over file tree
    public static void getFiles(File root){
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                getFiles(child);
            }
        } else {
            xmlFiles.add(root);
        }
    }


    // A method to add a root to the XML file since files are formatted incorrectly
    public static InputStream addRoot(File file){

        FileInputStream fis;
        List<InputStream> streams;
        InputStream cntr;

        try{
            fis = new FileInputStream(file);
            streams =
                    Arrays.asList(
                            new ByteArrayInputStream("<root>".getBytes()),
                            fis,
                            new ByteArrayInputStream("</root>".getBytes()));
            cntr =
                    new SequenceInputStream(Collections.enumeration(streams));
            return cntr;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }


    // A method to parse the XML and get the category tag
    public static String getCategory(InputStream in) {
        try {
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
