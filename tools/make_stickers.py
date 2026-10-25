"""Draws the WhatsApp sticker pack into app/src/main/assets/stickers/likehome.

WhatsApp's rules: 512x512 WebP stickers under 100 KB, a 96x96 PNG tray icon under
50 KB, 3 to 30 stickers. Run from the repo root: python3 tools/make_stickers.py
"""
import math
import os
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FONTS = os.path.join(ROOT, "app/src/main/res/font")
OUT = os.path.join(ROOT, "app/src/main/assets/stickers/likehome")

INK = (42, 29, 20, 255)
SAFFRON = (242, 166, 90, 255)
CREAM = (255, 250, 242, 255)
WATER = (59, 130, 196, 255)

LOOKS = {
    "mom": dict(skin=(233, 185, 138, 255), hair=INK, bun=True, bindi=True),
    "papa": dict(skin=(217, 165, 119, 255), hair=(59, 46, 37, 255), short=True, glasses=True, moustache=True),
    "dadi": dict(skin=(230, 180, 140, 255), hair=(184, 178, 170, 255), bun=True, glasses=True),
    "boss": dict(skin=(240, 201, 160, 255), hair=(74, 59, 48, 255), short=True, tie=True),
    "abuela": dict(skin=(221, 166, 124, 255), hair=(216, 210, 203, 255), curls=True),
}

# (file, character, mood 0-3, text, emojis, extra)
STICKERS = [
    ("01_good_morning.webp", "mom", 0, "Good morning beta!", "☀️,😊", None),
    ("02_five_minutes.webp", "mom", 1, "5 minutes became 10", "⏰,😤", None),
    ("03_sharma_ji.webp", "mom", 2, "Sharma ji's son is already awake", "🙄,🏃", None),
    ("04_bucket.webp", "mom", 3, "Bucket of water. Coming.", "🪣,💦,😡", "bucket"),
    ("05_uth_jao.webp", "mom", 3, "UTH JAO!", "📢,😠", "comic"),
    ("06_good_child.webp", "mom", 0, "Good child!", "🥰,🌟", "stars"),
    ("07_papa.webp", "papa", 1, "I am not angry. Yet.", "😐,👨", None),
    ("08_dadi.webp", "dadi", 2, "Hai Ram, still sleeping?", "👵,😱", None),
    ("09_boss.webp", "boss", 1, "Per my last alarm…", "💼,📧", None),
    ("10_abuela.webp", "abuela", 3, "The chancla is coming", "👡,😬", None),
    ("11_snooze_king.webp", "mom", 2, "Snooze King", "👑,😴", "crown"),
    ("12_mom_has_spoken.webp", "mom", 3, "No more snoozing. Mom has spoken.", "🙅,⛔", None),
]


def font(name, size):
    return ImageFont.truetype(os.path.join(FONTS, name), size)


def face(d, cx, cy, r, who, mood):
    look = LOOKS[who]
    if look.get("bun"):
        d.ellipse([cx - r * 0.32, cy - r * 1.28, cx + r * 0.32, cy - r * 0.64], fill=look["hair"])
    if look.get("curls"):
        for i in range(5):
            x = cx - r * 0.8 + i * r * 0.4
            d.ellipse([x - r * 0.3, cy - r * 1.05, x + r * 0.3, cy - r * 0.45], fill=look["hair"])
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=look["skin"], outline=INK, width=8)
    if look.get("short"):
        d.chord([cx - r * 0.95, cy - r * 1.02, cx + r * 0.95, cy + r * 0.25], 185, 355, fill=look["hair"])
    elif not look.get("curls"):
        d.chord([cx - r * 1.0, cy - r * 1.05, cx + r * 1.0, cy + r * 0.5], 180, 360, fill=look["hair"])
    else:
        d.chord([cx - r * 0.95, cy - r * 1.0, cx + r * 0.95, cy + r * 0.3], 180, 360, fill=look["hair"])
    if look.get("bindi"):
        d.ellipse([cx - 9, cy - r * 0.3 - 9, cx + 9, cy - r * 0.3 + 9], fill=(217, 130, 43, 255))
    ey = cy + r * 0.08
    ex = r * 0.34
    for sx in (-1, 1):
        d.ellipse([cx + sx * ex - 11, ey - 11, cx + sx * ex + 11, ey + 11], fill=INK)
        if look.get("glasses"):
            d.ellipse([cx + sx * ex - 30, ey - 30, cx + sx * ex + 30, ey + 30], outline=INK, width=6)
        tilt = mood * 9
        d.line([cx + sx * (ex + 34), ey - 46 - tilt, cx + sx * (ex - 20), ey - 34], fill=INK, width=12)
    if look.get("glasses"):
        d.line([cx - ex + 30, ey, cx + ex - 30, ey], fill=INK, width=6)
    if look.get("moustache"):
        d.chord([cx - r * 0.32, cy + r * 0.28, cx + r * 0.32, cy + r * 0.56], 180, 360, fill=look["hair"])
    my = cy + r * 0.5
    if mood == 0:
        d.arc([cx - r * 0.32, my - r * 0.2, cx + r * 0.32, my + r * 0.2], 20, 160, fill=INK, width=10)
    elif mood == 1:
        d.line([cx - r * 0.26, my + 6, cx + r * 0.26, my + 6], fill=INK, width=10)
    else:
        open_h = r * (0.14 + mood * 0.06)
        d.ellipse([cx - r * 0.22, my - open_h / 2, cx + r * 0.22, my + open_h], fill=INK)
    if look.get("tie"):
        d.polygon([(cx - 18, cy + r * 0.95), (cx + 18, cy + r * 0.95), (cx, cy + r * 1.25)], fill=(180, 65, 58, 255))


