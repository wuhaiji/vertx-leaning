package com.whj.vertx.verticle;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HttpServerRequestImpl;

public class MainVerticle extends AbstractVerticle {
    public void start() {
        vertx.deployVerticle(JsonVerticle.class.getName());
    }
}
