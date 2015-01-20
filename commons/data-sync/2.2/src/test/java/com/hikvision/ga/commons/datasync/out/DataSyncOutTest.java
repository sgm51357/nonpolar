package com.hikvision.ga.commons.datasync.out;

import org.testng.annotations.Test;
import com.hikvision.ga.commons.datasync.DataSyncFactory;
import com.hikvision.ga.commons.datasync.common.DataSyncException;


/**
 * @author shanguoming 2015年1月9日 上午10:23:32
 * @version V1.0   
 * @modify: {原因} by shanguoming 2015年1月9日 上午10:23:32
 */
public class DataSyncOutTest {
	
	@Test
	public void outSync() throws DataSyncException {
		DataSyncOutEvent event = new DataSyncOutEvent("file:///D:/development/tools/eclipse-jee(4.4.SR1)/workspaces/nonpolar/nonpolar/commons/data-sync/2.2/src/test/resources/out/apollo/sync", "vms", "file:///D:/development/tools/eclipse-jee(4.4.SR1)/workspaces/nonpolar/nonpolar/commons/data-sync/2.2/src/test/resources/out/apollo/sync.xml", new DataSyncOutSysServiceImpl());
		DataSyncFactory.getInstance().syncOutSys(event);
	}
}
