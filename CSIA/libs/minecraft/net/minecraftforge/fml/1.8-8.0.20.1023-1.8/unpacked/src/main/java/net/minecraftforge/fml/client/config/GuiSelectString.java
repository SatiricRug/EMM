/*
 * Forge Mod Loader
 * Copyright (c) 2012-2014 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors (this class):
 *     bspkrs - implementation
 */

package net.minecraftforge.fml.client.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;

/**
 * This class provides a screen that allows the user to select a value from a list.
 *
 * @author bspkrs
 */
public class GuiSelectString extends GuiScreen
{
    protected GuiScreen parentScreen;
    protected IConfigElement configElement;
    protected GuiSelectStringEntries entryList;
    protected GuiButtonExt btnUndoChanges, btnDefault, btnDone;
    protected String title;
    protected String titleLine2;
    protected String titleLine3;
    protected int slotIndex;
    protected final Map<Object, String> selectableValues;
    public final Object beforeValue;
    public Object currentValue;
    protected HoverChecker tooltipHoverChecker;
    @SuppressWarnings("rawtypes")
    protected List toolTip;
    protected boolean enabled;

    @SuppressWarnings("rawtypes")
    public GuiSelectString(GuiScreen parentScreen, IConfigElement configElement, int slotIndex, Map<Object, String> selectableValues, Object currentValue, boolean enabled)
    {
        this.field_146297_k = Minecraft.func_71410_x();
        this.parentScreen = parentScreen;
        this.configElement = configElement;
        this.slotIndex = slotIndex;
        this.selectableValues = selectableValues;
        this.beforeValue = currentValue;
        this.currentValue = currentValue;
        this.toolTip = new ArrayList();
        this.enabled = enabled;
        String propName = I18n.func_135052_a(configElement.getLanguageKey());
        String comment;

        comment = I18n.func_135052_a(configElement.getLanguageKey() + ".tooltip",
                "\n" + EnumChatFormatting.AQUA, configElement.getDefault(), configElement.getMinValue(), configElement.getMaxValue());

        if (!comment.equals(configElement.getLanguageKey() + ".tooltip"))
            toolTip = field_146297_k.field_71466_p.func_78271_c(
                    EnumChatFormatting.GREEN + propName + "\n" + EnumChatFormatting.YELLOW + comment, 300);
        else if (configElement.getComment() != null && !configElement.getComment().trim().isEmpty())
            toolTip = field_146297_k.field_71466_p.func_78271_c(
                    EnumChatFormatting.GREEN + propName + "\n" + EnumChatFormatting.YELLOW + configElement.getComment(), 300);
        else
            toolTip = field_146297_k.field_71466_p.func_78271_c(
                    EnumChatFormatting.GREEN + propName + "\n" + EnumChatFormatting.RED + "No tooltip defined.", 300);

        if (parentScreen instanceof GuiConfig)
        {
            this.title = ((GuiConfig) parentScreen).title;
            this.titleLine2 = ((GuiConfig) parentScreen).titleLine2;
            this.titleLine3 = I18n.func_135052_a(configElement.getLanguageKey());
            this.tooltipHoverChecker = new HoverChecker(28, 37, 0, parentScreen.field_146294_l, 800);

        }
        else
        {
            this.title = I18n.func_135052_a(configElement.getLanguageKey());
            this.tooltipHoverChecker = new HoverChecker(8, 17, 0, parentScreen.field_146294_l, 800);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void func_73866_w_()
    {
        this.entryList = new GuiSelectStringEntries(this, this.field_146297_k, this.configElement, this.selectableValues);

        int undoGlyphWidth = field_146297_k.field_71466_p.func_78256_a(UNDO_CHAR) * 2;
        int resetGlyphWidth = field_146297_k.field_71466_p.func_78256_a(RESET_CHAR) * 2;
        int doneWidth = Math.max(field_146297_k.field_71466_p.func_78256_a(I18n.func_135052_a("gui.done")) + 20, 100);
        int undoWidth = field_146297_k.field_71466_p.func_78256_a(" " + I18n.func_135052_a("fml.configgui.tooltip.undoChanges")) + undoGlyphWidth + 20;
        int resetWidth = field_146297_k.field_71466_p.func_78256_a(" " + I18n.func_135052_a("fml.configgui.tooltip.resetToDefault")) + resetGlyphWidth + 20;
        int buttonWidthHalf = (doneWidth + 5 + undoWidth + 5 + resetWidth) / 2;
        this.field_146292_n.add(btnDone = new GuiButtonExt(2000, this.field_146294_l / 2 - buttonWidthHalf, this.field_146295_m - 29, doneWidth, 20, I18n.func_135052_a("gui.done")));
        this.field_146292_n.add(btnDefault = new GuiUnicodeGlyphButton(2001, this.field_146294_l / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5,
                this.field_146295_m - 29, resetWidth, 20, " " + I18n.func_135052_a("fml.configgui.tooltip.resetToDefault"), RESET_CHAR, 2.0F));
        this.field_146292_n.add(btnUndoChanges = new GuiUnicodeGlyphButton(2002, this.field_146294_l / 2 - buttonWidthHalf + doneWidth + 5,
                this.field_146295_m - 29, undoWidth, 20, " " + I18n.func_135052_a("fml.configgui.tooltip.undoChanges"), UNDO_CHAR, 2.0F));
    }

    @Override
    protected void func_146284_a(GuiButton button)
    {
        if (button.field_146127_k == 2000)
        {
            try
            {
                this.entryList.saveChanges();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            this.field_146297_k.func_147108_a(this.parentScreen);
        }
        else if (button.field_146127_k == 2001)
        {
            this.currentValue = configElement.getDefault();
            this.entryList = new GuiSelectStringEntries(this, this.field_146297_k, this.configElement, this.selectableValues);
        }
        else if (button.field_146127_k == 2002)
        {
            this.currentValue = beforeValue;
            this.entryList = new GuiSelectStringEntries(this, this.field_146297_k, this.configElement, this.selectableValues);
        }
    }

    public void func_146274_d() throws IOException
    {
        super.func_146274_d();
        this.entryList.func_178039_p();
    }

    @Override
    protected void func_146286_b(int x, int y, int mouseEvent)
    {
        if (mouseEvent != 0 || !this.entryList.func_148181_b(x, y, mouseEvent))
        {
            super.func_146286_b(x, y, mouseEvent);
        }
    }

    @Override
    public void func_73863_a(int par1, int par2, float par3)
    {
        this.func_146276_q_();
        this.entryList.func_148128_a(par1, par2, par3);
        this.func_73732_a(this.field_146289_q, this.title, this.field_146294_l / 2, 8, 16777215);

        if (this.titleLine2 != null)
            this.func_73732_a(this.field_146289_q, this.titleLine2, this.field_146294_l / 2, 18, 16777215);

        if (this.titleLine3 != null)
            this.func_73732_a(this.field_146289_q, this.titleLine3, this.field_146294_l / 2, 28, 16777215);

        this.btnDone.field_146124_l = currentValue != null;
        this.btnDefault.field_146124_l = enabled && !this.entryList.isDefault();
        this.btnUndoChanges.field_146124_l = enabled && this.entryList.isChanged();
        super.func_73863_a(par1, par2, par3);

        if (this.tooltipHoverChecker != null && this.tooltipHoverChecker.checkHover(par1, par2))
            drawToolTip(this.toolTip, par1, par2);
    }

    @SuppressWarnings("rawtypes")
    public void drawToolTip(List stringList, int x, int y)
    {
        this.func_146283_a(stringList, x, y);
    }
}