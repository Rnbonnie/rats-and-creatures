import re

with open('src/main/java/com/rnoobb/rats/entity/custom/RatEntity.java', 'r') as f:
    content = f.read()

# Modify constructor to remove the duplicate listener adding logic
content = re.sub(r'        this\.homePos = this\.getBlockPos\(\);\n        this\.inventory\.addListener\(sender -> \{\n            // Слот 0 -> Надеваем на голову \(шлем/шапка\)\n            this\.equipStack\(EquipmentSlot\.HEAD, sender\.getStack\(0\)\);\n            // Слот 2 -> Даем в "руку" \(чтобы было видно в зубах\)\n            this\.equipStack\(EquipmentSlot\.MAINHAND, sender\.getStack\(2\)\);\n        \}\);\n', '', content)

with open('src/main/java/com/rnoobb/rats/entity/custom/RatEntity.java', 'w') as f:
    f.write(content)
