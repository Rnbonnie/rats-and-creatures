import subprocess
print(subprocess.getoutput("javap -classpath $(find ~/.gradle/caches/fabric-loom/ -name '*.jar' | grep -i 'minecraft-1.20' | head -n 1) net.minecraft.client.render.entity.model.BatEntityModel"))
