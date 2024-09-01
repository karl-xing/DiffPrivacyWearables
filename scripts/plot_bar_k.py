import re
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns


log_file_path = 'Fossil5e_data2_k.log' 

# Read log and extract data
pattern = r'k = (\d+\.\d+), Computation Time: (\d+), Memory Usage: (\d+\.\d+)'
data = []

with open(log_file_path, 'r') as file:
    for line in file:
        match = re.search(pattern, line)
        if match:
            k = float(match.group(1))
            computation_time = int(match.group(2))
            memory_usage = abs(float(match.group(3)))
            data.append((k, computation_time, memory_usage))

# Convert extracted data to a DataFrame
df = pd.DataFrame(data, columns=['k', 'computation_time_ms', 'memory_usage_kb'])

# Calculate mean and standard deviation for each k
summary = df.groupby('k').agg(
    computation_time_mean=('computation_time_ms', 'mean'),
    computation_time_std=('computation_time_ms', 'std'),
    memory_usage_mean=('memory_usage_kb', 'mean'),
    memory_usage_std=('memory_usage_kb', 'std')
).reset_index()

# Plotting the line plot with error bars

# Plot for Computation Time
plt.figure(figsize=(12, 6))
plt.errorbar(summary['k'], summary['computation_time_mean'], 
             yerr=summary['computation_time_std'], fmt='-o', capsize=5, label='Computation Time (ms)')
plt.xlabel('k')
plt.ylabel('Computation Time (ms)')
plt.title('Computation Time vs k with Error Bars')
plt.legend()
plt.show()

# Plot for Memory Usage
plt.figure(figsize=(12, 6))
plt.errorbar(summary['k'], summary['memory_usage_mean'], 
             yerr=summary['memory_usage_std'], fmt='-o', capsize=5, label='Memory Usage (KB)')
plt.xlabel('k')
plt.ylabel('Memory Usage (KB)')
plt.title('Memory Usage vs k with Error Bars')
plt.legend()
plt.show()
