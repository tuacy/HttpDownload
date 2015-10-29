package com.tuacy.httpdownload;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.tuacy.library.httpdownload.DownloadListener;
import com.tuacy.library.httpdownload.DownloadManager;
import com.tuacy.library.httpdownload.DownloadRequest;


public class MainActivity extends AppCompatActivity {

	private static final String DEST_DIR_PATH = Environment.getExternalStorageDirectory().toString() + "/G-SmartRouter/download/";

	private ProgressBar mProgressBar1;
	private Button      mBtnStart1;
	private Button      mBtnStop1;
	private Button      mBtnCancel1;
	private ProgressBar mProgressBar2;
	private Button      mBtnStart2;
	private Button      mBtnStop2;
	private Button      mBtnCancel2;
	private ProgressBar mProgressBar3;
	private Button      mBtnStart3;
	private Button      mBtnStop3;
	private Button      mBtnCancel3;
	private ProgressBar mProgressBar4;
	private Button      mBtnStart4;
	private Button      mBtnStop4;
	private Button      mBtnCancel4;
	private ProgressBar mProgressBar5;
	private Button      mBtnStart5;
	private Button      mBtnStop5;
	private Button      mBtnCancel5;
	private Button      mBtnStartAll;
	private Button      mBtnStopAll;
	private Button      mBtnCancelAll;

	private DownloadRequest mRequest1;
	private DownloadRequest mRequest2;
	private DownloadRequest mRequest3;
	private DownloadRequest mRequest4;
	private DownloadRequest mRequest5;
	private DownloadManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		mProgressBar1 = (ProgressBar) findViewById(R.id.progress_download1);
		mBtnStart1 = (Button) findViewById(R.id.btn_start_download1);
		mBtnStop1 = (Button) findViewById(R.id.btn_stop_download1);
		mBtnCancel1 = (Button) findViewById(R.id.btn_cancel_download1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.progress_download2);
		mBtnStart2 = (Button) findViewById(R.id.btn_start_download2);
		mBtnStop2 = (Button) findViewById(R.id.btn_stop_download2);
		mBtnCancel2 = (Button) findViewById(R.id.btn_cancel_download2);
		mProgressBar3 = (ProgressBar) findViewById(R.id.progress_download3);
		mBtnStart3 = (Button) findViewById(R.id.btn_start_download3);
		mBtnStop3 = (Button) findViewById(R.id.btn_stop_download3);
		mBtnCancel3 = (Button) findViewById(R.id.btn_cancel_download3);
		mProgressBar4 = (ProgressBar) findViewById(R.id.progress_download4);
		mBtnStart4 = (Button) findViewById(R.id.btn_start_download4);
		mBtnStop4 = (Button) findViewById(R.id.btn_stop_download4);
		mBtnCancel4 = (Button) findViewById(R.id.btn_cancel_download4);
		mProgressBar5 = (ProgressBar) findViewById(R.id.progress_download5);
		mBtnStart5 = (Button) findViewById(R.id.btn_start_download5);
		mBtnStop5 = (Button) findViewById(R.id.btn_stop_download5);
		mBtnCancel5 = (Button) findViewById(R.id.btn_cancel_download5);

