package com.whj.vertx.verticle;

import com.whj.vertx.util.MysqlUtils;
import com.whj.vertx.util.ResponseUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class FutureMysqlVerticle extends AbstractVerticle {
    public static final InternalLogger log = Log4J2LoggerFactory.getInstance(FutureMysqlVerticle.class);

    private MysqlUtils mysqlUtils;

    public void start() {

        // Create the client pool
        mysqlUtils = new MysqlUtils(vertx);
        MysqlUtils mysqlUtils = new MysqlUtils(vertx);
        //初始化router
        Router router = Router.router(vertx);
        //post获取body参数必须加入这一行
        router.route().handler(BodyHandler.create());
        router.route().last().failureHandler(rct->{
           rct.failure().printStackTrace();
            JsonObject object = new JsonObject().put("code", 500).put("msg", "服务器异常请联系网站管理员");
            rct.response()
                    .putHeader("content-type", "application/json").end(object.toString());
        });
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
            Future<SqlConnection> FutureSqlConn = this.mysqlUtils.getConn();
            // 第二部执行查询
            List<Object> params = new ArrayList<>();//查询参数list
            params.add(Begin);
            params.add(Size);
            Future<RowSet<Row>> FutureRowSet = FutureSqlConn.compose(sqlConn -> this.mysqlUtils.getRowSet(sqlConn, sql, params));
            // 第三步获取查询结果并返回
            FutureRowSet.onSuccess(rowSet -> {
                JsonArray objects = this.mysqlUtils.getResults(rowSet, 2);
                HttpServerResponse response = ResponseUtils.getJSONResponse(req);
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


}