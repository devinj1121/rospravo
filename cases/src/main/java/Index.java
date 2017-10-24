import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;



public class Index {


    public static void main(String args[]){

        String urlString = "http://localhost:8983/solr/test";
        SolrClient solr = new HttpSolrClient.Builder(urlString).build();


        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", "123456");
        document.addField("name", "Kenmore Dishwasher");
        document.addField("price", "599.99");

        try{
            solr.add(document);
            solr.commit();
            solr.deleteById("123456");
            solr.commit();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }


    }


}
