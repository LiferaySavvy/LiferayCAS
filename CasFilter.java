{\rtf1\ansi\ansicpg1252\deff0\deflang1033{\fonttbl{\f0\fswiss\fcharset0 Arial;}}
{\*\generator Msftedit 5.41.15.1515;}\viewkind4\uc1\pard\f0\fs20 /**\par
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.\par
 *\par
 * This library is free software; you can redistribute it and/or modify it under\par
 * the terms of the GNU Lesser General Public License as published by the Free\par
 * Software Foundation; either version 2.1 of the License, or (at your option)\par
 * any later version.\par
 *\par
 * This library is distributed in the hope that it will be useful, but WITHOUT\par
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS\par
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more\par
 * details.\par
 */\par
\par
package com.liferay.portal.servlet.filters.sso.cas;\par
\par
import com.liferay.portal.kernel.log.Log;\par
import com.liferay.portal.kernel.log.LogFactoryUtil;\par
import com.liferay.portal.kernel.util.HttpUtil;\par
import com.liferay.portal.kernel.util.ParamUtil;\par
import com.liferay.portal.kernel.util.PropsKeys;\par
import com.liferay.portal.kernel.util.StringUtil;\par
import com.liferay.portal.kernel.util.Validator;\par
import com.liferay.portal.servlet.filters.BasePortalFilter;\par
import com.liferay.portal.util.PortalUtil;\par
import com.liferay.portal.util.PrefsPropsUtil;\par
import com.liferay.portal.util.PropsValues;\par
\par
import java.util.HashMap;\par
import java.util.Map;\par
import java.util.concurrent.ConcurrentHashMap;\par
\par
import javax.servlet.FilterChain;\par
import javax.servlet.http.Cookie;\par
import javax.servlet.http.HttpServletRequest;\par
import javax.servlet.http.HttpServletResponse;\par
import javax.servlet.http.HttpSession;\par
\par
import org.jasig.cas.client.authentication.AttributePrincipal;\par
import org.jasig.cas.client.util.CommonUtils;\par
import org.jasig.cas.client.validation.Assertion;\par
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;\par
import org.jasig.cas.client.validation.TicketValidator;\par
\par
/**\par
 * @author Michael Young\par
 * @author Brian Wing Shun Chan\par
 * @author Raymond Aug\'c3\'a9\par
 * @author Tina Tian\par
 * @author Zsolt Balogh\par
 */\par
public class CASFilter extends BasePortalFilter \{\par
\par
\tab public static String LOGIN = CASFilter.class.getName() + "LOGIN";\par
\par
\tab public static void reload(long companyId) \{\par
\tab\tab _ticketValidators.remove(companyId);\par
\tab\}\par
\par
\tab protected Log getLog() \{\par
\tab\tab return _log;\par
\tab\}\par
\par
\tab protected TicketValidator getTicketValidator(long companyId)\par
\tab\tab throws Exception \{\par
\par
\tab\tab TicketValidator ticketValidator = _ticketValidators.get(companyId);\par
\par
\tab\tab if (ticketValidator != null) \{\par
\tab\tab\tab return ticketValidator;\par
\tab\tab\}\par
\par
\tab\tab String serverName = PrefsPropsUtil.getString(\par
\tab\tab\tab companyId, PropsKeys.CAS_SERVER_NAME, PropsValues.CAS_SERVER_NAME);\par
\tab\tab String serverUrl = PrefsPropsUtil.getString(\par
\tab\tab\tab companyId, PropsKeys.CAS_SERVER_URL, PropsValues.CAS_SERVER_URL);\par
\tab\tab String loginUrl = PrefsPropsUtil.getString(\par
\tab\tab\tab companyId, PropsKeys.CAS_LOGIN_URL, PropsValues.CAS_LOGIN_URL);\par
\par
\tab\tab Cas20ProxyTicketValidator cas20ProxyTicketValidator =\par
\tab\tab\tab new Cas20ProxyTicketValidator(serverUrl);\par
\par
\tab\tab Map<String, String> parameters = new HashMap<String, String>();\par
\par
\tab\tab parameters.put("serverName", serverName);\par
\tab\tab parameters.put("casServerUrlPrefix", serverUrl);\par
\tab\tab parameters.put("casServerLoginUrl", loginUrl);\par
\tab\tab parameters.put("redirectAfterValidation", "false");\par
\par
\tab\tab cas20ProxyTicketValidator.setCustomParameters(parameters);\par
\par
\tab\tab _ticketValidators.put(companyId, cas20ProxyTicketValidator);\par
\par
\tab\tab return cas20ProxyTicketValidator;\par
\tab\}\par
\par
\tab protected void processFilter(\par
\tab\tab\tab HttpServletRequest request, HttpServletResponse response,\par
\tab\tab\tab FilterChain filterChain)\par
\tab\tab throws Exception \{\par
\par
\tab\tab long companyId = PortalUtil.getCompanyId(request);\par
\tab\tab String serverURL=request.getRequestURL().toString();\par
\tab\tab\par
\tab\tab System.out.println("#####################::serverURL before"+serverURL);\par
\tab\tab if (serverURL.indexOf("/portal/logout") != -1) \{\par
\tab\tab\tab\par
\tab\tab\tab serverURL=StringUtil.replace(serverURL,"/c/portal/logout"," ");\par
\tab\tab\}\par
\tab\tab if (serverURL.indexOf("/portal/login") != -1) \{\par
\tab\tab\tab serverURL=StringUtil.replace(serverURL,"/c/portal/login"," ");\par
\tab\tab\}\par
\tab\tab //if (serverURL.indexOf("http://")!= -1) \{\par
\tab\tab\tab //serverURL=StringUtil.replace(serverURL,"http://"," ");\par
\tab\tab\tab //System.out.println("#####################::httpBlock"+serverURL);\par
\tab\tab //\}\par
\tab\tab //if (serverURL.indexOf("https://")!= -1) \{\par
\tab\tab\tab //serverURL=StringUtil.replace(serverURL,"https://"," ");\par
\tab\tab\tab //System.out.println("#####################::httpsBlock"+serverURL);\par
\tab\tab //\}\par
\tab\tab serverURL=serverURL.trim();\par
\tab\tab System.out.println("===================="+serverURL);\par
\tab\tab\par
\tab\tab if (PrefsPropsUtil.getBoolean(\par
\tab\tab\tab\tab companyId, PropsKeys.CAS_AUTH_ENABLED,\par
\tab\tab\tab\tab PropsValues.CAS_AUTH_ENABLED)) \{\par
\par
\tab\tab\tab HttpSession session = request.getSession();\par
\tab\tab\tab String pathInfo = request.getPathInfo();\par
\tab\tab\tab System.out.println("***************"+pathInfo);\par
\tab\tab\tab System.out.println("***************####"+pathInfo.indexOf("/portal/logout"));\par
\par
\tab\tab\tab if (pathInfo.indexOf("/portal/logout") != -1) \{\par
\tab\tab\tab\tab session.invalidate();\par
\tab\tab\tab\tab //Cookie cookie = new Cookie("JSESSIONID", null);\par
\tab\tab\tab     //cookie.setPath(request.getContextPath());\par
\tab\tab\tab    // cookie.setMaxAge(0);\par
\tab\tab\tab    // response.addCookie(cookie);\par
\tab\tab\tab\tab String logoutUrl = PrefsPropsUtil.getString(\par
\tab\tab\tab\tab\tab companyId, PropsKeys.CAS_LOGOUT_URL,\par
\tab\tab\tab\tab\tab PropsValues.CAS_LOGOUT_URL);\par
\tab\tab\tab\tab System.out.println("***************####logoutUrl"+logoutUrl);\par
\tab\tab\tab\tab System.out.println("***************####serverURL"+serverURL);\par
\tab\tab\tab\tab logoutUrl = HttpUtil.addParameter(\par
\tab\tab\tab\tab\tab\tab logoutUrl, "logoutUrl", serverURL);\par
\tab\tab\tab\tab\tab System.out.println("^^^^^^^^^^^^^^^logoutUrl"+logoutUrl);\par
\tab\tab\tab\tab response.sendRedirect(logoutUrl);\par
\tab\tab\tab\tab return ;\par
\tab\tab\tab\}\par
\tab\tab\tab else \{\par
\tab\tab\tab\tab String login = (String)session.getAttribute(LOGIN);\par
\tab\tab\tab\tab\par
\tab\tab\tab\tab System.out.println("11111111111111111111"+login);\par
\tab\tab\tab\tab //String serverURL=request.getRequestURL().toString();\par
\tab\tab\tab\tab //serverURL=serverURL.replace("/c/portal/login","");\par
\tab\tab\tab\tab //serverURL=StringUtil.replace(serverURL,"/c/portal/login"," ");\par
\tab\tab\tab\tab //System.out.println("#####################::serverURL before"+serverURL);\par
\tab\tab\tab\tab //if (serverURL.indexOf("http://")!= -1) \{\par
\tab\tab\tab\tab\tab //serverURL=StringUtil.replace(serverURL,"http://"," ");\par
\tab\tab\tab\tab\tab //System.out.println("#####################::httpBlock"+serverURL);\par
\tab\tab\tab\tab //\}\par
\tab\tab\tab\tab //if (serverURL.indexOf("https://")!= -1) \{\par
\tab\tab\tab\tab\tab //serverURL=StringUtil.replace(serverURL,"https://"," ");\par
\tab\tab\tab\tab\tab //System.out.println("#####################::httpsBlock"+serverURL);\par
\tab\tab\tab\tab //\}\par
\tab\tab\tab\tab //String serverName = PrefsPropsUtil.getString(\par
\tab\tab\tab\tab\tab //companyId, PropsKeys.CAS_SERVER_NAME,\par
\tab\tab\tab\tab\tab //PropsValues.CAS_SERVER_NAME);\par
\tab\tab\tab\tab String serverName = request.getServerName()+":8080";\par
\tab\tab\tab\tab //System.out.println("#####################::getRemoteAddr"+request.getRemoteAddr());\par
\tab\tab\tab\tab //System.out.println("#####################getRemoteHost::"+request.getRemoteHost());\par
\tab\tab\tab\tab //System.out.println("######################getRemotePort::"+request.getRemotePort());\par
\tab\tab\tab\tab //System.out.println("#####################getRequestURI"+request.getRequestURI());\par
\tab\tab\tab\tab //System.out.println("######################getLocalPort"+request.getLocalPort());\par
\tab\tab\tab\tab //System.out.println("######################getLocalAddr"+request.getLocalAddr());\par
\tab\tab\tab\tab //System.out.println("######################getPathInfo"+request.getPathInfo());\par
\tab\tab\tab\tab //System.out.println("######################getQueryString"+request.getQueryString());\par
\tab\tab\tab\tab //System.out.println("######################getServerPort"+request.getServerPort());\par
\tab\tab\tab\tab //System.out.println("######################getRequestURL"+request.getRequestURL());\tab\par
\tab\tab\tab\tab //System.out.println("22222222222222222222222"+serverName);\par
\tab\tab\tab\tab String serviceUrl = PrefsPropsUtil.getString(\par
\tab\tab\tab\tab\tab companyId, PropsKeys.CAS_SERVICE_URL,\par
\tab\tab\tab\tab\tab PropsValues.CAS_SERVICE_URL);\par
\tab\tab\tab\tab\par
\tab\tab\tab\tab System.out.println("33333333333333333333333"+serviceUrl);\par
\tab\tab\tab\tab if (Validator.isNull(serviceUrl)) \{\par
\tab\tab\tab\tab\tab\par
\tab\tab\tab\tab\tab System.out.println("******************************&&&&&&&&&&&&"+serviceUrl);\par
\tab\tab\tab\tab\tab serviceUrl = CommonUtils.constructServiceUrl(\par
\tab\tab\tab\tab\tab\tab request, response, serviceUrl, serverURL, "ticket",\par
\tab\tab\tab\tab\tab\tab false);\par
\tab\tab\tab\tab\tab System.out.println("4444444444444444444444"+serviceUrl);\par
\tab\tab\tab\tab\}\par
\par
\tab\tab\tab\tab String ticket = ParamUtil.getString(request, "ticket");\par
\tab\tab\tab\tab System.out.println("5555555555555555555555555"+ticket);\par
\par
\tab\tab\tab\tab if (Validator.isNull(ticket)) \{\par
\tab\tab\tab\tab\tab if (Validator.isNotNull(login)) \{\par
\tab\tab\tab\tab\tab\tab processFilter(\par
\tab\tab\tab\tab\tab\tab\tab CASFilter.class, request, response, filterChain);\par
\tab\tab\tab\tab\tab\}\par
\tab\tab\tab\tab\tab else \{\par
\tab\tab\tab\tab\tab\tab String loginUrl = PrefsPropsUtil.getString(\par
\tab\tab\tab\tab\tab\tab\tab companyId, PropsKeys.CAS_LOGIN_URL,\par
\tab\tab\tab\tab\tab\tab\tab PropsValues.CAS_LOGIN_URL);\par
\par
\tab\tab\tab\tab\tab\tab loginUrl = HttpUtil.addParameter(\par
\tab\tab\tab\tab\tab\tab\tab loginUrl, "service", serviceUrl);\par
\tab\tab\tab\tab\tab\tab System.out.println("66666666666666666666666666666"+loginUrl);\par
\par
\tab\tab\tab\tab\tab\tab response.sendRedirect(loginUrl);\par
\tab\tab\tab\tab\tab\}\par
\par
\tab\tab\tab\tab\tab return;\par
\tab\tab\tab\tab\}\par
\par
\tab\tab\tab\tab TicketValidator ticketValidator = getTicketValidator(\par
\tab\tab\tab\tab\tab companyId);\par
\tab\tab\tab\tab System.out.println("77777777777777777777777"+ticketValidator);\par
\tab\tab\tab\tab Assertion assertion = ticketValidator.validate(\par
\tab\tab\tab\tab\tab ticket, serviceUrl);\par
\tab\tab\tab\tab System.out.println("888888888888888888888"+assertion);\par
\tab\tab\tab\tab if (assertion != null) \{\par
\tab\tab\tab\tab\tab AttributePrincipal attributePrincipal =\par
\tab\tab\tab\tab\tab\tab assertion.getPrincipal();\par
\tab\tab\tab\tab\tab login = attributePrincipal.getName();\par
\tab\tab\tab\tab\tab session.setAttribute(LOGIN, login);\par
\tab\tab\tab\tab\tab System.out.println("9999999999999999999999999"+login);\par
\tab\tab\tab\tab\}\par
\tab\tab\tab\}\par
\tab\tab\}\par
\par
\tab\tab processFilter(CASFilter.class, request, response, filterChain);\par
\tab\}\par
\par
\tab private static Log _log = LogFactoryUtil.getLog(CASFilter.class);\par
\par
\tab private static Map<Long, TicketValidator> _ticketValidators =\par
\tab\tab new ConcurrentHashMap<Long, TicketValidator>();\par
\par
\}\par
}
 