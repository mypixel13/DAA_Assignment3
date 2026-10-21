"""Reads results/results.csv, draws the three plots and prints the numbers used in REPORT.md.

Run from the project root:  python3 scripts/analyze.py
"""
import csv
import math
import os
import sys

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

CSV_PATH = "results/results.csv"
PLOT_DIR = "results/plots"
OUT_MD = "results/analysis.md"

# categorical colours in fixed order, one per algorithm (same colour in every plot)
COLORS = {
    "Kruskal": "#2a78d6",
    "Prim": "#eb6834",
    "Kosaraju": "#1baf7a",
    "Kahn": "#6250d6",
    "DagShortestPath": "#e87ba4",
}
STYLE = {"sparse": "-", "dense": "--"}
MARKER = {"sparse": "o", "dense": "s"}
INK = "#0b0b0b"
MUTED = "#52514e"
GRID = "#e4e3df"


def load():
    rows = []
    with open(CSV_PATH) as f:
        for r in csv.DictReader(f):
            r["density"] = r["graph"].split("_")[1]
            for k in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
                r[k] = int(r[k])
            r["time_ms"] = float(r["time_ms"])
            rows.append(r)
    return rows


def series(rows, algorithm, density):
    pts = sorted((r for r in rows if r["algorithm"] == algorithm and r["density"] == density),
                 key=lambda r: r["n"])
    return pts


def style_axes(ax, xlabel, ylabel, title):
    ax.set_title(title, loc="left", fontsize=12, color=INK)
    ax.set_xlabel(xlabel, color=MUTED)
    ax.set_ylabel(ylabel, color=MUTED)
    ax.grid(True, color=GRID, linewidth=0.8)
    ax.set_axisbelow(True)
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    for side in ("left", "bottom"):
        ax.spines[side].set_color(GRID)
    ax.tick_params(colors=MUTED)


def legend(ax, loc="best"):
    ax.legend(frameon=False, fontsize=8, labelcolor=INK, ncol=2, loc=loc)


def slope(xs, ys):
    return float(np.polyfit(np.log10(xs), np.log10(ys), 1)[0])


def plot_operations(rows):
    fig, ax = plt.subplots(figsize=(7.5, 5))
    # lines that coincide (Kosaraju and DagShortestPath do 2m operations) are drawn
    # with different widths, the first algorithm is the widest and sits at the bottom
    for i, (alg, color) in enumerate(COLORS.items()):
        for dens in ("sparse", "dense"):
            pts = series(rows, alg, dens)
            ns = [p["n"] for p in pts]
            ops = [p["dsu_ops"] + p["edge_scans"] + p["comparisons"] for p in pts]
            ax.plot(ns, ops, STYLE[dens], marker=MARKER[dens], markersize=9 - 1.4 * i,
                    linewidth=5.5 - 1.1 * i, color=color, label=f"{alg} ({dens})", zorder=3 + i)
    ax.set_xscale("log")
    ax.set_yscale("log")
    style_axes(ax, "n (vertices)", "operations = dsu_ops + edge_scans + comparisons",
               "Operations vs n")
    legend(ax)
    fig.tight_layout()
    fig.savefig(f"{PLOT_DIR}/1_operations_vs_n.png", dpi=150)
    plt.close(fig)


def plot_scans_per_edge(rows):
    fig, ax = plt.subplots(figsize=(7.5, 5))
    # Prim and Kosaraju both sit at 2.0, Kahn and DagShortestPath both at 1.0:
    # different widths make the hidden line visible
    for i, alg in enumerate(("Prim", "Kosaraju", "Kahn", "DagShortestPath")):
        for dens in ("sparse", "dense"):
            pts = series(rows, alg, dens)
            ax.plot([p["n"] for p in pts], [p["edge_scans"] / p["m"] for p in pts],
                    STYLE[dens], marker=MARKER[dens], markersize=10 - 2 * i,
                    linewidth=6 - 1.5 * i, color=COLORS[alg], label=f"{alg} ({dens})", zorder=3 + i)
    ax.set_xscale("log")
    ax.set_ylim(0, 2.6)
    style_axes(ax, "n (vertices)", "edge_scans / m",
               "Scans per edge (flat line = linear work)")
    legend(ax)
    fig.tight_layout()
    fig.savefig(f"{PLOT_DIR}/2_scans_per_edge.png", dpi=150)
    plt.close(fig)


def plot_kruskal_share(rows):
    fig, ax = plt.subplots(figsize=(7.5, 5))
    for dens in ("sparse", "dense"):
        pts = series(rows, "Kruskal", dens)
        ax.plot([p["n"] for p in pts], [p["edge_scans"] / p["m"] for p in pts],
                STYLE[dens], marker=MARKER[dens], markersize=6, linewidth=2,
                color=COLORS["Kruskal"], label=f"Kruskal ({dens}, m = {'4n' if dens == 'sparse' else '20n'})")
    ax.set_xscale("log")
    ax.set_ylim(0, 1.1)
    style_axes(ax, "n (vertices)", "edges examined / m",
               "Share of edges Kruskal examines before the tree is ready")
    ax.legend(frameon=False, fontsize=9, labelcolor=INK, loc="center right")
    fig.tight_layout()
    fig.savefig(f"{PLOT_DIR}/3_kruskal_edge_share.png", dpi=150)
    plt.close(fig)


