# 本项目仅供学习交流
## Java实现的某音爬虫
首先使用 Charles、Fiddler 等抓包工具获取手机APP的http请求头、cookie信息  
类似:![抓包截图](https://github.com/snake19870227/dy-helper/blob/master/doc/WX20200226-201819@2x.png?raw=true)  
将得到的信息配置到 application-http.yml(新建) 中  
```
douyin:
  download:
    local-path: 本地存储路径根目录(视频会根据用户分文件夹)
    api:
      headers:
        sdk-version: ""
        tt-token: ""
        tt-trace-id: ""
        gorgon: ""
        ss-dp: ""
        user-agent: ""
      cookies:
        d_ticket: ""
        sid_guard: ""
        uid_tt: ""
        sid_tt: ""
        sessionid: ""
        odin_tt: ""
        install_id: ""
        ttreq: ""
``` 
启动后访问：[http://localhost:8889/](http://localhost:8889)  
---
**uid非某音号(短号),secUid可为空**  
用户分享链接为用户主页右上角分享出来的短链接解析后的真实链接  
下载范围
- 自传，用户自行上传的视频
- 收藏，用户收藏的视频(仅测试过本人账号)
---
下载的视频按照用户分子文件夹，文件夹名为 'shortId + nickname + uid'  
用户文件夹下每次会记录日志 yyyy-MM-dd.log.json ,日志每行为对象`com.github.douyin.entity.DyLocalVideo`序列化为json的结果