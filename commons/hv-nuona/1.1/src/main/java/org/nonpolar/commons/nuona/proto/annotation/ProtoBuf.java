/*
 * Copyright 2002-2007 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nonpolar.commons.nuona.proto.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义java与第三方之间的通信协议
 * @author shanguoming 2015年1月12日 下午5:52:47
 * @version V1.0   
 * @modify: {原因} by shanguoming 2015年1月12日 下午5:52:47
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ProtoBuf {
	
	/**
	 * 定义字段是否必填
	 * @author shanguoming 2015年1月12日 下午5:51:42
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月12日 下午5:51:42
	 */
	boolean required() default false;
	
	/**
	 * 设置顺序，从1开始
	 * @author shanguoming 2015年1月12日 下午5:51:24
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月12日 下午5:51:24
	 */
	int order() default 0;
	
	/**
	 * 设置备注
	 * @author shanguoming 2015年1月12日 下午5:51:35
	 * @return
	 * @modify: {原因} by shanguoming 2015年1月12日 下午5:51:35
	 */
	String description() default "";
}
