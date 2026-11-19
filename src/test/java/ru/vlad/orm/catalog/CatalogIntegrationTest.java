package ru.vlad.orm.catalog;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.vlad.orm.support.ApiException;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static ru.vlad.orm.catalog.CatalogDto.*;

@SpringBootTest
@ActiveProfiles("test")
class CatalogIntegrationTest {
    @Autowired CatalogService catalog;
    @Autowired CourseRepository courses;

    @Test
    void detachedUnfetchedCollectionFailsButEntityGraphKeepsModulesAvailable() {
        long courseId = course();
        catalog.createModule(courseId, new ModuleInput("Persistence"));
        Course unloaded = courses.findById(courseId).orElseThrow();
        assertThatThrownBy(() -> unloaded.getModules().size()).isInstanceOf(LazyInitializationException.class);
        Course loaded = courses.findWithModulesById(courseId).orElseThrow();
        assertThat(loaded.getModules()).hasSize(1);
        assertThat(catalog.courseDetails(courseId).modules()).extracting(ModuleView::title).containsExactly("Persistence");
    }

    @Test
    void deletingUnenrolledCourseCascadesToModulesAndLessons() {
        long courseId = course();
        long moduleId = catalog.createModule(courseId, new ModuleInput("Persistence")).id();
        long lessonId = catalog.createLesson(moduleId, new LessonInput("Entity mapping", "Content")).id();
        catalog.deleteCourse(courseId);
        assertThatThrownBy(() -> catalog.getModule(moduleId)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> catalog.getLesson(lessonId)).isInstanceOf(ApiException.class);
    }

    @Test
    void aStudentCannotBeAssignedAsCourseTeacher() {
        var student = catalog.createUser(new UserInput("Student", random() + "@example.test", Role.STUDENT));
        var category = catalog.createCategory(new CategoryInput(random()));
        assertThatThrownBy(() -> catalog.createCourse(new CourseInput("ORM", null, category.id(), student.id(), Set.of())))
                .isInstanceOf(ApiException.class).hasMessageContaining("TEACHER");
    }

    @Test
    void profileHasOnePerUserAndSupportsUpdateAndDeletion() {
        long user = catalog.createUser(new UserInput("Student", random() + "@example.test", Role.STUDENT)).id();
        long profile = catalog.putProfile(user, new ProfileInput("Bio"), true).id();
        assertThatThrownBy(() -> catalog.putProfile(user, new ProfileInput("Duplicate"), true)).isInstanceOf(ApiException.class);
        assertThat(catalog.putProfile(user, new ProfileInput("Revised"), false).id()).isEqualTo(profile);
        assertThat(catalog.getProfile(user).bio()).isEqualTo("Revised");
        catalog.deleteProfile(user);
        assertThatThrownBy(() -> catalog.getProfile(user)).isInstanceOf(ApiException.class);
    }

    @Test
    void aTagCanBeSharedByMultipleCourses() {
        var tag = catalog.createTag(new TagInput(random()));
        var teacher = catalog.createUser(new UserInput("Teacher", random() + "@example.test", Role.TEACHER));
        var category = catalog.createCategory(new CategoryInput(random()));
        for (String title : new String[]{"JPA", "Spring Data"}) {
            long id = catalog.createCourse(new CourseInput(title, "Description", category.id(), teacher.id(), Set.of(tag.id()))).id();
            assertThat(catalog.getCourse(id).tagIds()).containsExactly(tag.id());
        }
    }

    private long course() {
        var teacher = catalog.createUser(new UserInput("Teacher", random() + "@example.test", Role.TEACHER));
        var category = catalog.createCategory(new CategoryInput(random()));
        return catalog.createCourse(new CourseInput("ORM", null, category.id(), teacher.id(), Set.of())).id();
    }

    private static String random() { return UUID.randomUUID().toString(); }
}
