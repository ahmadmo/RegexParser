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
    +-: r21
    |   |
    |   +-: r20
    |   |   |
    |   |   +-: £
    |   |
    |   +-: *
    |
    +-: r19
        |
        +-: r13
        |   |
        |   +-: r12
        |   |   |
        |   |   +-: a
        |   |
        |   +-: r8
        |       |
        |       +-: r7
        |       |   |
        |       |   +-: b
        |       |
        |       +-: +
        |
        +-: |
        |
        +-: r18
            |
            +-: r16
            |   |
            |   +-: r15
            |   |   |
            |   |   +-: r14
            |   |   |   |
            |   |   |   +-: c
            |   |   |
            |   |   +-: r10
            |   |       |
            |   |       +-: r9
            |   |       |   |
            |   |       |   +-: a
            |   |       |
            |   |       +-: *
            |   |
            |   +-: r11
            |       |
            |       +-: r6
            |       |   |
            |       |   +-: r4
            |       |   |   |
            |       |   |   +-: r3
            |       |   |   |   |
            |       |   |   |   +-: €
            |       |   |   |
            |       |   |   +-: r2
            |       |   |       |
            |       |   |       +-: r1
            |       |   |       |   |
            |       |   |       |   +-: $
            |       |   |       |
            |       |   |       +-: +
            |       |   |
            |       |   +-: |
            |       |   |
            |       |   +-: r5
            |       |       |
            |       |       +-: a
            |       |
            |       +-: +
            |
            +-: r17
                |
                +-: c
```
