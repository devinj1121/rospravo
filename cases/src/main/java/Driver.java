import java.io.File;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {

    static Scanner stdin = new Scanner(System.in);
    static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        boolean done = false;
        System.out.println("Please select an (integer) option:\n\t1: Process\n\t2: Index");
        do{
            try{
                int choice = stdin.nextInt();
                switch (choice) {
                    case 1:
                        stdin.nextLine();
                        ProcessXML();
                        done = true;
                        break;
                    case 2:
                        stdin.nextLine();
                        Index();
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

    public static void ProcessXML(){
        System.out.print("Please enter the FULL path of the root directory: ");
        ProcessXMLHelper(new File(stdin.nextLine()));
    }
    private  static void ProcessXMLHelper(File root) {
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                ProcessXMLHelper(child);
            }
        }
        else {
            threadPool.submit(new ProcessXML(root));
        }
    }

    public static void Index(){
        System.out.print("Please enter the FULL path of the root directory: ");
        IndexHelper(new File(stdin.nextLine()));
    }
    private static void IndexHelper(File root){
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                ProcessXMLHelper(child);
            }
        }
        else {
            threadPool.submit(new Index(root));
        }
    }
}
