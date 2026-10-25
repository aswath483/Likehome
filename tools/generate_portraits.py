"""Generates the character portraits with an AI image model: fal.ai or Google Gemini.

Each character's sweet portrait (level 0) is generated first; the other three moods are
made by editing that image, so it stays the same person, outfit and light.

  FAL_KEY=... python3 tools/generate_portraits.py            # all 20
  FAL_KEY=... python3 tools/generate_portraits.py mom        # one character
  FAL_KEY=... python3 tools/generate_portraits.py mom/east_asian  # one regional look
  FAL_KEY=... python3 tools/generate_portraits.py --looks    # every look not made yet
  FAL_KEY=... python3 tools/generate_portraits.py boss --levels=1,3  # redo moods from level 0

Uses fal.ai when FAL_KEY is set (FLUX 1.1 Pro Ultra for the first portrait, FLUX Kontext
for the mood edits; override with FAL_BASE_MODEL and FAL_EDIT_MODEL), otherwise Gemini
with GEMINI_API_KEY (override with GEMINI_IMAGE_MODEL).

Writes app/src/main/assets/characters/<character>/<level>.webp and a contact sheet at
build/portraits/sheet.png for review. PORTRAIT_STYLE picks the look: animated (default),
illustrated or photo.
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

STYLES = {
    "animated": (
        "Stylised 3D animated film character portrait, soft rounded features, big expressive eyes, "
        "smooth clean shading, warm cinematic lighting, friendly and charming, head and shoulders, centred, "
        "looking at the viewer, softly blurred background, square 1:1, no text, no watermark"
    ),
    "illustrated": (
        "Warm 2D hand-drawn cartoon illustration, clean confident outlines, soft flat colours with gentle "
        "shading, expressive friendly face, head and shoulders, centred, looking at the viewer, simple soft "
        "background, square 1:1, no text, no watermark"
    ),
    "photo": (
        "Warm natural-light portrait photograph, head and shoulders, centred, looking at the camera, "
        "soft blurred background, shallow depth of field, gentle film colour grade, square 1:1, "
        "plain composition, no text, no watermark"
    ),
}
STYLE_NAME = os.environ.get("PORTRAIT_STYLE", "animated")
STYLE = STYLES[STYLE_NAME]

CAST = {
    "mom": "an Indian woman in her late 40s with warm brown skin, kind eyes, black hair in a low bun "
           "with a little grey, a small red bindi, a thin gold chain and a soft orange cotton saree; "
           "background: a sunlit home kitchen",
    "papa": "an Indian man in his mid-50s with short greying hair, a neat moustache, rectangular reading "
            "glasses and a pressed blue checked half-sleeve shirt; background: a living room in morning light",
    "dadi": "an Indian grandmother in her mid-70s with silver hair in a bun, round gold-rimmed glasses, "
            "a cream cotton saree with a maroon border and a shawl; background: a courtyard with plants",
    "boss": "a Black woman office manager in her 40s with short natural hair, small stud earrings, a smart "
            "navy blazer with a lanyard, holding a coffee mug; "
            "background: a bright modern office",
    "abuela": "a Latin American grandmother in her early 70s with short curly silver hair, small gold hoop "
              "earrings, a floral blouse and an apron; background: a warm colourful kitchen",
}

MOODS = {
    "mom": [
        "smiling warmly while holding a cup of chai",
        "eyebrows raised and lips pressed together, one hand on her hip, mildly annoyed",
        "a side-eye glance, head tilted, a sceptical half-smile, holding up a smartphone and pointing at its screen",
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
        "a polite tight smile, raising one wrist and pointing at the wristwatch on it",
        "looking up from a laptop, one eyebrow raised, unimpressed",
        "mock panic, both hands on cheeks, an open laptop in front glowing with a red alert icon and no readable text, comic",
    ],
    "abuela": [
        "a big warm smile",
        "hands on hips, eyebrows raised",
        "a side-eye with a sceptical smile, wagging a finger",
        "comic outrage, holding up a slipper, eyes wide, funny and loving",
    ],
}


# Regional looks the user can pick for the family characters, in assets/characters/<who>/<look>/.
# The look in DEFAULT_LOOK is the one in the character's own folder.
DEFAULT_LOOK = {"mom": "south_asian", "papa": "south_asian", "boss": "african"}
LOOKS = {
    "mom": {
        "east_asian": "an East Asian woman in her late 40s with a neat black bob with a little grey, kind eyes, small "
                      "pearl earrings and a soft coral cardigan over a cream blouse; background: a sunlit home kitchen",
        "african": "a Black woman in her late 40s with warm dark skin, natural hair in a colourful patterned headwrap, "
                   "small gold earrings and an orange patterned wrap dress; background: a sunlit home kitchen",
        "european": "a white woman in her late 40s with shoulder-length wavy auburn hair with a little grey, light "
                    "freckles and a soft blue knit sweater; background: a sunlit home kitchen",
        "latin": "a Latina woman in her late 40s with long dark wavy hair in a loose ponytail, small gold hoop earrings "
                 "and a mustard yellow blouse; background: a sunlit home kitchen",
        "middle_eastern": "a Middle Eastern woman in her late 40s wearing a soft rose hijab, kind dark eyes, a thin gold "
                          "bracelet and a cream long-sleeve blouse; background: a sunlit home kitchen",
    },
    "papa": {
        "east_asian": "an East Asian man in his mid-50s with short greying hair, rectangular reading glasses and a "
                      "pressed light grey polo shirt; background: a living room in morning light",
        "african": "a Black man in his mid-50s with short greying hair, a trimmed grey beard, rectangular reading "
                   "glasses and a pressed green checked shirt; background: a living room in morning light",
        "european": "a white man in his mid-50s with thinning greying hair, a neat grey moustache, rectangular reading "
                    "glasses and a brown knit vest over a white shirt; background: a living room in morning light",
        "latin": "a Latino man in his mid-50s with wavy greying hair, a thick moustache, rectangular reading glasses "
                 "and a pressed white guayabera shirt; background: a living room in morning light",
        "middle_eastern": "a Middle Eastern man in his mid-50s with short greying hair, a neat grey beard, rectangular "
                          "reading glasses and a pressed beige shirt; background: a living room in morning light",
    },
    "boss": {
        "south_asian": "a South Asian man office manager in his 40s with neat black hair, a trimmed beard, a smart "
                       "navy blazer with a lanyard, holding a coffee mug; background: a bright modern office",
        "east_asian": "an East Asian woman office manager in her 40s with a sleek black bob, a smart navy blazer with "
                      "a lanyard, holding a coffee mug; background: a bright modern office",
        "european": "a white man office manager in his 40s with short sandy hair, a smart navy blazer with a lanyard, "
                    "holding a coffee mug; background: a bright modern office",
        "latin": "a Latina woman office manager in her 40s with dark hair in a neat bun, a smart navy blazer with a "
                 "lanyard, holding a coffee mug; background: a bright modern office",
        "middle_eastern": "a Middle Eastern man office manager in his 40s with short dark hair, a neat beard, a smart "
                          "navy blazer with a lanyard, holding a coffee mug; background: a bright modern office",
    },
}


def describe(who, look):
    return CAST[who] if look is None else LOOKS[who][look]


def moods(who, look):
    if who == "mom" and look not in (None, "south_asian"):
        return [m.replace("chai", "tea") for m in MOODS[who]]
    return MOODS[who]


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


def save(img, who, look, level):
    folder = os.path.join(OUT, who, look) if look else os.path.join(OUT, who)
    os.makedirs(folder, exist_ok=True)
    side = min(img.size)
    left, top = (img.width - side) // 2, (img.height - side) // 2
    img = img.crop((left, top, left + side, top + side)).resize((1024, 1024), Image.LANCZOS)
    path = os.path.join(folder, f"{level}.webp")
    img.save(path, "WEBP", quality=85, method=6)
    print(f"  saved {os.path.relpath(path, ROOT)} ({os.path.getsize(path) // 1024} KB)")
    return img


def generate(who, look=None, levels=(0, 1, 2, 3)):
    print(who + (f"/{look}" if look else ""))
    folder = os.path.join(OUT, who, look) if look else os.path.join(OUT, who)
    lines = moods(who, look)
    if 0 in levels:
        base = save(create(f"{STYLE}. Character: {describe(who, look)}, {lines[0]}."), who, look, 0)
    else:
        base = Image.open(os.path.join(folder, "0.webp")).convert("RGB")
    for level in (l for l in levels if l != 0):
        prompt = (
            "Edit this image: keep exactly the same character, art style, face, hair, clothes, background, "
            f"framing and light. Only change the expression and pose to: {lines[level]}. Keep it warm "
            "and loving, never frightening. Square 1:1, no text."
        )
        save(edit(base, prompt), who, look, level)


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
    flags = dict(a[2:].split("=", 1) if "=" in a else (a[2:], "") for a in sys.argv[1:] if a.startswith("--"))
    levels = tuple(int(x) for x in flags["levels"].split(",")) if "levels" in flags else (0, 1, 2, 3)
    targets = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "looks" in flags:  # every regional look that has no art yet
        targets += [f"{who}/{look}" for who in LOOKS for look in LOOKS[who]
                    if not os.path.exists(os.path.join(OUT, who, look, "3.webp"))]
    for target in targets or list(CAST):
        who, _, look = target.partition("/")
        generate(who, look or None, levels)
    sheet()
