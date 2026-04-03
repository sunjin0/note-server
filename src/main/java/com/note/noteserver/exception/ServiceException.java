package com.note.noteserver.exception;

import com.note.noteserver.util.I18nMessageUtil;
import lombok.Getter;

/**
 * 支持国际化的运行时异常
 * 使用消息代码代替硬编码的错误消息
 */
@Getter
public class ServiceException extends RuntimeException {

    private final String messageCode;
    private final Object[] args;

    /**
     * 创建国际化异常
     *
     * @param messageCode 消息代码
     */
    public ServiceException(String messageCode) {
        super(I18nMessageUtil.getMessage(messageCode));
        this.messageCode = messageCode;
        this.args = null;
    }

    /**
     * 创建国际化异常（带参数）
     *
     * @param messageCode 消息代码
     * @param args        消息参数
     */
    public ServiceException(String messageCode, Object... args) {
        super(I18nMessageUtil.getMessage(messageCode, args));
        this.messageCode = messageCode;
        this.args = args;
    }

    /**
     * 创建国际化异常（带原因）
     *
     * @param messageCode 消息代码
     * @param cause       原始异常
     */
    public ServiceException(String messageCode, Throwable cause) {
        super(I18nMessageUtil.getMessage(messageCode), cause);
        this.messageCode = messageCode;
        this.args = null;
    }

    /**
     * 创建国际化异常（带参数和原因）
     *
     * @param messageCode 消息代码
     * @param args        消息参数
     * @param cause       原始异常
     */
    public ServiceException(String messageCode, Object[] args, Throwable cause) {
        super(I18nMessageUtil.getMessage(messageCode, args), cause);
        this.messageCode = messageCode;
        this.args = args;
    }

    /**
     * 获取国际化消息
     *
     * @return 国际化后的消息
     */
    public String getLocalizedMessage() {
        return I18nMessageUtil.getMessage(messageCode, args);
    }

    /**
     * 获取消息代码
     *
     * @return 消息代码
     */
    public String getMessageCode() {
        return messageCode;
    }

    /**
     * 获取消息参数
     *
     * @return 消息参数数组
     */
    public Object[] getArgs() {
        return args;
    }
}
