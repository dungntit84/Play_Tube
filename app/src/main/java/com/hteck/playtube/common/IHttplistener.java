package com.hteck.playtube.common;

public interface IHttplistener {
	enum ResultType {
		Done,
		ConnectionError,
		Error
	}
	void returnResult(Object sender, byte[] data, ResultType resultType);
}
