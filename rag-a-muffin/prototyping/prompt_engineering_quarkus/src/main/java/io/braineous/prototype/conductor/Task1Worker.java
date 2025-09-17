package io.braineous.prototype.conductor;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.metadata.tasks.Task;

public class Task1Worker implements Worker{

    public Task1Worker() {
    }

    /**
     * @return
     */
    public String getTaskDefName() {
        return "task_1";
    }

    /**
     * @param task
     * @return
     */
    public TaskResult execute(Task task) {
        System.out.println("Executing task_1 with input: " + task.getInputData());
        TaskResult result = new TaskResult(task);
        result.setStatus(TaskResult.Status.COMPLETED);
        result.getOutputData().put("message", "Hello from Java worker!");
        System.out.println(result);
        return result;
    }
}
