package ds.gae.tasks;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.taskqueue.DeferredTask;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;

public class ConfirmQuotesTask implements DeferredTask {
	
	private List<Quote> quotes = new ArrayList<>();
	
	public ConfirmQuotesTask(List<Quote> quotes) {
		this.quotes.addAll(quotes);
	}

	@Override
	public void run() {
		try {
			System.out.println("Trying to confirm quotes...");
			CarRentalModel.get().confirmQuotes(quotes);
			System.out.println("Quotes confirmed!");
		} catch (ReservationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Failed confirming quotes :(");
			throw new IllegalStateException();
		}
	}

}
