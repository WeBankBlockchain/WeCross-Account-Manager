package com.webank.wecross.account.service;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
            System.out.println("WeCross-Account-Manager start success");
        } catch (BeanCreationException e) {
            if (e.getCause().getCause()
                    instanceof org.hibernate.exception.JDBCConnectionException) {
                System.out.println("ERROR: Database connection exception, please check!");
            }
        } catch (Exception e) {
            printException(e);
        }
    }

    private static void printException(Throwable e) {
        if (e != null) {
            System.out.println("ERROR: " + e.getMessage());
            printException(e.getCause());
        }
    }
}
