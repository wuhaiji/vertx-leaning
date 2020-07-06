package com.whj.vertx.util;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.util.*;

public class MysqlUtils {

    private MySQLPool mysqlClient;

    //获取mysql 连接池
    public MysqlUtils(Vertx vertx) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3307)
                .setHost("47.100.57.24")
                .setDatabase("test")
                .setUser("root")
                .setPassword("123456");
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(5);
        this.mysqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
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

    public static void main(String[] args) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("strog","123123");
        map.put("sdf","435566");
        map.put("sdfg","7788");
        for (Iterator<Map.Entry<Object, Object>> iterator = map.entrySet().iterator(); iterator.hasNext();){
            Map.Entry<Object, Object> next = iterator.next();
            System.out.println(next.getKey()+"="+next.getValue());
        }
    }

}
