package ru.vlad.orm.catalog;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static ru.vlad.orm.catalog.CatalogDto.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) { this.service = service; }

    @GetMapping("/courses/{id}/details")
    public CourseDetails courseDetails(@PathVariable Long id) { return service.courseDetails(id); }

    @GetMapping("/users/{userId}/profile")
    public ProfileView profile(@PathVariable Long userId) { return service.getProfile(userId); }

    @PostMapping("/users/{userId}/profile")
    public ResponseEntity<ProfileView> createProfile(@PathVariable Long userId, @Valid @RequestBody ProfileInput input) {
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/profile"))
                .body(service.putProfile(userId, input, true));
    }

    @PutMapping("/users/{userId}/profile")
    public ProfileView updateProfile(@PathVariable Long userId, @Valid @RequestBody ProfileInput input) {
        return service.putProfile(userId, input, false);
    }

    @DeleteMapping("/users/{userId}/profile")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        service.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public List<UserView> listUsers() { return service.listUsers(); }

    @PostMapping("/users")
    public ResponseEntity<UserView> createUser(@Valid @RequestBody UserInput input) {
        UserView created = service.createUser(input);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @GetMapping("/users/{id}")
    public UserView getUser(@PathVariable Long id) { return service.getUser(id); }

    @PutMapping("/users/{id}")
    public UserView updateUser(@PathVariable Long id, @Valid @RequestBody UserInput input) {
        return service.updateUser(id, input);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public List<CategoryView> listCategories() { return service.listCategories(); }

    @PostMapping("/categories")
    public ResponseEntity<CategoryView> createCategory(@Valid @RequestBody CategoryInput input) {
        CategoryView created = service.createCategory(input);
        return ResponseEntity.created(URI.create("/api/categories/" + created.id())).body(created);
    }

    @GetMapping("/categories/{id}")
    public CategoryView getCategory(@PathVariable Long id) { return service.getCategory(id); }

    @PutMapping("/categories/{id}")
    public CategoryView updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryInput input) {
        return service.updateCategory(id, input);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        service.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    public List<TagView> listTags() { return service.listTags(); }

    @PostMapping("/tags")
    public ResponseEntity<TagView> createTag(@Valid @RequestBody TagInput input) {
        TagView created = service.createTag(input);
        return ResponseEntity.created(URI.create("/api/tags/" + created.id())).body(created);
    }

    @GetMapping("/tags/{id}")
    public TagView getTag(@PathVariable Long id) { return service.getTag(id); }

    @PutMapping("/tags/{id}")
    public TagView updateTag(@PathVariable Long id, @Valid @RequestBody TagInput input) {
        return service.updateTag(id, input);
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        service.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses")
    public List<CourseView> listCourses() { return service.listCourses(); }

    @PostMapping("/courses")
    public ResponseEntity<CourseView> createCourse(@Valid @RequestBody CourseInput input) {
        CourseView created = service.createCourse(input);
        return ResponseEntity.created(URI.create("/api/courses/" + created.id())).body(created);
    }

    @GetMapping("/courses/{id}")
    public CourseView getCourse(@PathVariable Long id) { return service.getCourse(id); }

    @PutMapping("/courses/{id}")
    public CourseView updateCourse(@PathVariable Long id, @Valid @RequestBody CourseInput input) {
        return service.updateCourse(id, input);
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        service.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/modules")
    public List<ModuleView> listModules(@PathVariable Long courseId) { return service.listModules(courseId); }

    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<ModuleView> createModule(@PathVariable Long courseId, @Valid @RequestBody ModuleInput input) {
        ModuleView created = service.createModule(courseId, input);
        return ResponseEntity.created(URI.create("/api/modules/" + created.id())).body(created);
    }

    @GetMapping("/modules/{id}")
    public ModuleView getModule(@PathVariable Long id) { return service.getModule(id); }

    @PutMapping("/modules/{id}")
    public ModuleView updateModule(@PathVariable Long id, @Valid @RequestBody ModuleInput input) {
        return service.updateModule(id, input);
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        service.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/modules/{moduleId}/lessons")
    public List<LessonView> listLessons(@PathVariable Long moduleId) { return service.listLessons(moduleId); }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<LessonView> createLesson(@PathVariable Long moduleId, @Valid @RequestBody LessonInput input) {
        LessonView created = service.createLesson(moduleId, input);
        return ResponseEntity.created(URI.create("/api/lessons/" + created.id())).body(created);
    }

    @GetMapping("/lessons/{id}")
    public LessonView getLesson(@PathVariable Long id) { return service.getLesson(id); }

    @PutMapping("/lessons/{id}")
    public LessonView updateLesson(@PathVariable Long id, @Valid @RequestBody LessonInput input) {
        return service.updateLesson(id, input);
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        service.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
