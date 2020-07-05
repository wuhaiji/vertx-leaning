package com.whj.vertx.verticle;

import com.whj.vertx.util.DBUtils;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;


public class MysqlVerticle extends AbstractVerticle {
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

            @Nullable Integer pageSize = Integer.valueOf(req.request().getParam("pageSize"));
            @Nullable Integer pageNo = Integer.valueOf(req.request().getParam("pageNo"));
            // Get a connection from the pool
            mysqlClient.getConnection(ar1 -> {

                if (ar1.succeeded()) {

                    System.out.println("Connected");

                    // Obtain our connection
                    SqlConnection conn = ar1.result();
                    Integer size = 10;
                    Integer begin = 0;
                    if (pageSize > 0) {
                        size = pageSize;
                    }
                    if (pageNo > 0) {
                        begin = size * (pageNo - 1);
                    }

                    System.out.println("begin:" + begin);
                    System.out.println("size:" + size);
                    //所有操作可以共用一个连接，在回调中嵌套查询就行
                    conn
                            .preparedQuery("SELECT user_id, username FROM sys_user limit ?,?")
                            // `?` 是参数占位符
                            // tuple用于传递查询参数
                            .execute(Tuple.of(begin, size), ar2 -> {
                                conn.close();//写在异步回调第一行，防止忘记关闭连接
                                if (ar2.succeeded()) {
                                    //获取参数
                                    RowSet<Row> result1 = ar2.result();
                                    JsonArray objects = DBUtils.getResults(result1, 2);
                                    req.response()
                                            .putHeader("content-type", "application/json")
                                            .end(objects.toString());
                                }
                            });
                } else {
                    System.out.println("Could not connect: " + ar1.cause().getMessage());
                }
            });
        });

        //将router与http server绑定
        vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
            if (http.succeeded()) {
                System.out.println("启动json verticle成功,请访问：127.0.0.1:8080");
            }
        });

    }


}