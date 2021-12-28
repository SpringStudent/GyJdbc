package com.gysoft.jdbc;

import com.gysoft.jdbc.annotation.Table;

/**
 * @author 周宁
 * @Date 2019-01-04 11:18
 */
@Table(name = "tb_role")
public class Role {

    public static void main(String[] args) {
        int idx = 0;
        int num = 0;
        while(idx<35){
            num +=1;
            if(num%3==0&&num%7==0){
                idx +=1;
            }
        }
        System.out.println(num);
    }
}
