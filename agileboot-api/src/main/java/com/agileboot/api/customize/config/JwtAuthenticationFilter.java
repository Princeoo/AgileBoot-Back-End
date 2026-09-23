package com.agileboot.api.customize.config;

import com.agileboot.api.customize.service.MiniappTokenService;
import com.agileboot.api.customize.service.MiniappTokenService.TokenSession;
import com.agileboot.domain.iam.auth.MiniappAuthApplicationService;
import com.agileboot.infrastructure.user.miniapp.MiniappLoginUser;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * token过滤器 验证token有效性
 * 继承OncePerRequestFilter类的话  可以确保只执行filter一次， 避免执行多次
 * @author valarchie
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final MiniappTokenService tokenService;

    private final MiniappAuthApplicationService authApplicationService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
            && "/miniapp/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tokenFromRequest = tokenService.getTokenFromRequest(request);

        if (tokenFromRequest != null && !tokenFromRequest.trim().isEmpty()) {
            TokenSession tokenSession = tokenService.authenticate(tokenFromRequest);
            authApplicationService.assertLoginAllowed(tokenSession.getSession().getSubjectId());
            MiniappLoginUser loginUser = tokenSession.getSession().getPrincipal();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null,
                loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
