"""Generates item models, language file, recipes and tags for More Tools.
Run:  python3 tools/gen_data.py
"""
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
MOD = "moretools"

TIER_MATS = {  # name -> crafting ingredient
    "emerald": "minecraft:emerald",
    "obsidian": "minecraft:obsidian",
    "amethyst": "minecraft:amethyst_shard",
}
BIG_MATS = {
    "iron": "minecraft:iron_ingot",
    "diamond": "minecraft:diamond",
}
TOOLS = ["sword", "pickaxe", "axe", "shovel", "hoe"]
BIG_TOOLS = ["hammer", "excavator", "lumber_axe"]

TOOL_PATTERNS = {
    "sword": ["X", "X", "#"],
    "pickaxe": ["XXX", " # ", " # "],
    "axe": ["XX", "X#", " #"],
    "shovel": ["X", "#", "#"],
    "hoe": ["XX", " #", " #"],
    "hammer": ["XXX", "X#X", " # "],
    "excavator": [" X ", "X#X", " # "],
    "lumber_axe": ["XXX", "X# ", " # "],
}


def write(path, data):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def title(name):
    return " ".join(w.capitalize() for w in name.split("_"))


items = []
for m in TIER_MATS:
    for t in TOOLS:
        items.append(f"{m}_{t}")
for m in ("iron", "diamond", "netherite"):
    for t in BIG_TOOLS:
        items.append(f"{m}_{t}")
items += ["diamond_paxel", "netherite_paxel"]

# --- models + item definitions + lang ---
lang = {"itemGroup.moretools": "More Tools"}
for name in items:
    write(f"assets/{MOD}/models/item/{name}.json", {
        "parent": "minecraft:item/handheld",
        "textures": {"layer0": f"{MOD}:item/{name}"},
    })
    write(f"assets/{MOD}/items/{name}.json", {
        "model": {"type": "minecraft:model", "model": f"{MOD}:item/{name}"},
    })
    lang[f"item.{MOD}.{name}"] = title(name)
write(f"assets/{MOD}/lang/en_us.json", lang)


# --- recipes ---
def shaped(name, mat, tool):
    write(f"data/{MOD}/recipe/{name}.json", {
        "type": "minecraft:crafting_shaped",
        "category": "equipment",
        "key": {"#": "minecraft:stick", "X": mat},
        "pattern": TOOL_PATTERNS[tool],
        "result": {"id": f"{MOD}:{name}"},
    })


for m, ing in TIER_MATS.items():
    for t in TOOLS:
        shaped(f"{m}_{t}", ing, t)
for m, ing in BIG_MATS.items():
    for t in BIG_TOOLS:
        shaped(f"{m}_{t}", ing, t)

write(f"data/{MOD}/recipe/diamond_paxel.json", {
    "type": "minecraft:crafting_shapeless",
    "category": "equipment",
    "ingredients": ["minecraft:diamond_pickaxe", "minecraft:diamond_axe", "minecraft:diamond_shovel"],
    "result": {"id": f"{MOD}:diamond_paxel"},
})

for t in BIG_TOOLS + ["paxel"]:
    write(f"data/{MOD}/recipe/netherite_{t}_smithing.json", {
        "type": "minecraft:smithing_transform",
        "addition": "#minecraft:netherite_tool_materials",
        "base": f"{MOD}:diamond_{t}",
        "result": {"id": f"{MOD}:netherite_{t}"},
        "template": "minecraft:netherite_upgrade_smithing_template",
    })


# --- tags ---
def tag(path, values):
    write(path, {"replace": False, "values": values})


tag(f"data/{MOD}/tags/block/mineable/paxel.json",
    ["#minecraft:mineable/pickaxe", "#minecraft:mineable/axe", "#minecraft:mineable/shovel"])
for m, ing in TIER_MATS.items():
    tag(f"data/{MOD}/tags/item/{m}_tool_materials.json", [ing])

ids = lambda names: [f"{MOD}:{n}" for n in names]
paxels = ["diamond_paxel", "netherite_paxel"]
tag("data/minecraft/tags/item/swords.json", ids([f"{m}_sword" for m in TIER_MATS]))
tag("data/minecraft/tags/item/pickaxes.json",
    ids([f"{m}_pickaxe" for m in TIER_MATS] + [f"{m}_hammer" for m in ("iron", "diamond", "netherite")] + paxels))
tag("data/minecraft/tags/item/axes.json",
    ids([f"{m}_axe" for m in TIER_MATS] + [f"{m}_lumber_axe" for m in ("iron", "diamond", "netherite")] + paxels))
tag("data/minecraft/tags/item/shovels.json",
    ids([f"{m}_shovel" for m in TIER_MATS] + [f"{m}_excavator" for m in ("iron", "diamond", "netherite")] + paxels))
tag("data/minecraft/tags/item/hoes.json", ids([f"{m}_hoe" for m in TIER_MATS]))

print(f"generated data for {len(items)} items")
