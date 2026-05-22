import re

with open('src/client/java/com/rnoobb/rats/client/renderer/BatCompanionRenderer.java', 'r') as f:
    content = f.read()

# Add scale setup
imports = """import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;"""

content = content.replace("import net.minecraft.util.Identifier;", imports)

scale_method = """    @Override
    protected void scale(BatCompanionEntity entity, MatrixStack matrixStack, float f) {
        matrixStack.scale(0.35F, 0.35F, 0.35F);
    }
}"""

content = content.replace("}", scale_method, 1) # Note: we just append to the renderer class, not the model class

# Actually, a better replace targeting the BatCompanionRenderer class specifically:
renderer_scale = """    @Override
    protected void scale(BatCompanionEntity entity, MatrixStack matrixStack, float f) {
        matrixStack.scale(0.35F, 0.35F, 0.35F);
    }
}"""
content = re.sub(r'    @Override\n    public Identifier getTexture\(BatCompanionEntity entity\) \{\n        return TEXTURE;\n    \}\n\}',
                 '    @Override\n    public Identifier getTexture(BatCompanionEntity entity) {\n        return TEXTURE;\n    }\n\n' + renderer_scale, content)


with open('src/client/java/com/rnoobb/rats/client/renderer/BatCompanionRenderer.java', 'w') as f:
    f.write(content)
