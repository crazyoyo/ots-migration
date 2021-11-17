package com.amazonaws.otsmgr;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chochen on 2021/5/31.
 */
public class S3Util {

    private static final Logger log = LoggerFactory.getLogger(OtsMgrApplication.class);

    public static AmazonS3 getS3Client(String ak, String sk) {

        AmazonS3 client = null;
        try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(ak, sk);
            client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.CN_NORTHWEST_1).build();
        } catch (IllegalArgumentException e) {
            log.error("error create S3 client. ", e);
        }
        return client;
    }
}
