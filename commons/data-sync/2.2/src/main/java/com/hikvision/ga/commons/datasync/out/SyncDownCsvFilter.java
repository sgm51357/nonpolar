package com.hikvision.ga.commons.datasync.out;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 针对去csv文件通过流的形式进行处理
 * @author fangzhibin 2015年1月8日 下午6:43:41
 * @version V1.0   
 * @modify: {原因} by fangzhibin 2015年1月8日 下午6:43:41
 */
public class SyncDownCsvFilter implements Filter {
	
	private final Logger log = LoggerFactory.getLogger("data-sync:SyncDownCsvFilter");

	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		log.info("------获取csv文件返回流过滤器开始!");
		ServletOutputStream out = response.getOutputStream();
		HttpServletResponse rsp = (HttpServletResponse)response;
		HttpServletRequest req = (HttpServletRequest)request;
		rsp.setContentType("application/octet-stream;charset=UTF-8");
		FileInputStream in = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			String uri = req.getRequestURI();
			String ctx = req.getContextPath();
			String rootPath = this.getClass().getResource("/").getPath();
			if (rootPath.indexOf("/target/classes/") > 0) {
				rootPath = rootPath.replace("/target/classes/", "/src/main/webapp");
			} else if (rootPath.indexOf("/WEB-INF/classes/") > 0) {
				rootPath = rootPath.replace("/WEB-INF/classes/", "");
			}
			String file = null;
			if (null != ctx && !"".equals(ctx) && !"/".equals(ctx)) {
				String filePath = uri.replaceFirst(ctx, "");
				if (filePath.indexOf("?") > 0) {
					filePath = filePath.substring(0, filePath.indexOf("?"));
				}
				file = rootPath + filePath;
			}
			if (null != file) {
				in = new FileInputStream(file);
				bis = new BufferedInputStream(in);
				bos = new BufferedOutputStream(out);
				byte[] buff = new byte[4096];
				int bytesRead;
				while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
					bos.write(buff, 0, bytesRead);
				}
			}
			log.info("------获取csv文件返回流转换本地文件流结束!");
		} catch (final MalformedURLException e) {
			log.warn(e.getMessage());
			throw e;
		} catch (final IOException e) {
			log.warn(e.getMessage());
			throw e;
		} finally {
			if (bis != null) bis.close();
			if (bos != null) bos.close();
			if (in != null) in.close();
		}
	}
	
	public void destroy() {
	}
}
