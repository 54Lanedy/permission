package com.mmall.util;

import org.springframework.web.servlet.tags.EscapeBodyTag;

import java.util.Date;
import java.util.Random;

/**
 * 密码生成工具,
 * Created by liyue
 * Time 2019/9/22 13:57
 */
public class PasswordUtil {

    public static final String[] word = {
            "a","b","c","d","e","f","g","h","j","k","m","n","p","q",
            "r","s","t","u","v","w","x","y","z",
            "A","B","C","D","E","F","G","H","J","K","M","N","P","Q",
            "R","S","T","U","V","W","X","Y","Z"
    };

    public static final String[] num = {
        "2","3","4","5","6","7","8","9"
    };

    public static String randomPassword(){
        StringBuffer stringBuffer = new StringBuffer();
        Random random = new Random(new Date().getTime());
        boolean flag = false;
        //长度：至少8位+随机2位,长度在8-10之间
        int length = random.nextInt(3) + 8;
        //生成每个字符
        for (int i = 0; i < length; i++) {
            if (flag) {
                stringBuffer.append(num[random.nextInt(num.length)]);
            }else {
                stringBuffer.append(word[random.nextInt(word.length)]);
            }
            //密码：数字字母交叉
            flag=!flag;
        }
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        System.out.println(randomPassword());
    }
}
