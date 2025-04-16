package by.nurbolat.exchangerate.dao;

import by.nurbolat.exchangerate.entity.Currencies;
import by.nurbolat.exchangerate.exceptions.DatabaseAccessException;
import by.nurbolat.exchangerate.utils.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CurrenciesDao implements Dao<Currencies>{
    private final static CurrenciesDao INSTANCE = new CurrenciesDao();

    private final static String FIND_ALL_SQL = """
            SELECT id, code, fullname, sign FROM currencies
            """;

    private final static String FIND_CURRENCY_BY_CODE =
            FIND_ALL_SQL + " WHERE code = ?";

    private final static String FIND_CURRENCY_BY_ID =
            FIND_ALL_SQL + " WHERE id = ?";

    private final static String SAVE_CURRENCY = """
            INSERT INTO currencies(code,fullname,sign) 
            VALUES(?,?,?) 
            """;

    @Override
    public Currencies save(String name,String code, String sign) throws DatabaseAccessException,RuntimeException {
        Currencies currency = new Currencies();

        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(SAVE_CURRENCY,Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1,code);
            statement.setString(2,name);
            statement.setString(3,sign);

            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next())
                currency.setId(keys.getInt("id"));

            currency.setCode(code);
            currency.setFullName(name);
            currency.setSign(sign);

            return currency;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }catch (RuntimeException e){
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    @Override
    public Currencies findByCode(String code) throws DatabaseAccessException {
        Currencies currency = new Currencies();
        try(var connection = ConnectionManager.get();
            var statement = connection.prepareStatement(FIND_CURRENCY_BY_CODE)){
            statement.setString(1,code);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                currency.setId(resultSet.getInt("id"));
                currency.setCode(resultSet.getString("Code"));
                currency.setFullName(resultSet.getString("fullName"));
                currency.setSign(resultSet.getString("sign"));
            }

            return currency;

        } catch (RuntimeException e) {
            throw new DatabaseAccessException(e.getMessage());
        }catch (SQLException e){
            throw  new RuntimeException(e);
        }
    }

    @Override
    public List<Currencies> findAll() throws DatabaseAccessException {
        List<Currencies> currencies = new ArrayList<>();

        try (var connection = ConnectionManager.get();
             var statement = connection.prepareStatement(FIND_ALL_SQL)){

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                currencies.add(new Currencies(
                        resultSet.getInt("id"),
                        resultSet.getString("Code"),
                        resultSet.getString("fullname"),
                        resultSet.getString("Sign")
                ));
            }

            return currencies;

        } catch (RuntimeException e) {
            throw new DatabaseAccessException(e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Currencies findCurrencyById(int id, Connection connection) throws DatabaseAccessException {
        Currencies currency = new Currencies();
        try(var statement = connection.prepareStatement(FIND_CURRENCY_BY_ID)){
            statement.setInt(1,id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                currency.setId(resultSet.getInt("id"));
                currency.setCode(resultSet.getString("Code"));
                currency.setFullName(resultSet.getString("fullName"));
                currency.setSign(resultSet.getString("sign"));
            }

            return currency;

        } catch (RuntimeException e) {
            throw new DatabaseAccessException(e.getMessage());
        }catch (SQLException e){
            throw  new RuntimeException(e);
        }
    }

    private CurrenciesDao(){
    }

    public static CurrenciesDao getInstance(){
        return INSTANCE;
    }
}
