package nc.impl.ic.m4455;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.pub.compiler.AbstractCompiler2;
import nc.bs.trade.business.HYPubBO;
import nc.impl.ic.m4455.action.ApproveAction;
import nc.impl.ic.m4455.action.CommitAction;
import nc.impl.ic.m4455.action.UnApproveAction;
import nc.impl.ic.m4455.action.UnCommitAction;
import nc.itf.ic.m4455.self.ISapplyBillApprove;
import nc.vo.bd.material.MaterialVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.ic.m4455.entity.SapplyBillBodyVO;
import nc.vo.ic.m4455.entity.SapplyBillHeadVO;
import nc.vo.ic.m4455.entity.SapplyBillVO;
import nc.vo.org.OrgVO;
import nc.vo.pmpub.project.ProjectHeadVO;
import nc.vo.pub.BusinessException;
import nc.vo.pubapp.pattern.exception.ExceptionUtils;
import nc.vo.sm.UserVO;
import nc.ws.intf.OAFlowVO;
import nc.ws.intf.OaWorkFlowUtil;
import nc.ws.intf.OaWorkFlowVO;
import nc.ws.intf.WorkFlowBill;
import net.sf.json.JSONArray;

// 出库申请
public class SapplyBillApproveImpl implements ISapplyBillApprove {
	private static String WorkFId;// OA WorkflowId 流程ID
	private static String TableName;// OA TableName 表明

	public SapplyBillVO[] approve(SapplyBillVO[] vos, AbstractCompiler2 script)
			throws BusinessException {
		try {
			ApproveAction action = new ApproveAction();
			return action.approve(vos, script);
		} catch (Exception ex) {
			ExceptionUtils.marsh(ex);

			return null;
		}
	}

	public SapplyBillVO[] commit(SapplyBillVO[] vos, AbstractCompiler2 script)
			throws BusinessException {
		try {
			CommitAction action = new CommitAction();
			SapplyBillVO[] billVOs = action.commit(vos, script);
			senOaData(vos);
			return billVOs;
		} catch (Exception ex) {
			ExceptionUtils.marsh(ex);

			return null;
		}
	}

	private HYPubBO hyPubBO;

	public HYPubBO getHyPubBO() {
		if (null == hyPubBO) {
			hyPubBO = new HYPubBO();
		}
		return hyPubBO;
	}

	public void setHyPubBO(HYPubBO hyPubBO) {
		this.hyPubBO = hyPubBO;
	}

	private void senOaData(SapplyBillVO[] billVOs) throws BusinessException {
		for (SapplyBillVO temp : billVOs) {
			SapplyBillHeadVO hVO = (SapplyBillHeadVO) temp.getParentVO();
			if ("4".equals(getDef2(hVO.getPk_org()))
					&& hVO.getVtrantypecode().contains("4455")) {
				OAFlowVO oaVo = OaWorkFlowUtil.getOAFlowInfo(hVO.getPk_org(),
						"4455");
				if (oaVo != null && oaVo.getIsdr() == 0) {
					WorkFId = oaVo.getFlowid();
					TableName = oaVo.getTablename();
					// 获取主表数据
					JSONArray headData = getMainMap(hVO);
					// 获取子表数据
					JSONArray bodyData = getDtaileDataMap(temp);
					// 构造workflow信息
					OaWorkFlowVO workFlowVO = new OaWorkFlowVO();
					workFlowVO.setPrimaryKey(temp.getPrimaryKey());
					workFlowVO.setPkGroup(hVO.getPk_group());
					workFlowVO.setPkOrg(hVO.getPk_org());
					workFlowVO.setBillMaker(hVO.getBillmaker());
					workFlowVO.setCreator(hVO.getCreator());
					workFlowVO.setBillCode("4455");
					// 测试临时写死，后期改为接口获取
					workFlowVO.setWorkflowId(OaWorkFlowUtil
							.getOAFlowID(WorkFId));
					workFlowVO.setWorkflowName("出库申请单");
					WorkFlowBill bill = OaWorkFlowUtil
							.getWorkFlowBill(workFlowVO);
					// WorkFlowBill的自定义项3 判断是否港华单据
					bill.setDef3("ZT");
					// 制单人身份证号
					UserVO userVO = (UserVO) getHyPubBO().queryByPrimaryKey(
							UserVO.class, temp.getParentVO().getBillmaker());
					String idCard = (String) getHyPubBO().findColValue(
							"bd_psndoc",
							"id",
							"nvl(dr,0) = 0 and pk_psndoc='"
									+ userVO.getPk_psndoc() + "'");
					bill.setDef5(idCard);
					// 调用OA工具类同步数据至OA
					OaWorkFlowUtil.sendOaData(headData, bodyData, bill);
				}
			}
		}
	}

