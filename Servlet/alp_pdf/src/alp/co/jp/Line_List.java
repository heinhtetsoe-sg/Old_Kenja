package alp.co.jp;

import java.io.Serializable;
import java.util.ArrayList;

import alp.co.jp.line.Line;
import alp.co.jp.line.Net;
import alp.co.jp.line.Rect;
import alp.co.jp.line.RundRect;

public class Line_List implements Serializable {

    /**
     * シリアルバージョン
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<Line> line_ = null;
    private ArrayList<Rect> rect_ = null;
    private ArrayList<RundRect> rundrect_ = null;
    private ArrayList<Net> net_ = null;

    public Line_List() {
	super();
	// TODO 自動生成されたコンストラクター・スタブ
	line_ = new ArrayList<Line>();
	rect_ = new ArrayList<Rect>();
	rundrect_ = new ArrayList<RundRect>();
	net_ = new ArrayList<Net>();
    }

    public ArrayList<Line> getLine_() {
	return line_;
    }

    public ArrayList<Rect> getRect_() {
	return rect_;
    }

    public ArrayList<RundRect> getRundrect_() {
	return rundrect_;
    }

    public ArrayList<Net> getNet_() {
	return net_;
    }

    public int addLine(final Line line) {
	line_.add(line);
	return line_.size();
    }

    public int addRect(final Rect rect) {
	rect_.add(rect);
	return rect_.size();
    }

    public int addRundRect(final RundRect rundrect) {
	rundrect_.add(rundrect);
	return rundrect_.size();
    }

    public int addNet(final Net net) {
	net_.add(net);
	return net_.size();
    }

    public void AddAll(final Line_List llist) {
	ArrayList<Line> lline = llist.getLine_();
	ArrayList<Rect> lrect = llist.getRect_();
	ArrayList<RundRect> lrundrect = llist.getRundrect_();
	ArrayList<Net> lnet = llist.getNet_();

	if (lline.size() > 0) {
	    line_.addAll(lline);
	}
	if (lrect.size() > 0) {
	    rect_.addAll(lrect);
	}
	if (lrundrect.size() > 0) {
	    rundrect_.addAll(lrundrect);
	}
	if (lnet.size() > 0) {
	    net_.addAll(lnet);
	}
    }
}
