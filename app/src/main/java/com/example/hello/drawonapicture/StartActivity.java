package com.example.hello.drawonapicture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;


public class StartActivity extends ActionBarActivity {

	private ImageView mPhoto;

	private RelativeLayout mFrame;  //the master layout
	private LinearLayout mButtonLayout;
	private Button buttonSnap, buttonSave, buttonShare;
	private Button buttonColor1, buttonColor2, buttonColor3, buttonColor4; //TODO hmmm

	private int REQUEST_PHOTO = 1;
	private Uri photoSaveUri;

	private int photoHeight;
	private int photoWidth;

	private int photoX;
	private int photoY;

	private PhotoDrawArea photoDrawArea;

	private final String TAG = "Start activity";


	protected static int drawingColor = 0X66FF00FF; //random default value


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);


		//Set imageview to most of screen with dotted border

		mPhoto = (ImageView) findViewById(R.id.photograph);
		mFrame = (RelativeLayout) findViewById(R.id.main_frame);
		mButtonLayout = (LinearLayout) findViewById(R.id.button_layout);

		buttonSnap = (Button) findViewById(R.id.snap_button);
		buttonSnap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StartActivity.this.takePhoto();
			}
		});
		//todo rest of listeners


		//TODO Set mPhoto's size need to measure view components first.

		setUpTouchListener();

	}


//	hackyhackhack

	private static boolean collectingLine;
	static ArrayList<Point> points;

	private void setUpTouchListener() {

		mFrame.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				//TODO does this point intersect picture? return false.
				// If on picture, collect points and return true

				Log.i("touch event", " event id " + event.getActionMasked());


				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					//start collecting motion events

					Log.i("touch", "Action down coords = " + event.getX() + " "+ event.getY());
					collectingLine = true;
					photoDrawArea.addPointToCurrentLine(new Point(event.getX(), event.getY()));
					StartActivity.this.photoDrawArea.invalidate();
					return true;

				}

				else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
					Log.i("touch", "Action up coords = " + event.getX() + " " + event.getY());

					photoDrawArea.addPointToCurrentLine(new Point(event.getX(), event.getY()));
					photoDrawArea.endThisLine();
					StartActivity.this.photoDrawArea.invalidate();
					return true;

				}

				else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
					Log.i("touch", "Action up coords = " + event.getX() + " " + event.getY());
					photoDrawArea.addPointToCurrentLine(new Point(event.getX(), event.getY()));
					StartActivity.this.photoDrawArea.invalidate();
					return true;

				}

				else {
					}

				//else return false;
				return false; ///todo review   TODO action_cancel?
			}
		});



	}


	private void takePhoto() {

		Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		//Generate unique file name

		UUID uuid = UUID.randomUUID();

		String filename = "DRAW_ON_PICTURE_" + uuid.toString() + ".jpg";
		File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File photoFileInDirectory = new File(storageDirectory, filename);


		photoSaveUri = Uri.fromFile(photoFileInDirectory);

		Log.i(TAG, photoSaveUri.toString());

		takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, photoSaveUri);

		startActivityForResult(takePhoto, REQUEST_PHOTO);

	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {

		if (request == REQUEST_PHOTO && result == RESULT_OK) {

			//Get full size photo and load into imageview

			//What is screensize?

			int screenheight = mFrame.getHeight();
			int screenwidth = mFrame.getWidth();

			Log.i(TAG, "screen height, width = " + screenheight + " " + screenwidth);

			//How much room do buttons need?

			int buttonHeight = mButtonLayout.getHeight();

			int padding = (int)getResources().getDimension(R.dimen.color_button_margin);

		 	int targetPhotoHeight = screenheight - buttonHeight;
			int targetPhotoWidth = screenwidth;




			Log.i(TAG, "button height " + buttonHeight);
			Log.i(TAG, "target photo height, width = " + targetPhotoHeight + " " + targetPhotoWidth);

			Log.i(TAG, "original imageview height, width = " + mPhoto.getHeight() + " " + mPhoto.getWidth());

			//	mPhoto.setMaxHeight(targetPhotoHeight);
		//	mPhoto.setMaxWidth(targetPhotoWidth);



			//Scale photo to these sizes (or as close as possible)

			//Decode photo taken

			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(photoSaveUri.getPath(), bitmapOptions);
			int originalHeight = bitmapOptions.outHeight;
			int originalWidth = bitmapOptions.outWidth;

			int scaleFactor = Math.min(originalHeight/targetPhotoHeight, originalWidth/targetPhotoWidth);

			if (scaleFactor == 1)  {scaleFactor = 2;} ///todo HACK scale properly

			Log.i(TAG, "original photo height, width, scale factor = " + originalHeight + " " + originalWidth + " " + scaleFactor);


			bitmapOptions.inJustDecodeBounds = false;
			bitmapOptions.inSampleSize = scaleFactor;
			bitmapOptions.inPurgeable = true;

			Bitmap finalImage = BitmapFactory.decodeFile(photoSaveUri.getPath(), bitmapOptions);

			//set photox, photo y

			//Scale to fit imageview....
			//TODO bitmap.createScaledBitmap




			photoHeight = finalImage.getHeight();
			photoWidth = finalImage.getWidth();

			int photoScaledHeight, photoScaledWidth;

			//TODO do this properly
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(finalImage, targetPhotoWidth, targetPhotoHeight, false);

			Log.i(TAG, "final bitmap height, width = " + photoHeight + " " + photoWidth);

			mPhoto.setImageBitmap(finalImage);

			int[] xy = new int[2];
			mPhoto.getLocationInWindow(xy);  //?
			photoX = xy[0];
			photoY = xy[1]; //check, TODO ??

			Log.i(TAG, "image x y coord from get location in window =" + photoX + " " + photoY);
			Log.i(TAG, "image x y coord from getleft get top =" + mPhoto.getLeft() + " " + mPhoto.getTop());



			Log.i(TAG, "final imageview height, width = " + mPhoto.getHeight() + " " + mPhoto.getWidth());


			//todo rotation issues

			addDrawArea();

		}

	}

	private void addDrawArea() {

		photoDrawArea = new PhotoDrawArea(this, photoHeight, photoWidth, photoX, photoY);
		mFrame.addView(photoDrawArea);
		photoDrawArea.postInvalidate();

	}

	private void setButtonListeners() {

		buttonColor1 = (Button) findViewById(R.id.pink);
		buttonColor1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//set color to be pink
				drawingColor = getResources().getColor(R.color.pink);
			}
		});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_start, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}


