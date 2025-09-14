package io.braineous.prototype.conductor;

import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.worker.Worker;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        TaskClient taskClient = new TaskClient();
        taskClient.setRootURI("http://localhost:8080/api/");

        TaskRunnerConfigurer configurer = new TaskRunnerConfigurer
                .Builder(taskClient, Arrays.<Worker>asList(new Task1Worker()))
                .withThreadCount(1)
                .build();

        configurer.init();
    }
}
