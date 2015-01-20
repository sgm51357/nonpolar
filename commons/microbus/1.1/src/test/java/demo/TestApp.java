package demo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestApp {
	
	private static final String PATH = "mapdb/test";
	private static long TOTAL = 1000000;
	private static long end = 0;
	
	protected void write(int x) {
		StatMapDB db = new StatMapDB(PATH + x, StatMapDB.DBMod.WRITE);
		Map<String, String> map = db.getStatMapDB();
		long sum = 0;
		long oneStartTime = System.nanoTime();
		for (int i = 0; i < TOTAL; i++) {
			String uuid = UUID.randomUUID().toString();
			map.put(uuid + i, uuid);
			if (i % 10000 == 0) {
				System.out.println(x + "---写入" + ((i / 10000 + 1) * 10000) + "万条");
			}
		}
		sum += (System.nanoTime() - oneStartTime);
		System.out.println(x + "-1000000数据写入-avg:" + sum / TOTAL + " ns, count:" + sum / TOTAL + "ms");
		// System.out.println(x+"--write 10 million times:" + (System.currentTimeMillis() - startTime) + " ms");
		db.close();
	}
	
	protected void read(int i) {
		StatMapDB db = new StatMapDB(PATH + i, StatMapDB.DBMod.READ);
		Map<String, String> map = db.getStatMapDB();
		long startTime = System.currentTimeMillis();
		for (Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
		}
		System.out.println(i+" times:" + (System.currentTimeMillis() - startTime) + " ms");
		db.close();
	}
	
	protected static FutureTask<String> createFutureTask(final StatMapDB db, final String key) {
		return new java.util.concurrent.FutureTask<String>(new java.util.concurrent.Callable<String>() {
			
			public String call() {
				Map<String, String> map = db.getStatMapDB();
				return map.get(key);
			}
		});
	}
	
	protected static FutureTask<String> createWrite(final int i) {
		return new java.util.concurrent.FutureTask<String>(new java.util.concurrent.Callable<String>() {
			
			public String call() {
				TestApp testApp = new TestApp();
				testApp.write(i);
				end = System.currentTimeMillis();
				return "";
			}
		});
	}
	
	protected static FutureTask<String> createRead(final int i) {
		return new java.util.concurrent.FutureTask<String>(new java.util.concurrent.Callable<String>() {
			
			public String call() {
				System.out.println("-------create read "+i);
				TestApp testApp = new TestApp();
				testApp.read(i);
				end = System.currentTimeMillis();
				return "";
			}
		});
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		long start = System.currentTimeMillis();
//		Executor executor = Executors.newCachedThreadPool();
		for (int i = 0; i < 10; i++) {
//			executor.execute(createWrite(i));
			TestApp testApp = new TestApp();
//			testApp.write(i);
			testApp.read(i);
//			executor.execute(createRead(i));
		}
		
		System.out.println((100000 * 100) + "写入时间:" + (System.currentTimeMillis()-start) + "ms");
		// boolean flag = false;
		// if (flag) {
		// FutureTask<String> future0 = null;
		// FutureTask<String> future1 = null;
		// FutureTask<String> future2 = null;
		// FutureTask<String> future3 = null;
		// FutureTask<String> future4 = null;
		// FutureTask<String> future5 = null;
		// FutureTask<String> future6 = null;
		// FutureTask<String> future7 = null;
		// FutureTask<String> future8 = null;
		// FutureTask<String> future9 = null;
		// String uuid = UUID.randomUUID().toString();
		// long t1 = System.currentTimeMillis();
		// long count = 0l;
		// for (int i = 0; i < 0; i++) {
		// long tx = System.currentTimeMillis();
		// StatMapDB db = new StatMapDB(PATH + i, StatMapDB.DBMod.READ);
		// long ty = System.currentTimeMillis();
		// count += (ty - tx);
		// switch (i) {
		// case 0:
		// future0 = createFutureTask(db, uuid);
		// case 1:
		// future1 = createFutureTask(db, uuid);
		// case 2:
		// future2 = createFutureTask(db, uuid);
		// case 3:
		// future3 = createFutureTask(db, uuid);
		// case 4:
		// future4 = createFutureTask(db, uuid);
		// case 5:
		// future5 = createFutureTask(db, uuid);
		// case 6:
		// future6 = createFutureTask(db, uuid);
		// case 7:
		// future7 = createFutureTask(db, uuid);
		// case 8:
		// future8 = createFutureTask(db, uuid);
		// case 9:
		// future9 = createFutureTask(db, uuid);
		// }
		// }
		// executor.execute(future0);
		// executor.execute(future1);
		// executor.execute(future2);
		// executor.execute(future3);
		// executor.execute(future4);
		// executor.execute(future5);
		// executor.execute(future6);
		// executor.execute(future7);
		// executor.execute(future8);
		// executor.execute(future9);
		// future0.get(10000, TimeUnit.SECONDS);
		// future1.get(10000, TimeUnit.SECONDS);
		// future2.get(10000, TimeUnit.SECONDS);
		// future3.get(10000, TimeUnit.SECONDS);
		// future4.get(10000, TimeUnit.SECONDS);
		// future5.get(10000, TimeUnit.SECONDS);
		// future6.get(10000, TimeUnit.SECONDS);
		// future7.get(10000, TimeUnit.SECONDS);
		// future8.get(10000, TimeUnit.SECONDS);
		// future9.get(10000, TimeUnit.SECONDS);
		// System.out.println(count + "----------------" + (System.currentTimeMillis() - t1));
		// }
	}
}
