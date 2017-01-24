package de.srlabs.snoopsnitch.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import de.srlabs.snoopsnitch.EncryptedFileWriterError;
import de.srlabs.snoopsnitch.R;
import de.srlabs.snoopsnitch.StartupActivity;
import de.srlabs.snoopsnitch.analysis.ImsiCatcher;
import de.srlabs.snoopsnitch.BuildConfig;
import de.srlabs.snoopsnitch.qdmon.EncryptedFileWriter;
import de.srlabs.snoopsnitch.qdmon.MsdSQLiteOpenHelper;
import de.srlabs.snoopsnitch.upload.DumpFile;


public class Utils {

    private final static String TAG = "SNSN";
    private final static String mTAG = "Utils";

	public static HttpsURLConnection openUrlWithPinning(Context context, String strUrl) throws
			IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException {

        URL url = new URL(strUrl);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		final InputStream keystoreInputStream = context.getAssets().open("keystore.bks");

		final KeyStore keystore = KeyStore.getInstance("BKS");
		keystore.load(keystoreInputStream, "password".toCharArray());
		keystoreInputStream.close();

		final TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init(keystore);

		final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, tmf.getTrustManagers(), null);

		// TODO: Handle pinning errors with an appropriate error message/Notification
		// The parameter "TLSv1.2" in SSLContext.getInstance() does *not* force
		// it to use only TLSv1.2 and it will still fall back to SSLv3, which is
		// rather insecure and leads to Exceptions on certain phones.
		connection.setSSLSocketFactory(new ForceTLSSocketFactory(sslContext.getSocketFactory()));
		return connection;
	}

	/**
	 * Generates a new random app ID. Currently the App id consists of 8
	 * hexadecimal digits generated based on the Android SecureRandom class.
	 * 
	 * @return
	 */
	// We don't care about the quality of random here...
	//@SuppressLint("TrulyRandom")
	public static String generateAppId(){
		SecureRandom sr = new SecureRandom();
		byte[] random = new byte[4];
		sr.nextBytes(random);
		return String.format("%02x%02x%02x%02x", random[0],random[1],random[2],random[3]);
	}
	public static String formatTimestamp(long millis){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date date = new Date(millis);
		return dateFormat.format(date);
	}

    /*
    // ----------------------------------------------------------------------
    // Here we are interested in the technology and not the speed, so we
    // consider everything with 2,2.5,2.75 as "2G". Same for 3G.
    // The following were extracted from Google sources in getNetworkClass(),
    // from TelephonyManager.java.
    // ----------------------------------------------------------------------
    //2G
    NETWORK_TYPE_1xRTT      *       // This is labelled by Google as 2G, but is really a 3G technology
    NETWORK_TYPE_CDMA               // CDMA: Either IS95A or IS95B
    NETWORK_TYPE_EDGE
    NETWORK_TYPE_GPRS
    NETWORK_TYPE_GSM        *       // API >24
    NETWORK_TYPE_IDEN       *
    //3G
    NETWORK_TYPE_EHRPD      *       // CDMA: eHRPD
    NETWORK_TYPE_EVDO_0     *
    NETWORK_TYPE_EVDO_A     *
    NETWORK_TYPE_EVDO_B     *
    NETWORK_TYPE_HSDPA
    NETWORK_TYPE_HSPA
    NETWORK_TYPE_HSPAP              // API >12
    NETWORK_TYPE_HSUPA
    NETWORK_TYPE_TD_SCDMA   *       // API >24
    NETWORK_TYPE_UMTS
    //4G
    NETWORK_TYPE_IWLAN      *       // API >24
    NETWORK_TYPE_LTE                // API >11
    // ----------------------------------------------------------------------
    // Where * has been added 2016-12-13.
    // ----------------------------------------------------------------------
    */

	/**
	 * Determines the network generation based on the networkType retrieved via telephonyManager.getNetworkType()
	 * @param networkType
	 * @return
	 * 0: Invalid value
	 * 2: GSM
	 * 3: 3G
	 * 4: LTE
	 */
	public static int networkTypeToNetworkGeneration(int networkType) {
		if (networkType == 0)
			return 0;
        else if (  // networkType == TelephonyManager.NETWORK_TYPE_1xRTT // Moved to 3G !!
                   networkType == TelephonyManager.NETWORK_TYPE_CDMA     // CDMA: Either IS95A or IS95B
                || networkType == TelephonyManager.NETWORK_TYPE_EDGE
                || networkType == TelephonyManager.NETWORK_TYPE_GPRS
                || networkType == TelephonyManager.NETWORK_TYPE_GSM      // API >24
                || networkType == TelephonyManager.NETWORK_TYPE_IDEN)
            return 2;
        else if (  networkType == TelephonyManager.NETWORK_TYPE_1xRTT    // This is labelled by Google as 2G, but is really a 3G technology
                || networkType == TelephonyManager.NETWORK_TYPE_EHRPD    // CDMA: eHRPD
				|| networkType == TelephonyManager.NETWORK_TYPE_EVDO_0
                || networkType == TelephonyManager.NETWORK_TYPE_EVDO_A
                || networkType == TelephonyManager.NETWORK_TYPE_EVDO_B
                || networkType == TelephonyManager.NETWORK_TYPE_HSDPA
                || networkType == TelephonyManager.NETWORK_TYPE_HSPA
                || networkType == TelephonyManager.NETWORK_TYPE_HSPAP    // API >12
				|| networkType == TelephonyManager.NETWORK_TYPE_HSUPA
				|| networkType == TelephonyManager.NETWORK_TYPE_TD_SCDMA // API >24
				|| networkType == TelephonyManager.NETWORK_TYPE_UMTS)
			return 3;
		else if (  networkType == TelephonyManager.NETWORK_TYPE_IWLAN    // API >24
                || networkType == TelephonyManager.NETWORK_TYPE_LTE)     // API >11
                // ToDo: ?? IWLAN may be considered prone to other type of MiTM attack and might
                //          need to be treated differently...
			return 4;
		else {
			return 0;
		}
	}

	public static String readFromAssets(Context context, String fileName) throws IOException {

		InputStream inputStream = context.getAssets().open(fileName);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1)	{
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException ee) {
            Log.e(TAG, mTAG + ": IOException in readFromAssets():\n"+ ee.toString());
		}
		return byteArrayOutputStream.toString();
	}

	public static String readFromFileInput(Context context, String fileName) throws IOException {

		InputStream inputStream = context.openFileInput(fileName);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1)	{
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException ee) {
            Log.e(TAG, mTAG + ": IOException in readFromFileInput():\n"+ ee.toString());
		}
		return byteArrayOutputStream.toString();
	}

	public static String readFromFileOrAssets(Context context, String fileName) throws IOException {
		String jsonData;
		//  FIXME: Check for file existence - do not use exception for control flow
		try{
			jsonData = readFromFileInput(context, fileName);
		} catch(FileNotFoundException e){
			jsonData = readFromAssets(context, fileName);
		}
		return jsonData;
	}

	public static void showDeviceIncompatibleDialog(Activity activity, String incompatibilityReason, final Runnable callback){
		String dialogMessage =
				activity.getResources().getString(R.string.alert_deviceCompatibility_header) + " " +	incompatibilityReason + " " +
				activity.getResources().getString(R.string.alert_deviceCompatibility_message);

		MsdDialog.makeFatalConditionDialog(activity, dialogMessage, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				callback.run();
			}
		}, null,
		new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				callback.run();
			}
		}, false
				).show();
	}

	private static String dumpRow (Cursor c) {
		String result = "VALUES(";
		for (int pos = 0; pos < c.getColumnCount(); pos++) {
			switch (c.getType(pos))	{
                case Cursor.FIELD_TYPE_NULL:
                    result += "null";
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    result += Integer.toString(c.getInt(pos));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    result += Float.toString(c.getFloat(pos));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    result += DatabaseUtils.sqlEscapeString(c.getString(pos));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    result += "X'";
                    for(byte b:c.getBlob(pos)) {
                        result += String.format("%02X",b);
                    }
                    result += "'";
                    break;
                default:
                    return "Invalid field type " + c.getType(pos) + " at position " + pos;
			}
			if (pos < c.getColumnCount() - 1) {
				result += ", ";
			}
		}
		return result + ")";
	}

	private static void dumpRows (SQLiteDatabase db, String table, EncryptedFileWriter outputFile,
								  String query) throws EncryptedFileWriterError {
		Cursor c = db.rawQuery (query, null);
		while (c.moveToNext()) {
			outputFile.write("INSERT INTO '" + table + "' " + dumpRow(c) + ";\n");
		}
		c.close();
	}

	/**
	 * Dump data related to an event to file
	 * The pseudo param id (ID of an event) is obtained by an SQL query of a given time range:
	 * @param startTime starting time range of events
	 * @param endTime ending time range of events
	 * @param outputFile Target file
	 * @throws EncryptedFileWriterError
	 */
	public static void dumpDatabase(Context context, SQLiteDatabase db, ImsiCatcher imsiCatcher,
									long startTime, long endTime, EncryptedFileWriter outputFile)
			throws EncryptedFileWriterError {

		// Create view with recent session_info IDs (last day)
		db.execSQL("DROP VIEW IF EXISTS si_dump");
		db.execSQL(
				"CREATE VIEW si_dump AS " +
				"SELECT id FROM session_info WHERE " +
				"(mcc > 0 AND lac > 0) AND " +
				"timestamp > datetime(" + Long.toString(startTime/1000) + ", 'unixepoch', '-1 hour') AND " +
				"timestamp < datetime(" + Long.toString(endTime/1000) + ", 'unixepoch', '+1 hour')");

		// session_info, paging_info
		dumpRows(db, "session_info", outputFile, "SELECT si.* FROM session_info as si, si_dump ON si_dump.id = si.id");
		dumpRows(db, "paging_info", outputFile,  "SELECT pi.* FROM paging_info as pi, si_dump  ON si_dump.id = pi.sid");

		// sms_meta, catcher and event entries
		dumpRows(db, "sms_meta", outputFile, "SELECT sm.* FROM sms_meta sm, si_dump on si_dump.id = sm.id;");
		dumpRows(db, "catcher", outputFile,  "SELECT c.* FROM catcher c, si_dump on si_dump.id = c.id;");
		dumpRows(db, "events", outputFile,  "SELECT e.* FROM events e, si_dump on si_dump.id = e.id;");

		// create view with all relevant cell_info IDs
		db.execSQL("DROP VIEW IF EXISTS ci_dump");
		String sql = "CREATE VIEW ci_dump AS " +
				"SELECT cell_info.* FROM cell_info, config WHERE ";
		if(imsiCatcher != null)
			sql += "(mcc = " + imsiCatcher.getMcc() + " AND mnc = " + imsiCatcher.getMnc() + " AND lac = " + imsiCatcher.getLac() + " AND cid = " + imsiCatcher.getCid() + ") OR ";
		sql += "(abs(strftime('%s', first_seen) - " + Long.toString(startTime/1000) +
				") < (cell_info_max_delta + (max(delta_arfcn, neig_max_delta))))";
		db.execSQL(sql);

		// cell_info, arfcn_list
		dumpRows(db, "cell_info", outputFile,  "SELECT ci.* FROM cell_info  as ci, ci_dump ON ci.id = ci_dump.id");
		dumpRows(db, "arfcn_list", outputFile, "SELECT al.* FROM arfcn_list as al, ci_dump ON al.id = ci_dump.id");

		// config
		dumpRows(db, "config", outputFile, "SELECT * FROM config;");

		// location_info (30 minutes before and after the event)
		dumpRows(db, "location_info", outputFile,
				"SELECT * FROM location_info WHERE " +
				"timestamp > datetime(" + Long.toString(startTime/1000) + ", 'unixepoch', '-30 minutes') AND " +
				"timestamp < datetime(" + Long.toString(endTime/1000) + ", 'unixepoch', '+30 minutes')");

		// info table
		String info =
				"INSERT INTO 'info' VALUES (\n" +
                "'" + MsdConfig.getAppId(context) + "', -- App ID\n" +
                //"'" + context.getResources().getString(R.string.app_version) + "', -- App version\n" +
                "'" + BuildConfig.VERSION_NAME + "', -- App version\n" +
                "'" + Build.VERSION.RELEASE +    "', -- Android version\n" +
                "'" + Build.MANUFACTURER +       "', -- Phone manufacturer\n" +
                "'" + Build.BOARD +              "', -- Phone board\n" +
                "'" + Build.BRAND +              "', -- Phone brand\n" +
                "'" + Build.PRODUCT +            "', -- Phone product\n" +
                "'" + Build.MODEL +              "', -- Phone model\n" +
                "'" + Build.getRadioVersion() +  "', -- Baseband\n" +
                "'" + MsdLog.getTime() +         "', -- Time of export\n" +
                (imsiCatcher == null ? "0" : Long.toString(imsiCatcher.getId())) + "   -- Offending ID\n" +
                ");";
		outputFile.write(info);
	}

	/**
	 * @param context
	 * @param db
	 * @param imsiCatcher Can be null for uploading suspicious activity
	 * @param startTime
	 * @param endTime
	 * @throws EncryptedFileWriterError
	 * @throws SQLException
	 * @throws IOException
	 */
	public static DumpFile uploadMetadata(Context context, SQLiteDatabase db, ImsiCatcher imsiCatcher,
                long startTime, long endTime, String prefix)
                throws EncryptedFileWriterError, SQLException, IOException {

		final boolean encryptedDump = true;
		final boolean plainDump = MsdConfig.dumpUnencryptedEvents(context);

		// Anonymize database before dumping
		MsdSQLiteOpenHelper.readSQLAsset(context, db, "anonymize.sql", false);

		String fileName = prefix + (imsiCatcher == null ? ("" + System.currentTimeMillis()) : Long.toString(imsiCatcher.getId())) + ".gz";
		EncryptedFileWriter outputFile =
				new EncryptedFileWriter(context, fileName + ".smime", encryptedDump, fileName, plainDump);

		Utils.dumpDatabase(context, db, imsiCatcher, startTime, endTime, outputFile);
		outputFile.close();

		DumpFile meta = new DumpFile(outputFile.getEncryptedFilename(), DumpFile.TYPE_METADATA, startTime, endTime);
		meta.setImsi_catcher(true);
		meta.recordingStopped();
		meta.insert(db);
		meta.markForUpload(db);
		return meta;
	}

	/**
	 * Gets the device major number of the diag device from /proc/devices (or
	 * null if the Kernel does not contain a diag driver).
	 *
	 * @return
	 */
	public static Integer getDiagDeviceNodeMajor() {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/devices")));
			while(true){
				String line = r.readLine();
				if(line == null) // EOF
					return null;
				line = line.trim();
				String[] line_elements = line.split("\\s+");
				if(line_elements.length != 2)
					continue;
				if(line_elements[1].trim().toLowerCase(Locale.US).equals("dia")){
					return Integer.parseInt(line_elements[0].trim());
				}
			}
		} catch (IOException e) {
			return null;
		} finally{
			try {
				if(r != null)
					r.close();
			} catch (IOException ee) {
                Log.e(TAG, mTAG + ": IOException in getDiagDeviceNodeMajor():\n"+ ee.toString());
            }
		}
	}
	/**
	 * Create the diag device (if it doesn't already exist).
	 * @return
	 * Error message as String or null if successful
	 */
	public static String createDiagDevice(){
		File diagDevice = new File("/dev/diag");
		if(diagDevice.exists())
			return null;
		Integer diagDeviceMajor = Utils.getDiagDeviceNodeMajor();
		if(diagDeviceMajor == null){
			return "Diag device does not exist and /proc/devices does not contain any entry for 'dia'";
		}
		// Try both standard mknod and busybox mknod
		// ToDo: Need to check if we actually HAVE busybox
		String mknodCmd = "mknod /dev/diag c " + diagDeviceMajor + " 0 || busybox mknod /dev/diag c " + diagDeviceMajor + " 0";
		String suBinary = DeviceCompatibilityChecker.getSuBinary();
		String cmd[] = { suBinary, "-c", mknodCmd};
		Process mknod;
		try {
			mknod = Runtime.getRuntime().exec(cmd);
		} catch (IOException e1) {
            Log.e(TAG, mTAG + ": IOException in createDiagDevice():\n"+ e1.toString());
			return e1.getMessage();
		}
		try {
			mknod.waitFor();
		} catch (InterruptedException ee) {
            Log.e(TAG, mTAG + ": InterruptedException in createDiagDevice():\n"+ ee.toString());
		}
		if(!diagDevice.exists()){
			return "Failed to create diag device: " + mknodCmd;
		}
		return null;
	}

    // Defined in build.gradle
    public static String getPackageName() { return BuildConfig.APPLICATION_ID; }

    /**
     * WIP
     *
     * Dump (or backup) database to SD card.
     *
     * Reference:  Redmine Task #1542  -- Add local database export
     *
     */
    public void exportDatabase() {
        try {
            File sd = Environment.getExternalStorageDirectory(); //  /storage/emulated/0/
            File data = Environment.getDataDirectory();          //  /data/

            String databaseName = "msd.db";

            if (sd.canWrite()) {
                //  /data/data/de.srlabs.snoopsnitch/databases/msd.db
                String currentDBPath = "//data//" + getPackageName() + "//databases//" + databaseName + "";
                String backupDBPath = "msd_backup.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, mTAG + ":exportDatabase() Failed with exception: " + e);
        }
    }

    public static boolean isDeviceMSM() {

        //MsdLog.osgetprop();
        return true; //
    }

}
