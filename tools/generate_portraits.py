"""Generates the character portraits with an AI image model: fal.ai or Google Gemini.

Each character's sweet portrait (level 0) is generated first; the other three moods are
made by editing that image, so it stays the same person, outfit and light.

  FAL_KEY=... python3 tools/generate_portraits.py            # all 20
  FAL_KEY=... python3 tools/generate_portraits.py mom        # one character
  FAL_KEY=... python3 tools/generate_portraits.py mom --base-only

Uses fal.ai when FAL_KEY is set (FLUX 1.1 Pro Ultra for the first portrait, FLUX Kontext
for the mood edits; override with FAL_BASE_MODEL and FAL_EDIT_MODEL), otherwise Gemini
with GEMINI_API_KEY (override with GEMINI_IMAGE_MODEL).

Writes app/src/main/assets/characters/<character>/<level>.webp and a contact sheet at
build/portraits/sheet.png for review.
"""
import base64
import io
import json
import os
import sys
import time
import urllib.request

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app/src/main/assets/characters")
SHEET = os.path.join(ROOT, "build/portraits/sheet.png")
MODEL = os.environ.get("GEMINI_IMAGE_MODEL", "gemini-2.5-flash-image")
URL = f"https://generativelanguage.googleapis.com/v1beta/models/{MODEL}:generateContent"

STYLE = (
    "Warm natural-light portrait photograph, head and shoulders, centred, looking at the camera, "
    "soft blurred background, shallow depth of field, gentle film colour grade, square 1:1, "
    "plain composition, no text, no watermark"
)

CAST = {
    "mom": "an Indian woman in her late 40s with warm brown skin, kind eyes, black hair in a low bun "
           "with a little grey, a small red bindi, a thin gold chain and a soft orange cotton saree; "
           "background: a sunlit home kitchen",
    "papa": "an Indian man in his mid-50s with short greying hair, a neat moustache, rectangular reading "
            "glasses and a pressed blue checked half-sleeve shirt; background: a living room in morning light",
    "dadi": "an Indian grandmother in her mid-70s with silver hair in a bun, round gold-rimmed glasses, "
            "a cream cotton saree with a maroon border and a shawl; background: a courtyard with plants",
    "boss": "an office manager in their 40s in a smart navy blazer with a lanyard, holding a coffee mug; "
            "background: a bright modern office",
    "abuela": "a Latin American grandmother in her early 70s with short curly silver hair, small gold hoop "
              "earrings, a floral blouse and an apron; background: a warm colourful kitchen",
}

MOODS = {
    "mom": [
        "smiling warmly while holding a cup of chai",
        "eyebrows raised and lips pressed together, one hand on her hip, mildly annoyed",
        "a side-eye glance, head tilted, a sceptical half-smile, pointing at a phone screen",
        "theatrical comic outrage, eyes wide, mouth open mid-shout, holding up a steel bucket of water, funny and loving",
    ],
    "papa": [
        "a small calm smile",
        "looking over his glasses, unimpressed, arms folded",
        "one eyebrow raised, holding up a folded newspaper, quietly comparing",
        "dramatic comic disappointment, hand on forehead, holding a TV remote out of reach",
    ],
    "dadi": [
        "a gentle loving smile",
        "peering over her glasses, gently scolding, one finger raised",
        "eyes narrowed with a knowing look, as if telling an old story",
        "comic theatrical shock, both hands raised, a walking stick in one hand, loving",
    ],
    "boss": [
        "a friendly confident smile",
        "a polite tight smile, tapping a wristwatch",
        "looking up from a laptop, one eyebrow raised, unimpressed",
        "mock panic, holding up a laptop showing an urgent calendar invite, comic",
    ],
    "abuela": [
        "a big warm smile",
        "hands on hips, eyebrows raised",
        "a side-eye with a sceptical smile, wagging a finger",
        "comic outrage, holding up a slipper, eyes wide, funny and loving",
    ],
}


FAL_BASE = os.environ.get("FAL_BASE_MODEL", "fal-ai/flux-pro/v1.1-ultra")
FAL_EDIT = os.environ.get("FAL_EDIT_MODEL", "fal-ai/flux-pro/kontext")


def post(url, payload, headers):
    req = urllib.request.Request(url, data=json.dumps(payload).encode(), headers={"Content-Type": "application/json", **headers})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=300) as resp:
                return json.load(resp)
        except Exception as e:  # rate limits and flaky responses: back off and retry
            if attempt == 3:
                raise
            print(f"  retrying after error: {e}")
            time.sleep(5 * (attempt + 1))


