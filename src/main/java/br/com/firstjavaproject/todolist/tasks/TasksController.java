package br.com.firstjavaproject.todolist.tasks;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.firstjavaproject.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TasksController {

  @Autowired
  private ITasksRepository iTasksRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TasksModel tasksModel, HttpServletRequest request) {

    var userId = request.getAttribute("userId");
    tasksModel.setUserId((UUID) userId);

    var currentDate = LocalDateTime.now();

    if (currentDate.isAfter(tasksModel.getStartAt()) ||
        currentDate.isAfter(tasksModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insira uma data de início/término válida.");
    }

    if (tasksModel.getStartAt().isAfter(tasksModel.getEndAt())
        || tasksModel.getEndAt().isBefore(tasksModel.getStartAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insira uma data de início/término válida.");
    }

    var task = this.iTasksRepository.save(tasksModel);

    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TasksModel> index(HttpServletRequest request) {
    var userId = request.getAttribute("userId");
    var tasks = this.iTasksRepository.findByUserId((UUID) userId);

    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity update(@RequestBody TasksModel tasksModel, HttpServletRequest request, @PathVariable UUID id) {

    var task = this.iTasksRepository.findById(id).orElse(null);

    if (task == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada.");
    }

    var userId = request.getAttribute("userId");

    if (task.getUserId().equals(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Você não tem permissão para alterar esta tarefa.");
    }

    Utils.copyNonNullProperties(tasksModel, task);

    var updatedTask = this.iTasksRepository.save(task);

    return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
  }
}