def table(header, body):
    out = ["| " + " | ".join(header) + " |", "|" + "|".join("---" for _ in header) + "|"]
    for line in body:
        out.append("| " + " | ".join(line) + " |")
    return "\n".join(out)


def analysis(rows):
    md = []

    # ratios counter / formula
    for dens in ("sparse", "dense"):
        body = []
        for n in (1000, 3162, 10000, 31623, 100000):
            def get(alg):
                return next(r for r in rows if r["algorithm"] == alg and r["density"] == dens and r["n"] == n)
            k, p, s, h, d = (get(a) for a in ("Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"))
            E, V = k["m"], k["n"]
            body.append([
                str(n),
                f"{k['dsu_ops'] / E:.3f}",
                f"{k['dsu_ops'] / k['edge_scans']:.3f}",
                f"{k['comparisons'] / (E * math.log2(E)):.3f}",
                f"{p['edge_scans'] / (2 * E):.3f}",
                f"{p['comparisons'] / (E * math.log2(E)):.3f}",
                f"{s['edge_scans'] / (V + E):.3f}",
                f"{h['edge_scans'] / (V + E):.3f}",
                f"{d['edge_scans'] / (V + E):.3f}",
            ])
        md.append(f"### Counter / formula, {dens}\n")
        md.append(table(["n", "Kruskal dsuOps/E", "Kruskal dsuOps/scans", "Kruskal cmp/(E log2 E)",
                         "Prim scans/2E", "Prim cmp/(E log2 E)",
                         "Kosaraju scans/(V+E)", "Kahn scans/(V+E)", "DAG scans/(V+E)"], body))
        md.append("")

    # slopes of edge_scans vs n on log-log axes
    body = []
    for alg in COLORS:
        for dens in ("sparse", "dense"):
            pts = series(rows, alg, dens)
            ns = [p["n"] for p in pts]
            body.append([alg, dens,
                         f"{slope(ns, [p['edge_scans'] for p in pts]):.3f}",
                         f"{slope(ns, [p['dsu_ops'] + p['edge_scans'] + p['comparisons'] for p in pts]):.3f}"])
    md.append("### Log-log slopes\n")
    md.append(table(["algorithm", "density", "slope of edgeScans", "slope of total operations"], body))
    md.append("")

    # Kruskal share
    body = []
    for n in (1000, 3162, 10000, 31623, 100000):
        sp = next(r for r in rows if r["algorithm"] == "Kruskal" and r["density"] == "sparse" and r["n"] == n)
        de = next(r for r in rows if r["algorithm"] == "Kruskal" and r["density"] == "dense" and r["n"] == n)
        body.append([str(n), f"{sp['edge_scans'] / sp['m']:.3f}", f"{de['edge_scans'] / de['m']:.3f}"])
    md.append("### Share of edges Kruskal examines\n")
    md.append(table(["n", "sparse", "dense"], body))
    md.append("")

    # max depth
    body = []
    for dens in ("sparse", "dense"):
        for n in (1000, 3162, 10000, 31623, 100000):
            r = next(r for r in rows if r["algorithm"] == "Kosaraju" and r["density"] == dens and r["n"] == n)
            body.append([dens, str(n), str(r["max_depth"]), f"{r['max_depth'] / n:.3f}"])
    md.append("### Kosaraju maxDepth\n")
    md.append(table(["density", "n", "maxDepth", "maxDepth / n"], body))
    md.append("")

    # times for the 100 000 graphs
    body = []
    for alg in COLORS:
        row = [alg]
        for dens in ("sparse", "dense"):
            r = next(r for r in rows if r["algorithm"] == alg and r["density"] == dens and r["n"] == 100000)
            row.append(f"{r['time_ms']:.1f}")
        body.append(row)
    md.append("### Median time (ms) at n = 100 000\n")
    md.append(table(["algorithm", "sparse", "dense"], body))
    md.append("")

    # Tarjan vs Kosaraju
    tpath = "results/tarjan_vs_kosaraju.csv"
    if os.path.exists(tpath):
        body = []
        with open(tpath) as f:
            for r in csv.DictReader(f):
                body.append([r["graph"], r["m"], r["kosaraju_edge_scans"], r["tarjan_edge_scans"],
                             r["kosaraju_max_depth"], r["tarjan_max_depth"]])
        md.append("### Tarjan vs Kosaraju\n")
        md.append(table(["graph", "m", "Kosaraju scans", "Tarjan scans", "Kosaraju maxDepth", "Tarjan maxDepth"], body))
        md.append("")

    text = "\n".join(md)
    with open(OUT_MD, "w") as f:
        f.write(text)
    print(text)


def main():
    if not os.path.exists(CSV_PATH):
        sys.exit(f"{CSV_PATH} not found, run the benchmark first")
    os.makedirs(PLOT_DIR, exist_ok=True)
    rows = load()
    plot_operations(rows)
    plot_scans_per_edge(rows)
    plot_kruskal_share(rows)
    analysis(rows)


if __name__ == "__main__":
    main()
