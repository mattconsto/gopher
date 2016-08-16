package gopher;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class WrappedImageView extends ImageView {
	WrappedImageView() {
		setPreserveRatio(false);
	}
	WrappedImageView(Image image) {
		super(image);
	}
	WrappedImageView(String string) {
		super(string);
	}
	
	@Override
	public double minWidth(double height) {
		return 0;
	}
	
	@Override
	public double prefWidth(double height) {
		Image I = getImage();
		if (I == null) return minWidth(height);
		return I.getWidth();
	}
	
	@Override
	public double maxWidth(double height) {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public double minHeight(double width) {
		return 0;
	}
	
	@Override
	public double prefHeight(double width) {
		Image I = getImage();
		if (I == null) return minHeight(width);
		return I.getHeight();
	}
	
	@Override
	public double maxHeight(double width) {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean isResizable() {
		return true;
	}
	
	@Override
	public void resize(double width, double height) {
		setFitWidth(prefWidth(height));
		setFitHeight(prefHeight(width));
	}
}