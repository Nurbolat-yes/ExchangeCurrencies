package by.nurbolat.exchangerate.servlets;

import by.nurbolat.exchangerate.dao.ExchangeRatesDao;
import by.nurbolat.exchangerate.entity.ExchangeRates;
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
import java.util.List;

@WebServlet("/exchangeRates/*")
public class ExchangeRatesServlet extends HttpServlet {
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

        ExchangeRatesDao exchangeRatesDao = ExchangeRatesDao.getInstance();
        try {
             List<ExchangeRates> exchangeRates = exchangeRatesDao.findAll();

             String result = new Gson().toJson(exchangeRates);
             pw.println(result);
             pw.flush();

             response.setStatus(HttpServletResponse.SC_OK);

        } catch (DatabaseAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String result = new Gson().toJson(e.toString());
            pw.println(result);
            pw.flush();
            throw new RuntimeException(e);
        }
    }
}
