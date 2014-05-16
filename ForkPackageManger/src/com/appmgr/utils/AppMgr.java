package com.appmgr.utils;

/** author changqiang **/
/** email  hcq0618@163.com **/
import java.lang.reflect.Method;

import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;

public final class AppMgr {
	public static final String COMMAND_MOVE_PACKAGE = "movePackage";

	public static final int RETURN_CODE_FAILED = -1;
	public static final int RETURN_CODE_SUCCESS = 0;
	public static final int STORE_APP_EXTERNAL_LOCALTION = 2;
	public static final int STORE_APP_INTERNAL_LOCALTION = 1;
	public static final int STORE_APP_AUTO_LOCALTION = 0;

	Object mPackageManager;

	private static void returnCommand(int paramInt) {
		System.out.print(paramInt + "\n");
		System.exit(0);
	}

	public static void main(String[] args) {
		for (String arg : args)
			System.out.print("options:" + arg + "\n");
		new AppMgr().handleCommand(args);
	}

	private void handleCommand(String[] args) {
		// TODO Auto-generated method stub
		try {
			String command = args[0];
			if (command == null || command.equals(""))
				return;

			Class<?> serviceManager = Class
					.forName("android.os.ServiceManager");
			Method getService = serviceManager.getMethod("getService",
					java.lang.String.class);
			IBinder service = (IBinder) getService.invoke(null, "package");

			Class<?> iPackageManagerStub = Class
					.forName("android.content.pm.IPackageManager$Stub");
			Method asInterface = iPackageManagerStub.getMethod("asInterface",
					IBinder.class);
			mPackageManager = asInterface.invoke(null, service);

			if (mPackageManager == null) {
				System.out.println(getClass() + ":mPackageManager is null\n");
				returnCommand(RETURN_CODE_FAILED);
				return;
			}

			if (command.equalsIgnoreCase(COMMAND_MOVE_PACKAGE)) {
				if (args.length < 2)
					return;
				runMovePackage(args[1], args[2]);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			returnCommand(RETURN_CODE_FAILED);
		}
	}

	private void runMovePackage(String packageName, String location) {
		try {
			int install_location = Integer.parseInt(location);
			PackageMoveObserver localPackageMoveObserver = new PackageMoveObserver();
			for (Method m : PackageManager.class.getDeclaredMethods()) {
				System.out.print("method:" + m.getName() + "\n");
				// Method movePackage = PackageManager.class.getMethod(
				// COMMAND_MOVE_PACKAGE, String.class,
				// android.content.pm.IPackageMoveObserver.Stub.class,
				// Integer.TYPE);
				if (m.getName().equalsIgnoreCase(COMMAND_MOVE_PACKAGE)) {
					m.invoke(mPackageManager, packageName,
							localPackageMoveObserver, install_location);
					break;
				}
			}

			boolean bool = localPackageMoveObserver.finished;
			if (!bool) {
				localPackageMoveObserver.wait();
			}

			switch (localPackageMoveObserver.result) {
			case 1:
				returnCommand(RETURN_CODE_SUCCESS);
				return;
			default:
				returnCommand(RETURN_CODE_FAILED);
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			returnCommand(RETURN_CODE_FAILED);
		}
	}

	class PackageMoveObserver extends
			android.content.pm.IPackageMoveObserver.Stub {

		boolean finished = false;
		int result;

		public void packageMoved(String paramString, int paramInt)
				throws RemoteException {
			finished = true;
			result = paramInt;
			notifyAll();
		}
	}
}
