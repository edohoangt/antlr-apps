import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class LoadCSV {

    public static class Loader extends CSVBaseListener {
        
        public static final String EMPTY = "";
        
        List<Map<String, String>> rows = new ArrayList<>();
        List<String> header;
        List<String> curRowFieldValues;

        public void exitHdr(CSVParser.HdrContext ctx) {
            this.header = new ArrayList<>();
            this.header.addAll(this.curRowFieldValues);
        }

        public void enterRow(CSVParser.RowContext ctx) {
            this.curRowFieldValues = new ArrayList<>();
        }

        public void exitEmpty(CSVParser.EmptyContext ctx) {
            this.curRowFieldValues.add(EMPTY);
        }

        public void exitRow(CSVParser.RowContext ctx) {
            if (ctx.getParent().getRuleIndex() == CSVParser.RULE_hdr) return;

            // A data row
            Map<String, String> m = new LinkedHashMap<>();
            int i = 0;
            for (String v: curRowFieldValues) {
                m.put(header.get(i), v);
                i++;
            }
            rows.add(m);
        }

        public void exitString(CSVParser.StringContext ctx) {
            this.curRowFieldValues.add(ctx.STRING().getText());
        }

        public void exitText(CSVParser.TextContext ctx) {
            this.curRowFieldValues.add(ctx.TEXT().getText());
        }

    }

    public static void main(String[] args) throws IOException {
        String inputFile = null;
        if (args.length > 0) inputFile = args[0];
        
        InputStream is = System.in;
        if (inputFile != null) is = new FileInputStream(inputFile);

        CSVLexer lexer = new CSVLexer(CharStreams.fromStream(is));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CSVParser parser = new CSVParser(tokens);
        parser.setBuildParseTree(true);
        ParseTree tree = parser.file();

        ParseTreeWalker walker = new ParseTreeWalker();
        Loader loader = new Loader();
        walker.walk(loader, tree);
        System.out.println(loader.rows);
    }
    
}