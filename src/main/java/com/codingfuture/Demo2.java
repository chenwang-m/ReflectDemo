package com.codingfuture;

import com.codingfuture.dao.StudentDao;
import com.codingfuture.entity.Student;

public class Demo2 {

    public static void main(String[] args) throws Exception {
        Student student = new Student();
        //student.setId(50);
        student.setName("王六");
        student.setAge(40);
        StudentDao studentDao = new StudentDao();
        //studentDao.add(student);
//        System.out.println(studentDao.findById(10));
//        System.out.println(studentDao.deleteById(10));
        System.out.println(studentDao.updateById(student,40));
        //studentDao.updateById(student,student.getId());
    }
}
