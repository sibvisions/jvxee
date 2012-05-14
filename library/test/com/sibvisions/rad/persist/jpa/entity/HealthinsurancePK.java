package com.sibvisions.rad.persist.jpa.entity;

import java.io.Serializable;

public class HealthinsurancePK implements Serializable {

	private int id;
	private int nr;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNr() {
		return nr;
	}
	public void setNr(int nr) {
		this.nr = nr;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + nr;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HealthinsurancePK other = (HealthinsurancePK) obj;
		if (id != other.id)
			return false;
		if (nr != other.nr)
			return false;
		return true;
	}
		
}
