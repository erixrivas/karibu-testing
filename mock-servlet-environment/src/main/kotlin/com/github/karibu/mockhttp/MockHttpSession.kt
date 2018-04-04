package com.github.karibu.mockhttp

import java.util.Collections
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

import javax.servlet.ServletContext
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionContext

open class MockHttpSession(
    private val sessionId: String,
    private val servletContext: ServletContext,
    private val creationTime: Long,
    private var maxInactiveInterval: Int
) : HttpSession {
    private val attributes = ConcurrentHashMap<String, Any>()
    private val valid = AtomicBoolean(true)

    constructor(session: HttpSession) : this(session.id, session.servletContext, session.lastAccessedTime, session.maxInactiveInterval) {
        copyAttributes(session)
    }

    fun destroy() {
        attributes.clear()
    }

    override fun getCreationTime(): Long {
        if (!valid.get()) throw IllegalStateException()
        return creationTime
    }

    override fun getId(): String {
        if (!valid.get()) throw IllegalStateException()
        return sessionId
    }

    override fun getLastAccessedTime(): Long {
        if (!valid.get()) throw IllegalStateException()
        return 0
    }

    override fun getServletContext(): ServletContext = servletContext

    override fun setMaxInactiveInterval(interval: Int) {
        this.maxInactiveInterval = interval
    }

    override fun getMaxInactiveInterval(): Int = maxInactiveInterval

    override fun getSessionContext(): HttpSessionContext? = null

    override fun getAttribute(name: String): Any? {
        if (!valid.get()) throw IllegalStateException()
        return attributes[name]
    }

    override fun getValue(name: String): Any? {
        if (!valid.get()) throw IllegalStateException()
        return attributes[name]
    }

    override fun getAttributeNames(): Enumeration<String> {
        if (!valid.get()) throw IllegalStateException()
        return attributes.keys()
    }

    override fun getValueNames(): Array<String> {
        if (!valid.get()) throw IllegalStateException()
        return Collections.list(attributes.keys()).toTypedArray()
    }

    override fun setAttribute(name: String, value: Any) {
        if (!valid.get()) throw IllegalStateException()
        attributes[name] = value
    }

    override fun putValue(name: String, value: Any) {
        if (!valid.get()) throw IllegalStateException()
        attributes[name] = value
    }

    override fun removeAttribute(name: String) {
        if (!valid.get()) throw IllegalStateException()
        attributes.remove(name)
    }

    override fun removeValue(name: String) {
        if (!valid.get()) throw IllegalStateException()
        attributes.remove(name)
    }

    fun copyAttributes(httpSession: HttpSession): MockHttpSession {
        val e = httpSession.attributeNames
        var k: String?
        while (e.hasMoreElements()) {
            k = e.nextElement()
            if (k == null) continue

            val o = httpSession.getAttribute(k) ?: continue

            attributes[k] = o
        }
        return this
    }

    override fun invalidate() {
        if (!valid.get()) throw IllegalStateException()
        valid.set(false)
    }

    override fun isNew(): Boolean {
        if (!valid.get()) throw IllegalStateException()
        return false
    }

    companion object {
        fun create(ctx: ServletContext): MockHttpSession = MockHttpSession("1", ctx, System.currentTimeMillis(), 30)
    }
}
