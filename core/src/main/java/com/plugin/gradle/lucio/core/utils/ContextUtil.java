
package com.plugin.gradle.lucio.core.utils;

import android.content.Context;

public class ContextUtil {

		private static volatile Context sApplicationContext;

		public static Context getAppContext() {
			if (sApplicationContext == null) {
				synchronized (ContextUtil.class) {
					if (sApplicationContext == null) {
						try {
							sApplicationContext = Reflect.on("android.app.ActivityThread").call("currentActivityThread").call("getApplication").get();
						} catch (Throwable e1) {
							sApplicationContext = Reflect.on("android.app.ActivityThread").call("currentApplication").get();
							e1.printStackTrace();
						}
					}
				}
			}
			return sApplicationContext;
		}
	}