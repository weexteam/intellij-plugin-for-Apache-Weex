package com.darin.weex.ui.preview

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil

import javax.swing.*
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.ArrayList

/**
 * Created by darin on 5/23/16.
 */
class WeexSplitEditorToolbar(targetComponentForActions: JComponent) : JPanel(GridBagLayout()), Disposable {

    private val mySpacingPanel: MySpacingPanel

    private val myRightToolbar: ActionToolbar

    private val myGutters = ArrayList<EditorGutterComponentEx>()

    private val myAdjustToGutterListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            adjustSpacing()
        }

        override fun componentShown(e: ComponentEvent?) {
            adjustSpacing()
        }

        override fun componentHidden(e: ComponentEvent?) {
            adjustSpacing()
        }
    }

    init {

        val leftToolbar = createToolbarFromGroupId(LEFT_TOOLBAR_GROUP_ID)
        leftToolbar.setTargetComponent(targetComponentForActions)


        myRightToolbar = createToolbarFromGroupId(RIGHT_TOOLBAR_GROUP_ID)
        myRightToolbar.setTargetComponent(targetComponentForActions)

        mySpacingPanel = MySpacingPanel(myRightToolbar.component.preferredSize.getHeight().toInt())
        val centerPanel = JPanel(BorderLayout())
        centerPanel.add(JLabel("View: ", SwingConstants.RIGHT), BorderLayout.EAST)

        add(mySpacingPanel)
        add(leftToolbar.component)
        add(centerPanel,
                GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0))
        add(myRightToolbar.component)

        border = BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.CONTRAST_BORDER_COLOR)

        addComponentListener(myAdjustToGutterListener)
    }

    fun addGutterToTrack(gutterComponentEx: EditorGutterComponentEx) {
        myGutters.add(gutterComponentEx)

        gutterComponentEx.addComponentListener(myAdjustToGutterListener)
    }

    fun refresh() {
        adjustSpacing()
        myRightToolbar.updateActionsImmediately()
    }

    private fun adjustSpacing() {
        var leftMostGutter: EditorGutterComponentEx? = null
        for (gutter in myGutters) {
            if (!gutter.isShowing) {
                continue
            }
            if (leftMostGutter == null || leftMostGutter.x > gutter.x) {
                leftMostGutter = gutter
            }
        }

        val spacing: Int
        if (leftMostGutter == null) {
            spacing = 0
        } else {
            spacing = leftMostGutter.whitespaceSeparatorOffset
        }
        mySpacingPanel.setSpacing(spacing)

        revalidate()
        repaint()
    }

    override fun dispose() {
        removeComponentListener(myAdjustToGutterListener)
        for (gutter in myGutters) {
            gutter.removeComponentListener(myAdjustToGutterListener)
        }
    }

    private inner class MySpacingPanel(private val myHeight: Int) : JPanel() {

        private var mySpacing: Int = 0

        init {
            mySpacing = 0
            isOpaque = false
        }

        override fun getPreferredSize(): Dimension {
            return Dimension(mySpacing, myHeight)
        }

        fun setSpacing(spacing: Int) {
            mySpacing = spacing
        }
    }

    companion object {
        private val LEFT_TOOLBAR_GROUP_ID = "Weex.Toolbar.Left"
        private val RIGHT_TOOLBAR_GROUP_ID = "Weex.Toolbar.Right"

        private fun createToolbarFromGroupId(groupId: String): ActionToolbar {
            val actionManager = ActionManager.getInstance()

            if (!actionManager.isGroup(groupId)) {
                throw IllegalStateException(groupId + " should have been a group")
            }
            val group = actionManager.getAction(groupId) as ActionGroup
            val editorToolbar = actionManager.createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, group, true) as ActionToolbarImpl
            editorToolbar.isOpaque = false
            editorToolbar.border = JBEmptyBorder(0, 2, 0, 2)

            return editorToolbar
        }
    }
}

