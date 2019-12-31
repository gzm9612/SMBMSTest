package cn.smbms.controller;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.role.RoleServiceImpl;
import cn.smbms.service.user.UserService;
import cn.smbms.service.user.UserServiceImpl;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;
import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller

public class UserController {
    @Resource
    UserService userService;

    @Resource
    RoleService roleService;

    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping(value = "doLogin" ,method = RequestMethod.POST)
    public String doLogin(Model model,String userCode, String userPassword, HttpSession session){
        System.out.println("login ============ " );
        //调用service方法，进行用户匹配
        System.out.println(userCode+userPassword);
        User user = userService.login(userCode,userPassword);
        System.out.println(user);
        if(null != user){
            session.setAttribute(Constants.USER_SESSION, user);
            //页面跳转（frame.jsp）
            return "/frame";
        }else{
            //页面跳转（login.jsp）带出提示信息--转发
            model.addAttribute("error", "用户名或密码不正确");
            return "login";
        }
    }

    @RequestMapping("frame")
    public String frame(HttpSession session){
        if(session.getAttribute("Constants.USER_SESSION")==null){

            System.out.println("用户需要登录"+session.getAttribute("Constants.USER_SESSION"));
            return "login";
        }else{
            return "redirect:frame";
        }
    }

    @RequestMapping("doLoginOut")
    public String doLoginOut(HttpSession session){
        session.removeAttribute(Constants.USER_SESSION);
        return "redirect:login";
    }

    @RequestMapping("userlist")
    public String doQuery(String queryname,String queryUserRole,String pageIndex,HttpServletRequest request){
        int queryUserRoleNew = 0;
        List<User> userList = null;
        //设置页面容量
        int pageSize = Constants.pageSize;
        //当前页码
        int currentPageNo = 1;

        System.out.println("queryUserName servlet--------"+queryname);
        System.out.println("queryUserRole servlet--------"+queryUserRole);
        System.out.println("query pageIndex--------- > " + pageIndex);
        if(queryname == null){
            queryname = "";
        }
        if(queryUserRole != null && !queryUserRole.equals("")){
            queryUserRoleNew = Integer.parseInt(queryUserRole);
        }

        if(pageIndex != null){
            try{
                currentPageNo = Integer.valueOf(pageIndex);
            }catch(NumberFormatException e){
                return "error";
            }
        }
        //总数量（表）
        int totalCount	= userService.getUserCount(queryname,queryUserRoleNew);
        //总页数
        PageSupport pages=new PageSupport();
        pages.setCurrentPageNo(currentPageNo);
        pages.setPageSize(pageSize);
        pages.setTotalCount(totalCount);

        int totalPageCount = pages.getTotalPageCount();

        //控制首页和尾页
        if(currentPageNo < 1){
            currentPageNo = 1;
        }else if(currentPageNo > totalPageCount){
            currentPageNo = totalPageCount;
        }


        userList = userService.getUserList(queryname,queryUserRoleNew,currentPageNo, pageSize);
        request.setAttribute("userList", userList);
        List<Role> roleList = null;
        roleList = roleService.getRoleList();
        request.setAttribute("roleList", roleList);
        request.setAttribute("queryUserName", queryname);
        request.setAttribute("queryUserRole", queryUserRoleNew);
        request.setAttribute("totalPageCount", totalPageCount);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("currentPageNo", currentPageNo);
        return "userlist";
    }


    @RequestMapping("updatepwd")
    public String updatePwd(HttpServletRequest request ,String newpassword) {

        Object o = request.getSession().getAttribute(Constants.USER_SESSION);
        boolean flag = false;
        if(o != null && !StringUtils.isNullOrEmpty(newpassword)){
            flag = userService.updatePwd(((User)o).getId(),newpassword);
            if(flag){
                request.setAttribute(Constants.SYS_MESSAGE, "修改密码成功,请退出并使用新密码重新登录！");
                request.getSession().removeAttribute(Constants.USER_SESSION);//session注销
                return "/login";
            }else{
                request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
            }
        }else{
            request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
        }
        return "pwdmodify";
    }

    @RequestMapping("pwdmodify")
    public String pwdmodify(){
        return "pwdmodify";
    }

    @ResponseBody
    @RequestMapping(value="oldpwdQuery",produces = "application/json;charset=utf-8")
    public String getPwdByUserId(HttpServletRequest request ,String oldpassword) {
        Object o = request.getSession().getAttribute(Constants.USER_SESSION);
        Map<String, String> resultMap = new HashMap<String, String>();
        if(null == o ){//session过期
            resultMap.put("result", "sessionerror");
        }else if(StringUtils.isNullOrEmpty(oldpassword)){//旧密码输入为空
            resultMap.put("result", "error");
        }else{
            String sessionPwd = ((User)o).getUserPassword();
            if(oldpassword.equals(sessionPwd)){
                resultMap.put("result", "true");
            }else{//旧密码输入不正确
                resultMap.put("result", "false");
            }
        }

        return (JSONArray.toJSONString(resultMap));

    }


