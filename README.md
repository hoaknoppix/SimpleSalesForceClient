# SimpleSalesForceClient
An simple salesforce client for java

How to use:
```java
SalesForceClient salesForceClient = new SalesForceClient("your_user_name", "your_password", "your_security_token", "your_domain(eg test or dev)", "sales_force_version(e.g. 42.0)");
QueryResponse queryResponse = salesForceClient.query("Select firstName from Contact"); //SOQL
```
