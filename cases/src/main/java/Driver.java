import java.io.File;
import java.io.FileWriter;
import java.rmi.server.ExportException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {

    static Scanner stdin = new Scanner(System.in);
    static ExecutorService threadPool = Executors.newFixedThreadPool(1);
    static File outputFile = new File("C:\\Users\\Devin\\Desktop\\output.csv");
    final static String CSV_HEADER = "file;date;caseNum;result;region;court;judge;plaintiff;plaintReps;defendant;defReps;amountSought;amountAwarded;expedited;breaks";

    public static void main(String[] args) {
        boolean done = false;
        System.out.println("Please select an (integer) option:\n\t1: Unzip\n\t2: Normalize\n\t3: Index");
        do {
            try {
                int option = stdin.nextInt();
                if(option < 1 || option > 3){
                    throw new InputMismatchException();
                }
                else{
                    stdin.nextLine();
                    done = true;
                    recurseTree(option);
                }
            }
            catch (InputMismatchException e) {
                System.out.print("Enter an integer 1-3: ");
                stdin.nextLine(); // Clear the scanner
            }
        }
        while (!done);
        threadPool.shutdown();
    }

    public static void recurseTree(int option) {
        System.out.print("Please enter the FULL path of the root directory: ");
        if(option == 3){
            try{
                FileWriter fw = new FileWriter(outputFile, true);
                fw.append(CSV_HEADER + "\n");
                fw.flush();
                fw.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        recurseTreeHelper(new File(stdin.nextLine()), option);
    }

    private static void recurseTreeHelper(File root, int option) {
        File[] directoryListing = root.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                recurseTreeHelper(child, option);
            }
        } else {
            switch(option) {
                case 1:
                    threadPool.submit(new UnzipXML(root));
                    break;
                case 2:
                    threadPool.submit(new NormalizeXML(root));
                    break;
                case 3:
                    threadPool.submit(new IndexXML(root));
                    break;
            }
        }
    }
}
