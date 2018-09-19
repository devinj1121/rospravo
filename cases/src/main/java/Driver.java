import java.io.File;
import java.io.FileWriter;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {

    static Scanner stdin = new Scanner(System.in);
    static ExecutorService threadPool = Executors.newFixedThreadPool(1);
    static File outputFile = new File(System.getProperty("user.home") + "/Desktop/output.csv");
    final static String CSV_HEADER = "file;date;caseNum;result;region;court;judge;plaintiff;plaintReps;defendant;defReps;amountSought;amountAwarded;expedited;breaks";

    public static void main(String[] args) {
        boolean done = false;
        System.out.println("Please select an (integer) option:\n\t1: Normalize XML Files\n\t2: Create CSV");
        do {
            try {
                // Make sure valid input
                int option = stdin.nextInt();
                if(option < 1 || option > 2){
                    throw new InputMismatchException();
                }
                // Clear the scanner and signal that input loop is done, if option 2 make a CSV
                stdin.nextLine();
                done = true;
                if(option == 2){
                    try{
                        FileWriter fw = new FileWriter(outputFile, false);
                        fw.append(CSV_HEADER + "\n");
                        fw.flush();
                        fw.close();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                // Start recursion of file tree
                System.out.print("Please enter the FULL path of the root directory: ");
                recurseTree(new File(stdin.nextLine()), option);
            }
            catch (InputMismatchException e) {
                System.out.print("Enter an integer 1-2: ");
                stdin.nextLine();
            }
        }
        while(!done);
        threadPool.shutdown();
    }

    public static void recurseTree(File root, int option) {
            if(root.listFiles() != null){
                for(File child : root.listFiles()){
                    recurseTree(child, option);
                }
            }
            switch(option) {
                case 1:
                    threadPool.submit(new NormalizeXML(root));
                    break;
                case 2:
                    threadPool.submit(new IndexXML(root));
                    break;
            }
        }
}
