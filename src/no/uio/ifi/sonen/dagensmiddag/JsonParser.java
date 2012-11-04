package no.uio.ifi.sonen.dagensmiddag;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class JsonParser {

	private static InputStream inputStream;
	private static String fileName = "dinners.json";

	public static JSONObject getMeSomeJson(Context context) throws IOException {

		byte[] buffer = "No data".getBytes();
		try {
			inputStream = context.openFileInput(fileName);
			Log.d("JSON", "Already have file: " + inputStream);
			buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
		} catch (FileNotFoundException e1) {
			Log.d("JSON", "No file on phone, dowloading...");
			URL url = new URL("http://www.dagensmiddag.net/index.json");
			URLConnection urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(1000);
			inputStream = urlConnection.getInputStream();
			Log.d("JSON", "Got file: " + inputStream);
			FileOutputStream fileOutputStream = context.openFileOutput(
					fileName, Context.MODE_PRIVATE);
			buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			fileOutputStream.write(buffer);
			Log.d("JSON", "Saved file to " + fileOutputStream);
			fileOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		inputStream.close();

		try {
			return new JSONObject(new String(buffer));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
