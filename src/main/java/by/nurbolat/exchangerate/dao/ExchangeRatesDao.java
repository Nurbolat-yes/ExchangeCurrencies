package by.nurbolat.exchangerate.dao;

import by.nurbolat.exchangerate.entity.Currencies;
import by.nurbolat.exchangerate.entity.ExchangeRates;
import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.exceptions.DuplicateKeyValueExceptions;
import by.nurbolat.exchangerate.utils.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
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

    private final static String FIND_EXCHANGE_CURRENCY_BY_PAIR_OF_IDS_SQL =
            FIND_ALL_SQL + " WHERE basecurrencyid = ? and targetcurrencyid = ?";

    private final static String FIND_ID_BY_CODE_SQL = """
            SELECT id FROM currencies c
            WHERE c.code = ?
            """;

    private final static String SAVE_EXCHANGE_CURRENCY_SQL = """
            INSERT INTO exchangerates(basecurrencyid, targetcurrencyid, rate)
            VALUES (?,?,?)  
            """;

    private final static String UPDATE_EXCHANGE_RATE_SQL = """
            UPDATE exchangerates SET rate = ? WHERE basecurrencyid = ? AND targetcurrencyid = ?
            """;

    public ExchangeRates updateRate(ExchangeRates exchangeRates) throws DatabaseAccessException {
        ExchangeRates exchangeRate = new ExchangeRates();
        try(var connection = ConnectionManager.get();
            var statement = connection.prepareStatement(UPDATE_EXCHANGE_RATE_SQL)) {

            statement.setBigDecimal(1,exchangeRates.getRate());
            statement.setInt(2,exchangeRates.getBaseCurrency().getId());
            statement.setInt(3,exchangeRates.getTargetCurrency().getId());

            statement.executeUpdate();

            exchangeRate.setId(exchangeRates.getId());
            exchangeRate.setBaseCurrency(exchangeRates.getBaseCurrency());
            exchangeRate.setTargetCurrency(exchangeRates.getTargetCurrency());
            exchangeRate.setRate(exchangeRates.getRate());

            return exchangeRate;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public ExchangeRates save(ExchangeRates exchangeRates) throws DatabaseAccessException, RuntimeException, DuplicateKeyValueExceptions {
        ExchangeRates exchangeRate = new ExchangeRates();

        try(var connection = ConnectionManager.get();
            var statement = connection.prepareStatement(SAVE_EXCHANGE_CURRENCY_SQL,Statement.RETURN_GENERATED_KEYS)){

            statement.setInt(1,exchangeRates.getBaseCurrency().getId());
            statement.setInt(2,exchangeRates.getTargetCurrency().getId());
            statement.setBigDecimal(3,exchangeRates.getRate());

            statement.executeUpdate();

            exchangeRate.setBaseCurrency(exchangeRates.getBaseCurrency());
            exchangeRate.setTargetCurrency(exchangeRates.getTargetCurrency());
            exchangeRate.setRate(exchangeRates.getRate());

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()){
                exchangeRate.setId(keys.getInt("id"));
            }

            return exchangeRate;
        } catch (RuntimeException e) {
            throw new DatabaseAccessException(e.getMessage());
        }catch (SQLException e){
            throw new DuplicateKeyValueExceptions(e.getMessage());
        }
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
            var statement = connection.prepareStatement(FIND_ID_BY_CODE_SQL)){
           List<Integer> ids = new ArrayList<>(2);

           for (int i = 0; i < codes.length; i++) {
               statement.setString(1,codes[i]);

               ResultSet resultSet = statement.executeQuery();
               while (resultSet.next()){
                   ids.add(resultSet.getInt("id"));
               }

           }

           var statement2 = connection.prepareStatement(FIND_EXCHANGE_CURRENCY_BY_PAIR_OF_IDS_SQL, Statement.RETURN_GENERATED_KEYS);
           statement2.setInt(1,ids.get(0));
           statement2.setInt(2,ids.get(1));
           ids.clear();


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
