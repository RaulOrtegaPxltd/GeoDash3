package com.pxltd.geodash;

public class ServiceException extends Exception {
	private static final long serialVersionUID = 1L;
	private GeoExceptionCodes _errorCode;
	
	public enum GeoExceptionCodes{
		BAD_REQUEST(10600),COMMUNICATION_ERROR(10601),QUOTA_REACHED(10602),INVALID_KEY(10603);
		private final int errorCode;
		GeoExceptionCodes(int num){
			this.errorCode = num;
		}
	}
	
	public ServiceException(String msg, GeoExceptionCodes gec){
		super(msg);
		_errorCode = gec;		
	}
	
    public ServiceException(String message) {
        super(message);
    }
	
	public GeoExceptionCodes getExceptionCode(){
		return _errorCode;
	}
	
	public int getIntExceptionCode(){
		return _errorCode.errorCode;
	}
}
