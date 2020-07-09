package com.whj.vertx.多线程;

import org.openjdk.jol.info.ClassLayout;

import java.util.HashMap;

/**
 * java 对象内存布局
 */
public class ObjectLayout {
    public static void main(String[] args) {
        Object o = new Object();
        HashMap<String , Object> map = new HashMap<>();
        map.put("id",1);
        System.out.println(ClassLayout.parseInstance(o).toPrintable());
        synchronized (o){
            System.out.println(ClassLayout.parseInstance(o).toPrintable());
        }
    }
}
