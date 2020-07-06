package com.whj.vertx.util;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class ResponseUtils {
    //设置请求头
    public static HttpServerResponse getJSONResponse(RoutingContext req) {
        return req.response()
                .putHeader("content-type", "application/json");
    }
}
