package javax.arang.aws;

import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class RenameKeys {

	public static void main(String[] args) throws IOException {
		
		 /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion("us-east-1")
            .build();

        String bucketName = "genomeark";
        
        /*
         * List all objects under s3://genomeark/species
         * and add the datatype.
         */
        
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("species/Gopherus_evgoodei/rGopEvg1/assembly_v1/falcon_unzip");
        ObjectListing objectListing = s3.listObjects(listObjectsRequest);
        Double countKeys = 0d;
        
        
        while (true) {
        	for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        		//System.err.println(objectSummary.getKey());
        		countKeys++;
        		System.out.println(objectSummary.getKey());
        		if (!objectSummary.getKey().contains("intermediates")) {
        			s3.copyObject(bucketName, objectSummary.getKey(), bucketName, objectSummary.getKey().replace("falcon_unzip", "intermediates/falcon_unzip"));
        		}
        	}
        	if (objectListing.isTruncated()) {
        		objectListing = s3.listNextBatchOfObjects(objectListing);
        	} else {
        		break;
        	}
        }
        System.err.println("Total num. objects under s3://genomeark/species: " + String.format("%,.0f", countKeys));

	}

}
