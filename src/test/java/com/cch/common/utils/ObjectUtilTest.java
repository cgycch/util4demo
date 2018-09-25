package com.cch.common.utils;

import java.util.Date;

import com.cch.common.utils.base.ObjectUtil;
import com.cch.common.utils.entities.BeanOne;

public class ObjectUtilTest {
	public static void main(String[] args) {
		BeanOne one = new BeanOne();
		one.setName("hello");
		one.setAge(123);
		one.setDate(new Date());
		BeanOne two  = ObjectUtil.clone(one);
		System.out.println(one.equals(two));
		System.out.println(two.getName());
		System.out.println(two.getAge());
		System.out.println(two.getDate());
	}

}
