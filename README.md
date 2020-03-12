# 本项目仅供学习交流  
## Java实现的某音爬虫  
1. 可根据用户uid或分享链接拉取用户自上传视频，并提供下载、删除本地视频、已下载视频预览等功能
2. 使用elasticsearch进行本地用户索引、查询
### 使用  
1. 安装elasticsearch，如果使用非默认配置，需调整application.yml中springboot相应配置
2. 安装nginx，配置本地文件路径至nginx
3. 新增 application-http.yml ，配置如下  
```
douyin:
  download:
    local-path: #本地文件跟路径
    local-http-path: #使用nginx、apache等配置访问本地视频文件的访问前缀
``` 
启动后访问：[http://localhost:8889/](http://localhost:8889)  
---
下载的视频按照用户分子文件夹，文件夹名为 'shortId + uid'  