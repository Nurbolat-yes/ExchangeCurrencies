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
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        ExchangeRatesDao exchangeRatesDao = ExchangeRatesDao.getInstance();
        PrintWriter pw = response.getWriter();

        String codeCross = "USD";

        String codeFrom = request.getParameter("from");
        String codeTo = request.getParameter("to");

        if (codeFrom == null || codeTo == null || request.getParameter("amount") == null || request.getParameter("way") == null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse message = new ErrorResponse("A required form field is missing");

            pw.println(new Gson().toJson(message));
            pw.flush();
            return;
        }

        if (codeFrom.concat(codeTo).length() != 6){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse message = new ErrorResponse("wrong format of Code Currency");
            String result = new Gson().toJson(message.toString());
            pw.println(result);
            pw.flush();
            return;
        }

        int amount = Integer.parseInt(request.getParameter("amount"));
        int wayForExchange = Integer.parseInt(request.getParameter("way"));

        try {

            ExchangeRates exchangeRates = exchangeRatesDao.findByCode(codeFrom+codeTo);
            if (exchangeRates == null){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                ErrorResponse message = new ErrorResponse("Pair of Exchange Currency not found");
                String result = new Gson().toJson(message);
                pw.println(result);
                pw.flush();
                return;
            }

            if (wayForExchange == 1){
                String convertInfo = "amount: " + amount +"\n" +"Total: " + exchangeRates.getRate().multiply(new BigDecimal(amount));

                String result = new Gson().toJson(exchangeRates );
                pw.println(result);
                pw.println(convertInfo);
                pw.flush();
            }
            else if (wayForExchange == 2) {
                BigDecimal oneDevideByRate = new BigDecimal(1).divide(exchangeRates.getRate(),4, RoundingMode.CEILING);
                String convertInfo = "amount: " + amount +"\n" +"Total: " + oneDevideByRate.multiply(new BigDecimal(amount));

                String result = new Gson().toJson(exchangeRates );
                pw.println(result);
                pw.println(convertInfo);
                pw.flush();
            }
            else if (wayForExchange == 3) {
                BigDecimal rate1 = exchangeRatesDao.findByCode(codeFrom+codeCross).getRate();
                BigDecimal rate2 = exchangeRatesDao.findByCode(codeCross+codeTo).getRate();

                if (rate1 == null || rate2 == null){
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    ErrorResponse message = new ErrorResponse("This operation is not available for these pair of currencies");
                    String result = new Gson().toJson(message);
                    pw.println(result);
                    pw.flush();
                    return;
                }

                String convertInfo = "amount: " + amount +"\n" +"Total: " + rate1.multiply(rate2) + " by : "+codeCross;

                String result = new Gson().toJson(exchangeRates );
                pw.println(result);
                pw.println(convertInfo);
                pw.flush();
            }

        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            ErrorResponse message = new ErrorResponse(e.getMessage());

            String result = new Gson().toJson(message);
            pw.println(result);
            pw.flush();
            throw new RuntimeException(e);
        }

    }
}
