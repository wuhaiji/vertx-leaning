package com.whj.vertx.verticle;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.slf4j.impl.Log4jLoggerFactory;

/**
 * vertx整合模板引擎
 */
public class TemplateVerticle extends AbstractVerticle {

    private Router router;
    private ThymeleafTemplateEngine thymeleafTemplateEngine;
    public static final InternalLogger logger = Log4J2LoggerFactory.getInstance(TemplateVerticle.class);

    public void start() {
        thymeleafTemplateEngine = ThymeleafTemplateEngine.create(vertx);
        //初始化router
        router = Router.router(vertx);
        //post获取body参数必须加入这一行
        router.route().handler(BodyHandler.create());
        //get请求 getParam
        JsonObject json = new JsonObject();
        json.put("name","张三");
        json.put("age","123");
        //配置静态资源访问
        router.route("/*").handler(StaticHandler.create());


        router.route("/").handler(ctx -> {
            thymeleafTemplateEngine.render(
                    json, "templates/index.html",
                    bar -> {
                        if (bar.succeeded()) {
                            ctx.response()
                                    .putHeader("context-type", "text/html").end(bar.result());
                        }else{

                        }
                    }
            );
        });

        //将router与http server绑定
        vertx.createHttpServer().requestHandler(router).listen(9090, http -> {
            if (http.succeeded()) {
                logger.info("启动web服务成功...");
            } else {
                logger.info("启动web服务失败，原因：" + http.cause());
            }
        });

    }


}