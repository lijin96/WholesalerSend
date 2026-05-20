import re
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path
import xml.etree.ElementTree as ET


APP_RES_DIR = Path("app/src/main/res")
LAYOUT_DIR = APP_RES_DIR / "layout"

BASE_DIMENS_XML = APP_RES_DIR / "values-1280x752" / "dimens.xml"
TARGET_DIMENS_XML = APP_RES_DIR / "values-800x480" / "dimens.xml"

WIDTH_FACTOR = Decimal(800) / Decimal(1280)  # 0.625
HEIGHT_FACTOR = Decimal(480) / Decimal(752)  # ~0.63829787234
TEXT_FACTOR = (WIDTH_FACTOR + HEIGHT_FACTOR) / Decimal(2)
OTHER_FACTOR = TEXT_FACTOR

# Matches attributes like android:layout_width="150dp" (dp/sp, integer or decimal, optional negative)
ATTR_DIMEN_RE = re.compile(
    r"(?P<attr>[a-zA-Z0-9_:\-]+)\s*=\s*(?P<q>['\"])(?P<val>-?\d+(?:\.\d+)?)(?P<unit>dp|sp)(?P=q)",
)


def normalize_num_for_name(s: str) -> str:
    # Keep it deterministic, safe for resource name.
    # "-15" -> "neg15", "0.5" -> "0_5", "12.30" -> "12_3"
    if s.startswith("-"):
        s = "neg" + s[1:]
    s = s.replace("-", "")  # in case we already prefixed
    s = s.replace(".", "_")
    s = re.sub(r"_0+$", "", s)
    s = re.sub(r"_+$", "", s)
    if s == "":
        return "0"
    return s


def scale_value(val: str, factor: Decimal) -> str:
    dec = Decimal(val) * factor
    dec = dec.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
    # Convert to a clean string (e.g. "10.00" -> "10")
    s = format(dec, "f")
    if "." in s:
        s = s.rstrip("0").rstrip(".")
    return s


def role_for_attr(attr_name: str):
    # attr_name includes namespace prefix sometimes (android:)
    local = attr_name.split(":")[-1]
    local_lower = local.lower()

    # Text
    if local_lower.endswith("textsize"):
        return "t", TEXT_FACTOR

    # Width-related attributes
    width_hints = {
        "layout_width",
        "minwidth",
        "maxwidth",
        "width",
        "layout_marginleft",
        "layout_marginright",
        "layout_marginstart",
        "layout_marginend",
        "layout_marginalignleft",
        "paddingleft",
        "paddingright",
        "paddingstart",
        "paddingend",
    }
    height_hints = {
        "layout_height",
        "minheight",
        "maxheight",
        "height",
        "layout_margintop",
        "layout_marginbottom",
        "paddingtop",
        "paddingbottom",
    }

    if local_lower in width_hints:
        return "w", WIDTH_FACTOR
    if local_lower in height_hints:
        return "h", HEIGHT_FACTOR

    # For margin/padding attributes like layout_marginLeft / paddingTop (case-sensitive in input),
    # use suffix heuristics.
    if "margin" in local_lower or "padding" in local_lower:
        if any(k in local_lower for k in ["left", "right", "start", "end"]):
            return "w", WIDTH_FACTOR
        if any(k in local_lower for k in ["top", "bottom"]):
            return "h", HEIGHT_FACTOR

    return "o", OTHER_FACTOR


def load_existing_dimens_names(dimens_xml: Path):
    if not dimen_xml_exists(dimens_xml):
        return set()
    try:
        tree = ET.parse(str(dimens_xml))
        root = tree.getroot()
        return {d.attrib.get("name") for d in root.findall("dimen") if "name" in d.attrib}
    except Exception:
        return set()


def dimen_xml_exists(p: Path) -> bool:
    return p.exists() and p.is_file()


def read_existing_dimens(dimens_xml: Path):
    existing = {}
    if not dimen_xml_exists(dimens_xml):
        return existing
    try:
        tree = ET.parse(str(dimens_xml))
        root = tree.getroot()
        for d in root.findall("dimen"):
            name = d.attrib.get("name")
            val = (d.text or "").strip()
            if name:
                existing[name] = val
    except Exception:
        pass
    return existing


