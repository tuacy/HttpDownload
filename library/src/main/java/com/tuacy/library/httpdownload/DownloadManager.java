package com.tuacy.library.httpdownload;

import android.os.Handler;
import android.os.Looper;

import com.tuacy.library.concurrent.AndroidExecutors;
import com.tuacy.library.concurrent.PausableExecutorService;

public class DownloadManager {

	private final static int DEFAULT_THREADS = 3;

	/**
	 * custom http code invalid
	 */
	public static final int HTTP_INVALID = 1;

	/**
	 * custom http code error size
	 */
	public static final int HTTP_ERROR_SIZE = 1 << 1;

	/**
	 * custom http code error network
	 */
	public static final int HTTP_ERROR_NETWORK = 1 << 2;

	/**
	 * range not satisfiable
	 */
	public static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

	/**
	 * singleton instance
	 */
	private static DownloadManager          mInstance                 = null;
	/**
	 * manager the request we have joined the thread pool(then we can control the request)
	 */
	private        DownloadRequestHelpQueue mDownloadRequestHelpQueue = null;
	/**
	 * thread pool(pause able, priority task)
	 */
	private        PausableExecutorService  mPausableExecutorService  = null;
	/**
	 * download delivery: used to delivery callback to call back in main thread.
	 */
	private        DownloadDelivery         mDownloadDelivery         = null;

	/**
	 * @param nThreads thread pool threads max count
	 */
	private DownloadManager(int nThreads) {
		mDownloadRequestHelpQueue = new DownloadRequestHelpQueue();
		mPausableExecutorService = AndroidExecutors.newFixedPriorityExecutor(nThreads);
		mDownloadDelivery = new DownloadDelivery(new Handler(Looper.getMainLooper()));
	}


	public static DownloadManager getInstance() {
		if (null == mInstance) {
			synchronized (DownloadManager.class) {
				if (null == mInstance) {
					mInstance = new DownloadManager(DEFAULT_THREADS);
				}
			}
		}
		return mInstance;
	}

	public static DownloadManager getInstance(final int nThreads) {
		if (null == mInstance) {
			synchronized (DownloadManager.class) {
				if (null == mInstance) {
					mInstance = new DownloadManager(nThreads);
				}
			}
		}
		return mInstance;
	}


	/**
	 * Add request to queue and ready to download
	 *
	 * @param request request
	 * @return the id of download
	 */
	public int add(DownloadRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("DownloadRequest cannot be null");
		}

		request.cleanCancelOrStopState();
		if (mDownloadRequestHelpQueue.add(request)) {
			/** add to thread pool */
			DownloadPrioritizedRunnable downloadRunnable = new DownloadPrioritizedRunnable(request, mDownloadDelivery);
			mPausableExecutorService.execute(downloadRunnable);
			return request.getDownloadId();
		}
		return -1;
	}

	/**
	 * Cancel the download request by download id will delete the temp file
	 *
	 * @param downloadId the request download id
	 */
	public void cancel(int downloadId) {
		mDownloadRequestHelpQueue.cancel(downloadId);
	}

	/**
	 * Cancel the download request by url and will delete the temp file
	 *
	 * @param url the request download url
	 */
	public void cancel(String url) {
		mDownloadRequestHelpQueue.cancel(url);
	}

	/**
	 * Stop the download request by download id
	 *
	 * @param downloadId the request download id
	 */
	public void stop(int downloadId) {
		mDownloadRequestHelpQueue.stop(downloadId);
	}

	/**
	 * Stop the download request by download url
	 *
	 * @param url the request download url
	 */
	public void stop(String url) {
		mDownloadRequestHelpQueue.stop(url);
	}

	/**
	 * Cancel all the download request task will delete the temp file
	 */
	public void cancelAll() {
		mDownloadRequestHelpQueue.cancelAll();
	}

	/**
	 * Stop all the download request task
	 */
	public void stopAll() {
		mDownloadRequestHelpQueue.stopAll();
	}

	/**
	 * Pause the thread pool (if the thread already start will continue do until end if the thread not start will pause )
	 */
	public void threadPollPause() {
		mPausableExecutorService.pause();
	}

	/**
	 * Resume the pause threads in thread pool
	 */
	public void threadPollResume() {
		mPausableExecutorService.resume();
	}
}
