package org.fox.ttrss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.fox.ttrss.offline.OfflineDownloadService;
import org.fox.ttrss.share.CommonShareActivity;

public class GoOfflineActivity extends CommonShareActivity {
	private BroadcastReceiver m_broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context content, Intent intent) {
			if (intent.getAction().equals(OfflineDownloadService.INTENT_ACTION_SUCCESS)) {
				SharedPreferences localPrefs = getSharedPreferences("localprefs", Context.MODE_PRIVATE);
			    SharedPreferences.Editor editor = localPrefs.edit();
				editor.putBoolean("offline_mode_active", true);
				editor.commit();
			}

			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ApiRequest.disableConnectionReuseIfNecessary();

		IntentFilter filter = new IntentFilter();
		filter.addAction(OfflineDownloadService.INTENT_ACTION_SUCCESS);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(m_broadcastReceiver, filter);
		
		SharedPreferences localPrefs = getSharedPreferences("localprefs", Context.MODE_PRIVATE);		
		boolean isOffline = localPrefs.getBoolean("offline_mode_active", false);

		if (!isOffline) {
			ApiRequest.trustAllHosts(m_prefs.getBoolean("ssl_trust_any", false),
					m_prefs.getBoolean("ssl_trust_any_host", false));
			login(0);
		}
		else
			finish();
	}

	@Override
	protected void onLoggedIn(int requestId) {
		Intent intent = new Intent(GoOfflineActivity.this, OfflineDownloadService.class);
		intent.putExtra("sessionId", m_sessionId);
		startService(intent);
	}

	@Override
	protected void onLoggingIn(int requestId) {
	}
	
	@Override
	protected void onLoginFailure() {
		finish();
	}
}
