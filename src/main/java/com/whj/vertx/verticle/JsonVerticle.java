package com.whj.vertx.verticle;

import com.alibaba.fastjson.JSONObject;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class JsonVerticle extends AbstractVerticle {
    Router router;

    public void start() {
        //初始化router
        router = Router.router(vertx);
        //post获取body参数必须加入这一行
        router.route().handler(BodyHandler.create());
        //全局异常处理
        router.route().failureHandler(route -> {
            System.out.println("Routing error:" + route.failure());
            int code = 500;
            String error = route.failure().getMessage();
            if (error != null && error.contains("不存在")) {
                code = 404;
            }
            String string = new JsonObject().put("code", code).put("msg", error).toString();
            route.response().end(string);
        });

        //配置路由 地址
        router.route("/").handler(req -> {
            req.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("name", "vertx json").toString());
        });

        //get请求 getParam
        router.get("/add").handler(req -> {
            Integer num = Integer.parseInt(req.request().getParam("page"));//verticle获取参数就这一句
            req.response()
                    .end("" + (num + 2));
        });

        //get请求 rest参数
        router.get("/add/:page").handler(req -> {
            req.response()
                    .end(req.request().getParam("page"));
        });

        //post请求 json参数
        router.route("/test/json").handler(req -> {
            @Nullable String bodyAsString = req.getBodyAsString();
            System.out.println("bodyAsJson:" + bodyAsString);
            req.response()
                    .end(bodyAsString.toString());

        });
        //post请求 form data
        router.post("/test/form").handler(context -> {
            Integer page = Integer.valueOf(context.request().getFormAttribute("page"));
            @Nullable String page1 = context.request().getParam("page");
            System.out.println(page);
            context.response().end("页码：" + page+","+page1);
        });

        //将router与http server绑定
        vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
            if (http.succeeded()) {
                System.out.println("启动json verticle成功,请访问：127.0.0.1:8080");
            }
        });

    }
}