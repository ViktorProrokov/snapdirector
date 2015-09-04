package com.sungardas.init;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;

import com.sungardas.snapdirector.aws.dynamodb.model.User;
import com.sungardas.snapdirector.aws.dynamodb.repository.UserRepository;
import com.sungardas.snapdirector.dto.InitConfigurationDto;
import com.sungardas.snapdirector.exception.ConfigurationException;
import com.sungardas.snapdirector.exception.SnapdirectorException;
import com.sungardas.snapdirector.rest.RestAuthenticationFilter;
import com.sungardas.snapdirector.rest.filters.FilterProxy;

import com.sungardas.snapdirector.service.SharedDataService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.XmlWebApplicationContext;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;


@RestController
class InitController implements ApplicationContextAware {

    private static final Logger LOG = LogManager.getLogger(InitController.class);

    @Autowired
    private FilterProxy filterProxy;

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private SharedDataService sharedDataService;

    @Autowired
    private XmlWebApplicationContext applicationContext;

    private boolean CONTEXT_REFRESH_IN_PROCESS = false;

    @PostConstruct
    private void init() {
        // check that aws credentials are provided
        // try to authenticate as real admin user
        if (credentialsService.isAwsPropertyFileExists()) {
            LOG.info("Valid aws credentials were provided.");
            refreshContext();
        }
    }

    @ExceptionHandler(SnapdirectorException.class)
    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    private Exception internalServerError(SnapdirectorException exception) {
        LOG.error(exception);
        return exception;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/session")
    public ResponseEntity<String> init(@RequestBody User user) {
        if (CONTEXT_REFRESH_IN_PROCESS) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        // no aws credentials are provided
        // try to authenticate as default user admin@snapdirector:<instance-id>
        else if (credentialsService.checkDefaultUser(user.getEmail(), user.getPassword())) {
            return new ResponseEntity<>("{ \"role\":\"configurator\" }", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/configuration/awscreds", method = RequestMethod.POST)
    public ResponseEntity<String> setAwsCredential(@RequestBody CredentialsDto credentials) {
        credentialsService.setCredentialsIfValid(credentials);
        LOG.info("provided aws keys");
        return new ResponseEntity<>(OK);
    }


    @RequestMapping(value = "/configuration/current", method = RequestMethod.GET)
    public ResponseEntity<InitConfigurationDto> getConfiguration() {
        return new ResponseEntity<>(credentialsService.getInitConfigurationDto(), HttpStatus.OK);
    }


    @RequestMapping(value = "/configuration/current", method = RequestMethod.POST)
    public ResponseEntity<String> setConfiguration(@RequestBody String userInfo) {
        if(credentialsService.areCredentialsValid()){
            InitConfigurationDto initConfigurationDto = credentialsService.getInitConfigurationDto();
            if (userInfo == null && !userInfo.isEmpty() && !initConfigurationDto.getDb().isValid()) {
                throw new ConfigurationException("Please create default user");
            }
            sharedDataService.setUserInfo(userInfo);
            sharedDataService.setInitConfigurationDto(initConfigurationDto);
            credentialsService.storeCredentials();

            refreshContext();
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            throw new ConfigurationException("AWS credentials invalid");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (XmlWebApplicationContext) applicationContext;
    }

    private void refreshContext() {
        LOG.info("Context refresh process started.");
        CONTEXT_REFRESH_IN_PROCESS = true;

        applicationContext.setConfigLocation("/WEB-INF/spring-web-config.xml");
        applicationContext.refresh();

        // enabling auth filter
        RestAuthenticationFilter filter = applicationContext.getBean(RestAuthenticationFilter.class);
        filter.setUserRepository(applicationContext.getBean(UserRepository.class));
        filterProxy.setFilter(filter);

        LOG.info("Context refreshed successfully.");
        CONTEXT_REFRESH_IN_PROCESS = false;
    }
}
