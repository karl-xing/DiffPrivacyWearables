import re
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns


log_file_path = 'Fossil5e_data11_ldp.log'

# Read the log file and extract data
pattern = r'epsilon = (\d+\.\d+), Computation Time: (\d+), Memory Usage: (\d+\.\d+)'
data = []

with open(log_file_path, 'r') as file:
    for line in file:
        match = re.search(pattern, line)
        if match:
            epsilon = float(match.group(1))
            computation_time = int(match.group(2))
            memory_usage = float(match.group(3))
            data.append((epsilon, computation_time, memory_usage))

# Convert extracted data to a DataFrame
df = pd.DataFrame(data, columns=['epsilon', 'computation_time_ms', 'memory_usage_kb'])

# Calculate mean and standard deviation for each epsilon
summary = df.groupby('epsilon').agg(
    computation_time_mean=('computation_time_ms', 'mean'),
    computation_time_std=('computation_time_ms', 'std'),
    memory_usage_mean=('memory_usage_kb', 'mean'),
    memory_usage_std=('memory_usage_kb', 'std')
).reset_index()

# Plotting the line plot with error bars

# Plot for Computation Time
plt.figure(figsize=(12, 6))
plt.errorbar(summary['epsilon'], summary['computation_time_mean'], 
             yerr=summary['computation_time_std'], fmt='-o', capsize=5, label='Computation Time (ms)')
plt.xlabel('Epsilon')
plt.ylabel('Computation Time (ms)')
plt.title('Computation Time vs Epsilon with Error Bars')
plt.legend()
plt.show()

# Plot for Memory Usage
plt.figure(figsize=(12, 6))
plt.errorbar(summary['epsilon'], summary['memory_usage_mean'], 
             yerr=summary['memory_usage_std'], fmt='-o', capsize=5, label='Memory Usage (KB)')
plt.xlabel('Epsilon')
plt.ylabel('Memory Usage (KB)')
plt.title('Memory Usage vs Epsilon with Error Bars')
plt.legend()
plt.show()
