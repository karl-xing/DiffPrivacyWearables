# DiffPrivacyWearables

## Overview

DiffPrivacyWearables is an Android application designed to evaluate the performance of privacy-preserving algorithms, specifically k-Anonymity and Local Differential Privacy (LDP), on wearable devices such as those running WearOS. The project includes a toolkit for testing and analyzing these algorithms in resource-constrained environments, focusing on computational efficiency, memory usage, and real-time applicability.

## Features

- **Privacy-Preserving Algorithms**: Supports the implementation and evaluation of k-Anonymity and Local Differential Privacy (LDP) algorithms.
- **Automated Testing**: Provides automated testing functionality to measure the performance of these algorithms with various datasets and parameters.
- **Performance Metrics**: Captures key metrics such as computational time, memory usage, and (in future updates) CPU usage and battery consumption.
- **Data Visualization**: Includes Python scripts for visualizing performance metrics using line plots with error bars.

## Project Structure

```plaintext
DiffPrivacyWearables/
│
├── app/                    # Contains the main Android application source code
│   ├── src/
│   └── res/
│
├── scripts/                # Contains Python scripts for data analysis and visualization
│   ├── plot_box_ldp.py     # Script for generating box plots for LDP algorithm results
│   └── plot_bar_k.py       # Script for generating bar charts for k-Anonymity results
│
├── .gitignore              # Git ignore file
├── build.gradle            # Project build configuration
└── README.md               # This README file
```

## Getting Started

### Prerequisites

- **Android Studio**: The project is developed using Android Studio. Make sure you have the latest version installed.
- **WearOS Device or Emulator**: You need a WearOS device or emulator to run the application.
- **Python 3.x**: Required to run the data visualization scripts.

### Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/karl-xing/DiffPrivacyWearables.git
   cd DiffPrivacyWearables
   ```

2. **Open the project in Android Studio**:

    - Launch Android Studio.
    - Click on `File > Open...` and select the `DiffPrivacyWearables` directory.

3. **Build the project**:

    - Click on the `Build` menu and select `Build Project`.

4. **Run on Emulator/Device**:

    - Ensure you have a WearOS emulator or device connected.
    - Click on the `Run` button or select `Run > Run 'app'`.

### Python Scripts

The project includes Python scripts for visualizing the performance metrics:

- **Running the scripts**:

  Navigate to the `scripts/` directory and run the scripts with Python:

  ```bash
  cd scripts
  python plot_box_ldp.py
  python plot_bar_k.py
  ```

  These scripts require the following Python libraries:

    - `pandas`
    - `numpy`
    - `matplotlib`
    - `seaborn`

  You can install these using pip:

  ```bash
  pip install pandas numpy matplotlib seaborn
  ```

## Usage

- **Automated Testing**: Use the `TestActivity` class to run automated tests on different datasets and parameters. The results will be logged using Android's logcat.

- **Performance Metrics Visualization**: Use the Python scripts to analyze the log data and generate visualizations for computational time and memory usage.


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

