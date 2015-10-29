package com.tuacy.library.httpdownload;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadRequestQueue {

	private static final String TAG = DownloadRequestQueue.class.getSimpleName();

	public static final int DEFAULT_THREAD_POOL_THREAD_COUNT = 3;

	/**
	 * The default capacity of download request queue.
	 */
	public static final int CAPACITY = 20;

	/**
	 * The queue of download request.
	 */
	private PriorityBlockingQueue<DownloadRequest> mDownloadQueue = null;

	/**
	 * The set of all waiting request
	 */
	private final Set<DownloadRequest> mWaitingRequests = new HashSet<>();

	/**
	 * The set of all downloading request
	 */
	private final Set<DownloadRequest> mDoingRequests = new HashSet<>();

	/**
	 * Used for generating monotonically-increasing sequence numbers for requests.
	 */
	private AtomicInteger mSequenceGenerator = new AtomicInteger();

	/**
	 * Control thread pool thread count
	 */

	private DownloadDispatcher mDownloadDispatcher = null;

	/**
	 * @param dispatcher DownloadDispatcher
	 */
	public DownloadRequestQueue(DownloadDispatcher dispatcher) {
		this(dispatcher, CAPACITY);
	}

	public DownloadRequestQueue(DownloadDispatcher dispatcher, int queueMaxCount) {
		mDownloadDispatcher = dispatcher;
		mDownloadQueue = new PriorityBlockingQueue<>(queueMaxCount);
	}

	/**
	 * Add to queue
	 *
	 * @param request add request
	 */
	public boolean enqueue(DownloadRequest request) {
		/** check if url is empty */
		if (TextUtils.isEmpty(request.getUrl())) {
			Log.w(TAG, "download url cannot be empty");
			return false;
		}

		/** if the request is downloading, do nothing */
		if (isDownloading(request.getDownloadId()) || isDownloading(request.getUrl())) {
			Log.w(TAG, "the download request is in downloading");
			return false;
		}
		/** tag the request as belonging to this queue */
		request.setDownloadQueue(this);
		mDownloadQueue.add(request);
		synchronized (mWaitingRequests) {
			mWaitingRequests.add(request);
		}
		return true;
	}

	/**
	 * Out form the queue
	 *
	 * @return the request
	 */
	public DownloadRequest dequeue() {
		try {
			DownloadRequest request = mDownloadQueue.take();
			synchronized (mWaitingRequests) {
				mWaitingRequests.remove(request);
			}
			return request;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets a sequence number.
	 *
	 * @return return the sequence number
	 */
	public int getSequenceNumber() {
		return mSequenceGenerator.incrementAndGet();
	}

	/**
	 * In order to guarantee the maximum number of threads in thread pool in this function we use Semaphore
	 *
	 * @param request add request
	 */

	protected void startDownloadRequest(DownloadRequest request) {
		synchronized (mDoingRequests) {
			mDoingRequests.add(request);
		}
	}

	/**
	 * Remember to release the semaphore
	 *
	 * @param request finish request
	 */
	protected void finishDownloadRequest(DownloadRequest request) {
		synchronized (mDoingRequests) {
			mDoingRequests.remove(request);
			mDownloadDispatcher.releaseThreadPoolSemaphore();
		}
	}

	/**
	 * To check if the request is downloading according to download id.
	 *
	 * @param downloadId download id
	 * @return true if the request is downloading, otherwise return false
	 */
	protected DownloadRequest.DownloadState queryDownloadState(int downloadId) {
		synchronized (mDoingRequests) {
			/** is in downloading list */
			for (DownloadRequest request : mDoingRequests) {
				if (request.getDownloadId() == downloadId) {
					return request.getDownloadState();
				}
			}
		}

		synchronized (mWaitingRequests) {
			/** is in waiting */
			for (DownloadRequest request : mWaitingRequests) {
				if (request.getDownloadId() == downloadId) {
					return request.getDownloadState();
				}
			}
		}

		return DownloadRequest.DownloadState.INVALID;
	}

	/**
	 * To check if the request is downloading according to download id.
	 *
	 * @param downloadId download id
	 * @return DownloadRequest
	 */
	protected DownloadRequest queryDownloadRequest(int downloadId) {
		synchronized (mDoingRequests) {
			/** is in downloading list */
			for (DownloadRequest request : mDoingRequests) {
				if (request.getDownloadId() == downloadId) {
					return request;
				}
			}

		}
		synchronized (mWaitingRequests) {
			/** is in waiting */
			for (DownloadRequest request : mWaitingRequests) {
				if (request.getDownloadId() == downloadId) {
					return request;
				}
			}
		}

		return null;
	}

	/**
	 * To check if the request is downloading according to download url.
	 *
	 * @param url the url to check
	 * @return true if the request is downloading, otherwise return false
	 */
	protected DownloadRequest.DownloadState queryDownloadState(String url) {
		synchronized (mDoingRequests) {
			for (DownloadRequest request : mDoingRequests) {
				if (request.getUrl().equals(url)) {
					return request.getDownloadState();
				}
			}
		}

		synchronized (mWaitingRequests) {
			/** is in waiting */
			for (DownloadRequest request : mWaitingRequests) {
				if (request.getUrl().equals(url)) {
					return request.getDownloadState();
				}
			}
		}

		return DownloadRequest.DownloadState.INVALID;
	}

	/**
	 * To check if the request is downloading according to download id.
	 *
	 * @param url url
	 * @return DownloadRequest
	 */
	protected DownloadRequest queryDownloadRequest(String url) {
		synchronized (mDoingRequests) {
			/** is in downloading list */
			for (DownloadRequest request : mDoingRequests) {
				if (request.getUrl().equals(url)) {
					return request;
				}
			}

		}
		synchronized (mWaitingRequests) {
			/** is in waiting */
			for (DownloadRequest request : mWaitingRequests) {
				if (request.getUrl().equals(url)) {
					return request;
				}
			}
		}

		return null;
	}

	/**
	 * To check if the request is downloading according to download downloadId.
	 *
	 * @param downloadId the downloadId to check
	 * @return true if the request is downloading, otherwise return false
	 */
	protected boolean isDownloading(int downloadId) {
		DownloadRequest.DownloadState state = queryDownloadState(downloadId);
		return (state == DownloadRequest.DownloadState.PENDING || state == DownloadRequest.DownloadState.RUNNING);
	}

	/**
	 * To check if the request is downloading according to download url.
	 *
	 * @param url the url to check
	 * @return true if the request is downloading, otherwise return false
	 */
	protected boolean isDownloading(String url) {
		DownloadRequest.DownloadState state = queryDownloadState(url);
		return (state == DownloadRequest.DownloadState.PENDING || state == DownloadRequest.DownloadState.RUNNING);
	}

	/**
	 * To check if the request is downloading according to download state.
	 *
	 * @param state the state to check
	 * @return true if the request is downloading, otherwise return false
	 */
	protected boolean isDownloading(DownloadRequest.DownloadState state) {

		return (state == DownloadRequest.DownloadState.PENDING || state == DownloadRequest.DownloadState.RUNNING);
	}

	protected void setCancel(int downloadId) {
		DownloadRequest request = queryDownloadRequest(downloadId);
		if (null != request) {
			request.cancel();
		}
	}

	protected void setCancel(String url) {
		DownloadRequest request = queryDownloadRequest(url);
		if (null != request) {
			request.cancel();
		}
	}

	protected void setStop(int downloadId) {
		DownloadRequest request = queryDownloadRequest(downloadId);
		if (null != request) {
			request.stop();
		}
	}

	protected void setStop(String url) {
		DownloadRequest request = queryDownloadRequest(url);
		if (null != request) {
			request.stop();
		}
	}

	protected void stopAll() {
		synchronized (mDoingRequests) {
			/** is in downloading list */
			for (DownloadRequest request : mDoingRequests) {
				request.stop();
			}

		}
		synchronized (mWaitingRequests) {
			/** is in waiting */
			for (DownloadRequest request : mWaitingRequests) {
				request.stop();
			}
		}
	}

	protected void cancelAll() {
		synchronized (mDoingRequests) {
			/** is in downloading list */
			for (DownloadRequest request : mDoingRequests) {
				request.cancel();
			}

		}
		synchronized (mWaitingRequests) {
			/** is in waiting */
			for (DownloadRequest request : mWaitingRequests) {
				request.cancel();
			}
		}
	}

}
