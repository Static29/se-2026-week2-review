# Example 문자열 뒤집기 코드 오류 검토

## 1. 검토 대상

```java
import java.util.ArrayList;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        Example e = new Example();
        String s = e.reverse("abc", new ArrayList<>());
        System.out.println(s);
    }

    public String reverse(String str, List<String> list) {
        if (list == null)
            list = new ArrayList<>();
        if (str == null)
            return "str must not be null";

        if (str.length() <= 0) {
            StringBuffer sb = new StringBuffer();
            for (String s : list) {
                sb.append(s);
            }
            return sb.toString();
        }
        list.add(str.substring(str.length() - 1));
        return reverse(str.substring(0, str.length() - 1), list);
    }
}
```

위 `main()`은 정상적으로 `cba`를 출력한다.
조원 모두 유사한 코드 제출

## 2. 우리가 공통으로 생각한 오류 후보

공통으로 검토한 항목은 **널 포인터 문제**와 **스택·재귀 종료 조건 문제**이다. 다만 오류 후보로 생각한 것과 현재 코드에서 실제 발생하는 것은 구분해야 한다.

### 2.1 널 포인터 — 현재 코드에서는 방어됨

- `list == null`이면 새 `ArrayList`를 생성한다.
- `str == null`이면 `str.length()`를 호출하기 전에 오류 메시지를 반환한다.
- 따라서 이 두 인자에 `null`을 전달하는 것만으로 `NullPointerException`이 발생하지는 않는다.

```java
e.reverse("abc", null); // "cba"
e.reverse(null, null);  // "str must not be null"
```

다만 오류 메시지를 정상 결과와 같은 문자열로 반환하므로 호출자가 성공과 실패를 구분하기 어렵다는 설계 문제가 있다.

### 2.2 스택·종료 조건 — 종료 조건은 정상이나 스택 초과 가능

- 재귀 호출마다 문자열 길이가 1씩 줄어든다.
- 길이가 0이 되면 결과를 반환하므로 종료 조건 자체는 올바르다.
- 빈 문자열은 `substring()` 실행 전에 처리하므로 현재 흐름에서 인덱스 범위를 벗어나지 않는다.
- 하지만 길이 `n`의 문자열은 최초 호출을 포함해 약 `n + 1`개의 호출이 쌓인다.
- Java는 이 꼬리 재귀를 자동으로 반복문으로 바꿔 주지 않으므로 긴 입력에서는 `StackOverflowError`가 발생할 수 있다.

즉, **종료 조건 누락에 의한 무한 재귀가 아니라, 종료 조건에 도달하기 전에 호출 스택이 부족해지는 문제**이다. 발생하는 입력 길이는 JVM 설정과 실행 환경에 따라 달라진다.

## 3. 추가로 발생할 수 있는 오류와 문제

아래 항목은 주어진 `main()`에서 모두 발생한다는 뜻이 아니라, 입력·사용 방식·실행 환경에 따라 발생할 수 있다는 의미이다.

| 항목 | 발생 조건 및 원인 | 결과 |
| --- | --- | --- |
| 컴파일 오류 | 마크다운 코드 블록 표시인 백틱을 실제 Java 파일에 포함하거나, 일반적인 파일 기반 컴파일에서 파일 이름을 `Example.java`로 지정하지 않은 경우 | 컴파일 실패 |
| `StackOverflowError` | 긴 문자열을 재귀로 처리하여 호출 스택 한계를 초과한 경우 | 실행 중 오류 |
| `OutOfMemoryError` | 큰 입력에서 반복적인 부분 문자열 생성 등으로 사용 가능한 메모리를 초과한 경우 | 실행 중 오류. 스택 오류가 먼저 발생할 수도 있음 |
| `UnsupportedOperationException` | 원소 추가를 지원하지 않는 리스트에 비어 있지 않은 문자열을 처리하는 경우 | `list.add()`에서 예외 |
| 기존 리스트 내용 혼입 | 전달한 리스트에 이미 원소가 있는 경우 | 기존 원소가 뒤집은 문자열 앞에 붙음 |
| 리스트 재사용에 따른 누적 | 같은 리스트로 함수를 여러 번 호출한 경우 | 이전 호출의 결과까지 누적됨 |
| 외부 리스트 변경 | 전달받은 리스트 자체에 문자를 추가함 | 호출자의 데이터가 변경되며, 중간 실패 시 일부만 추가된 상태가 남을 수 있음 |
| 유니코드 문자 손상 | 이모지 등의 UTF-16 서로게이트 쌍을 코드 단위별로 뒤집음 | 문자가 깨지거나 잘못 표시될 수 있음 |
| 오류 반환 방식의 모호함 | `str == null`일 때 오류 메시지를 문자열로 반환함 | 정상 결과와 실패를 반환 타입으로 구분할 수 없음 |
| 의도하지 않은 `"null"` 포함 | 기존 리스트에 `null` 원소가 있음 | 예외 대신 결과에 `"null"`이라는 글자가 포함됨 |
| 동시성 문제 | 여러 스레드가 같은 `ArrayList`를 동시에 수정함 | 원소 누락·순서 혼란 등이 생기거나, 순회 중 `ConcurrentModificationException`이 발생할 수 있음 |

