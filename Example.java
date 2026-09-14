import java.util.ArrayList;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        Example e = new Example();
        String result = e.reverse("abc", new ArrayList<>());
        System.out.println(result); // cba
    }

    public String reverse(String str, List<String> list) {
        // 1. null 입력 검사
        if (str == null) {
            throw new IllegalArgumentException("문자열은 null일 수 없습니다.");
        }

        // 2. 수정 가능한 리스트로 복사
        if (list == null) {
            list = new ArrayList<>();
        }

        // 3. 종료 조건
        if (str.isEmpty()) {
            StringBuilder result = new StringBuilder();

            for (String s : list) {
                result.append(s);
            }

            return result.toString();
        }

        // 4. 마지막 문자를 리스트에 추가
        list.add(str.substring(str.length() - 1));

        // 5. 마지막 문자를 제외한 문자열로 재귀 호출
        return reverse(str.substring(0, str.length() - 1), list);
    }
}