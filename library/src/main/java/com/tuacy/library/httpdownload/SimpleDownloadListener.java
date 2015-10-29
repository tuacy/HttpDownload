package com.tuacy.library.httpdownload;

/**
 * Interface definition for a callback to be invoked when downloading. This is a simple download listener, it's only contains two method, so
 * if detail download information is needed, then use {@link DownloadListener}
 */
public interface SimpleDownloadListener {

	/**
	 * Invoked when downloading successfully.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param filePath   file path
	 */
	void onSuccess(int downloadId, String url, String filePath);

	/**
	 * Invoked when downloading failed.
	 *
	 * @param downloadId download id in download request queue
	 * @param url        download url
	 * @param statusCode status code
	 * @param errMsg     error message
	 */
	void onFailure(int downloadId, String url, int statusCode, String errMsg);
}
