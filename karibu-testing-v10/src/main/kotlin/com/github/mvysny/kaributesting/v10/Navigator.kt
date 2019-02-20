package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.server.RouteRegistry
import kotlin.test.expect
import kotlin.test.fail

/**
 * Returns the browser's current path. Returns null if there is no current UI.
 */
val currentPath: String? get() {
    return UI.getCurrent()?.internals?.activeViewLocation?.pathWithQueryParameters
}

/**
 * Returns the current view
 */
val currentView: Class<out Component>? get() {
    val path = (currentPath ?: return null).trim('/')
    val registry: RouteRegistry = UI.getCurrent().router.registry
    val segments = path.split('/')
    for (prefix in segments.size downTo 1) {
        val p = segments.subList(0, prefix).joinToString("/")
        val s = segments.subList(prefix, segments.size)
        val clazz = registry.getNavigationTarget(p, s).orElse(null)
        if (clazz != null) {
            return clazz
        }
    }
    return null
}

/**
 * Expects that given [view] is the currently displayed view.
 */
fun <V: Component> expectView(view: Class<V>) {
    @Suppress("UNCHECKED_CAST")
    expect(view) { currentView }
}

/**
 * Expects that given view is the currently displayed view.
 */
inline fun <reified V: Component> expectView() = expectView(V::class.java)