package com.neusoft.enroll.pay.order.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neusoft.enroll.base.bean.CompetitionUserMap;
import com.neusoft.enroll.base.bean.Competitionx;
import com.neusoft.enroll.base.bean.Teamx;
import com.neusoft.enroll.base.service.ICompetitionUserMapService;
import com.neusoft.enroll.base.service.ICompetitionxService;
import com.neusoft.enroll.base.service.ITeamxService;
import com.neusoft.enroll.pay.order.bean.RegisterIncomeRecordx;
import com.neusoft.enroll.pay.order.dao.IRegisterIncomeRecordxDao;
import com.neusoft.enroll.pay.order.mappers.RegisterIncomeRecordxMapper;
import com.neusoft.enroll.pay.order.service.IRegisterIncomeRecordxService;
import com.neusoft.enroll.pay.partner.service.ICmptPartnerService;
import com.neusoft.race.common.session.SessionUtil;
import com.neusoft.race.enroll.bean.CompetitionRoad;
import com.neusoft.race.enroll.service.CmptRoadService;
import com.neusoft.race.wallet.service.IEnrollPayService;
import com.neusoft.utils.convert.ConvertUtils;
import com.neusoft.utils.map.MyHashMap;
import com.neusoft.utils.page.PageFactory;
import com.neusoft.utils.string.StringUtil;
import com.neusoft.utils.web.RequestUtils;
import com.vonchange.headb.core.map.HeaMap;
@Service
@Transactional
public class RegisterIncomeRecordxServiceImpl  implements IRegisterIncomeRecordxService{
	@Resource
	private IRegisterIncomeRecordxDao registerIncomeRecordxDao;
   @Resource
	private RegisterIncomeRecordxMapper registerIncomeRecordxMapper;
   @Resource
   private CmptRoadService roadService;
   @Resource
   private IEnrollPayService enrollPayService;
   @Resource
   private ICompetitionUserMapService competitionUserMapService;
   @Resource
   private ITeamxService teamxService;
   @Resource
   private ICmptPartnerService cmptPartnerService;
   @Resource
   private ICompetitionxService competitionxService;
	@Override
	public   int deleteById(Integer rirId) {
		return registerIncomeRecordxMapper.deleteById(rirId);
	}

	@Override
	public int save(RegisterIncomeRecordx bean) {
		return registerIncomeRecordxMapper.insert(bean);
	}

	@Override
	public int updateByIdOrApplyId(RegisterIncomeRecordx bean) {
		return registerIncomeRecordxMapper.updateById(bean);
	}

	@Override
	public RegisterIncomeRecordx queryById(Integer rirId) {
		return registerIncomeRecordxMapper.queryById(rirId);
	}

	@Override
	public List<RegisterIncomeRecordx> queryList(Map<String, Object> param) {
		if(null==param){
			param= new HashMap<String, Object>();
		}
		if(null==param.get("isDelete")){
			param.put("isDelete",0);
		}
		return registerIncomeRecordxMapper.queryList(param);
	}

	@Override
	public Page<RegisterIncomeRecordx> queryPage(Map<String, Object> param, Pageable page) {
		if(null==param){
			param= new HashMap<String, Object>();
		}
		if(null==param.get("isDelete")){
			param.put("isDelete",0);
		}
		int count=registerIncomeRecordxMapper.queryPageCount(param);
		List<RegisterIncomeRecordx> list=registerIncomeRecordxMapper.queryPage(param, page);
		return PageFactory.createPage(list, page, count);
	}
	@Override
	public int saveOrUpdate(RegisterIncomeRecordx bean) {
		if(null==bean){
	     	return -1;
		}
		if(null!=bean.getRirId()){
		    return updateByIdOrApplyId(bean);
		}
		return save(bean);
	}

