package by.nurbolat.exchangerate.servlets;

import by.nurbolat.exchangerate.dao.ExchangeRatesDao;
import by.nurbolat.exchangerate.entity.ExchangeRates;
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

@WebServlet("/specificExchangeRate/*")
public class SpecificExchangeRateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ExchangeRatesDao exchangeRatesDao = ExchangeRatesDao.getInstance();
        PrintWriter pw = response.getWriter();

        String url = String.valueOf(request.getRequestURL());
        String[] splittedUrl = url.split("/");
        String pairOfCodes = splittedUrl[splittedUrl.length - 1];

        if (pairOfCodes.length() != 6){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse message = new ErrorResponse("wrong format of Code Currency");
            String result = new Gson().toJson(message.toString());
            pw.println(result);
            pw.flush();
            return;
        }

        ExchangeRates exchangeRates = null;
        try {
            exchangeRates = exchangeRatesDao.findByCode(pairOfCodes);
        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String result = new Gson().toJson(e.toString());
            pw.println(result);
            pw.flush();
        }

        if (exchangeRates.getId() == 0){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ErrorResponse message = new ErrorResponse("Pair of Exchange Currency not found");
            String result = new Gson().toJson(message);
            pw.println(result);
            pw.flush();
            return;
        }

        String json = new Gson().toJson(exchangeRates);
        pw.println(json);
        pw.flush();
    }
}
