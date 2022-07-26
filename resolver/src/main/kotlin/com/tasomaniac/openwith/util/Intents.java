package com.tasomaniac.openwith.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;

import timber.log.Timber;

public class Intents {

    public static final int PACKAGE_MANAGER_QUERY_FLAGS =
            Build.VERSION.SDK_INT > Build.VERSION_CODES.N ?
                    PackageManager.MATCH_DISABLED_COMPONENTS |
                            PackageManager.MATCH_UNINSTALLED_PACKAGES |
                            PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS
                    : 0;

    private Intents() {
        //no instance
    }

    public static Intent homeScreenIntent() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return homeIntent;
    }

    public static void startActivityFixingIntent(Context context, Intent intent)
            throws SecurityException, ActivityNotFoundException {
        try {
            final String packageName = intent.getComponent().getPackageName();
            final ApplicationInfo applicationInfo = context
                    .getPackageManager()
                    .getApplicationInfo(packageName, PACKAGE_MANAGER_QUERY_FLAGS);
            if (!applicationInfo.enabled &&
                    Runtime
                            .getRuntime()
                            .exec(new String[] { "su", "-c", "pm enable " + packageName })
                            .waitFor() != 0
            )
                throw new IOException("Unexpected su binary exit code.");
        } catch (SecurityException | IOException | InterruptedException e) {
            Timber.w(e, "Disabled activity unfreezing failed, trying to start anyway");
        } catch (PackageManager.NameNotFoundException e) {
            throw new ActivityNotFoundException(e.getMessage());
        }

        context.startActivity(IntentFixer.fixIntents(context, intent));
    }
}
