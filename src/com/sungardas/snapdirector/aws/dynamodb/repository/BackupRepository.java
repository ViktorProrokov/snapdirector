package com.sungardas.snapdirector.aws.dynamodb.repository;

import com.sungardas.snapdirector.aws.dynamodb.model.BackupEntry;

import java.util.List;

public interface BackupRepository {

    void save(BackupEntry backup);

    void delete(BackupEntry backupEntry);

    List<BackupEntry> get(String volumeId, String instanceId);

    BackupEntry getLast(String volumeId, String instanceId);

    BackupEntry getByBackupFileName(String backupName);

    List<BackupEntry> findAll(String instanceId);

    int count();
}
