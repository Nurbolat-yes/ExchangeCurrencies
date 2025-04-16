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

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        ExchangeRatesDao exchangeRatesDao = ExchangeRatesDao.getInstance();
        PrintWriter pw = response.getWriter();

        String codeFrom = request.getParameter("from");
        String codeTo = request.getParameter("to");
        int amount = Integer.parseInt(request.getParameter("amount"));
        int wayForExchange = Integer.parseInt(request.getParameter("way"));

        try {
            ExchangeRates exchangeRates = exchangeRatesDao.findByCode(codeFrom+codeTo);

            if (wayForExchange == 1){
                String convertInfo = "amount: " + amount +"\n" +"convertedAmount: " + exchangeRates.getRate().multiply(new BigDecimal(amount));

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
