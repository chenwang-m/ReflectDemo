package com.codingfuture.dao;

import com.codingfuture.annotation.Id;
import com.codingfuture.entity.Student;
import com.codingfuture.util.JDBCUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDao<T, ID> {

    private final String TABLE_NAME;
    private final String COLUMNS;
    private final Class<T> T_CLASS;
    private final Field[] FIELDS;
    private final Field ID_FIELD;

    {
        T_CLASS = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        // TODO 表名转换不严谨，没有处理驼峰格式
        TABLE_NAME = T_CLASS.getSimpleName().toLowerCase();
        //访问属性
        FIELDS = T_CLASS.getDeclaredFields();
        StringBuilder columnsBuilder = new StringBuilder();
        Field tempIdField = null;
        for (Field field : FIELDS) {
            field.setAccessible(true);
            columnsBuilder.append(field.getName());
            columnsBuilder.append(",");
            // 获取代表主键的属性
            if (field.isAnnotationPresent(Id.class)) {
                tempIdField = field;
            }
        }
        ID_FIELD = tempIdField;
        columnsBuilder.deleteCharAt(columnsBuilder.length() - 1);
        COLUMNS = columnsBuilder.toString();
    }

    public int add(T t) {
        // insert into person(id, name, age) values(null,?,?);

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < FIELDS.length; i++) {
            placeholders.append("?,");
        }
        placeholders.deleteCharAt(placeholders.length() - 1);

        String sql = String.format("insert into %s(%s) values(%s)",
                TABLE_NAME, COLUMNS, placeholders);

        try (Connection connection = JDBCUtils.getConnection()) {
            if (connection == null) {
                return 0;
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 0; i < FIELDS.length; i++) {
                ps.setObject(i + 1, FIELDS[i].get(t));
            }
            return ps.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            System.out.println("INSERT 失败");
            return 0;
        }
    }

    public List<T> findAll() {
        // select id,name,age from student
        String sql = String.format("select %s from %s", COLUMNS, TABLE_NAME);
        try (Connection connection = JDBCUtils.getConnection()) {
            if (connection == null) {
                return null;
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();

            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                T entity = T_CLASS.newInstance();

                for (Field field : FIELDS) {
                    Object fieldValue = resultSet.getObject(field.getName());
                    field.set(entity, fieldValue);
                }
                list.add(entity);
            }
            return list;
        } catch (SQLException e) {
            System.out.println("SELECT 失败");
            return null;
        } catch (InstantiationException e) {
            System.out.println("实例化异常");
            return null;
        } catch (IllegalAccessException e) {
            System.out.println("非法访问");
            return null;
        }
    }

    public T findById(ID id) {
        // select id,name,age from student where id=?

        String sql = String.format("select %s from %s where %s=?",
                COLUMNS, TABLE_NAME, ID_FIELD.getName());
        try (Connection connection = JDBCUtils.getConnection()) {
            if (connection == null) {
                return null;
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, id);
            ResultSet resultSet = ps.executeQuery();

            T entity = null;
            if (resultSet.next()) {
                entity = T_CLASS.newInstance();
                for (Field field : FIELDS) {
                    Object fieldValue = resultSet.getObject(field.getName());
                    field.set(entity, fieldValue);
                }
            }
            return entity;
        } catch (SQLException e) {
            System.out.println("SELECT 失败");
            return null;
        } catch (InstantiationException e) {
            System.out.println("实例化异常");
            return null;
        } catch (IllegalAccessException e) {
            System.out.println("非法访问");
            return null;
        }
    }

    //TODO
    public int deleteById(ID id) {
        //DELETE FROM student WHERE id = "10"
        int i = 0;
        try (Connection connection = JDBCUtils.getConnection()) {
            String sql = String.format("DELETE FROM %s WHERE %s = ?", TABLE_NAME, ID_FIELD.getName());
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // TODO
    public int updateById(T t,ID id) {
        //UPDATE student set name = ?,age = ? WHERE id = ?
        int num = 0;
        StringBuilder updatePlaceholders = new StringBuilder();
        /*for (Field field : FIELDS) {
            updatePlaceholders.append(field.getName());
            updatePlaceholders.append("=?,");
        }*/
        for (int i = 1; i < FIELDS.length; i++) {
            updatePlaceholders.append(FIELDS[i].getName());
            updatePlaceholders.append("=?,");
        }
        updatePlaceholders.deleteCharAt(updatePlaceholders.length() - 1);
        try (Connection connection = JDBCUtils.getConnection()) {
            if (connection == null) {
                return 0;
            }
            String sql = String.format("UPDATE %s set %s WHERE %s = ?",TABLE_NAME,updatePlaceholders,ID_FIELD.getName());
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 1; i < FIELDS.length; i++) {
                ps.setObject(i, FIELDS[i].get(t));
                num++;
            }
            ps.setObject(num+1,id);
            return ps.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            System.out.println("update 失败"+e.getMessage());
            return 0;
        }
    }

}
