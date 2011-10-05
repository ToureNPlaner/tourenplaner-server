/**
 * $$\\ToureNPlaner\\$$
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This utility class is used to convert the result of a /algsp request to gpx
 * and prints it on stdout
 * 
 * @author Niklas Schnelle
 * 
 */
public class SPtoGPX {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err
					.println("Please supply a .json file as first parameter or - to read from stdin");
			return;
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(
					args[0].equals("-") ? System.in : new FileInputStream(
							args[0]), Charset.forName("UTF-8")));
		} catch (FileNotFoundException e) {
			System.err.println("The file: " + args[0] + " could not be found");
			return;
		}
		JSONParser parser = new JSONParser();
		try {
			JSONObject requestJSON = (JSONObject) parser.parse(reader);
			@SuppressWarnings("unchecked")
			ArrayList<Map<String, Object>> points = (ArrayList<Map<String, Object>>) requestJSON
					.get("points");

			System.out
					.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
			System.out
					.println("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" creator=\"Oregon 400t\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\">");
			System.out.println("  <trk>\n"
					+ "    <name>Example GPX Document</name>");
			System.out.println("<trkseg>");
			for (Map<String, Object> point : points) {
				System.out.println("<trkpt lat=\"" + point.get("lt")
						+ "\" lon=\"" + point.get("ln") + "\"></trkpt>");
			}
			System.out.println("</trkseg>\n</trk>\n</gpx>");

		} catch (IOException e) {
			System.err.println("An IO Error ocurred: " + e.getMessage());
		} catch (ParseException e) {
			System.err.println("The file: " + args[0]
					+ " could not be parsed: " + e.getMessage());
		}
	}
}
