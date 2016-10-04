package com.example.gigafar1;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gigafar1.R;


public class MainActivity extends Activity {
	private TextView textView;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private Button uploadBtn;
	private final String upLoadServerUri = "http://www.gigafar.com/testuploadpage.php";
	ProgressDialog dialog = null;
	int serverResponseCode = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button uploadBtn = (Button)this.findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener( new OnClickListener(){
            public void onClick(View arg0) {
                Intent intent = new Intent( Intent.ACTION_PICK );
                intent.setType( "*/*" );
                Intent intent2 = Intent.createChooser( intent, "Choose File to Upload" );
                dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);
                startActivityForResult( intent2, 0 );
            }
        });
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        textView = (TextView) findViewById(R.id.textView);
        if(mNfcAdapter == null) {
			/*Toast.makeText(this,  "沒有NFC功能",  Toast.LENGTH_LONG).show();
			finish();
			return ;*/
        	textView.setText("[Warning] NFC functions do not work with this device.");
		}
        else {
        	textView.setText("Scan a tag");
            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
            		this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter ndef
            	= new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            try { ndef.addDataType("*/*");
            } catch (MalformedMimeTypeException e) { throw new RuntimeException("fail", e); }
            mFilters = new IntentFilter[] {
            		ndef, new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED), new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            };
            mTechLists = new String[][] { new String[] { "MifareClassic", "MifareUltralight", "IsoDep", "Ndef", "NdefFormatable", "NfcA", "NfcB", "NfcF", "NfcV", "TagTechnology" } };
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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
    }*/
    
    @Override
	protected void onNewIntent(Intent intent){
    	String idstr = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
    	textView.setText("Discovered a tag with id(" + idstr + ")");
    	Toast.makeText(this, idstr, Toast.LENGTH_LONG).show();
    	//Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://120.125.96.158/ex14.asp?fname="+idstr));
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gigafar.com/index_wine/m_wine/index.php?id=" + idstr));
    	startActivity(browserIntent);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    	if(mNfcAdapter != null) mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    	/*Toast.makeText(this, getIntent().getAction(), Toast.LENGTH_LONG).show();
    	if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())){
    		Parcelable[] msgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    			Toast.makeText(this, new String("ACTION_TAG_DISCOVERED"), Toast.LENGTH_LONG).show();
    			NdefRecord firstRecord = ((NdefMessage)msgs[0]).getRecords()[0];
    			byte[] payload = firstRecord.getPayload();
    			int payloadLength = payload.length;
    			int langLength = payload[0];
    			int textLength = payloadLength - langLength - 1;
    			byte[] text = new byte[textLength];
    			System.arraycopy(payload, 1+langLength, text, 0, textLength);
    			Toast.makeText(this, new String(text), Toast.LENGTH_LONG).show();
    		}
    		else if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
    			Toast.makeText(this, "ACTION_NDEF_DISCOVERED", Toast.LENGTH_LONG).show();
    			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    			NdefMessage msg = (NdefMessage) rawMsgs[0];
    			Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
    		}
    		else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
    			Toast.makeText(this, "ACTION_TECH_DISCOVERED", Toast.LENGTH_LONG).show();
    			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    			NdefMessage msg = (NdefMessage) rawMsgs[0];
    			Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();
    		}*/
    }

	@Override
	public void onPause() {
		super.onPause();
		if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	        case 0:
	        if (resultCode == RESULT_OK) {
	            Uri uri = data.getData();
	            try {
	            	final String path = FileUtils.getPath(this, uri);
	            	//Toast.makeText(this,  path,  Toast.LENGTH_LONG).show();
	            	new Thread(new Runnable() {
	                    public void run() {
	                    	Looper.prepare();
	                        runOnUiThread(new Runnable() { public void run() {} });                      
	                        uploadFile(path);
	                        Looper.loop();
	                    }
	                }).start();
	            }
	            catch (Exception e) {}
	        }
	        break;
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	private String ByteArrayToHexString(byte [] inarray) {
	    int i, j, in;
	    String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	    String out= "";
	
	    for(j = 0 ; j < inarray.length ; ++j) 
	        {
	        in = (int) inarray[j] & 0xff;
	        i = (in >> 4) & 0x0f;
	        out += hex[i];
	        i = in & 0x0f;
	        out += hex[i];
	        }
	    return out;
	}
	
	public int uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;  
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024; 
        File sourceFile = new File(sourceFileUri); 
        if (!sourceFile.isFile()) {
             dialog.dismiss(); 
             //Log.e("uploadFile", "Source File not exist :"+sourceFileUri);
             //Toast.makeText(this,  "Source File not exist : " + sourceFileUri,  Toast.LENGTH_LONG).show();
             final String emsg = "Source File not exist : " + sourceFileUri;
             runOnUiThread(new Runnable() {
                 public void run() {
                     //messageText.setText("Got Exception : see logcat ");
                     Toast.makeText(MainActivity.this, "Got Exception : " + emsg, Toast.LENGTH_LONG).show();
                 }
             });
             return 0;
        }
        else
        {
        	//Toast.makeText(this,  "Uploading...",  Toast.LENGTH_LONG).show();
        	try { 
                 FileInputStream fileInputStream = new FileInputStream(sourceFile);
                 URL url = new URL(upLoadServerUri);
                 conn = (HttpURLConnection) url.openConnection(); 
                 conn.setDoInput(true);
                 conn.setDoOutput(true);
                 conn.setUseCaches(false);
                 conn.setRequestMethod("POST");
                 conn.setRequestProperty("Connection", "Keep-Alive");
                 conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                 conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                 conn.setRequestProperty("uploaded_file", fileName); 
                 dos = new DataOutputStream(conn.getOutputStream());
                 dos.writeBytes(twoHyphens + boundary + lineEnd); 
                 dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                 dos.writeBytes(lineEnd);
                 bytesAvailable = fileInputStream.available(); 
                 bufferSize = Math.min(bytesAvailable, maxBufferSize);
                 buffer = new byte[bufferSize];
                 bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                 while (bytesRead > 0) {
	                   dos.write(buffer, 0, bufferSize);
	                   bytesAvailable = fileInputStream.available();
	                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
                 }
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                 serverResponseCode = conn.getResponseCode();
                 String serverResponseMessage = conn.getResponseMessage();
                 //Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                 //Toast.makeText(this,  serverResponseMessage,  Toast.LENGTH_LONG).show();
                 if(serverResponseCode == 200){
                     runOnUiThread(new Runnable() {
                          public void run() {
                              //String msg = "File Upload Completed.\n\n See uploaded file here : \n\n" + " http://www.androidexample.com/media/uploads/" + uploadFileName;
                              //MainActivity.setText(msg);
                              Toast.makeText(MainActivity.this, "File Upload Complete.", Toast.LENGTH_LONG).show();
                          }
                      });                
                 }    
                  
                 fileInputStream.close();
                 dos.flush();
                 dos.close();
                   
            } catch (MalformedURLException ex) {
                dialog.dismiss();  
                ex.printStackTrace();
                final String emsg = ex.getMessage();
                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(MainActivity.this, "MalformedURLException : " + emsg, Toast.LENGTH_LONG).show();
                    }
                });
                //Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
            } catch (Exception e) {
                dialog.dismiss();  
                e.printStackTrace();
                final String emsg = e.getMessage();
                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(MainActivity.this, "Got Exception : " + emsg, Toast.LENGTH_LONG).show();
                    }
                });
                //Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);  
            }
            dialog.dismiss();       
            return serverResponseCode; 
         }
	}
}
