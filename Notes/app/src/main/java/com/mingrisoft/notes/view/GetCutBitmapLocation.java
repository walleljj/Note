package com.mingrisoft.notes.view;


public class GetCutBitmapLocation {
	private float cutLeft = 0;
	private float cutTop = 0;
	private float cutRight = 0;
	private float cutBottom = 0;

	public void init(float x,float y){
		cutLeft = x;
		cutRight = x;
		cutTop = y;
		cutBottom = y;
	}

	//更新手写字的左右上下的位置
	public void setCutLeftAndRight(float x,float y){
		cutLeft = (x < cutLeft ? x : cutLeft);
		cutRight = (x > cutRight ? x : cutRight);
		cutTop = (y < cutTop ? y : cutTop);
		cutBottom = (y > cutBottom ? y : cutBottom);
	}


	//返回手写字的切割位置
	public float getCutLeft(){
		return cutLeft;
	}
	public float getCutTop(){
		return cutTop;
	}
	public float getCutRight(){
		return cutRight;
	}
	public float getCutBottom(){
		return cutBottom;
	}

}