def fal(model, prompt, image=None):
    payload = {"prompt": prompt, "aspect_ratio": "1:1", "num_images": 1, "output_format": "png", "sync_mode": True}
    if image is not None:
        payload["image_url"] = to_data_uri(image)
    data = post(f"https://fal.run/{model}", payload, {"Authorization": f"Key {os.environ['FAL_KEY']}"})
    url = data["images"][0]["url"]
    if url.startswith("data:"):
        raw = base64.b64decode(url.split(",", 1)[1])
    else:
        with urllib.request.urlopen(url, timeout=120) as resp:
            raw = resp.read()
    return Image.open(io.BytesIO(raw)).convert("RGB")


def to_data_uri(img):
    buf = io.BytesIO()
    img.save(buf, "PNG")
    return "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()


def create(prompt):
    if os.environ.get("FAL_KEY"):
        return fal(FAL_BASE, prompt)
    return call([{"text": prompt}])


def edit(image, prompt):
    if os.environ.get("FAL_KEY"):
        return fal(FAL_EDIT, prompt, image)
    return call([to_part(image), {"text": prompt}])


def call(parts):
    key = os.environ["GEMINI_API_KEY"]
    body = json.dumps({
        "contents": [{"parts": parts}],
        "generationConfig": {"responseModalities": ["IMAGE"]},
    }).encode()
    req = urllib.request.Request(URL, data=body, headers={"Content-Type": "application/json", "x-goog-api-key": key})
    for attempt in range(4):
        try:
            with urllib.request.urlopen(req, timeout=180) as resp:
                data = json.load(resp)
            for part in data["candidates"][0]["content"]["parts"]:
                inline = part.get("inlineData") or part.get("inline_data")
                if inline:
                    return Image.open(io.BytesIO(base64.b64decode(inline["data"]))).convert("RGB")
            raise RuntimeError(f"No image in response: {json.dumps(data)[:400]}")
        except Exception as e:  # rate limits and flaky responses: back off and retry
            if attempt == 3:
                raise
            print(f"  retrying after error: {e}")
            time.sleep(5 * (attempt + 1))


def to_part(img):
    buf = io.BytesIO()
    img.save(buf, "PNG")
    return {"inline_data": {"mime_type": "image/png", "data": base64.b64encode(buf.getvalue()).decode()}}


def save(img, who, level):
    folder = os.path.join(OUT, who)
    os.makedirs(folder, exist_ok=True)
    side = min(img.size)
    left, top = (img.width - side) // 2, (img.height - side) // 2
    img = img.crop((left, top, left + side, top + side)).resize((1024, 1024), Image.LANCZOS)
    path = os.path.join(folder, f"{level}.webp")
    img.save(path, "WEBP", quality=85, method=6)
    print(f"  saved {os.path.relpath(path, ROOT)} ({os.path.getsize(path) // 1024} KB)")
    return img


def generate(who, base_only=False):
    print(who)
    base = create(f"{STYLE}. Portrait of {CAST[who]}, {MOODS[who][0]}.")
    base = save(base, who, 0)
    if base_only:
        return
    for level in (1, 2, 3):
        prompt = (
            "Edit this photo: keep exactly the same person, face, hair, clothes, background, framing and "
            f"light. Only change the expression and pose to: {MOODS[who][level]}. Keep it warm and loving, "
            "never frightening. Square 1:1, no text."
        )
        save(edit(base, prompt), who, level)


def sheet():
    tiles = []
    for who in CAST:
        for level in range(4):
            path = os.path.join(OUT, who, f"{level}.webp")
            if os.path.exists(path):
                tiles.append((who, level, Image.open(path).convert("RGB").resize((256, 256))))
    if not tiles:
        return
    rows = sorted({t[0] for t in tiles}, key=list(CAST).index)
    img = Image.new("RGB", (256 * 4, 256 * len(rows)), (18, 15, 12))
    for who, level, tile in tiles:
        img.paste(tile, (level * 256, rows.index(who) * 256))
    os.makedirs(os.path.dirname(SHEET), exist_ok=True)
    img.save(SHEET)
    print(f"contact sheet: {os.path.relpath(SHEET, ROOT)}")


if __name__ == "__main__":
    if not (os.environ.get("FAL_KEY") or os.environ.get("GEMINI_API_KEY")):
        sys.exit("Set FAL_KEY (fal.ai) or GEMINI_API_KEY (Google AI Studio) first.")
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    for who in args or list(CAST):
        generate(who, base_only="--base-only" in sys.argv)
    sheet()
