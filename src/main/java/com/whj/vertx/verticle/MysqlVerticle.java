package com.whj.vertx.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;

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
            // Get a connection from the pool
            mysqlClient.getConnection(ar1 -> {

                if (ar1.succeeded()) {

                    System.out.println("Connected");

                    // Obtain our connection
                    SqlConnection conn = ar1.result();

                    // All operations execute on the same connection
                    conn
                            .query("SELECT user_id, username FROM sys_user")
                            .execute(ar2 -> {
                                RowSet<Row> result1 = ar2.result();
                                JsonArray objects = getResults(result1, 2);
                                if (ar2.succeeded()) {
                                    conn
                                            .query("SELECT count(*) FROM sys_user")
                                            .execute(ar3 -> {
                                                RowSet<Row> result = ar3.result();
                                                JsonObject count = getResults(result, 1).getJsonObject(0);
                                                req.response().end(objects.toString() + "," + count.toString());
                                                conn.close();
                                            });
                                } else {
                                    // Release the connection to the pool
                                    conn.close();
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

    private JsonArray getResults(RowSet<Row> result, Integer fieldNUmber) {
        JsonArray jsonArray = new JsonArray();
        result.forEach(item -> {
            JsonObject jsonObject = new JsonObject();
            for (int i = 0; i < fieldNUmber; i++) {
                String columnName = item.getColumnName(i);
                Object value = item.getValue(columnName);
                jsonObject.put(columnName, value);
            }
            jsonArray.add(jsonObject);
        });

        return jsonArray;
    }
}