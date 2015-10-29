package com.tuacy.library.httpdownload;

public class DownloadManager {

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

	private static DownloadManager mInstance = null;

	private DownloadDispatcher mDownloadDispatcher = null;

	/**
	 * @param threadPoolThreadMax thread pool threads max count
	 */
	private DownloadManager(int threadPoolThreadMax) {
		this(threadPoolThreadMax, DownloadRequestQueue.CAPACITY);
	}

	/**
	 * @param threadPoolThreadMax thread pool threads max count
	 * @param queueMaxCount       queue max count
	 */
	private DownloadManager(int threadPoolThreadMax, int queueMaxCount) {
		mDownloadDispatcher = new DownloadDispatcher(threadPoolThreadMax, queueMaxCount);
	}

	public static DownloadManager getInstance() {
		if (null == mInstance) {
			synchronized (DownloadManager.class) {
				if (null == mInstance) {
					mInstance = new DownloadManager(DownloadRequestQueue.DEFAULT_THREAD_POOL_THREAD_COUNT);
				}
			}
		}
		return mInstance;
	}

	public static DownloadManager getInstance(final int threadPoolThreadMax) {
		if (null == mInstance) {
			synchronized (DownloadManager.class) {
				if (null == mInstance) {
					mInstance = new DownloadManager(threadPoolThreadMax);
				}
			}
		}
		return mInstance;
	}

	public static DownloadManager getInstance(final int threadPoolThreadMax, final int queueMaxCount) {
		if (null == mInstance) {
			synchronized (DownloadManager.class) {
				if (null == mInstance) {
					mInstance = new DownloadManager(threadPoolThreadMax, queueMaxCount);
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
		return mDownloadDispatcher.add(request);
	}

	public void cancel(int downloadId) {
		mDownloadDispatcher.cancel(downloadId);
	}

	public void cancel(String url) {
		mDownloadDispatcher.cancel(url);
	}

	public void stop(int downloadId) {
		mDownloadDispatcher.stop(downloadId);
	}

	public void stop(String url) {
		mDownloadDispatcher.stop(url);
	}

	public void cancelAll() {
		mDownloadDispatcher.cancelAll();
	}

	public void stopAll() {
		mDownloadDispatcher.stopAll();
	}

}
