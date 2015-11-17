package ds.gae.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;

public class CarRentalServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// This will be invoked as part of a warming request, 
		// or the first user request if no warming request was invoked.
						
		// check if dummy data is available, and add if necessary
		if(!isDummyDataAvailable()) {
			addDummyData();
		}
	}
	
	private boolean isDummyDataAvailable() {
		// FIXEDME: use persistence instead
		//return CarRentalModel.get().CRCS.containsKey("Hertz");
		
		// I assume if any company is present the dummy data is loaded.
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
			Query q = em.createQuery("SELECT c FROM CarRentalCompany c" );
			return q.getResultList().size() > 0;
			//return CarRentalModel.get().CRCS.containsKey("Hertz");
		}
		finally {
			em.close();
		}
	}
	
	private void addDummyData() {
		loadRental("Hertz","hertz.csv");
        loadRental("Dockx","dockx.csv");
	}
	
	private void loadRental(String name, String datafile) {
		Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO, "loading {0} from file {1}", new Object[]{name, datafile});
        
		EntityManager em = ds.gae.EMF.get().createEntityManager();
		try {
        	
			Map<Car,CarType> cars = loadData(name, datafile);
            CarRentalCompany company = new CarRentalCompany(name, cars);
            
    		// FIXEDME: use persistence instead
            //CarRentalModel.get().CRCS.put(name, company);
            em.persist(company);

        } catch (NumberFormatException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
		finally {
			em.close();
		}
	}
	
	public static Map<Car,CarType> loadData(String name, String datafile) throws NumberFormatException, IOException {
		// FIXEDME: adapt the implementation of this method to your entity structure
		
		Map<Car,CarType> cars = new HashMap<>();
		int carId = 1;

		//open file from jar
		BufferedReader in = new BufferedReader(new InputStreamReader(CarRentalServletContextListener.class.getClassLoader().getResourceAsStream(datafile)));
		//while next line exists
		while (in.ready()) {
			//read line
			String line = in.readLine();
			//if comment: skip
			if (line.startsWith("#")) {
				continue;
			}
			//tokenize on ,
			StringTokenizer csvReader = new StringTokenizer(line, ",");
			//create new car type from first 5 fields
			CarType type = new CarType(csvReader.nextToken(),
					Integer.parseInt(csvReader.nextToken()),
					Float.parseFloat(csvReader.nextToken()),
					Double.parseDouble(csvReader.nextToken()),
					Boolean.parseBoolean(csvReader.nextToken()));
			//create N new cars with given type, where N is the 5th field
			for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
				cars.put(new Car(carId++/*, type*/), type);
			}
		}

		return cars;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// App Engine does not currently invoke this method.
	}
}