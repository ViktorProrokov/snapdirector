package com.sungardas.snapdirector.components;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.sungardas.snapdirector.aws.dynamodb.model.TaskEntry;
import com.sungardas.snapdirector.aws.dynamodb.model.WorkerConfiguration;
import com.sungardas.snapdirector.aws.dynamodb.repository.TaskRepository;
import com.sungardas.snapdirector.aws.dynamodb.repository.WorkerConfigurationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Component
public class TasksDispatcher {
	
	@Autowired
	private WorkerConfigurationRepository confRepository;
	@Value("${sungardas.worker.configuration}")
	private String configurationId;
	@Autowired
	private AmazonSQS sqs;
	@Autowired
	private TaskRepository taskRepository;
	
	WorkerConfiguration configuration;	
	private ExecutorService  executor;
	
	@PostConstruct
	private void init() {
		configuration = confRepository.findOne(configurationId);
		
		executor = Executors.newSingleThreadExecutor();
		executor.execute(new TasksSender());
	}
	
	@PreDestroy
	public void destroy() {
		executor.shutdownNow();
	}

	
	
	private class TasksSender implements Runnable {
		
		private final Logger LOGts = LogManager.getLogger(TasksSender.class);
		
		public void run() {
			String queueURL = configuration.getTaskQueueURL();
			String instanceId = configuration.getConfigurationId();

			LOGts.info(format("Starting recieving to tasks queue: %s", queueURL));
			
			
			HashMap<String , MessageAttributeValue> messageAttributes = new HashMap<String, MessageAttributeValue>();
			messageAttributes.put("listener-"+instanceId, new MessageAttributeValue().withDataType("String").withStringValue(instanceId));
			
			
			while (true) {
				//LOGts.info("\n\nLook for waiting tasks..");
				List<TaskEntry> taskModels = taskRepository.findByStatusAndInstanceId(TaskEntry.TaskEntryStatus.WAITING.getStatus(), instanceId);
				for (TaskEntry entry : taskModels) {
					SendMessageRequest sendRequest = new SendMessageRequest(queueURL, entry.toString());
					sendRequest.setDelaySeconds(0);
					sqs.sendMessage(sendRequest);
					entry.setStatus(TaskEntry.TaskEntryStatus.QUEUED.getStatus());
					LOGts.info("QUEUED message: \n" + entry.toString());
				}
				taskRepository.save(taskModels);
				sleep();
			}
		}
		
		private void sleep() {
			try {
				TimeUnit.SECONDS.sleep(20);
			} catch (InterruptedException e) {	e.printStackTrace(); }
		}
	}
}
