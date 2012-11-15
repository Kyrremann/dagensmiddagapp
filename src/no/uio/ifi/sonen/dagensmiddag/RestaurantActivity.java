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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RestaurantActivity extends FragmentActivity {

	private static InputStream inputStream;
	private static String fileName = "dinners.json";

	private SharedPreferences settings;
	private JSONObject dinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant);
		settings = getSharedPreferences(getString(R.string.app_name),
				MODE_PRIVATE);
		getMeSomeDinner(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_restaurant, menu);
		menu.findItem(R.id.menu_today).setChecked(
				settings.getBoolean("today", true));
		return true;
	}

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

	private void populateFood(int id) {
		Log.d("FOOD", "Getting food from " + getString(id));
		// if (findViewById(R.layout.activity_restaurant) == null)
		// setContentView(R.layout.activity_restaurant);

		JSONObject cafe;

		try {
			cafe = getCorrectCafe(id);
			setTitle(cafe.getString("name"));
			TextView textView;
			// TextView textView = (TextView) findViewById(R.id.dinner_hours);
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

	private JSONObject getCorrectCafe(int resId) throws JSONException {
		JSONArray cafes = dinner.getJSONArray("cafes");
		String name = getString(resId);
		for (int i = 0; i < cafes.length(); i++) {
			if (cafes.getJSONObject(i).getString("name").equals(name))
				return cafes.getJSONObject(i);
		}

		return null;
	}

	private int getWeekday() {
		int weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		if (weekday == 0)
			return 6;
		else
			return weekday - 2;
	}

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

	private JSONObject getMeSomeJson(boolean download) throws IOException {

		byte[] buffer = "No data".getBytes();
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
		} catch (IOException e) {
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
