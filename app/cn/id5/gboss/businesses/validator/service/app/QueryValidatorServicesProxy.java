package cn.id5.gboss.businesses.validator.service.app;

public class QueryValidatorServicesProxy implements cn.id5.gboss.businesses.validator.service.app.QueryValidatorServices {
  private String _endpoint = null;
  private cn.id5.gboss.businesses.validator.service.app.QueryValidatorServices queryValidatorServices = null;
  
  public QueryValidatorServicesProxy() {
    _initQueryValidatorServicesProxy();
  }
  
  public QueryValidatorServicesProxy(String endpoint) {
    _endpoint = endpoint;
    _initQueryValidatorServicesProxy();
  }
  
  private void _initQueryValidatorServicesProxy() {
    try {
      queryValidatorServices = (new cn.id5.gboss.businesses.validator.service.app.QueryValidatorServicesServiceLocator()).getQueryValidatorServices();
      if (queryValidatorServices != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)queryValidatorServices)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)queryValidatorServices)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (queryValidatorServices != null)
      ((javax.xml.rpc.Stub)queryValidatorServices)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public cn.id5.gboss.businesses.validator.service.app.QueryValidatorServices getQueryValidatorServices() {
    if (queryValidatorServices == null)
      _initQueryValidatorServicesProxy();
    return queryValidatorServices;
  }
  
  public java.lang.String querySingle(java.lang.String userName_, java.lang.String password_, java.lang.String type_, java.lang.String param_) throws java.rmi.RemoteException{
    if (queryValidatorServices == null)
      _initQueryValidatorServicesProxy();
    return queryValidatorServices.querySingle(userName_, password_, type_, param_);
  }
  
  public java.lang.String queryBatch(java.lang.String userName_, java.lang.String password_, java.lang.String type_, java.lang.String param_) throws java.rmi.RemoteException{
    if (queryValidatorServices == null)
      _initQueryValidatorServicesProxy();
    return queryValidatorServices.queryBatch(userName_, password_, type_, param_);
  }
  
  
}