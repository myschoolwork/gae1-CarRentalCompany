package ds.gae.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import ds.gae.ReservationException;

@Entity
public class CarRentalCompany {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
	
	@Id
	private String name;
	
	//@OneToMany(cascade = CascadeType.ALL)
	//private Set<Car> cars;
	
	@OneToMany(cascade = CascadeType.ALL)
	//private Map<Car,CarType> cars = new HashMap<>();
	private List<CarTypeCarMapping> cars = new ArrayList<>();
	
	//@OneToMany(cascade = CascadeType.ALL)
	//private Map<String,CarType> carTypes = new HashMap<String, CarType>();

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, List<CarTypeCarMapping> cars) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		setName(name);
		this.cars = cars;
	}

	/********
	 * NAME *
	 ********/

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	/*************
	 * CAR TYPES *
	 *************/

	public Collection<CarType> getAllCarTypes() {
		Set<CarType> types = new HashSet<CarType>();
		for (CarTypeCarMapping mapping : cars) {
			types.add(mapping.getType());
		}
		return types;
		//return new HashSet<>(cars.values());
	}
	
	private Map<String, CarType> getNameToCarTypeMapping() {
		Map<String, CarType> mapping = new HashMap<>();
		for (CarType type : getAllCarTypes()) {
			mapping.put(type.getName(), type);
		}
		return mapping;
	}
	
	public CarType getCarType(String carTypeName) {
		for (CarType type : getAllCarTypes()) {
			if (type.getName().equals(carTypeName)) {
				return type;
			}
		}
		
		//if(carTypes.containsKey(carTypeName))
		//	return carTypes.get(carTypeName);
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		//if(carTypes.containsKey(carTypeName))
		//	return getAvailableCarTypes(start, end).contains(carTypes.get(carTypeName));
		
		Map<String, CarType> mapping = getNameToCarTypeMapping();
		if(mapping.containsKey(carTypeName)) {
			return getAvailableCarTypes(start, end).contains(mapping.get(carTypeName));
		}
		
		throw new IllegalArgumentException("<" + carTypeName + "> No car type of name " + carTypeName);
	}
	
	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		/*for (Car car : cars) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}*/
		for (CarTypeCarMapping mapping : cars) {
			for (Car car : mapping.getCars()) {
				if (car.isAvailable(start, end)) {
					availableCarTypes.add(mapping.getType());
					break;
				}
			}
		}
		return availableCarTypes;
	}
	
	/*********
	 * CARS *
	 *********/
	
	private Car getCar(int uid) {
		for (CarTypeCarMapping mapping : cars) {
			for (Car car : mapping.getCars()) {
				if (car.getId() == uid) {
					return car;
				}
			}
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}
	
	public Set<Car> getCars() {
		Set<Car> carSet = new HashSet<>();
		
		for (CarTypeCarMapping mapping : cars) {
			carSet.addAll(mapping.getCars());
		}
		
    	return carSet;
    }
	
	public List<Car> getCars(String carType) {
		List<Car> carsOfType = new LinkedList<Car>();
		for (CarTypeCarMapping mapping : cars) {
			if (mapping.getType().getName().equals(carType)) {
				carsOfType.addAll(mapping.getCars());
			}
		}
		return carsOfType;
	}
	
	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (CarTypeCarMapping mapping : cars) {
			if (mapping.getType().getName().equals(carType)) {
				for (Car car : mapping.getCars()) {
					if (car.isAvailable(start, end)) {
						availableCars.add(car);
					}
				}
			}
		}
		return availableCars;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Quote createQuote(ReservationConstraints constraints, String client)
			throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}", 
                        new Object[]{name, client, constraints.toString()});
		
		CarType type = getCarType(constraints.getCarType());
		
		if(!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
				+ "> No cars available to satisfy the given constraints.");
		
		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());
		
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
						/ (1000 * 60 * 60 * 24D));
	}

	public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if(availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
	                + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int)(Math.random()*availableCars.size()));
		
		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	public void cancelReservation(Reservation res) {
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar(res.getCarId()).removeReservation(res);
	}
}