		mBtnStartAll = (Button) findViewById(R.id.btn_start_all);
		mBtnStopAll = (Button) findViewById(R.id.btn_stop_all);
		mBtnCancelAll = (Button) findViewById(R.id.btn_cancel_all);
	}

	private void initData() {
		mManager = DownloadManager.getInstance(3);
		mRequest1 = new DownloadRequest().setUrl("http://219.235.31.61/sw/189/26/info12")
										 .setDestDirectory(DEST_DIR_PATH)
										 .setProgressInterval(1000);
		mRequest1.setDownloadListener(new CustomerDownloadListener(mProgressBar1));
		mRequest1.setPriority(DownloadRequest.Priority.HIGH);
		mRequest2 = new DownloadRequest().setUrl("http://219.235.31.61/sw/193/32/info23")
										 .setDestDirectory(DEST_DIR_PATH)
										 .setProgressInterval(1000);
		mRequest2.setDownloadListener(new CustomerDownloadListener(mProgressBar2));
		mRequest2.setPriority(DownloadRequest.Priority.LOW);
		mRequest3 = new DownloadRequest().setUrl("http://219.235.31.61/sw/197/25/info22")
										 .setDestDirectory(DEST_DIR_PATH)
										 .setProgressInterval(1000);
		mRequest3.setDownloadListener(new CustomerDownloadListener(mProgressBar3));
		mRequest4 = new DownloadRequest().setUrl("http://219.235.31.61/sw/2/5/info1")
										 .setDestDirectory(DEST_DIR_PATH)
										 .setProgressInterval(1000);
		mRequest4.setDownloadListener(new CustomerDownloadListener(mProgressBar4));
		mRequest5 = new DownloadRequest().setUrl("http://219.235.31.61/sw/2/74/info2")
										 .setDestDirectory(DEST_DIR_PATH)
										 .setProgressInterval(1000);
		mRequest5.setDownloadListener(new CustomerDownloadListener(mProgressBar5));
		mRequest5.setPriority(DownloadRequest.Priority.HIGH);
	}

	private void initEvent() {

		mBtnStart1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = mManager.add(mRequest1);
				Log.d("vae_tag", id + "   id");
			}
		});
		mBtnStop1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.stop("http://219.235.31.61/sw/189/26/info12");
			}
		});
		mBtnCancel1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.cancel("http://219.235.31.61/sw/189/26/info12");
			}
		});

		mBtnStart2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = mManager.add(mRequest2);
				Log.d("vae_tag", id + "   id");
			}
		});
		mBtnStop2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.stop("http://219.235.31.61/sw/193/32/info23");
			}
		});
		mBtnCancel2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.cancel("http://219.235.31.61/sw/193/32/info23");
			}
		});

		mBtnStart3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = mManager.add(mRequest3);
				Log.d("vae_tag", id + "   id");
			}
		});
		mBtnStop3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.stop("http://219.235.31.61/sw/197/25/info22");
			}
		});
		mBtnCancel3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.cancel("http://219.235.31.61/sw/197/25/info22");
			}
		});

		mBtnStart4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = mManager.add(mRequest4);
				Log.d("vae_tag", id + "   id");
			}
		});
		mBtnStop4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.stop("http://219.235.31.61/sw/2/5/info1");
			}
		});
		mBtnCancel4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.cancel("http://219.235.31.61/sw/2/5/info1");
			}
		});

		mBtnStart5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = mManager.add(mRequest5);
				Log.d("vae_tag", id + "   id");
			}
		});
		mBtnStop5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.stop("http://219.235.31.61/sw/2/74/info2");
			}
		});
		mBtnCancel5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.cancel("http://219.235.31.61/sw/2/74/info2");
			}
		});

		mBtnStartAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("vae_tag", "request1 id:" + mManager.add(mRequest1));
				Log.d("vae_tag", "request2 id:" + mManager.add(mRequest2));
				Log.d("vae_tag", "request3 id:" + mManager.add(mRequest3));
				Log.d("vae_tag", "request4 id:" + mManager.add(mRequest4));
				Log.d("vae_tag", "request5 id:" + mManager.add(mRequest5));
			}
		});
		mBtnStopAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.stopAll();
			}
		});
		mBtnCancelAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mManager.cancelAll();
			}
		});
	}

	private class CustomerDownloadListener implements DownloadListener {

		public CustomerDownloadListener(ProgressBar progressBar) {
			mProgressBar = progressBar;
		}

		private ProgressBar mProgressBar;

		@Override
		public void onStart(int downloadId, String url, String filePath, long totalBytes) {
			Log.d("vae_tag", "onStart url:" + filePath);
		}

		@Override
		public void onRetry(int downloadId, String url, String filePath) {
			Log.d("vae_tag", "onRetry url:" + url);
		}

		@Override
		public void onProgress(int downloadId, String url, String filePath, long bytesWritten, long totalBytes) {
			mProgressBar.setProgress((int) ((bytesWritten * 1.0f / totalBytes) * 100));
		}

		@Override
		public void onSuccess(int downloadId, String url, String filePath) {
			Log.d("vae_tag", "onSuccess url:" + url);
		}

		@Override
		public void onFailure(int downloadId, String url, String filePath, int statusCode, String errMsg) {
			Log.d("vae_tag", "onFailure url:" + url);
		}

		@Override
		public void onCancel(int downloadId, String url, String filePath) {
			Log.d("vae_tag", "onCancel url:" + url);
		}

		@Override
		public void onStop(int downloadId, String url, String filePath) {
			Log.d("vae_tag", "onStop");
		}
	}
}
