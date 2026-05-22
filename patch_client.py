import re

with open('src/client/java/com/rnoobb/rats/RatsAndCreaturesClient.java', 'r') as f:
    content = f.read()

content = content.replace("import com.rnoobb.rats.client.model.BatCompanionModel;\n", "")
content = content.replace("\t\tEntityModelLayerRegistry.registerModelLayer(ModModelLayers.BAT, BatCompanionModel::getTexturedModelData);\n", "")

with open('src/client/java/com/rnoobb/rats/RatsAndCreaturesClient.java', 'w') as f:
    f.write(content)
