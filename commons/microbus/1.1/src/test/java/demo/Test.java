package demo;

import java.io.File;
import java.util.List;
import com.orientechnologies.orient.core.command.OCommand;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/** * @author wuguirongsg * */
public class Test {
	
	/** * */
	public Test() {
	}
	
	public static void main(String[] args) {
		String dbpath = "orientdbtest6";
		File dbfile = new File(dbpath);
		ODatabaseDocumentTx db;
		int count = 100000;
		if (!dbfile.exists()) {
			db = new ODatabaseDocumentTx("local:" + dbpath).create();
			db = new ODatabaseDocumentTx("local:" + dbpath).open("admin", "admin");
		} else {
			db = new ODatabaseDocumentTx("local:" + dbpath).open("admin", "admin");
		}
		long t1 = System.currentTimeMillis();
//		for (int x = 0; x < 50; x++) {
//			for (int i = 0; i < count; i++) {
//				ODocument animal = new ODocument("Animal"+x);
//				animal.field("id", i);
//				animal.field("name", "Gaudi_" + i);
//				animal.field("location", "Madrid");
//				animal.save();
//			}
//		}
		OCommandSQL cmd = new OCommandSQL("delete from Animal1");
		db.command(cmd).execute();
		System.out.println(count + "条记录写入时间：" + (System.currentTimeMillis() - t1));
		long t3 = System.currentTimeMillis();
		List<ODocument> result = db.query(new OSQLSynchQuery<ODocument>("select * from Animal1"));
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t3);
		int size = result.size();
		for (int i = 0; i < size; i++) {
			result.get(i).field("name");
		}
		System.out.println(size + "条记录读取时间：" + (System.currentTimeMillis() - t2));
	}
}
