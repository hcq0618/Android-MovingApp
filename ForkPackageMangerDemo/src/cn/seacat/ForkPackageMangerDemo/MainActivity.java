package cn.seacat.ForkPackageMangerDemo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.movepackagedemo.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	public static final int STORE_APP_EXTERNAL_LOCALTION = 2;
	public static final int STORE_APP_INTERNAL_LOCALTION = 1;
	public static final int STORE_APP_AUTO_LOCALTION = 0;

	public static final String COMMAND_MOVE_PACKAGE = "movePackage";

	public static final String RETURN_CODE_FAILED = "fail";
	public static final String RETURN_CODE_SUCCESS = "success";

	public static final String FORK_PACKAGE_MANAGER_CLASSNAME = "com.appmgr.utils.AppMgr";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					moveApp(MainActivity.this, "com.qihoo.appstore",
							"appmgr_dex.jar", STORE_APP_EXTERNAL_LOCALTION);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 
	 * 
	 * @param cx
	 * @param pkgName
	 * @param jar_name
	 * @param location
	 * @throws Exception
	 */
	public static boolean moveApp(Context cx, String pkgName, String jar_name,
			int location) {
		boolean result = false;
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			String data_path = cx.getFilesDir().getParent();
			String jar_path = data_path + "/" + jar_name;
			File out_file = new File(jar_path);
			if (!out_file.exists()) {
				inBuff = new BufferedInputStream(cx.getAssets().open(jar_name));

				outBuff = new BufferedOutputStream(new FileOutputStream(
						out_file));

				byte[] b = new byte[1024 * 5];
				int len;
				while ((len = inBuff.read(b)) != -1) {
					outBuff.write(b, 0, len);
				}
				outBuff.flush();
			}

			String params = " " + COMMAND_MOVE_PACKAGE + " " + pkgName + " "
					+ location + "\n";
			String print_result = appProcessCmd(cx, jar_path,
					FORK_PACKAGE_MANAGER_CLASSNAME, params);
			result = !print_result.contains(RETURN_CODE_FAILED);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				if (inBuff != null)
					inBuff.close();
				if (outBuff != null)
					outBuff.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return result;
	}

	private static String appProcessCmd(Context cx, String jar_path,
			String class_path, String params) throws Exception {
		String result = RETURN_CODE_FAILED;
		Process p = null;
		DataOutputStream os = null;
		InputStream is = null;
		try {
			p = Runtime.getRuntime().exec("su\n");

			os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
			os.writeBytes("export CLASSPATH=" + jar_path + "\n");
			os.writeBytes("exec app_process /system/bin " + class_path + params);
			os.flush();
			p.waitFor();

			is = p.getInputStream();
			if (is.available() > 0)
				result = getStringFromIO(is);
		} finally {
			if (os != null)
				os.close();
			if (is != null)
				is.close();
		}
		return result;
	}

	public static String getStringFromIO(InputStream is) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		String result = null;
		try {
			String temp;
			StringBuilder sb = new StringBuilder();
			br = new BufferedReader(new InputStreamReader(is));
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}

			result = sb.toString();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
		}
		return result;
	}

}
