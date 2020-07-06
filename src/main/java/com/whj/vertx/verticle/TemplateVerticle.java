package com.whj.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;


public class TemplateVerticle extends AbstractVerticle {

    Router router;

    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
            .setPort(3307)
            .setHost("47.100.57.24")
            .setDatabase("test")
            .setUser("root")
            .setPassword("123456");

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);

    MySQLPool mysqlClient;

    public void start() {
        // Create the client pool
        mysqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
        //初始化router
        router = Router.router(vertx);
        //post获取body参数必须加入这一行
        router.route().handler(BodyHandler.create());
        //get请求 getParam
        router.get("/user/get").handler(req -> {

        });

        //将router与http server绑定
        vertx.createHttpServer().requestHandler(router).listen(9090, http -> {
            if (http.succeeded()) {
                System.out.println("启动web服务成功...");
            } else {
                System.out.println("启动web服务失败，原因：" + http.cause());
            }
        });

    }



}