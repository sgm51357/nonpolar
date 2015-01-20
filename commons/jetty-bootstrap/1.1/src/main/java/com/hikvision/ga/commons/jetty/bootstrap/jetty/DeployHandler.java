package com.hikvision.ga.commons.jetty.bootstrap.jetty;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser;
import org.eclipse.jetty.xml.XmlParser.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class DeployHandler extends HandlerCollection {
	
	private static final Logger log = LoggerFactory.getLogger(DeployHandler.class);
	private String deployPath = "./webapps";
	
	public DeployHandler() {
		Set<String> ctxs = new HashSet<String>();
		try {
			XmlParser xmlParser = new XmlParser();
			Node node = xmlParser.parse("./config.xml");
			Node apps = node.get("apps");
			if (null != apps) {
				for (Iterator<Object> iterator = apps.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if (object instanceof Node) {
						Node app = (Node)object;
						if (null != app) {
							String startup = app.getAttribute("startup");
							if (null != startup && !"".equals(startup.trim())) {
								runScript(startup);
							}
						}
					}
				}
			}
			Node webapps = node.get("webapps");
			if (null != apps) {
				for (Iterator<Object> iterator = webapps.iterator(); iterator.hasNext();) {
					Object object = iterator.next();
					if (object instanceof Node) {
						Node webapp = (Node)object;
						String path = webapp.getAttribute("path");
						String contextPath = webapp.getAttribute("contextPath");
						if (null != path && !"".equals(path.trim()) && null != contextPath && !"".equals(contextPath.trim())) {
							if (contextPath.indexOf("/") != 0) {
								contextPath = "/" + contextPath;
							}
							WebAppContext app = new WebAppContext(path, contextPath);
							app.setExtractWAR(false);
							app.setThrowUnavailableOnStartupException(true);
							super.addHandler(app);
							ctxs.add(contextPath);
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("启动时加载config.xml异常", e);
		} catch (SAXException e) {
			log.error("启动时解析config.xml异常", e);
		}
		File deployDir = new File(this.deployPath);
		if ((deployDir.exists()) && (deployDir.isDirectory())) {
			File[] childs = deployDir.listFiles();
			if ((childs != null) && (childs.length > 0)) for (File c : childs)
				if (c.isDirectory()) {
					String ctx = "/" + c.getName();
					if (!ctxs.contains(ctx)) {
						WebAppContext app = new WebAppContext(c.getPath(), "/" + c.getName());
						app.setExtractWAR(false);
						app.setThrowUnavailableOnStartupException(true);
						super.addHandler(app);
					}
				}
		}
	}
	
	public void runScript(String scriptName) {
		try {
			if (null != scriptName && !"".equals(scriptName.trim())) {
				Process ps = null;
				File f = new File(scriptName);
				if (scriptName.indexOf(".bat") > 0) {
					ps = Runtime.getRuntime().exec("cmd /c start " + f.getCanonicalPath());
					// InputStream in = ps.getInputStream();
					// byte[] buf = new byte[1024];
					// while (in.read(buf) > 0) {
					// System.out.print(new String(buf));// 如果你不需要看输出，这行可以注销掉
					// }
					// in.close();
				} else if (scriptName.indexOf(".sh") > 0) {
					ps = Runtime.getRuntime().exec(f.getCanonicalPath());
				}
				if (null != ps) {
					ps.waitFor();
				}
			}
		} catch (IOException e) {
			log.error("执行bat文件时IO异常", e);
		} catch (InterruptedException e) {
			log.error("执行bat文件时中断异常", e);
		}
	}
}
