package com.amazonaws.otsmgr;

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncUploadTasks {

    private static final Logger log = LoggerFactory.getLogger(AsyncUploadTasks.class);

    @Async
    public void toS3() throws InterruptedException, ExecutionException {

    }
}
