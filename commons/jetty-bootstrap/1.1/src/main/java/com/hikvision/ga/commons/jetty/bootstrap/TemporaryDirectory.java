package com.hikvision.ga.commons.jetty.bootstrap;

import java.io.File;
import java.io.IOException;

public class TemporaryDirectory {
	
	public static final String PROPERTY = "java.io.tmpdir";
	
	public static File get() throws IOException {
		String location = System.getProperty("java.io.tmpdir", "tmp");
		File dir = new File(location).getCanonicalFile();
		dir.mkdir();
		return dir;
	}
}
