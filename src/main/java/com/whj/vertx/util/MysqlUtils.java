package com.whj.vertx.util;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.io.IOException;
import java.util.List;


public class MysqlUtils {
    public static final InternalLogger logger = Log4J2LoggerFactory.getInstance(MysqlUtils.class);
    private MySQLPool mysqlClient;

    //获取mysql 连接池
    public MysqlUtils(Vertx vertx) {
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
        configRetriever.getConfig(ar -> {
            JsonObject configJson = ar.result();
            JsonObject mysqlConfig = configJson.getJsonObject("mysqlConfig");
            MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                    .setPort(mysqlConfig.getInteger("port"))
                    .setHost(mysqlConfig.getString("Host"))
                    .setDatabase(mysqlConfig.getString("database"))
                    .setUser(mysqlConfig.getString("user"))
                    .setPassword(mysqlConfig.getString("password"));
            PoolOptions poolOptions = new PoolOptions()
                    .setMaxSize(5);
            this.mysqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
        });


    }

    private Future<JsonObject> getConfigJson(Vertx vertx) {
        Promise<JsonObject> promise = Promise.promise();

        return promise.future();
    }

    //封装查询返回结果
    public JsonArray getResults(RowSet<Row> result, Integer fieldNUmber) {
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

    public Future<RowSet<Row>> getRowSet(SqlConnection conn, String sql, List<Object> params) {
        Promise<RowSet<Row>> promise = Promise.promise();
        Tuple tuple = Tuple.tuple();
        for (Object param : params) {
            tuple.addValue(param);
        }
        conn
                .preparedQuery(sql)
                .execute(tuple, ar2 -> {
                    if (ar2.succeeded()) {
                        promise.complete(ar2.result());
                    } else {
                        promise.fail(ar2.cause());
                    }
                });
        return promise.future();
    }

    //获取sql连接
    public Future<SqlConnection> getConn() {
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
