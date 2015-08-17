#基于NIO的简单web服务器
------------

> 简易，灵活，更少的依赖，更多的扩展。更少的内存占用

##基本功能

- 1.实现对浏览器请求的处理，可以展示一些静态页面
- 2.支持文件上传，下载，cookie，Json
- 3.路由配置请求
- 4.freemarker 模板
- 5.多线程支持

##项目结构

 server(simplewebserver 核心代码)
 
 demo(web项目)
 
   * bin 方便打包，程序启动
   * conf 配置文件
   * logs 存放日志文件
   * src 代码文件
   * static 放置静态页面
   * templates freemarker模板文件
   * temp 文件上传缓存目录
   
##Changelog

V1.2(2015-08-16)

* 添加freemarker
* 增加日志信息记录
* 处理Session多线程线程安全


##一行代码代码就启动了web程序

```java
package com.fzb.test;
 
import com.fzb.http.server.InterceptorHelper;
import com.fzb.http.server.Router;
import com.fzb.http.server.impl.RouterServer;
 
public class ServerRun extends RouterServer{
    @Override
    public void configServer() {
        //config router
        Router.getInstance().addMapper("/user", MySimpleController.class);
         
        //config Intercepor
        InterceptorHelper.getInstance().addIntercepor(MyIntercepor.class);
    }
     
    public static void main(String[] args) {
        // 启动 server
        new Thread(new ServerRun()).start();
    }
}
```

然后浏览器输入 http://localhost:6058 

##demo

当你下载某个文件由于线路问题可能会很慢，那么你使用 [http://down.94fzb.com](http://down.94fzb.com) 下载，可以节省一些时间。


##其他

* webServer 默认端口为 `6058` 在conf/conf.properties 中
* 使用 `FreeMarkerKit.init` 初始化模板文件根目录
* 服务器上时建议打包为 `jar` 文件运行（推荐使用 maven，jar文件路径与`conf` 文件夹同目录）