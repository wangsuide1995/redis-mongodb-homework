package cn.com.taiji.mongoweb.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

@Controller
public class redisController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //这是登录页面，用来输入验证码的
    @RequestMapping({"/login","/",""})
    public String login(){
        return "login";
    }

    //生成验证码，并存入redis中，并设置过期时间，过期后自动注销
    @RequestMapping("/smsa/{phone}")
    public String  sms(Model model, HttpSession session,@PathVariable String phone){
        //生成6位数的验证码
        String code = RandomStringUtils.randomNumeric(6);
        //判断当前的redis中的key为“number2”的size为多少
        if (stringRedisTemplate.opsForList().size(phone)<3){
            //当前缓存中size如果小于3的话，从新发送验证码
            stringRedisTemplate.opsForList().leftPush(phone, code);
            //设置key的过期时间为60秒
            stringRedisTemplate.expire(phone, 600, TimeUnit.SECONDS);//设置过期时间

            model.addAttribute("send","已发送");
            model.addAttribute("sms",code);
            session.setAttribute("smsm",code);
            return "login";
        }else {
            model.addAttribute("fail","半小时内不能超过三次哦...");
            return "login";
        }
    }

    //当验证码正确后
    @RequestMapping("/index/{num}")
    public String index(@PathVariable String num, HttpSession session, Model model){
        String smss =(String) session.getAttribute("smsm");
       if (smss.equals(num)){
           return "index";
       }else {
           model.addAttribute("no","验证码不正确...");
           return "login";
       }
    }
}
