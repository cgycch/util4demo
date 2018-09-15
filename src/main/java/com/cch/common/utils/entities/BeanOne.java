package com.cch.common.utils.entities;

import java.util.Date;

import com.cch.common.utils.excel.annotation.ExcelField;
import com.cch.common.utils.excel.annotation.ExcelField.Align;

public class BeanOne {
	private String name;
	private String pass;
	private int age;
	private double price;
	private boolean flag;
	private Date date;
	
	@ExcelField(title="NAME", attrName="name", align=Align.CENTER, sort=10)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@ExcelField(title="PASSWORD", attrName="pass", align=Align.CENTER, sort=20)
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	@ExcelField(title="AGE", attrName="age", align=Align.LEFT, sort=30)
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	@ExcelField(title="PRICE", attrName="price", align=Align.CENTER, sort=40, dataFormat="0.0")
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@ExcelField(title="FLAG", attrName="flag", align=Align.CENTER, sort=50, type=ExcelField.Type.IMPORT)
	public boolean getFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	@ExcelField(title="DATE", attrName="date", align=Align.CENTER, sort=60, type=ExcelField.Type.EXPORT, dataFormat="yyyy-MM-dd HH:mm")
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	
}
