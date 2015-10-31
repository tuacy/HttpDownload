package com.tuacy.library.concurrent;

import java.util.concurrent.ExecutorService;

public interface PausableExecutorService extends ExecutorService {

	public void pause();

	public void resume();

	public boolean isPause();
	
}
