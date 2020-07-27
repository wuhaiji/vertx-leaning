package com.whj.vertx.verticle;


import com.whj.vertx.util.MysqlUtils;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
    public static final InternalLogger logger = Log4J2LoggerFactory.getInstance(MysqlUtils.class);
    @Override
    public void start(Promise<Void> startPromise) {
        /**
         * 有些时候您的 Verticle 启动会耗费一些时间，您想要在这个过程做一些事，并且您做的这些事并不想等到Verticle部署完成过后再发生。
         * 如：您想在 start 方法中部署其他的 Verticle,您不能在您的 start 方法中阻塞等待其他的 Verticle 部署完成，
         * 这样做会破坏 黄金法则。
         */
        String name = FutureMysqlVerticle.class.getName();
        logger.info("verticle name:"+name);
        vertx.deployVerticle(name, res -> {
            if (res.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(res.cause());
            }
        });
    }
}
