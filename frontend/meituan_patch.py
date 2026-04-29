import os
import re

directory = r"D:\项目实战\Order-Project\src\views\user"

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    original = content

    replacements = [
        # Pink bounds
        (r'#db2777', r'#FFC300'),
        (r'rgba\(219,\s*39,\s*119', r'rgba(255, 195, 0'),
        (r'#be185d', r'#E5AF00'),
        (r'#fdf2f8', r'#FFF8E1'),
        (r'#fce7f3', r'#FFECB3'),
        (r'#9d174d', r'#555555'),
        
        # Indigo bounds
        (r'#4f46e5', r'#FFC300'),
        (r'rgba\(79,\s*70,\s*229', r'rgba(255, 195, 0'),
        (r'#4338ca', r'#E5AF00'),
        (r'#eef2ff', r'#FFF8E1'),
        (r'#e0e7ff', r'#FFECB3'),

        # Blue
        (r'#409eff', r'#FFC300'),
        (r'#ecf5ff', r'#FFF8E1'),
        (r'#c6d8f7', r'#FFECB3'),
        (r'#e6f1fe', r'#FFF9E6'),
        (r'#d4e8fd', r'#FFECB3'),
        (r'#66b1ff', r'#FFD44D'),
    ]

    for old, new in replacements:
        content = re.sub(old, new, content, flags=re.IGNORECASE)

    # 2. Fix the .btn-primary text color from #ffffff to #222222
    def fix_btn(match):
        block = match.group(0)
        block = re.sub(r'color:\s*#ffffff\s*;', 'color: #222222;', block, flags=re.IGNORECASE)
        # Fix element plus primary buttons as well if defined directly
        return block
    content = re.sub(r'\.btn-primary\s*\{[^}]*\}', fix_btn, content)

    # 3. Fix Icon wrappers (Background becomes Yellow, icon color becomes black)
    def fix_icon_wrap(match):
        block = match.group(0)
        block = re.sub(r'background:\s*#FFF8E1\s*;', 'background: #FFC300;', block, flags=re.IGNORECASE)
        block = re.sub(r'color:\s*#FFC300\s*;', 'color: #222222;', block, flags=re.IGNORECASE)
        block = re.sub(r'background:.*linear-gradient.*;', 'background: #FFC300;', block, flags=re.IGNORECASE)
        return block
    content = re.sub(r'\.icon-wrapper\s*\{[^}]*\}', fix_icon_wrap, content)
    content = re.sub(r'\.shop-icon-circle\s*\{[^}]*\}', fix_icon_wrap, content)
    content = re.sub(r'\.icon-box\s*\{[^}]*\}', fix_icon_wrap, content)

    # 4. Fix El-tabs active text
    def fix_tabs(match):
        block = match.group(0)
        block = re.sub(r'color:\s*#FFC300\s*;', 'color: #222222; font-weight: 700;', block, flags=re.IGNORECASE)
        return block
    content = re.sub(r'\.premium-tabs\s*:deep\(\.el-tabs__item\.is-active\)\s*\{[^}]*\}', fix_tabs, content)

    # 5. Fix price numbers (Change unreadable yellow back to bold red for prices)
    def fix_price(match):
        block = match.group(0)
        block = re.sub(r'color:\s*#FFC300\s*;', 'color: #FF4A26;', block, flags=re.IGNORECASE)
        return block
    content = re.sub(r'\.price-number\s*\{[^}]*\}', fix_price, content)
    content = re.sub(r'\.subtotal-text\s*\{[^}]*\}', fix_price, content)
    content = re.sub(r'\.highlight-amount\s*\{[^}]*\}', fix_price, content)
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith('.vue'):
            process_file(os.path.join(root, file))

print("DONE")
