package no.uio.ifi.sonen.dagensmiddag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
		
		// textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
		return textView;
	}
}
