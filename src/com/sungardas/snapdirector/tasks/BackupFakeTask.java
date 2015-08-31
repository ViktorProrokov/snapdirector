package com.sungardas.snapdirector.tasks;

import com.amazonaws.auth.AWSCredentials;
import com.sungardas.snapdirector.aws.dynamodb.model.BackupEntry;
import com.sungardas.snapdirector.aws.dynamodb.model.BackupState;
import com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry;
import com.sungardas.snapdirector.aws.dynamodb.repository.BackupRepository;
import com.sungardas.snapdirector.aws.dynamodb.repository.TaskRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry.TaskEntryStatus.RUNNING;

@Component
@Scope("prototype")
@Profile("dev")
public class BackupFakeTask implements BackupTask {
	private static final Logger LOG = LogManager.getLogger(BackupFakeTask.class);
    
    @Autowired
	private TaskRepository taskRepository;
    @Autowired
	private BackupRepository backupRepository;
    
    @Autowired
    private AWSCredentials amazonAWSCredentials;
    
    private TaskEntry taskEntry;

    
    public void setTaskEntry(TaskEntry taskEntry) {
    	this.taskEntry= taskEntry;
    }

    @Override
    public void execute() {
        LOG.info("Task " + taskEntry.getId() + ": Change task state to 'inprogress'");
        taskEntry.setStatus(RUNNING.getStatus());
        taskRepository.save(taskEntry);

        LOG.info(taskEntry.toString());
        String timestamp = Long.toString(System.currentTimeMillis());
        String volumeId = taskEntry.getVolume();
        String filename = volumeId + "." + timestamp + ".backup";
        BackupEntry backup = new BackupEntry(taskEntry.getVolume(), filename, timestamp, "123456789", BackupState.COMPLETED, taskEntry.getInstanceId(),
        		"snap-00100110","gp2","3000", "10");
        LOG.info("Task " + taskEntry.getId() + ":put backup info'");
        backupRepository.save(backup);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
        
        LOG.info("Task " + taskEntry.getId() + ": Delete completed task:" + taskEntry.getId());
        taskRepository.delete(taskEntry);
        LOG.info("Task completed.");
    }

}