	private JSONArray getMainMap(SapplyBillHeadVO parentVO)
			throws BusinessException {
		// 构造数据
		List<Map<String, Object>> list = OaWorkFlowUtil.transBean2Map(parentVO);
		// ---------其他字段begin
		// 库存组织
		String stockName = (String) getHyPubBO().findColValue(
				"org_stockorg",
				"name",
				"nvl(dr,0) = 0 and pk_stockorg = '" + parentVO.getPk_org()
						+ "'");
		list.add(OaWorkFlowUtil.listAddObj("zzmc", stockName));
		UserVO userVO = (UserVO) getHyPubBO().queryByPrimaryKey(UserVO.class,
				parentVO.getBillmaker());
		// 订单类型名称
		String billTypeName = (String) getHyPubBO().findColValue(
				"bd_billtype",
				"billtypename",
				"nvl(dr,0) = 0 and pk_billtypeid  = '"
						+ parentVO.getCtrantypeid() + "'");
		list.add(OaWorkFlowUtil.listAddObj("typename", billTypeName));
		// 申请部门
		String vDeptName = (String) getHyPubBO().findColValue("org_dept_v",
				"name ",
				"nvl(dr,0) = 0 and pk_vid  = '" + parentVO.getCdptvid() + "'");
		list.add(OaWorkFlowUtil.listAddObj("sqbm", vDeptName));
		// 外部施工单位
		if(parentVO.getCconstructvendorid() != null){
			SupplierVO supplierVO = (SupplierVO) getHyPubBO().queryByPrimaryKey(
					SupplierVO.class, parentVO.getCconstructvendorid());
			String name = "";
			if (null != supplierVO.getPk_supplier()) {
				name = supplierVO.getName();
			}
			list.add(OaWorkFlowUtil.listAddObj("wbsgdw", name));
			list.add(OaWorkFlowUtil.listAddObj("jsdw", name));
		}
		// 施工队
		if(parentVO.getVdef12() != null){
			SupplierVO supplierVO = (SupplierVO) getHyPubBO().queryByPrimaryKey(
					SupplierVO.class, parentVO.getVdef12());
			String name = "";
			if (null != supplierVO.getPk_supplier()) {
				name = supplierVO.getName();
			}
			list.add(OaWorkFlowUtil.listAddObj("sgd", name));
		}

		// 制单人用户编码
		list.add(OaWorkFlowUtil.listAddObj("usercode", userVO.getUser_code()));
		// 制单人名称
		list.add(OaWorkFlowUtil.listAddObj("username", userVO.getUser_name()));
		// 制单人身份证号
		String idCard = (String) getHyPubBO().findColValue("bd_psndoc", "id",
				"nvl(dr,0) = 0 and pk_psndoc='" + userVO.getPk_psndoc() + "'");
		if (null == idCard) {
			throw new BusinessException("制单人身份证号码未维护");
		}
		list.add(OaWorkFlowUtil.listAddObj("userid", idCard));
		// ---------其他字段end
		JSONArray arr = JSONArray.fromObject(list);
		return arr;
	}

