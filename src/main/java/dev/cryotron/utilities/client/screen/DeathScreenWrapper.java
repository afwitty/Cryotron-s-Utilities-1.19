package dev.cryotron.utilities.client.screen;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeathScreenWrapper extends DeathScreen {

	private DeathScreen deathScreen;
	private float alpha;
	
    protected BooleanSupplier condition = () -> true;
	
	public DeathScreenWrapper(DeathScreen deathScreen) {
		super(null, deathScreen.hardcore);
        this.deathScreen = deathScreen;
	}

	@Override
	protected void init() {
//        //noinspection ConstantConditions
        deathScreen.init(minecraft, width, height);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void render(PoseStack stack, int pMouseX, int pMouseY, float pPartialTick) {
        int alphaColor = Mth.ceil(this.alpha * 255.0F) << 24;

        this.fillGradient(stack, 0, 0, this.width, this.height, 0xff000000, 0xff000000);
        stack.pushPose();
        stack.scale(2.0F, 2.0F, 2.0F);
        drawCenteredString(stack, this.font, deathScreen.getTitle(), this.width / 2 / 2, 30, 16777215 | alphaColor);
        stack.popPose();
        if (deathScreen.causeOfDeath != null) {
            drawCenteredString(stack, this.font, deathScreen.causeOfDeath, this.width / 2, 85, 16777215 | alphaColor);
        }

        drawCenteredString(stack, this.font, deathScreen.deathScore, this.width / 2, 100, 16777215 | alphaColor);
        if (deathScreen.causeOfDeath != null && pMouseY > 85 && pMouseY < 85 + 9) {
            Style style = deathScreen.getClickedComponentStyleAt(pMouseX);
            this.renderComponentHoverEffect(stack, style, pMouseX, pMouseY);
        }

        for (GuiEventListener guieventlistener : deathScreen.children()) {
            if (guieventlistener instanceof AbstractWidget) {
                ((AbstractWidget) guieventlistener).setAlpha(alpha);
            }
        }
        for(Button button : deathScreen.exitButtons) {
        	button.render(stack, pMouseX, pMouseY, pPartialTick);
        }
    }
    
    @Override
    public void handleExitToTitleScreen() {
           this.exitToTitleScreen();
     }
    
    @Override
    public void exitToTitleScreen() {
        if (this.minecraft.level != null) {
           this.minecraft.level.disconnect();
        }

        this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
     }
    
    public boolean shouldCloseOnEsc() {
        return false;
     }
    
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (condition.getAsBoolean())
            return deathScreen.mouseClicked(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public void tick() {
        if (condition.getAsBoolean())
            deathScreen.tick();
    }

    @Override
    public void removed() {
        if (condition.getAsBoolean())
            deathScreen.removed();
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (condition.getAsBoolean())
            return deathScreen.mouseReleased(pMouseX, pMouseY, pButton);
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (condition.getAsBoolean())
            return deathScreen.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (condition.getAsBoolean())
            return deathScreen.mouseScrolled(pMouseX, pMouseY, pDelta);
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (condition.getAsBoolean())
            return deathScreen.keyPressed(pKeyCode, pScanCode, pModifiers);
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void afterMouseMove() {
        if (condition.getAsBoolean())
            deathScreen.afterMouseMove();
    }

    @Override
    public void afterMouseAction() {
        if (condition.getAsBoolean())
            deathScreen.afterMouseAction();
    }

    @Override
    public void afterKeyboardAction() {
        if (condition.getAsBoolean())
            deathScreen.afterKeyboardAction();
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (condition.getAsBoolean())
            return deathScreen.keyReleased(pKeyCode, pScanCode, pModifiers);
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        if (condition.getAsBoolean())
            deathScreen.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public Optional<GuiEventListener> getChildAt(double pMouseX, double pMouseY) {
        if (condition.getAsBoolean())
            return deathScreen.getChildAt(pMouseX, pMouseY);
        return super.getChildAt(pMouseX, pMouseY);
    }
    
}
