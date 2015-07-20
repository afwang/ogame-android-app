/*
	Copyright 2014 Alexander Wang

	This file is part of Ogame on Android.

	Ogame on Android is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Ogame on Android is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wikaba.ogapp.agent.parsers;

import com.wikaba.ogapp.agent.FleetAndResources;
import com.wikaba.ogapp.agent.models.FleetEvent;
import com.wikaba.ogapp.agent.OgameResources;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FleetEventParser extends AbstractParser<List<FleetEvent>> {

	public List<FleetEvent> parse(String raw, OgameResources resources) {
		List<FleetEvent> eventList;
		try {
			byte[] byteArray = raw.getBytes("UTF-8");
			ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
			eventList = parse(byteStream, resources);
		} catch(UnsupportedEncodingException e) {
			eventList = null;
		}
		return eventList;
	}

	public List<FleetEvent> parse(InputStream stream, OgameResources resources) {
		List<FleetEvent> eventList = new LinkedList<>();

		try {
			XmlPullParserFactory xppfactory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = xppfactory.newPullParser();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			xpp.setInput(reader);
			xpp.defineEntityReplacementText("ndash", "-");
			xpp.defineEntityReplacementText("nbsp", " ");

			FleetEvent lastScannedEvent = null;
			//To make the next for loop look easier to read and understand, we get to the first instance of
			//<tr class="eventFleet">
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = xpp.getName();

				if (tagName != null && tagName.equals("tr") && hasAttrValue(xpp, "class", "eventFleet")) {
					lastScannedEvent = new FleetEvent();
					int attrCount = xpp.getAttributeCount();
					for (int index = 0; index < attrCount; index++) {
						String attrName = xpp.getAttributeName(index);
						if (attrName.equals("data-mission-type")) {
							String value = xpp.getAttributeValue(index);
							lastScannedEvent.data_mission_type = Integer.valueOf(value);
						} else if (attrName.equals("data-return-flight")) {
							String value = xpp.getAttributeValue(index);
							lastScannedEvent.data_return_flight = Boolean.valueOf(value);
						} else if (attrName.equals("data-arrival-time")) {
							String value = xpp.getAttributeValue(index);
							lastScannedEvent.data_arrival_time = Long.valueOf(value);
						}
					}
					//We must call next() here before breaking. Otherwise, the next loop
					//will add the incomplete FleetEvent object since it will detect
					//the same <tr...> element and think it's a new event when it's
					//still the same first event.
					xpp.next();
					break;
				}
				try {
					eventType = xpp.next();
				} catch (XmlPullParserException e) {
						/* For some strange reason, the emulator can reach this catch block with
						 * e set to null. (Why and how?) Might be a debugger bug
						 */
					System.out.println("Analysis of an error: " + e + '\n' + e.getMessage());
					e.printStackTrace();
				} catch (ArrayIndexOutOfBoundsException e) {
					//This exception occurs near the end of the document, but it is not something that
					//should stop the app over.
					System.err.println("Possibly reached end of document (HTML is painful): " + e + '\n' + e.getMessage());
					e.printStackTrace();
					eventType = XmlPullParser.END_DOCUMENT;
				}
			}

			//No events scanned. Just return.
			if (lastScannedEvent == null)
				return eventList;

			eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				//Begin parsing for fleet events.
				if (eventType == XmlPullParser.START_TAG) {
					String tagName = xpp.getName();
					tagName = (tagName == null) ? "" : tagName;

					if (tagName.equals("tr") && hasAttrValue(xpp, "class", "eventFleet")) {
						eventList.add(lastScannedEvent);
						lastScannedEvent = new FleetEvent();

						int attrCount = xpp.getAttributeCount();
						for (int index = 0; index < attrCount; index++) {
							String attrName = xpp.getAttributeName(index);
							if (attrName.equals("data-mission-type")) {
								String value = xpp.getAttributeValue(index);
								lastScannedEvent.data_mission_type = Integer.valueOf(value);
							} else if (attrName.equals("data-return-flight")) {
								String value = xpp.getAttributeValue(index);
								lastScannedEvent.data_return_flight = Boolean.valueOf(value);
							} else if (attrName.equals("data-arrival-time")) {
								String value = xpp.getAttributeValue(index);
								lastScannedEvent.data_arrival_time = Long.valueOf(value);
							}
						}
					} else if (tagName.equals("td")) {
						if (hasAttrValue(xpp, "class", "originFleet")) {
							/* Example from the extracted response sample:
							 * 	<td class="originFleet"> <--XPP pointer is here currently
									<span class="tooltip" title="A Whole New World">
										<figure class="planetIcon planet"></figure>
										A Whole New World
									</span>
								</td>
							 */
							tagName = xpp.getName();
							int htmlevent = 0;
							int counter = 0;
							//From the response extract, we need 5 next()'s to get to the text we need.
							//Set a hard limit just in case.
							while ((htmlevent != XmlPullParser.END_TAG || !tagName.equalsIgnoreCase("figure")) && counter < 5) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}

							xpp.next();
							if (xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.originFleet = xpp.getText();
								if (lastScannedEvent.originFleet != null)
									lastScannedEvent.originFleet = lastScannedEvent.originFleet.trim();
							}
						} else if (hasAttrValue(xpp, "class", "coordsOrigin")) {
							/* Example:
							 * <td class="coordsOrigin"> <-- XPP pointer here
									<a href="http://s125-en.ogame.gameforge.com/game/index.php?page=galaxy&galaxy=1&system=373" target="_top">
										[1:373:8]
									</a>
								</td>
							 */
							tagName = xpp.getName();

							//We need 2 next()'s to get to the <a> element. Use a hard limit just in case.
							int htmlevent = 0;
							int counter = 0;
							while ((htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("a")) && counter < 2) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}

							xpp.next();
							if (xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.coordsOrigin = xpp.getText();
								if (lastScannedEvent.coordsOrigin != null)
									lastScannedEvent.coordsOrigin = lastScannedEvent.coordsOrigin.trim();
							}
						} else if (hasAttrValue(xpp, "class", "icon_movement") || hasAttrValue(xpp, "class", "icon_movement_reserve")) {
							//Have to parse another HTML snippet. This HTML is both encoded to not confuse
							//the parser, so it must be decoded first. Then it must be put through another
							//XmlPullParser to gather the data.
							/* Example:
							 * <td class="icon_movement"> <-- xpp point here
							 * 	<span class="blah blah blah"
							 * 		title="bunch of escaped HTML we have to unescape"
							 * 		data-federation-user-id="">
							 * 			&nbsp;
							 * 	</span>
							 * </td>
							 */
							tagName = xpp.getName();
							int htmlevent = 0;
							tagName = (tagName == null) ? "" : tagName;
							while (htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("span")) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
							}

							Map<String, Long> fleetData = null;
							if (xpp.getEventType() == XmlPullParser.START_TAG) {
								int attrSize = xpp.getAttributeCount();
								String titleValue = null;
								for (int index = 0; index < attrSize; index++) {
									String attrName = xpp.getAttributeName(index);
									if (attrName.equals("title")) {
										titleValue = xpp.getAttributeValue(index);
									}
								}

								if (titleValue != null) {
									fleetData = parseFleetResComposition(titleValue);
								}
							}

							//prevent null data
							if (fleetData != null) {
								lastScannedEvent.fleetResources.putAll(fleetData);
							}
						} else if (hasAttrValue(xpp, "class", "destFleet")) {
							/* Example:
							 * <td class="destFleet"> <-- XPP pointer here
									<span class="tooltip" title="Slot 8 unavailable">
										<figure class="planetIcon planet"></figure>
										Slot 8 unavailable
									</span>
								</td>
							 */
							int counter = 0;
							int htmlevent = 0;
							tagName = xpp.getName();
							tagName = (tagName == null) ? "" : tagName;
							while ((htmlevent != XmlPullParser.END_TAG || !tagName.equalsIgnoreCase("figure")) && counter < 5) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}
							xpp.next();
							if (xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.destFleet = xpp.getText();
								if (lastScannedEvent.destFleet != null)
									lastScannedEvent.destFleet = lastScannedEvent.destFleet.trim();
							}
						} else if (hasAttrValue(xpp, "class", "destCoords")) {
							/* Example:
							 * <td class="destCoords"> <--XPP pointer here
									<a href="http://s125-en.ogame.gameforge.com/game/index.php?page=galaxy&galaxy=1&system=204" target="_top">
										[1:204:8]
									</a>
								</td>
							 */

							int counter = 0;
							int htmlevent = 0;
							tagName = xpp.getName();
							tagName = (tagName == null) ? "" : tagName;
							while ((htmlevent != XmlPullParser.START_TAG || !tagName.equalsIgnoreCase("a")) && counter < 2) {
								htmlevent = xpp.next();
								tagName = xpp.getName();
								counter++;
							}

							xpp.next();
							if (xpp.getEventType() == XmlPullParser.TEXT) {
								lastScannedEvent.destCoords = xpp.getText();
								if (lastScannedEvent.destCoords != null)
									lastScannedEvent.destCoords = lastScannedEvent.destCoords.trim();
							}
						}
					}
				}
				try {
					eventType = xpp.next();
				} catch (XmlPullParserException e) {
					System.out.println("Analysis of an error: " + e + '\n' + e.getMessage());
					e.printStackTrace();
					eventType = XmlPullParser.END_DOCUMENT;
				} catch (ArrayIndexOutOfBoundsException e) {
					//This exception occurs near the end of the document, but it is not something that
					//should stop the app over.
					System.err.println("Possibly reached end of document (HTML is painful): " + e + '\n' + e.getMessage());
					e.printStackTrace();
					eventType = XmlPullParser.END_DOCUMENT;
				}
			}
			eventList.add(lastScannedEvent);
		} catch (XmlPullParserException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
		}
		return eventList;
	}

	/**
	 * Unescapes the HTML-escaped data in the parameter string htmlEncodedData.
	 * The string is then parsed with an XmlPullParser to extract fleet and resource
	 * data. The data is inserted into a Map<String, Long> object. This Map is then
	 * returned.
	 *
	 * @param htmlEncodedData - HTML-escaped string containing the details of the fleet breakdown and composition
	 * @return a Map<String, Long> object containing fleet and resource composition. Keys are listed in
	 * class FleetAndResources
	 */
	private Map<String, Long> parseFleetResComposition(String htmlEncodedData) {
		Map<String, Long> fleetResData = new HashMap<>();

		StringReader strReader = null;
		XmlPullParser subxpp = null;
		try {
			strReader = new StringReader(htmlEncodedData);
			subxpp = XmlPullParserFactory.newInstance().newPullParser();
			subxpp.setInput(strReader);
			subxpp.defineEntityReplacementText("nbsp", " ");

			boolean parsingShips = false;
			boolean parsingRes = false;
			String currentShip;
			String currentRes;

			int eventType = subxpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (subxpp.getEventType() == XmlPullParser.TEXT) {
					String textData = subxpp.getText();
					textData = textData.replaceAll(":", "");
					if (textData.equals("Ships")) {
						parsingShips = true;
						break;
					}
				}
				try {
					subxpp.next();
					eventType = subxpp.getEventType();
				} catch (XmlPullParserException e) {
					System.out.println("Caught an exception. Not stopping: " + e + '\n' + e.getMessage());
					e.printStackTrace();
				}
			}

			while (parsingShips && eventType != XmlPullParser.END_DOCUMENT) {
				subxpp.next();
				eventType = subxpp.getEventType();
				if (eventType == XmlPullParser.TEXT) {
					String textData = subxpp.getText();
					if (textData != null) {
						textData = textData.trim();
					}
					if (textData != null && textData.length() > 0) {
						if (textData.equals("Shipment:")) {
							parsingRes = true;
							break;
						} else {
							textData = textData.substring(0, textData.length() - 1);
							currentShip = FleetAndResources.getName(textData);
						}

						textData = "";
						while (textData.length() == 0) {
							subxpp.next();
							eventType = subxpp.getEventType();
							if (eventType == XmlPullParser.TEXT) {
								textData = subxpp.getText();
								textData = textData.trim();
							}
						}

						String numshipstr = textData;
						numshipstr = numshipstr.replaceAll("\\.", "");
						if (currentShip != null && currentShip.length() > 0) {
							Long numships = Long.valueOf(numshipstr);
							fleetResData.put(currentShip, numships);
						}
					}
				}
			}

			eventType = subxpp.getEventType();
			while (parsingRes && eventType != XmlPullParser.END_DOCUMENT) {
				subxpp.next();
				eventType = subxpp.getEventType();
				if (eventType == XmlPullParser.TEXT) {
					String textData = subxpp.getText();
					if (textData != null) {
						textData = textData.trim();
					}
					if (textData != null && textData.length() > 0) {
						String resType = subxpp.getText();
						if (FleetAndResources.METAL_TAG.equals(resType)) {
							currentRes = FleetAndResources.METAL;
						} else if (FleetAndResources.CRYSTAL_TAG.equals(resType)) {
							currentRes = FleetAndResources.CRYSTAL;
						} else if (FleetAndResources.DEUT_TAG.equals(resType)) {
							currentRes = FleetAndResources.DEUT;
						} else {
							continue;
						}

						textData = "";
						while (textData.length() == 0) {
							subxpp.next();
							eventType = subxpp.getEventType();
							if (eventType == XmlPullParser.TEXT) {
								textData = subxpp.getText();
								textData = textData.trim();
							}
						}

						String amount = textData;
						amount = amount.replaceAll("\\.", "");
						if (amount.length() > 0) {
							Long resAmount = Long.valueOf(amount);
							fleetResData.put(currentRes, resAmount);
						}
					}
				}
			}
		} catch (XmlPullParserException | IOException e) {
			System.err.println(e.toString() + '\n' + e.getMessage());
			e.printStackTrace();
		} finally {
			if (subxpp != null) {
				try {
					subxpp.setInput(null);
				} catch (XmlPullParserException e) {
					System.err.println(e.toString() + '\n' + e.getMessage());
					e.printStackTrace();
				}
			}

			if (strReader != null)
				strReader.close();
		}

		return fleetResData;
	}
}
