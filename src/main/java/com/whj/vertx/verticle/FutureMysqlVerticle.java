package com.whj.vertx.verticle;

import com.whj.vertx.util.DBUtils;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;


public class FutureMysqlVerticle extends AbstractVerticle {

    private Router router;

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

            int pageSize = Integer.parseInt(req.request().getParam("pageSize"));
            int pageNo = Integer.parseInt(req.request().getParam("pageNo"));
            String sql = "SELECT user_id as userId, username FROM sys_user limit ?,?";

            if (pageSize <= 0) {
                pageSize = 10;
            }
            if (pageNo > 0) {
                pageNo = pageSize * (pageNo - 1);
            } else {
                pageNo = 0;
            }
            //lambda表达式必须传入未改变的变量
            final int Begin = pageNo;
            final int Size = pageSize;
            // 第一部获取数据库连接
            Future<SqlConnection> FutureSqlConn = this.getConn();
            // 第一部执行查询
            Future<RowSet<Row>> FutureRowSet = FutureSqlConn.compose(
                    conn -> this.getRowSet(sql, conn, Begin, Size)
            );
            // 第三步获取查询结果并返回
            FutureRowSet.onSuccess(rows -> {
                JsonArray objects = DBUtils.getResults(rows, 2);
                HttpServerResponse response = getJSONResponse(req);
                response
                        .end(objects.toString());
            });
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

    private HttpServerResponse getJSONResponse(RoutingContext req) {
        return req.response()
                .putHeader("content-type", "application/json");
    }

    private Future<RowSet<Row>> getRowSet(String sql, SqlConnection conn, int begin, int size) {
        Promise<RowSet<Row>> promise = Promise.promise();
        conn
                .preparedQuery(sql)
                .execute(Tuple.of(begin, size), ar2 -> {
                    conn.close();
                    if (ar2.succeeded()) {
                        promise.complete(ar2.result());
                    } else {
                        promise.fail(ar2.cause());
                    }
                });
        return promise.future();
    }

    //获取sql连接
    private Future<SqlConnection> getConn() {
        Promise<SqlConnection> promise = Promise.promise();
        mysqlClient.getConnection(ar1 -> {
            if (ar1.succeeded()) {
                promise.complete(ar1.result());
            } else {
                promise.fail(ar1.cause());
            }
        });
        return promise.future();
    }


}