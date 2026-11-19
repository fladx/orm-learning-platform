package ru.vlad.orm.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import ru.vlad.orm.support.ApiException;
import static ru.vlad.orm.catalog.CatalogDto.*;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final TagRepository tags;
    private final CourseRepository courses;
    private final ModuleRepository modules;
    private final LessonRepository lessons;
    private final ProfileRepository profiles;

    public CatalogService(UserRepository users, CategoryRepository categories, TagRepository tags, CourseRepository courses, ModuleRepository modules, LessonRepository lessons, ProfileRepository profiles) {
        this.users = users;
        this.categories = categories;
        this.tags = tags;
        this.courses = courses;
        this.modules = modules;
        this.lessons = lessons;
        this.profiles = profiles;
    }

    private static <T> T require(JpaRepository<T, Long> repo, Long id, String name) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound(name + " " + id + " not found"));
    }

    public CourseDetails courseDetails(Long id) {
        Course course = courses.findWithModulesById(id).orElseThrow(() -> ApiException.notFound("Course " + id + " not found"));
        var moduleViews = course.getModules().stream().map(CatalogService::toView).toList();
        // Fetch lessons in one query, avoiding a second collection join and N+1 lazy loads.
        List<LessonView> lessonViews = moduleViews.isEmpty() ? List.of()
                : lessons.findByModuleIdInOrderById(moduleViews.stream().map(ModuleView::id).toList())
                         .stream().map(CatalogService::toView).toList();
        return new CourseDetails(toView(course), moduleViews, lessonViews);
    }

    public ProfileView getProfile(Long userId) {
        return profileView(profiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Profile not found")));
    }

    @Transactional
    public ProfileView putProfile(Long userId, ProfileInput input, boolean createOnly) {
        User user = require(users, userId, "User");
        Profile profile = profiles.findByUserId(userId).orElse(null);
        if (profile != null && createOnly) throw ApiException.conflict("User already has a profile");
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        profile.setBio(input.bio());
        return profileView(profiles.save(profile));
    }

    @Transactional
    public void deleteProfile(Long userId) {
        Profile profile = profiles.findByUserId(userId).orElseThrow(() -> ApiException.notFound("Profile not found"));
        profile.getUser().setProfile(null);
        profiles.delete(profile);
    }

    private static ProfileView profileView(Profile p) {
        return new ProfileView(p.getId(), p.getUser().getId(), p.getBio());
    }

    public List<UserView> listUsers() { return users.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(CatalogService::toView).toList(); }
    public UserView getUser(Long id) { return toView(require(users, id, "User")); }

    @Transactional
    public UserView createUser(UserInput r) {
        User e = new User();
        edit(e, r);
        return toView(users.save(e));
    }

    @Transactional
    public UserView updateUser(Long id, UserInput r) {
        User e = require(users, id, "User");
        edit(e, r);
        return toView(e);
    }

    @Transactional
    public void deleteUser(Long id) { users.delete(require(users, id, "User")); }

    private static void edit(User e, UserInput r) {
        e.setName(r.name().trim()); e.setEmail(r.email().trim().toLowerCase(Locale.ROOT)); if (e.getId() == null) e.setRole(r.role()); else if (e.getRole() != r.role()) throw ApiException.badRequest("User role cannot be changed");
    }

    private static UserView toView(User e) { return new UserView(e.getId(), e.getName(), e.getEmail(), e.getRole()); }

    public List<CategoryView> listCategories() { return categories.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(CatalogService::toView).toList(); }
    public CategoryView getCategory(Long id) { return toView(require(categories, id, "Category")); }

    @Transactional
    public CategoryView createCategory(CategoryInput r) {
        Category e = new Category();
        edit(e, r);
        return toView(categories.save(e));
    }

    @Transactional
    public CategoryView updateCategory(Long id, CategoryInput r) {
        Category e = require(categories, id, "Category");
        edit(e, r);
        return toView(e);
    }

    @Transactional
    public void deleteCategory(Long id) { categories.delete(require(categories, id, "Category")); }

    private static void edit(Category e, CategoryInput r) {
        e.setName(r.name().trim());
    }

    private static CategoryView toView(Category e) { return new CategoryView(e.getId(), e.getName()); }

    public List<TagView> listTags() { return tags.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(CatalogService::toView).toList(); }
    public TagView getTag(Long id) { return toView(require(tags, id, "Tag")); }

    @Transactional
    public TagView createTag(TagInput r) {
        Tag e = new Tag();
        edit(e, r);
        return toView(tags.save(e));
    }

    @Transactional
    public TagView updateTag(Long id, TagInput r) {
        Tag e = require(tags, id, "Tag");
        edit(e, r);
        return toView(e);
    }

    @Transactional
    public void deleteTag(Long id) { tags.delete(require(tags, id, "Tag")); }

    private static void edit(Tag e, TagInput r) {
        e.setName(r.name().trim());
    }

    private static TagView toView(Tag e) { return new TagView(e.getId(), e.getName()); }

    public List<CourseView> listCourses() { return courses.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(CatalogService::toView).toList(); }
    public CourseView getCourse(Long id) { return toView(require(courses, id, "Course")); }

    @Transactional
    public CourseView createCourse(CourseInput r) {
        Course e = new Course();
        edit(e, r);
        return toView(courses.save(e));
    }

    @Transactional
    public CourseView updateCourse(Long id, CourseInput r) {
        Course e = require(courses, id, "Course");
        edit(e, r);
        return toView(e);
    }

    @Transactional
    public void deleteCourse(Long id) { courses.delete(require(courses, id, "Course")); }

    private void edit(Course e, CourseInput r) {
        e.setTitle(r.title().trim()); e.setDescription(r.description());
        e.setCategory(require(categories, r.categoryId(), "Category"));
        var teacher = require(users, r.teacherId(), "User");
        if (teacher.getRole() != Role.TEACHER) throw ApiException.badRequest("Course teacher must have TEACHER role");
        e.setTeacher(teacher);
        e.getTags().clear();
        if (r.tagIds() != null) r.tagIds().forEach(id -> e.getTags().add(require(tags, id, "Tag")));
    }

    private static CourseView toView(Course e) { return new CourseView(e.getId(), e.getTitle(), e.getDescription(), e.getCategory().getId(), e.getTeacher().getId(), e.getTags().stream().map(Tag::getId).sorted().toList()); }

    public List<ModuleView> listModules(Long courseId) {
        require(courses, courseId, "Course");
        return modules.findByCourseIdOrderById(courseId).stream().map(CatalogService::toView).toList();
    }
    public ModuleView getModule(Long id) { return toView(require(modules, id, "Module")); }

    @Transactional
    public ModuleView createModule(Long courseId, ModuleInput r) {
        Module e = new Module();
        e.setCourse(require(courses, courseId, "Course"));
        edit(e, r);
        return toView(modules.save(e));
    }

    @Transactional
    public ModuleView updateModule(Long id, ModuleInput r) {
        Module e = require(modules, id, "Module");
        edit(e, r);
        return toView(e);
    }

    @Transactional
    public void deleteModule(Long id) { modules.delete(require(modules, id, "Module")); }

    private static void edit(Module e, ModuleInput r) {
        e.setTitle(r.title().trim());
    }

    private static ModuleView toView(Module e) { return new ModuleView(e.getId(), e.getTitle(), e.getCourse().getId()); }

    public List<LessonView> listLessons(Long moduleId) {
        require(modules, moduleId, "Module");
        return lessons.findByModuleIdOrderById(moduleId).stream().map(CatalogService::toView).toList();
    }
    public LessonView getLesson(Long id) { return toView(require(lessons, id, "Lesson")); }

    @Transactional
    public LessonView createLesson(Long moduleId, LessonInput r) {
        Lesson e = new Lesson();
        e.setModule(require(modules, moduleId, "Module"));
        edit(e, r);
        return toView(lessons.save(e));
    }

    @Transactional
    public LessonView updateLesson(Long id, LessonInput r) {
        Lesson e = require(lessons, id, "Lesson");
        edit(e, r);
        return toView(e);
    }

    @Transactional
    public void deleteLesson(Long id) { lessons.delete(require(lessons, id, "Lesson")); }

    private static void edit(Lesson e, LessonInput r) {
        e.setTitle(r.title().trim()); e.setContent(r.content());
    }

    private static LessonView toView(Lesson e) { return new LessonView(e.getId(), e.getTitle(), e.getContent(), e.getModule().getId()); }
}
