# CopySpider

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Description

**CopySpider** is a JavaFX-based desktop application developed by me. It allows users to efficiently combine multiple files and folders into a single consolidated file. With an intuitive graphical user interface, users can select individual files or entire directories, apply various filters, and customize the output according to their needs.

## Features

- **File and Folder Selection:** Easily select multiple files and folders to include in the combination process.
- **Drag and Drop Support:** Drag and drop files or folders directly into the application window.
- **File Type Filtering:** Specify file extensions to include only the desired file types.
- **Path Representation:** Choose between absolute paths or relative paths based on a selected base directory.
- **Encoding Options:** Select from multiple encoding formats such as UTF-8, ISO-8859-1, and US-ASCII.
- **Recursive Depth Control:** Define the depth for folder traversal to include nested directories.
- **Progress Monitoring:** Visual progress bar to monitor the status of the file combination process.
- **User-Friendly Interface:** Intuitive buttons, menus, and dialogs to enhance user experience.
- **Customization:** Easily modify supported file types and other settings within the source code.

## Installation

### Prerequisites

- **Java Development Kit (JDK) 17 or higher:** [Download JDK](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- **JavaFX SDK 17.0.2:** [Download JavaFX](https://gluonhq.com/products/javafx/)

### Steps

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/zakariafarih/copySpider.git
   ```

2. **Navigate to the Project Directory:**

   ```bash
   cd copySpider
   ```

3. **Set Up JavaFX:**

   Ensure that the JavaFX SDK is correctly set up in your development environment. You can refer to the [JavaFX Getting Started Guide](https://openjfx.io/openjfx-docs/) for detailed instructions.

4. **Build the Project:**

   Use your preferred IDE (e.g., IntelliJ IDEA, Eclipse) to import the project and build it. Make sure to include the JavaFX libraries in your project's module path.

5. **Run the Application:**

   Execute the `FileCombinerApp` class to launch the application.

## Usage

1. **Launching the Application:**

   After running the `FileCombinerApp`, a welcome dialog will appear. Click "OK" to proceed to the main interface.

2. **Selecting Files and Folders:**

   - **Select Files:** Click the "Select Files" button to choose individual files.
   - **Select Folders:** Click the "Select Folders" button to choose directories. You can select multiple folders by confirming additional selections when prompted.
   - **Drag and Drop:** Alternatively, drag files or folders into the application window to add them to the selection list.

3. **Filtering Files:**

   - In the "File Type Filter" field, enter the desired file extensions separated by commas (e.g., `.txt,.java`).
   - The application will only include files matching these extensions during the combination process.

4. **Path Representation:**

   - **Absolute Path:** Select this option to include full file paths in the combined output.
   - **Relative Path:** Select this option and choose a base directory to include paths relative to the selected base.

5. **Encoding Options:**

   - Choose the desired encoding format from the dropdown menu to ensure proper reading and writing of file contents.

6. **Recursive Depth Control:**

   - Use the spinner to set the maximum depth for folder traversal. This determines how deeply the application will search through nested directories.

7. **Combining Files:**

   - Click the "Combine Files" button to start the process.
   - A progress bar will display the ongoing status.
   - Upon completion, a confirmation dialog will notify you of the successful combination.

8. **Clearing Selections:**

   - Click the "Clear Selection" button to remove all selected files and folders from the list.

9. **Menu Options:**

   - **File Menu:**
     - **Select Files/Folders:** Alternative way to add files or folders.
     - **Clear Selection:** Remove all selections.
     - **Exit:** Close the application.
   - **Help Menu:**
     - **How to Use:** Detailed instructions on using the application.
     - **About:** Information about the application and the author.

## Contributing

Contributions are welcome! Please follow these steps to contribute:

1. **Fork the Repository:**

   Click the "Fork" button at the top-right corner of this repository to create a personal copy.

2. **Clone Your Fork:**

   ```bash
   git clone https://github.com/your-username/copySpider.git
   ```

3. **Create a New Branch:**

   ```bash
   git checkout -b feature/YourFeatureName
   ```

4. **Make Your Changes:**

   Implement your feature or bug fix.

5. **Commit Your Changes:**

   ```bash
   git commit -m "Add your commit message"
   ```

6. **Push to Your Fork:**

   ```bash
   git push origin feature/YourFeatureName
   ```

7. **Submit a Pull Request:**

   Navigate to the original repository and click "New Pull Request" to submit your changes for review.

## License

This project is licensed under the [MIT License](LICENSE).

## Contact

**Zakaria Farih**  
Email: zakariafarih142@gmail.com  
GitHub: [https://github.com/zakariafarih](https://github.com/AllMightyyyy)