def wrap(text, fnt, width, d):
    words, lines, line = text.split(), [], ""
    for w in words:
        test = (line + " " + w).strip()
        if d.textlength(test, font=fnt) <= width:
            line = test
        else:
            lines.append(line)
            line = w
    lines.append(line)
    return lines


def bubble(d, text, box, comic=False):
    x0, y0, x1, y1 = box
    fill = (255, 210, 63, 255) if comic else CREAM
    d.rounded_rectangle(box, radius=40, fill=fill, outline=INK, width=8)
    d.polygon([(x0 + 60, y1 - 4), (x0 + 120, y1 - 4), (x0 + 40, y1 + 50)], fill=fill, outline=None)
    d.line([(x0 + 60, y1 - 1), (x0 + 40, y1 + 50), (x0 + 120, y1 - 1)], fill=INK, width=8)
    size = 64 if comic else 46
    fnt = font("bangers_regular.ttf", size) if comic else font("hind_bold.ttf", size)
    while True:
        lines = wrap(text, fnt, x1 - x0 - 50, d)
        h = len(lines) * size * 1.12
        if h <= y1 - y0 - 30 or size <= 28:
            break
        size -= 4
        fnt = font("bangers_regular.ttf", size) if comic else font("hind_bold.ttf", size)
    y = y0 + (y1 - y0 - h) / 2
    for ln in lines:
        w = d.textlength(ln, font=fnt)
        d.text((x0 + (x1 - x0 - w) / 2, y), ln, font=fnt, fill=INK)
        y += size * 1.12


def extra(d, kind):
    if kind == "bucket":
        d.rounded_rectangle([360, 330, 470, 450], radius=12, fill=WATER, outline=INK, width=8)
        d.arc([360, 280, 470, 380], 180, 360, fill=INK, width=8)
        for i, x in enumerate((385, 415, 445)):
            d.line([x, 320, x + (i - 1) * 12, 285], fill=WATER, width=10)
    elif kind == "stars":
        for (x, y) in ((400, 330), (455, 400), (380, 430)):
            pts = []
            for k in range(10):
                rr = 26 if k % 2 == 0 else 11
                a = math.pi / 2 + k * math.pi / 5
                pts.append((x + rr * math.cos(a), y - rr * math.sin(a)))
            d.polygon(pts, fill=SAFFRON, outline=INK)
    elif kind == "crown":
        d.polygon([(110, 230), (130, 175), (165, 215), (190, 160), (215, 215), (250, 175), (270, 230)], fill=SAFFRON, outline=INK)


def outline(img, px=12):
    alpha = img.split()[-1]
    grown = alpha.filter(ImageFilter.MaxFilter(px * 2 + 1))
    base = Image.new("RGBA", img.size, (255, 255, 255, 0))
    base.putalpha(grown)
    white = Image.new("RGBA", img.size, (255, 255, 255, 255))
    base = Image.composite(white, base, grown)
    base.alpha_composite(img)
    return base


def sticker(who, mood, text, kind):
    img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    comic = kind == "comic"
    bubble(d, text, (150, 36, 476, 236) if not comic else (120, 40, 476, 220), comic=comic)
    face(d, 190, 360, 112, who, mood)
    if kind and not comic:
        extra(d, kind)
    return outline(img)


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, who, mood, text, _, kind in STICKERS:
        img = sticker(who, mood, text, kind)
        path = os.path.join(OUT, name)
        quality = 80
        while True:
            img.save(path, "WEBP", quality=quality, method=6)
            if os.path.getsize(path) < 100_000 or quality <= 40:
                break
            quality -= 10
        print(name, os.path.getsize(path))
    tray = Image.new("RGBA", (96 * 4, 96 * 4), (0, 0, 0, 0))
    face(ImageDraw.Draw(tray), 192, 210, 150, "mom", 0)
    tray = tray.resize((96, 96), Image.LANCZOS)
    tray.save(os.path.join(OUT, "tray.png"), optimize=True)
    print("tray.png", os.path.getsize(os.path.join(OUT, "tray.png")))
    # Emojis for the content provider, one line per sticker: file|emoji,emoji
    with open(os.path.join(OUT, "contents.txt"), "w", encoding="utf-8") as f:
        for name, _, _, _, emojis, _ in STICKERS:
            f.write(f"{name}|{emojis}\n")


if __name__ == "__main__":
    main()
