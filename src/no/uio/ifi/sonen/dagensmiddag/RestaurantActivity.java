package no.uio.ifi.sonen.dagensmiddag;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * In this project we are using just one class and one activity. From the API an
 * activity is a singel, focused thing a user can do.
 * 
 * PS: You can hover over most of the variables in the code when you are using
 * Eclipse too see the API information.
 * 
 * @see Activity
 * 
 * @author Kyrre Havik Eriksen
 * @mail Kyrrehe@ifi.uio.no
 * @version 0.9
 */
public class RestaurantActivity extends Activity {

	/**
	 * This is the name used to save the file to the phone.
	 */
	private static String fileName = "dinners.json";

	/**
	 * A SharedPreferences is you private storage on a phone. It's an "area"
	 * where no other apps can see your stored data. {@link http
	 * ://developer.android.com/guide/topics/data/data-storage.html}
	 */
	private SharedPreferences settings;

	/**
	 * This is the global json-object containing all the information about the
	 * cafes, food, and menu.
	 */
	private JSONObject dinner;

	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant);
		settings = getSharedPreferences(getString(R.string.app_name),
				MODE_PRIVATE);
		getMeSomeDinner(false);
	}

	/**
	 * In Android 4 they implemented an own xml to design your menu. So if you
	 * go to res/menu/activity_restaurant.xml you can see how I want the menu to
	 * look like, and what is a check-box. I've even grouped all the restaurant
	 * together. {@link http://developer.android.com/guide/topics/ui/menus.html}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_restaurant, menu);
		menu.findItem(R.id.menu_today).setChecked(
				settings.getBoolean("today", true));
		return true;
	}

	/**
	 * Used to handle user interaction with the menu. When a user click on one
	 * of the menu items, the information is sent to this menu, and you can get
	 * the specific menu item from the variable item
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.cafe_frederikke:
			populateFood(R.string.frederikke);
			break;
		case R.id.cafe_ifi:
			populateFood(R.string.ifi);
			break;
		case R.id.cafe_ole:
			populateFood(R.string.kafe_ole);
			break;
		case R.id.cafe_sv:
			populateFood(R.string.sv_kafeen);
			break;
		case R.id.menu_today:

			/**
			 * To write to our local storage using SharedPreferences we need to
			 * create an Editor variable. With the editor we can use normal put
			 * commands (similar to HashMaps). Just remember to call .commit()
			 * when your done. Or else the information won't be written to the
			 * phone.
			 */
			SharedPreferences.Editor editor = settings.edit();
			if (item.isChecked()) {
				item.setChecked(false);
				editor.putBoolean("today", false);
			} else {
				item.setChecked(true);
				editor.putBoolean("today", true);
			}
			editor.commit();
			break;
		case R.id.menu_update:
			getMeSomeDinner(true);
			populateFood(R.string.ifi);
			break;
		}

		return true;
	}

	/**
	 * Used to populate the TextView's in our layout with the food and opening
	 * hours.
	 * 
	 * @param id
	 *            id of the String representation of the cafe you want to see.
	 */
	private void populateFood(int id) {
		Log.d("FOOD", "Getting food from " + getString(id));

		JSONObject cafe;

		try {
			cafe = getCorrectCafe(id);
			setTitle(cafe.getString("name"));
			TextView textView;

			LinearLayout layout = (LinearLayout) findViewById(R.id.dinner_hours);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					layout.getLayoutParams());
			layoutParams.setMargins(10, 10, 10, 10);

			if (settings.getBoolean("today", true)) {
				textView = new TextView(this);
				textView.setLayoutParams(layoutParams);
				textView.setText(cafe.getJSONArray("open").getString(
						getWeekday()));
				layout.addView(textView);
			} else {
				String hours = "";
				JSONArray array = cafe.getJSONArray("open");
				for (int i = 0; i < array.length(); i++) {
					textView = new TextView(this);
					textView.setLayoutParams(layoutParams);
					hours = array.getString(i);
					textView.setText(hours);
					layout.addView(textView);
				}
			}

			textView = (TextView) findViewById(R.id.dinner_food);
			if (settings.getBoolean("today", true))
				textView.setText(parseFood(cafe.getJSONArray("menu")
						.getJSONObject(getWeekday())));
			else {
				String food = "";
				JSONArray array = cafe.getJSONArray("menu");
				for (int i = 0; i < array.length(); i++) {
					food += parseFood(array.getJSONObject(i)) + "\n";
				}
				textView.setText(food);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Since JSON is not always that readable, we need to parse the information
	 * by our self. This method takes an JSON object and get the food and return
	 * it as a String.
	 * 
	 * @param jsonObject
	 *            The menu you want to parse
	 * @return the food parsed from the JSON object
	 */
	private String parseFood(JSONObject jsonObject) {
		String food = "";
		try {
			@SuppressWarnings("unchecked")
			Iterator<String> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				JSONArray array = jsonObject.getJSONArray(key);
				food += key + ":\n\t";
				for (int i = 0; i < array.length(); i++)
					food += array.getString(i) + "\n";
			}

		} catch (JSONException e) {
			food = "No soup for you!";
			e.printStackTrace();
		}
		return food;
	}

	/**
	 * Used to get the correct cafe from the global dinner object.
	 * 
	 * @param resId
	 *            the String resource for the cafe you want
	 * @return the json object of the correct cafe
	 * @throws JSONException
	 */
	private JSONObject getCorrectCafe(int resId) throws JSONException {
		JSONArray cafes = dinner.getJSONArray("cafes");
		String name = getString(resId);
		for (int i = 0; i < cafes.length(); i++) {
			if (cafes.getJSONObject(i).getString("name").equals(name))
				return cafes.getJSONObject(i);
		}

		return null;
	}

	/**
	 * Since Java's built in Calendar starts on Sundays I've made an own method
	 * that start on a Monday
	 * 
	 * @return int the correct weekday
	 */
	private int getWeekday() {
		int weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		if (weekday == 0)
			return 6;
		else
			return weekday - 2;
	}

	/**
	 * Starts the Thread needed to handle network communication It's important
	 * to remember that when you need to use internet, you need to set the
	 * permission in the AndroidManifest.xml file.
	 * 
	 * @param download
	 *            true if you want to force downloading of a new file
	 */
	private void getMeSomeDinner(final boolean download) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					dinner = getMeSomeJson(download);
					Log.d("JSON", "Returned with JSON file");
					if (!download)
						populateFood(R.string.ifi);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Used to connect to the internet and download a new JSON file, or if you
	 * have the file, it will load it from the phone
	 * 
	 * @param download
	 *            true to download a new file, even if you already have a file
	 * @return The JSON object
	 * @throws IOException
	 */
	private JSONObject getMeSomeJson(boolean download) throws IOException {

		byte[] buffer = "No data".getBytes();
		InputStream inputStream;
		try {
			if (download)
				throw new FileNotFoundException();

			inputStream = openFileInput(fileName);
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
			FileOutputStream fileOutputStream = openFileOutput(fileName,
					Context.MODE_PRIVATE);
			buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			fileOutputStream.write(buffer);
			Log.d("JSON", "Saved file to " + fileOutputStream);
			fileOutputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return new JSONObject(new String(buffer));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
