package br.com.coutbiaafnts.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.coutbiaafnts.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

   @Autowired
   private IUserRepository userRepository;

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException, IOException {

            // get authentication (user e password) -> separando o usuário da senha
            var authorization = request.getHeader("Authorization");
            
            // ? tira o "Basic" e deixa só o resto (calculando o tamanho da palavra e removendo os espaços)
            var authEncoded = authorization.substring("Basic".length()).trim();

            // ? cria um decode (um array de bytes)
            byte [] authDecoded = Base64.getDecoder().decode(authEncoded);
            
            // ? converter o array de bytes para string
            var authString = new String(authDecoded);

            // ? ["username", "password"]
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // valid user no BD
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
               response.sendError(401);
            } else {
               // valid password no BD
               var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
               if (passwordVerify.verified){ // retorna (true || false)
                  filterChain.doFilter(request, response);
               } else {
                  response.sendError(401);
               }
               // go
   
   

            }

   }
   
}