	@SuppressWarnings({ "rawtypes" })
	private JSONArray getDtaileDataMap(SapplyBillVO temp)
			throws BusinessException {
		Map bodyMap = getBody(temp.getBodys());
		JSONArray dtlistString = JSONArray.fromObject(bodyMap);
		return dtlistString;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getBody(SapplyBillBodyVO[] bvo) throws BusinessException {
		Map dtMap = new HashMap();
		// 测试临时写死，后期改为接口获取
		dtMap.put("tableDBName", TableName + "_dt1");
		List workflowRequestTableRecords = new ArrayList();
		for (SapplyBillBodyVO temp : bvo) {
			// 构造数据
			List<Map<String, Object>> workflowRequestTableFields = OaWorkFlowUtil
					.transBean2Map(temp);
			// 其他字段------begin
			MaterialVO materialVO = (MaterialVO) getHyPubBO()
					.queryByPrimaryKey(MaterialVO.class, temp.getCmaterialvid());
			if (null != materialVO) {
				// 物料编码
				workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj(
						"material_code", materialVO.getCode()));
				// 物料名称
				workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj(
						"material_name", materialVO.getName()));
				// 规格
				workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj(
						"materialspec", materialVO.getMaterialspec()));
				// 型号
				String str4 = "";
				if (null != materialVO.getMaterialtype()) {
					str4 = materialVO.getMaterialtype();
				}
				workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj(
						"materialtype", str4));
			}
			// 主单位
			String zdwstr = "";
			if (null != temp.getCunitid()) {
				zdwstr = (String) getHyPubBO().findColValue(
						"bd_measdoc",
						"name",
						"nvl(dr,0) = 0 and pk_measdoc  = '" + temp.getCunitid()
								+ "'");
			}
			workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj("zdw",
					zdwstr));
			// 单位
			String dwstr = "";
			if (null != temp.getCastunitid()) {
				dwstr = (String) getHyPubBO().findColValue(
						"bd_measdoc",
						"name",
						"nvl(dr,0) = 0 and pk_measdoc  = '"
								+ temp.getCastunitid() + "'");
			}
			workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj("dw",
					dwstr));
			
			if (null != temp.getCprojectid()) {
				// 项目
				ProjectHeadVO projectVO = (ProjectHeadVO) getHyPubBO()
						.queryByPrimaryKey(ProjectHeadVO.class,
								temp.getCprojectid());
				// 项目编码
				workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj(
						"bproject_code", projectVO.getProject_code()));
				// 项目名称
				workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj(
						"bproject_name", projectVO.getProject_name()));
			}
			// CBS名称
			String cbsName = (String) getHyPubBO().findColValue(
					"bd_cbsnode",
					"name",
					"nvl(dr,0) = 0 and pk_cbsnode  = '" + temp.getPk_cbsnode()
							+ "'");
			workflowRequestTableFields.add(OaWorkFlowUtil.listAddObj("cbsmc",
					cbsName));
			// 其他字段------end
			Map workflowRequestTableFieldsMap = new HashMap();
			workflowRequestTableFieldsMap.put("recordOrder", "0");
			workflowRequestTableFieldsMap.put("workflowRequestTableFields",
					workflowRequestTableFields);
			workflowRequestTableRecords.add(workflowRequestTableFieldsMap);
		}
		dtMap.put("workflowRequestTableRecords", workflowRequestTableRecords);
		return dtMap;
	}

	public SapplyBillVO[] unapprove(SapplyBillVO[] vos, AbstractCompiler2 script)
			throws BusinessException {
		try {
			UnApproveAction action = new UnApproveAction();
			return action.unApprove(vos, script);
		} catch (Exception ex) {
			ExceptionUtils.marsh(ex);

			return null;
		}
	}

	public SapplyBillVO[] uncommit(SapplyBillVO[] vos, AbstractCompiler2 script)
			throws BusinessException {
		try {
			UnCommitAction action = new UnCommitAction();
			unOaCommit(vos);
			SapplyBillVO[] billVOs = action.unCommit(vos, script);
			return billVOs;
		} catch (Exception ex) {
			ExceptionUtils.marsh(ex);

			return null;
		}
	}

	private void unOaCommit(SapplyBillVO[] aggVO) throws BusinessException {
		for (SapplyBillVO temp : aggVO) {
			OaWorkFlowUtil.backOaWorkFlow(temp.getPrimaryKey());
		}
	}

	public String getDef2(String pk_org) throws BusinessException {
		String def2 = "";
		OrgVO orgVO = (OrgVO) new HYPubBO().queryByPrimaryKey(OrgVO.class,
				pk_org);
		if (null != orgVO) {
			def2 = orgVO.getDef2();
		}
		return def2;
	}
}
