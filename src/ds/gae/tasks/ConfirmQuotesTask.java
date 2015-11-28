package ds.gae.tasks;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.utils.SystemProperty;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class ConfirmQuotesTask implements DeferredTask {
	
	private List<Quote> quotes = new ArrayList<>();
	
	public ConfirmQuotesTask(List<Quote> quotes) {
		this.quotes.addAll(quotes);
	}

	@Override
	public void run() {
		try {
			System.out.println("Trying to confirm quotes...");
			
			List<Reservation> ress = CarRentalModel.get().confirmQuotes(quotes);
			
			//TODO: Add info about reservations
			StringBuilder body = new StringBuilder();
			body.append("Your reservations were successful!");
			for (Reservation res : ress) {
				body.append("\n\n");
				body.append(res.toString());
			}
			
			sendMail("Reservations successful!", body.toString());
			System.out.println("Quotes confirmed!");
		} catch (ReservationException e) {
			sendMail("Reservations failed.", "Something went wrong during the reservation of your quotes, please retry.");
			System.out.println("Failed confirming quotes :(");
		}
	}
	
	private void sendMail(String subject, String body) {
		try {
			System.out.println("Sending email: ");
		    System.out.println("Subject: " + subject);
		    System.out.println(body);
			
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);

			Message msg = new MimeMessage(session);
			//In a real app the 'from' email should be authorized in the GAE Settings Page online.
			msg.setFrom(new InternetAddress("info@carrental.appspotmail.com", "Car Rental Information Office"));
		    msg.addRecipient(Message.RecipientType.TO, new InternetAddress("pablo.bollansee@gmail.com", "Mr. You"));
		    msg.setSubject(subject);
		    msg.setText(body);
		    //for some reason the logging of the email doesn't show the body, however it does show the data-length,
		    // which is >0, and equal to the message I set, so it does seem to be set correctly, just not shown correctly in the log
		    Transport.send(msg);
		} catch (UnsupportedEncodingException | MessagingException e) {
			e.printStackTrace();
			System.out.println("Failed sending confirmation email.");
		}
	}

}
