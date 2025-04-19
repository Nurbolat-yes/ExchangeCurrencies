package by.nurbolat.exchangerate.servlets;

import by.nurbolat.exchangerate.dao.CurrenciesDao;
import by.nurbolat.exchangerate.dao.ExchangeRatesDao;
import by.nurbolat.exchangerate.entity.Currencies;
import by.nurbolat.exchangerate.entity.ExchangeRates;
import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.exceptions.DuplicateKeyValueExceptions;
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
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
    ExchangeRatesDao exchangeRatesDao = ExchangeRatesDao.getInstance();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        PrintWriter pw = response.getWriter();

        String url = String.valueOf(request.getRequestURL());
        String[] splittedUrl = url.split("/");
        String pairOfCodes = splittedUrl[splittedUrl.length -1];

        if (pairOfCodes.length() <= 6 && pairOfCodes.length() > 0 ){
            RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/specificExchangeRate/" + pairOfCodes);
            requestDispatcher.forward(request,response);
        }

        try {
             List<ExchangeRates> exchangeRates = exchangeRatesDao.findAll();

             String result = new Gson().toJson(exchangeRates);
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

        CurrenciesDao currenciesDao = CurrenciesDao.getInstance();
        PrintWriter pw = response.getWriter();

        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        BigDecimal rate = new BigDecimal(request.getParameter("rate"));

        if (baseCurrencyCode == null || targetCurrencyCode == null || rate.equals(new BigDecimal("0"))){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse message = new ErrorResponse("A required form field is missing");

            pw.println(new Gson().toJson(message));
            pw.flush();
            return;
        }

        try {
            Currencies base = currenciesDao.findByCode(baseCurrencyCode);
            Currencies target = currenciesDao.findByCode(targetCurrencyCode);

            if (base.getId() == 0 || target.getId() == 0){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                ErrorResponse message = new ErrorResponse("Currency (or currencies) by this code does not exist in Database");
                pw.println(new Gson().toJson(message));
                pw.flush();
                return;
            }

            ExchangeRates tempExchangeRates = new ExchangeRates();
            tempExchangeRates.setBaseCurrency(base);
            tempExchangeRates.setTargetCurrency(target);
            tempExchangeRates.setRate(rate);

            ExchangeRates exchangeRates = exchangeRatesDao.save(tempExchangeRates);

            String result = new Gson().toJson(exchangeRates);
            pw.println(result);
            pw.flush();


        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String result = new Gson().toJson(e.toString());
            pw.println(result);
            pw.flush();
            throw new RuntimeException(e);
        } catch (DuplicateKeyValueExceptions e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);

            ErrorResponse message = new ErrorResponse(e.getMessage());

            String result = new Gson().toJson(message);
            pw.println(result);
            pw.flush();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter pw = response.getWriter();

        BigDecimal rate = new BigDecimal(request.getParameter("rate"));


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

        ExchangeRates updatedExchangeRates = null;
        try {
            ExchangeRates exchangeRates = exchangeRatesDao.findByCode(pairOfCodes);
            exchangeRates.setRate(rate);
            updatedExchangeRates = exchangeRatesDao.updateRate(exchangeRates);
            if (updatedExchangeRates == null){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                ErrorResponse message = new ErrorResponse("Pair of Exchange Currency not found");
                String result = new Gson().toJson(message);
                pw.println(result);
                pw.flush();
                return;
            }
        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String result = new Gson().toJson(e.toString());
            pw.println(result);
            pw.flush();
        }

        String json = new Gson().toJson(updatedExchangeRates);
        pw.println(json);
        pw.flush();
        
    }
}
