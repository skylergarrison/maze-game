package ui;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.*;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import falstad.Maze;
import falstad.MazeHolder;
import android.widget.AdapterView.OnItemSelectedListener;
import wsgarrison.ui.R;

/**
 * AMazeActivity is the landing page for the application. From this screen, users can select the maze generation
 * algorithm, the difficulty of the maze, and whether or not to load from a file with the selected parameters.
 * 
 * The generate button advances to the next activity and starts the maze generation.
 * 
 * @author William S Garrison
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AMazeActivity extends ActionBarActivity  implements OnItemSelectedListener{
	
	//boolean to tell the maze program to load from a file or not
	boolean fromFile = false;
	Maze maze;

	/**
	 * onCreate method that instantiates the activity and associated the proper layout.
	 * In AMazeActivity, the spinner for the selection of the maze generation algorithm is instantiated,
	 * the seekbar to let the user choose the difficulty,
	 * and the seekbar change listener to handle the input from the seekbar.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		spinner.setOnItemSelectedListener(this);
		
		SeekBar difficultyBar = (SeekBar) findViewById(R.id.seekBar1);
		
		difficultyBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				
			int newProgress = 0;
	
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
				newProgress = progress;
			}
	
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				Log.v("TrackingBar Msg", "started tracking touch");
			}
	
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.v("TrackingBar Msg", "Bar#: " + newProgress);
				
				Log.v("Difficulty Msg", "SeekBar value:" + newProgress);
				maze.setDifficulty(newProgress);
				TextView textView = (TextView) findViewById(R.id.textView4);
				textView.setText(newProgress + " ");
				
			}
		});
		
		maze = new Maze();
		MazeHolder.setData(maze);
		
		maze.incTest();
		Log.v("Test persistent maze", "Usage: " + maze.getTest());
	}

	/**
	 * Creates a simple default menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Handles the default menu input.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Sends this activity to the next, which is GeneratingActivity.
	 */
	public void goNext(View view) {
		Intent intent = new Intent(this, GeneratingActivity.class);
		intent.putExtra("file", fromFile);
		startActivity(intent);
	}
	
	/**
	 * Handles the selection of an item in the spinner. Toasts it and sends it to verbose in LogCat.
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		String input = parent.getItemAtPosition(position).toString();

		Log.v("Spinner Msg", parent.getSelectedItem().toString());

		if(input.equalsIgnoreCase("backtracking")!=true){
			maze.setGenerateMethod(0);
		}else if(input.equalsIgnoreCase("prims")!=true){
			maze.setGenerateMethod(1);
		}else if(input.equalsIgnoreCase("ellers")!=true){
			//ellers is non-functional, use prims
			maze.setGenerateMethod(1);
		}
	}

	/**
	 * Handles the selection of an nothing when using the spinner. Sends it to verbose in LogCat.
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		Log.v("Spinner Msg", "nothing selected");	
	}
	
	/**
	 * Handles the clicking of a checkbox. Sends it to verbose in LogCat.
	 */
	public void onCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    if (checked){
        	fromFile = true;
        	Log.v("CheckBox Msg", "File: true");
        }
        else{
        	fromFile = false;
        	Log.v("CheckBox Msg", "File: false");
        }
	}
}
