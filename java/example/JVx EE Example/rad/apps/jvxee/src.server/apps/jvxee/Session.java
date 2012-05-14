/*
 * Copyright 2009 SIB Visions GmbH
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
 * 09.05.2012 - [SW] - creation
 */
package apps.jvxee;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


/**
 * The LCO for the session.
 * <p/>
 * @author Stefan Wurm
 */
public class Session extends Application {
	
	public EntityManager getEntityManager() throws Exception {
		
	    EntityManagerFactory emf = Persistence.createEntityManagerFactory("jvxee");   
	    EntityManager entityManager = emf.createEntityManager();
		
	    return entityManager;
		    
	}		

}
