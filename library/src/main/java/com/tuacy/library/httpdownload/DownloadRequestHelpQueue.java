package com.tuacy.library.httpdownload;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.tuacy.library.httpdownload.DownloadRequest.DownloadState;

/**
 * Created by tuacy on 2015/11/1.
 */
public class DownloadRequestHelpQueue {

	private static final String TAG = DownloadRequestHelpQueue.class.getSimpleName();

	/**
	 * The set of all requests currently being processed by this DownloadQueue. A Request will be in this set if it is waiting in any queue
	 * or currently being processed by any dispatcher.
	 */
	private final Set<DownloadRequest> mCurrentRequests = new HashSet<>();

	/**
	 * Used for generating monotonically-increasing sequence numbers for requests.
	 */
	private AtomicInteger mSequenceGenerator = new AtomicInteger();

	/**
	 * Gets a sequence number.
	 *
	 * @return return the sequence number
	 */
	public int getSequenceNumber() {
		return mSequenceGenerator.incrementAndGet();
	}

	/**
	 * Get the downloading task size.
	 *
	 * @return task size
	 */
	protected int getDownloadingSize() {
		return mCurrentRequests.size();
	}

	/**
	 * To check if the request is downloading according to download id.
	 *
	 * @param downloadId download id
	 * @return true if the request is downloading, otherwise return false
	 */
	protected DownloadState query(int downloadId) {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				if (request.getDownloadId() == downloadId) {
					return request.getDownloadState();
				}
			}
		}

		return DownloadState.INVALID;
	}

	/**
	 * To check if the request is downloading according to download url.
	 *
	 * @param url the url to check
	 * @return true if the request is downloading, otherwise return false
	 */
	protected DownloadState query(String url) {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				if (request.getUrl().equals(url)) {
					return request.getDownloadState();
				}
			}
		}

		return DownloadState.INVALID;
	}

	/**
	 * Add download request to the download request queue.
	 *
	 * @param request download request
	 * @return true if the request is not in queue, otherwise return false
	 */
	protected boolean add(DownloadRequest request) {
		/* check if url is empty */
		if (TextUtils.isEmpty(request.getUrl())) {
			Log.w(TAG, "download url cannot be empty");
			return false;
		}

		/* if download id is not set, generate one */
		if (request.getDownloadId() == -1) {
			int downloadId = getSequenceNumber();
			request.setDownloadId(downloadId);
		}

		/* if the request is downloading, do nothing */
		if (query(request.getDownloadId()) != DownloadState.INVALID || query(request.getUrl()) != DownloadState.INVALID) {
			Log.w(TAG, "the download request is in downloading");
			return false;
		}

		/* tag the request as belonging to this queue */
		request.setDownloadQueue(this);
		/* add it to the set of current requests */
		synchronized (mCurrentRequests) {
			mCurrentRequests.add(request);
		}
		return true;
	}

	/**
	 * Set all request stop
	 */
	public void stopAll() {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				request.stop();
			}
		}
	}

	/**
	 * Set all request cancel
	 */
	public void cancelAll() {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				request.cancel();
			}
		}
	}

	/**
	 * Set request stop by request id
	 *
	 * @param downloadId request download id
	 */
	public void stop(int downloadId) {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				if (request.getDownloadId() == downloadId) {
					request.stop();
				}
			}
		}
	}

	/**
	 * Set request stop by request url
	 *
	 * @param url request url
	 */
	public void stop(String url) {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				if (request.getUrl().equals(url)) {
					request.stop();
				}
			}
		}
	}

	/**
	 * Set request cancel by request download id
	 *
	 * @param downloadId request download id
	 */
	public void cancel(int downloadId) {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				if (request.getDownloadId() == downloadId) {
					request.cancel();
				}
			}
		}
	}

	/**
	 * Set request cancel by request url
	 *
	 * @param url request url
	 */
	public void cancel(String url) {
		synchronized (mCurrentRequests) {
			for (DownloadRequest request : mCurrentRequests) {
				if (request.getUrl().equals(url)) {
					request.cancel();
				}
			}
		}
	}

	/**
	 * Download task finish(may be fault, cancel, stop, success......)
	 *
	 * @param request request
	 */
	public void finishDownloadRequest(DownloadRequest request) {
		synchronized (mCurrentRequests) {
			mCurrentRequests.remove(request);
		}
	}


}
