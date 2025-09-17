package com.boylegu.springboot_vue.util;

import com.boylegu.springboot_vue.entities.Matter;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * 事项对比工具类
 * 提供事项对象之间的对比功能
 */
public class ObjectCompareUtils {

    /**
     * 比较两个对象是否相等
     */
    public static boolean objectEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        } else if (obj1 == null || obj2 == null) {
            return false;
        }
        // 特殊处理集合类型
        if (obj1 instanceof Collection && obj2 instanceof Collection) {
            return collectionEqual((Collection<?>) obj1, (Collection<?>) obj2);
        }
        return obj1.equals(obj2);
    }
    
    /**
     * 比较两个集合是否相等
     */
    public static boolean collectionEqual(Collection<?> c1, Collection<?> c2) {
        if (c1.size() != c2.size()) {
            return false;
        }

        // 对于所有集合类型，都转换为List进行比较（保持顺序）
        try {
            List<?> list1 = new ArrayList<>(c1);
            List<?> list2 = new ArrayList<>(c2);
            return list1.equals(list2);
        } catch (Exception e) {
            // 如果转换出错，回退到原始比较
            return c1.equals(c2);
        }
    }
}