	@Override
	public RegisterIncomeRecordx queryOrderByOrderNo(String orderNo) {
		return 	registerIncomeRecordxDao.findFirst(new HeaMap().set("applyId_eq", orderNo)
				.setAddOrder("rirId_desc"));
	}
	@Override
	public RegisterIncomeRecordx queryByUserMapIds(String userMapIds) {
		return registerIncomeRecordxMapper.queryByUserMapIds(userMapIds);
	}
	@Override
	public RegisterIncomeRecordx generateOrder(Integer userMapId) {
		CompetitionUserMap roadUser = competitionUserMapService.queryById(userMapId);
		if (null == roadUser) {
			return null;
		}
		RegisterIncomeRecordx oldRegisterIncomeRecord = queryByUserMapIds(ConvertUtils.toString(userMapId));
		if (null != oldRegisterIncomeRecord && null!=oldRegisterIncomeRecord.getTradeStatus()&&oldRegisterIncomeRecord.getTradeStatus().equals("TRADE_SUCCESS")) {
			return null;
		}
		Date payStartTime = null;
		if (null != oldRegisterIncomeRecord) {
			payStartTime = oldRegisterIncomeRecord.getPayStartTime();
		}
		BigDecimal sumAmount =enrollPayService.getSumAmountById(userMapId);
		if (sumAmount.compareTo(new BigDecimal(0.009)) < 0) {
			return null;
		}
		if (null == oldRegisterIncomeRecord ||sumAmount.compareTo(oldRegisterIncomeRecord.getAmount())!=0
				||(null != payStartTime && (new Date().getTime() - payStartTime.getTime()) > 30 * 60*1000)
				) {
			// 新增加 重新生成订单	
			String applyId = StringUtil.uuid();
			RegisterIncomeRecordx bean = new RegisterIncomeRecordx();
			bean.setAmount(sumAmount);
			bean.setUserId(ConvertUtils.toString(userMapId));
			bean.setApplyId(applyId);
			bean.setCmptId(roadUser.getCmptId());
			//根据赛事ID获取parnter
			Integer registerId=SessionUtil.getUserId(RequestUtils.getRequest());
			
			bean.setPartner(cmptPartnerService.queryCodeByCmptId(roadUser.getCmptId()));
			bean.setRoadId(roadUser.getRoadId());
			bean.setRegisterId(registerId);
			bean.setPayPhone(roadUser.getCallPhone());
			bean.setRegisterId(SessionUtil.getUserId(RequestUtils.getRequest()));
			bean.setPayStartTime(new Date());
			CompetitionRoad road=roadService.queryRoadById(0, roadUser.getRoadId());
			Competitionx cmpt=competitionxService.queryById(roadUser.getCmptId());
			String subject=StringUtil.format("{0}-{1}-报名费", cmpt.getCmptName(),road.getRoadName());
			bean.setSubject(subject);
			save(bean);
			return bean;
		}
		return oldRegisterIncomeRecord;
	}

	@Override
	public RegisterIncomeRecordx generateOrderByTeamId(Integer teamId) {
		
		Teamx team = teamxService.queryById(teamId);
		if (null == team||team.getAllPay()==0) {
			return null;
		}
		List<CompetitionUserMap> teamSavedUsers = competitionUserMapService.
				queryList(new MyHashMap().set("teamId", team.getTeamId()).set("isPayed", -1)
						.set("teamSave", 1).set("sumAmountFlag", true));
		BigDecimal sumAmount=new BigDecimal(0);
		StringBuffer userMapIdsSB=new StringBuffer();
		if(null==teamSavedUsers||teamSavedUsers.size()==0){
			return null;
		}
		for (CompetitionUserMap competitionRoadUser : teamSavedUsers) {
			sumAmount=sumAmount.add(competitionRoadUser.getSumAmount());
			userMapIdsSB.append(competitionRoadUser.getUserMapId()+",");
		}
		CompetitionRoad road=roadService.queryRoadById(0, team.getRoadId());
		if(road.getIsOnePay()==1){
			sumAmount = sumAmount.add(road.getTeamPrice());
		}
		String userMapIds=userMapIdsSB.toString().substring(0, userMapIdsSB.length()-1);
		RegisterIncomeRecordx oldRegisterIncomeRecord = queryByUserMapIds(userMapIds);
		if (null != oldRegisterIncomeRecord && null!=oldRegisterIncomeRecord.getTradeStatus()&&oldRegisterIncomeRecord.getTradeStatus().equals("TRADE_SUCCESS")) {
			return null;
		}
		Date payStartTime = null;
		if (null != oldRegisterIncomeRecord) {
			payStartTime = oldRegisterIncomeRecord.getPayStartTime();
		}
		if (sumAmount.compareTo(new BigDecimal(0.009)) < 0) {
			return null;
		}
		if (null == oldRegisterIncomeRecord ||sumAmount.compareTo(oldRegisterIncomeRecord.getAmount())!=0
				||(null != payStartTime && (new Date().getTime() - payStartTime.getTime()) > 30 * 60*1000)
				) {
			// 新增加 重新生成订单		
			String applyId = StringUtil.uuid();
			RegisterIncomeRecordx bean = new RegisterIncomeRecordx();
			bean.setAmount(sumAmount);
			bean.setUserId(userMapIds);
			bean.setApplyId(applyId);
			bean.setCmptId(team.getCmptId());
			//根据赛事ID获取parnter
			bean.setPartner(cmptPartnerService.queryCodeByCmptId(team.getCmptId()));
			bean.setRoadId(team.getRoadId());
			bean.setRegisterId(SessionUtil.getUserId(RequestUtils.getRequest()));
			bean.setPayStartTime(new Date());
			bean.setRegisterId(SessionUtil.getUserId(RequestUtils.getRequest()));
			Competitionx cmpt=competitionxService.queryById(team.getCmptId());
			String subject=StringUtil.format("{0}-{1}-{2}-报名费", cmpt.getCmptName(),road.getRoadName(),team.getTeamName());
			bean.setSubject(subject);
			save(bean);
			return bean;
		}
		return oldRegisterIncomeRecord;
	}


}