    @RequestMapping("modify")
    public String modify(User user) {
        user.setModifyDate(new Date());
        if(userService.modify(user)){
            return "redirect:userlist";
        }else{
            return "usermodify";
        }
    }


    @ResponseBody
    @RequestMapping(value = "view",produces = "application/json;charset=utf-8")
    public String getUserById(HttpServletRequest request, String uid){
        if(!StringUtils.isNullOrEmpty(uid)){
            //调用后台方法得到user对象
            User user = userService.getUserById(uid);
            return JSONArray.toJSONStringWithDateFormat(user,"yyyy-MM-dd");
        }
        return "userlist";
    }
    /*@RequestMapping("view")
    public String getUserById(HttpServletRequest request, String uid){
        if(!StringUtils.isNullOrEmpty(uid)){
            //调用后台方法得到user对象
            User user = userService.getUserById(uid);
            request.setAttribute("user", user);
            return "userview";
        }
        return "userlist";
    }*/

    @RequestMapping("modifyuser")
    public String modifyUser(HttpServletRequest request, String uid){
        if(!StringUtils.isNullOrEmpty(uid)){
            //调用后台方法得到user对象
            User user = userService.getUserById(uid);
            request.setAttribute("user", user);
            return "usermodify";
        }
        return "userlist";
    }

    @ResponseBody
    @RequestMapping(value = "deluser",produces = "application/json;charset=utf-8")
    public String delUser(String uid){
        Integer delId = 0;
        try{
            delId = Integer.parseInt(uid);
        }catch (Exception e) {
            // TODO: handle exception
            delId = 0;
        }
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(delId <= 0){
            resultMap.put("delResult", "notexist");
        }else{
            if(userService.deleteUserById(delId)){
                resultMap.put("delResult", "true");
            }else{
                resultMap.put("delResult", "false");
            }
        }
        //把resultMap转换成json对象输出
        return (JSONArray.toJSONString(resultMap));

    }


    @ResponseBody
    @RequestMapping(value = "ucexist",produces = "application/json;charset=utf-8")
    public String userCodeExist(String userCode) {
        //判断用户账号是否可用
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(StringUtils.isNullOrEmpty(userCode)){
            resultMap.put("userCode", "exist");
        }else{
            User user = userService.selectUserCodeExist(userCode);
            if(null != user){
                resultMap.put("userCode","exist");
            }else{
                resultMap.put("userCode", "notexist");
            }
        }
         return (JSONArray.toJSONString(resultMap));

    }

    @ResponseBody
    @RequestMapping(value = "getrolelist",produces = "application/json;charset=utf-8")
    public String getRoleList(){
        List<Role> roleList = null;
        roleList = roleService.getRoleList();
        //把roleList转换成json对象输出

        return (JSONArray.toJSONString(roleList));

    }


    @RequestMapping("useradd")
   public String useradd(){
        return "useradd";
    }

    @RequestMapping("adduser")
    public String add(HttpServletRequest request, User user, @RequestParam(value = "idPicPath1",required = false)MultipartFile attach) {

        String idPicPath=null;

        if(!attach.isEmpty()){
            String path="D:\\test";
            String oldFilename = attach.getOriginalFilename();
            String suffix = FilenameUtils.getExtension(oldFilename);
            int fileMax=500000;
            if(attach.getSize()>fileMax){
                return "useradd";
            }else if(suffix.equalsIgnoreCase("jpg")||suffix.equalsIgnoreCase("png")||suffix.equalsIgnoreCase("jpeg")||suffix.equalsIgnoreCase("pneg")){
                String fileName = System.currentTimeMillis()+new Random().nextInt(100000)+"_Person."+suffix;
                File targetFIle = new File(path,fileName);
                if(!targetFIle.exists()){
                    targetFIle.mkdirs();
                }
                try{
                    attach.transferTo(targetFIle);
                }catch (Exception e){
                    e.printStackTrace();
                    return "useradd";
                }
                idPicPath = path+File.separator+fileName;
            }else {
                return "useradd";
            }
        }
        user.setCreationDate(new Date());
        user.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        user.setIdPicPath(idPicPath);
        if(userService.add(user)){
            return "redirect:userlist";
        }else{
            return "useradd";
        }
    }


}
