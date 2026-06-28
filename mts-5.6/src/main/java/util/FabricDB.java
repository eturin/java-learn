package util;

import memDB.DB;
import memDB.DBImpl;

public class FabricDB {
    private static DB db;
    public static DB getDB() {
        if (db != null) return db;
        else return initBD();
    }

    private static synchronized DB initBD() {
        if(db != null) return db;
        else {
            db = new DBImpl(10);
            return db;
        }
    }
}
