package com.gimis.ps.msg;

import java.io.Serializable;

 

public class DefaultEnd implements Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 4110362857097114305L;
	
	
	private Short crcCode;

    public Short getCrcCode()
    {
        return crcCode;
    }

    public void setCrcCode(Short crcCode)
    {
        this.crcCode = crcCode;
    }
}
