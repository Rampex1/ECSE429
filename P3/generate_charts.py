import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Set style
sns.set_style("whitegrid")
plt.rcParams["figure.figsize"] = (14, 10)

# Read the CSV files
test_results = pd.read_csv("test_results.csv")
system_monitor = pd.read_csv("performance_log.csv")

# Create a figure with multiple subplots
fig, axes = plt.subplots(2, 2, figsize=(16, 12))
fig.suptitle("TODO API Performance Analysis", fontsize=16, fontweight="bold")

# 1. Average Time vs Object Count (by Operation)
ax1 = axes[0, 0]
for operation in test_results["Operation"].unique():
    data = test_results[test_results["Operation"] == operation]
    ax1.plot(
        data["ObjectCount"],
        data["AvgTime_ms"],
        marker="o",
        label=operation,
        linewidth=2,
    )
ax1.set_xlabel("Number of Objects", fontsize=12)
ax1.set_ylabel("Average Time (ms)", fontsize=12)
ax1.set_title("Average Operation Time vs Object Count", fontsize=14, fontweight="bold")
ax1.legend()
ax1.grid(True, alpha=0.3)

# 2. Total Time vs Object Count (by Operation)
ax2 = axes[0, 1]
for operation in test_results["Operation"].unique():
    data = test_results[test_results["Operation"] == operation]
    ax2.plot(
        data["ObjectCount"],
        data["TotalTime_ms"],
        marker="s",
        label=operation,
        linewidth=2,
    )
ax2.set_xlabel("Number of Objects", fontsize=12)
ax2.set_ylabel("Total Time (ms)", fontsize=12)
ax2.set_title("Total Operation Time vs Object Count", fontsize=14, fontweight="bold")
ax2.legend()
ax2.grid(True, alpha=0.3)

# 3. Memory Usage
ax3 = axes[1, 0]
ax3.plot(
    test_results["ObjectCount"],
    test_results["UsedMemory_MB"],
    marker="o",
    label="Used Memory",
    linewidth=2,
    color="red",
)
ax3.plot(
    test_results["ObjectCount"],
    test_results["FreeMemory_MB"],
    marker="s",
    label="Free Memory",
    linewidth=2,
    color="green",
)
ax3.set_xlabel("Number of Objects", fontsize=12)
ax3.set_ylabel("Memory (MB)", fontsize=12)
ax3.set_title("JVM Memory Usage vs Object Count", fontsize=14, fontweight="bold")
ax3.legend()
ax3.grid(True, alpha=0.3)

# 4. System CPU Over Time
ax4 = axes[1, 1]
system_monitor["Timestamp"] = pd.to_datetime(system_monitor["Timestamp"])
system_monitor["Seconds"] = (
    system_monitor["Timestamp"] - system_monitor["Timestamp"].min()
).dt.total_seconds()
ax4.plot(system_monitor["Seconds"], system_monitor["CPU%"], linewidth=2, color="purple")
ax4.set_xlabel("Time (seconds)", fontsize=12)
ax4.set_ylabel("CPU Usage (%)", fontsize=12)
ax4.set_title("System CPU Usage Over Time", fontsize=14, fontweight="bold")
ax4.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig("performance_analysis.png", dpi=300, bbox_inches="tight")
print("Chart saved as 'performance_analysis.png'")

# Create additional detailed charts
fig2, axes2 = plt.subplots(1, 3, figsize=(18, 5))
fig2.suptitle("Operation Comparison", fontsize=16, fontweight="bold")

operations = test_results["Operation"].unique()
colors = ["#FF6B6B", "#4ECDC4", "#45B7D1"]

for idx, operation in enumerate(operations):
    data = test_results[test_results["Operation"] == operation]
    axes2[idx].bar(
        data["ObjectCount"].astype(str),
        data["AvgTime_ms"],
        color=colors[idx],
        alpha=0.7,
    )
    axes2[idx].set_xlabel("Object Count", fontsize=11)
    axes2[idx].set_ylabel("Avg Time (ms)", fontsize=11)
    axes2[idx].set_title(f"{operation} Performance", fontsize=13, fontweight="bold")
    axes2[idx].tick_params(axis="x", rotation=45)
    axes2[idx].grid(True, alpha=0.3, axis="y")

plt.tight_layout()
plt.savefig("operation_comparison.png", dpi=300, bbox_inches="tight")
print("Chart saved as 'operation_comparison.png'")

# Print summary statistics
print("\n" + "=" * 60)
print("PERFORMANCE SUMMARY")
print("=" * 60)
print(test_results.groupby("Operation")[["AvgTime_ms", "TotalTime_ms"]].describe())
