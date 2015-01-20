package demo;

import java.io.File;
import java.util.Map;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class StatMapDB {
	
	private static final String MAP_NAME = "STAT_MAP";
	private String filePath;
	DB db = null;
	Map<String, String> statMap = null;
	DBMod type = null;
	
	static enum DBMod {
		READ,
		WRITE
	}
	
	public StatMapDB(String filePath, DBMod type) {
		this.filePath = filePath;
		this.type = type;
		init();
	}
	
	private void init() {
		File file = new File(filePath);
		db = DBMaker.newFileDB(file).closeOnJvmShutdown().transactionDisable().asyncWriteFlushDelay(100).make();
		// db = DBMaker.newMemoryDirectDB().closeOnJvmShutdown().transactionDisable().asyncWriteFlushDelay(1000).make();
		if (type.equals(DBMod.WRITE)) {
			if (file.exists()) {
				System.out.println("file exist");
				statMap = db.getTreeMap(MAP_NAME);
				// file.delete();
				// new File(filePath + ".p").delete();
			} else {
				// statMap = db.createTreeMap(MAP_NAME).make();
				statMap = db.getTreeMap(MAP_NAME);
			}
		} else {
			statMap = db.getTreeMap(MAP_NAME);
		}
	}
	
	public Map<String, String> getStatMapDB() {
		return this.statMap;
	}
	
	public void close() {
		if (db != null) {
			db.close();
			db = null;
		}
	}
}
