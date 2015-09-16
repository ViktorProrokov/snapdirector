package com.sungardas.snapdirector.dto;


public class SystemConfiguration {

    private S3 s3;

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public SDFS getSdfs() {
        return sdfs;
    }

    public void setSdfs(SDFS sdfs) {
        this.sdfs = sdfs;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    private SDFS sdfs;
    private Queue queue;


    public Long getLastBackup() {
        return lastBackup;
    }

    public void setLastBackup(Long lastBackup) {
        this.lastBackup = lastBackup;
    }

    private Long lastBackup;





    public static class S3 {
        private String bucketName;


//
//        public String getBucketName() {
//            return bucketName;
//        }
//
        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }
    }

    public static class SDFS {
        private String volumeName;
        private String volumeSize;
        private String mountPoint;

        public String getVolumeName() {
            return volumeName;
        }

        public void setVolumeName(String volumeName) {
            this.volumeName = volumeName;
        }

        public String getVolumeSize() {
            return volumeSize;
        }

        public void setVolumeSize(String volumeSize) {
            this.volumeSize = volumeSize;
        }

        public String getMountPoint() {
            return mountPoint;
        }

        public void setMountPoint(String mountPoint) {
            this.mountPoint = mountPoint;
        }
    }

    public static class Queue {
        private String queueName;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }
    }
}