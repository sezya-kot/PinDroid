/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.activity;

import java.util.ArrayList;

import com.pindroid.R;
import com.pindroid.Constants;
import com.pindroid.authenticator.AuthenticatorActivity;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class AppBaseListActivity extends ListActivity {
	
	protected AccountManager mAccountManager;
	protected Account mAccount;
	protected Context mContext;
	protected String username = null;
	protected Resources res;
	protected SharedPreferences settings;
	
	protected long lastUpdate;
	protected boolean privateDefault;
	protected boolean toreadDefault;
	protected String defaultAction;
	protected boolean markAsRead;
	
	private boolean first = true;
	
	Bundle savedState;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		savedState = savedInstanceState;
		super.onCreate(savedState);
		
		res = getResources();
		
		mContext = this;
		mAccountManager = AccountManager.get(this);
		
		loadSettings();
		init();
	}
	
	private void init(){
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length < 1) {		
			Intent i = new Intent(this, AuthenticatorActivity.class);
			startActivity(i);
			
			return;
		} else if(lastUpdate == 0) {
	
			Toast.makeText(this, res.getString(R.string.syncing_toast), Toast.LENGTH_LONG).show();
			
			if(mAccount == null || username == null)
				loadAccounts();
			
			ContentResolver.requestSync(mAccount, BookmarkContentProvider.AUTHORITY, Bundle.EMPTY);
		} else {
			loadAccounts();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if(!first) {
			loadSettings();
			init();
		}
		first = false;
		
	}
	
	private void loadAccounts(){
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		}
		
		ArrayList<String> accounts = new ArrayList<String>();
		
		for(Account a : mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)) {
			accounts.add(a.name);
		}
		
		BookmarkManager.TruncateBookmarks(accounts, this, true);
		TagManager.TruncateOldTags(accounts, this);
		
		username = mAccount.name;
	}
	
	private void loadSettings(){
		settings = PreferenceManager.getDefaultSharedPreferences(this);
    	lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	privateDefault = settings.getBoolean("pref_save_private_default", false);
    	toreadDefault = settings.getBoolean("pref_save_toread_default", false);
    	defaultAction = settings.getString("pref_view_bookmark_default_action", "browser");
    	markAsRead = settings.getBoolean("pref_markasread", false);	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    if(!isMyself()) {
	    	menu.findItem(R.id.menu_search).setEnabled(false);
	    }
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_addbookmark:
			Intent addBookmark = new Intent(this, AddBookmark.class);
			startActivity(addBookmark);
			return true;
	    case R.id.menu_search:			
			this.onSearchRequested();
	        return true;
	    case R.id.menu_settings:
			Intent prefs = new Intent(this, Preferences.class);
			startActivity(prefs);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	protected boolean isMyself() {
		if(mAccount != null && username != null)
			return mAccount.name.equals(username);
		else return false;
	}
}