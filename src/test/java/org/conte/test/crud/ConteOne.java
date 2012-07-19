package org.conte.test.crud;

import org.conte.annotation.BelongsTo;
import org.conte.model.ActiveRecord;

public class ConteOne extends ActiveRecord{
	private int id;
	private String name;
	
	@BelongsTo("conteId")
	private Conte conte;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Conte getConte() {
		return conte;
	}
	public void setConte(Conte conte) {
		this.conte = conte;
	}
}
