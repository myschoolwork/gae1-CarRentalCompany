package ds.gae.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;
import ds.gae.tasks.ConfirmQuotesTask;
import ds.gae.view.ViewTools;
import ds.gae.view.JSPSite;

@SuppressWarnings("serial")
public class ConfirmQuotesServlet extends HttpServlet {
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession();
		HashMap<String, ArrayList<Quote>> allQuotes = (HashMap<String, ArrayList<Quote>>) session.getAttribute("quotes");
		
		String email = (String)session.getAttribute("email");

		//try {
			ArrayList<Quote> qs = new ArrayList<Quote>();
			
			for (String crcName : allQuotes.keySet()) {
				qs.addAll(allQuotes.get(crcName));
			}
			
			// DONE Use google queues
			Queue queue = QueueFactory.getDefaultQueue();
			//queue.add(TaskOptions.Builder.withUrl("/worker").param("quotes", qs)); //use deferred task
			// delay 5 seconds for testing
			queue.add(TaskOptions.Builder.withPayload(new ConfirmQuotesTask(qs, email))
				      .etaMillis(System.currentTimeMillis() + 5 * 1000));

			//CarRentalModel.get().confirmQuotes(qs); //done in task now
			
			// Clear current quotes
			session.setAttribute("quotes", new HashMap<String, ArrayList<Quote>>());
			
			// DONE
			// If you wish confirmQuotesReply.jsp to be shown to the client as
			// a response of calling this servlet, please replace the following line 
			// with 
			resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
			//resp.sendRedirect(JSPSite.CREATE_QUOTES.url());
			
		/*} catch (ReservationException e) {
			session.setAttribute("errorMsg", ViewTools.encodeHTML(e.getMessage()));
			resp.sendRedirect(JSPSite.RESERVATION_ERROR.url());				
		}*/
	}
}
