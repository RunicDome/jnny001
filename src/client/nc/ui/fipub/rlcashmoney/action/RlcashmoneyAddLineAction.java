package nc.ui.fipub.rlcashmoney.action;

import nc.ui.pubapp.uif2app.actions.batch.BatchAddLineAction;
import nc.vo.fipub.rlcashmoney.CashMoneyVO;
/**
  batch addLine or insLine action autogen
*/
public class RlcashmoneyAddLineAction extends BatchAddLineAction {

	private static final long serialVersionUID = 1L;

	@Override
	protected void setDefaultData(Object obj) {
		super.setDefaultData(obj);
		CashMoneyVO singleDocVO = (CashMoneyVO) obj;
//		singleDocVO.setPk_group(this.getModel().getContext().getPk_group());
//		singleDocVO.setPk_org(this.getModel().getContext().getPk_org());
	}

}