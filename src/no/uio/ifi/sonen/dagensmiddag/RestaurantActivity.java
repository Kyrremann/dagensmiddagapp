package no.uio.ifi.sonen.dagensmiddag;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RestaurantActivity extends FragmentActivity {

	private JSONObject dinner;
	private static InputStream inputStream;
	private static String fileName = "dinners.json";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_restaurant);
		setContentView(R.layout.activity_loading_dinner);

		getMeSomeDinner();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_restaurant, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_today:
			SharedPreferences settings = getSharedPreferences(
					getString(R.string.app_name), MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			if (item.isChecked()) {
				item.setChecked(false);
				editor.putBoolean("today", false);
			} else {
				item.setChecked(true);
				editor.putBoolean("today", true);
			}
			break;
		case R.id.menu_update:
			getMeSomeDinner();
			break;
		}

		return true;
	}

	private void populateFood(int ifi) {
		if (findViewById(R.id.layout_food) == null)
			setContentView(R.layout.activity_restaurant);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout_food);
		JSONArray menu;
		try {
			setTitle("Uke: " + dinner.getString("week"));
			JSONArray cafes = dinner.getJSONArray("cafes");
			TextView textView = new TextView(this);
			textView.setText(cafes.getJSONObject(0).getString("name"));
			layout.addView(textView);
			
			textView = new TextView(this);
			textView.setText(cafes.getJSONObject(0).getJSONArray("open").getString(getWeekday()));
			layout.addView(textView);
			
			textView = new TextView(this);
			textView.setText(cafes.getJSONObject(0).getJSONArray("menu").getString(getWeekday()));
			layout.addView(textView);
			
//			if (menu != null) {
//				((TextView) rootView.findViewById(R.id.dinner_week))
//						.setText("Week: " + args.getString("week"));
//				TextView textView = new TextView(getActivity());
//				textView.setTextAppearance(getActivity(),
//						android.R.attr.textAppearanceLarge);
//				String food = "";
//				SharedPreferences settings = getSharedPreferences(
//						getString(R.string.app_name), MODE_PRIVATE);
//				JSONObject today;
//				Iterator keys;
//				if (settings.getBoolean("today", false)) {
//					System.out.println("true");
//					for (int day = 0; day < menu.length(); day++) {
//						today = (JSONObject) menu.get(day);
//						keys = today.keys();
//						while (keys.hasNext()) {
//							String key = (String) keys.next();
//							food += key + "\n\t";
//							for (int i = 0; i < today.getJSONArray(key)
//									.length(); i++)
//								food += today.getJSONArray(key).get(i) + "\n";
//						}
//					}
//				} else {
//					System.out.println("false");
//					today = (JSONObject) menu.get(getWeekday());
//					keys = today.keys();
//					System.out.println(today);
//					while (keys.hasNext()) {
//						String key = (String) keys.next();
//						food += key + "\n\t";
//						for (int i = 0; i < today.getJSONArray(key).length(); i++)
//							food += today.getJSONArray(key).get(i) + "\n";
//					}
//				}
//
//				textView.setText(food);
//				rootView.addView(textView);
//			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private int getWeekday() {
		int weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
		if (weekday == 0)
			return 6;
		else
			return weekday;
	}

	private void getMeSomeDinner() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					dinner = getMeSomeJson();
					Log.d("JSON", "Returned with JSON file");
					populateFood(R.string.ifi);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private JSONObject getMeSomeJson() throws IOException {

		byte[] buffer = "No data".getBytes();
		try {
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
