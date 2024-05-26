package nc.ui.pbm.materialplan.ace.handler;

import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pubapp.uif2app.event.IAppEventHandler;
import nc.ui.pubapp.uif2app.event.card.CardBodyAfterEditEvent;
import nc.ui.trade.business.HYPubBO_Client;
import nc.uif.pub.exception.UifException;
import nc.vo.bd.taxcode.TaxrateVO;
import nc.vo.pub.lang.UFDouble;

@SuppressWarnings("restriction")
public class AceBodyAfterEditHandler extends Object implements IAppEventHandler<CardBodyAfterEditEvent> {
	public void handleAppEvent(CardBodyAfterEditEvent e) {
		String key = e.getKey();
		BillCardPanel panel = e.getBillCardPanel();
		String pk_org_v = panel.getHeadItem("pk_org_v").getValueObject().toString();
		try {
			String def7 = (String) HYPubBO_Client.findColValue("org_orgs", "def7", "nvl(dr,0) = 0 and pk_vid='" + pk_org_v + "'");
			if(null == def7 || !def7.equals("1")) {
				return;
			}
		} catch (UifException e2) {
			e2.printStackTrace();
		}
		if ("pk_material_v".equals(key) && null != panel.getBodyValueAt(e.getRow(), "pk_material_v")) {
			String pk_material_v = panel.getBodyValueAt(e.getRow(), "pk_material_v").toString();
			// 如果多选的情况
			if(pk_material_v.contains(",")) {
				pk_material_v = pk_material_v.split(",")[0];
			}
			try {
				String pk_mattaxes = (String) HYPubBO_Client.findColValue("bd_material", "pk_mattaxes", "nvl(dr,0) = 0 and pk_material='"
						+ pk_material_v + "'");
				String pk_taxcode = (String) HYPubBO_Client.findColValue("bd_taxcode", "pk_taxcode", "nvl(dr,0) = 0 and mattaxes='"
						+ pk_mattaxes + "'");
				panel.setBodyValueAt(pk_taxcode, e.getRow(), "bdef6");
				TaxrateVO[] taxrateVO = (TaxrateVO[]) HYPubBO_Client.queryByCondition(TaxrateVO.class, "nvl(dr,0) = 0 and pk_taxcode='"+pk_taxcode+"'");
				if(null != taxrateVO && taxrateVO.length >= 1) {
					UFDouble taxrate = taxrateVO[0].getTaxrate();
					panel.setBodyValueAt(taxrate, e.getRow(), "bdef7");
				}
				
			} catch (UifException e1) {
				e1.printStackTrace();
			}
			
		}else if ("nnum".equals(key) || "mater_unit_price".equals(key)
				|| "mater_price".equals(key)
				|| "bdef6".equals(key)
				|| "bdef7".equals(key)
				|| "bdef9".equals(key)
				|| "bdef10".equals(key)
				|| "bdef8".equals(key)
				|| "service_prc_ratio".equals(key)) {
			// 数量
			UFDouble num = (UFDouble) panel.getBodyValueAt(e.getRow(), "nnum") == null ? new UFDouble(0)
			: (UFDouble) panel.getBodyValueAt(e.getRow(), "nnum");
			// 服务价格系数
			UFDouble service_prc_ratio = panel.getBodyValueAt(e.getRow(), "service_prc_ratio") == null ? new UFDouble(1)
			: new UFDouble(panel.getBodyValueAt(e.getRow(), "service_prc_ratio").toString());
			// 无税单价
			UFDouble mater_unit_price = panel.getBodyValueAt(e.getRow(), "mater_unit_price") == null ? new UFDouble(0)
			: new UFDouble(panel.getBodyValueAt(e.getRow(), "mater_unit_price").toString());
			// 税率
			try {
				String pk_taxcode = (String) panel.getBodyValueAt(e.getRow(), "bdef6");// 税码
				TaxrateVO[] taxrateVO = (TaxrateVO[]) HYPubBO_Client.queryByCondition(TaxrateVO.class, "nvl(dr,0) = 0 and pk_taxcode='"+pk_taxcode+"'");
				if(null != taxrateVO && taxrateVO.length >= 1) {
					UFDouble taxrate = taxrateVO[0].getTaxrate();
					panel.setBodyValueAt(taxrate, e.getRow(), "bdef7");
				}
			} catch (UifException e1) {
				e1.printStackTrace();
			}
			UFDouble bdef7 = panel.getBodyValueAt(e.getRow(), "bdef7") == null ? new UFDouble(0)
			: new UFDouble(panel.getBodyValueAt(e.getRow(), "bdef7").toString());
			// 无税总价
			UFDouble mater_price = service_prc_ratio.multiply(mater_unit_price.multiply(num));
			panel.setBodyValueAt(mater_price, e.getRow(), "mater_price");
			// 含税单价
			UFDouble bdef10 = mater_unit_price.multiply(bdef7.div(100).add(1));
			panel.setBodyValueAt(bdef10, e.getRow(), "bdef10");
			// 含税总价
			UFDouble bdef8 = service_prc_ratio.multiply(bdef10.multiply(num));
			panel.setBodyValueAt(bdef8, e.getRow(), "bdef8");
			// 税额
			UFDouble bdef9 = bdef8.sub(mater_price);
			panel.setBodyValueAt(bdef9, e.getRow(), "bdef9");
		}
	}
}
