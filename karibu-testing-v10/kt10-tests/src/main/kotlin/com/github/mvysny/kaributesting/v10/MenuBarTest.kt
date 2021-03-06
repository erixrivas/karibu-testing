package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.menuBar
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.menubar.MenuBar
import kotlin.test.expect

internal fun DynaNodeGroup.menuBarTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    // MenuBar is present in Vaadin 14+ only
    if (VaadinMeta.version >= 14) {
        test("_clickItemWithCaption") {
            var clicked = 0
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo", { _ -> clicked++ }) {}
            }
            menuBar._clickItemWithCaption("foo")
            expect(1) { clicked }
        }

        test("_click") {
            var clicked = 0
            lateinit var mi: MenuItem
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                mi = item("foo", { _ -> clicked++ }) {}
            }
            menuBar._click(mi)
            expect(1) { clicked }
        }
    }
}
