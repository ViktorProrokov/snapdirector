package com.sungardas.snapdirector.tasks;

import com.sungardas.snapdirector.aws.dynamodb.model.BackupEntry;
import com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry;
import com.sungardas.snapdirector.aws.dynamodb.repository.BackupRepository;
import com.sungardas.snapdirector.aws.dynamodb.repository.TaskRepository;
import com.sungardas.snapdirector.exception.DataAccessException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry.TaskEntryStatus.*;

@Component
@Scope("prototype")
@Profile("dev")
public class DeleteFakeTask implements DeleteTask {

    private static final Logger LOG = LogManager.getLogger(DeleteFakeTask.class);

    private TaskEntry taskEntry;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BackupRepository backupRepository;

    @Override
    public void setTaskEntry(TaskEntry taskEntry) {
        this.taskEntry = taskEntry;
    }

    @Override
    public void execute() {
        LOG.info("Task " + taskEntry.getId() + ": Change task state to 'running'");
        taskEntry.setStatus(RUNNING.getStatus());
        taskRepository.save(taskEntry);

        BackupEntry backupEntry = new BackupEntry();
        backupEntry.setVolumeId(taskEntry.getVolume());
        backupEntry.setFileName(taskEntry.getOptions());

        try {
            backupRepository.delete(backupEntry);
            taskEntry.setStatus(COMPLETE.getStatus());
            taskRepository.save(taskEntry);

            //TODO check delete logic
            taskRepository.delete(taskEntry);
            LOG.info("Task " + taskEntry.getId() + ": Change task state to 'complete'");
        } catch (DataAccessException e){
            LOG.error(e);
            taskEntry.setStatus(ERROR.getStatus());
            taskRepository.save(taskEntry);
        }
    }
}
