package by.nurbolat.exchangerate.servlets;

import by.nurbolat.exchangerate.dao.CurrenciesDao;
import by.nurbolat.exchangerate.entity.Currencies;
import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.utils.ErrorResponse;
import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/currencies/*")
public class CurrenciesServlet extends HttpServlet {
    CurrenciesDao currenciesDao = CurrenciesDao.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        PrintWriter pw = response.getWriter();

        String url = String.valueOf(request.getRequestURL());
        String[] splittedUrl = url.split("/");
        String code = splittedUrl[splittedUrl.length -1];

        if (code.length() <= 3 && code.length() > 0 ){
            RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/specificCurrency/" + code);
            requestDispatcher.forward(request,response);
        }

        try {
            List<Currencies> currenciesList = currenciesDao.findAll();
            String result = new Gson().toJson(currenciesList);

            pw.println(result);
            pw.flush();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            ErrorResponse message = new ErrorResponse(e.getMessage());

            String result = new Gson().toJson(message);
            pw.println(result);
            pw.flush();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        PrintWriter pw = response.getWriter();

        String name = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");

        if (name == null || code == null || sign == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse message = new ErrorResponse("A required form field is missing");

            pw.println(new Gson().toJson(message));
            pw.flush();
            return;
        }
        Currencies tempCurrency = new Currencies();
        tempCurrency.setFullName(name);
        tempCurrency.setCode(code);
        tempCurrency.setSign(sign);

        try {
            Currencies currencies = currenciesDao.save(tempCurrency);

            if (currencies != null){
                response.setStatus(HttpServletResponse.SC_CREATED);
            }

            String result = new Gson().toJson(currencies);
            pw.println(result);
            pw.flush();

        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            ErrorResponse message = new ErrorResponse(e.getMessage());

            String result = new Gson().toJson(message);
            pw.println(result);
            pw.flush();
        }

    }
}
