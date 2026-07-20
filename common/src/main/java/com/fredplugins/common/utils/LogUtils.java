package com.fredplugins.common.utils;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class LogUtils {
//	public final Logger log;
//	public LogUtils(String level) {
//		log = getLogger(this.getClass(), level);
//	}

	public static <C> Logger getLogger(@NonNull Class<C> ownerClazz, @NonNull String level) {
		ch.qos.logback.classic.Logger toRet = (ch.qos.logback.classic.Logger) getLogger(ownerClazz);
		toRet.setLevel(
			Objects.requireNonNull(ch.qos.logback.classic.Level.toLevel(level, null))
		);
		return toRet;
	}

	public static void setLevel(@NonNull Logger log, @NonNull String level) {
		ch.qos.logback.classic.Logger toRet = (ch.qos.logback.classic.Logger) log;
		toRet.setLevel(
			Objects.requireNonNull(ch.qos.logback.classic.Level.toLevel(level, null))
		);
	}

	public static Logger checkLevel(@NonNull Logger log, @NonNull Logger parent) {
		ch.qos.logback.classic.Logger toRet = (ch.qos.logback.classic.Logger) log;
		ch.qos.logback.classic.Logger p = (ch.qos.logback.classic.Logger) parent;
		assert(toRet.getEffectiveLevel() == p.getEffectiveLevel());
		return toRet;
	}

	public static <C> Logger getLogger(@NonNull Class<C> ownerClazz) {
		return LoggerFactory.getLogger(ownerClazz);
	}

	public static <C> Logger createChild(@NonNull Logger parent, @NonNull Class<C> ownerClazz) {
		ch.qos.logback.classic.Logger p = (ch.qos.logback.classic.Logger) parent;
		return p.getLoggerContext().getLogger(ownerClazz);
	}

	public static <C> Logger createChild(@NonNull Logger parent, @NonNull Class<C> ownerClazz, @NonNull String level) {
		ch.qos.logback.classic.Logger p = (ch.qos.logback.classic.Logger) parent;

		ch.qos.logback.classic.Logger toRet = p.getLoggerContext().getLogger(ownerClazz);
		toRet.setLevel(
			Objects.requireNonNull(
				ch.qos.logback.classic.Level.toLevel(level, null)
			)
		);
		return toRet;
	}
}
