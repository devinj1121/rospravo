import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;


// A class with various methods for handling the xml in this project
public class ProcessXML implements Runnable{
    private File file;
    public ProcessXML(File file) {
        this.file = file;
    }
    public void run() {
        // process the file

    }
//    static Scanner stdin = new Scanner(System.in);
//
//    public static void main(String[] args){
//        boolean done = false;
//        System.out.println("Please select an (integer) option:\n\t1: Unzip\n\t2: Normalize");
//        do{
//            try{
//                int choice = stdin.nextInt();
//                switch (choice) {
//                    case 1:
//                        stdin.nextLine();
//                        unzip();
//                        done = true;
//                        break;
//                    case 2:
//                        stdin.nextLine();
//                        normalize();
//                        done = true;
//                        break;
//                    default: System.out.print("Enter an integer 1-2: ");
//                }
//            }
//            catch(InputMismatchException e){
//                System.out.print("Enter an integer 1-2: ");
//                stdin.nextLine(); // Clear the scanner
//            }
//        }
//        while(!done);
//
//    }
//    // Recursive method which unzips a file or file tree
//    public static void unzip(){
//        System.out.print("Please enter the FULL path of the root directory: ");
//        unzipHelper(new File(stdin.nextLine()));
//    }
//    private static void unzipHelper(File root){
//
//        File[] directoryListing = root.listFiles();
//        if (directoryListing != null) {
//            for (File child : directoryListing) {
//                unzipHelper(child);
//            }
//        }
//        else {
//            byte[] buffer = new byte[1024];
//            try{
//                GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(root));
//                FileOutputStream out = new FileOutputStream(new File(root.getAbsolutePath().replace(".gz", "")));
//                int len;
//                while ((len = gzis.read(buffer)) > 0) {
//                    out.write(buffer, 0, len);
//                }
//                gzis.close();
//                out.close();
//                root.delete();
//                System.out.println(root.getName() + " unzipped.");
//            }
//            catch(IOException ex){
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    // Formats XML for parsing
//    public static void normalize() {
//        System.out.print("Please enter the FULL path of the root directory: ");
//        normalizeHelper(new File(stdin.nextLine()));
//    }
//
//    private static void normalizeHelper(File root){
//        File[] directoryListing = root.listFiles();
//        if (directoryListing != null) {
//            for (File child : directoryListing) {
//                normalizeHelper(child);
//            }
//        }
//        else {
//            // Read file into scanner
//            final List<String> lines = new ArrayList<>();
//            try (Scanner in = new Scanner(root)) {
//                while (in.hasNextLine())
//                    lines.add(in.nextLine());
//            }
//            catch(IOException ex){
//                ex.printStackTrace();
//            }
//
//            // Normalize
//            try (PrintStream out = new PrintStream(root)) {
//                char current;
//                out.println("<root>");
//                for (String line : lines) {
//                    out.print("    ");
//                    for (int i = 0; i < line.length(); i++) {
//                        current = line.charAt(i);
//                        if ((current == 0x9) ||
//                                (current == 0xA) ||
//                                (current == 0xD) ||
//                                ((current >= 0x20) && (current <= 0xD7FF)) ||
//                                ((current >= 0xE000) && (current <= 0xFFFD)) ||
//                                ((current >= 0x10000) && (current <= 0x10FFFF))){
//                            out.print(Character.toString(current));
//                        }
//                    }
//                    out.println();
//                }
//                out.println("</root>");
//                out.close();
//            }
//            catch(Exception ex){
//                ex.printStackTrace();
//            }
//            System.out.println(root.getName() + " normalized.");
//        }
//    }
}
