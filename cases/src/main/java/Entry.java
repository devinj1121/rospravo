import java.util.ArrayList;

public class Entry {
    private String category;
    private String result;
    private String date;
    private String region;
    private String court;
    private String judge;
    private String casenumber;
    private String expedited;
    private String plaintiff;
    private String defendant;
    private ArrayList<String> plaintiffreps;
    private ArrayList<String> defendantreps;
    private String amountsought;
    private String interest;
    private String penalties;
    private String amountawarded;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }

    public String getJudge() {
        return judge;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }

    public String getCasenumber() {
        return casenumber;
    }

    public void setCasenumber(String casenumber) {
        this.casenumber = casenumber;
    }

    public String getExpedited() {
        return expedited;
    }

    public void setExpedited(String expedited) {
        this.expedited = expedited;
    }

    public String getPlaintiff() {
        return plaintiff;
    }

    public void setPlaintiff(String plaintiff) {
        this.plaintiff = plaintiff;
    }

    public String getDefendant() {
        return defendant;
    }

    public void setDefendant(String defendant) {
        this.defendant = defendant;
    }

    public ArrayList<String> getPlaintiffreps() {
        return plaintiffreps;
    }

    public void setPlaintiffreps(ArrayList<String> plaintiffreps) {
        this.plaintiffreps = plaintiffreps;
    }

    public ArrayList<String> getDefendantreps() {
        return defendantreps;
    }

    public void setDefendantreps(ArrayList<String> defendantreps) {
        this.defendantreps = defendantreps;
    }

    public String getAmountsought() {
        return amountsought;
    }

    public void setAmountsought(String amountsought) {
        this.amountsought = amountsought;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getPenalties() {
        return penalties;
    }

    public void setPenalties(String penalties) {
        this.penalties = penalties;
    }

    public String getAmountawarded() {
        return amountawarded;
    }

    public void setAmountawarded(String amountawarded) {
        this.amountawarded = amountawarded;
    }
}
