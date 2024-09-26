package ch.ergon.adam.oracle;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleScriptParser {

    Pattern COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/");

    private final List<String> script;

    private String statementSoFar;
    private int lineNo;
    private int statementLineNo;
    private List<SqlStatement> result;

    public OracleScriptParser(String sourceScript) {
        script = Arrays.asList(sourceScript.split("\n"));
    }

    public List<SqlStatement> parse() {
        resetParser();
        for (String line : script) {
            lineNo += 1;
            String normalizedLine = line.trim().toLowerCase();
            if (normalizedLine.startsWith("--")) {
                continue;
            } else if (line.equals("/") || line.equals(";")) {
                recognizeTerminator();
            } else {
                recognizeSqlLine(line);
            }
        }
        recognizeTerminator();
        return result;
    }

    private void resetParser() {
        result = new LinkedList<>();
        statementSoFar = "";
        lineNo = 0;
        statementLineNo = 0;
    }

    private void recognizeTerminator() {
        String sql = stripSemicolonIfRequired(statementSoFar);
        statementSoFar = "";
        if (!stripComments(sql).isEmpty()) {
            result.add(new SqlStatement(sql, statementLineNo));
        }
    }

    private String stripSemicolonIfRequired(String sql) {
        sql = sql.trim();
        // only strip the semicolon at the very end, and only if it is not a BEGIN-END block
        if (sql.endsWith(";") && !sql.toLowerCase().endsWith("end;")) {
            sql = sql.substring(0, sql.length()-1);
        }
        return sql;
    }

    private String stripComments(String sql) {
        // strip /* ... */ comments
        Matcher matcher = COMMENT_PATTERN.matcher(sql);
        return matcher.replaceAll("").trim();
    }

    private void recognizeSqlLine(String line) {
        if (statementSoFar == "") {
            statementLineNo = lineNo;
        }
        statementSoFar += line + System.lineSeparator();
    }

    public record SqlStatement(String statement, int lineNo) {}
}

