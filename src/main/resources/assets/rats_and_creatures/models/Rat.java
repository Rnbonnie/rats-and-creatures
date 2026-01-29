// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class Rat extends EntityModel<rat> {
	private final ModelPart bb_main;
	public Rat(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -12.0F, -4.0F, 11.0F, 9.0F, 10.0F, new Dilation(0.0F))
		.uv(42, 0).cuboid(-2.0F, -10.0F, -23.0F, 5.0F, 3.0F, 3.0F, new Dilation(0.0F))
		.uv(42, 6).cuboid(2.0F, -17.0F, -14.0F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F))
		.uv(48, 48).cuboid(-0.5F, -10.0F, -25.0F, 2.0F, 2.0F, 2.0F, new Dilation(0.0F))
		.uv(0, 19).cuboid(-4.0F, -11.0F, -13.0F, 9.0F, 8.0F, 9.0F, new Dilation(0.0F))
		.uv(0, 36).cuboid(-3.0F, -13.0F, -20.0F, 7.0F, 7.0F, 7.0F, new Dilation(0.0F))
		.uv(42, 12).cuboid(-5.0F, -17.0F, -14.0F, 5.0F, 5.0F, 1.0F, new Dilation(0.0F))
		.uv(36, 31).cuboid(2.0F, -3.0F, 3.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F))
		.uv(46, 31).cuboid(-4.0F, -3.0F, 3.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F))
		.uv(36, 19).cuboid(-1.0F, -8.0F, 6.0F, 3.0F, 3.0F, 9.0F, new Dilation(0.0F))
		.uv(28, 48).cuboid(-4.0F, -3.0F, -12.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F))
		.uv(38, 48).cuboid(2.0F, -3.0F, -12.0F, 3.0F, 3.0F, 2.0F, new Dilation(0.0F))
		.uv(28, 36).cuboid(-1.0F, -8.0F, 15.0F, 3.0F, 3.0F, 9.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(Entity.rat entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}