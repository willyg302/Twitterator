package export;

import java.sql.Connection;

/**
 * Example Gephi (graph utility) export class. Can also double as a raw export
 * class with line numbers.
 *
 * @author Baohuy Ung, William Gaul
 * @version 1.0
 */
public class Gephi extends BaseExport {

    private static int nodeCount = 0;

    public Gephi(Connection conn) {
        this(conn, "GephiTest", "SELECT * FROM TWEETS");
    }

    public Gephi(Connection conn, String fileName, String sql) {
        super(fileName, sql);
        setDelimiter(";");
        setConversionStrings("|", "--");

        setConnection(conn);

        addExportColumnWithModifier(-1, "N_id", "Gephi", "incrementNodeCount");
        addExportColumn(1, "created");
        addExportColumn(2, "id");
        addExportColumn(3, "text");
        addExportColumn(4, "source");
        addExportColumn(5, "is_trunc");
        addExportColumn(6, "in_reply");
        addExportColumn(7, "reply_user");
        addExportColumn(8, "fav");
        addExportColumn(9, "reply_sn");
        addExportColumn(10, "geo");
        addExportColumn(11, "place");
        addExportColumn(12, "rtCount");
        addExportColumn(13, "rtByMe");
        addExportColumn(14, "contrib");
        addExportColumn(15, "annot");
        addExportColumn(16, "rtStatus");
        addExportColumn(17, "mentions");
        addExportColumn(18, "urls");
        addExportColumn(19, "hash");
        addExportColumn(20, "userID");
    }

    public static String incrementNodeCount() {
        String temp = Integer.toString(nodeCount);
        nodeCount++;
        return temp;
    }
}