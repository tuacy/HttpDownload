package com.tuacy.library.httpdownload;

import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.tuacy.library.httpdownload.DownloadManager.HTTP_ERROR_NETWORK;
import static com.tuacy.library.httpdownload.DownloadManager.HTTP_ERROR_SIZE;
import static com.tuacy.library.httpdownload.DownloadManager.HTTP_INVALID;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class DownloadRunnable implements Runnable {

	private static final String TAG = DownloadDispatcher.class.getSimpleName();

	/**
	 * Sleep time before download.
	 */
	private static final int SLEEP_BEFORE_DOWNLOAD = 1500;

	/**
	 * Sleep time before retrying download.
	 */
	private static final int SLEEP_BEFORE_RETRY = 3500;

	/**
	 * Http connection time out.
	 */
	private static final int DEFAUL_TIME_OUT = 20 * 1000;

	/**
	 * Buffer size used in data transfer.
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Maximum of redirection, should not more than 7.
	 */
	private static final int MAX_REDIRECTION = 5;

	/**
	 * Range not satisfiable.
	 */
	private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

	/**
	 * Http temp redirect.
	 */
	private static final int HTTP_TEMP_REDIRECT = 307;

	/**
	 * Accept encoding in request header.
	 */
	private static final String ACCEPT_ENCODING = "Accept-Encoding";

	/**
	 * Transfer encoding in header.
	 */
	private static final String TRANSFER_ENCODING = "Transfer-Encoding";

	/**
	 * Content length in header.
	 */
	private static final String CONTENT_LENGTH = "Content-Length";

	/**
	 * Redirect location.
	 */
	private static final String LOCATION = "Location";

	/**
	 * End of input stream.
	 */
	private static final String END_OF_STREAM = "unexpected end of stream";

	/**
	 * Default thread name.
	 */
	private static final String DEFAULT_THREAD_NAME = "DownloadDispatcher";

	/**
	 * Idle thread name.
	 */
	private static final String IDLE_THREAD_NAME = "DownloadDispatcher-Idle";

	/**
	 * Redirection time happens in this request.
	 */
	private int mRedirectionCount = 0;

	/**
	 * Save total bytes in case.
	 */
	private long mTotalBytes = 0;

	/**
	 * Used to save last progress timestamp.
	 */
	private long mLastProgressTimestamp;

	/**
	 * Used to tell us this dispatcher has dead.
	 */
	private volatile boolean mQuit = false;

	private DownloadDelivery mDelivery = null;
	private DownloadRequest  mRequest  = null;

	public DownloadRunnable(DownloadRequest request, DownloadDelivery downloadDelivery) {
		mRequest = request;
		mDelivery = downloadDelivery;
	}

	@Override
	public void run() {

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		Thread.currentThread().setName(DEFAULT_THREAD_NAME);

		mTotalBytes = 0;
		mRedirectionCount = 0;

		/** start download */
		executeDownload(mRequest);
	}

	/* update download state */
	private void updateState(DownloadRequest request, DownloadRequest.DownloadState state) {
		request.setDownloadState(state);
	}

	/* update download start state */
	private void updateStart(DownloadRequest request, long totalBytes) {
		/* if the request has failed before, donnot deliver callback */
		if (request.getDownloadState() == DownloadRequest.DownloadState.FAILURE) {
			updateState(request, DownloadRequest.DownloadState.RUNNING);
			return;
		}

		/* set the download state of this request as running */
		updateState(request, DownloadRequest.DownloadState.RUNNING);
		mDelivery.postStart(request, totalBytes);
	}

	/* update download retrying */
	private void updateRetry(DownloadRequest request) {
		mDelivery.postRetry(request);
	}

	/* update download progress */
	private void updateProgress(DownloadRequest request, long bytesWritten, long totalBytes) {
		long currentTimestamp = System.currentTimeMillis();
		if (bytesWritten != totalBytes && currentTimestamp - mLastProgressTimestamp < request.getProgressInterval()) {
			return;
		}

		/* save progress timestamp */
		mLastProgressTimestamp = currentTimestamp;

		if (!request.isCanceled()) {
			mDelivery.postProgress(request, bytesWritten, totalBytes);
		}
	}

	/* update download success */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void updateSuccess(DownloadRequest request) {
		updateState(request, DownloadRequest.DownloadState.SUCCESSFUL);

		/* notify the request download finish */
		request.finish();

		File file = new File(request.getTmpDestinationPath());
		if (file.exists()) {
			file.renameTo(new File(request.getDestFilePath()));
		}

		/* deliver success message */
		mDelivery.postSuccess(request);
	}

	/* update download failure */
	private void updateFailure(DownloadRequest request, int statusCode, String errMsg) {
		updateState(request, DownloadRequest.DownloadState.FAILURE);

		/* if the status code is 0, may be cause by the net error */
		if ((statusCode == HTTP_INVALID || statusCode == HTTP_ERROR_SIZE) && request.getRetryTime() >= 0) {
			try {
				/* update progress in case */
				long bytesWritten = new File(request.getTmpDestinationPath()).length();
				updateProgress(request, bytesWritten, mTotalBytes);

				/* sleep a while before retrying */
				Thread.sleep(SLEEP_BEFORE_RETRY);
			} catch (InterruptedException e) {
				/* we may have been interrupted because it was time to quit */
				if (mQuit) {
					request.finish();
					return;
				}
			}

			/* retry downloading */
			if (!request.isCanceled()) {
				updateRetry(request);
				executeDownload(request);
			}

			return;
		}

		/* notify the request that downloading has finished */
		request.finish();

		/* deliver failure message */
		mDelivery.postFailure(request, statusCode, errMsg);
	}

	/* update download cancel */
	private void updateCancel(DownloadRequest request) {

		/* notify the request that downloading has finished */
		request.finish();
		File file = new File(request.getTmpDestinationPath());
		if (file.exists()) {
			file.delete();
		}
		/* deliver cancel message */
		mDelivery.postCancel(request);
	}

	/* update download success */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void updateStop(DownloadRequest request) {
		/* notify the request that downloading has finished */
		request.finish();
		/* deliver stop message */
		mDelivery.postStop(request);
	}

	/* read response header from server */
	private long getContentLength(HttpURLConnection conn) {
		String transferEncoding = conn.getHeaderField(TRANSFER_ENCODING);
		if (transferEncoding == null || transferEncoding.equalsIgnoreCase("chunked")) {
			return conn.getHeaderFieldInt(CONTENT_LENGTH, -1);
		} else {
			return -1;
		}
	}

	/* transfer data from server to local file */
	private void transferData(HttpURLConnection conn, DownloadRequest request) throws IOException {
		long contentLength = getContentLength(conn);
		if (contentLength == -1) {
			return;
		}

		InputStream is = null;
		File file = new File(request.getTmpDestinationPath());
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		long bytesWritten = file.length();
		contentLength += bytesWritten;
		/* if the file has existed, seek to breakpoint */
		if (bytesWritten > 0) {
			raf.seek(bytesWritten);
		}

		mTotalBytes = contentLength;
		/* deliver start callback */
		updateStart(request, contentLength);

		try {
			/* get input stream from connection */
			is = conn.getInputStream();
			if (is != null) {
				byte[] buffer = new byte[BUFFER_SIZE];
				int length;

				while (true) {
					/** if the request has interrupted */
					if (Thread.currentThread().isInterrupted()) {
						Log.i(TAG, "thread interrupted, download id: " + request.getDownloadId());
						updateStop(request);
						return;
					}

					/** if the request has cancel */
					if (request.isCanceled()) {
						Log.i(TAG, "download has canceled, download id: " + request.getDownloadId());
						updateCancel(request);
						return;
					}
					/** if the request has stop */
					if (request.isStop()) {
						Log.i(TAG, "download has stop, download id: " + request.getDownloadId());
						updateStop(request);
						return;
					}

					/* if current is not wifi and mobile network is not allowed, stop */
					if (request.getAllowedNetworkTypes() != 0 &&
						!DownloadUtils.isWifi(request.getContext()) &&
						(request.getAllowedNetworkTypes() & DownloadRequest.NETWORK_MOBILE) == 0) {
						updateFailure(request, HTTP_ERROR_NETWORK, "network error");
						return;
					}

					/* read data into buffer from input stream */
					length = readFromInputStream(buffer, is);
					if (length == -1) {
						long fileSize = new File(request.getTmpDestinationPath()).length();

						/* deliver progress callback before deliver success */
						updateProgress(request, fileSize, contentLength);

						/* if file size equals total bytes, then download successfully */
						if (fileSize == contentLength) {
							updateSuccess(request);
						} else {
							updateFailure(request, HTTP_INVALID, "file size error");
						}

						return;
					} else if (length == Integer.MIN_VALUE) {
						updateFailure(request, HTTP_ERROR_SIZE, "transfer data error");
						return;
					}

					bytesWritten += length;
					/* write buffer into local file */
					raf.write(buffer, 0, length);

					/* deliver progress callback */
					updateProgress(request, bytesWritten, contentLength);
				}
			}
		} finally {
			raf.close();
			silentCloseInputStream(is);
		}
	}

	/* read data from input stream */
	private int readFromInputStream(byte[] buffer, InputStream is) {
		try {
			return is.read(buffer);
		} catch (IOException e) {
			if (END_OF_STREAM.equals(e.getMessage())) {
				return -1;
			}

			return Integer.MIN_VALUE;
		}
	}

	/* a utility function to close an input stream without raising an exception */
	private static void silentCloseInputStream(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (IOException e) {
			Log.w(TAG, "cannot close input stream", e);
		}
	}

	private void executeDownload(DownloadRequest request) {
		if (Thread.currentThread().isInterrupted()) {
			return;
		}

		//TODO: handle https url
		/* use http url connection to download file */
		HttpURLConnection conn = null;

		try {
			URL url = new URL(request.getUrl());
			conn = (HttpURLConnection) url.openConnection();

			/* config http url connection */
			conn.setInstanceFollowRedirects(false);
			conn.setUseCaches(false);
			conn.setRequestProperty(ACCEPT_ENCODING, "identity");
			conn.setConnectTimeout(DEFAUL_TIME_OUT);
			conn.setReadTimeout(DEFAUL_TIME_OUT);

			/* if the file existed, restart from breakpoint */
			File file = new File(request.getTmpDestinationPath());
			if (file.exists()) {
				long breakpoint = file.length();
				/* set the range to continue the downloading */
				conn.setRequestProperty("Range", "bytes=" + breakpoint + "-");
			}

			/* status code */
			int statusCode = conn.getResponseCode();
			switch (statusCode) {
				case HTTP_OK:
				case HTTP_PARTIAL:
					transferData(conn, request);
					break;

				case HTTP_MOVED_PERM:
				case HTTP_MOVED_TEMP:
				case HTTP_TEMP_REDIRECT:
				case HTTP_SEE_OTHER:
					if (mRedirectionCount++ < MAX_REDIRECTION) {
						Log.i(TAG, "redirect for download id: " + request.getDownloadId());
						/* take redirect url and call executeDownload recursively */
						String redirectUrl = conn.getHeaderField(LOCATION);
						request.setUrl(redirectUrl);
						executeDownload(request);
					} else {
						/* redirect to many times */
						updateFailure(request, statusCode, "redirect too many times");
					}
					break;

				case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
				case HTTP_UNAVAILABLE:
				case HTTP_INTERNAL_ERROR:
				default:
					updateFailure(request, statusCode, conn.getResponseMessage());
					break;
			}
		} catch (IOException e) {
			updateFailure(request, HTTP_INVALID, e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
