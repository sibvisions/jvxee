package com.sibvisions.rad.persist.jpa.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;

@IdClass(HealthinsurancePK.class)
@Entity
public class Healthinsurance implements Serializable {

    private int id;
    
    private int nr;
    
    private String health_insurance;
    private String street;
    private int zip;
    private String city;
    
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	@Id
	public int getNr() {
		return nr;
	}
	public void setNr(int nr) {
		this.nr = nr;
	}
	@Column(unique=true)
	public String getHealth_insurance() {
		return health_insurance;
	}
	public void setHealth_insurance(String health_insurance) {
		this.health_insurance = health_insurance;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	@Column(length=4) 
	public int getZip() {
		return zip;
	}
	public void setZip(int zip) {
		this.zip = zip;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}

}
