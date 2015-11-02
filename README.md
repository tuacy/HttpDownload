&#160; &#160; &#160; &#160;通过HTTP下载网络上面的文件，可以设置下载任务的优先级优先级高的先下载，允许那种网络下载，对下载任务做相应的暂停和取消。

##一. 简单的介绍

1). 很大一部分的代码都是来自(https://github.com/Coolerfall/Android-HttpDownloadManager)。
	在这个得基础上做的一些修改，改成用线程池来实现的。

2). 里面类的简单介绍

(1). DownloadManager 单例里面维护了一个可以设定优先级的线程池。

(2). DownloadRequest 下载请求的一个封装，包括url，本地文件路径，progress更新的间隔，那种网络情况下才同意下载，是否暂停取消等一下属性。

(3). DownloadRequestHelpQueue 加这个类是因为我们要想办法控制下载任务的暂停和取消，我们知道一旦把一个下载的任务加入到线程池当中去了之后我们是控制不到线程池中的线程的，所以在DownloadRequestHelpQueue我们维护一个Set<DownloadRequest> mCurrentRequests的Set来记录我们添加的下载DownloadRequest。当我们要把一个download task(DownloadRequest)加入到线程池之前我们会先通过mCurrentRequests判断这个任务先前有没有加过，如果没有加过我们就把这个下载任务加入到线程池同时加入到mCurrentRequests中，这样线程池中的线程会mCurrentRequests会指向同一个DownloadRequest，然后我们控制mCurrentRequests中的DownloadRequest也就控制了线程池中的DownloadRequest这样我们就可以做一些暂停取消之类的操作了。最后一点如果我们的一个下载任务结束了之后也要从mCurrentRequests把相应的任务删掉。

(4). DownloadPrioritizedRunnable 线程池中要加入的Runnable，实现了Runnable和Comparable(给任务设置优先级)。具体的下载和任务的取消都会在这个里面体现出来。

(5). 就是下载过程的监听了，回调实现的，具体可以看下代码哦。


&#160; &#160; &#160; &#160;代码中有一个简单的使用实例。

&#160; &#160; &#160; &#160;最后要说一点就是暂停和继续功能我们是通过另一种方式达到的，因为我们都知道线程我们是没办法暂停的。所以如果你要实现暂停和继续的功能在调用stop之后你要再次把这个下载任务加入进去，他会继续在原来下载过的基础上下载，从而达到了暂停和继续的功能。

&#160; &#160; &#160; &#160;介绍写的非常简单，所有的一切都可以去看下代码的具体实现，代码应该是比较简单的一下子就能看明白。
	
