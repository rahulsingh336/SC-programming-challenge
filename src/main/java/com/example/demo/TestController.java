package com.example.demo;


import com.google.common.collect.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TestController {

    private List<String> statusHolder = Arrays.asList("CREATED", "APPROVED", "REJECTED", "BLOCKED", "DONE");


    private TaskRepository taskRepository;

    @Autowired
    public TestController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("tasks/{id}")
    public ResponseEntity getTask(@PathVariable Long id) {
        System.out.println("value of id is :::  " + id);
        Optional<Task> byId = taskRepository.findById(id);
        return byId.map(task -> new ResponseEntity<>(task.toDto(), HttpStatus.OK)).orElseGet(() -> new ResponseEntity(HttpStatus.NO_CONTENT));
    }

    @PostMapping("tasks")
    public Long createTask(@RequestBody TaskDto taskDto) {
        System.out.println("Task DTO :::: " + taskDto.toString());
        Task task = new Task(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        return taskRepository.save(task).getId();
    }

    @PutMapping("tasks/{id}")
    public ResponseEntity updateTask(@RequestBody TaskDto taskDto, @PathVariable Long id) {
        //check status
        if (!statusHolder.contains(taskDto.getStatus())) {
            return new ResponseEntity(HttpStatus.OK);
        }
        Optional<Task> byId = taskRepository.findById(id);
        if(byId.isPresent()) {
             Task task = byId.get();
             task.setTitle(taskDto.getTitle());
             task.setDescription(taskDto.getDescription());
             task.setTaskStatus(TaskStatus.valueOf(taskDto.getStatus()));
             taskRepository.save(task);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping("tasks/{id}")
    public ResponseEntity updateTask(@PathVariable Long id) {
        Optional<Task> byId = taskRepository.findById(id);
        if(byId.isPresent()) {
            taskRepository.delete(byId.get());
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("tasks/describe/{id}")
    public ResponseEntity<String> describeTask(@PathVariable Long id) {
        Optional<Task> byId = taskRepository.findById(id);
        if(byId.isPresent()) {
            String foundDescription = String.format("Description of Task [%s: %s] is: %s", id, byId.get().toDto().getTitle()
            , byId.get().toDto().getDescription());
            return new ResponseEntity<>(foundDescription, HttpStatus.OK);
        } else {
            String notFoundDescription = String.format("Task with id = %s does not exist", id);
            return new ResponseEntity(notFoundDescription, HttpStatus.OK);
        }
    }

    @GetMapping("tasks")
    public ResponseEntity findAllTasks() {
        Iterable<Task> findAllTasks = taskRepository.findAll();
        List<TaskDto> allTasks = Streams.stream(findAllTasks).map(Task::toDto).collect(Collectors.toList());
        return new ResponseEntity<>(allTasks, HttpStatus.OK);
    }

    @GetMapping("tasks/describe")
    public ResponseEntity describeAllTask() {

        Iterable<Task> findAllTasks = taskRepository.findAll();
        List<String> allTasksDescription = Streams.stream(findAllTasks).map(Task::toDto)
                .map(taskDto -> String.format("Description of Task [%s: %s] is: %s", taskDto.getId(),taskDto.getTitle(), taskDto.getDescription())).collect(Collectors.toList());
        return new ResponseEntity(allTasksDescription, HttpStatus.OK);
    }

    /*@PostMapping("tasks")
    public String createTask(@PathVariable Long id) {
        System.out.println("value of id is :::  " + id);
        Optional.ofNullable(null).orElse("");
        return "";
    }*/
}
