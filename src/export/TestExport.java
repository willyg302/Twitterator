package export;

import db.DB_Connect;

public class TestExport {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("ERROR: Test class must be specified as a command line argument.");
            return;
        }
        long timer;
        try {
            timer = System.nanoTime();

            if (args[0].equals("ConvoGraph")) {
                DB_Connect.convoExport();
            } else if (args[0].equals("Gephi")) {
                DB_Connect.gephiExport();
            } else if (args[0].equals("GeoExport")) {
                DB_Connect.geoExport();
            } else if (args[0].equals("CSV")) {
                DB_Connect.csvExport();
            } else if (args[0].equals("TimeGraph")) {
                DB_Connect.timeExport();
            } else if (args[0].equals("GephiHash")) {
                DB_Connect.gHash();
            } else {
                System.out.println("ERROR: Invalid class specified.");
                return;
            }

            timer = System.nanoTime() - timer;
            System.out.println("Completion Time in ns: " + timer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}