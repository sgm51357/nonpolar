package com.hikvision.ga.commons.datasync.in;

import org.testng.annotations.Test;
import com.hikvision.ga.commons.datasync.DataSyncFactory;
import com.hikvision.ga.commons.datasync.common.DataSyncException;

/**
 * @author shanguoming 2015年1月8日 下午3:05:52
 * @version V1.0
 * @modify: {原因} by shanguoming 2015年1月8日 下午3:05:52
 */
public class DataSyncInTest {
	
	@Test
	public void vms1Sync() throws DataSyncException {
		DataSyncInEvent event = new DataSyncInEvent("file:///D:/development/tools/eclipse-jee(4.4.SR1)/workspaces/nonpolar/nonpolar/commons/data-sync/2.2/src/test/resources/in/vmses/vms1", "vms", "file:///D:/development/tools/eclipse-jee(4.4.SR1)/workspaces/nonpolar/nonpolar/commons/data-sync/2.2/src/test/resources/in/vmses/vms1/vms.xml", new DataSyncInSysServiceImpl());
		DataSyncFactory.getInstance().syncInSys(event);
	}
}
