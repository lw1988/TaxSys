package cn.itcast.nsfw.complain.action;

import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.struts2.ServletActionContext;

import cn.itcast.core.action.BaseAction;
import cn.itcast.core.util.QueryHelper;
import cn.itcast.nsfw.complain.entity.Complain;
import cn.itcast.nsfw.complain.entity.ComplainReply;
import cn.itcast.nsfw.complain.service.ComplainService;

import com.opensymphony.xwork2.ActionContext;
import com.sun.org.apache.xerces.internal.impl.dv.xs.YearDV;

public class ComplainAction extends BaseAction {
      @Resource
      private ComplainService complainService;
      private Complain  complain;
      private String startTime;
      private String endTime;
      private ComplainReply reply;
      private String strTitle;
  	  private String strstate;
  	  private  Map<String,Object > statisticMap;
	      //列表
	 public String listUI() {
		 //加载状态列表
		
			ActionContext.getContext().getContextMap().put("complainStateMap", complain.COMPLAIN_STATE_MAP);
			 try {
			 QueryHelper queryHelper=new QueryHelper(Complain.class, "c");
			 if (StringUtils.isNotBlank(startTime)) {
				 startTime =URLDecoder.decode(startTime,"utf-8");
				 queryHelper.addCondition("c.compTime >= ?", DateUtils.parseDate(startTime+":00", "yyyy-MM-dd HH:mm:ss"));
			}
			 if (StringUtils.isNotBlank(endTime)) {
				endTime =URLDecoder.decode(endTime,"utf-8");
				 queryHelper.addCondition("c.compTime <= ?", DateUtils.parseDate(endTime+"59", "yyyy-MM-dd HH:mm:ss"));
			}
			 if (complain !=null) {
				if (StringUtils.isNotBlank(complain.getCompTitle())) {
					complain.setCompTitle(URLDecoder.decode(complain.getCompTitle(), "utf-8"));
				    queryHelper.addCondition("c.compTitle like ?", "%"+complain.getCompTitle()+"%");
				}
				if (StringUtils.isNotBlank(complain.getState())) {
				    queryHelper.addCondition("c.state =?", complain.getState());
				}
			}
			 
			 queryHelper.addOrderByProperty("c.state", queryHelper.ORDER_BY_DESC);
			 queryHelper.addOrderByProperty("c.compTime", queryHelper.ORDER_BY_DESC);
			 pageResult =complainService.getPageResult(queryHelper, getPageNo(), getPageSize());
		} catch (Exception e) {
			e.printStackTrace();
		}
		 return "listUI";
	}
	 //跳转到受理页面
	 public String dealUI(){
		 ActionContext.getContext().getContextMap().put("complainStateMap", complain.COMPLAIN_STATE_MAP);
		 if(complain !=null){
			 strTitle =complain.getCompTitle();
			 strstate =complain.getState();
			 complain =complainService.findObjectById(complain.getCompId());
		 }
		 return "dealUI";
	 }
	 public String deal(){
		 ActionContext.getContext().getContextMap().put("complainStateMap", complain.COMPLAIN_STATE_MAP);
		 //1.更新投诉信息为已受理
		 if(complain !=null){
               Complain tem = complainService.findObjectById(complain.getCompId());
               if (!Complain.COMOLAIN_STATE_DONE.equals(tem.getState())) {//受理状态改变
				 tem.setState(Complain.COMOLAIN_STATE_DONE);
               }
               //2.保存回复信息
               if (reply!=null) {
				reply.setComplain(tem);
				reply.setReplyTime(new Timestamp(new Date().getTime()));
				tem.getComplainReplies().add(reply);
			}
               complainService.update(tem);
		 }
		 return "list";
	 }
	 
	 //跳转到统计页面
	 public String annualStatisticChartUI(){
		 return "annualStatisticChartUI";
	 }
	 //查询统计数
	 public String getAnnualStatisticData(){
			//1、获取年份
			HttpServletRequest request = ServletActionContext.getRequest();
			int year = 0;
			if(request.getParameter("year") != null){
				year = Integer.valueOf(request.getParameter("year"));
			} else {
				//默认 当前年份
				year = Calendar.getInstance().get(Calendar.YEAR);
			}
			//2、获取统计年度的每个月的投诉数
			statisticMap = new HashMap<String, Object>();
			statisticMap.put("msg", "success");
			statisticMap.put("chartData", complainService.getAnnualStatisticDataByYear(year));
			return "annualStatisticData";
		}
	 
	public Complain getComplain() {
		return complain;
	}
	public void setComplain(Complain complain) {
		this.complain = complain;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public ComplainReply getReply() {
		return reply;
	}
	public void setReply(ComplainReply reply) {
		this.reply = reply;
	}
	public String getStrTitle() {
		return strTitle;
	}
	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}
	public String getStrstate() {
		return strstate;
	}
	public void setStrstate(String strstate) {
		this.strstate = strstate;
	}
	public Map<String, Object> getStatisticMap() {
		return statisticMap;
	}
	public void setStatisticMap(Map<String, Object> statisticMap) {
		this.statisticMap = statisticMap;
	}
	
	 
	 
}
