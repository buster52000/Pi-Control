package com.cicc.google.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cicc.google.OAuthHandler;
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

	public CalendarControler(String email) throws Exception {
		auth = new OAuthHandler(email);
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		reloadCredentials();
	}

	public ArrayList<String> getCalendarIDs() throws IOException, InterruptedException {
		if (auth.getExpiresInSeconds() < 5)
			reloadCredentials();
		CalendarList cList = client.calendarList().list().execute();
		ArrayList<String> cIDs = new ArrayList<String>();
		for (CalendarListEntry ent : cList.getItems())
			cIDs.add(ent.getId());
		return cIDs;
	}

	public ArrayList<String[]> getCalendarSummaries() throws IOException, InterruptedException {
		if (auth.getExpiresInSeconds() < 5)
			reloadCredentials();
		CalendarList cList = client.calendarList().list().execute();
		ArrayList<String[]> cSum = new ArrayList<String[]>();
		for (CalendarListEntry ent : cList.getItems())
			cSum.add(new String[] { ent.getSummary(), ent.getId() });
		return cSum;
	}

	public ArrayList<String[]> getCalendarNames() throws IOException, InterruptedException {
		if (auth.getExpiresInSeconds() < 5)
			reloadCredentials();
		CalendarList cList = client.calendarList().list().execute();
		ArrayList<String[]> cNames = new ArrayList<String[]>();
		for (CalendarListEntry ent : cList.getItems()) {
			if (ent.getSummaryOverride() != null)
				cNames.add(new String[] { ent.getSummaryOverride(), ent.getId() });
			else
				cNames.add(new String[] { ent.getSummary(), ent.getId() });
		}
		return cNames;
	}
	
	public ArrayList<Event> getEvents(String calID) throws IOException, InterruptedException {
		if (auth.getExpiresInSeconds() < 5)
			reloadCredentials();
		ArrayList<Event> events = new ArrayList<Event>();
		Events evts = client.events().list(calID).execute();
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
