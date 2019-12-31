package cn.smbms.controller;

import cn.smbms.pojo.Bill;
import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.bill.BillService;
import cn.smbms.service.bill.BillServiceImpl;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.service.provider.ProviderServiceImpl;
import cn.smbms.tools.Constants;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class BillController {

    @Resource
    private ProviderService providerService;

    @Resource
    private BillService billService;

    @ResponseBody
    @RequestMapping(value = "getproviderlist",produces = "application/json;charset=utf-8")
    public String getProviderlist() {
        System.out.println("getproviderlist ========================= ");
        List<Provider> providerList = new ArrayList<Provider>();
        providerList = providerService.getProviderList("","");
        //把providerList转换成json对象输出
        return (JSONArray.toJSONString(providerList));
    }

    @RequestMapping("getbillByIdView")
    public String getBillByIdView(String billid,HttpServletRequest request){
        if(!StringUtils.isNullOrEmpty(billid)){
            Bill bill = null;
            bill = billService.getBillById(billid);
            request.setAttribute("bill", bill);
            return "billview";
        }
        return "billlist";
    }

    @RequestMapping("getbillByIdModify")
    public String getBillByIdModify(String billid,HttpServletRequest request){
        if(!StringUtils.isNullOrEmpty(billid)){
            Bill bill = null;
            bill = billService.getBillById(billid);
            request.setAttribute("bill", bill);
            return "billmodify";
        }
        return "billlist";
    }

    @RequestMapping("billadd")
    public String billadd(){
        return "billadd";
    }

    @RequestMapping("modifybill")
    public String modify(Bill bill,String id,HttpServletRequest request) {
        System.out.println("modify===============");
        bill.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        bill.setModifyDate(new Date());
        boolean flag = false;
        flag = billService.modify(bill);
        if(flag){
            return "redirect:billlist";
        }else{
            return "billmodify";
        }
    }

    @ResponseBody
    @RequestMapping(value = "delbill",produces ="application/json;charset=utf-8")
    public String delBill(String billid){
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(!StringUtils.isNullOrEmpty(billid)){
            boolean flag = billService.deleteBillById(billid);
            if(flag){//删除成功
                resultMap.put("delResult", "true");
            }else{//删除失败
                resultMap.put("delResult", "false");
            }
        }else{
            resultMap.put("delResult", "notexit");
        }
        //把resultMap转换成json对象输出
        return (JSONArray.toJSONString(resultMap));
    }

    @RequestMapping("addbill")
    public String add(Bill bill,HttpServletRequest request){
        bill.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        bill.setCreationDate(new Date());
        boolean flag = false;
        flag = billService.add(bill);
        System.out.println("add flag -- > " + flag);
        if(flag){
            return "redirect:billlist";
        }
        else{
            return "billadd";
        }
    }

    @RequestMapping("billlist")
    public String query(String queryProductName,String queryProviderId,String queryIsPayment,HttpServletRequest request) {

        List<Provider> providerList = new ArrayList<Provider>();
        providerList = providerService.getProviderList("", "");
        request.setAttribute("providerList", providerList);
        if (StringUtils.isNullOrEmpty(queryProductName)) {
            queryProductName = "";
        }
        List<Bill> billList = new ArrayList<Bill>();
        Bill bill = new Bill();
        if (StringUtils.isNullOrEmpty(queryIsPayment)) {
            bill.setIsPayment(0);
        } else {
            bill.setIsPayment(Integer.parseInt(queryIsPayment));
        }

        if (StringUtils.isNullOrEmpty(queryProviderId)) {
            bill.setProviderId(0);
        } else {
            bill.setProviderId(Integer.parseInt(queryProviderId));
        }
        bill.setProductName(queryProductName);
        billList = billService.getBillList(bill);
        request.setAttribute("billList", billList);
        request.setAttribute("queryProductName", queryProductName);
        request.setAttribute("queryProviderId", queryProviderId);
        request.setAttribute("queryIsPayment", queryIsPayment);
        return "billlist";
    }
}