### 3.1 추가할 수 없는 리스트

```java
e.reverse("abc", List.of()); // 수정 불가능한 리스트, Java 9 이상
e.reverse("abc", java.util.Arrays.asList("x")); // 크기 고정 리스트
```

두 경우 모두 `add()`를 지원하지 않아 `UnsupportedOperationException`이 발생한다. 문자열이 빈 문자열이거나 `null`이면 `add()`에 도달하지 않아 해당 예외가 발생하지 않는다.

### 3.2 기존 값 혼입과 재사용

```java
List<String> list = new ArrayList<>();
list.add("hello");

System.out.println(e.reverse("abc", list)); // hellocba
System.out.println(e.reverse("xy", list));  // hellocbayx
```

문자열만 뒤집는 것이 목적이라면 이러한 결과는 논리적 오류이다.

### 3.3 유니코드 문제

Java 문자열의 인덱스와 `length()`는 UTF-16 코드 단위를 기준으로 한다. 일부 이모지는 두 코드 단위로 표현되므로 현재 코드는 그 내부 순서까지 뒤집는다.

```java
e.reverse("A😀B", new ArrayList<>());
```

이 경우 이모지가 정상적으로 보존되지 않을 수 있다. 결합 문자나 복합 이모지를 화면에 보이는 문자 단위로 뒤집으려면 별도의 처리가 필요하다.

### 3.4 시간과 메모리 비용

현대 Java에서 `substring(0, str.length() - 1)`은 부분 문자열 내용을 복사한다. 복사하는 양은 대략 다음과 같다.

```text
(n - 1) + (n - 2) + ... + 1 = n(n - 1) / 2
```

따라서 시간과 누적 메모리 할당량이 O(n²)까지 증가한다. 누적 할당량이 항상 동시에 점유하는 메모리 크기와 같다는 뜻은 아니며, 실제 메모리 부족 여부는 실행 환경과 가비지 컬렉션에도 영향을 받는다.

## 4. 검토 결론

- **널 포인터:** 공통으로 검토한 후보이지만, 현재 코드의 인자 `null` 처리는 방어되어 있다.
- **종료 조건:** 문자열이 줄어들고 빈 문자열에서 반환하므로 정상이다.
- **스택 오류:** 종료 조건이 올바르더라도 긴 입력에서 `StackOverflowError`는 발생할 수 있다.
- **추가 문제:** 리스트의 수정 가능 여부, 기존 내용과 재사용, 메모리 비용, 유니코드 처리, 동시 사용 등을 확인해야 한다.

## 5. 간단한 개선 예시

문자열 뒤집기만 필요하다면 누적용 리스트와 재귀를 제거할 수 있다.

```java
public String reverse(String str) {
    if (str == null) {
        throw new IllegalArgumentException("str must not be null");
    }
    return new StringBuilder(str).reverse().toString();
}
```

호출부도 `e.reverse("abc")`로 변경한다. 이 구현은 일반적으로 O(n) 시간과 O(n) 공간을 사용하며, 재귀 스택과 외부 리스트 변경 문제가 없다. 유효한 서로게이트 쌍도 보존한다. 다만 결합 문자나 복합 이모지까지 화면에 보이는 문자 단위로 뒤집는 것은 별도 처리가 필요하다.
