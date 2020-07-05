package com.whj.vertx.verticle;


import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {
    public void start() {
        vertx.deployVerticle(FutureMysqlVerticle.class.getName());
    }
}
