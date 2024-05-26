package nc.impl.sscbd.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nc.bs.dao.DAOException;
import nc.bs.framework.adaptor.IHttpServletAdaptor;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.comn.NetStreamContext;
import nc.bs.framework.server.ISecurityTokenCallback;
import nc.bs.logging.Logger;
import nc.ui.pcm.utils.GetDao;

@SuppressWarnings({ "restriction" })
public class UpdateSupplierImpl implements IHttpServletAdaptor {

	@Override
	public void doAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		GetDao getDao = NCLocator.getInstance().lookup(GetDao.class);
		String ds = "design";
		InvocationInfoProxy.getInstance().setUserDataSource(ds);
		InvocationInfoProxy.getInstance().setGroupId("0001A1100000000001QS");// 设置默认的集团主键
		InvocationInfoProxy.getInstance().setUserCode("liuli");// 设置接口默认操作员编码
		InvocationInfoProxy.getInstance().setUserId("1001A110000000000HV8");// 设置接口默认操作员主键
		ISecurityTokenCallback tc = NCLocator.getInstance().lookup(
				ISecurityTokenCallback.class);
		byte[] token = tc.token("NCSystem".getBytes(), "pfxx".getBytes());
		NetStreamContext.setToken(token);
		resp.setCharacterEncoding("utf-8");
		resp.setContentType("text/html;charset=utf-8");
		req.setCharacterEncoding("utf-8");
		// 付款
		String fksql = "SELECT DISTINCT MX.SUPPLIER,FK.BILLNO,GX.DEF3,GX.BILLCODE FROM AP_PAYBILL FK LEFT JOIN AP_PAYITEM "
				+ "MX ON FK.PK_PAYBILL = MX.PK_PAYBILL AND NVL(FK.DR,0) = 0 AND NVL(MX.DR,0) = 0 LEFT JOIN SSC_SSCTASK GX "
				+ "ON FK.BILLNO = GX.BILLCODE AND NVL(GX.DR,0) = 0 WHERE (GX.DEF3 = 'null' OR GX.DEF3 = '~') AND GX.PK_BILLTYPE = '0000Z3000000000000F3'"
				+ " AND MX.SUPPLIER IS NOT NULL ";
		// 应付
		String yfsql = "SELECT DISTINCT MX.SUPPLIER,YF.BILLNO,GX.DEF3,GX.BILLCODE FROM AP_PAYABLEBILL YF LEFT JOIN AP_PAYABLEITEM "
				+ "MX ON YF.PK_PAYABLEBILL = MX.PK_PAYABLEBILL AND NVL(YF.DR,0) = 0 AND NVL(MX.DR,0) = 0 LEFT JOIN SSC_SSCTASK "
				+ "GX ON YF.BILLNO = GX.BILLCODE AND NVL(GX.DR,0) = 0 WHERE (GX.DEF3 = 'null' OR GX.DEF3 = '~') AND GX.PK_BILLTYPE = "
				+ "'0000Z3000000000000F1' AND MX.SUPPLIER IS NOT NULL";
		// 报销
		String bxsql = "SELECT DISTINCT BX.HBBM,BX.DJBH,GX.DEF3,GX.BILLCODE FROM ER_BXZB BX LEFT JOIN SSC_SSCTASK GX "
				+ "ON BX.DJBH = GX.BILLCODE AND NVL(GX.DR,0) = 0 WHERE (GX.DEF3 = 'null' OR GX.DEF3 = '~') AND GX.PK_BILLTYPE = "
				+ "'0000Z30000000000264X' AND BX.HBBM IS NOT NULL";
		try {
			// 付款
			List<Object[]> fkls = getDao.query(fksql);
			if (fkls != null && fkls.size() > 0) {
				for (int i = 0; i < fkls.size(); i++) {
					String supplier = fkls.get(i)[0] + "";// 供应商
					String billno = fkls.get(i)[1] + "";// 单据号
					if (supplier != null) {
						String upsql = "UPDATE SSC_SSCTASK SET DEF3 = '"
								+ supplier + "' WHERE BILLCODE = '" + billno
								+ "';";
						if (i < 5) {
							Logger.error("fkupsql=====" + upsql);
						}
						getDao.executeUpdate(upsql);
						// ssc_activetask
						String upactsql = "UPDATE SSC_ACTIVETASK SET DEF3 = '"
								+ supplier + "' WHERE BILLCODE = '" + billno
								+ "';";
						if (i < 5) {
							Logger.error("upactsql=====" + upactsql);
						}
						getDao.executeUpdate(upactsql);
					}
				}
			}
			// 应付
			List<Object[]> yfls = getDao.query(yfsql);
			if (yfls != null && yfls.size() > 0) {
				for (int i = 0; i < yfls.size(); i++) {
					String supplier = yfls.get(i)[0] + "";// 供应商
					String billno = yfls.get(i)[1] + "";// 单据号
					if (supplier != null) {
						String upsql = "UPDATE SSC_SSCTASK SET DEF3 = '"
								+ supplier + "' WHERE BILLCODE = '" + billno
								+ "';";
						if (i < 5) {
							Logger.error("yfupsql=====" + upsql);
						}
						getDao.executeUpdate(upsql);
						// ssc_activetask
						String upactsql = "UPDATE SSC_ACTIVETASK SET DEF3 = '"
								+ supplier + "' WHERE BILLCODE = '" + billno
								+ "';";
						if (i < 5) {
							Logger.error("upactsql=====" + upactsql);
						}
						getDao.executeUpdate(upactsql);
					}
				}
			}
			// 报销
			List<Object[]> bxls = getDao.query(bxsql);
			if (bxls != null && bxls.size() > 0) {
				for (int i = 0; i < bxls.size(); i++) {

					String supplier = bxls.get(i)[0] + "";// 供应商
					String billno = bxls.get(i)[1] + "";// 单据号
					if (supplier != null) {
						String upsql = "UPDATE SSC_SSCTASK SET DEF3 = '"
								+ supplier + "' WHERE BILLCODE = '" + billno
								+ "';";
						if (i < 5) {
							Logger.error("bxupsql=====" + upsql);
						}
						getDao.executeUpdate(upsql);
						// ssc_activetask
						String upactsql = "UPDATE SSC_ACTIVETASK SET DEF3 = '"
								+ supplier + "' WHERE BILLCODE = '" + billno
								+ "';";
						if (i < 5) {
							Logger.error("upactsql=====" + upactsql);
						}
						getDao.executeUpdate(upactsql);
					}
				}
			}
			resp.setStatus(200);
			PrintWriter writer = resp.getWriter();
			writer.write("执行成功！");
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
