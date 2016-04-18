# RegexParser
## Test

```java
import org.util.text.regex.ParseTree;
import org.util.text.regex.Parser;

...

String regex = "£*(ab+|ca*(€$+|a)+c)";
ParseTree tree = Parser.parse(regex);
System.out.println(tree);
```

Output :

```
+-: r22
    |
    +-: r19
    |   |
    |   +-: r18
    |   |   |
    |   |   +-: r17
    |   |   |   |
    |   |   |   +-: c
    |   |   |
    |   |   +-: r16
    |   |       |
    |   |       +-: r11
    |   |       |   |
    |   |       |   +-: +
    |   |       |   |
    |   |       |   +-: r6
    |   |       |       |
    |   |       |       +-: r5
    |   |       |       |   |
    |   |       |       |   +-: a
    |   |       |       |
    |   |       |       +-: |
    |   |       |       |
    |   |       |       +-: r4
    |   |       |           |
    |   |       |           +-: r2
    |   |       |           |   |
    |   |       |           |   +-: +
    |   |       |           |   |
    |   |       |           |   +-: r1
    |   |       |           |       |
    |   |       |           |       +-: $
    |   |       |           |
    |   |       |           +-: r3
    |   |       |               |
    |   |       |               +-: €
    |   |       |
    |   |       +-: r15
    |   |           |
    |   |           +-: r10
    |   |           |   |
    |   |           |   +-: *
    |   |           |   |
    |   |           |   +-: r9
    |   |           |       |
    |   |           |       +-: a
    |   |           |
    |   |           +-: r14
    |   |               |
    |   |               +-: c
    |   |
    |   +-: |
    |   |
    |   +-: r13
    |       |
    |       +-: r8
    |       |   |
    |       |   +-: +
    |       |   |
    |       |   +-: r7
    |       |       |
    |       |       +-: b
    |       |
    |       +-: r12
    |           |
    |           +-: a
    |
    +-: r21
        |
        +-: *
        |
        +-: r20
            |
            +-: £
```
