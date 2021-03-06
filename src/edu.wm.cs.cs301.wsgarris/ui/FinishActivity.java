package ui;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import falstad.Maze;
import falstad.MazeFileWriter;
import falstad.MazeHolder;
import wsgarrison.ui.R;

/**
 * FinishActivity is the fourth and final screen of the Maze program. Tells the user whether they won or not,
 * shows them how many steps they took, shows them how much battery they have remaining, tells them how to get back
 * to the initial screen (AMazeActivity).
 * 
 * Has buttons to go back to the main screen, and to save the maze that was just played to a file for that difficulty
 * and algorithm.
 * 
 * @author William S Garrison
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FinishActivity extends ActionBarActivity {

	//declares variables for the winstate, for the steps taken, and for the battery left.
	boolean win;
	private Maze maze;
	
	/**
	 * onCreate method that instantiates the activity and associated the proper layout.
	 * In FinishActivity, it sets the View, and checks the intent of the PlayActivity.
	 * 
	 * Sets the internal variables to the values from intent, then changes the display text based on the data
	 * received.
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finish_activity);
		
		maze = MazeHolder.getData();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    win = extras.getBoolean("win");
		}
		
		TextView textView = (TextView) findViewById(R.id.textView1);
		TextView textView2 = (TextView) findViewById(R.id.textView2);
		TextView textView3 = (TextView) findViewById(R.id.textView3);
		
		textView2.setText("Path Length: " + maze.getPathSteps());
		textView3.setText("Battery: " + maze.getBattery());
		
		if(win==true){
			textView.setText("YOU WIN!");
			
		}else{
			textView.setText("YOU LOSE!");
			MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.i_cant_lose);
			mediaPlayer.start();
		}
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
	 * Handles the simple menu.
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
	 * Goes back to the original screen, AMazeActivity.
	 */
	public void goBack(View view) {
		Intent intent = new Intent(this, AMazeActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Saves the maze that was just played to a file according to the difficulty and algorithm chosen at the
	 * beginning.
	 */
	public void save(View view) {
		String filename = maze.getDifficulty()+"_"+maze.getGenerateMethod();
		try{
			MazeFileWriter.store(filename, maze.getWidth(), maze.getHeight(), maze.getRooms(), 
					maze.getPartitiers(), maze.getBSP(), maze.getCells(), maze.getIntArrayDistances(),
					maze.getStartPosition()[0], maze.getStartPosition()[1], getApplicationContext());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Log.v("Save message", "Filesave Complete!");
		
		Context context = getApplicationContext();
		CharSequence text = "Filesave Complete!";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
}