class PhotoDrawArea extends View {


	private Context mContext;
	private int height, width, x, y;
	private Paint mPainter;

	private int userStrokeWidth = 10;

	private ArrayList<ArrayList<Point>> allLines;   //an arralist of arraylists

	private ArrayList<Point> collectOneLine;

	//private ArrayList<Point> points;



	public PhotoDrawArea(Context context, int height, int width, int x, int y) {
		super(context);

		Log.i("p", "photo constructor" + height + " " + width + " " + x + " " + y);
		this.x = 100;
		this.y = 100;
		this.height = 1300;
		this.width = 900;   ///todo fix
		mContext = context;
		mPainter = new Paint();

		allLines = new ArrayList<ArrayList<Point>>();

		addTest();

	}

	@Override
	protected void onDraw(Canvas canvas) {

			Log.i("p", "photo on draw");

		//Draw white box
			mPainter.setColor(mContext.getResources().getColor(android.R.color.white));
			mPainter.setStyle(Paint.Style.STROKE);
			mPainter.setStrokeWidth(3);
			canvas.drawRect(x, y, x + width, y + height, mPainter);  //TODO border on actual imageview border


			mPainter.setColor(StartActivity.drawingColor);
		mPainter.setStrokeWidth(userStrokeWidth);

			//draw all lines drawn so far

//		logallLines();

		for (ArrayList<Point> pointsForLine : allLines) {

			ArrayList<Line> lineSegments = createListOfLineSegments(pointsForLine);

			for (Line l : lineSegments) {
				canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPainter);
			}
		}

		//also draw the current line in progress, if there is one

		if (collectOneLine != null) {

			ArrayList<Line> lineSegments = createListOfLineSegments(collectOneLine);

				for (Line l : lineSegments) {
					canvas.drawLine(l.startX, l.startY, l.endX, l.endY, mPainter);
				}

			}

	}

	private void logallLines() {


		for (ArrayList<Point> pointsForLine : allLines) {

			String pointsString = "";
			for (Point p : pointsForLine) {
				pointsString = pointsString + "x=" + p.x + " y=" + p.y + ",   ";
			}Log.i("LINES" , pointsString);							}
		}



	//Take one list of points and create a list of line segments
	private ArrayList<Line> createListOfLineSegments(ArrayList<Point> points) {


		ArrayList<Line> lineSegments = new ArrayList<>();

		if (points != null) {

			if (points.size() == 0) {
//				nothing - return empty array list

			} else if (points.size() == 1 ) {
				//add one line with start and end in same place
				Point solePoint = points.get(0);
				Line dot = new Line(solePoint.x, solePoint.y, solePoint.x, solePoint.y);
				lineSegments.add(dot);
			} else
			//two or more points - make lines

			for (int p = 0 ; p < points.size() - 1 ; p++) {

				//Make line with this point and the one following it
				Point point = points.get(p);
				Point nextPoint = points.get(p+1);
				Line segment = new Line(point.x, point.y, nextPoint.x, nextPoint.y);
				lineSegments.add(segment);
			}

		}
		return lineSegments;
	}

	protected void addLinetoCollection(ArrayList<Line> linelist) {

		//TEST TODO

		//allLines.add(linelist);

	}

	protected void addPointToCurrentLine(Point p) {

		if (collectOneLine == null) {
			collectOneLine = new ArrayList<>();
		}
		collectOneLine.add(p);

		Log.i("one point added", "p=" + p.x + " " + p.y);
	}

	protected void endThisLine() {
		//TODO

		//Copy collectOneLine into new ArrayList

		ArrayList<Point> collectedLine = new ArrayList<>(collectOneLine);
		allLines.add(collectedLine);

		collectOneLine = null;


	}

	protected void addTest(){

		/*ArrayList<Line> lineCollection = new ArrayList<Line>();
		lineCollection.add(new Line(100, 100, 400, 400));
		lineCollection.add(new Line(600, 700, 200, 400));
		lineCollection.add(new Line(100, 100, 400, 600));
		lineCollection.add(new Line(600, 400, 300, 800));*/

	}



	protected class Line {
		float startX, startY, endX, endY;
		int color;   //TODO support this
		public Line(float startX, float startY, float endX, float endY) {
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}
	}

}

class Point{
	int x, y;
	Point(float x, float y) {
		this.x = (int)x;
		this.y = (int)y;
	}
}
