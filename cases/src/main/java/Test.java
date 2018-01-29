import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String [] args){
        String temp = ": Манихина Галина Владимировна  доверенность № 254с";
        // ([А-Я]+[а-я]+)\s([А-Я]+[а-я]+)\s([А-Я]+[а-я]+)
        // "[а-я]+\\s[а-я]+\\s[а-я]+", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        Pattern pattern = Pattern.compile("([А-Я]+[а-я]+)\\s([А-Я]+[а-я]+)\\s([А-Я]+[а-я]+)");
        Matcher matcher = pattern.matcher(temp);
        if (matcher.find()){
            System.out.println(matcher.group(0));
        }
    }
}
