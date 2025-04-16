package by.nurbolat.exchangerate.dao;

import by.nurbolat.exchangerate.entity.Currencies;
import by.nurbolat.exchangerate.entity.ExchangeRates;
import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.utils.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRatesDao implements Dao<ExchangeRates>{
    private static final ExchangeRatesDao INSTANCE = new ExchangeRatesDao();
    private static final CurrenciesDao currenciesDao = CurrenciesDao.getInstance();

    private static final String FIND_ALL_SQL = """
            SELECT id, basecurrencyid, targetcurrencyid, rate FROM exchangerates
            """;

    private final static String FIND_EXCHANGERATE_BY_PAIR_OF_IDS =
            FIND_ALL_SQL + " WHERE basecurrencyid = ? and targetcurrencyid = ?";

    private final static String FIND_ID_BY_CODE = """
            SELECT id FROM currencies c
            WHERE c.code = ?
            """;


    @Override
    public ExchangeRates save(String name, String code, String sign) throws DatabaseAccessException, RuntimeException {
        return null;
    }

    @Override
    public ExchangeRates findByCode(String pairOfCodes) throws DatabaseAccessException {
       String baseCode = pairOfCodes.substring(0,3);
       String targetCode = pairOfCodes.substring(3,6);

       String[] codes = new String[2];
       codes[0] = baseCode;
       codes[1] = targetCode;

       ExchangeRates exchangeRates = new ExchangeRates();

       try (var connection = ConnectionManager.get();
            var statement = connection.prepareStatement(FIND_ID_BY_CODE)){
           List<Integer> ids = new ArrayList<>(2);

           for (int i = 0; i < codes.length; i++) {
               statement.setString(1,codes[i]);

               ResultSet resultSet = statement.executeQuery();
               while (resultSet.next()){
                   ids.add(resultSet.getInt("id"));
               }

           }

           var statement2 = connection.prepareStatement(FIND_EXCHANGERATE_BY_PAIR_OF_IDS, Statement.RETURN_GENERATED_KEYS);
           statement2.setInt(1,ids.get(0));
           statement2.setInt(2,ids.get(1));

           ResultSet resultSet =  statement2.executeQuery();
           while (resultSet.next()){
               exchangeRates.setId(resultSet.getInt("id"));
               exchangeRates.setBaseCurrency(currenciesDao.findCurrencyById(resultSet.getInt("basecurrencyid"),connection));
               exchangeRates.setTargetCurrency(currenciesDao.findCurrencyById(resultSet.getInt("targetcurrencyid"),connection));
               exchangeRates.setRate(resultSet.getBigDecimal("rate"));
           }

           ids.clear();
           statement2.close();

           return exchangeRates;
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
    }

    @Override
    public List<ExchangeRates> findAll() throws DatabaseAccessException {
        List<ExchangeRates> exchangeRates = new ArrayList<>();

        try(var connection = ConnectionManager.get();
            var statement = connection.prepareStatement(FIND_ALL_SQL)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                exchangeRates.add(new ExchangeRates(
                        resultSet.getInt("id"),
                        currenciesDao.findCurrencyById(resultSet.getInt("basecurrencyid"),connection),
                        currenciesDao.findCurrencyById(resultSet.getInt("targetcurrencyid"),connection),
                        resultSet.getBigDecimal("rate")
                ));
            }
            return exchangeRates;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ExchangeRatesDao getInstance(){
        return INSTANCE;
    }

    private ExchangeRatesDao(){}
}
