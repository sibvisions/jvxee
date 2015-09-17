/*
 * Copyright 2015 SIB Visions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * History
 *
 * 17.09.2015 - [RZ] - creation
 */
package com.sibvisions.rad.persist.jpa.entity.flight;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Flight
{
	private String airline;
	
	@OneToOne
	private Aircraft aircraftRegistrationNumber;
	
	@OneToOne
	private Airport airportDestination;
	
	@OneToOne
	private Airport airportOrigin;
	
	@Id
	private String flightNumber;
	
	public Flight()
	{
	}
	
	public String getAirline()
	{
		return airline;
	}
	
	public Aircraft getAircraftRegistrationNumber()
	{
		return aircraftRegistrationNumber;
	}
	
	public Airport getAirportDestination()
	{
		return airportDestination;
	}
	
	public Airport getAirportOrigin()
	{
		return airportOrigin;
	}
	
	public String getFlightNumber()
	{
		return flightNumber;
	}
	
	public void setAirline(String pAirline)
	{
		airline = pAirline;
	}
	
	public void setAircraftRegistrationNumber(Aircraft pAircraftRegistrationNumber)
	{
		aircraftRegistrationNumber = pAircraftRegistrationNumber;
	}
	
	public void setAirportDestination(Airport pAirportDestination)
	{
		airportDestination = pAirportDestination;
	}
	
	public void setAirportOrigin(Airport pAirportOrigin)
	{
		airportOrigin = pAirportOrigin;
	}
	
	public void setFlightNumber(String pFlightNumber)
	{
		flightNumber = pFlightNumber;
	}
	
}
