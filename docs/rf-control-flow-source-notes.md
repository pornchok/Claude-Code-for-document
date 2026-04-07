# Source Notes: Robot Framework Control Flow

> ไฟล์นี้ใช้ภายใน — ลบได้หลัง Quality Review ผ่านแล้ว

---

## SOURCE 1: Official User Guide (Control Structures)
SOURCE: https://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html
VERSION: RF 7.3.2 (tested locally, Python 3.14.0 on darwin)

### IF / ELSE
QUOTE: "Robot Framework supports IF/ELSE structure that works similarly as if/else in most programming languages."

QUOTE: "The IF marker starts the IF block and it must be followed by a condition. Keywords inside the block are executed if the condition is true."

QUOTE: "ELSE IF and ELSE branches are optional and there can be multiple ELSE IF branches."

### Inline IF
QUOTE: "Normal IF/ELSE structure is a bit verbose if there is a need to execute only a single statement based on a condition. An alternative is using inline IF syntax where the statement to execute follows the IF marker and possible condition directly."
VERSION NOTE: RF 5.0+

### FOR Loop
QUOTE: "The loop syntax starts with FOR (case-sensitive) followed by one or more loop variables, then IN (case-sensitive), then the values to iterate over."

QUOTE: "FOR...IN RANGE: The loop iterates over a range of numbers."
FORMAT: `FOR    ${var}    IN RANGE    [start]    end    [step]`

QUOTE: "FOR...IN ENUMERATE: In enumerate loops the first loop variable gets the index of the current loop iteration."
FORMAT: `FOR    ${index}    ${item}    IN ENUMERATE    @{list}    [start=N]`
start= parameter: RF 6.0+

QUOTE: "FOR...IN ZIP: In zip loops the variables are assigned from multiple list-like sequences."
CRITICAL: ต้องใช้ list-like variable (ไม่ใช่ inline string literals)
TESTED: `${names}=    Create List    ...` → ใช้ได้, inline literals → FAIL

### WHILE Loop
QUOTE: "WHILE loops repeat the loop body as long as the loop condition is true. This is a common pattern in many programming languages, but often FOR loops are sufficient in Robot Framework."
VERSION NOTE: RF 5.0+

QUOTE: "To avoid accidental infinite loops, the loop has an iteration limit that defaults to 10000."
QUOTE: "The limit can be changed with the limit configuration parameter."
Examples: `limit=100`, `limit=10 seconds`, `limit=NONE`

### BREAK / CONTINUE
QUOTE: "Loop execution can be controlled with BREAK and CONTINUE statements that work similarly as in Python and many other programming languages."
VERSION NOTE: RF 5.0+

---

## SOURCE 2: Inline IF with assignment
QUOTE: "It is also possible to use inline IF with assignment."
EXAMPLE: `${var} =    IF    ${cond}    Keyword    ELSE    Other`
