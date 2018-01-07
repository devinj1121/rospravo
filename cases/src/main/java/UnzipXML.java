import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class UnzipXML implements Runnable {

    private File file;

    public UnzipXML(File file) {
        this.file = file;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
            FileOutputStream out = new FileOutputStream(new File(file.getAbsolutePath().replace(".gz", "")));
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzis.close();
            out.close();
            file.delete();
            System.out.println(file.getName() + " unzipped.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
