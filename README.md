💱 Currency Exchange API (Java + Postgresql)

📌 Project Overview
A lightweight REST API for managing currencies, exchange rates, and performing real-time currency conversions. Built with Java Servlets and Postgresql, without using any frameworks.

The project implements core CRUD operations (Create, Read, Update) for both currencies and exchange rates, enabling full control over the currency database.

⚠️ No frontend/web interface — interaction is done via HTTP requests (GET, POST, PATCH).
  

🚀 Features
List all currencies

Get details of a specific currency

Add new currencies

View and add exchange rates

Update existing exchange rates

Convert currency amounts using: in http://localhost:8080/exchange?from=USD&to=EUR&amount=10&way=1 

parameter way means the way of exchange

Direct rates way=1

Inverse rates way=2

Cross-rates via USD way=3

🗃️ Database Structure

Table: Currencies

Column	Type	Description
ID	int	Auto-increment primary key
Code	varchar	Unique 3-letter currency code (e.g. USD)
FullName	varchar	Full name of the currency
Sign	varchar	Currency symbol

Table: ExchangeRates

Column	Type	Description
ID	int	Auto-increment primary key
BaseCurrencyId	int	Foreign key → Currencies.ID
TargetCurrencyId	int	Foreign key → Currencies.ID
Rate	decimal(6)	Exchange rate from base to target currency

🛠 Technologies Used
Java (Servlets)

JDBC

Postgresql

HTTP (GET, POST, PATCH)

JSON

Maven

💻 How to Run Locally
1.Clone the repository:
  git clone https://github.com/Nurbolat-yes/ExchangeCurrencies.git

2.Import the project as a Maven project in IntelliJ IDEA or Eclipse.

3.Make sure Tomcat and SQLite are installed.

4.Deploy the generated .war file to Tomcat.

5.Access the API at:
  http://localhost:8080/ExchangeCurrencies

Postman for http requests: https://nurbolat-7685145.postman.co/workspace/Nurbolat's-Workspace~96e8c5b9-434f-432e-8cc6-e3b7aef71c9e/collection/43539133-3a5aa6fc-fa7e-4416-95c1-6bd49c1e934a?action=share&creator=43539133
