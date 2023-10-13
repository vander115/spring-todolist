package br.com.vandersuncat.springtodolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.vandersuncat.springtodolist.user.IUserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var servletPath = request.getServletPath();

    if (!servletPath.startsWith("/task/")) {
      filterChain.doFilter(request, response);
      return;
    }
    // Pegar a autenticação (usuário e senha)
    var authorization = request.getHeader("Authorization");
    var user_data = authorization.substring("Basic".length()).trim();

    byte[] auth = Base64.getDecoder().decode(user_data);

    var user_pass = new String(auth);

    String[] credentials = user_pass.split(":");

    var username = credentials[0];
    var password = credentials[1];

    var user = this.userRepository.findByUsername(username);

    // Validar usuário
    if (user == null) {
      response.sendError(401);
    } else {
      // Validar Senha
      var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
      if (passwordVerify.verified) {
        request.setAttribute("idUser", user.getId());
        filterChain.doFilter(request, response);
      } else {
        response.sendError(401);
      }

    }
  }

}
