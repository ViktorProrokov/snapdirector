package com.sungardas.snapdirector.aws.dynamodb.repository;

import com.sungardas.snapdirector.aws.dynamodb.model.SnapshotEntry;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface SnapshotRepository extends CrudRepository<SnapshotEntry, String> {

    List<SnapshotEntry> findByInstanceId(String instanceId);

}
