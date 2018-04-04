package com.github.karibu.testing

import com.github.karibu.mockhttp.MockContext
import com.github.karibu.mockhttp.MockHttpSession
import com.github.karibu.mockhttp.MockServletConfig
import com.vaadin.server.*
import com.vaadin.ui.UI
import java.net.URI
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    private val strongRefSession = ThreadLocal<VaadinSession>()
    private val strongRefUI = ThreadLocal<UI>()
    /**
     * Creates new mock session and UI for a test. Just call this before all and every of your UI tests are ran.
     * @param uiFactory called once from this method, to provide instance of your app's UI. By default it returns [MockUI].
     * A basic Vaadin environment is prepared before calling this factory, in order to be safe to instantiate your UI.
     * To instantiate your UI just call your UI constructor, for example `YourUI()`
     * @param servlet the servlet to set to [VaadinServletService]; defaults to [VaadinServlet].
     */
    fun setup(uiFactory: ()->UI = { MockUI() }, servlet: VaadinServlet = VaadinServlet()) {
        // prepare mocking servlet environment
        val servletContext = MockContext()
        servlet.init(MockServletConfig(servletContext))
        val httpSession = MockHttpSession.create(servletContext)

        // mock Vaadin environment: Service
        val config = DefaultDeploymentConfiguration(MockVaadin::class.java, Properties())
        val service = object : VaadinServletService(servlet, config) {
            override fun isAtmosphereAvailable() = false
        }
        service.init()
        VaadinService.setCurrent(service)

        // Session
        val session = VaadinSession(service)
        httpSession.setAttribute(service.serviceName + ".lock", ReentrantLock().apply { lock() })
        session.refreshTransients(WrappedHttpSession(httpSession), service)
        VaadinSession.setCurrent(session)
        strongRefSession.set(session)

        // UI
        val ui = uiFactory()
        strongRefUI.set(ui)
        UI.setCurrent(ui)
        ui.session = session
        Page::class.java.getDeclaredField("location").apply {
            isAccessible = true
            set(ui.page, URI("http://localhost:8080"))
        }
        try {
            UI::class.java.getDeclaredField("uiRootPath").apply {
                isAccessible = true
                set(ui, "")
            }
        } catch (e: NoSuchFieldException) {
            // Vaadin 8.1 and older doesn't have this field.
        }
        UI::class.java.getDeclaredMethod("init", VaadinRequest::class.java).apply {
            isAccessible = true
            invoke(ui, null)
        }
    }
}

internal class MockUI : UI() {
    override fun init(request: VaadinRequest?) {
    }
}
