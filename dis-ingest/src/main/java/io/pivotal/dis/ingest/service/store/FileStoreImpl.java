package io.pivotal.dis.ingest.service.store;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.IOUtils;

public class FileStoreImpl implements FileStore {

    private final AmazonS3 amazonS3;
    private final String bucketName;
    private final AccessControlList disruptionsAcl;

    public FileStoreImpl(AmazonS3 amazonS3, String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        disruptionsAcl = new AccessControlList();
        disruptionsAcl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
    }

    @Override
    public void save(String name, String input) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, name, IOUtils.toInputStream(input), null).withAccessControlList(disruptionsAcl);
        amazonS3.putObject(putObjectRequest);
    }

}
