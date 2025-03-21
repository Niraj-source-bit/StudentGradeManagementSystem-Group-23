package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class StudentGradeManagementSystem extends Application {

    // Data models
    private static class User {
        private String username;
        private String password;
        private String fullName;

        public User(String username, String password, String fullName) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
    }

    private static class Grade {
        private long id;
        private String studentId;
        private String subject;
        private double grade;
        private String date;
        private String teacherId;

        public Grade(long id, String studentId, String subject, double grade, String date, String teacherId) {
            this.id = id;
            this.studentId = studentId;
            this.subject = subject;
            this.grade = grade;
            this.date = date;
            this.teacherId = teacherId;
        }

        public long getId() { return id; }
        public String getStudentId() { return studentId; }
        public String getSubject() { return subject; }
        public double getGrade() { return grade; }
        public String getDate() { return date; }
        public String getTeacherId() { return teacherId; }
        public void setGrade(double grade) { this.grade = grade; }
        public void setDate(String date) { this.date = date; }
    }

    private static class Notification {
        private String userId;
        private String message;
        private String date;

        public Notification(String userId, String message, String date) {
            this.userId = userId;
            this.message = message;
            this.date = date;
        }

        public String getUserId() { return userId; }
        public String getMessage() { return message; }
        public String getDate() { return date; }
    }

    // Data storage
    private List<User> students = new ArrayList<>();
    private List<User> teachers = new ArrayList<>();
    private List<User> admins = new ArrayList<>();
    private List<Grade> grades = new ArrayList<>();
    private List<Notification> notifications = new ArrayList<>();

    // Current state
    private User currentUser;
    private String currentUserType;
    private Preferences prefs = Preferences.userNodeForPackage(StudentGradeManagementSystem.class);

    // UI components
    private Stage primaryStage;
    private Scene loginScene;
    private VBox loginForm;
    private VBox registerForm;
    private Label loginTitle;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Initialize data
        loadSampleData();

        // Create login screen
        VBox loginRoot = createLoginScreen();
        loginScene = new Scene(loginRoot, 800, 600);

        // Apply styles
        loginScene.getStylesheets().add(getClass().getResource("styles.css") != null ?
                getClass().getResource("styles.css").toExternalForm() : "");

        // Show stage
        primaryStage.setTitle("Student Grade Management System");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // ========== DATA INITIALIZATION ==========

    private void loadSampleData() {
        // Add admin user
        admins.add(new User("admin", "admin123", "System Administrator"));

        // Add sample teachers
        teachers.add(new User("teacher1", "pass123", "John Smith"));
        teachers.add(new User("teacher2", "pass123", "Jane Doe"));

        // Add sample students
        students.add(new User("student1", "pass123", "Alice Johnson"));
        students.add(new User("student2", "pass123", "Bob Williams"));

        // Add sample grades
        grades.add(new Grade(1, "student1", "Mathematics", 85, LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), "teacher1"));
        grades.add(new Grade(2, "student1", "Science", 75, LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), "teacher1"));
        grades.add(new Grade(3, "student2", "History", 92, LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), "teacher2"));
    }

    // ========== LOGIN SCREEN ==========

    private VBox createLoginScreen() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f0f2f5;");

        // Create content container
        VBox container = new VBox(20);
        container.setMaxWidth(900);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        // Header
        HBox header = createHeader();

        // Login options
        HBox loginOptions = createLoginOptions();

        // Login form (initially hidden)
        loginForm = createLoginForm();
        loginForm.setVisible(false);
        loginForm.setManaged(false);

        // Register form (initially hidden)
        registerForm = createRegisterForm();
        registerForm.setVisible(false);
        registerForm.setManaged(false);

        // Add all components to container
        container.getChildren().addAll(header, loginOptions, loginForm, registerForm);
        root.getChildren().add(container);

        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setSpacing(20);

        Label title = new Label("Student Grade Management System");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #1a73e8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button registerBtn = new Button("Register New User");
        registerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8;");
        registerBtn.setOnAction(e -> showRegisterForm());

        header.getChildren().addAll(title, spacer, registerBtn);
        return header;
    }

    private HBox createLoginOptions() {
        HBox options = new HBox(15);
        options.setAlignment(Pos.CENTER);
        options.setPadding(new Insets(20, 0, 20, 0));

        Button studentLoginBtn = createLoginButton("Student Login", "-fx-background-color: #1a73e8;", e -> showLoginForm("student"));
        Button adminLoginBtn = createLoginButton("Admin Login", "-fx-background-color: #2c3e50;", e -> showLoginForm("admin"));
        Button teacherLoginBtn = createLoginButton("Teacher Login", "-fx-background-color: #1a73e8;", e -> showLoginForm("teacher"));

        options.getChildren().addAll(studentLoginBtn, adminLoginBtn, teacherLoginBtn);
        return options;
    }

    private Button createLoginButton(String text, String style, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setPrefWidth(180);
        button.setPrefHeight(50);
        button.setStyle(style + " -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 10;");
        button.setOnAction(handler);
        return button;
    }

    private VBox createLoginForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(500);
        form.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        loginTitle = new Label("Login");
        loginTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        loginTitle.setStyle("-fx-text-fill: #1a73e8;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 2;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 2;");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(100);
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-background-radius: 8;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 8;");
        cancelButton.setOnAction(e -> hideLoginForm());

        buttons.getChildren().addAll(loginButton, cancelButton);

        form.getChildren().addAll(loginTitle, usernameField, passwordField, buttons);

        // Set login action
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (currentUserType.equals("admin")) {
                Optional<User> admin = admins.stream()
                        .filter(a -> a.getUsername().equals(username) && a.getPassword().equals(password))
                        .findFirst();

                if (admin.isPresent()) {
                    currentUser = admin.get();
                    showDashboard("admin");
                } else {
                    showAlert("Invalid admin credentials");
                }
            } else {
                List<User> userList = currentUserType.equals("student") ? students : teachers;
                Optional<User> user = userList.stream()
                        .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                        .findFirst();

                if (user.isPresent()) {
                    currentUser = user.get();
                    showDashboard(currentUserType);
                } else {
                    showAlert("Invalid credentials");
                }
            }
        });

        return form;
    }

    private VBox createRegisterForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(500);
        form.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label registerTitle = new Label("Register New User");
        registerTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        registerTitle.setStyle("-fx-text-fill: #1a73e8;");

        ComboBox<String> userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("Student", "Teacher");
        userTypeCombo.setPromptText("Select User Type");
        userTypeCombo.setPrefHeight(40);
        userTypeCombo.setMaxWidth(Double.MAX_VALUE);
        userTypeCombo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 2;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 2;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 2;");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setPrefHeight(40);
        fullNameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 2;");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);

        Button registerButton = new Button("Register");
        registerButton.setPrefHeight(40);
        registerButton.setPrefWidth(100);
        registerButton.setDefaultButton(true);
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 8;");
        cancelButton.setOnAction(e -> hideRegisterForm());

        buttons.getChildren().addAll(registerButton, cancelButton);

        form.getChildren().addAll(registerTitle, userTypeCombo, usernameField, passwordField, fullNameField, buttons);

        // Set register action
        registerButton.setOnAction(e -> {
            String userType = userTypeCombo.getValue();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String fullName = fullNameField.getText();

            if (userType == null || userType.isEmpty()) {
                showAlert("Please select a user type");
                return;
            }

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                showAlert("All fields are required");
                return;
            }

            List<User> userList = userType.equalsIgnoreCase("student") ? students : teachers;

            boolean usernameExists = userList.stream()
                    .anyMatch(u -> u.getUsername().equals(username));

            if (usernameExists) {
                showAlert("Username already exists");
                return;
            }

            userList.add(new User(username, password, fullName));
            hideRegisterForm();
            showAlert("Registration successful! Please login.");
            showLoginForm(userType.toLowerCase());
        });

        return form;
    }

    private void showLoginForm(String userType) {
        currentUserType = userType;
        loginTitle.setText(userType.substring(0, 1).toUpperCase() + userType.substring(1) + " Login");
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        registerForm.setVisible(false);
        registerForm.setManaged(false);
    }

    private void hideLoginForm() {
        loginForm.setVisible(false);
        loginForm.setManaged(false);
    }

    private void showRegisterForm() {
        registerForm.setVisible(true);
        registerForm.setManaged(true);
        loginForm.setVisible(false);
        loginForm.setManaged(false);
    }

    private void hideRegisterForm() {
        registerForm.setVisible(false);
        registerForm.setManaged(false);
    }

    // ========== DASHBOARD SCREENS ==========

    private void showDashboard(String userType) {
        BorderPane dashboard = new BorderPane();
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: #f0f2f5;");

        // Create header
        HBox header = createDashboardHeader(userType);
        dashboard.setTop(header);

        // Set content based on user type
        switch (userType) {
            case "student":
                dashboard.setCenter(createStudentDashboard());
                break;
            case "teacher":
                dashboard.setCenter(createTeacherDashboard());
                break;
            case "admin":
                dashboard.setCenter(createAdminDashboard());
                break;
        }

        Scene dashboardScene = new Scene(dashboard, 900, 700);
        dashboardScene.getStylesheets().add(getClass().getResource("styles.css") != null ?
                getClass().getResource("styles.css").toExternalForm() : "");
        primaryStage.setScene(dashboardScene);
    }

    private HBox createDashboardHeader(String userType) {
        HBox header = new HBox(15);
        header.setPadding(new Insets(0, 0, 15, 0));
        header.setStyle("-fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 2 0;");

        String titleText = userType.substring(0, 1).toUpperCase() + userType.substring(1) + " Dashboard";
        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setStyle("-fx-text-fill: #1a73e8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);

        if (userType.equals("student")) {
            Button notificationBtn = new Button();
            notificationBtn.setGraphic(new Label("🔔"));
            notificationBtn.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 8; -fx-padding: 10;");
            notificationBtn.setOnAction(e -> showNotifications());
            buttonBox.getChildren().add(notificationBtn);
        }

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8;");
        logoutBtn.setOnAction(e -> logout());

        buttonBox.getChildren().add(logoutBtn);
        header.getChildren().addAll(title, spacer, buttonBox);

        return header;
    }

    private ScrollPane createStudentDashboard() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Create status summary
        VBox statusSummary = createStatusSummary();

        // Create grades table
        TableView<Grade> gradesTable = createGradesTable();

        // Filter grades for this student
        List<Grade> studentGrades = grades.stream()
                .filter(g -> g.getStudentId().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        // Add data to table
        gradesTable.getItems().addAll(studentGrades);

        // Add components to dashboard
        content.getChildren().addAll(statusSummary, gradesTable);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    private VBox createStatusSummary() {
        VBox summary = new VBox(15);
        summary.setPadding(new Insets(20));
        summary.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label title = new Label("Overall Status");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #1a73e8;");

        HBox summaryContent = new HBox(30);
        summaryContent.setPadding(new Insets(10, 0, 0, 0));

        // Filter grades for this student
        List<Grade> studentGrades = grades.stream()
                .filter(g -> g.getStudentId().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        int totalGrades = studentGrades.size();
        long failedGrades = studentGrades.stream().filter(g -> g.getGrade() < 30).count();
        String overallStatus = failedGrades == 0 ? "Pass" : "Fail";
        String statusColorClass = failedGrades == 0 ? "-fx-background-color: rgba(76, 175, 80, 0.1); -fx-text-fill: #4CAF50;"
                : "-fx-background-color: rgba(244, 67, 54, 0.1); -fx-text-fill: #f44336;";

        VBox totalBox = new VBox(5);
        Label totalLabel = new Label("Total Subjects");
        Label totalValue = new Label(String.valueOf(totalGrades));
        totalValue.setFont(Font.font("System", FontWeight.BOLD, 16));
        totalBox.getChildren().addAll(totalLabel, totalValue);

        VBox failedBox = new VBox(5);
        Label failedLabel = new Label("Failed Subjects");
        Label failedValue = new Label(String.valueOf(failedGrades));
        failedValue.setFont(Font.font("System", FontWeight.BOLD, 16));
        failedBox.getChildren().addAll(failedLabel, failedValue);

        VBox statusBox = new VBox(5);
        Label statusLabel = new Label("Overall Status");
        Label statusValue = new Label(overallStatus);
        statusValue.setFont(Font.font("System", FontWeight.BOLD, 16));
        statusValue.setStyle(statusColorClass + " -fx-padding: 3 8; -fx-background-radius: 4;");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        summaryContent.getChildren().addAll(totalBox, failedBox, statusBox);
        summary.getChildren().addAll(title, summaryContent);

        return summary;
    }

    private ScrollPane createTeacherDashboard() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Create statistics panel
        VBox stats = new VBox(10);
        stats.setPadding(new Insets(20));
        stats.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label statsTitle = new Label("Statistics");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        statsTitle.setStyle("-fx-text-fill: #1a73e8;");

        Label studentsCount = new Label("Total Students: " + students.size());
        studentsCount.setFont(Font.font("System", FontWeight.NORMAL, 14));

        stats.getChildren().addAll(statsTitle, studentsCount);

        // Create add grade form
        VBox addGradeForm = new VBox(15);
        addGradeForm.setPadding(new Insets(20));
        addGradeForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label formTitle = new Label("Add Grade");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        formTitle.setStyle("-fx-text-fill: #1a73e8;");

        ComboBox<String> studentSelect = new ComboBox<>();
        studentSelect.setPromptText("Select Student");
        studentSelect.setMaxWidth(Double.MAX_VALUE);

        // Add student names to combo box
        for (User student : students) {
            studentSelect.getItems().add(student.getFullName() + " (" + student.getUsername() + ")");
        }

        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");

        TextField gradeField = new TextField();
        gradeField.setPromptText("Grade");

        Button addGradeButton = new Button("Add Grade");
        addGradeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 8;");

        addGradeButton.setOnAction(e -> {
            String selectedStudent = studentSelect.getValue();
            String subject = subjectField.getText();
            String gradeText = gradeField.getText();

            if (selectedStudent == null || subject.isEmpty() || gradeText.isEmpty()) {
                showAlert("All fields are required");
                return;
            }

            try {
                double gradeValue = Double.parseDouble(gradeText);

                if (gradeValue < 0 || gradeValue > 100) {
                    showAlert("Grade must be between 0 and 100");
                    return;
                }

                // Extract student username from selection
                String studentUsername = selectedStudent.substring(selectedStudent.lastIndexOf("(") + 1, selectedStudent.lastIndexOf(")"));

                // Create new grade
                long newId = System.currentTimeMillis();
                String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                Grade newGrade = new Grade(newId, studentUsername, subject, gradeValue, currentDate, currentUser.getUsername());

                grades.add(newGrade);

                // Clear form
                studentSelect.setValue(null);
                subjectField.clear();
                gradeField.clear();

                // Refresh dashboard
                showDashboard("teacher");

            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid number for grade");
            }
        });

        addGradeForm.getChildren().addAll(formTitle, studentSelect, subjectField, gradeField, addGradeButton);

        // Create grades table
        TableView<Grade> gradesTable = createTeacherGradesTable();

        // Filter grades for this teacher
        List<Grade> teacherGrades = grades.stream()
                .filter(g -> g.getTeacherId().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        // Add data to table
        gradesTable.getItems().addAll(teacherGrades);

        // Add components to dashboard
        content.getChildren().addAll(stats, addGradeForm, gradesTable);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    private ScrollPane createAdminDashboard() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Create manage users panel
        VBox manageUsers = new VBox(15);
        manageUsers.setPadding(new Insets(20));
        manageUsers.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label usersTitle = new Label("Manage Users");
        usersTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        usersTitle.setStyle("-fx-text-fill: #1a73e8;");

        HBox userLists = new HBox(20);
        userLists.setPadding(new Insets(10, 0, 0, 0));

        // Teachers list
        VBox teachersList = new VBox(10);
        Label teachersTitle = new Label("Teachers");
        teachersTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        teachersTitle.setStyle("-fx-text-fill: #1a73e8;");

        VBox teachersBox = new VBox(5);
        for (User teacher : teachers) {
            HBox userRow = createUserRow(teacher, "teacher");
            teachersBox.getChildren().add(userRow);
        }

        teachersList.getChildren().addAll(teachersTitle, teachersBox);

        // Students list
        VBox studentsList = new VBox(10);
        Label studentsTitle = new Label("Students");
        studentsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        studentsTitle.setStyle("-fx-text-fill: #1a73e8;");

        VBox studentsBox = new VBox(5);
        for (User student : students) {
            HBox userRow = createUserRow(student, "student");
            studentsBox.getChildren().add(userRow);
        }

        studentsList.getChildren().addAll(studentsTitle, studentsBox);

        // Add user lists to the main container
        userLists.getChildren().addAll(teachersList, studentsList);
        manageUsers.getChildren().addAll(usersTitle, userLists);

        // Create statistics panel
        VBox stats = new VBox(10);
        stats.setPadding(new Insets(20));
        stats.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label statsTitle = new Label("System Statistics");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        statsTitle.setStyle("-fx-text-fill: #1a73e8;");

        Label totalStudents = new Label("Total Students: " + students.size());
        totalStudents.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label totalTeachers = new Label("Total Teachers: " + teachers.size());
        totalTeachers.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Label totalGrades = new Label("Total Grades: " + grades.size());
        totalGrades.setFont(Font.font("System", FontWeight.NORMAL, 14));

        stats.getChildren().addAll(statsTitle, totalStudents, totalTeachers, totalGrades);

        // Add components to dashboard
        content.getChildren().addAll(manageUsers, stats);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    private HBox createUserRow(User user, String userType) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 1);");

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label fullNameLabel = new Label(user.getFullName());
        fullNameLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 8;");
        deleteButton.setOnAction(e -> deleteUser(user, userType));

        row.getChildren().addAll(usernameLabel, fullNameLabel, spacer, deleteButton);
        return row;
    }

    private void deleteUser(User user, String userType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (userType.equals("student")) {
                students.remove(user);
            } else if (userType.equals("teacher")) {
                teachers.remove(user);
            }
            showDashboard("admin"); // Refresh the dashboard
        }
    }

    // ========== NOTIFICATIONS ==========

    private void showNotifications() {
        List<Notification> userNotifications = notifications.stream()
                .filter(n -> n.getUserId().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        if (userNotifications.isEmpty()) {
            showAlert("You have no notifications.");
            return;
        }

        VBox notificationBox = new VBox(10);
        notificationBox.setPadding(new Insets(20));
        notificationBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label title = new Label("Notifications");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #1a73e8;");

        for (Notification notification : userNotifications) {
            HBox notificationRow = new HBox(10);
            notificationRow.setPadding(new Insets(10));
            notificationRow.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

            Label messageLabel = new Label(notification.getMessage());
            messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

            Label dateLabel = new Label(notification.getDate());
            dateLabel.setFont(Font.font("System", FontWeight.LIGHT, 12));
            dateLabel.setStyle("-fx-text-fill: #666;");

            notificationRow.getChildren().addAll(messageLabel, dateLabel);
            notificationBox.getChildren().add(notificationRow);
        }

        Scene notificationScene = new Scene(notificationBox, 400, 300);
        Stage notificationStage = new Stage();
        notificationStage.setTitle("Notifications");
        notificationStage.setScene(notificationScene);
        notificationStage.show();
    }

    // ========== UTILITY METHODS ==========

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logout() {
        currentUser = null;
        currentUserType = null;
        primaryStage.setScene(loginScene);
    }

    private TableView<Grade> createGradesTable() {
        TableView<Grade> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Grade, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<Grade, Double> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

        TableColumn<Grade, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(subjectCol, gradeCol, dateCol);
        return table;
    }

    private TableView<Grade> createTeacherGradesTable() {
        TableView<Grade> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Grade, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<Grade, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<Grade, Double> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

        TableColumn<Grade, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(studentCol, subjectCol, gradeCol, dateCol);
        return table;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
