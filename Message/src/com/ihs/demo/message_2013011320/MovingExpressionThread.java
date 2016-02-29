package com.ihs.demo.message_2013011320;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ihs.message_2013011320.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.view.*;
import android.view.ViewGroup.LayoutParams;

@SuppressLint("NewApi")
class Coordinate{
	public float x,y;
	Coordinate(float x,float y){
		this.x = x;
		this.y = y;
	}
}
@SuppressLint("NewApi")
public class MovingExpressionThread extends Thread {
	static final float pi = 3.1415926535f / 2;
	static final int timegap = 15;
	FrameLayout layout;
	Context context;
	Handler handler;
	int phoneWidth;
	int phoneHeight;
	int options;
	/*ViewGroup.LayoutParams.WRAP_CONTENT*/
	LayoutParams standardLayoutParams;
	View head,message;
	int sr,expressionId;
	int expressionNum,expressionGap;
	MovingExpressionThread(FrameLayout layout,Context context,Handler handler,int expressionId,int expressionNum,int size){
		super();
		this.expressionId = expressionId;
		this.layout = layout;
		this.context = context;
		this.handler = handler;
		this.expressionNum = expressionNum;
		standardLayoutParams = new LayoutParams(size, size);
		WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
		phoneWidth = wm.getDefaultDisplay().getWidth();
		phoneHeight = wm.getDefaultDisplay().getHeight();
		options = 1;
	}
	MovingExpressionThread(FrameLayout layout,Context context,Handler handler,View head,View message,int sr,int expressionId,int expressionNum,int size){
		this(layout,context,handler,expressionId,expressionNum,size);
		this.head = head;
		this.message = message;
		this.sr = sr;
		options = 2;
	}
	List<Coordinate> trace;	
	MovingExpressionThread(FrameLayout layout,Context context,Handler handler,List<Coordinate> trace,int expressionNum,int expressionGap,int expressionId,int size){
		this(layout,context,handler,expressionId,expressionNum,size);
		this.trace = trace;		
		this.expressionGap = expressionGap;
		options = 3;
	}
	private int dip2px(float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    } 
	@SuppressLint("NewApi")
	void rainy(){ // 表情雨播放
		int imageNum = expressionNum;
		float range = 0.5f;
		Random random = new Random(System.currentTimeMillis());
		List<ImageView> expressions= new ArrayList<ImageView>(imageNum);
		List<Coordinate> coordinates = new ArrayList<Coordinate>(imageNum);
		List<Float> speed = new ArrayList<Float>(imageNum);
		ImageView tmp;
		Coordinate coordinate;
		for (int i = 0;i < imageNum;i++){
			tmp = this.createImageView(expressionId);
			coordinate = new Coordinate(random.nextFloat() * phoneWidth,-random.nextFloat() * phoneHeight*range);
			tmp.setX(coordinate.x);
			tmp.setY(coordinate.y);
			coordinates.add(coordinate);
			expressions.add(tmp);
			speed.add(new Float(random.nextFloat() * 2 * timegap / 5 + timegap * 4 / 5));
		}
		this.add(expressions);
		while (expressions.size() > 0){
			for (int i = 0;i < expressions.size();i++){
				coordinates.get(i).y += speed.get(i);
				if (coordinates.get(i).y > phoneHeight){
					this.remove(expressions.get(i));
					expressions.remove(i);
					coordinates.remove(i);
					speed.remove(i);
					i--;
				}
			}
			final List<ImageView> $expressions = expressions;
			final List<Coordinate> $coordinates = coordinates;
			handler.post(new Runnable(){
				public void run() {
					for (int i = 0;i < $expressions.size();i++)
						$expressions.get(i).setY($coordinates.get(i).y);
				}			
			});
			try {
				Thread.sleep(timegap);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	 private   Coordinate getDescendantCoordRelativeToSelf(View descendant,int x,int y) {
        float  scale = 1.0f;
        float [] pt = {x, y};
        descendant.getMatrix().mapPoints(pt);
        scale *= descendant.getScaleX();
        pt[0] += descendant.getLeft();
        pt[1] += descendant.getTop();
        return new Coordinate(pt[0],pt[1]);
	 }
	@SuppressLint("NewApi")
	void shootAround(){ // 扫射表情播放
		try {
			Thread.sleep(timegap);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		final float headX = head.getX() + dip2px(55) / 2;
		final float headY = head.getY() + dip2px(55);
		Log.d("luyu", "luyu:head center"+message.getX()+ " "+ message.getY()+" " + head.getX()+" "+head.getY() + " " + headX+" "+headY);
		int directionNum = 15;
		int imageNum = expressionNum;
		List<ImageView> expressions= new ArrayList<ImageView>(imageNum);
		List<Coordinate> coordinates = new ArrayList<Coordinate>(imageNum);
		List<Coordinate> speeds = new ArrayList<Coordinate>(imageNum);
		Coordinate standardSpeed[] = new Coordinate[directionNum];
		for (int i = 0;i < directionNum;i++){
			standardSpeed[i] = new Coordinate(sr * timegap * (float)Math.cos(pi * (2.f/(directionNum - 1)*i -1)),timegap * (float)Math.sin(pi * (2.f/(directionNum - 1)*i -1)));
		}
		for (int i = 0; i < imageNum;i++){
			expressions.add(this.createImageView(expressionId));
			coordinates.add(new Coordinate(0,0));
			speeds.add(standardSpeed[Math.abs((i) % (directionNum * 2 - 1) - directionNum + 1)]);
		}
		this.add(expressions);
		for (int timeslip = 0;true;timeslip++){	
			//Log.d("luyu","luyu:11");
			for (int i = 0;i < Math.min(expressions.size(), timeslip / 3 + 1);i++){
				coordinates.get(i).x += speeds.get(i).x;
				coordinates.get(i).y += speeds.get(i).y;
				if (Math.abs(coordinates.get(i).x + headX - phoneWidth/2) > phoneWidth/2 || Math.abs(coordinates.get(i).y) > phoneHeight * 0.8){
					this.remove(expressions.get(i));
					expressions.remove(i);
					coordinates.remove(i);
					speeds.remove(i);
					i--;
				}
			}
			/*if (timeslip < imageNum * 3 && timeslip % 3 == 0){	
				expressions.add(this.createImageView(expressionId));
				coordinates.add(new Coordinate(0,0));
				speeds.add(standardSpeed[Math.abs((timeslip / 3) % (directionNum * 2 - 1) - directionNum + 1)]);
				this.add(expressions.get(expressions.size() - 1));
			}	*/	
			if (expressions.size() <= 0)
				break;
			final List<ImageView> $expressions = expressions;
			final List<Coordinate> $coordinates = coordinates;
			handler.post(new Runnable(){
				public void run() {
					final float centerX = message.getX();
					final float centerY = message.getY();
					for (int i = 0;i < $expressions.size();i++){						
						$expressions.get(i).setX(headX + centerX + $coordinates.get(i).x);
						$expressions.get(i).setY(headY + centerY + $coordinates.get(i).y);
					}
				}			
			});
			//Log.d("luyu","luyu:22");
			try {
				Thread.sleep(timegap);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	void userDefinedTrace(){ // 自定义轨迹播放
		//Log.d("luyu","luyu:00");
		int imageNum = expressionNum;
		List<ImageView> expressions= new ArrayList<ImageView>(imageNum);
		List<Coordinate> coordinates = new ArrayList<Coordinate>(imageNum);
		for (int i = 0;i < imageNum;i++){
			expressions.add(this.createImageView(expressionId));
			coordinates.add(trace.get(0));			
		}
		//Log.d("luyu","luyu:11");
		this.add(expressions);
		//Log.d("luyu","luyu:22");
		for (int timeslip = 0;timeslip<trace.size();timeslip++){
			//Log.d("luyu","luyu:"+timeslip);
			for (int i = 0;i < imageNum;i++)
				coordinates.set(i, trace.get(Math.max(timeslip - expressionGap * i, 0)));
			//Log.d("luyu","luyu:01");
			//coordinates.set(imageNum - 1, trace.get(timeslip));
			final List<ImageView> $expressions = expressions;
			final List<Coordinate> $coordinates = coordinates;
			//Log.d("luyu","luyu:02");
			handler.post(new Runnable(){
				public void run() {
					for (int i = 0;i < $expressions.size();i++){						
						$expressions.get(i).setX($coordinates.get(i).x);
						$expressions.get(i).setY($coordinates.get(i).y);
					}
				}			
			});
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.remove(expressions);
	}
	@Override
	public void run(){
		switch (options){
		case 1:
			rainy();
			break;
		case 2:
			shootAround();
			break;
		case 3:
			 userDefinedTrace();
			 break;
		}
		
		/*Log.d("luyu","luyu:inMovingExpressionThread" + phoneWidth + " " + phoneHeight);		
		final ImageView expression = this.createImageView();		
		Log.d("luyu","luyu:1");		
		this.add(expression);
		float x = 0,y = 0;
		while (x < phoneWidth && y < phoneHeight){
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x += 5;
			y += 5;
			final ImageView $expression = expression;
			final float $x = x;
			final float $y = y;
			handler.post(new Runnable(){
				public void run() {
					$expression.setX($x);
					$expression.setY($y);
				}			
			});
		}	
		this.remove(expression);
		Log.d("luyu","luyu:2");*/
	}
	private void add(List<ImageView> expressions){
		final List<ImageView>  $expressions = expressions;
		final FrameLayout $layout = layout;
		handler.post(new Runnable(){		
			public void run() {
				for (int i = 0; i < $expressions.size();i++)
					$layout.addView($expressions.get(i));
			}			
		});
	}
	private void remove(List<ImageView> expressions){
		final List<ImageView>  $expressions = expressions;
		final FrameLayout $layout = layout;
		handler.post(new Runnable(){		
			public void run() {
				for (int i = 0; i < $expressions.size();i++)
					$layout.removeView($expressions.get(i));
			}			
		});
	}
	private void add(ImageView expression){
		final ImageView  $expression = expression;
		final FrameLayout $layout = layout;
		handler.post(new Runnable(){		
			public void run() {
				$layout.addView($expression);
			}			
		});
	}
	private void remove(ImageView expression){
		final ImageView  $expression = expression;
		final FrameLayout $layout = layout;
		handler.post(new Runnable(){		
			public void run() {
				$layout.removeViewInLayout($expression);
			}			
		});
	}
	private ImageView createImageView(int i){
		final ImageView expression = new ImageView(context);		
		expression.setLayoutParams(standardLayoutParams);
		expression.setScaleType(ScaleType.FIT_XY);
		switch (i){
		case -1:
			expression.setImageResource(R.drawable.e_pch);
			break;
		default:
			expression.setImageResource(R.drawable.e_00+i);
		}		
		return expression;
	}
}
/*handler.post(new Runnable(){			
ImageView $expression = expression;
FrameLayout $layout = layout;
public void run() {
	$layout.addView($expression);
}			
});*/
/*LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
final ImageView expression = new ImageView(context);		
expression.setLayoutParams(lp);
expression.setScaleType(ScaleType.CENTER);
expression.setImageResource(R.drawable.bishi);*/
