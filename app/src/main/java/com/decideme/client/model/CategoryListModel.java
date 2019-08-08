package com.decideme.client.model;

import java.util.ArrayList;

public class CategoryListModel {

	private String response;
	private String message;
	private ArrayList<CategoryList> cat_data = new ArrayList<CategoryList>();

	public String getResponse() {
		return response;
	}

	public String getMessage() {
		return message;
	}

	public ArrayList<CategoryList> getCat_data() {
		return cat_data;
	}
}
