<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>

<#-- list 数据的展示 -->
<b>展示list中的stu数据:</b>
<br>
<br>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#--variable??,如果该变量存在,返回true,否则返回false -->
    <#if stus??>
        <#list stus as stu>
            <#if stu.name='小红'>
                <tr style="color: red">
                    <td>${stu_index+1}</td>
                    <td>${stu.name}</td>
                    <td>${stu.age}</td>
                    <td>${stu.money}</td>
                    <#--${name!''}表示如果name为空显示空字符串-->
                    <#--${(stu.bestFriend.name)!''}表示，如果stu或bestFriend或name为空默认显示空字符串。-->
                    <td>${stu.birthday!666}</td>
                </tr>
            <#else >
                <tr>
                    <td>${stu_index+1}</td>
                    <td>${stu.name}</td>
                    <td>${stu.age}</td>
                    <td>${stu.money}</td>
                </tr>
            </#if>
        </#list>
    </#if>
</table>
<hr>

<#-- Map 数据的展示 -->
<b>map数据的展示：</b>
<br/><br/>
<a href="###">方式一：通过map['keyname'].property</a><br/>
输出stu1的学生信息：<br/>
<#-- 根据键获取对应值 -->
姓名：${map['stu1'].name}<br/>
年龄：${map['stu1'].age}<br/>
<br/>
<a href="###">方式二：通过map.keyname.property</a><br/>
输出stu2的学生信息：<br/>
姓名：${map.stu2.name}<br/>
年龄：${map.stu2.age}<br/>

<br/>
<a href="###">遍历map中两个学生信息：</a><br/>
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>钱包</td>
    </tr>
    <#-- 将键提取到list集合中，遍历键根据键取值 -->
    <#list map?keys as key>
        <tr>
            <td>${key_index}</td>
            <td>${map[key].name}</td>
            <td>${map[key].age}</td>
            <td>${map[key].money}</td>
        </tr>
    </#list>
</table>
<hr>

<b>算数运算符</b>
<br/><br/>
100 + 5 运算 : ${100+5}<br>
10 - 5 * 5 运算 : ${100-5*5}<br>
5 / 2 运算 : ${5/2}<br>
12 % 10 运算 : ${12 % 10}<br>
<hr>

</body>
</html>