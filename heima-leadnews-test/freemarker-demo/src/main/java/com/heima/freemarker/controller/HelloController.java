package com.heima.freemarker.controller;

import com.heima.freemarker.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class HelloController {

    @GetMapping("basic")
    public String test(Model model) {
        // 传入纯文本的参数
        model.addAttribute("name", "freemarker");

        // 创建实体类，传入相关参数
        Student student = new Student();
        student.setName("藏三");
        student.setAge(18);
        model.addAttribute("stu", student);

        return "01-basic";
    }
    
    @GetMapping("/list")
    public String testList(Model model) {
        // 1.---------------------------
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        // 2.--------------------------
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        // 将两个对象存入集合
        List<Student> list = new ArrayList<>();
        list.add(stu1);
        list.add(stu2);

        // model中存入集合
        model.addAttribute("stus", list);

        // 将数据存入map集合
        Map<String, Student> map = new HashMap<>();
        map.put("stu1", stu1);
        map.put("stu2", stu2);
        model.addAttribute("map", map);

        return "02-list";
    }

    @GetMapping("operation")
    public String operation(Model model) {
        // 构建date数据
        Date now = new Date();
        model.addAttribute("date1", now);
        model.addAttribute("date2", now);

        model.addAttribute("point", 10138);

        return "03_operation";
    }

    @GetMapping("innerFunc")
    public String testInnerFunc(Model model) {
        //1.1 小强对象模型数据
        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());
        //1.2 小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);
        //1.3 将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        model.addAttribute("stus", stus);
        // 2.1 添加日期
        Date date = new Date();
        model.addAttribute("today", date);
        // 3.1 添加数值
        model.addAttribute("point", 102920122);
        return "04-innerFunc";
    }
}
