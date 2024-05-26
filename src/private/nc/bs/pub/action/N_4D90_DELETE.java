package nc.bs.pub.action;

import nc.bs.framework.common.NCLocator;
import nc.bs.pcm.pcmproject.plugin.bpplugin.PcmprojectPluginPoint;
import nc.bs.pubapp.pf.action.AbstractPfAction;
import nc.impl.pubapp.pattern.rule.processer.CompareAroundProcesser;
import nc.itf.pcm.IPcmprojectMaintain;
import nc.vo.pcm.pcmproject.AggPcmProject;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;

public class N_4D90_DELETE extends AbstractPfAction<AggPcmProject> {

	@Override
	protected CompareAroundProcesser<AggPcmProject> getCompareAroundProcesserWithRules(
			Object userObj) {
		CompareAroundProcesser<AggPcmProject> processor = new CompareAroundProcesser<AggPcmProject>(
				PcmprojectPluginPoint.SCRIPT_DELETE);
		// TODO 在此处添加前后规则
		return processor;
	}

	@Override
	protected AggPcmProject[] processBP(Object userObj,
			AggPcmProject[] clientFullVOs, AggPcmProject[] originBills) {
		IPcmprojectMaintain operator = NCLocator.getInstance().lookup(
				IPcmprojectMaintain.class);
		try {
			operator.delete(clientFullVOs, originBills);
		} catch (BusinessException e) {
			ExceptionUtils.wrappBusinessException(e.getMessage());
		}
		return clientFullVOs;
	}

}
