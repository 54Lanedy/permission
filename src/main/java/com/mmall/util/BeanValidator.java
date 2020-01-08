package com.mmall.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.exception.ParamException;
import org.apache.commons.collections.MapUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

/**
 * 基于注解（@NotNull、@Max、@Min）的参数校验类
 * Created by liyue
 * Time 2019/9/16 22:53
 */
public class BeanValidator {
    private static ValidatorFactory validatorFactory=Validation.buildDefaultValidatorFactory();

    //校验参数
    public static <T> Map<String,String> validate(T t,Class... groups){
        Validator validator = validatorFactory.getValidator();
        Set validateResult = validator.validate(t, groups);
        if (validateResult.isEmpty()) {
            return Collections.emptyMap();
        }else {
            LinkedHashMap errors=Maps.newLinkedHashMap();
            Iterator iterator = validateResult.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation violation = (ConstraintViolation) iterator.next();
                errors.put(violation.getPropertyPath().toString(),violation.getMessage());
            }
            return errors;
        }
    }

    //校验集合
    public static Map<String,String> validateList(Collection<?> collection) {
        //空值检查
        Preconditions.checkNotNull(collection);
        Iterator iterator = collection.iterator();
        Map errors;
        do {
            if (!iterator.hasNext()) {
                return Collections.emptyMap();
            }
            Object object = iterator.next();
            errors=validate(object,new Class[0]);
        } while (errors.isEmpty()) ;
        return errors;
    }

    public static Map<String,String> validateObject(Object first ,Object...objects){
        if (objects!=null && objects.length>0) {
            return validateList(Lists.asList(first,objects));
        }
        else {
            return validate(first,new Class[0]);
        }

    }

    public static void check(Object param) throws ParamException{
        Map<String, String> map = BeanValidator.validateObject(param);
        //map不为空时说明校验不通过
        if (MapUtils.isNotEmpty(map)) {
            throw  new ParamException(map.toString());
        }
    }

}
