package shortlink.util.http;

/*
* API response
*
* This is template for return data on response body with format (status, message, data)
*
* */
public class ApiResponse<T> {
    /*
    * Status
    * */
    private int status;

    /*
    * Message
    * */
    private String message;

    /*
    * Data
    * */
    private T data;

    /*
    * Constructor
    * */
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /*
    * Get status
    *
    * return status
    * */
    public int getStatus() {
        return status;
    }

    /*
    * Get message
    *
    * return message
    * */
    public String getMessage() {
        return message;
    }

    /*
    * Get data
    *
    * return data
    * */
    public T getData() {
        return data;
    }

}
