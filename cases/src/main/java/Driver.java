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

public class Driver {

    public static void main(String[] args) {
        XML.addRoot();
    }
}
