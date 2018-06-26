package com.dku.dblab.android.pptRemoteView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class MainActivity extends Activity  implements SensorEventListener {
	private Button B_con, B_discon;
	private ImageView Img_View = null;
	private EditText ip_input=null;
	
	private String ipaddr=null;	//������
	final private int port=1828;      //��Ʈ��ȣ
	final private int port_Img = 9999; //�̹��� �ε��ϴ� ��Ʈ��ȣ.
	
	private Thread img_Load = null;
	private Thread ppt_Con = null;
	private Boolean Thread_On = false;
	
	private Button btn1 = null;
	private SensorManager sm = null;
    private long lastTime;
    Sensor rotesensor = null;
    Sensor m_acc_sensor, m_mag_sensor;
    float azimuth2 =0;
    DecimalFormat format;
    float[] m_acc_data = null;
    float[] m_mag_data = null;
    float[] m_result_data = new float[3];
    float[] m_rotation = new float[9];
    float[] accel_data = new float[3];
    float[] gravity = new float[3];
    float curr = 0;
    float past = 0;
    boolean senstate = false;
	final private byte Right = 0x1; //right
	final private byte Left = 0x2; //left
	
	private Boolean isRight = false;
	private Boolean isLeft = false;
	
    private Thread ConnectionThread = null;
	
	public void Show_alert(String msg, String title) 
	{ 
		AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setMessage(Html.fromHtml(msg));
		alert.setPositiveButton(android.R.string.ok, null);
		alert.setTitle(title);
		alert.show();
	}
	
	private void Clear_Threads()
	{
	    runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
			@Override
			public void run() {
				if(Thread_On == true)
				{
					Thread_On = false; 
					try { Thread.sleep(100); } catch(Exception ex) { }
					
					try { img_Load.stop(); } catch (Exception ex) { }
					img_Load=null; 
					try { ppt_Con.stop(); } catch (Exception ex) { }
					ppt_Con = null;
				}
			}
		});
	}
	
	private void new_Connection()
	{
		ConnectionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try
				{
					Clear_Threads();
					
					// TODO Auto-generated method stub
					Socket TestSoc = null;
					TestSoc = new Socket(ipaddr, port);
					TestSoc.close();
					
					runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
    					@Override
    					public void run() {
    						Thread_On = true;
							new_ImgLoad(); img_Load.start(); 
							new_pptCon(); ppt_Con.start(); //���� ���� ������ �۵�
							
							Show_alert("Your device <b><font color=#ff0000>CONNECTION</font></b> to server has been <b><font color=#ff0000>CONNECTED</font></b>.", "Connected!");
    					}
    				});
				} 
			catch (Exception ex) {
				
				runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
					@Override
					public void run() {
						Show_alert("Check your <b><font color=#ff0000>IP IS CORRECT</font></b>."
								+ "<br></br>" +
								"Check whether <b><font color=#ff0000>PORT</font></b> 9999 or 1828 is <b><font color=#ff0000>ALREADY OPENED</font></b>." 
								+ "<br></br>" +
								"Check whether your computer <b><font color=#ff0000>SERVER IS RUNNING</font></b>." 
								+ "<br></br>" +
								"If your computer IP is in private IP Address Range, your device need to be in same IP Address Range."
								, "Unavailable connection!");
    					}
    				});
				}
			}
		});
	}
	
	private void new_ImgLoad()
	{
		img_Load = new Thread(new Runnable() { //���ο� �����忡�� ��� �̹����� �޾ƿ�
        	@Override
        	public void run()
        	{ 
        		Socket soc = null;
        		InputStream IS = null;
        		
        		while(Thread_On)
        		{
        	    	try {
        	    		try { soc = new Socket(ipaddr, port_Img); } //����õ�
                		catch (Exception ex) {
                			Clear_Threads();
        					runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
            					@Override
            					public void run() {
            						Show_alert("Your device <b><font color=#ff0000>CONNECTION</font></b> to server has been <b><font color=#ff0000>LOST</font></b>.", "Disconnected!");
            					}
            				});
        					break;
                		}
        	    		
        				IS = soc.getInputStream();
        				final Bitmap bmp = BitmapFactory.decodeStream(IS);
        			    IS.close();
        			    
        			    runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
        					@Override
        					public void run() {
        						Img_View.setImageBitmap(bmp); //�ش� UI�� �����ϴ� �����忡���� �̹��� ���� ����
        					}
        				});
        				soc.close();
        				
        				Thread.sleep(100); //0.1�� ����
        				
        			} catch (Exception e) { 
    					runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
        					@Override
        					public void run() {
        						Show_alert("UNKNOWN ERROR", "ERROR");
        					}
        				});
    					break; 
					}
        		}
        	}
        });
	}
	
	private void new_pptCon()
	{
		ppt_Con = new Thread(new Runnable() {
        	@Override
        	public void run()
        	{ 
        		Socket soc = null;
        		OutputStream OS = null; 
        		try{ soc = new Socket(ipaddr, port); } //������ Ű�� �� ������ �̹� Ȯ����
        		catch (Exception ex) {
					runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
    					@Override
    					public void run() {
    						Show_alert("UNKNOWN ERROR", "ERROR");
    					}
    				});
        		}
        		
        		while(Thread_On)
        		{
        	    	try {
        	    		OS = soc.getOutputStream();
        	    		
        				if(isRight)
        					{ OS.write(Right); isRight = false; }
        				if(isLeft) 
        					{ OS.write(Left); isLeft = false; }
        				
        				Thread.sleep(1);
        			} catch (Exception e) { 
    					runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
        					@Override
        					public void run() {
        						Show_alert("UNKNOWN ERROR", "ERROR");
        					}
        				});
    					break; 
					}
        		}
        		try { soc.close(); }
        		catch (Exception ex) { 
					runOnUiThread(new Runnable() { //UI�� �����ϴ� �����忡�� �����ϰڴٴ� �ǹ�
    					@Override
    					public void run() {
    						Show_alert("UNKNOWN ERROR", "ERROR");
    					}
    				});
        		}
        	}
        });
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		B_con=(Button)findViewById(R.id.con);
		B_discon=(Button)findViewById(R.id.discon);
		ip_input=(EditText)findViewById(R.id.ip2);
		Img_View=(ImageView)findViewById(R.id.imageView1);
		
		 btn1 = (Button)findViewById(R.id.btn1);
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
			
		  	format = new DecimalFormat();
			format.applyLocalizedPattern("0.#");
			m_acc_sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			m_mag_sensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			rotesensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			
		//connect		
		B_con.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Thread_On == false)
				{
					try { ipaddr=ip_input.getText().toString().trim(); }
					catch (Exception ex) { ipaddr = null; }
					
					if(ipaddr.length() > 0)
					{
						new_Connection();
						ConnectionThread.start();
					}
					else Show_alert("<b><font color=#ff0000>INPUT</font></b> your computer <b><font color=#ff0000>IP BEFORE CONNECT</font></b>.", "Unavailable connection!");
				}
				else Show_alert("Your device <b><font color=#ff0000>CONNECTION</font></b> to server has been <b><font color=#ff0000>ALREADY CONNECTED</font></b>.", "Already connected!");
			}
		});
		
		//disconnect
		B_discon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { 
				if(Thread_On == true)
				{
					Clear_Threads();
					Show_alert("Your device <b><font color=#ff0000>CONNECTION</font></b> to server has been <b><font color=#ff0000>LOST</font></b>.", "Disconnected!");
				}
				
				else Show_alert("Your device <b><font color=#ff0000>CONNECTION</font></b> to server has been <b><font color=#ff0000>ALREADY LOST</font></b>.", "Already not connected!");
			}
		});	
	
	
	btn1.setOnTouchListener(new View.OnTouchListener() {
		
		

			public boolean onTouch(View view, MotionEvent event) {
			
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				senstate = true;
			return false;}
			if(event.getAction() == MotionEvent.ACTION_UP)
			{
				senstate = false;
				return false;}
			if(event.getAction() == MotionEvent.ACTION_CANCEL){
				senstate = false;
			return false;
			}
			
			return true;
			}

			public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
				senstate = true;
			return true;
			}
		
	});
	
	}
	//volume up & down key event
	@Override 
	  public boolean dispatchKeyEvent(KeyEvent event)
	 { 
			//if press the volume_up key
	        if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP )
	        { 
	            if(event.getAction() == KeyEvent.ACTION_DOWN)
	            {
	            	if(Thread_On == false) //not connected
	            		Show_alert("You need to <b><font color=#ff0000>CONNECT</font></b> your device to computer server <b><font color=#ff0000>BEFORE</font></b> control PPT.", "Not connected!");

					else if(isLeft == false) isLeft = true;
	            	//send "left" string to server
	            } 
	            return true; 
	        }
	        //if press the volum_down key
	        if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN )
	        {
	            if(event.getAction() == KeyEvent.ACTION_DOWN)
	            {
	            	if(Thread_On == false) //not connected
	            		Show_alert("You need to <b><font color=#ff0000>CONNECT</font></b> your device to computer server <b><font color=#ff0000>BEFORE</font></b> control PPT.", "Not connected!");

					else if(isRight == false) isRight = true;
	            	//send "right" string to server
	            } 
	            return true; 
	        } 
	        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
	        {
	            if(event.getAction() == KeyEvent.ACTION_DOWN) {
	            	Clear_Threads();
	            	try{ ConnectionThread.stop(); } catch (Exception ex) { }
	            	ConnectionThread = null;
            	} 
	            return super.dispatchKeyEvent(event); 
	        }
	        return super.dispatchKeyEvent(event); 
	    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
	// TODO Auto-generated method stub
	    
		final float alpha = (float)0.8;
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			if(senstate == true)
			{
				past = curr;
				 long currentTime = System.currentTimeMillis();
		         long gabOfTime = (currentTime - lastTime);

		    	
		    if(gabOfTime > 1000)
		    {
		    gravity[0]= alpha * gravity[0] + (1-alpha) * event.values[0];
		    gravity[1]= alpha * gravity[1] + (1-alpha) * event.values[1];
		    gravity[1]= alpha * gravity[1] + (1-alpha) * event.values[1];
			accel_data[0] = event.values[0] - gravity[0];
			accel_data[1] = event.values[1] - gravity[1];
			accel_data[2] = event.values[2] - gravity[2];
		     curr = accel_data[0];
		     
		    
		    }
		  //  String intBitX = Integer.toBinaryString(Float.floatToIntBits(accel_bef));
		   // String intBity = Integer.toBinaryString(Float.floatToIntBits(accel_data[0]));
	 	  
	   
		   
		    
	       
			
		//	int before = (int)intBitX.charAt(0);	
		//	int after = (int)intBity.charAt(0);
			
	
		//	System.out.println(a[count]);
			    	
				if(azimuth2 >= 50 && azimuth2 <= 230)
			    	    {
			    	    	if((past * curr)< 0) 
			    	    	{ 	
			    				System.out.println("a[count-1] =" + past);
			    				System.out.println("a[count] =" + curr);
			    	    	
			    				if(Thread_On == false) //not connected
				            		Show_alert("You need to <b><font color=#ff0000>CONNECT</font></b> your device to computer server <b><font color=#ff0000>BEFORE</font></b> control PPT.", "Not connected!");

								else if(isRight == false) isRight = true;
			    	    		
			    		    }
				  	    	else
			    	    	{				
			    	    		
								//  result = intBity;
								
								
			    			}	
				  	    }
				else
				{ 
					if((past * curr)< 0) 
			    	    
					{ 	
	    				System.out.println("a[count-1] =" + past);
	    				System.out.println("a[count] =" + curr);
	    				if(Thread_On == false) //not connected
		            		Show_alert("You need to <b><font color=#ff0000>CONNECT</font></b> your device to computer server <b><font color=#ff0000>BEFORE</font></b> control PPT.", "Not connected!");

						else if(isLeft == false) isLeft = true;
		            	//send "left" string to server
	    				
	    	    		
	    				  
	    		    }
				
					
				}	
		
			
		//	  System.out.println(azimuth);
		
		
	
	
		    }
			else
			{
					
			}
				
		}	
		
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			m_acc_data = event.values.clone();
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			m_mag_data = event.values.clone();
		
		if(m_acc_data != null && m_mag_data != null)
		{
		  SensorManager.getRotationMatrix(m_rotation, null, m_acc_data, m_mag_data);
		  SensorManager.getOrientation(m_rotation, m_result_data);
					 
			
		m_result_data[0] = (float)Math.toDegrees(m_result_data[0]);
		if(m_result_data[0]<0) m_result_data[0] += 360;
		
//		azimuth = (int)m_result_data[0];
		//System.out.println("azimuth :" + azimuth);
		}
		
		if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
			azimuth2 = (int)event.values[0];
			System.out.println("azimuth :" + azimuth2);
		}
	
		
	}
	 protected void onPause(){
	    	super.onPause();
	        sm.unregisterListener(this);
	    }

	 protected void onResume(){
			super.onResume();
		
		    sm.registerListener(this, m_acc_sensor, SensorManager.SENSOR_DELAY_NORMAL); 
		    sm.registerListener(this, m_mag_sensor, SensorManager.SENSOR_DELAY_GAME);
		    sm.registerListener(this, rotesensor, SensorManager.SENSOR_DELAY_GAME);
		}
}