package com.whj.vertx.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class DBUtils {

    //封装查询返回结果
    public static JsonArray getResults(RowSet<Row> result, Integer fieldNUmber) {
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