def ensure_resources_xml_has_resources_tag(dimens_xml: Path):
    if not dimen_xml_exists(dimens_xml):
        dimens_xml.parent.mkdir(parents=True, exist_ok=True)
        dimens_xml.write_text("<resources>\n</resources>\n", encoding="utf-8")


def append_dimens(dimens_xml: Path, new_dimens: dict):
    # new_dimens: name -> value (including unit, e.g. "12dp")
    ensure_resources_xml_has_resources_tag(dimens_xml)
    existing_text = dimen_xml_exists(dimens_xml) and dimen_xml_exists(dimens_xml)
    # We'll do a safe text-based append: find last </resources>
    text = dimen_xml_exists(dimens_xml) and dimen_xml_exists(dimens_xml)
    content = dimen_xml_exists(dimens_xml) and dimen_xml_exists(dimens_xml)
    old = dimen_xml_exists(dimens_xml) and dimens_xml.read_text(encoding="utf-8")
    if not old:
        old = "<resources>\n</resources>\n"
    if "</resources>" not in old:
        old = old.strip() + "\n</resources>\n"
    insertion = []
    for name, value in sorted(new_dimens.items()):
        insertion.append(f'    <dimen name="{name}">{value}</dimen>\n')
    old_lines = old.split("</resources>")
    before = old_lines[0]
    after = "</resources>" + (old_lines[1] if len(old_lines) > 1 else "")
    # Insert before closing tag, preserving existing formatting.
    if not before.endswith("\n"):
        before += "\n"
    new_content = before + "".join(insertion) + after
    dimens_xml.write_text(new_content, encoding="utf-8")


def main():
    if not LAYOUT_DIR.exists():
        raise SystemExit(f"LAYOUT_DIR not found: {LAYOUT_DIR}")
    if not BASE_DIMENS_XML.exists():
        raise SystemExit(f"BASE_DIMENS_XML not found: {BASE_DIMENS_XML}")
    if not TARGET_DIMENS_XML.exists():
        raise SystemExit(f"TARGET_DIMENS_XML not found: {TARGET_DIMENS_XML}")

    # Collect generated dimen entries:
    # name -> (base_value_with_unit, target_value_with_unit)
    generated = {}
    # Layout modifications.
    layout_files = sorted(LAYOUT_DIR.rglob("*.xml"))
    print(f"Found layout xml files: {len(layout_files)}")

    for xml_path in layout_files:
        content = xml_path.read_text(encoding="utf-8")

        def repl(m):
            attr = m.group("attr")
            q = m.group("q")
            val = m.group("val")
            unit = m.group("unit")

            role, factor = role_for_attr(attr)
            orig_norm = normalize_num_for_name(val)
            name = f"scaled_{role}_{unit}_{orig_norm}"

            # Record dimen values if not already
            if name not in generated:
                base_scaled = scale_value(val, Decimal(1))
                # base is the original value, keep original exact formatting (but trimmed)
                base_val = val
                target_val = scale_value(val, factor)
                generated[name] = (
                    f"{base_val}{unit}",
                    f"{target_val}{unit}",
                )

            return f'{attr}={q}@dimen/{name}{q}'

        new_content = ATTR_DIMEN_RE.sub(repl, content)
        if new_content != content:
            xml_path.write_text(new_content, encoding="utf-8")
            print(f"Updated: {xml_path}")

    # Add new dimen entries into both files (without removing existing).
    base_existing = read_existing_dimens(BASE_DIMENS_XML)
    target_existing = read_existing_dimens(TARGET_DIMENS_XML)

    base_to_add = {}
    target_to_add = {}

    for name, (base_val, target_val) in generated.items():
        if name not in base_existing:
            base_to_add[name] = base_val
        if name not in target_existing:
            target_to_add[name] = target_val

    if base_to_add:
        append_dimens(BASE_DIMENS_XML, base_to_add)
        print(f"Added to base dimen: {len(base_to_add)}")
    if target_to_add:
        append_dimens(TARGET_DIMENS_XML, target_to_add)
        print(f"Added to target dimen: {len(target_to_add)}")

    print(f"Done. Generated dimen count: {len(generated)}")


if __name__ == "__main__":
    main()

