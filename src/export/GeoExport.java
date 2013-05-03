package export;

import java.sql.Connection;

/**
 * Based on the Gephi class, exports the all nodes from a database with geo-tags
 * enabled
 *
 * @author Baohuy Ung, William Gaul
 * @version 1.0
 */
public class GeoExport extends BaseExport {

    private static int nodeCount = 1;

    public GeoExport(Connection conn) {
        this(conn, "GeoExportTest", "SELECT * FROM TWEETS WHERE GEOLOCATION IS NOT NULL");
    }

    public GeoExport(Connection conn, String fileName, String sql) {
        super(fileName, sql);
        setDelimiter(",");
        setConversionStrings(",", " ");

        setConnection(conn);

        addExportColumnWithModifier(-1, "N_id", "GeoExport", "incrementNodeCount");
        addExportColumn(1, "created");
        addExportColumn(2, "id");
        addExportColumn(3, "text");
        addExportColumn(6, "in_reply");
        addExportColumn(7, "reply_user");
        addExportColumn(9, "reply_sn");
        addExportColumnWithModifier(10, "geo_late", "GeoExport", "lateGet");
        addExportColumnWithModifier(10, "geo_long", "GeoExport", "longGet");
        addExportColumn(11, "place");
        addExportColumn(12, "rtCount");
        addExportColumn(13, "rtByMe");
        addExportColumn(14, "contrib");
        addExportColumn(16, "rtStatus");
        addExportColumn(17, "mentions");
        addExportColumn(18, "urls");
        addExportColumn(19, "hash");
        addExportColumn(20, "userID");
        addExportColumn(21, "userSN");
    }

    public static String lateGet(String geo) {
        String temp = geo.substring(geo.indexOf("=") + 1, geo.indexOf(","));
        Double d = new Double(temp);
        int tempInt = (int) (d * 100);
        d = tempInt / 100.0;
        return d.toString();
    }

    public static String longGet(String geo) {
        String temp = geo.substring(geo.lastIndexOf("=") + 1, geo.length() - 1);
        Double d = new Double(temp);
        int tempInt = (int) (d * 100);
        d = tempInt / 100.0;
        return d.toString();
    }

    public static String incrementNodeCount() {
        String temp = Integer.toString(nodeCount);
        nodeCount++;
        return temp;
    }
}