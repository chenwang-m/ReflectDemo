package com.codingfuture.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtils {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql:///mydb03",
                    "root", "1234");
        } catch (SQLException e) {
            System.out.println("获取JDBC连接失败");
            return null;
        }
    }
}
