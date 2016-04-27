#基于NIO的web服务器
------------

> 简易，灵活，更少的依赖，更多的扩展。更少的内存占用，能快速搭建Web项目。可快速运行嵌入式, Android 设备上

------------

##基本功能

- 1.实现对浏览器请求的处理，可以展示一些静态页面
- 2.支持文件上传，下载，cookie，Json
- 3.路由配置请求
- 4.freemarker 模板
- 5.多线程支持
- 6.支持 https

##项目结构

 server(simplewebserver 核心代码)
 

 demo(web项目)
 
```
 ├── bin 方便打包，程序启动
 ├── conf 配置文件
 ├── logs 存放日志文件
 ├── pom.xml maven 配置文件
 ├── src 代码文件
 ├── static 放置静态页面
 ├── temp 文件上传缓存目录
 └── templates 模板文件
```

##Changelog

V1.3(2015-12-13)

* 引入对 Https 的支持
* 变更创建 WebServer的方式
* 支持 Gzip 流的压缩
* 支持单进程启动多个 Server (Router,Interceptor 非 static)
* 增加对部分代理软件请求的支持
* 修复部分请求上传文件导致的异常

V1.2(2015-08-16)

* 添加freemarker
* 增加日志信息记录
* 处理Session多线程线程安全


## 启动了webServer示例


```java
package com.fzb.demo.file;

import com.fzb.http.kit.FreeMarkerKit;
import com.fzb.http.kit.PathKit;
import com.fzb.http.server.MethodInvokeInterceptor;
import com.fzb.http.server.SimpleServerConfig;
import com.fzb.http.server.WebServerBuilder;
import com.fzb.http.server.impl.RequestConfig;
import com.fzb.http.server.impl.ResponseConfig;
import com.fzb.http.server.impl.ServerConfig;

public class ServerRun extends SimpleServerConfig {

    @Override
    public ServerConfig getServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.getRouter().addMapper("/_file", MySimpleController.class);
        serverConfig.addInterceptor(MethodInvokeInterceptor.class);
        try {
            FreeMarkerKit.init(PathKit.getRootPath() + "/templates");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverConfig;
    }

    @Override
    public RequestConfig getRequestConfig() {
        return null;
    }

    @Override
    public ResponseConfig getResponseConfig() {
        ResponseConfig responseConfig = new ResponseConfig();
        responseConfig.setIsGzip(true);
        return responseConfig;
    }

    public static void main(String[] args) {
        ServerRun serverConfig = new ServerRun();
        new WebServerBuilder.Builder().config(serverConfig).build().startWithThread();
    }

}
```

然后浏览器输入 http://localhost:6058 

##demo

当你下载某个文件由于线路问题可能会很慢，那么你使用 [http://tool.94fzb.com](http://tool.94fzb.com) 下载，可以节省一些时间。

##其他

* webServer 默认端口为 `6058` 在conf/conf.properties 中
* 使用 `FreeMarkerKit.init` 初始化模板文件根目录
* 服务器上时建议打包为 `jar` 文件运行（推荐使用 maven，jar文件路径与`conf` 文件夹同目录）
* 执行 `cd simplewebserver && sh package.sh ` 将 simplewebserver 添加到本地maven仓库中