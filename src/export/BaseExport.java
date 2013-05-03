package export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import twitterator.Twitterator;

/**
 * Base class for exporting, as an attempt to consolidate many similar functions
 * in one place. Extending this class should make it relatively easy to add a
 * new export class.
 *
 * @author William Gaul
 * @version 1.0
 */
public class BaseExport {

    protected String fileName;
    protected String sql;
    protected String d;
    // Strings to replace in the textConvert() method
    protected String badStr, replStr;
    // Need not be used...
    protected Connection conn;
    // Mapping of columns in the exported file
    protected ArrayList<ColumnMapping> exportColumns;

    public BaseExport() {
        exportColumns = new ArrayList<ColumnMapping>();
    }

    public BaseExport(String fileName, String sql) {
        this();
        this.fileName = fileName;
        this.sql = sql;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSQLStatement(String sql) {
        this.sql = sql;
    }

    public void setDelimiter(String d) {
        this.d = d;
    }

    public void setConversionStrings(String bad, String repl) {
        this.badStr = bad;
        this.replStr = repl;
    }

    public void addExportColumn(int DBColumn, String columnName) {
        exportColumns.add(new ColumnMapping(DBColumn, columnName));
    }

    public void addExportColumnWithModifier(int DBColumn, String columnName, String className, String method) {
        ColumnMapping temp = new ColumnMapping(DBColumn, columnName);
        temp.addModifier(className, method);
        exportColumns.add(temp);
    }

    public void make() throws SQLException, IOException {
        make(conn);
    }

    public void make(Connection conn) throws SQLException, IOException {
        System.out.println("Creating statement...");
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(sql);

        // Write header
        writeA(makeHeader());

        // Write data
        boolean eof = false;
        String line;
        rs.first();
        int numLines = 0;
        while (!eof) {
            line = makeLine(rs);
            //System.out.println("Inserting Line: " + line);
            writeA(line);
            numLines++;
            rs.next();
            if (rs.isAfterLast()) {
                eof = true;
            }
        }
        Twitterator.log("Wrote " + numLines + " lines to file " + fileName);
    }

    protected String makeHeader() throws SQLException, IOException {
        String header = "";
        for (ColumnMapping cm : exportColumns) {
            header += (cm.getColumnName() + d);
        }
        header += "\n";
        return header;
    }

    protected String makeLine(ResultSet rs) throws SQLException, IOException {
        String temp;
        String line = "";

        for (ColumnMapping cm : exportColumns) {
            int col = cm.getDBColumn();
            if (col == -1) {
                temp = cm.modify();
            } else {
                temp = cm.modify(rs.getString(col));
            }
            //System.out.println(temp);
            line += (textConvert(temp) + d);
        }
        line += "\n";
        //line = textConvert(line);
        return line;
    }

    /**
     * Helper method to write each line into the file as it comes out of the DB.
     */
    public void writeA(String s) throws IOException {
        File file = new File(fileName);
        file.setWritable(true);
        FileWriter fw = new FileWriter(file, true);
        fw.append(s);
        fw.close();
    }

    /**
     * Converts the text from the DB which is not CSV friendly into a single
     * string. Replaces newlines and delimiters with spaces.
     */
    public String textConvert(String s) {
        if (s == null) {
            return s;
        }
        if (s.contains("\n")) {
            System.out.println("Return characters found! Replacing with space...");
            s = s.replace("\n", " ");
        }
        if (s.contains(badStr)) {
            s = s.replace(badStr, replStr);
        }
        return s;
    }

    /**
     * A class that holds a mapping from an export column to either a:
     *    - Column in the DB
     *    - Function from another class (invoked by reflection, returning a String)
     *    - Both (in which case the function must operate on the column data)
     * More than one export column may map to the same DB column.
     *
     * Modifiers are a useful way to post-process data from the DB result set.
     * Only one is allowed per export column. Originally modifiers were handled
     * separately, but this has since been refactored to allow for empty DB-less
     * columns.
     *
     * Modifiers must be static functions that return a String.
     */
    protected class ColumnMapping {

        // The column in the DB that this maps to (-1 for no column)
        private int DBColumn;
        // Name of this column in the export file and header (could be DB column name as well)
        private String columnName;
        // Optional if this column has a modifier, the name of the method's containing class to call
        private String className;
        // Modifier for this column, if any (only one per export column)
        private String method;

        public ColumnMapping(int col, String name) {
            this.DBColumn = col;
            this.columnName = name;

            // By default
            this.method = "none";
        }

        public void addModifier(String className, String method) {
            this.className = className;
            this.method = method;
        }

        public int getDBColumn() {
            return DBColumn;
        }

        public String getColumnName() {
            return columnName;
        }

        public String modify() {
            if (method.equals("none")) {
                return "null";
            }
            String temp = "";
            try {
                Method m = Class.forName(className).getDeclaredMethod(method, new Class[]{});
                Object o = m.invoke(null, new Object[]{});
                temp += o;
            } catch (Exception e) {
            }
            return temp;
        }

        public String modify(String s) {
            if (method.equals("none")) {
                return s;
            }
            String temp = "";
            try {
                Method m = Class.forName(className).getDeclaredMethod(method, new Class[]{String.class});
                Object o = m.invoke(null, new Object[]{s});
                temp += o;
            } catch (Exception e) {
            }
            return temp;
        }
    }
}