package com.cicc.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class CalendarControler {

	private Credential cred;
	private OAuthHandler auth;

	private static final String APPLICATION_NAME = "Pi Control";

	private static HttpTransport httpTransport;

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static com.google.api.services.calendar.Calendar client;

	public static void main(String args[]) throws Exception {
		System.out.println("test");
		CalendarControler cal = new CalendarControler("buster52000@gmail.com");
		cal.getEventsOnDate("My Calendar", Calendar.getInstance());
	}

	public CalendarControler(String email) throws Exception {
		auth = new OAuthHandler(email);
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		reloadCredentials();
	}

	public ArrayList<String> getCalanderIDs() throws IOException, InterruptedException {
		if (auth.getExpiresInSeconds() < 5)
			reloadCredentials();
		CalendarList cList = client.calendarList().list().execute();
		ArrayList<String> cIDs = new ArrayList<String>();
		for (CalendarListEntry ent : cList.getItems())
			cIDs.add(ent.getSummary());
		return cIDs;
	}

	public ArrayList<Event> getEvents(String calID) throws IOException, InterruptedException {
		if (auth.getExpiresInSeconds() < 5)
			reloadCredentials();
		String id = null;
		for (CalendarListEntry ent : client.calendarList().list().execute().getItems())
			if (ent.getSummary().equals(calID)) {
				id = ent.getId();
				break;
			}
		if (id == null)
			return null;
		ArrayList<Event> events = new ArrayList<Event>();
		Events evts = client.events().list(id).execute();
		for (Event evt : evts.getItems())
			events.add(evt);
		return events;
	}

	public ArrayList<Event> getEventsOnDate(String calID, Calendar date) throws IOException, InterruptedException {
		ArrayList<Event> events = getEvents(calID);
		Pattern patternDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
		ArrayList<Event> evtArr = new ArrayList<Event>();
		for (Event evt : events) {
			EventDateTime eventDateTime;
			eventDateTime = evt.getStart();
			String evtDate;
			if (eventDateTime != null) {
				try {
					evtDate = eventDateTime.getDate().toStringRfc3339();
				} catch (NullPointerException e) {
					evtDate = eventDateTime.getDateTime().toStringRfc3339();
				}
				System.out.println(evtDate);
				Matcher matchDate = patternDate.matcher(evtDate);
				if (matchDate.find()) {
					String dateStr = matchDate.group();
					if (dateStr.equals(date.get(Calendar.YEAR) + "-" + ((date.get(Calendar.MONTH) + 1) <= 9 ? "0" : "") + (date.get(Calendar.MONTH) + 1) + "-" + (date.get(Calendar.DAY_OF_MONTH) <= 9 ? "0" : "") + date.get(Calendar.DAY_OF_MONTH)))
						evtArr.add(evt);
				}
			}
		}
		return evtArr;
	}

	private void reloadCredentials() throws IOException, InterruptedException {
		cred = auth.getCredentials();
		client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, cred).setApplicationName(APPLICATION_NAME).build();
	}

}
