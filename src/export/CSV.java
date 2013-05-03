package export;

/**
 * Raw CSV export of data from the DB.
 *
 * @author Baohuy Ung, William Gaul
 * @version 1.0
 */
public class CSV extends BaseExport {

    public CSV(String fileName) {
        super(fileName + ".csv", "SELECT * FROM TWEETS");
        setDelimiter(",");
        setConversionStrings(",", " ");

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
        addExportColumn(21, "userSN");
    }
}