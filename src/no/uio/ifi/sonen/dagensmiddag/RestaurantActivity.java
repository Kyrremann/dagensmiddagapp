package no.uio.ifi.sonen.dagensmiddag;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RestaurantActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private Context context;
	private JSONObject dinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_restaurant);
		this.context = this;
		getMeSomeDinner();
		// Create the adapter that will return a fragment for each of the four
		// primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

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
			break;
		case R.id.menu_update:
			getMeSomeDinner();
			break;
		}

		return true;
	}

	private void getMeSomeDinner() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					dinner = JsonParser.getMeSomeJson(context);
					Log.d("JSON", "Returned with JSON file");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {
			Fragment fragment = new DinnerSectionFragment();
			Bundle args = new Bundle();
			args.putInt(DinnerSectionFragment.ARG_SECTION_NUMBER, index + 1);
			try {
				args.putString("week", dinner.getString("week"));
				JSONArray cafes = dinner.getJSONArray("cafes");
				if (getString(R.string.frederikke).equalsIgnoreCase(
							getPageTitle(index).toString())) {
						args.putString("menu",
								((JSONObject) cafes.get(index)).toString());
					} else if (getString(R.string.sv_kafeen).equalsIgnoreCase(
							getPageTitle(index).toString())) {
						args.putString("menu",
								((JSONObject) cafes.get(index)).toString());
					} else if (getString(R.string.ifi).equalsIgnoreCase(
							getPageTitle(index).toString())) {
						args.putString("menu",
								((JSONObject) cafes.get(index)).toString());
					} else if (getString(R.string.kafe_ole).equalsIgnoreCase(
							getPageTitle(index).toString())) {
						args.putString("menu",
								((JSONObject) cafes.get(index)).toString());
					}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.frederikke).toUpperCase();
			case 1:
				return getString(R.string.ifi).toUpperCase();
			case 2:
				return getString(R.string.kafe_ole).toUpperCase();
			case 3:
				return getString(R.string.sv_kafeen).toUpperCase();
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public class DinnerSectionFragment extends Fragment {
		public DinnerSectionFragment() {
		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			Bundle args = getArguments();
			JSONArray menu;
			try {
				menu = new JSONObject(args.getString("menu"))
						.getJSONArray("menu");
				// TODO: Make it pretty, and add all days
				textView.setText("Week: " + args.getString("week")
						+ "\nDinner: " + menu.get(getWeekday()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return textView;
		}
		
		private int getWeekday() {
			int weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
			if (weekday == 0)
				return 6;
			else
				return weekday;
		}
	}
}
