import java.util.ArrayList;
import java.util.List;

public class Example {

    public static void main(String[] args) {
        Example e = new Example();
        String s = e.reverse("abc", new ArrayList<>());        
        System.out.println(s);        
    }

    public String reverse(String str, List<String> list) {
        if(list == null)
            list = new ArrayList<>();
        if (str.length() == 0) {  
            StringBuilder sb = new StringBuilder();
            for (String s : list) {
                sb.append(s);
            }
            return sb.toString();          
            
        }
        list.add(str.substring(str.length()-1));
        return reverse(str.substring(0, str.length()-1), list);            
    }
}
