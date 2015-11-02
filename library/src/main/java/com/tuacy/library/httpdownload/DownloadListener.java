package com.tuacy.library.httpdownload;

/**
 * Interface definition for a callback to be invoked when downloading. This download listener contains detail download information. If
 * simple listener is needed, then use {@link SimpleDownloadListener}
 */
public interface DownloadListener {

	/**
	 * Invoked when downloading is started.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   download file path
	 * @param totalBytes total bytes of the file
	 */
	void onStart(int downloadId, String url, String filePath, long totalBytes);

	/**
	 * Invoked when download retrying.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   download file path
	 */
	void onRetry(int downloadId, String url, String filePath);

	/**
	 * Invoked when downloading is in progress.
	 *
	 * @param downloadId   download id in download request queue
	 * @param url          download url
	 * @param filePath     download file path
	 * @param bytesWritten the bytes has written to local disk
	 * @param totalBytes   total bytes of the file
	 */
	void onProgress(int downloadId, String url, String filePath, long bytesWritten, long totalBytes);

	/**
	 * Invoked when downloading successfully.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   download file path
	 */
	void onSuccess(int downloadId, String url, String filePath);

	/**
	 * Invoked when downloading failed.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   file path
	 * @param statusCode error code
	 * @param errMsg     error message
	 */
	void onFailure(int downloadId, String url, String filePath, int statusCode, String errMsg);

	/**
	 * Invoked when downloading is cancel.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   download file path
	 */
	void onCancel(int downloadId, String url, String filePath);

	/**
	 * Invoked when downloading is stop.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   download file path
	 */
	void onStop(int downloadId, String url, String filePath);
}
