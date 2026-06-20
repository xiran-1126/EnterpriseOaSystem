package com.oa.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.oa.common.BusinessType;
import com.oa.common.OperationLog;
import com.oa.entity.SysOperLog;
import com.oa.service.SysOperLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Aspect
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperLogService sysOperLogService;

    @Pointcut("@annotation(com.oa.common.OperationLog)")
    public void operLogPointCut() {
    }

    @AfterReturning(pointcut = "operLogPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    @AfterThrowing(pointcut = "operLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    @Async
    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            OperationLog controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }

            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(1);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                operLog.setOperName(authentication.getName());
            }

            if (e != null) {
                operLog.setStatus(0);
                operLog.setErrorMsg(e.getMessage());
            }

            HttpServletRequest request = getRequest();
            if (request != null) {
                operLog.setOperIp(getIpAddr(request));
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
            }

            operLog.setBusinessType(controllerLog.businessType().getDescription());
            operLog.setModuleName(controllerLog.moduleName());
            operLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName());
            operLog.setOperTime(LocalDateTime.now());

            if (controllerLog.saveRequestParam()) {
                Object[] args = joinPoint.getArgs();
                operLog.setOperParam(argsArrayToString(args));
            }

            if (controllerLog.saveResponseData() && jsonResult != null) {
                try {
                    operLog.setJsonResult(JSONUtil.toJsonStr(jsonResult));
                } catch (Exception ex) {
                    log.warn("序列化响应数据失败", ex);
                }
            }

            sysOperLogService.saveOperLog(operLog);
        } catch (Exception exp) {
            log.error("操作日志记录异常", exp);
        }
    }

    private String argsArrayToString(Object[] paramsArray) {
        if (ArrayUtil.isEmpty(paramsArray)) {
            return "";
        }
        try {
            StringBuilder params = new StringBuilder();
            for (Object o : paramsArray) {
                if (o != null && !isFilterObject(o)) {
                    try {
                        params.append(JSONUtil.toJsonStr(o)).append(" ");
                    } catch (Exception e) {
                        log.warn("序列化参数失败", e);
                    }
                }
            }
            return params.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof BindingResult;
    }

    private OperationLog getAnnotationLog(JoinPoint joinPoint) {
        try {
            return joinPoint.getTarget().getClass()
                    .getMethod(joinPoint.getSignature().getName(), getParameterTypes(joinPoint))
                    .getAnnotation(OperationLog.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Class<?>[] getParameterTypes(JoinPoint joinPoint) {
        return ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
