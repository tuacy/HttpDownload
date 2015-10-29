package com.tuacy.library.httpdownload;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class DownloadDispatcher {

	private static final String TAG = DownloadRequestQueue.class.getSimpleName();

	private static final int MESSAGE_ADD_NEW_TASK = 0x000;

	/**
	 * Internal task scheduling Thread + Loop + Handler
	 */
	private Thread          mPoolThread                 = null;
	private Handler         mPoolThreadHandler          = null;
	/**
	 * Make sure Handler init
	 */
	private ExecutorService mThreadPool                 = null;
	private Semaphore       mSemaphorePoolThreadHandler = new Semaphore(0);
	private Semaphore       mSemaphoreThreadPool        = null;

	/**
	 * Queue
	 */
	private DownloadRequestQueue mDownloadRequestQueue = null;
	private DownloadDelivery     mDownloadDelivery     = null;

	public DownloadDispatcher(int threadCount, int queueMaxCount) {
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mSemaphoreThreadPool = new Semaphore(threadCount);
		mDownloadRequestQueue = new DownloadRequestQueue(this, queueMaxCount);
		mDownloadDelivery = new DownloadDelivery(new Handler(Looper.getMainLooper()));
		initInternalScheduling();
	}

	private void initInternalScheduling() {
		mPoolThread = new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				mPoolThreadHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						switch (msg.what) {
							case MESSAGE_ADD_NEW_TASK:
								/** get form queue */
								DownloadRequest request = mDownloadRequestQueue.dequeue();
								if (null != request) {
									/** add to downloading Set list */
									mDownloadRequestQueue.startDownloadRequest(request);
									DownloadRunnable downloadRunnable = new DownloadRunnable(request, mDownloadDelivery);
									mThreadPool.execute(downloadRunnable);
									acquireThreadPoolSemaphore();
								}
								break;
						}
					}
				};
				mSemaphorePoolThreadHandler.release();
				Looper.loop();
			}
		};
		mPoolThread.start();
	}


	public int add(DownloadRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("DownloadRequest cannot be null");
		}
		request.cleanCancelOrStopState();

		/** if download id is not set, generate one */
		if (request.getDownloadId() == -1) {
			int downloadId = mDownloadRequestQueue.getSequenceNumber();
			request.setDownloadId(downloadId);
		}

		/** add download request into download request queue */
		if (mDownloadRequestQueue.enqueue(request)) {
			/** make sure Handler init */
			if (null == mPoolThreadHandler) {
				try {
					mSemaphorePoolThreadHandler.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			sendMessage();
			return request.getDownloadId();
		} else {
			return -1;
		}
	}

	public void sendMessage() {
		/** send message to internal scheduling add a new task */
		mPoolThreadHandler.sendEmptyMessage(MESSAGE_ADD_NEW_TASK);
	}

	public void acquireThreadPoolSemaphore() {
		if (null != mSemaphoreThreadPool) {
			try {
				mSemaphoreThreadPool.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void releaseThreadPoolSemaphore() {
		if (null != mSemaphoreThreadPool) {
			mSemaphoreThreadPool.release();
		}
	}

	public void cancel(int downloadId) {
		mDownloadRequestQueue.setCancel(downloadId);
	}

	public void cancel(String url) {
		mDownloadRequestQueue.setCancel(url);
	}

	public void stop(int downloadId) {
		mDownloadRequestQueue.setStop(downloadId);
	}

	public void stop(String url) {
		mDownloadRequestQueue.setStop(url);
	}

	public void stopAll() {
		mDownloadRequestQueue.stopAll();
	}

	public void cancelAll() {
		mDownloadRequestQueue.cancelAll();
	}

	public void exit() {
		if (null != mPoolThread) {
			mPoolThread.interrupt();
		}
	}
}
