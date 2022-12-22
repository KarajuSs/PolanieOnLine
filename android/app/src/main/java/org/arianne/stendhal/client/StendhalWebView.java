/***************************************************************************
 *                     Copyright © 2022 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package org.arianne.stendhal.client;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
//import android.view.DragEvent;
//import android.view.InputDevice;
//import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class StendhalWebView {
	private boolean testing = true;
	private boolean gameActive = false;

	private final AppCompatActivity mainActivity;
	private WebView clientView;

	public StendhalWebView(final AppCompatActivity activity) {
		mainActivity = activity;
	}

	public void init() {
		clientView = (WebView) mainActivity.findViewById(R.id.clientWebView);
		final WebSettings viewSettings = clientView.getSettings();

		viewSettings.setJavaScriptEnabled(true);

		// keep elements in position in portrait mode
		viewSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		viewSettings.setLoadWithOverviewMode(true);
		viewSettings.setUseWideViewPort(true);
		
		initLoadURLHandler();
		initTouchHandler();

		// TODO: "main" should be default & "testing" used in separate build
		selectServer();

		// initial page
		clientView.loadUrl("https://s1.polanieonline.eu/account/mycharacters.html");
	}

	private void initLoadURLHandler() {
		clientView.setWebViewClient(new WebViewClient() {
			/* handle changing URLs */
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				/* FIXME: external URLs should be opened in browser */
				view.loadUrl(checkClientUrl(url));
				gameActive = isClientUrl(clientView.getUrl());

				return false;
			}
		});
	}

	private void initTouchHandler() {
		/*
		clientView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(final View view, final MotionEvent event) {
				if (gameActive) {
					final int source = event.getSource();
					if (source == InputDevice.SOURCE_TOUCHSCREEN) {
						view.dispatchTouchEvent(MotionEvent.obtain(
							event.getDownTime(), event.getEventTime(), event.getAction(),
							event.getX(), event.getY(), event.getPressure(), event.getSize(),
							event.getMetaState(), event.getXPrecision(), event.getYPrecision(),
							InputDevice.SOURCE_MOUSE, event.getEdgeFlags()));
						// consume event
						return true;
					}
					return true;
				}
				return false;
			}
		});
		clientView.setOnDragListener(new View.OnDragListener() {
			@Override
			public boolean onDrag(final View view, final DragEvent event) {
				if (gameActive) {
					// consume event
					return true;
				}
				return false;
			}
		});
		*/
	}

	/**
	 * Checks if a URL is a link to one of the web clients.
	 *
	 * @param url
	 *     URL to be checked.
	 * @return
	 *     <code>true</code> if url links to "client" or "testclient".
	 */
	private boolean isClientUrl(final String url) {
		return url.contains("polanieonline.eu/client/")
			|| url.contains("polanieonline.eu/testclient/");
	}

	/**
	 * Formats client URL for currently selected server.
	 *
	 * @param url
	 *     URL to be checked.
	 * @return
	 *     URL to be loaded.
	 */
	private String checkClientUrl(String url) {
		if (isClientUrl(url)) {
			if (testing) {
				url = url.replace("/client/", "/testclient/");
			} else {
				url = url.replace("/testclient/", "/client/");
			}
		}

		return url;
	}

	/**
	 * Opens a message dialog for user to choose between main & test servers.
	 */
	private void selectServer() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
		builder.setMessage("Wybierz serwer");
		builder.setCancelable(false);

		builder.setPositiveButton("Główny", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				testing = false;
				dialog.cancel();
			}
		});

		builder.setNegativeButton("Testowy", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				testing = true;
				dialog.cancel();
			}
		});

		final AlertDialog selectServer = builder.create();
		selectServer.show();
	}
}
