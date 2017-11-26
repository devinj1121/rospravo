import java.io.*;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class Unzip {
    public static void main(String[] args){
        // Get file
        Scanner stdin = new Scanner(System.in);
        System.out.print("Please enter the FULL path of the root directory: ");
        File root  = new File(stdin.nextLine());
        unzip(root);

    }
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
}
