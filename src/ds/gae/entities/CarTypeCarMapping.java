package ds.gae.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.google.appengine.api.datastore.Key;

@Entity
public class CarTypeCarMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	@OneToOne(cascade = CascadeType.ALL)
	private CarType type;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<Car> cars;
	
	public CarTypeCarMapping(CarType type, List<Car> cars) {
		this.type = type;
		this.cars = cars;
	}
	
	CarType getType() {
		return type;
	}
	
	List<Car> getCars() {
		return cars; //safer to return copy
	}
	
}
