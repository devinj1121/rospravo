import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;


// A class with various methods for handling the xml in this project
public class XML {

    static Scanner stdin = new Scanner(System.in);

    public static void main(String[] args){
        boolean done = false;
        System.out.println("Please select an (integer) option:\n\t1: Unzip\n\t2: Add Root Element\n\t3: Get Category");
        do{
            try{
                int choice = stdin.nextInt();
                switch (choice) {
                    case 1:
                        stdin.nextLine();
                        unzip();
                        done = true;
                        break;
                    case 2:
                        stdin.nextLine();
                        addRoots();
                        done = true;
                        break;
                    default: System.out.print("Enter an integer 1-2: ");
                }
            }
            catch(InputMismatchException e){
                System.out.print("Enter an integer 1-2: ");
                stdin.nextLine(); // Clear the scanner
            }
        }
        while(!done);

    }
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
    public static void addRoots() {
        System.out.print("Please enter the FULL path of the root directory: ");
        addRootsHelper(new File(stdin.nextLine()));
    }

    private static void addRootsHelper(File root){
        FileInputStream fis;
        List<InputStream> streams;
        InputStream is;
        OutputStream os;

        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                addRootsHelper(child);
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
            System.out.println("Root added to " + root.getName());
        }
    }
}
