package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;
 
public class CarRentalModel {
	
	private static CarRentalModel instance;
	
	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}
	
	private CarRentalCompany getCompany(String crcName, EntityManager em) {
		Query q = em.createQuery("SELECT c FROM " + CarRentalCompany.class.getName() + " c WHERE c.name = :crcName", CarRentalCompany.class)
				.setParameter("crcName", crcName);
		return (CarRentalCompany)q.getSingleResult();
	}

		
	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param 	crcName
	 * 			the car rental company
	 * @return	The list of car types (i.e. name of car type), available
	 * 			in the given car rental company.
	 */
	public Set<String> getCarTypesNames(String crcName) {
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			CarRentalCompany company = getCompany(crcName, em);
			
			Set<String> names = new HashSet<>();
			for (CarType type : company.getAllCarTypes()) {
				names.add(type.getName());
			}
			
			return names;
		}
		finally {
			em.close();
		}
	}

    /**
     * Get all registered car rental companies
     *
     * @return	the list of car rental companies
     */
    public Collection<String> getAllRentalCompanyNames() {
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			Query q = em.createQuery("SELECT c.name FROM " + CarRentalCompany.class.getName() + " c" );
			return q.getResultList();
		}
		finally {
			em.close();
		}
    	//return CRCS.keySet();
    }
	
	/**
	 * Create a quote according to the given reservation constraints (tentative reservation).
	 * 
	 * @param	company
	 * 			name of the car renter company
	 * @param	renterName 
	 * 			name of the car renter 
	 * @param 	constraints
	 * 			reservation constraints for the quote
	 * @return	The newly created quote.
	 *  
	 * @throws ReservationException
	 * 			No car available that fits the given constraints.
	 */
    synchronized public Quote createQuote(String company, String renterName, ReservationConstraints constraints) throws ReservationException {
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			CarRentalCompany crc =  getCompany(company, em);
	    	Quote out = null;

	        if (crc != null) {
	            out = crc.createQuote(constraints, renterName);
	        } else {
	        	throw new ReservationException("CarRentalCompany not found.");    	
	        }
	        
	        return out;
		}
		finally {
			em.close();
		}
    }
    
	/**
	 * Confirm the given quote.
	 *
	 * @param 	q
	 * 			Quote to confirm
	 * 
	 * @throws ReservationException
	 * 			Confirmation of given quote failed.	
	 */
	synchronized public void confirmQuote(Quote q) throws ReservationException {
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		Reservation res = null;
		try {
			CarRentalCompany crc =  getCompany(q.getRentalCompany(), em);
	    	res = crc.confirmQuote(q);
		}
		catch (ReservationException e) {
			if (res != null) {
				CarRentalCompany crc =  getCompany(res.getRentalCompany(), em);
		    	crc.cancelReservation(res);
			}
			throw e;
		}
		finally {
			em.close();
		}
	}
	
    /**
	 * Confirm the given list of quotes
	 * 
	 * @param 	quotes 
	 * 			the quotes to confirm
	 * @return	The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws 	ReservationException
	 * 			One of the quotes cannot be confirmed. 
	 * 			Therefore none of the given quotes is confirmed.
	 */
	synchronized public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {    	
		//EntityManager em = ds.gae.EMF.get().createEntityManager();
		//EntityTransaction tx = em.getTransaction();
		//tx.begin();
		List<Reservation> ress = new ArrayList<>();
		try {
			for (Quote q : quotes) {
				EntityManager em = ds.gae.EMF.get().createEntityManager();
				
				CarRentalCompany crc =  getCompany(q.getRentalCompany(), em);
				ress.add(crc.confirmQuote(q));
				
				em.close();
			}
	    	//tx.commit();
	    	return ress;
		}
		catch (ReservationException e) {
			for (Reservation r : ress) {
				EntityManager em = ds.gae.EMF.get().createEntityManager();
				
				CarRentalCompany crc =  getCompany(r.getRentalCompany(), em);
				crc.cancelReservation(r);
				
				em.close();
			}
			throw e;
		}
		finally {
			/*if (tx.isActive()) {
				tx.rollback();
			}*/
			//em.close();
		}
    	
    	//return null;
    }
	
	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param 	renter
	 * 			name of the car renter
	 * @return	the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			/*Query q = em.createQuery("SELECT r "
					+ "FROM " + Reservation.class.getName() + " r "
					+ "WHERE r.carRenter = :renter",
					Reservation.class)
				.setParameter("renter", renter);
			return q.getResultList();*/
			List<Reservation> out = new ArrayList<Reservation>();
			
			Query q = em.createQuery("SELECT c FROM " + CarRentalCompany.class.getName() + " c", CarRentalCompany.class);
			List<CarRentalCompany> companies = q.getResultList();
	    	for (CarRentalCompany crc : companies) {
	    		for (Car c : crc.getCars()) {
	    			for (Reservation r : c.getReservations()) {
	    				if (r.getCarRenter().equals(renter)) {
	    					out.add(r);
	    				}
	    			}
	    		}
	    	}
	    	
	    	return out;
		}
		finally {
			em.close();
		}
    }

    /**
     * Get the car types available in the given car rental company.
     *
     * @param 	crcName
     * 			the given car rental company
     * @return	The list of car types in the given car rental company.
     */
    public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			/*Query q = em.createQuery("SELECT ct "
					+ "FROM CarRentalCompany c, IN(c.carTypes) ct "
					+ "WHERE c.name LIKE :crcName")
				.setParameter("crcName", crcName);*/
			return getCompany(crcName, em).getAllCarTypes();
		}
		finally {
			em.close();
		}
    }
	
    /**
     * Get the list of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A list of car IDs of cars with the given car type.
     */
    public Collection<Integer> getCarIdsByCarType(String crcName, CarType carType) {
    	Collection<Integer> out = new ArrayList<Integer>();
    	for (Car c : getCarsByCarType(crcName, carType)) {
    		out.add(c.getId());
    	}
    	return out;
    }
    
    /**
     * Get the amount of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A number, representing the amount of cars of the given car type.
     */
    public int getAmountOfCarsByCarType(String crcName, CarType carType) {
    	return this.getCarsByCarType(crcName, carType).size();
    }

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String crcName, CarType carType) {				
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			/*Query q = em.createQuery("SELECT c "
					+ "FROM Car c "
					+ "WHERE c.type = :type" ) // car nolonger knows it's type
				.setParameter("type", carType);
			return q.getResultList();*/
			return getCompany(crcName, em).getCars(carType.getName());
		}
		finally {
			em.close();
		}
	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param 	renter
	 * 			the car renter
	 * @return	True if the number of reservations of the given car renter is higher than 0.
	 * 			False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;		
	}	
}