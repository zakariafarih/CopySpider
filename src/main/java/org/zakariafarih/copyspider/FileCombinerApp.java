package org.zakariafarih.copyspider;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileCombinerApp extends Application {

    // Instance Variables for UI Components
    private TableView<FileItem> tableView;
    private ObservableList<FileItem> fileItems;

    private RadioButton absolutePathRadio;
    private RadioButton relativePathRadio;
    private TextField relativeBaseField;
    private Button selectBaseButton;

    private ComboBox<String> encodingComboBox;
    private TextField fileTypeFilterField;
    private Spinner<Integer> depthSpinner;

    private File baseDirectory = null;
    private Set<String> supportedExtensions = new HashSet<>(Arrays.asList(".txt", ".java", ".csv"));

    private ProgressBar progressBar;
    private MenuBar menuBar;

    // Buttons defined as instance variables for event handling
    private Button selectFilesButton;
    private Button selectFoldersButton;
    private Button clearSelectionButton;
    private Button combineButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Combiner");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Initialize Menu Bar
        menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // Top: Selection Buttons and File Type Filter
        HBox topBox = createTopBox(primaryStage);
        VBox topContainer = new VBox(menuBar, topBox);
        root.setTop(topContainer);

        // Center: TableView to display selected files
        tableView = createTableView();
        root.setCenter(tableView);

        // Bottom: Options and Combine Button
        VBox bottomBox = createBottomBox(primaryStage);
        root.setBottom(bottomBox);

        // Drag and Drop Support
        setupDragAndDrop(root);

        // Event Handlers
        setupEventHandlers(primaryStage);

        // Show Welcome Dialog
        showWelcomeDialog();

        root.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("styles/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the MenuBar with File and Help menus.
     */
    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem selectFilesItem = new MenuItem("Select Files");
        MenuItem selectFoldersItem = new MenuItem("Select Folders");
        MenuItem clearSelectionItem = new MenuItem("Clear Selection");
        MenuItem exitItem = new MenuItem("Exit");

        selectFilesItem.setOnAction(e -> selectFiles(primaryStage));
        selectFoldersItem.setOnAction(e -> selectFolders(primaryStage));
        clearSelectionItem.setOnAction(e -> {
            if (confirmAction("Are you sure you want to clear all selections?")) {
                fileItems.clear();
            }
        });
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(selectFilesItem, selectFoldersItem, clearSelectionItem, new SeparatorMenuItem(), exitItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem howToUseItem = new MenuItem("How to Use");
        MenuItem aboutItem = new MenuItem("About");

        howToUseItem.setOnAction(e -> showHowToUseDialog());
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(howToUseItem, aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    /**
     * Creates the top HBox containing selection buttons and file type filter.
     */
    private HBox createTopBox(Stage primaryStage) {
        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(10, 0, 10, 0));

        selectFilesButton = new Button("Select Files");
        selectFilesButton.setTooltip(new Tooltip("Select individual files to combine"));

        selectFoldersButton = new Button("Select Folders");
        selectFoldersButton.setTooltip(new Tooltip("Select folders to include their files"));

        clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setTooltip(new Tooltip("Clear all selected files and folders"));

        Label filterLabel = new Label("File Type Filter:");
        fileTypeFilterField = new TextField();
        fileTypeFilterField.setPromptText("e.g., .txt,.java");
        fileTypeFilterField.setPrefWidth(150);
        fileTypeFilterField.setTooltip(new Tooltip("Specify file extensions to include (comma-separated)"));

        topBox.getChildren().addAll(selectFilesButton, selectFoldersButton, clearSelectionButton, filterLabel, fileTypeFilterField);
        return topBox;
    }

    /**
     * Creates the TableView to display selected files and folders.
     */
    private TableView<FileItem> createTableView() {
        TableView<FileItem> tableView = new TableView<>();
        fileItems = FXCollections.observableArrayList();
        tableView.setItems(fileItems);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setPlaceholder(new Label("No files or folders selected"));

        // Type Column
        TableColumn<FileItem, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(100);

        // File Path Column
        TableColumn<FileItem, String> pathColumn = new TableColumn<>("File Path");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathColumn.setPrefWidth(700);

        tableView.getColumns().addAll(typeColumn, pathColumn);
        return tableView;
    }

    /**
     * Creates the bottom VBox containing options and the Combine button.
     */
    private VBox createBottomBox(Stage primaryStage) {
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        // Path Representation Options
        HBox pathOptionsBox = new HBox(10);
        pathOptionsBox.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup pathToggleGroup = new ToggleGroup();
        absolutePathRadio = new RadioButton("Absolute Path");
        absolutePathRadio.setToggleGroup(pathToggleGroup);
        absolutePathRadio.setSelected(true);
        absolutePathRadio.setTooltip(new Tooltip("Use absolute paths in the combined file"));

        relativePathRadio = new RadioButton("Relative Path");
        relativePathRadio.setToggleGroup(pathToggleGroup);
        relativePathRadio.setTooltip(new Tooltip("Use paths relative to the base directory"));

        relativeBaseField = new TextField();
        relativeBaseField.setPromptText("Base Directory for Relative Path");
        relativeBaseField.setPrefWidth(300);
        relativeBaseField.setDisable(true);
        relativeBaseField.setTooltip(new Tooltip("Base directory to calculate relative paths"));

        selectBaseButton = new Button("Select Base Directory");
        selectBaseButton.setDisable(true);
        selectBaseButton.setTooltip(new Tooltip("Choose the base directory for relative paths"));

        pathOptionsBox.getChildren().addAll(absolutePathRadio, relativePathRadio, relativeBaseField, selectBaseButton);

        // Encoding Options
        HBox encodingBox = new HBox(10);
        encodingBox.setAlignment(Pos.CENTER_LEFT);

        Label encodingLabel = new Label("Select Encoding:");
        encodingComboBox = new ComboBox<>();
        encodingComboBox.getItems().addAll("UTF-8", "ISO-8859-1", "US-ASCII");
        encodingComboBox.setValue("UTF-8");
        encodingComboBox.setTooltip(new Tooltip("Select the encoding for reading and writing files"));

        encodingBox.getChildren().addAll(encodingLabel, encodingComboBox);

        // Recursive Depth Control
        HBox depthBox = new HBox(10);
        depthBox.setAlignment(Pos.CENTER_LEFT);

        Label depthLabel = new Label("Recursive Depth:");
        depthSpinner = new Spinner<>(1, 10, 5);
        depthSpinner.setEditable(true);
        depthSpinner.setTooltip(new Tooltip("Set the maximum depth for folder traversal"));

        depthBox.getChildren().addAll(depthLabel, depthSpinner);

        // Combine Button
        combineButton = new Button("Combine Files");
        combineButton.setMaxWidth(Double.MAX_VALUE);
        combineButton.setTooltip(new Tooltip("Combine the selected files and folders into a single file"));

        // Progress Bar
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(800);
        progressBar.setVisible(false);
        progressBar.setTooltip(new Tooltip("Shows the progress of the file combination process"));

        bottomBox.getChildren().addAll(pathOptionsBox, encodingBox, depthBox, combineButton, progressBar);
        return bottomBox;
    }

    /**
     * Sets up Drag and Drop functionality for the root pane.
     */
    private void setupDragAndDrop(BorderPane root) {
        root.setOnDragOver(event -> {
            if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        root.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> draggedFiles = db.getFiles();
                for (File file : draggedFiles) {
                    if (file.isFile()) {
                        addFileItem(file);
                    } else if (file.isDirectory()) {
                        addFolderItem(file);
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Sets up event handlers for various UI components.
     */
    private void setupEventHandlers(Stage primaryStage) {
        // Event Handlers for Selection Buttons
        selectFilesButton.setOnAction(e -> selectFiles(primaryStage));
        selectFoldersButton.setOnAction(e -> selectFolders(primaryStage));
        clearSelectionButton.setOnAction(e -> {
            if (confirmAction("Are you sure you want to clear all selections?")) {
                fileItems.clear();
            }
        });

        // Event Handler for Select Base Directory Button
        selectBaseButton.setOnAction(e -> selectBaseDirectory(primaryStage));

        // Toggle Group Listener for Path Representation Options
        ToggleGroup pathToggleGroup = new ToggleGroup();
        absolutePathRadio.setToggleGroup(pathToggleGroup);
        relativePathRadio.setToggleGroup(pathToggleGroup);

        pathToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == relativePathRadio) {
                relativeBaseField.setDisable(false);
                selectBaseButton.setDisable(false);
            } else {
                relativeBaseField.setDisable(true);
                selectBaseButton.setDisable(true);
            }
        });

        // Event Handler for Combine Button
        combineButton.setOnAction(e -> {
            try {
                combineFiles(primaryStage);
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to combine files: " + ex.getMessage());
            }
        });
    }

    /**
     * Opens a FileChooser to select multiple files and adds them to the table.
     */
    private void selectFiles(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");
        // Apply file type filter if specified
        String filterText = fileTypeFilterField.getText().trim();
        if (!filterText.isEmpty()) {
            String[] extensions = filterText.split(",");
            List<String> extensionList = Arrays.stream(extensions)
                    .map(ext -> ext.trim())
                    .filter(ext -> ext.startsWith("."))
                    .collect(Collectors.toList());
            if (!extensionList.isEmpty()) {
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                        "Supported Files (" + String.join(", ", extensionList) + ")", extensionList);
                fileChooser.getExtensionFilters().add(extFilter);
            }
        }
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            selectedFiles.forEach(this::addFileItem);
        }
    }

    /**
     * Opens a custom folder selection dialog that supports multiple folder selection with Ctrl+click.
     */
    private void selectFolders(Stage stage) {
        // Create a custom dialog for folder selection
        Dialog<List<File>> dialog = new Dialog<>();
        dialog.setTitle("Select Folders");
        dialog.setHeaderText("Select multiple folders using Ctrl+click");
        dialog.getDialogPane().setPrefSize(800, 500);

        // Set the button types
        ButtonType selectButtonType = ButtonType.OK;
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);

        // Create the main layout
        BorderPane pane = new BorderPane();

        // Current path indicator
        TextField currentPathField = new TextField();
        currentPathField.setEditable(false);

        // Up button
        Button upButton = new Button("⬆ Up");

        // Path navigation controls
        HBox navigationBox = new HBox(10, new Label("Location:"), currentPathField, upButton);
        navigationBox.setPadding(new Insets(5));
        navigationBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(currentPathField, Priority.ALWAYS);

        // File system list view (using String representations instead of custom objects)
        ListView<String> folderListView = new ListView<>();
        folderListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set up the main layout
        pane.setTop(navigationBox);
        pane.setCenter(folderListView);

        dialog.getDialogPane().setContent(pane);

        // Map to store the mapping between display strings and actual File objects
        Map<String, File> folderMap = new HashMap<>();

        // Keep track of the current directory
        final File[] currentDirectory = new File[1];
        currentDirectory[0] = new File(System.getProperty("user.home"));

        // Function to update the folder list view
        Runnable updateFolderList = () -> {
            folderListView.getItems().clear();
            folderMap.clear();
            currentPathField.setText(currentDirectory[0].getAbsolutePath());

            File[] files = currentDirectory[0].listFiles();
            if (files != null) {
                List<String> items = new ArrayList<>();

                // Add directories
                for (File file : files) {
                    if (file.isDirectory() && !file.isHidden() && file.canRead()) {
                        String displayName = "📁 " + file.getName();
                        items.add(displayName);
                        folderMap.put(displayName, file);
                    }
                }

                // Sort by name
                Collections.sort(items);

                folderListView.getItems().addAll(items);
            }
        };

        // Initialize the folder list
        updateFolderList.run();

        // Handle up button click
        upButton.setOnAction(e -> {
            File parent = currentDirectory[0].getParentFile();
            if (parent != null) {
                currentDirectory[0] = parent;
                updateFolderList.run();
            }
        });

        // Handle double-click on a folder to navigate into it
        folderListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = folderListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    File selectedFile = folderMap.get(selectedItem);
                    if (selectedFile != null && selectedFile.isDirectory()) {
                        currentDirectory[0] = selectedFile;
                        updateFolderList.run();
                    }
                }
            }
        });

        // Set the result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                List<File> selectedFolders = new ArrayList<>();
                for (String item : folderListView.getSelectionModel().getSelectedItems()) {
                    File folder = folderMap.get(item);
                    if (folder != null && folder.isDirectory()) {
                        selectedFolders.add(folder);
                    }
                }
                return selectedFolders;
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<List<File>> result = dialog.showAndWait();
        result.ifPresent(folders -> {
            if (!folders.isEmpty()) {
                folders.forEach(this::addFolderItem);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Selection", "No folders were selected.");
            }
        });
    }

    /**
     * Adds a file to the table after validating its readability and supported extension.
     */
    private void addFileItem(File file) {
        if (file.isFile()) {
            if (!file.canRead()) {
                showAlert(Alert.AlertType.WARNING, "Permission Denied", "Cannot read file: " + file.getAbsolutePath());
                return;
            }
            if (!isSupportedFile(file)) {
                showAlert(Alert.AlertType.WARNING, "Unsupported File Type", "File type not supported: " + file.getName());
                return;
            }
            // Avoid duplicates
            boolean exists = fileItems.stream().anyMatch(item -> item.getPath().equals(file.getAbsolutePath()));
            if (!exists) {
                fileItems.add(new FileItem(file.getAbsolutePath(), "File"));
            }
        }
    }

    /**
     * Adds a folder to the table and processes its files based on the file type filter.
     */
    private void addFolderItem(File folder) {
        if (folder.isDirectory()) {
            // Avoid duplicates
            boolean exists = fileItems.stream().anyMatch(item -> item.getPath().equals(folder.getAbsolutePath()));
            if (!exists) {
                fileItems.add(new FileItem(folder.getAbsolutePath(), "Folder"));
            }
        }
    }

    /**
     * Opens a DirectoryChooser to select the base directory for relative paths.
     */
    private void selectBaseDirectory(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Base Directory for Relative Path");
        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            baseDirectory = selectedDir;
            relativeBaseField.setText(baseDirectory.getAbsolutePath());
        }
    }

    /**
     * Combines the selected files and folders into a single output file.
     */
    private void combineFiles(Stage stage) throws IOException {
        if (fileItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select files or folders to combine.");
            return;
        }

        // Determine path representation
        boolean useAbsolutePath = absolutePathRadio.isSelected();
        if (!useAbsolutePath) {
            if (baseDirectory == null) {
                showAlert(Alert.AlertType.WARNING, "Base Directory Required", "Please select a base directory for relative paths.");
                return;
            }
            if (!baseDirectory.isDirectory() || !baseDirectory.canRead()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Base Directory", "The selected base directory is invalid or unreadable.");
                return;
            }
        }

        // Choose output file location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Combined File");
        fileChooser.setInitialFileName("combined.txt");
        File outputFile = fileChooser.showSaveDialog(stage);
        if (outputFile == null) {
            return; // User cancelled
        }

        // Check if output file exists
        if (outputFile.exists()) {
            if (!confirmAction("The file \"" + outputFile.getName() + "\" already exists. Do you want to overwrite it?")) {
                return;
            }
        }

        // Get selected encoding
        String selectedEncoding = encodingComboBox.getValue();
        Charset charset;
        try {
            charset = Charset.forName(selectedEncoding);
        } catch (UnsupportedCharsetException e) {
            showAlert(Alert.AlertType.ERROR, "Unsupported Encoding", "The selected encoding is not supported.");
            return;
        }

        // Get file type filters
        String filterText = fileTypeFilterField.getText().trim();
        Set<String> activeExtensions = new HashSet<>();
        if (!filterText.isEmpty()) {
            activeExtensions = Arrays.stream(filterText.split(","))
                    .map(ext -> ext.trim().toLowerCase())
                    .filter(ext -> ext.startsWith("."))
                    .collect(Collectors.toSet());
            if (activeExtensions.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Invalid File Type Filter", "Please enter valid file extensions starting with a dot.");
                return;
            }
        }

        // Get recursive depth
        int maxDepth = depthSpinner.getValue();

        // Disable UI components during processing
        disableUI(true);

        // Create a Task to perform the file combination in the background
        Set<String> finalActiveExtensions = activeExtensions;
        Task<Void> combineTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath(), charset)) {
                    int totalItems = fileItems.size();
                    int processedItems = 0;

                    for (FileItem item : fileItems) {
                        if (isCancelled()) {
                            break;
                        }

                        try {
                            processFileItem(item, writer, useAbsolutePath, finalActiveExtensions, maxDepth);
                        } catch (IOException e) {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to process: " + item.getPath()));
                        }

                        processedItems++;
                        updateProgress(processedItems, totalItems);
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to write to output file: " + e.getMessage()));
                }

                return null;
            }
        };

        // Bind the progress bar to the task's progress
        progressBar.progressProperty().bind(combineTask.progressProperty());
        progressBar.setVisible(true);

        combineTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            disableUI(false);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Files have been combined successfully.");
        });

        combineTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            disableUI(false);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to combine files: " + combineTask.getException().getMessage());
        });

        // Start the task in a new thread
        new Thread(combineTask).start();
    }

    /**
     * Processes a single FileItem, writing its content to the combined file.
     */
    private void processFileItem(FileItem item, BufferedWriter writer, boolean useAbsolutePath,
                                 Set<String> activeExtensions, int maxDepth) throws IOException {
        if (item.getType().equals("File")) {
            File file = new File(item.getPath());
            writeFile(writer, file, useAbsolutePath);
        } else if (item.getType().equals("Folder")) {
            File folder = new File(item.getPath());
            processFolder(writer, folder.toPath(), useAbsolutePath, activeExtensions, maxDepth, 0);
        }
    }

    /**
     * Recursively processes a folder, writing each file's content to the combined file.
     */
    private void processFolder(BufferedWriter writer, Path folderPath, boolean useAbsolutePath,
                               Set<String> activeExtensions, int maxDepth, int currentDepth) throws IOException {
        if (currentDepth > maxDepth) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    processFolder(writer, entry, useAbsolutePath, activeExtensions, maxDepth, currentDepth + 1);
                } else if (Files.isRegularFile(entry) && Files.isReadable(entry)) {
                    File file = entry.toFile();
                    if (activeExtensions.isEmpty() || activeExtensions.contains(getFileExtension(file.getName()))) {
                        writeFile(writer, file, useAbsolutePath);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to process folder: " + folderPath.toString(), e);
        }
    }

    /**
     * Writes the content of a file to the combined file with a header.
     */
    private void writeFile(BufferedWriter writer, File file, boolean useAbsolutePath) throws IOException {
        String pathToWrite;
        if (useAbsolutePath) {
            pathToWrite = file.getAbsolutePath();
        } else {
            Path basePath = baseDirectory.toPath().toAbsolutePath().normalize();
            Path filePath = file.toPath().toAbsolutePath().normalize();
            if (!filePath.startsWith(basePath)) {
                throw new SecurityException("File path " + filePath + " is outside the base directory " + basePath);
            }
            pathToWrite = basePath.relativize(filePath).toString();
        }

        writer.write("----- " + pathToWrite + " -----\n");

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (MalformedInputException e) {
            throw new IOException("Unsupported encoding in file: " + file.getAbsolutePath(), e);
        }

        writer.write("\n\n");
    }

    /**
     * Checks if a file has a supported extension.
     */
    private boolean isSupportedFile(File file) {
        String name = file.getName().toLowerCase();
        return supportedExtensions.contains(getFileExtension(name));
    }

    /**
     * Extracts the file extension from a file name.
     */
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(index) : "";
    }

    /**
     * Displays an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
    }

    /**
     * Shows a confirmation dialog and returns true if the user confirms.
     */
    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Disables or enables UI components during processing.
     */
    private void disableUI(boolean disable) {
        tableView.setDisable(disable);
        menuBar.setDisable(disable);
        combineButton.setDisable(disable);
        selectFilesButton.setDisable(disable);
        selectFoldersButton.setDisable(disable);
        clearSelectionButton.setDisable(disable);
        absolutePathRadio.setDisable(disable);
        relativePathRadio.setDisable(disable);
        relativeBaseField.setDisable(disable || !relativePathRadio.isSelected());
        selectBaseButton.setDisable(disable || !relativePathRadio.isSelected());
        encodingComboBox.setDisable(disable);
        fileTypeFilterField.setDisable(disable);
        depthSpinner.setDisable(disable);
    }

    /**
     * Shows the "How to Use" dialog.
     */
    private void showHowToUseDialog() {
        String instructions = "### How to Use File Combiner App\n\n" +
                "1. **Select Files:** Click on the 'Select Files' button to choose individual files you want to combine.\n" +
                "2. **Select Folders:** Click on the 'Select Folders' button to choose folders. All readable files within these folders (up to the specified recursive depth) will be included.\n" +
                "3. **File Type Filter:** Specify the file extensions to include (e.g., `.txt,.java`). If left empty, all supported file types will be included.\n" +
                "4. **Path Representation:** Choose between 'Absolute Path' and 'Relative Path'. For relative paths, select a base directory.\n" +
                "5. **Encoding:** Select the desired encoding for reading and writing files.\n" +
                "6. **Recursive Depth:** Set how deep the application should traverse subfolders.\n" +
                "7. **Combine Files:** Click the 'Combine Files' button to start the process. A progress bar will indicate the progress.\n" +
                "8. **Drag and Drop:** You can also drag and drop files or folders directly into the application window.\n" +
                "9. **Clear Selection:** Click the 'Clear Selection' button to remove all selected files and folders.\n\n" +
                "### Notes:\n" +
                "- Ensure you have read permissions for all selected files and folders.\n" +
                "- The output file will contain headers indicating the source of each combined section.\n" +
                "- Supported file types can be modified in the source code by updating the `supportedExtensions` set.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Use");
        alert.setHeaderText(null);
        Label label = new Label(instructions);
        label.setWrapText(true);
        ScrollPane scrollPane = new ScrollPane(label);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        alert.getDialogPane().setContent(scrollPane);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Shows the "About" dialog.
     */
    private void showAboutDialog() {
        String aboutText = "### File Combiner App\n\n" +
                "Version: 1.0\n" +
                "Author: Zakaria Farih\n\n" +
                "This application allows you to combine multiple files and folders into a single file. " +
                "You can choose to represent file paths as absolute or relative, select the desired encoding, " +
                "and set the depth for recursive folder traversal.\n\n" +
                "© 2024 Zakaria Farih. All rights reserved.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        Label label = new Label(aboutText);
        label.setWrapText(true);
        ScrollPane scrollPane = new ScrollPane(label);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        alert.getDialogPane().setContent(scrollPane);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Shows a welcome dialog when the application starts.
     */
    private void showWelcomeDialog() {
        String welcomeText = "### Welcome to File Combiner App!\n\n" +
                "This application helps you combine multiple files and folders into a single file. " +
                "You can select individual files or entire folders, choose how to represent file paths, " +
                "and specify various options to tailor the output to your needs.\n\n" +
                "Use the menu bar or the buttons to get started.";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Welcome");
        alert.setHeaderText(null);
        Label label = new Label(welcomeText);
        label.setWrapText(true);
        ScrollPane scrollPane = new ScrollPane(label);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        alert.getDialogPane().setContent(scrollPane);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Represents a file or folder item in the TableView.
     */
    public static class FileItem {
        private final SimpleStringProperty path;
        private final SimpleStringProperty type;

        public FileItem(String path, String type) {
            this.path = new SimpleStringProperty(path);
            this.type = new SimpleStringProperty(type);
        }

        public String getPath() {
            return path.get();
        }

        public void setPath(String path) {
            this.path.set(path);
        }

        public String getType() {
            return type.get();
        }

        public void setType(String type) {
            this.type.set(type);
        }
    }
}
