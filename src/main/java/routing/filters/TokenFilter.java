package routing.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

public class TokenFilter extends ZuulFilter {
    private static final String PATH_LOGIN = "/account/{lang}/v1/login";
    private static final String PATH_REGISTRATION = "/account/{lang}/v1";
    private static final String PATH_VALIDATION = "https://propetsaccountin.herokuapp.com/account/{lang}/v1/validation/update";

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String[] tempPath = request.getServletPath().split("/");
        boolean pathWithToken = isPathWithToke(tempPath, request);
        if (pathWithToken) {
            return null;
        }

        String token = request.getHeader("X-Token");
        if (token == null) {
            throw new ZuulException("Request with X-Token", 404, "Request with X-Token");
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Token", token);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(PATH_VALIDATION);
        RequestEntity<String> tokenRequest = new RequestEntity<>(headers, HttpMethod.PUT, uriBuilder.build().toUri());
        ResponseEntity<String> response =
                restTemplate.exchange(tokenRequest, String.class);
        boolean valid = Boolean.getBoolean(response.getHeaders().getFirst("Valid"));
        if (!valid) {
            throw new ZuulException("Request with X-Token", 404, "Token not a valid");
        }
        token = response.getHeaders().getFirst("X-Token");
        ctx.addZuulRequestHeader("X-Token", token);
        return null;
    }

    private boolean isPathWithToke(String[] tempPath, HttpServletRequest request) {
        boolean register = tempPath[1].equals("account") && tempPath[tempPath.length - 1].equals("login") && request.getMethod().equals("POST");
        boolean login = tempPath[1].equals("account") && tempPath[tempPath.length - 1].equals("v1") && request.getMethod().equals("POST");
        boolean validate = tempPath[1].equals("account") && tempPath[tempPath.length - 1].equals("update") && request.getMethod().equals("PUT");
        if (register || login||validate){
            return true;
        }else return false;
    }
}
