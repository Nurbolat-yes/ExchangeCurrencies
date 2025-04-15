package by.nurbolat.exchangerate.servlets;

import by.nurbolat.exchangerate.dao.CurrenciesDao;
import by.nurbolat.exchangerate.entity.Currencies;
import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.utils.ErrorResponse;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/specificCurrency/*")
public class SpecificCurrencyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CurrenciesDao currenciesDao = CurrenciesDao.getInstance();
        PrintWriter pw = response.getWriter();

        String url = String.valueOf(request.getRequestURL());
        String[] splittedUrl = url.split("/");
        String code = splittedUrl[splittedUrl.length - 1];

        if (code.length() != 3){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse message = new ErrorResponse("wrong format of Code Currency");
            String result = new Gson().toJson(message.toString());
            pw.println(result);
            pw.flush();
            return;
        }

        Currencies currency = null;
        try {
            currency = currenciesDao.findCurrencyByCode(code);
        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String result = new Gson().toJson(e.toString());
            pw.println(result);
            pw.flush();
        }

        if (currency.getId() == 0){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ErrorResponse message = new ErrorResponse("Currency not found");
            String result = new Gson().toJson(message);
            pw.println(result);
            pw.flush();
            return;
        }

        String json = new Gson().toJson(currency);
        pw.println(json);
        pw.flush();
    }
}
