import pandas as pd
import matplotlib.pyplot as plt

# Wczytanie danych
df = pd.read_csv("training_data.csv")

# Oblicz średnią kroczącą (np. okno = 10 rund)
window_size = 10
df["RollingAvgReward"] = df["CumulativeReward"].rolling(window=window_size).mean()

# Tworzenie dwóch wykresów obok siebie
fig, (ax1, ax2) = plt.subplots(2, 1, figsize=(12, 8), sharex=True)

# Wykres 1: Cumulative Reward z punktami wygranych
ax1.plot(df["Round"], df["CumulativeReward"], label="Reward", color="orange", marker="o")
wins_df = df[df["Won"] == 1]
ax1.scatter(wins_df["Round"], wins_df["CumulativeReward"], color="blue", label="Win", zorder=5)
ax1.set_ylabel("Cumulative Reward")
ax1.set_title("Training Progress: Reward Over Time")
ax1.legend()
ax1.grid(True)

# Wykres 2: Średnia krocząca
ax2.plot(df["Round"], df["RollingAvgReward"], label=f"Rolling Avg Reward (window={window_size})", color="green")
ax2.set_xlabel("Round")
ax2.set_ylabel("Rolling Avg")
ax2.set_title("Rolling Average of Reward")
ax2.legend()
ax2.grid(True)

# Automatyczne dopasowanie układu
plt.tight_layout()

# Wyświetlenie i zapis
plt.show()
fig.savefig("training_progress_combined.png", dpi=300)
fig.savefig("training_progress_combined.pdf")
