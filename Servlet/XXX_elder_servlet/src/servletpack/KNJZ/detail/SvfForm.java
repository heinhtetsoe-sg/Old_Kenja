/*
 * $Id: 85182a0a5944b935d50c853d58eb3102b6d19409 $
 *
 * 作成日: 2018/09/10
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SvfForm {

    private static final Log log = LogFactory.getLog(SvfForm.class);

    private String revision = "$Revision: 77305 $";

    public final File _formFile;

    public boolean _debug;

    private static final double dpi = 400.0;
    private static final double pointPerInch = 72;

    private static final String encoding = "MS932";

    private static enum ContentDiv {
        NOTHING(new String[] {}),
        PAGE(
                ";********** ページ基本定義 **********",
                ";Page Size  Dir  Mini  Hopper   AdjtX   AdjtY  CopyCnt  PrintCnt        FormTitle  Stack ZenPitch PageLength LinkForm  LinkOnOff"),
        MARGIN_DEFINIION(
                ";********** フィールド位置マージン定義 **********",
                ";AdjtF   X     Y"),
        SUBFORM_RECORD(
                ";****************************** ＳｕｂＦｏｒｍの定義 ******************************",
                ";       Name    X1    Y1    X2    Y2 Dir Offset Flag1 LinkSubForm 区分 角 半径 角区分 ﾌﾗｸﾞ 線 幅 区分 "),
        KOTEIMOJI(
                ";****************************** 固定文字の定義 ******************************",
                ";        K S S S S EndX D    X    Y  Pot L Ac Br Bl It Ol Sd 3D Lt Sm Sty                                     Cpy Kaiten RepeatID F"),
        FIELD(
                ";****************************** フィールドの定義 ******************************",
                ";Field  No Name                   K Ket S S S S EndX D    X    Y  Pot L Ac Br Bl It Ol Sd 3D Lt Sm Sty Ef Cf Str Cpy ZP KAKUDO Type RID RC RHV RPitch LinkName Kaiten MaskCnt MaskNo .(20). -1 -1 RepeatMod(*100)  Lock F"),
        LINE(
                ";****************************** 線の定義 ******************************",
                ";    Syu Haba Hasi  X1 Y1 X2 Y2 Log Copy RepeatID WidthKind Color"),
        IMAGE(
                ";****************************** ビットマップフィールドの定義 ******************************",
                ";Field  No Name                   K Ket S S S S EndX D    X    Y  Pot L Ac Br Bl It Ol Sd 3D Lt Sm Sty Ef Cf Str Cpy ZP KAKUDO Type RID RC RHV RPitch LinkName Kaiten MaskCnt MaskNo .(20). -1 -1 RepeatMod(*100)  Lock F"),
        BOX(
                ";****************************** ＢＯＸの定義 ******************************",
                ";       Syu  Haba  Hasi  Paint  Soto    X1    Y1    X2    Y2   Kado    Hankei   Log   PLgt Copy   RepeatID Round_Cut WidthKind"),
        REPEAT(
                ";****************************** リピートの定義 ******************************",
                ";Repeat  No left top right bottom Dir count1 pitch1 mod1 page count2 pitch2 mod2"),
        COLOR(
                ";***** 色情報の定義 *****");

        final String[] _teigiComment;
        ContentDiv(final String ... teigiComment) {
            _teigiComment = teigiComment;
        }

        public boolean containsComment(final String text) {
            if (null == text) {
                return false;
            }
            return text.startsWith(_teigiComment[0]);
        }

        public boolean containsAny(final String text) {
            if (null == text) {
                return false;
            }
            for (final String c : _teigiComment) {
                if (text.startsWith(c)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static enum ReadType {
        UNKNOWN,
        HEADER,
        MARGIN_DEFINITION,
        LINE,
        BOX,
        TEXT,
        IMAGE,
        FIELD,
        SUBFORM,
        REPEAT,
        COLOR,
        FOOTER
    }

    public static enum PageSize {
        HAGAKI("0", 105, 148), A5("1", 148, 210), A4("2", 210, 297), A3("3", 297, 420), B5("4", 176, 250), B4("5", 250, 353), B3("6", 353, 500);

        final int _width;
        final int _height;
        final String _svfVal;
        PageSize(final String svfVal, final int mmWidth, final int mmHeight) {
            _svfVal = svfVal;
            final int dpi = 400;
            final int _10InchPerMm = 254;
            _width = mmWidth * dpi * 10 / _10InchPerMm;
            _height = mmHeight * dpi * 10 / _10InchPerMm;
        }

        public String svfValue() {
            return String.valueOf(_svfVal);
        }

        public static PageSize get(final String svfVal) {
            for (PageSize ps : values()) {
                if (ps._svfVal.equals(svfVal)) {
                    return ps;
                }
            }
            return null;
        }
    }

    public static enum PageDirection {
        TATE("0"), YOKO("1");

        final String _svfVal;
        PageDirection(final String svfVal) {
            _svfVal = svfVal;
        }

        public String svfValue() {
            return _svfVal;
        }

        public static PageDirection get(final String svfVal) {
            for (PageDirection dp : values()) {
                if (dp._svfVal.equals(svfVal)) {
                    return dp;
                }
            }
            return null;
        }
    }

    public static class Bunsu {
        public static Bunsu _default = new Bunsu(1, 1);
        public final int _bunshi;
        public final int _bunbo;
        Bunsu(final int bunshi, final int bunbo) {
            if (bunbo == 2) {
                if (bunshi == 1) {
                } else {
                    throw new IllegalArgumentException("SvfForm.Bunsu : bunshi = " + bunshi + ", bunbo = " + bunbo);
                }
            } else if (bunbo == 1) {
                if (ArrayUtils.contains(new int[] {1, 2, 3, 4, 5, 6, 7, 8}, bunshi)) {
                } else {
                    throw new IllegalArgumentException("SvfForm.Bunsu : bunshi = " + bunshi + ", bunbo = " + bunbo);
                }
            } else {
                throw new IllegalArgumentException("SvfForm.Bunsu : bunshi = " + bunshi + ", bunbo = " + bunbo);
            }
            _bunshi = bunshi;
            _bunbo = bunbo;
        }
        public boolean equals(final Object o) {
            if (!(o instanceof Bunsu)) {
                return false;
            }
            final Bunsu b = (Bunsu) o;
            return _bunshi == b._bunshi && _bunbo == b._bunbo;
        }
    }

    /**
     * SVF フォーム 文字修飾
     */
    public static class MojiShushoku {
        private static MojiShushoku _default = new MojiShushoku();
        private boolean _shiromoji;
        private boolean _outline;
        private boolean _bold;
        private boolean _italic;
        private boolean _shadow;
        private MojiShushoku(final String shiromoji, final String outline, final String bold, final String italic, final String shadow) {
            _shiromoji = "100".equals(shiromoji);
            _bold = "1".equals(bold);
            _italic = "1".equals(italic);
            _outline = "1".equals(outline);
            _shadow = "1".equals(shadow);
        }
        private MojiShushoku() {
        }
        private MojiShushoku copy() {
            final MojiShushoku rtn = new MojiShushoku();
            rtn._shiromoji = _shiromoji;
            rtn._outline = _outline;
            rtn._bold = _bold;
            rtn._italic = _italic;
            rtn._shadow = _shadow;
            return rtn;
        }
        public boolean isShiromoji() {
            return _shiromoji;
        }
        public MojiShushoku setShiromoji(final boolean shiromoji) {
            final MojiShushoku rtn = copy();
            rtn._shiromoji = shiromoji;
            return rtn;
        }
        public boolean isOutline() {
            return _outline;
        }
        public MojiShushoku setOutline(final boolean outline) {
            final MojiShushoku rtn = copy();
            rtn._outline = outline;
            return rtn;
        }
        public boolean isBold() {
            return _bold;
        }
        public MojiShushoku setBold(final boolean bold) {
            final MojiShushoku rtn = copy();
            rtn._bold = bold;
            return rtn;
        }
        public boolean isItalic() {
            return _italic;
        }
        public MojiShushoku setItalic(final boolean italic) {
            final MojiShushoku rtn = copy();
            rtn._italic = italic;
            return rtn;
        }
        public boolean isShadow() {
            return _shadow;
        }
        public MojiShushoku setShadow(final boolean shadow) {
            final MojiShushoku rtn = copy();
            rtn._shadow = shadow;
            return rtn;
        }
    }

    private static enum AddRemove {
        ADD,
        REMOVE
    }

    public static enum Font {
        Mincho(0),
        Gothic(1);

        private final int _val;
        Font(final int val) {
            _val = val;
        }
        private static Font valueOf(final int val) {
            for (final Font font : Font.values()) {
                if (font._val == val) {
                    return font;
                }
            }
            return null;
        }
    }

    public static enum LinePosition {
        UPPER,
        LOWER,
        LEFT,
        RIGHT;
    }

    public static enum FrameLine {
        NONE(0),
        SQUARE(1),
        EACH(2);

        final int _val;
        FrameLine(final int val) {
            _val = val;
        }
        private static FrameLine valueOf(final int val) {
            for (final FrameLine frameLine : FrameLine.values()) {
                if (frameLine._val == val) {
                    return frameLine;
                }
            }
            return null;
        }
    }

    public static enum LineOptionIndex {
        TOP(0),
        BOTTOM(1),
        LEFT(2),
        RIGHT(3);

        final int _idx;
        LineOptionIndex(final int idx) {
            _idx = idx;
        }
    }

    public static class LineOption {
        public static LineOption _default = LineOption.of(LineKind.SOLID, LineWidth.width(1));
        final LineKind _lineKind;
        final LineWidth _lineWidth;
        private LineOption(final LineKind lineKind, final LineWidth lineWidth) {
            _lineKind = lineKind;
            _lineWidth = lineWidth;
        }
        public static LineOption of(LineKind lineKind, LineWidth lineWidth) {
            return new LineOption(lineKind, lineWidth);
        }
        public String toSvfString() {
            return _lineKind._idx + " " + _lineWidth._idx + " " + (_lineWidth._inputFlag ? "1" : "0");
        }
        public String toString() {
            return "LineOption(" + _lineKind + ", " + _lineWidth + ")";
        }
    }

    /**
     * 線種
     */
    public static enum LineKind {
        /** なし(LineKind.EACH のみ有効) */
        NONE(-1),
        /** 実戦 */
        SOLID(0),
        /** 破線1 */
        DOTTED1(1),
        /** 破線2 */
        DOTTED2(2),
        /** 破線3 */
        DOTTED3(3),
        /** 1点鎖線 */
        DOT1_CHAINED(4),
        /** 2点鎖線 */
        DOT2_CHAINED(5)
        ;

        final int _idx;
        LineKind(final int idx) {
            _idx = idx;
        }

        private static LineKind valueOf(final int idx) {
            for (final LineKind v : LineKind.values()) {
                if (v._idx == idx) {
                    return v;
                }
            }
            try {
                throw new IllegalArgumentException("LineKind : " + idx);
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }
    }

    /**
     * 線幅
     */
    public static class LineWidth {
        /** なし */
        public static LineWidth NONE = new LineWidth(-1, false);
        /** 極細 */
        public static LineWidth THINEST = new LineWidth(1, false);
        /** 細 */
        public static LineWidth THIN = new LineWidth(2, false);
        /** 中細 */
        public static LineWidth MEDIUM_THIN = new LineWidth(3, false);
        /** 中 */
        public static LineWidth MEDIUM = new LineWidth(4, false);
        /** 中太 */
        public static LineWidth MEDIUM_THICK = new LineWidth(5, false);
        /** 太 */
        public static LineWidth THICK = new LineWidth(6, false);

        final int _idx; // 列挙。ただし_inputFlagが1の場合、線幅
        final boolean _inputFlag;
        private LineWidth(final int idx, final boolean inputFlag) {
            _idx = idx;
            _inputFlag = inputFlag;
        }
        public static LineWidth width(final int width) {
            return new LineWidth(width, true);
        }
        private static LineWidth valueOf(final int idx) {
            for (final LineWidth v : Arrays.asList(NONE, THINEST, THIN, MEDIUM_THIN, MEDIUM, MEDIUM_THICK, THICK)) {
                if (v._idx == idx) {
                    return v;
                }
            }
            try {
                if (idx <= 0) {
                    throw new IllegalArgumentException("LineWidth : " + idx);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return width(idx);
        }

        public String toString() {
            if (this == NONE) {
                return "LineWidth.NONE";
            } else if (this == THINEST) {
                return "LineWidth.THINEST";
            } else if (this == THIN) {
                return "LineWidth.THIN";
            } else if (this == MEDIUM_THIN) {
                return "LineWidth.MEDIUM_THIN";
            } else if (this == MEDIUM) {
                return "LineWidth.MEDIUM";
            } else if (this == MEDIUM_THICK) {
                return "LineWidth.MEDIUM_THICK";
            } else if (this == THICK) {
                return "LineWidth.THICK";
            }
            return "LineWidth.width(" + _idx + ")";
        }
    }

    private PageSize _pageSize;
    private PageDirection _pageDirection;
    private BigDecimal _adjtX;
    private BigDecimal _adjtY;
    private String _pageColor;
    private List<ContentsLine> _contents;

    private Map _contentDivAddRemoveMap = new HashMap();

    private <T extends Element> List<T> getContentAddRemoveList(final Class<T> c, final AddRemove addRemove) {
        if (!_contentDivAddRemoveMap.containsKey(addRemove)) {
            _contentDivAddRemoveMap.put(addRemove, new HashMap());
        }
        final Map<Class<T>, List> mappedMap = (Map) _contentDivAddRemoveMap.get(addRemove);
        if (!mappedMap.containsKey(c)) {
            mappedMap.put(c, new ArrayList<T>());
        }
        final List<T> mappedList = mappedMap.get(c);
        return mappedList;
    }

    public SvfForm(final File formFile) {
        _formFile = formFile;
    }

    public boolean readFile() {
        if (_debug) {
            log.info(revision);
            log.info(" file = " + _formFile);
        }

        final String path = _formFile.getAbsolutePath();

        boolean successFlg = true;
        BufferedReader r = null;
        final List<String> readLines = new ArrayList<String>();
        try {
             r = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
             String line;
             while (null != (line = r.readLine())) {
                 readLines.add(line);
             }
        } catch (Throwable t) {
            log.error("exception!", t);
            successFlg = false;
        } finally {
            try {
                if (null != r) {
                    r.close();
                }
            } catch (Exception e) {
            }
        }
        try {
            _contents = new ArrayList<ContentsLine>();
            for (final String readLine : readLines) {
                ContentsLine cl = new ContentsLine();
                cl._readLine = readLine;
                _contents.add(cl);
            }
             setContentLines(_contents);
          } catch (Throwable t) {
              log.error("exception!", t);
              successFlg = false;
          }
        return successFlg;
    }

    public static class ContentsLine {
        ReadType _type;
        String _readLine;
        Line _line;
        Box _box;
        KoteiMoji _koteiMoji;
        ImageField _image;
        Field _field;
        SubForm _subForm;
        Record _record;
        Repeat _repeat;
        String _cLine;
        String _cText;
        String _cField;
        String[] _cFieldArray;
        String _cImage;
        String[] _cImageArray;
        String _cBox;
        String _cSubForm;
        String _cRecord;
        boolean _isLastField;
        boolean _isPageHeader;
        boolean _isMarginDefinition;
        boolean _isSubformHeader;
        boolean _isLastSubformHeader;
        public <T extends Element> T get(final Class<T> clazz) {
            if (clazz == Line.class) {
                return clazz.cast(_line);
            } else if (clazz == Box.class) {
                return clazz.cast(_box);
            } else if (clazz == KoteiMoji.class) {
                return clazz.cast(_koteiMoji);
            } else if (clazz == ImageField.class) {
                return clazz.cast(_image);
            } else if (clazz == Field.class) {
                return clazz.cast(_field);
            } else if (clazz == SubForm.class) {
                return clazz.cast(_subForm);
            } else if (clazz == Record.class) {
                return clazz.cast(_record);
            } else if (clazz == Repeat.class) {
                return clazz.cast(_repeat);
            }
            return null;
        }

        public Element get() {
            if (null != _line) {
                return _line;
            } else if (null != _box) {
                return _box;
            } else if (null != _koteiMoji) {
                return _koteiMoji;
            } else if (null != _image) {
                return _image;
            } else if (null != _field) {
                return _field;
            } else if (null != _subForm) {
                return _subForm;
            } else if (null != _record) {
                return _record;
            } else if (null != _repeat) {
                return _repeat;
            }
            return null;
        }
    }

    public PageSize getPageSize() {
        return _pageSize;
    }

    public PageDirection getPageDirection() {
        return _pageDirection;
    }

    public int getFormWidth() {
        if (_pageDirection == PageDirection.YOKO) {
            return _pageSize._height;
        }
        return _pageSize._width;
    }

    public int getFormHeight() {
        if (_pageDirection == PageDirection.YOKO) {
            return _pageSize._width;
        }
        return _pageSize._height;
    }

    private static String unquote(final String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        log.warn(" not string : [" + s + "]");
        return s;
    }

    private static String quote(final String s) {
        return "\"" + StringUtils.replace(StringUtils.defaultString(s), "\\\\\"", "\"\"") + "\""; // エスケープシーケンス対応 (splitBySpace参照)
    }

    private static String keta(final int num, final int keta) {
        return StringUtils.leftPad(String.valueOf(num), keta);
    }

    private void setContentLines(final List<ContentsLine> contentLines) {
        ContentDiv contentDiv = ContentDiv.NOTHING;
        ContentDiv contentDivBefore = null;
        ContentsLine contentsLineBefore = null;

        ReadType currentType = ReadType.HEADER;
        for (final ContentsLine cl : contentLines) {
            final String readLine = cl._readLine;

//			if (_debug) {
//				log.info(contentDiv + " " + readLine);
//			}
            switch (contentDiv) {
            case PAGE: {
                if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                    //log.info(" comment : " + contents);
                    cl._type = currentType;
                    continue;
                } else if (readLine.startsWith("Page")) {
                    currentType = ReadType.HEADER;
                    cl._type = currentType;
                    cl._isPageHeader = true;
                    final String[] arr = splitBySpace(readLine);
                    _pageSize = PageSize.get(arr[1]);
                    _pageDirection = PageDirection.get(arr[2]);
                    _adjtX = new BigDecimal(arr[5]);
                    _adjtY = new BigDecimal(arr[6]);
                    _pageColor = arr[25];
                    contentDiv = ContentDiv.NOTHING;
                } else {
                    if (_debug) {
                        log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                    }
                    contentDiv = ContentDiv.NOTHING;
                }
            }
            break;
            case MARGIN_DEFINIION: {
                if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                    //log.info(" comment : " + contents);
                    cl._type = currentType;
                    continue;
                } else if (readLine.startsWith(" AdjtF")) {
                    currentType = ReadType.MARGIN_DEFINITION;
                    cl._type = currentType;
                    cl._isMarginDefinition = true;
                    contentDiv = ContentDiv.NOTHING;
                } else {
                    if (_debug) {
                        log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                    }
                    contentDiv = ContentDiv.NOTHING;
                }
            }
            break;
            case LINE: {
                    if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                        //log.info(" comment : " + contents);
                        continue;
                    } else if (readLine.startsWith(" Line") || readLine.startsWith("fLine")) {
                        currentType = ReadType.LINE;
                        cl._type = currentType;
                        final String[] arr = splitBySpace(readLine);
                        //log.info(ArrayUtils.toString(arr));
                        if (readLine.startsWith(" Line")) {
                            final int lineKind = Integer.parseInt(arr[1]);
                            final int lineWidth = Integer.parseInt(arr[2]);
                            final Point upperLeft = new Point(Integer.parseInt(arr[4]), Integer.parseInt(arr[5]));
                            final Point lowerRight = new Point(Integer.parseInt(arr[6]), Integer.parseInt(arr[7]));
                            cl._line = new Line(false, LineKind.valueOf(lineKind), LineWidth.valueOf(lineWidth), upperLeft, lowerRight, readLine);
                        } else { // contents.startsWith("fLine")
                            final int lineKind = Integer.parseInt(arr[1]);
                            final int lineWidth = Integer.parseInt(arr[2]);
                            final Point upperLeft = new Point(Integer.parseInt(arr[3]), Integer.parseInt(arr[4]));
                            final Point lowerRight = new Point(Integer.parseInt(arr[5]), Integer.parseInt(arr[6]));
                            cl._line = new Line(true, LineKind.valueOf(lineKind), LineWidth.valueOf(lineWidth), upperLeft, lowerRight, readLine);
                        }
                    } else {
                        if (_debug) {
                            log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                        }
                        contentDiv = ContentDiv.NOTHING;
                    }
                }
                break;
            case BOX: {
                if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                    //log.info(" comment : " + contents);
                    continue;
                } else if (readLine.startsWith(" Box")) {
                    currentType = ReadType.BOX;
                    cl._type = currentType;
                    final String[] arr = splitBySpace(readLine);
                    final int lineKind = Integer.parseInt(arr[1]);
                    final int lineWidth = Integer.parseInt(arr[2]);
                    final int w3 = Integer.parseInt(arr[3]);
                    final int paint = Integer.parseInt(arr[4]);
                    final int w5 = Integer.parseInt(arr[5]);
                    final Point upperLeft = new Point(Integer.parseInt(arr[6]), Integer.parseInt(arr[7]));
                    final Point lowerRight = new Point(Integer.parseInt(arr[8]), Integer.parseInt(arr[9]));
                    final int cornerBits = Integer.parseInt(arr[10]);
                    final int cornerRadius = Integer.parseInt(arr[11]);
                    final int w12 = Integer.parseInt(arr[12]);
                    final int amikakeMeido = Integer.parseInt(arr[13]);
                    final String copy = unquote(arr[14]);
                    final int repeatId = Integer.parseInt(arr[15]);
                    final int cornerCutFlg = Integer.parseInt(arr[16]);
                    final int w17 = Integer.parseInt(arr[17]);
                    cl._box = new Box(LineKind.valueOf(lineKind), LineWidth.valueOf(lineWidth), w3, paint, w5, upperLeft, lowerRight, cornerBits, cornerRadius, w12, amikakeMeido, copy, repeatId, cornerCutFlg, w17);
                } else {
                    if (_debug) {
                        log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                    }
                    contentDiv = ContentDiv.NOTHING;
                }
            }
            break;
            case KOTEIMOJI: {
                    if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                        //log.info(" comment : " + contents);
                        continue;
                    } else if (readLine.startsWith(" Text") || readLine.startsWith("fText")) {
                        boolean isFText = readLine.startsWith("fText");
                        currentType = ReadType.TEXT;
                        cl._type = currentType;
                        final String[] arr = splitBySpace(readLine);
                        final String moji = arr[21];
                        final Font font = Font.valueOf(Integer.parseInt(arr[1]));
                        final int tatebaiBunshi = Integer.parseInt(arr[2]);
                        final int tatebaiBunbo = Integer.parseInt(arr[3]);
                        final int yokobaiBunshi = Integer.parseInt(arr[4]);
                        final int yokobaiBunbo = Integer.parseInt(arr[5]);
                        final int endX = Integer.parseInt(arr[6]);
                        final int x = Integer.parseInt(arr[8]);
                        final int y = Integer.parseInt(arr[9]);
                        final int mojiPoint = Integer.parseInt(arr[10]);
                        final boolean isVertical = "1".equals(arr[7]);
                        final String shiromoji = arr[13];
                        final String bold = arr[14];
                        final String italic = arr[15];
                        final String outline = arr[16];
                        final String shadow = arr[17];
                        final int degree;
                        if (!isFText) {
                            degree = Integer.parseInt(arr[23]);
                        } else {
                            degree = Integer.parseInt(arr[22]);
                        }
                        final MojiShushoku mojiShushoku = new MojiShushoku(shiromoji, outline, bold, italic, shadow);
                        cl._koteiMoji = new KoteiMoji(isFText, moji, font, endX, new Point(x, y), mojiPoint, isVertical, mojiShushoku, new Bunsu(tatebaiBunshi, tatebaiBunbo), new Bunsu(yokobaiBunshi, yokobaiBunbo), degree);
                        if (_debug) {
                            log.info(" koteiMoji = " + cl._koteiMoji);
                        }
                    } else {
                        if (_debug) {
                            log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                        }
                        contentDiv = ContentDiv.NOTHING;
                    }
                }
                break;
            case COLOR: {
                    if (readLine.startsWith("CLine")) {
                        currentType = ReadType.COLOR;
                        cl._type = currentType;
                        cl._cLine = readLine;
                    } else if (readLine.startsWith("CText")) {
                        currentType = ReadType.COLOR;
                        cl._type = currentType;
                        cl._cText = readLine;
                    } else if (readLine.startsWith("CField")) {
                        currentType = ReadType.COLOR;
                        cl._type = currentType;
                        cl._cField = readLine;
                        cl._cFieldArray = splitBySpace(readLine);
                    } else if (readLine.startsWith("CBox")) {
                        currentType = ReadType.COLOR;
                        cl._type = currentType;
                        cl._cBox = readLine;
                    } else if (readLine.startsWith("CSubForm")) {
                        currentType = ReadType.COLOR;
                        cl._type = currentType;
                        cl._cSubForm = readLine;
                    } else if (readLine.startsWith("CRecord")) {
                        currentType = ReadType.COLOR;
                        cl._type = currentType;
                        cl._cRecord = readLine;
                    } else if (!readLine.startsWith("C")) {
                        contentDiv = ContentDiv.NOTHING;
                    }
                }
                break;
            case IMAGE: {
                    if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                        //log.info(" comment : " + contents);
                        continue;
                    } else if (readLine.startsWith(" Field")) {
                        currentType = ReadType.IMAGE;
                        cl._type = currentType;
                        final String[] arr = splitBySpace(readLine);
                        final String no = arr[1];
                        final String fieldname = arr[2];
                        final String variableOrFixed = arr[3];
                        final int x = Integer.parseInt(arr[11]);
                        final int y = Integer.parseInt(arr[12]);
                        final int endX = Integer.parseInt(arr[9]);
                        final int height = Integer.parseInt(arr[13]);
                        final String repeatNo = arr[32];
                        final String color = "17";
                        final String rc = arr[33];
                        final String rhv = arr[34];
                        final String rpitch = arr[35];
                        final String repeatMod = arr[61];
                        cl._image = new ImageField(no, unquote(fieldname), new Point(x, y), endX, height, variableOrFixed, repeatNo, color, rc, rhv, rpitch, repeatMod);
                        if (_debug) {
                            log.info(" image = " + cl._image);
                        }
                    } else {
                        if (_debug) {
                            log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                        }
                        contentDiv = ContentDiv.NOTHING;
                    }
                }
                break;
            case FIELD: {
                    if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                        //log.info(" comment : " + contents);
                        continue;
                    } else if (readLine.startsWith(" Field")) {
                        currentType = ReadType.FIELD;
                        cl._type = currentType;
                        final String[] arr = splitBySpace(readLine);
                        final String no = arr[1];
                        final String fieldname = arr[2];
                        final Font font = Font.valueOf(Integer.parseInt(arr[3]));
                        final int fieldLength = Integer.parseInt(arr[4]);
                        final int tatebaiBunshi = Integer.parseInt(arr[5]);
                        final int tatebaiBunbo = Integer.parseInt(arr[6]);
                        final int yokobaiBunshi = Integer.parseInt(arr[7]);
                        final int yokobaiBunbo = Integer.parseInt(arr[8]);
                        final int endX = Integer.parseInt(arr[9]);
                        final boolean directionIsVertical = 1 == Integer.parseInt(arr[10]);
                        final int x = Integer.parseInt(arr[11]);
                        final int y = Integer.parseInt(arr[12]);
                        final int charPoint = Integer.parseInt(arr[13]);
                        final String shiromoji = arr[16];
                        final String bold = arr[17];
                        final String italic = arr[18];
                        final String outline = arr[19];
                        final String shadow = arr[20];
                        final String printMethod = arr[24];
                        final String editEqn = arr[25];
                        final String calcFormula = arr[26];
                        final String strStr = arr[27];
                        final String strCpy = arr[28];
                        final int degree = Integer.parseInt(arr[30]);
                        final String dataType = arr[31];

                        final String repeatNo = arr[32];
                        final int repeatCount = Integer.parseInt(arr[33]);
                        final int repeatDirection = Integer.parseInt(arr[34]);
                        final int repeatPitch = Integer.parseInt(arr[35]);
                        final String linkFieldname = arr[36];
                        final int hanzen = Integer.parseInt(arr[37]);
                        final int repeatPitchAmari = Integer.parseInt(arr[61]);
                        final int mask = Integer.parseInt(arr[66]);
                        final String flg2 = arr[67];
                        final String flg3 = arr[69];
                        String comment = null;
                        try {
                            comment = arr[75];
                        } catch (Exception e) {
                            if (_debug) {
                                log.info(" exception " + e.toString() + ", field array length = " + arr.length + ", comment = " + arr[arr.length - 1]);
                                comment = arr[arr.length - 1];
                            }
                        }
                        final MojiShushoku mojiShushoku = new MojiShushoku(shiromoji, outline, bold, italic, shadow);
                        cl._field = new Field(no, unquote(fieldname), font, fieldLength, new Bunsu(tatebaiBunshi, tatebaiBunbo), new Bunsu(yokobaiBunshi, yokobaiBunbo), endX, directionIsVertical, new Point(x, y), charPoint, mojiShushoku, Field.PrintMethod.getPrintMethod(printMethod), unquote(comment), unquote(editEqn), hanzen, mask
                                , new Field.RepeatConfig(repeatNo, repeatCount, repeatDirection, repeatPitch, repeatPitchAmari)
                                , unquote(linkFieldname)
                                , unquote(calcFormula), unquote(strStr), unquote(strCpy), degree, dataType
                                , flg2, flg3);
                        if (_debug) {
                            log.info(" field = " + cl._field);
                        }
                    } else {
                        if (_debug) {
                            log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                        }
                        contentDiv = ContentDiv.NOTHING;
                    }
                }
                break;
            case SUBFORM_RECORD: {
                if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                    //log.info(" comment : " + contents);
                    cl._isSubformHeader = true;
                    continue;
                } else if (readLine.startsWith("Record")) {
                    currentType = ReadType.SUBFORM;
                    cl._type = currentType;
                    final String[] arr = splitBySpace(readLine);
                    final String fieldname = arr[1];
                    final int x = Integer.parseInt(arr[2]);
                    final int y = Integer.parseInt(arr[3]);
                    final int x2 = Integer.parseInt(arr[4]);
                    final int y2 = Integer.parseInt(arr[5]);
                    final int recordFlag = Integer.parseInt(arr[6]);
                    final int amikakePatternFlg = Integer.parseInt(arr[7]);
                    final int amikakePattern = Integer.parseInt(arr[8]);
                    final int amikakeMeido = Integer.parseInt(arr[9]);
                    final String amikakeGyouPattern = arr[10];
                    final FrameLine waku = FrameLine.valueOf(Integer.parseInt(arr[11]));
                    final int cornerFlag = Integer.parseInt(arr[12]);
                    final int cornderRadius = Integer.parseInt(arr[13]);
                    final int cornerCutFlg = Integer.parseInt(arr[14]);
                    final int w15 = Integer.parseInt(arr[15]);
                    final LineOption[] lineFlags = new LineOption[4];
                    for (int i = 0; i < 4; i++) {
                        final int v1 = Integer.parseInt(arr[16 + i * 3]);
                        final int v2 = Integer.parseInt(arr[16 + i * 3 + 1]);
                        final int v3 = Integer.parseInt(arr[16 + i * 3 + 2]);
                        lineFlags[i] = LineOption.of(LineKind.valueOf(v1), 1 == v3 ? LineWidth.width(v2) : LineWidth.valueOf(v2));// 16 ~ 27
                    }
                    final int autoLinkFieldPitch = Integer.parseInt(arr[28]);
                    final int w29 = Integer.parseInt(arr[29]);
                    final int w30 = Integer.parseInt(arr[30]);
                    cl._record = new Record(unquote(fieldname), new Point(x, y), new Point(x2, y2), recordFlag, amikakePatternFlg, amikakePattern, amikakeMeido, amikakeGyouPattern, waku, cornerFlag, cornderRadius, cornerCutFlg, w15, lineFlags, autoLinkFieldPitch, w29, w30);
                    if (_debug) {
                        log.info(" record = " + cl._record);
                    }
                } else if (readLine.startsWith("SubForm")) {
                    currentType = ReadType.SUBFORM;
                    cl._type = currentType;
                    final String[] arr = splitBySpace(readLine);
                    final String fieldname = arr[1];
                    final int x = Integer.parseInt(arr[2]);
                    final int y = Integer.parseInt(arr[3]);
                    final int x2 = Integer.parseInt(arr[4]);
                    final int y2 = Integer.parseInt(arr[5]);
                    final boolean directionIsVertical = 1 == Integer.parseInt(arr[6]);
                    final int offset = Integer.parseInt(arr[7]);
                    final int noDataNoPrintFlg = Integer.parseInt(arr[8]);
                    final String linkSubform = unquote(arr[9]);
                    final int waku = Integer.parseInt(arr[10]);
                    final int cornerFlg = Integer.parseInt(arr[11]);
                    final int radius = Integer.parseInt(arr[12]);
                    final int cornerCutFlg = Integer.parseInt(arr[13]);
                    final int frameFixFlg = Integer.parseInt(arr[14]);
                    final LineOption[] lineFlags = new LineOption[4];
                    for (int i = 0; i < 4; i++) {
                        final int v1 = Integer.parseInt(arr[15 + i * 3]);
                        final int v2 = Integer.parseInt(arr[15 + i * 3 + 1]);
                        final int v3 = Integer.parseInt(arr[15 + i * 3 + 2]);
                        lineFlags[i] = LineOption.of(LineKind.valueOf(v1), 1 == v3 ? LineWidth.width(v2) : LineWidth.valueOf(v2));// 15 ~ 26
                    }
                    for (int i = 0; i < 3 * 4; i++) {
                    }
                    final int groupSuppressKeyBreakFlg = Integer.parseInt(arr[27]);
                    cl._subForm = new SubForm(unquote(fieldname), new Point(x, y), new Point(x2, y2), directionIsVertical, offset, noDataNoPrintFlg, StringUtils.isBlank(linkSubform) ? null : linkSubform
                            , FrameLine.valueOf(waku), cornerFlg, radius, cornerCutFlg, frameFixFlg, lineFlags, groupSuppressKeyBreakFlg);
                    if (_debug) {
                        log.info(" subForm = " + cl._subForm);
                    }
                } else {
                    if (_debug) {
                        log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                    }
                    contentDiv = ContentDiv.NOTHING;
                }
            }
            break;
            case REPEAT: {
                    if (readLine.startsWith(";") && !readLine.startsWith(";**")) {
                        //log.info(" comment : " + contents);
                        continue;
                    } else if (readLine.startsWith(" Repeat")) {
                        currentType = ReadType.REPEAT;
                        cl._type = currentType;
                        final String[] arr = splitBySpace(readLine);
                        final String repeatNo = arr[1];
                        final int left = Integer.parseInt(arr[2]);
                        final int top = Integer.parseInt(arr[3]);
                        final int right = Integer.parseInt(arr[4]);
                        final int bottom = Integer.parseInt(arr[5]);
                        final int direction = Integer.parseInt(arr[6]);
                        final int count = Integer.parseInt(arr[7]);
                        final int pitch = Integer.parseInt(arr[8]);
                        final int mod = Integer.parseInt(arr[9]);
                        final String page = unquote(arr[10]);
                        cl._repeat = new Repeat(repeatNo, left, top, right, bottom, direction, count, pitch, mod, page);
                        if (_debug) {
                            log.info(" repeat = " + cl._repeat);
                        }
                    } else if (StringUtils.isBlank(readLine)) {
                        // ignore
                    } else {
                        if (_debug) {
                            log.info(" set nothing :" + readLine + " (" + contentDiv + ")");
                        }
                        contentDiv = ContentDiv.NOTHING;
                    }
                }
                break;
            default:
            }
            if (ContentDiv.NOTHING == contentDiv) {
                if (ContentDiv.PAGE.containsComment(readLine)) {
                    contentDiv = ContentDiv.PAGE;
                    currentType = ReadType.HEADER;
                } else if (ContentDiv.MARGIN_DEFINIION.containsComment(readLine)) {
                    contentDiv = ContentDiv.MARGIN_DEFINIION;
                    currentType = ReadType.MARGIN_DEFINITION;
                } else if (ContentDiv.LINE.containsComment(readLine)) {
                    contentDiv = ContentDiv.LINE;
                    currentType = ReadType.LINE;
                } else if (ContentDiv.BOX.containsComment(readLine)) {
                    contentDiv = ContentDiv.BOX;
                    currentType = ReadType.BOX;
                } else if (ContentDiv.KOTEIMOJI.containsComment(readLine)) {
                    contentDiv = ContentDiv.KOTEIMOJI;
                    currentType = ReadType.TEXT;
                } else if (ContentDiv.COLOR.containsComment(readLine)) {
                    contentDiv = ContentDiv.COLOR;
                    currentType = ReadType.COLOR;
                } else if (ContentDiv.IMAGE.containsComment(readLine)) {
                    contentDiv = ContentDiv.IMAGE;
                    currentType = ReadType.IMAGE;
                } else if (ContentDiv.FIELD.containsComment(readLine)) {
                    contentDiv = ContentDiv.FIELD;
                    currentType = ReadType.FIELD;
                } else if (ContentDiv.SUBFORM_RECORD.containsComment(readLine)) {
                    contentDiv = ContentDiv.SUBFORM_RECORD;
                    currentType = ReadType.SUBFORM;
                } else if (ContentDiv.REPEAT.containsComment(readLine)) {
                    contentDiv = ContentDiv.REPEAT;
                    currentType = ReadType.REPEAT;
                }
                cl._type = currentType;
                if (ContentDiv.NOTHING != contentDiv) {
                    if (_debug) {
                        log.info(" switch to " + readLine);
                    }
                }
            } else {
                cl._type = currentType;
            }
            if (null != contentsLineBefore && contentDivBefore == ContentDiv.FIELD && contentDiv != ContentDiv.FIELD) {
                contentsLineBefore._isLastField = true;
            }
            contentsLineBefore = cl;
            contentDivBefore = contentDiv;
        }
        if (null != contentsLineBefore && contentDivBefore == ContentDiv.FIELD) {
            contentsLineBefore._isLastField = true;
        }
        SubForm subformBefore = null;
        for (final ContentsLine contentLine : contentLines) {
            if (null != contentLine._subForm) {
                subformBefore = contentLine._subForm;
            } else if (null != contentLine._record) {
                if (null == subformBefore) {
                    log.warn("record has no subform : " + contentLine._record);
                } else {
                    contentLine._record.setSubForm(subformBefore);
                }
            }
        }

        final List<String> listCLine = new ArrayList<String>();
        final List<String> listCText = new ArrayList<String>();
        final List<String> listField = new ArrayList<String>();
        final List<String> listImage = new ArrayList<String>();
        final List<String> listBox = new ArrayList<String>();
        final List<List<ContentsLine>> listListCField = new ArrayList<List<ContentsLine>>();
        boolean isCField = false;
        for (int i = 0; i < _contents.size(); i++) {
            final ContentsLine cl = _contents.get(i);
            if (null != cl._cField) {
                if (!isCField) {
                    listListCField.add(new ArrayList<ContentsLine>());
                }
                listListCField.get(listListCField.size() - 1).add(cl);
                isCField = true;
            } else {
                if (isCField) {
                    isCField = false;
                }
                if (null != cl._cLine) {
                    listCLine.add(cl._cLine);
                } else if (null != cl._cText) {
                    listCText.add(cl._cText);
                } else if (null != cl._field) {
                    listField.add(cl._readLine);
                } else if (null != cl._image) {
                    listImage.add(cl._readLine);
                } else if (null != cl._box) {
                    listBox.add(cl._readLine);
                }
            }
        }
        final List<ContentsLine> countCFieldOfField = new ArrayList<ContentsLine>();
        final List<ContentsLine> countCFieldOfImage = new ArrayList<ContentsLine>();
        if (listField.size() > 0 && listImage.size() > 0 && listListCField.size() == 2) {
            countCFieldOfField.addAll(listListCField.get(0));
            countCFieldOfImage.addAll(listListCField.get(1));
        } else if (listField.size() > 0 && listImage.size() > 0 && listListCField.size() == 1 && listListCField.get(0).size() == listField.size() + listImage.size()) {
            countCFieldOfField.addAll(listListCField.get(0).subList(0, listField.size()));
            countCFieldOfImage.addAll(listListCField.get(0).subList(listField.size(), listListCField.get(0).size()));
        } else if (listField.size() > 0 && listListCField.size() == 1) {
            countCFieldOfField.addAll(listListCField.get(0));
        } else if (listImage.size() > 0 && listListCField.size() == 1) {
            countCFieldOfImage.addAll(listListCField.get(0));
        } else if (listField.size() == 0 && listImage.size() == 0 && listListCField.size() == 0) {
            // do nothing
        } else {
            log.warn("not match CField count : " + listField.size() + " + " + listImage.size() + " <> " + listListCField.size());
        }
        for (final ContentsLine cl : countCFieldOfImage) {
            cl._cImage = cl._cField;
            cl._cImageArray = cl._cFieldArray;
            cl._cField = null;
        }

        final List<ImageField> imageFields = getElementList(ImageField.class);
        for (int i = 0; i < imageFields.size(); i++) {
            final ImageField imageField = imageFields.get(i);
            if (i < countCFieldOfImage.size()) {
                imageField._color = countCFieldOfImage.get(i)._cImageArray[2];
            }
        }
    }

    private static String[] splitBySpace(final String s) {
        final List<String> list = new ArrayList<String>();
        if (null != s) {
            final LinkedList<Character> rest = new LinkedList<Character>();
            for (final char ch : s.toCharArray()) {
                rest.add(ch);
            }
            StringBuilder current = null;
            boolean isModeString = false;
            while (rest.size() > 0) {
                final char next = rest.pollFirst();
                final boolean isSpace = Character.isWhitespace(next);
                if (null == current) {
                    if (next == '"') {
                        current = new StringBuilder();
                        isModeString = true;
                        current.append(next);
                    } else if (isSpace) {
                        continue;
                    } else {
                        current = new StringBuilder();
                        current.append(next);
                    }
                } else {
                    // current not null
                    if (isModeString) {
                        if (next == '"' && rest.size() > 1 && rest.get(0) == '"') { // 「"」が2文字連続 -> SVFの「"」のEscape Sequence
                            rest.pollFirst();
                            current.append("\\\\").append(next);
                        } else if (next == '"') {
                            current.append(next);
                            list.add(current.toString());
                            current = null;
                            isModeString = false;
                        } else {
                            current.append(next);
                        }
                    } else if (isSpace) {
                        list.add(current.toString());
                        current = null;
                    } else {
                        current.append(next);
                    }
                }
            }
            if (null != current && current.length() > 0) {
                list.add(current.toString());
            }
        }
        final String[] arr = new String[list.size()];
        return list.toArray(arr);
    }

    private void println(final PrintWriter w, final String s) {
        final byte[] newline = {0x0d, 0x0a}; // 改行コードCR+LF
        w.print(s);
        w.write(newline[0]);
        w.write(newline[1]);
    }

    private void println(final PrintWriter w, final String[] ss) {
        for (final String s : ss) {
            println(w, s);
        }
    }

    public File writeTempFile() throws IOException {
        final File newFile = File.createTempFile(_formFile.getName(), ".frm", _formFile.getParentFile());
        if (_debug) {
            log.info(" create file " + newFile + " ... line = " + _contents.size());
        }
        PrintWriter w = null;
        try {
//    		boolean addedLine = false;
//    		boolean addedField = false;
//    		boolean addedKoteiMoji = false;
//    		boolean addedImage = false;
//    		boolean addedBox = false;
            w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(newFile), encoding));

//        	for (int i = 0; i < _contents.size(); i++) {
//        		log.info(" _contents " + i + " = " + _contents.get(i)._type);
//        	}

            // KoteiMoji
            final List<KoteiMoji> koteimojis = getAddRemoveResultList(KoteiMoji.class);
            Collections.sort(koteimojis, new YxComparator());

            // Line
            final List<Line> lines = getAddRemoveResultList(Line.class);
            Collections.sort(lines, new YxComparator());

            // Field
            final List<Field> fields = getAddRemoveResultList(Field.class);
            Collections.sort(fields, new YxComparator());

            // ImageField
            final List<ImageField> imageFields = getAddRemoveResultList(ImageField.class);
            Collections.sort(imageFields, new YxComparator());

            // SubForm
            final List<SubForm> subFormList = getAddRemoveResultList(SubForm.class);

            final List<ContentsLine> subFormHeaderLines = new ArrayList<ContentsLine>();
            for (int i = 0; i < _contents.size(); i++) {
                final ContentsLine cl = _contents.get(i);
                if (cl._isSubformHeader) {
                    subFormHeaderLines.add(cl);
                }
            }
            if (subFormHeaderLines.size() > 0) {
                subFormHeaderLines.get(subFormHeaderLines.size() - 1)._isLastSubformHeader = true;
            }

            for (int i = 0; i < _contents.size(); i++) {
                final ContentsLine cl = _contents.get(i);
                boolean add = true;

                for (final ContentDiv cd : Arrays.asList(ContentDiv.BOX, ContentDiv.KOTEIMOJI, ContentDiv.LINE, ContentDiv.REPEAT, ContentDiv.FIELD, ContentDiv.IMAGE)) {
                    if (cd.containsAny(cl._readLine)) {
                        add = false;
                    }
                }
                if (null != cl._subForm || null != cl._record || cl._type == ReadType.BOX || cl._type == ReadType.TEXT || cl._type == ReadType.LINE || cl._type == ReadType.REPEAT) {
                    add = false;
                } else if (null != cl._line) {
                    add = false;
//        			if (!addedLine) {
//        				for (final Line line : lines) {
//        					println(w, StringUtils.defaultString(line._srcString, line.toSvfString()));
//        				}
//        				addedLine = true;
//        			}
                } else if (null != cl._field) {
                    add = false;
//        			if (!addedField) {
//        				int no = 1;
//        				for (final Field field : fields) {
//        					println(w, field.setNo(String.valueOf(no)).toSvfString());
//                    		no += 1;
//        				}
//        				addedField = true;
//        			}
                } else if (null != cl._box) {
//        			if (!addedBox) {
//        				for (final Box box : getContentAddList(Box.class)) {
//        					println(w, box.toSvfString());
//        				}
//        				addedBox = true;
//        			}
//        			if (getContentRemoveList(Box.class).contains(cl._box)) {
//        				add = false;
//        			}
                    add = false;
                } else if (null != cl._koteiMoji) {
                    add = false;
//        			if (!addedKoteiMoji) {
//        				for (final KoteiMoji koteimoji : koteimojis) {
//        					println(w, koteimoji.toSvfString());
//        				}
//        				addedKoteiMoji = true;
//        			}
                } else if (null != cl._image) {
//        			if (!addedImage) {
//        				for (final ImageField imageField : imageFields) {
//        					println(w, imageField.toSvfString());
//        				}
//        				addedImage = true;
//        			}
                    add = false;
                } else if (null != cl._cSubForm || null != cl._cRecord || null != cl._cLine || null != cl._cBox || null != cl._cText || null != cl._cField || null != cl._cImage) {
                    // 色情報
                    add = false;
                }
                if (add) {
                    if (cl._isPageHeader) {
                        final String[] arr = splitBySpace(cl._readLine);
                        arr[5] = _adjtX.toString();
                        arr[6] = _adjtY.toString();
                        arr[25] = _pageColor;
                        println(w, join(arr, " "));

                    } else {
                        println(w, cl._readLine);
                    }
                    boolean printItems = false;
                    if (subFormHeaderLines.size() == 0 && cl._isMarginDefinition) {
                        println(w, ContentDiv.SUBFORM_RECORD._teigiComment);
                        printItems = true;
                    } else if (cl._isLastSubformHeader) {
                        printItems = true;
                    }
                    if (printItems) {
                        for (final SubForm subForm : subFormList) {
                            println(w, subForm.toSvfString());
                            for (final SvfForm.Record record : getAddRemoveSubFormRecordList(subForm)) {
                                println(w, record.toSvfString());
                            }
                        }

                        if (!koteimojis.isEmpty()) {
                            println(w, ContentDiv.KOTEIMOJI._teigiComment);
                            for (final KoteiMoji koteimoji : koteimojis) {
                                println(w, koteimoji.toSvfString());
                            }
                        }

                        if (!fields.isEmpty()) {
                            println(w, ContentDiv.FIELD._teigiComment);
                            int no = 1;
                            for (final Field field : fields) {
                                println(w, field.setNo(String.valueOf(no)).toSvfString());
                                no += 1;
                            }
                        }

                        if (!lines.isEmpty()) {
                            println(w, ContentDiv.LINE._teigiComment);
                            for (final Line line : lines) {
                                println(w, StringUtils.defaultString(line._srcString, line.toSvfString()));
                            }
                        }

                        if (!imageFields.isEmpty()) {
                            println(w, ContentDiv.IMAGE._teigiComment);
                            for (final ImageField imageField : imageFields) {
                                println(w, imageField.toSvfString());
                            }
                        }

                        // Box
                        final List<Box> boxes = getAddRemoveResultList(Box.class);
                        if (!boxes.isEmpty()) {
                            println(w, ContentDiv.BOX._teigiComment);
                            for (final Box box : boxes) {
                                println(w, box.toSvfString());
                            }
                        }

                        // Repeat
                        final List<Repeat> repeats = getAddRemoveResultList(Repeat.class);
                        if (!repeats.isEmpty()) {
                            println(w, ContentDiv.REPEAT._teigiComment);
                            for (final Repeat repeat : repeats) {
                                println(w, repeat.toSvfString());
                                println(w, ""); // Repeatのみ空行あり
                            }
                        }
                    }
                    if (ContentDiv.COLOR.containsComment(cl._readLine)) {

                        for (final SubForm subForm : subFormList) {
                            println(w, "CSubForm 17");
                            for (int j = 0; j < getAddRemoveSubFormRecordList(subForm).size(); j++) {
                                println(w, "CRecord 17 17");
                            }
                        }

                        for (int j = 0, max = getAddRemoveResultList(KoteiMoji.class).size(); j < max; j++) {
                            println(w, "CText 17");
                        }

                        for (int j = 0, max = fields.size(); j < max; j++) {
                            println(w, "CField " + String.valueOf(j + 1) + " " + "17");
                        }


                        for (int j = 0, max = getAddRemoveResultList(Line.class).size(); j < max; j++) {
                            println(w, "CLine 17");
                        }

                        for (int j = 0, max = imageFields.size(); j < max; j++) {
                            final ImageField img = imageFields.get(j);
                            println(w, "CField " + String.valueOf(j + 1) + " " + img._color);
                        }

                        for (int j = 0, max = getAddRemoveResultList(Box.class).size(); j < max; j++) {
                            println(w, "CBox 17 17");
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("exception!", e);
            throw e;
        } finally {
            if (null != w) {
                try {
                    w.flush();
                    w.close();
                } catch (Exception e) {
                }
            }
        }
        return newFile;
    }

    public <T extends Element> List<T> getAddRemoveResultList(final Class<T> c) {
        final List<T> rtn = getElementList(c);
        rtn.removeAll(getContentRemoveList(c));
        rtn.addAll(getContentAddList(c));
        return rtn;
    }

    public void checkKindai() {
        // 近大
        // vfreport.PDF.properties.ja
        // UsePdfAdjust=True
        // Adjust=96,-70
        KNJDefineSchool ds = new KNJDefineSchool();
        if ("KIN".equals(ds.schoolmark)) {
            addAdjtX(new BigDecimal(-96));
            addAdjtY(new BigDecimal(-70));
        }
    }

    public BigDecimal getAdjtX() {
        return _adjtX;
    }

    public BigDecimal getAdjtY() {
        return _adjtY;
    }

    public void setAdjtX(final BigDecimal x) {
        _adjtX = x;
    }

    public void setAdjtY(final BigDecimal y) {
        _adjtY = y;
    }

    public void addAdjtX(final BigDecimal x) {
        setAdjtX(getAdjtX().add(x));
    }

    public void addAdjtY(final BigDecimal y) {
        setAdjtY(getAdjtY().add(y));
    }

    public void setColor(final boolean isColor) {
        _pageColor = isColor ? "1" : "0";
    }

    private static String join(final String[] list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String comma0 = "";
        for (final String s : list) {
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    private static String leftPad(final int keta, final char ch, final int s) {
        return StringUtils.leftPad(String.valueOf(s), keta, ch);
    }

//    private static String[] parseLine(final String line) {
//    	final List<String> list = new ArrayList<String>();
//        StringBuffer stb = new StringBuffer();
//        final char[] chars = line.toCharArray();
//        boolean flg1 = false;
//        boolean flg2 = false;
//        for (int i = 0, len = chars.length; i < len; i++) {
//            final char c = chars[i];
//            switch (c) {
//            case '\t':
//            case ' ':
//                if (flg1) {
//                    stb.append(c);
//                    flg2 = true;
//                } else if (stb.length() > 0) {
//                    list.add(stb.toString());
//                    stb = new StringBuffer();
//                    flg2 = false;
//                }
//                break;
//            case '"':
//                if (flg1) {
//                    if ((i + 1 < len) && (chars[(i + 1)] == '"')) {
//                        i++;
//                        stb.append(c);
//                        flg2 = true;
//                    } else {
//                        flg1 = false;
//                        list.add(stb.toString());
//                        stb = new StringBuffer();
//                        flg2 = false;
//                    }
//                } else {
//                	flg1 = true;
//                }
//                break;
//            default:
//                stb.append(c);
//                flg2 = true;
//            }
//        }
//        if (flg2) {
//          list.add(stb.toString());
//        }
//        final String[] arr = new String[list.size()];
//        return list.toArray(arr);
//    }

    public static class Element {

    }

    public static class Point extends Element implements Comparable<Point> {
        public final int _x;
        public final int _y;
        public Point(final int x, final int y) {
            _x = x;
            _y = y;
        }
        public Point setX(final int x) {
            return new Point(x, _y);
        }
        public Point setY(final int y) {
            return new Point(_x, y);
        }
        public Point addX(final int addx) {
            return setX(_x + addx);
        }
        public Point addY(final int addy) {
            return setY(_y + addy);
        }
        public Point add(final Point point) {
            return addX(point._x).addY(point._y);
        }
        public Point negate(final Point point) {
            return new Point(-_x, -_y);
        }
        public boolean xBetween(final int x1, final int x2) {
            return x1 <= _x && _x <= x2;
        }
        public boolean xBetween(final Point p1, final Point p2) {
            return xBetween(p1._x, p2._x);
        }
        public boolean yBetween(final int y1, final int y2) {
            return y1 <= _y && _y <= y2;
        }
        public boolean yBetween(final Point p1, final Point p2) {
            return yBetween(p1._y, p2._y);
        }
        public int compareTo(Point op) {
            return (_x * 10000 + _y) - (op._x * 10000 + op._y);
        }
        public static int abs(final Point p1, final Point p2) {
            return (int) Math.sqrt(Math.pow((p2._x - p1._x), 2) + Math.pow((p2._y - p1._y), 2));
        }
        public boolean equals(final Object o) {
            if (o instanceof Point) {
                final Point oe = (Point) o;
                return _x == oe._x && _y == oe._y;
            }
            return false;
        }
        public String toString() {
            return "P(" + _x + ", " + _y + ")";
        }
    }

    public interface Positioned {
        public Point getPoint();
    }

    public static class Line extends Element implements Comparable<Line>, Positioned {
        String _srcString;
        public final boolean _isFline;
        public final LineKind _lineKind;
        public final LineWidth _lineWidth;
        public final Point _start;
        public final Point _end;
//    	int _injiIchiChouseiPointX;
//    	int _injiIchiChouseiPointY;

        @Deprecated
        public Line(final int lineKind, final int lineWidth, final Point start, final Point end, final String srcString) {
            this(true, LineKind.valueOf(lineKind), LineWidth.valueOf(lineWidth), start, end, srcString);
        }
        @Deprecated
        public Line(final int lineWidth, final Point start, final Point end) {
            this(false, LineKind.SOLID, LineWidth.valueOf(lineWidth), start, end, null);
        }
        public Line(final Point start, final Point end) {
            this(false, LineKind.SOLID, LineWidth.THIN, start, end, null);
        }
        public Line(final LineWidth lineWidth, final Point start, final Point end) {
            this(false, LineKind.SOLID, lineWidth, start, end, null);
        }
        public Line(final boolean isFline, final LineKind lineKind, final LineWidth lineWidth, final Point start, final Point end) {
            this(isFline, lineKind, lineWidth, start, end, null);
        }
        private Line(final boolean isFline, final LineKind lineKind, final LineWidth lineWidth, final Point start, final Point end, final String srcString) {
            _isFline = isFline;
            _lineKind = lineKind;
            _lineWidth = lineWidth;
            _srcString = srcString;
            _start = start;
            _end = end;
        }

        public Point getPoint() {
            return _start;
        }

        public String toSvfString() {
            if (_isFline) {
                return "fLine " + _lineKind._idx + " " + _lineWidth._idx + " " + _start._x + " " + _start._y + " " + _end._x + " " + _end._y + " 0 " + (_lineWidth._inputFlag ? "1" : "0");
            }
            return " Line " + _lineKind._idx + " " + _lineWidth._idx + " 0 " + _start._x + " " + _start._y + " " + _end._x + " " + _end._y + " 0 " + quote("1") + " 0 " + (_lineWidth._inputFlag ? "1" : "0");
        }
        public Line setFline(final boolean isFline) {
            return new Line(isFline, _lineKind, _lineWidth, _start, _end, null);
        }
        public Line setLineKind(final LineKind lineKind) {
            return new Line(_isFline, lineKind, _lineWidth, _start, _end, null);
        }
        public Line setLineWidth(final LineWidth lineWidth) {
            return new Line(_isFline, _lineKind, lineWidth, _start, _end, null);
        }
        public Line setX(final int x) {
            return addX(x - _start._x);
        }
        public Line setY(final int y) {
            return addY(y - _start._y);
        }
        public Line addX(final int addx) {
            return new Line(_isFline, _lineKind, _lineWidth, _start.addX(addx), _end.addX(addx), null);
        }
        public Line addY(final int addy) {
            return new Line(_isFline, _lineKind, _lineWidth, _start.addY(addy), _end.addY(addy), null);
        }
        public Line setStart(final Point start) {
            return setX(start._x).setY(start._y);
        }
        public Line setEnd(final Point end) {
            return new Line(_isFline, _lineKind, _lineWidth, _start, end, null);
        }
        /**
         * 縦線か
         * @return 縦線ならtrue
         */
        public boolean isVertical() {
            return _start._x == _end._x;
        }
        /**
         * 横線か
         * @return 横線ならtrue
         */
        public boolean isHorizontal() {
            return _start._y == _end._y;
        }
        public int compareTo(Line ol) {
            int cmp;
            cmp = _start.compareTo(ol._start);
            if (0 != cmp) {
                return cmp;
            }
            cmp = _end.compareTo(ol._end);
            return cmp;
        }
        public boolean equals(final Object o) {
            if (o instanceof Line) {
                final Line oe = (Line) o;
                return _lineWidth == oe._lineWidth && _start.equals(oe._start) && _end.equals(oe._end);
            }
            return false;
        }
        public String toString() {
            return "SvfLine(" + _lineKind + ", " + _lineWidth + ", " + _start + ", " + _end + ")";
        }
        public Map toMap() {
            final Map map = new HashMap();
            map.put("X1", String.valueOf(_start._x));
            map.put("Y1", String.valueOf(_start._y));
            map.put("X2", String.valueOf(_end._x));
            map.put("Y2", String.valueOf(_end._y));
//			map.put("INJI_ICHI_CHOUSEI_POINT_X", String.valueOf(_injiIchiChouseiPointX));
//			map.put("INJI_ICHI_CHOUSEI_POINT_Y", String.valueOf(_injiIchiChouseiPointY));
            return map;
        }
    }

    public static class Box extends Element implements Positioned {
        public final LineKind _lineKind;
        public final LineWidth _lineWidth;
        private final int _w3;
        private final int _paint;
        private final int _soto; // 0:枠線なし 1:枠線あり
        public final Point _upperLeft;
        public final Point _lowerRight;
        public final int _cornerBits; // 1:左上、2:右上、4:左下、8:右下の合計
        public final int _cornerRadius; // 1以上:コーナースタイル丸
        private final int _w12;
        private final int _amikakeMeido;
        private final String _copy;
        private final int _repeatId;
        public final int _cornerCutFlg; // 1:コーナースタイルカット(cornerFlag=1前提) 0:コーナースタイルカット以外
        private final int _widthValueFlag; // 1:線幅は手動指定
        @Deprecated
        public Box(final int lineKind, final int lineWidth, final Point upperLeft, final Point lowerRight) {
            this(LineKind.valueOf(lineKind), LineWidth.valueOf(lineWidth), 0, 0, 1, upperLeft, lowerRight, 0, 0, 0, 0, "1", 0, 0, 0);
        }
        public Box(final LineKind lineKind, final LineWidth lineWidth, final Point upperLeft, final Point lowerRight) {
            this(lineKind, lineWidth, 0, 0, 1, upperLeft, lowerRight, 0, 0, 0, 0, "1", 0, 0, 0);
        }
        private Box(final LineKind lineKind, final LineWidth lineWidth, final int w3, final int paint, final int soto, final Point upperLeft, final Point lowerRight
                , final int cornerBits, final int cornerRadius, final int w12, final int amikakeMeido
                , final String copy, final int repeatId, final int cornerCutFlg, final int widthValueFlag) {
            _lineKind = lineKind;
            _lineWidth = lineWidth;
            _w3 = w3;
            _paint = paint;
            _soto = soto;
            _upperLeft = upperLeft;
            _lowerRight = lowerRight;
            _cornerBits = cornerBits;
            _cornerRadius = cornerRadius;
            _w12 = w12;
            _amikakeMeido = amikakeMeido;
            _copy = copy;
            _repeatId = repeatId;
            _cornerCutFlg = cornerCutFlg;
            _widthValueFlag = widthValueFlag;
        }
        public Box setLineKind(final LineKind lineKind) {
            return new Box(lineKind, _lineWidth, _w3, _paint, _soto, _upperLeft, _lowerRight
                    , _cornerBits, _cornerRadius, _w12, _amikakeMeido
                    , _copy, _repeatId, _cornerCutFlg, _widthValueFlag);
        }
        public Box setLineWidth(final LineWidth lineWidth) {
            return new Box(_lineKind, lineWidth, _w3, _paint, _soto, _upperLeft, _lowerRight
                    , _cornerBits, _cornerRadius, _w12, _amikakeMeido
                    , _copy, _repeatId, _cornerCutFlg, _widthValueFlag);
        }
        public Box setCornerBits(final int cornerBits) {
            return new Box(_lineKind, _lineWidth, _w3, _paint, _soto, _upperLeft, _lowerRight
                    , cornerBits, _cornerRadius, _w12, _amikakeMeido
                    , _copy, _repeatId, _cornerCutFlg, _widthValueFlag);
        }
        public Box setCornerRadius(final int cornerRadius) {
            return new Box(_lineKind, _lineWidth, _w3, _paint, _soto, _upperLeft, _lowerRight
                    , _cornerBits, cornerRadius, _w12, _amikakeMeido
                    , _copy, _repeatId, _cornerCutFlg, _widthValueFlag);
        }
        public Point getPoint() {
            return _upperLeft;
        }
        public boolean contains(final Point point) {
            return between(point._x, _upperLeft._x, _lowerRight._x) && between(point._y, _upperLeft._y, _lowerRight._y);
        }
        public boolean equals(final Object o) {
            if (o instanceof Box) {
                final Box oe = (Box) o;
                return _lineKind == oe._lineKind && _lineWidth == oe._lineWidth && _upperLeft.equals(oe._upperLeft) && _lowerRight.equals(oe._lowerRight);
            }
            return false;
        }
        public List<Line> toLines() {
            return Arrays.asList(
                    new Line(true, _lineKind, _lineWidth, _upperLeft, new Point(_lowerRight._x, _upperLeft._y), null),
                    new Line(true, _lineKind, _lineWidth, new Point(_lowerRight._x, _upperLeft._y), _lowerRight, null),
                    new Line(true, _lineKind, _lineWidth, _upperLeft, new Point(_upperLeft._x, _lowerRight._y), null),
                    new Line(true, _lineKind, _lineWidth, new Point(_upperLeft._x, _lowerRight._y), _lowerRight, null)
                    );
        }
        public String toSvfString() {
            //return " Box " + _lineKind + " " + _lineWidth + " " + _w3 + " " + _paint + " " + _w5 + " " + _upperLeft._x + " " + _upperLeft._y + " " + _lowerRight._x + " " + _lowerRight._y + " " + _cornerBits + " " + _cornerRadius + " " + _w12 + "  " + _amikakeMeido + " " + quote(_copy) + " " + _repeatId + " " + _cornerCutFlg + " " + _widthValueFlag;
            return " Box      " + _lineKind._idx + "     " + _lineWidth._idx + "     " + _w3 + "      " + _paint + "     " + _soto + "  " + keta(_upperLeft._x, 4) + "  " + keta(_upperLeft._y, 4) + "  " + keta(_lowerRight._x, 4) + "  " + keta(_lowerRight._y, 4) + "     " + keta(_cornerBits, 2) + "        " + keta(_cornerRadius, 2) + "     " + _w12 + "      " + _amikakeMeido + " " + quote(_copy) + "  " + keta(_repeatId, 3) + "          " + _cornerCutFlg + "           " + _widthValueFlag + " ";
        }
        public String toString() {
            return "Box(" + _lineKind + ", " + _lineWidth + ", " + _upperLeft + ", " + _lowerRight + ")";
        }
    }

    public static class KoteiMoji extends Element implements Positioned {
        public final boolean _isFText;
        public final String _moji;
        public final Font _font;
        public final int _endX;
        public final Point _point;
        public final MojiShushoku _mojishushoku;
        public final int _charPoint10;
        public final boolean _isVertical;
        public final Bunsu _tatebai; // 1 ~ 8 / 1, 2
        public final Bunsu _yokobai; // 1 ~ 8 / 1, 2
        private final String _hankakuFont; // 0~
        private final int _degree;
        private final String _pitchFreeOrFix; // 0 or 1

        @Deprecated
        public KoteiMoji(final String moji, final int minchoOrGothic, final Point point, final int charPoint10, final boolean isVertical) {
            this(true, moji, Font.valueOf(minchoOrGothic), -1, point, charPoint10, isVertical, MojiShushoku._default, Bunsu._default, Bunsu._default, 0);
        }
        public KoteiMoji(final String moji, final Point point, final int charPoint10) {
            this(false, moji, Font.Mincho, -1, point, charPoint10, false, MojiShushoku._default, Bunsu._default, Bunsu._default, 0);
        }

        private KoteiMoji(final boolean isFText, final String moji, final Font font, final int endX, final Point point, final int charPoint10, final boolean isVertical, final MojiShushoku mojiShushoku, final Bunsu tatebai, final Bunsu yokobai, final int degree) {
            _isFText = isFText;
            _moji = moji;
            _font = font;
            _endX = endX;
            _point = point;
            _mojishushoku = mojiShushoku;
            _charPoint10 = charPoint10;
            _isVertical = isVertical;
            _tatebai = tatebai;
            _yokobai = yokobai;
            _hankakuFont = "0";
            _degree = degree;
            _pitchFreeOrFix = "0";
        }
        public KoteiMoji setFText(final boolean isFText) {
            return new KoteiMoji(isFText, _moji, _font, _endX, _point, _charPoint10, _isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji replaceMojiWith(final String moji) {
            return new KoteiMoji(_isFText, moji, _font, _endX, _point, _charPoint10, _isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji setFont(final Font font) {
            return new KoteiMoji(_isFText, _moji, font, _endX, _point, _charPoint10, _isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji setEndX(final int endX) {
            return new KoteiMoji(_isFText, _moji, _font, endX, _point, _charPoint10, _isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji setX(final int x) {
            return setPoint(_point.setX(x));
        }
        public KoteiMoji setY(final int y) {
            return setPoint(_point.setY(y));
        }
        public KoteiMoji addX(final int addx) {
            return setPoint(_point.addX(addx));
        }
        public KoteiMoji addY(final int addy) {
            return setPoint(_point.addY(addy));
        }
        public KoteiMoji setPoint(final Point point) {
            return new KoteiMoji(_isFText, _moji, _font, _endX + (point._x - _point._x), point, _charPoint10, _isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji setMojiPoint(final int charPoint10) {
            return new KoteiMoji(_isFText, _moji, _font, -1, _point, charPoint10, _isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji setVertical(final boolean isVertical) {
            return new KoteiMoji(_isFText, _moji, _font, -1, _point, _charPoint10, isVertical, _mojishushoku, _tatebai, _yokobai, _degree);
        }
        public KoteiMoji setShiromoji(final boolean shiromoji) {
            return setMojiShushoku(_mojishushoku.setShiromoji(shiromoji));
        }
        public KoteiMoji setOutline(final boolean outline) {
            return setMojiShushoku(_mojishushoku.setOutline(outline));
        }
        public KoteiMoji setBold(final boolean bold) {
            return setMojiShushoku(_mojishushoku.setBold(bold));
        }
        public KoteiMoji setItalic(final boolean italic) {
            return setMojiShushoku(_mojishushoku.setItalic(italic));
        }
        public KoteiMoji setShadow(final boolean shadow) {
            return setMojiShushoku(_mojishushoku.setShadow(shadow));
        }
        private KoteiMoji setMojiShushoku(final MojiShushoku mojiShushoku) {
            return new KoteiMoji(_isFText, _moji, _font, _endX, _point, _charPoint10, _isVertical, mojiShushoku, _tatebai, _yokobai, _degree);
        }
        public Point getPoint() {
            return _point;
        }
        public String toSvfString() {
            final int endX;
            if (_endX == -1) {
                if (_isVertical) {
                    endX = _point._y + _charPoint10 * _moji.length();
                } else {
                    endX = _point._x + (int) charPointToPixel(_charPoint10 / 10.0);
                }
            } else {
                endX = _endX;
            }
            final String moji = _moji.length() > 0 && _moji.charAt(0) == '"' && _moji.charAt(_moji.length() - 1) == '"' ? _moji : "\"" + _moji + "\"";
            final String pre = _font._val + " " + _tatebai._bunshi + " " + _tatebai._bunbo + " " + _yokobai._bunshi + " " + _yokobai._bunbo + " " + leftPad(4, ' ', endX) + " " + (_isVertical ? "1" : "0") + " " + leftPad(4, ' ', _point._x) + " " + leftPad(4, ' ', _point._y) + " " + leftPad(4, ' ', _charPoint10) + " 0  0  " + (_mojishushoku._shiromoji ? "100" : "0") + "  " + (_mojishushoku._bold ? "1" : "0") + "  " + (_mojishushoku._italic ? "1" : "0") + "  " + (_mojishushoku._outline ? "1" : "0") + "  " + (_mojishushoku._shadow ? "1" : "0") + "  0  0  0 " + moji + " ";
            final String post = _degree + " 0 " + _hankakuFont + " -1 -1 " + _pitchFreeOrFix + " 0";
            if (_isFText == false) {
                return " Text    " + pre + "\"1\"" + " " + post;
            }
            return "fText    " + pre + post;
        }
        public boolean equals(final Object o) {
            if (o instanceof KoteiMoji) {
                final KoteiMoji oe = (KoteiMoji) o;
                return _moji.equals(oe._moji) && _endX == oe._endX && _point.equals(oe._point) && _charPoint10 == oe._charPoint10 && _isVertical == oe._isVertical;
            }
            return false;
        }
        public String toString() {
            return "KoteiMoji(" + _moji + ", " + _point + ")";
        }
    }

    public static class ImageField extends Element implements Positioned {
        public final String _no;
        public final String _fieldname;
        public final Point _point;
        public final int _endX;
        public final int _height;
        public final String _variableOrFixed;
        public final String _repeatNo;
        private String _color;
        public final String _rc;
        public final String _rhv;
        public final String _rpitch;
        public final String _repeatMod;
        public ImageField(final String no, final String fieldname, final Point point, final int endX, final int height, final String variableOrFixed, final String repeatNo) {
            this(no, fieldname, point, endX, height, variableOrFixed, repeatNo, "17", "0", "0", "0", "0");
        }
        private ImageField(final String no, final String fieldname, final Point point, final int endX, final int height, final String variableOrFixed, final String repeatNo, final String color, final String rc, final String rhv, final String rpitch, final String repeatMod) {
            _no = no;
            _fieldname = fieldname;
            _point = point;
            _endX = endX;
            _height = height;
            _variableOrFixed = variableOrFixed;
            _repeatNo = repeatNo;
            _color = color;
            _rc = rc;
            _rhv = rhv;
            _rpitch = rpitch;
            _repeatMod = repeatMod;
        }
        public ImageField copyTo(final String fieldname) {
            return new ImageField(null, fieldname, _point, _endX, _height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField setNo(final String no) {
            return new ImageField(no, _fieldname, _point, _endX, _height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField addX(final int addx) {
            return new ImageField(_no, _fieldname, _point.addX(addx), _endX + addx, _height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField addY(final int addy) {
            return new ImageField(_no, _fieldname, _point.addY(addy), _endX, _height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField setPoint(final Point point) {
            return setX(point._x).setY(point._y);
        }
        public ImageField setX(final int x) {
            return addX(x - _point._x);
        }
        public ImageField setY(final int y) {
            return addY(y - _point._y);
        }
        public ImageField setFieldname(final String fieldname) {
            return new ImageField(_no, fieldname, _point, _endX, _height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField setHeight(final int height) {
            return new ImageField(_no, _fieldname, _point, _endX, height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField setEndX(final int endX) {
            return new ImageField(_no, _fieldname, _point, endX, _height, _variableOrFixed, _repeatNo, _color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public ImageField setColor(final String color) {
            return new ImageField(_no, _fieldname, _point, _endX, _height, _variableOrFixed, _repeatNo, color, _rc, _rhv, _rpitch, _repeatMod);
        }
        public String getColor() {
            return _color;
        }
        public Point getPoint() {
            return _point;
        }
        public String toSvfString() {
            final String cpy = quote("1");
            final String zp = "-1";
            final String kakudo = "0";
            final String type = "3";
            final String linkname = quote("");
            return " Field  " + _no + " " + quote(_fieldname) + StringUtils.repeat(" ", 18) + _variableOrFixed + "  1 1 1 1 1  " + _endX + " 0 " + _point._x + " " + _point._y + " " + _height + " 0 0 0 0 0 0 0 0 0 0 0 " + quote("") + " " + quote("") + " " + quote("") + " " + cpy + " " + zp + " " + kakudo + " " + type + " " + _repeatNo + " " + _rc + " " + _rhv + " " + _rpitch + " " + linkname + " 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 -1 -1 " + _repeatMod + " 0 0 " + quote("") + " 0 0 0 " + quote("") + " 0 0 -1 0 0 " + quote("") + " " + quote("");
        }
        public boolean equals(final Object o) {
            if (o instanceof ImageField) {
                final ImageField oe = (ImageField) o;
                return _no.equals(oe._no) && _fieldname.equals(oe._fieldname) && _point.equals(oe._point) && _endX == oe._endX && _height == oe._height && _variableOrFixed.equals(oe._variableOrFixed) && _repeatNo.equals(oe._repeatNo);
            }
            return false;
        }
        public String toString() {
            return "Image(" + _no + ", " + _fieldname + ", " + _point + ")";
        }
    }

    public static class Field extends Element implements Comparable<Field>, Positioned {
        public final String _no;
        public final String _fieldname;
        public final Font _font; // 0 or 1
        public final int _fieldLength;
        public final int _endX;
        public final boolean _directionIsVertical;
        public final Point _position;
        public final int _charPoint10;
        public final String _comment;
        public final String _editEqn;
        public final String _calcFormula;
        public final RepeatConfig _repeatConfig;
        public final String _linkFieldname;
        public final Bunsu _tatebai; // 1 ~ 8 / 1, 2
        public final Bunsu _yokobai; // 1 ~ 8 / 1, 2
        private final MojiShushoku _mojiShushoku;
        private final PrintMethod _printMethod; // 0 ~ 5
        private final String _dataType; // 0:文字型、1:数値型
        private final int _degree; // 0, 90, 180, 270
        private final int _dataLength; // 0~
        private final String _picthType;
        private final String _pitchFreeOrFix; // 0 or 1
        private final String _strStr;
        private final String _strCpy;
        private final int _hanzen; // 0 or 1
        private final String _mskNo;
        private final String _inputLock; // 0 or 1
        private final String _hanKind;
        private final String _linkDelimiter;
        /**
         * 改頁 +8<br>
         * マスク +1<br>
         * 先頭頁のみ +16<br>
         * 最終頁のみ +32<br>
         * グループの最後で印字 +256<br>
         * グループサプレス +128<br>
         * グループサプレス解除 +4<br>
         * 重複時の印刷 センタリング+64<br>
         * 重複時の罫線 印字しない+2<br>
         * 累積地のクリア +512<br>
         * ページカウントのクリア +1024<br>
         */
        private final int _mask;
        private final String _flg2;
        private final String _strRecName;
        private final String _flg3;
        private final String _headerRecName;

        public enum MaskFlag {
            MASK(1),
            CHOUFUKUJI_NO_KEISEN_INJISHINAI(2),
            GROUP_SUPPRESS_KAIJO(4),
            KAI_PAGE(8),
            SENTOU_PAGE_NOMI(16),
            SAISHU_PAGE_NOMI(32),
            CHOUFUKUJI_NO_INSATSU_CENTERING(64),
            GROUP_SUPPRESS(128),
            GROUP_NO_SAIGO_DE_INJI(256),
            RUISEKICHI_NO_CLEAR(512),
            PAGE_COUNT_NO_CLEAR(1024);

            final int _bits;
            MaskFlag(final int bit) {
                _bits = bit;
            }
            private static Collection<MaskFlag> flags(final int n) {
                final Set<MaskFlag> flags = new TreeSet();
                for (final MaskFlag flag : MaskFlag.values()) {
                    if ((n & flag._bits) > 0) {
                        flags.add(flag);
                    }
                }
                return flags;
            }
            private static int enabled(int n, final MaskFlag...flags) {
                for (final MaskFlag flag : flags) {
                    n |= flag._bits;
                }
                return n;
            }
            private static int disabled(int n, final MaskFlag...flags) {
                for (final MaskFlag flag : flags) {
                    n ^= flag._bits;
                }
                return n;
            }
            private static String bits(final int n) {
                final StringBuilder sb = new StringBuilder();
                for (final MaskFlag flag : values()) {
                    sb.append((flag._bits & n) > 0 ? 1 : 0);
                }
                return sb.reverse().toString();
            }
        }

        private static final String editEqnDefault = "";
        private static final int hanzenDefault = 0;
        private static final int maskDefault = 0;
        private static final String calcFormulaDefault = "";
        private static final String strStrDefault = "";
        private static final String strCpyDefault = "1";
        private static final int degreeDefault = 0;
        private static final String datatypeDefault = "0";
        private static final String flg2Default = "0";
        private static final String flg3Default = "0";

        @Deprecated
        public Field(final String no, final String fieldname, final int minchoOrGothic, final int fieldLength, final int endX, final boolean directionIsVertical, final Point position, final int charPoint10
                , final String shiromoji, final String bold, final String italic, final String outline, final String shadow, final String printMethod
                , final String comment, final String editEqn, final int hanzen, final String linkFieldname) {
            this(no, fieldname, Font.valueOf(minchoOrGothic), fieldLength, Bunsu._default, Bunsu._default, endX, directionIsVertical, position, charPoint10
                    , MojiShushoku._default.setShiromoji("100".equals(shiromoji)).setBold("1".equals(bold)).setItalic("1".equals(italic)).setOutline("1".equals(outline)).setShadow("1".equals(shadow)), PrintMethod.getPrintMethod(printMethod)
                    , comment, editEqn, hanzen, maskDefault
                    , new RepeatConfig("0", 0, 0, 0, 0)
                    , ""
                    , calcFormulaDefault, strStrDefault, strCpyDefault, degreeDefault, datatypeDefault, flg2Default, flg3Default
                    );
        }

        public Field(final String no, final String fieldname, final Font font, final int fieldLength, final int endX, final boolean directionIsVertical, final Point position, final int charPoint10
                , final String comment) {
            this(no, fieldname, font, fieldLength, Bunsu._default, Bunsu._default, endX, directionIsVertical, position, charPoint10
                    , MojiShushoku._default, PrintMethod.MUHENSHU
                    , comment, editEqnDefault, hanzenDefault, maskDefault
                    , new RepeatConfig("0", 0, 0, 0, 0)
                    , ""
                    , calcFormulaDefault, strStrDefault, strCpyDefault, degreeDefault, datatypeDefault, flg2Default, flg3Default
                    );
        }

        private Field(final String no, final String fieldname, final Font font, final int fieldLength, final Bunsu tatebai, final Bunsu yokobai, final int endX, final boolean directionIsVertical, final Point position, final int charPoint10
                , final MojiShushoku mojiShushoku, final PrintMethod printMethod
                , final String comment, final String editEqn, final int hanzen, final int mask, final RepeatConfig repeatConfig, final String linkFieldname
                , final String calcFormula, final String strStr, final String strCpy, final int degree, final String dataType
                , final String flg2, final String flg3) {
            _no = no;
            _fieldname = fieldname;
            _font = font;
            _fieldLength = fieldLength;
            _endX = endX;
            _directionIsVertical = directionIsVertical;
            _position = position;
            _charPoint10 = charPoint10;
            _comment = comment;
            _editEqn = StringUtils.defaultString(editEqn);
            _calcFormula = calcFormula;
            _strStr = strStr;
            _strCpy = strCpy;
            _repeatConfig = repeatConfig;
            _linkFieldname = linkFieldname;
            _mojiShushoku = mojiShushoku;
            _printMethod = printMethod;
            _tatebai = tatebai;
            _yokobai = yokobai;
            _dataType = dataType;
            _degree = degree;
            _dataLength = 0;
            _picthType = "0";
            _pitchFreeOrFix = "0";
            _hanzen = hanzen;
            _mskNo = "0";
            _inputLock = "0";
            _hanKind = "0";
            _linkDelimiter = "";
            _mask = mask;
            _flg2 = flg2;
            _strRecName = "";
            _flg3 = flg3;
            _headerRecName = "";
        }

        /**
         * フィールド名をセットしたフィールドインスタンスを得る
         * @param fieldname フィールド名
         * @return フィールド名をセットしたフィールドインスタンス
         */
        public Field copyTo(final String fieldname) {
            return new Field("", fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setNo(final String no) {
            return new Field(no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setPosition(final Point position) {
            final int newEndX;
            if (_directionIsVertical) {
                newEndX = position._y - _position._y + _endX;
            } else {
                newEndX = position._x - _position._x + _endX;
            }
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3).setEndX(newEndX);
        }
        public Field setX(final int x) {
            return setPosition(new Point(x, _position._y));
        }
        public Field addX(final int dx) {
            return setX(_position._x + dx);
        }
        public Field setY(final int y) {
            return setPosition(new Point(_position._x, y));
        }
        public Field addY(final int dy) {
            return setY(_position._y + dy);
        }

        /**
         * 文字ポイントをセットしたフィールドインスタンスを得る
         * @param charPoint10 文字ポイント(×10)
         * @return 文字ポイントをセットしたフィールドインスタンス
         */
        public Field setCharPoint10(final int charPoint10) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }

        /**
         * フィールド長をセットしたフィールドインスタンスを得る
         * @param fieldLength フィールド長
         * @return フィールド長をセットしたフィールドインスタンス
         */
        public Field setFieldLength(final int fieldLength) {
            return new Field(_no, _fieldname, _font, fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setEndX(final int endX) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        /**
         * 編集式をセットしたフィールドインスタンスを得る
         * @param henshuShiki 編集式
         * @return 編集式をセットしたフィールドインスタンス
         */
        public Field setHenshuShiki(final String henshuShiki) {
            final String editEqn = henshuShiki;
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setPrintMethod(final PrintMethod printMethod) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, printMethod, _comment, _editEqn, _hanzen, _mask , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        /**
         * 明朝ゴシックをセットしたフィールドインスタンスを得る
         * @param font (0:明朝 1:ゴシック)
         * @return 明朝ゴシックをセットしたフィールドインスタンス
         */
        public Field setFont(final Font font) {
            return new Field(_no, _fieldname, font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setHankakuZenkaku(final int hanzen) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, hanzen, _mask , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setLinkFieldname(final String linkFieldname) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask , _repeatConfig, linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public boolean isShiromoji() {
            return _mojiShushoku._shiromoji;
        }
        public boolean isOutline() {
            return _mojiShushoku._outline;
        }
        public boolean isBold() {
            return _mojiShushoku._bold;
        }
        public boolean isItalic() {
            return _mojiShushoku._italic;
        }
        public boolean isShadow() {
            return _mojiShushoku._shadow;
        }
        public Field setShiromoji(final boolean shiromoji) {
            return setMojiShushoku(_mojiShushoku.setShiromoji(shiromoji));
        }
        public Field setOutline(final boolean outline) {
            return setMojiShushoku(_mojiShushoku.setOutline(outline));
        }
        public Field setBold(final boolean bold) {
            return setMojiShushoku(_mojiShushoku.setBold(bold));
        }
        public Field setItalic(final boolean italic) {
            return setMojiShushoku(_mojiShushoku.setItalic(italic));
        }
        public Field setShadow(final boolean shadow) {
            return setMojiShushoku(_mojiShushoku.setShadow(shadow));
        }
        private Field setMojiShushoku(final MojiShushoku mojiShushoku) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        /**
         * 縦倍をセットしたフィールドインスタンスを得る<br>
         * ※分子、分母の指定はパターン固定
         * @param bunshi 縦倍分子
         * @param bunbo 縦倍分
         * @return 縦倍をセットしたフィールドインスタンス
         */
        public Field setTatebai(final int bunshi, final int bunbo) {
            return new Field(_no, _fieldname, _font, _fieldLength, new Bunsu(bunshi, bunbo), _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        /**
         * 横倍をセットしたフィールドインスタンスを得る<br>
         * ※分子、分母の指定はパターン固定
         * @param bunshi 横倍分子
         * @param bunbo 横倍分母
         * @return 横倍をセットしたフィールドインスタンス
         */
        public Field setYokobai(final int bunshi, final int bunbo) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, new Bunsu(bunshi, bunbo), _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }

        public Collection<MaskFlag> getMaskEnabled() {
            return MaskFlag.flags(_mask);
        }
        /**
         * マスクをセットしたフィールドインスタンスを得る<br>
         * @param flags
         * @return マスクをセットしたフィールドインスタンス
         */
        public Field setMaskEnabled(final MaskFlag...flags) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, MaskFlag.enabled(_mask, flags), _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        /**
         * マスクをセットしたフィールドインスタンスを得る<br>
         * @param flags
         * @return マスクをセットしたフィールドインスタンス
         */
        public Field setMaskDisabled(final MaskFlag...flags) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, MaskFlag.disabled(_mask, flags) , _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setRepeatConfig(final RepeatConfig repeatConfig) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public Field setDegree(int degree) {
            if (!(0 == degree || 90 == degree || 180 == degree || 270 == degree)) {
                log.warn(" invalid degree : " + degree + ", set degree 0.");
                degree = 0;
            }
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, degree, _dataType, _flg2, _flg3);
        }
        public Field setDataNumType(final boolean isNumType) {
            return new Field(_no, _fieldname, _font, _fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, _hanzen, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, isNumType ? "1" : datatypeDefault, _flg2, _flg3);
        }
        public Field setZenkaku(final boolean zenkaku) {
            int fieldLength = _fieldLength;
            if (!isZenkaku() && zenkaku) {
                fieldLength = _fieldLength * 2;
            } else if (isZenkaku() && !zenkaku) {
                fieldLength = _fieldLength / 2;
            }
            return new Field(_no, _fieldname, _font, fieldLength, _tatebai, _yokobai, _endX, _directionIsVertical, _position, _charPoint10
                    , _mojiShushoku, _printMethod, _comment, _editEqn, zenkaku ? 1 : 0, _mask, _repeatConfig, _linkFieldname
                    , _calcFormula, _strStr, _strCpy, _degree, _dataType, _flg2, _flg3);
        }
        public boolean isZenkaku() {
            return 1 == _hanzen;
        }
        public Point getPoint() {
            return _position;
        }
        public boolean equals(final Object o) {
            if (o instanceof Field) {
                final Field oe = (Field) o;
                return _no.equals(oe._no) && _fieldname.equals(oe._fieldname) && _fieldLength == oe._fieldLength && _endX == oe._endX && _directionIsVertical == oe._directionIsVertical && _position.equals(oe._position) && _charPoint10 == oe._charPoint10;
            }
            return false;
        }
        public String toSvfString() {
            return " Field  " + _no + " " + quote(_fieldname)  + "" + StringUtils.repeat(" ", 18) + _font._val + "  " + _fieldLength
                    + " " + _tatebai._bunshi + " " + _tatebai._bunbo + " " + _yokobai._bunshi + " " + _yokobai._bunbo + "  " + _endX + " " + (_directionIsVertical ? "1" : "0") + " " + _position._x + " " + _position._y + " " + _charPoint10
                    + " 0 0 " + (_mojiShushoku.isShiromoji() ? "100" : "0") + " " + (_mojiShushoku.isBold() ? "1" : "0") + " " + (_mojiShushoku.isItalic() ? "1" : "0") + " " + (_mojiShushoku.isOutline() ? "1" : "0") + " " + (_mojiShushoku.isShadow() ? "1" : "0") + " 0 0 0 " + _printMethod._value + " " + quote(_editEqn) + " " + quote(_calcFormula) + " " + quote(_strStr) + " " + quote(_strCpy) // 14~28
                    + " -1 " + _degree + " " + _dataType + " " + _repeatConfig._repeatNo + " " + _repeatConfig._repeatCount + " " + _repeatConfig._repeatDirection + " " + _repeatConfig._repeatPitch + " " + quote(_linkFieldname) // 29~36
                    + " " + _hanzen + " " + _mskNo + " 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 -1 -1" // 37~60
                    + " " + _repeatConfig._repeatPitchAmari + " " + _inputLock + " " + _hanKind + " " + quote(_linkDelimiter) + " 0 " + _mask + " " + _flg2 + " " + quote(_strRecName) + " " + _flg3 + " " + _dataLength // 61~70
                    + " -1 " + _picthType + " " + _pitchFreeOrFix + " " + quote(_headerRecName) + " " + quote(_comment); // 71~75
        }
        public int compareTo(final Field f) {
            final int n = 999999999;
            if (!_fieldname.equals(f._fieldname)) {
                return (int) Math.signum(_fieldname.compareTo(f._fieldname)) * n;
            }
            if (0 != _fieldLength - f._fieldLength) {
                return (int) Math.signum(_fieldLength - f._fieldLength) * n;
            }
            if (_directionIsVertical != f._directionIsVertical) {
                return (_directionIsVertical ? 1 : -1) * n;
            }
            if (_charPoint10 != f._charPoint10) {
                return (int) Math.signum(_charPoint10 - f._charPoint10) * n;
            }
            if (null == _repeatConfig._repeatNo && null != f._repeatConfig._repeatNo) {
                return n;
            }
            if (null != _repeatConfig._repeatNo && null == f._repeatConfig._repeatNo) {
                return -n;
            }
            int diff = 0;
            if (!_position.equals(f._position)) {
                diff += (int) Math.sqrt(Math.pow(_position._x - f._position._x, 2) + Math.pow(_position._y - f._position._y, 2));
            }
            diff += Math.abs(_endX - f._endX);
            return diff;
        }
        public String toString() {
            return "Field(" + _no + ", " + _fieldname + ", " + _position + ", length = " + _fieldLength + ", endX = " + _endX + ", charPoint10 = " + _charPoint10 + ", editEqn = " + _editEqn + ", comment = " + _comment + ")";
        }

        public enum PrintMethod {
            MUHENSHU("0"),
            MIGITSUME("1"),
            HIDARITSUME("2"),
            CENTERING("3"),
            KINTOUWARI("4"),
            SHOUSUTENICHI_KOTEI_MIGITSUME("5"),
            ;

            final String _value;
            PrintMethod(final String value) {
                _value = value;
            }
            private static PrintMethod getPrintMethod(final String value) {
                for (final PrintMethod m : values()) {
                    if (m._value.equals(value)) {
                        return m;
                    }
                }
                return null;
            }
        }

        public static class RepeatConfig {
            public final String _repeatNo;
            public final int _repeatCount;
            public final int _repeatDirection;
            public final int _repeatPitch;
            public final int _repeatPitchAmari;
            public RepeatConfig(final String repeatNo, final int repeatCount, final int repeatDirection, final int repeatPitch, final int repeatPitchAmari) {
                _repeatNo = repeatNo;
                _repeatCount = repeatCount;
                _repeatDirection = repeatDirection;
                _repeatPitch = repeatPitch;
                _repeatPitchAmari = repeatPitchAmari;
            }
            public RepeatConfig setRepeatCount(final int repeatCount) {
                return new RepeatConfig(_repeatNo, repeatCount, _repeatDirection, _repeatPitch, _repeatPitchAmari);
            }
            public RepeatConfig setRepeatPitchPoint(final double repeatPitchPoint) {
                return new RepeatConfig(_repeatNo, _repeatCount, _repeatDirection, (int) (repeatPitchPoint * 15.69), _repeatPitchAmari);
            }
        }
    }

    public static class SubForm extends Element {
        public final String _name;
        public final Point _point1;
        public final Point _point2;
        public final int _offset; // レコードのオフセット
        public final int _noDataNoPrintFlag; // 1:データがない時、サブフォームを印刷しない
        public final boolean _directionIsVertical; // 0:横、1:縦
        public final String _linkSubform;
        public final FrameLine _waku; // 0:なし、1:四角、2:四辺
        public final int _cornerBits; // 1:左上、2:右上、4:左下、8:右下の合計
        public final int _cornerRadius; // 1以上:コーナースタイル丸
        public final int _cornerCutFlg; // 1:コーナースタイルカット(cornerFlag=1前提) 0:コーナースタイルカット以外
        private final int _frameFixFlg; // 1:レコード数に関わらず、枠の大きさは固定にする
        private final LineOption[] _wakuOptions; // length: 3 * 4 = 12、上[線種、線幅、線幅（数値）]、下[...]、左[...]、右[...]
        private final int _groupSuppressKeyBreakFlg; // 1:印刷モード大計、中計、小計、明細 2:キーフィールドのグループサプレスをキーブレークと連動させないの和

        private static final int offsetDefault = 0;
        private static final int noDataNoPrintFlgDefault = 0;
        private static final String linkSubFormDefault = null;
        private static final FrameLine wakuDefault = FrameLine.SQUARE;
        private static final int frameFixFlgDefault = 0;
        private static final int cornerBitsDefault = 0;
        private static final int cornerRadiusDefault = 0;
        private static final int cornerCutFlgDefault = 0;
        private static final LineOption[] lineFlagsDefault = {LineOption.of(LineKind.SOLID, LineWidth.MEDIUM_THIN), LineOption._default, LineOption._default, LineOption._default};
        private static final int groupSuppressKeyBreakFlgDefault = 0;

        public SubForm(final String name, final Point point1, final Point point2, final boolean directionIsVertical) {
            this(name, point1, point2, directionIsVertical, offsetDefault, noDataNoPrintFlgDefault, linkSubFormDefault, wakuDefault,
                    cornerBitsDefault, cornerRadiusDefault, cornerCutFlgDefault, frameFixFlgDefault,
                    lineFlagsDefault, groupSuppressKeyBreakFlgDefault
                    );

        }
        private SubForm(final String name, final Point point1, final Point point2, final boolean directionIsVertical, final int offset, final int noDataNoPrintFlag, final String linkSubform, final FrameLine waku
                , final int cornerBits, final int cornerRadius, final int cornerCutFlg, final int frameFixFlg
                , final LineOption[] wakuOptions, final int groupSuppressKeyBreakFlg) {
            _name = name;
            _point1 = point1;
            _point2 = point2;
            _directionIsVertical = directionIsVertical;
            _offset = offset;
            _noDataNoPrintFlag = noDataNoPrintFlag;
            _linkSubform = linkSubform;
            _waku = waku;
            _cornerBits = cornerBits;
            _cornerRadius = cornerRadius;
            _cornerCutFlg = cornerCutFlg;
            _frameFixFlg = frameFixFlg;
            _wakuOptions = wakuOptions;
            _groupSuppressKeyBreakFlg = groupSuppressKeyBreakFlg;
        }
        public SubForm setWaku(final FrameLine waku) {
            final LineOption[] wakuOptions = Arrays.copyOf(_wakuOptions, _wakuOptions.length);
            if (waku == FrameLine.EACH && _waku != FrameLine.EACH) {
                if (_wakuOptions[0]._lineKind == LineKind.NONE) {
                    log.warn(" modify wakuOptions[0] " + _wakuOptions[0] + " => " + LineOption._default);
                    wakuOptions[0] = LineOption._default;
                }
            }
            return new SubForm(_name, _point1, _point2, _directionIsVertical, _offset, _noDataNoPrintFlag, _linkSubform, waku, _cornerBits, _cornerRadius, _cornerCutFlg, _frameFixFlg, wakuOptions, _groupSuppressKeyBreakFlg);
        }
        public SubForm setWakuOptions(final LineOptionIndex lineOptionsIndex, final LineOption lineOption) {
            final LineOption[] wakuOptions = Arrays.copyOf(_wakuOptions, _wakuOptions.length);
            wakuOptions[lineOptionsIndex._idx] = lineOption;
            if (_waku == FrameLine.EACH) {
                if (_wakuOptions[0]._lineKind == LineKind.NONE) {
                    log.warn(" modify wakuOptions[0] " + _wakuOptions[0] + " => " + LineOption._default);
                    wakuOptions[0] = LineOption._default;
                }
            }
            return new SubForm(_name, _point1, _point2, _directionIsVertical, _offset, _noDataNoPrintFlag, _linkSubform, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _frameFixFlg, wakuOptions, _groupSuppressKeyBreakFlg);
        }
        public SubForm setPoint1(final Point point1) {
            return new SubForm(_name, point1, _point2, _directionIsVertical, _offset, _noDataNoPrintFlag, _linkSubform, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _frameFixFlg, _wakuOptions, _groupSuppressKeyBreakFlg);
        }
        public SubForm setPoint2(final Point point2) {
            return new SubForm(_name, _point1, point2, _directionIsVertical, _offset, _noDataNoPrintFlag, _linkSubform, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _frameFixFlg, _wakuOptions, _groupSuppressKeyBreakFlg);
        }
        public SubForm setOffset(final int offset) {
            return new SubForm(_name, _point1, _point2, _directionIsVertical, offset, _noDataNoPrintFlag, _linkSubform, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _frameFixFlg, _wakuOptions, _groupSuppressKeyBreakFlg);
        }
        public SubForm setLinkSubFormname(final String linkSubFormname) {
            return new SubForm(_name, _point1, _point2, _directionIsVertical, _offset, _noDataNoPrintFlag, linkSubFormname, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _frameFixFlg, _wakuOptions, _groupSuppressKeyBreakFlg);
        }
        public SubForm setWidth(final int width) {
            return setPoint2(_point2.setX(_point2._x - getWidth() + width));
        }
        public SubForm setHeight(final int height) {
            return setPoint2(_point2.setY(_point2._y - getHeight() + height));
        }
        public int getWidth() {
            return _point2._x - _point1._x;
        }
        public int getHeight() {
            return _point2._y - _point1._y;
        }
        public String toSvfString() {
            return "SubForm " + quote(_name) + "    " + _point1._x + " " + _point1._y + " " + _point2._x + " " + _point2._y + " " + (_directionIsVertical ? "1" : "0") + " " + _offset + " " + _noDataNoPrintFlag + " " + quote(_linkSubform) + " " + _waku._val
                  + " " + _cornerBits + " " + _cornerRadius + " " + _cornerCutFlg + " " + _frameFixFlg
                  + " " + _wakuOptions[0].toSvfString()
                  + " " + _wakuOptions[1].toSvfString()
                  + " " + _wakuOptions[2].toSvfString()
                  + " " + _wakuOptions[3].toSvfString()
                  + " " + _groupSuppressKeyBreakFlg
                    ;
        }
        public boolean equals(final Object o) {
            if (o instanceof SubForm) {
                final SubForm oe = (SubForm) o;
                return _name.equals(oe._name) && _point1.equals(oe._point1) && _point2.equals(oe._point2) && _directionIsVertical == oe._directionIsVertical;
            }
            return false;
        }
        public String toString() {
            return "SubForm(" + _name + ", " + _point1 + ", " + _point2 + ")";
        }
    }

    public static class Record extends Element {
        public final String _name;
        public final Point _point1; // サブフォーム内相対座標左上
        public final Point _point2; // サブフォーム内相対座標右下
        public final int _recordFlag; // 1:ｻﾌﾞﾌｫｰﾑに入らない場合も印刷する(16or32必須), 2:明細ﾚｺｰﾄﾞ, 4:ﾍｯﾀﾞﾚｺｰﾄﾞ(4必須), 8:明細ｸﾞﾙｰﾌﾟの先頭で(4必須), 16:総計ﾚｺｰﾄﾞ, 32:合計ﾚｺｰﾄﾞ, 64:明細行が1行の場合、(32必須), 128: 全ﾌｨｰﾙﾄﾞが初期値の時、512:ﾍﾟｰｼﾞの先頭で印字
        public final int _amikakePatternFlg; // 1:網掛けﾊﾟﾀｰﾝ有効
        public final int _amikakePattern; // 網掛けﾊﾟﾀｰﾝ
        public final int _amikakeMeido; // 網掛け明度 (amikakePattern=17:ｸﾞﾚｰｽｹｰﾙのみ）
        public final String _amikakeGyouPattern; // 網掛け行 1:すべて、10:奇数行、01:偶数行
        public final FrameLine _waku; // 0:なし、1:四角、2:四辺
        public final int _cornerBits; // 1:左上、2:右上、4:左下、8:右下の合計
        public final int _cornerRadius; // 1以上:コーナースタイル丸
        public final int _cornerCutFlg; // 1:コーナースタイルカット(cornerFlag=1前提) 0:コーナースタイルカット以外
        public final int _w15;
        private final LineOption[] _wakuOptions; // length: 3 * 4 = 12、上[線種、線幅、線幅（数値）]、下[...]、左[...]、右[...]
        private final int _autoLinkFieldPitch10; // 自動ﾘﾝｸﾌｨｰﾙﾄﾞﾋﾟｯﾁ(倍)
        private final int _w29;
        private final int _w30;
        private SubForm _subForm;

        private static final FrameLine wakuDefault = FrameLine.SQUARE;
        private static final LineOption[] wakuOptionsDefault = {LineOption._default, LineOption._default, LineOption._default, LineOption._default};
        private static final int recordFlgDefault = RecordFlag.MEISAI_RECORD._bits;
        private static final int amikakePatternFlgDefault = 0;
        private static final int amikakePatternDefault = 0;
        private static final int amikakeMeidoDefault = 0;
        private static final String _amikakeGyouPatternDefaultALL = "1";
        private static final int cornerBitsDefault = 0;
        private static final int cornerRadiusDefault = 0;
        private static final int cornerCutFlgDefault = 0;
        private static final int autoLinkFieldPitch10Default = 12;
        private static final int w15Default = 0;
        private static final int w29Default = 0;
        private static final int w30Default = 0;

        public static enum RecordFlag {
            SUBFORM_NI_HAIRANAI_BAAIMO_INSATSUSURU(1),
            MEISAI_RECORD(2),
            HEADER_RECORD(4),
            MEISAI_GROUP_NO_SENTOU_DEINSATSU(8),
            SOKEI_RECORD(16),
            GOKEI_RECORD(32),
            MEISAI_GYOU_GA_1GYO_NO_BAAI_INSATSU_SHINAI(64),
            ZEN_FIELD_GA_SHOKITI_NO_TOKI_INJI_SSHINAI(128),
            PAGE_NO_SENTOU_DE_INJI(512);

            final int _bits;
            RecordFlag(final int bit) {
                _bits = bit;
            }
            private static Collection<RecordFlag> flags(final int n) {
                final Set<RecordFlag> flags = new TreeSet();
                for (final RecordFlag flag : RecordFlag.values()) {
                    if ((n & flag._bits) > 0) {
                        flags.add(flag);
                    }
                }
                return flags;
            }
            private static int enabled(int n, final RecordFlag...flags) {
                for (final RecordFlag flag : flags) {
                    n |= flag._bits;
                }
                return n;
            }
            private static int disabled(int n, final RecordFlag...flags) {
                final int src = n;
                for (final RecordFlag flag : flags) {
                    n ^= flag._bits;
                }
                log.info(" " + src + " disabled " + ArrayUtils.toString(flags) + " => " + n);
                return n;
            }
            private static String bits(final int n) {
                final StringBuilder sb = new StringBuilder();
                for (final RecordFlag flag : values()) {
                    sb.append((flag._bits & n) > 0 ? 1 : 0);
                }
                return sb.reverse().toString();
            }
        }

        public Record(final String name, final Point point1, final Point point2) {
            this(name, point1, point2, recordFlgDefault
                    , amikakePatternFlgDefault, amikakePatternDefault, amikakeMeidoDefault, _amikakeGyouPatternDefaultALL, wakuDefault, cornerBitsDefault, cornerRadiusDefault, cornerCutFlgDefault, w15Default,
                    wakuOptionsDefault, autoLinkFieldPitch10Default, w29Default, w30Default
                    );
        }
        private Record(final String name, final Point point1, final Point point2, final int recordFlag,
                final int amikakePatternFlg, final int amikakePattern, final int amikakeMeido, final String amikakeGyouPattern, final FrameLine waku, final int cornerBits, final int cornerRadius, final int cornerCutFlg, final int w15,
                final LineOption[] wakuOptions, final int autoLinkFieldPitch10, final int w29, final int w30
                ) {
            _name = name;
            _point1 = point1;
            _point2 = point2;
            _recordFlag = recordFlag;
            _amikakePatternFlg = amikakePatternFlg;
            _amikakePattern = amikakePattern;
            _amikakeMeido = amikakeMeido;
            _amikakeGyouPattern = amikakeGyouPattern;
            _waku = waku;
            _cornerBits = cornerBits;
            _cornerRadius = cornerRadius;
            _cornerCutFlg = cornerCutFlg;
            _w15 = w15;
            _wakuOptions = wakuOptions;
            _autoLinkFieldPitch10 = autoLinkFieldPitch10;
            _w29 = w29;
            _w30 = w30;
        }
        public Point getAbsPoint1() {
            return _subForm._point1.add(_point1);
        }
        public Point getAbsPoint2() {
            return _subForm._point1.add(_point2);
        }
        public int getWidth() {
            return _point2._x - _point1._x;
        }
        public SubForm getSubForm() {
            return _subForm;
        }
        public void setSubForm(final SubForm subForm) {
            _subForm = subForm;
        }
        public int getHeight() {
            return _point2._y - _point1._y;
        }

        public Record setWakuNone() {
            return new Record(_name, _point1, _point2, _recordFlag,
                    _amikakePatternFlg, _amikakePattern, _amikakeMeido, _amikakeGyouPattern, FrameLine.NONE, _cornerBits, _cornerRadius, _cornerCutFlg, _w15,
                    _wakuOptions, _autoLinkFieldPitch10, _w29, _w30
                    );
        }
        public Record setWakuSquare(final LineOption lineOption) {
            final LineOption[] wakuOptionsCopy = Arrays.copyOf(_wakuOptions, _wakuOptions.length);
            wakuOptionsCopy[LineOptionIndex.TOP._idx] = lineOption;
            return new Record(_name, _point1, _point2, _recordFlag,
                    _amikakePatternFlg, _amikakePattern, _amikakeMeido, _amikakeGyouPattern, FrameLine.SQUARE, _cornerBits, _cornerRadius, _cornerCutFlg, _w15,
                    _wakuOptions, _autoLinkFieldPitch10, _w29, _w30
                    );
        }
        public Record setEachWakuOptions(final LineOptionIndex lineOptionIndex, final LineOption lineOption) {
            final FrameLine waku = FrameLine.EACH;
            final LineOption[] wakuOptionsCopy = Arrays.copyOf(_wakuOptions, _wakuOptions.length);
            wakuOptionsCopy[lineOptionIndex._idx] = lineOption;
            return new Record(_name, _point1, _point2, _recordFlag,
                    _amikakePatternFlg, _amikakePattern, _amikakeMeido, _amikakeGyouPattern, waku, _cornerBits, _cornerRadius, _cornerCutFlg, _w15,
                    wakuOptionsCopy, _autoLinkFieldPitch10, _w29, _w30
                    );
        }
        public Record setRecordFlagEnabled(final RecordFlag...flags) {
            return new Record(_name, _point1, _point2, RecordFlag.enabled(_recordFlag, flags),
                    _amikakePatternFlg, _amikakePattern, _amikakeMeido, _amikakeGyouPattern, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _w15,
                    _wakuOptions, _autoLinkFieldPitch10, _w29, _w30
                    );
        }
        public Record setRecordFlagDisabled(final RecordFlag...flags) {
            return new Record(_name, _point1, _point2, RecordFlag.disabled(_recordFlag, flags),
                    _amikakePatternFlg, _amikakePattern, _amikakeMeido, _amikakeGyouPattern, _waku, _cornerBits, _cornerRadius, _cornerCutFlg, _w15,
                    _wakuOptions, _autoLinkFieldPitch10, _w29, _w30
                    );
        }
        public String toSvfString() {
            return "Record  " + quote(_name) + "    " + _point1._x + " " + _point1._y + " " + _point2._x + " " + _point2._y + " " + _recordFlag + " " + _amikakePatternFlg + " " + _amikakePattern + " " + _amikakeMeido + " " + _amikakeGyouPattern + " " + _waku._val + " " + _cornerBits
                  + " " + _cornerRadius + " " + _cornerCutFlg + " " + _w15
                  + " " + _wakuOptions[0].toSvfString()
                  + " " + _wakuOptions[1].toSvfString()
                  + " " + _wakuOptions[2].toSvfString()
                  + " " + _wakuOptions[3].toSvfString()
                  + " " + _autoLinkFieldPitch10 + " " + _w29 + " " + _w30
                    ;
        }
        public boolean equals(final Object o) {
            if (o instanceof Record) {
                final Record oe = (Record) o;
                return _name.equals(oe._name) && _point1.equals(oe._point1) && _point2.equals(oe._point2);
            }
            return false;
        }
        public String toString() {
            return "Record(" + _name + ", " + _point1 + ", " + _point2 + ", subForm = " + (null == _subForm ? null : _subForm._name) + ", mask = " + _recordFlag + ")";
        }
    }

    public static class Repeat extends Element {
        public final String _no;
        public final int _left;
        public final int _top;
        public final int _right;
        public final int _bottom;
        public final int _direction; // 0:縦、1:横
        public final int _count;
        public final int _pitch;
        public final int _mod1;
        public final String _page;

        public Repeat(final String no, final int left, final int top, final int right, final int bottom, final int direction, final int count, final int pitch, final int mod1, final String page) {
            _no = no;
            _left = left;
            _top = top;
            _right = right;
            _bottom = bottom;
            _direction = direction;
            _count = count;
            _pitch = pitch;
            _mod1 = mod1;
            _page = page;
        }
        public String toSvfString() {
            return " Repeat  " + _no + " " + _left + " " + _top + " " + _right + " " + _bottom + " " + _direction + " " + _count + " " + _pitch + " " + _mod1 + " " + quote(_page) + " ";
        }
        public boolean equals(final Object o) {
            if (o instanceof Repeat) {
                final Repeat oe = (Repeat) o;
                return _no.equals(oe._no) && _direction == oe._direction && _count == oe._count;
            }
            return false;
        }
        public Repeat setX(final int x) {
            return new Repeat(_no, x, _top, x + (_right - _left), _bottom, _direction, _count, _pitch, _mod1, _page);
        }
        public Repeat setY(final int y) {
            return new Repeat(_no, _left, y, _right, y + (_bottom - _top), _direction, _count, _pitch, _mod1, _page);
        }
        public Repeat addX(final int x) {
            return setX(_left + x);
        }
        public Repeat addY(final int y) {
            return setY(_top + y);
        }
        public Repeat setCount(final int count) {
            return new Repeat(_no, _left, _top, _right, _bottom, _direction, count, _pitch, _mod1, _page);
        }
        public Repeat setPitch(final int pitch) {
            return new Repeat(_no, _left, _top, _right, _bottom, _direction, _count, pitch, _mod1, _page);
        }
        public String toString() {
            return "Repeat(" + _no + ", " + _direction + ", " + _count + ")";
        }
    }

    /**
     * 文字サイズをピクセルに変換した値を得る
     * @param charPoint 文字サイズ
     * @return 文字サイズをピクセルに変換した値
     */
    public static double charPointToPixel(final double charPoint) {
        return charPoint * dpi / pointPerInch;
    }

//    private Point getPageBaseInfo(final List fileContents) {
//        final Perl5Util util = new Perl5Util();
//        Point injiIchiCyousei = null;
//    	boolean isPageKihonTeigi = false;
//    	for (final Iterator it = fileContents.iterator(); it.hasNext();) {
//    		String contents = (String) it.next();
//    		if (isPageKihonTeigi) {
//    			if (contents.startsWith(";")) {
//    				//log.info(" comment : " + contents);
//    				continue;
//    			} else if (contents.startsWith("Page")) {
//                    contents = util.substitute("s/\\s+/ /g", contents); // 2つ以上のスペースを1つに
//                    final String[] arr = splitBySpace(contents);
//                    injiIchiCyousei = new Point(Integer.parseInt(arr[5]), Integer.parseInt(arr[6]));
//    			} else {
//    				break;
//    			}
//    		} else if (contents.startsWith(";********** ページ基本定義 **********")) {
//    			//log.info(" comment : " + contents);
//    			isPageKihonTeigi = true;
//    		}
//    	}
//    	return injiIchiCyousei;
//	}
//
//    private List getLineInfoFromBox() throws MalformedPatternException {
//    	final List fileContents = _contents;
//    	final List lineInfoList = new ArrayList();
//    	final Perl5Util util = new Perl5Util();
//    	boolean isTeigi = false;
//    	for (final Iterator it = fileContents.iterator(); it.hasNext();) {
//    		String contents = (String) it.next();
//    		if (isTeigi) {
//    			if (contents.startsWith(";")) {
//    				//log.info(" comment : " + contents);
//    				continue;
//    			} else if (contents.startsWith(" Box")) {
//                    contents = util.substitute("s/\\s+/ /g", contents); // 2つ以上のスペースを1つに
//                    final String[] arr = splitBySpace(contents);
//                    //log.info(ArrayUtils.toString(arr));
//                    final Point upperLeft = new Point(Integer.parseInt(arr[6]), Integer.parseInt(arr[7]));
//                    final Point lowerRight = new Point(Integer.parseInt(arr[8]), Integer.parseInt(arr[9]));
//                    final Point upperRight = new Point(lowerRight._x, upperLeft._y);
//                    final Point lowerLeft = new Point(upperLeft._x, lowerRight._y);
//                    lineInfoList.add(new Line(upperLeft, upperRight)); // 上辺
//                    lineInfoList.add(new Line(upperRight, lowerRight)); // 右辺
//                    lineInfoList.add(new Line(lowerLeft, lowerRight)); // 下辺
//                    lineInfoList.add(new Line(upperLeft, lowerLeft)); // 左辺
//    			} else {
//    				break;
//    			}
//    		} else if (contents.startsWith(BOX_NO_TEIGI)) {
//    			//log.info(" comment : " + contents);
//    			isTeigi = true;
//    		}
//    	}
//    	return lineInfoList;
//	}

    public static class YxComparator implements Comparator<Element> {
        public int compare(Element e1, Element e2) {
            if (e1 instanceof Field && e2 instanceof Field) {
                final Field f1 = (Field) e1;
                final Field f2 = (Field) e2;
                int rtn;
                rtn = f1._position._y - f2._position._y;
                if (0 != rtn) return rtn;
                rtn = f1._position._x - f2._position._x;
                if (0 != rtn) return rtn;
                rtn = f1._fieldname.compareTo(f2._fieldname);
                return rtn;
            } else if (e1 instanceof Line && e2 instanceof Line) {
                final Line l1 = (Line) e1;
                final Line l2 = (Line) e2;
                int rtn;
                rtn = l1._start._y - l2._start._y;
                if (0 != rtn) return rtn;
                rtn = l1._start._x - l2._start._x;
                if (0 != rtn) return rtn;
                rtn = l1._end._y - l2._end._y;
                if (0 != rtn) return rtn;
                rtn = l1._end._x - l2._end._x;
                if (0 != rtn) return rtn;
                rtn = l1._lineWidth._idx - l2._lineWidth._idx;
                return rtn;
            } else if (e1 instanceof KoteiMoji && e2 instanceof KoteiMoji) {
                final KoteiMoji m1 = (KoteiMoji) e1;
                final KoteiMoji m2 = (KoteiMoji) e2;
                int rtn;
                rtn = m1._point._y - m2._point._y;
                if (0 != rtn) return rtn;
                rtn = m1._point._x - m2._point._x;
                if (0 != rtn) return rtn;
                rtn = m1._endX - m2._endX;
                if (0 != rtn) return rtn;
                rtn = m1._moji.compareTo(m2._moji);
                return rtn;
            } else if (e1 instanceof Box && e2 instanceof Box) {
                final Box m1 = (Box) e1;
                final Box m2 = (Box) e2;
                int rtn;
                rtn = m1._upperLeft._y - m2._upperLeft._y;
                if (0 != rtn) return rtn;
                rtn = m1._upperLeft._x - m2._upperLeft._x;
                if (0 != rtn) return rtn;
                rtn = m1._lowerRight._y - m2._lowerRight._y;
                if (0 != rtn) return rtn;
                rtn = m1._lowerRight._x - m2._lowerRight._x;
                if (0 != rtn) return rtn;
                return rtn;
            } else if (e1 instanceof Repeat && e2 instanceof Repeat) {
                final Repeat m1 = (Repeat) e1;
                final Repeat m2 = (Repeat) e2;
                int rtn;
                rtn = m1._top - m2._top;
                if (0 != rtn) return rtn;
                rtn = m1._left - m2._left;
                return rtn;
            } else if (e1 instanceof SubForm && e2 instanceof SubForm) {
                final SubForm m1 = (SubForm) e1;
                final SubForm m2 = (SubForm) e2;
                int rtn;
                rtn = m1._point1._y - m2._point1._y;
                if (0 != rtn) return rtn;
                rtn = m1._point1._x - m2._point1._x;
                return rtn;
            } else if (e1 instanceof KoteiMoji && e2 instanceof KoteiMoji) {
                final KoteiMoji m1 = (KoteiMoji) e1;
                final KoteiMoji m2 = (KoteiMoji) e2;
                int rtn;
                rtn = m1._point._y - m2._point._y;
                return rtn;
            }
            return 0;
        }
    }

    private static <T, U, S> Map<U, S> getMappedMap(final Map<T, Map<U, S>> map, final T key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<U, S>());
        }
        return map.get(key1);
    }

    private static <T, U> List<T> getMappedList(final Map<U, List<T>> map, final U key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<T>());
        }
        return map.get(key1);
    }

    private <T extends Element> List<T> getContentAddList(final Class<T> c) {
        return getContentAddRemoveList(c, AddRemove.ADD);
    }

    private <T extends Element> List<T> getContentRemoveList(final Class<T> c) {
        return getContentAddRemoveList(c, AddRemove.REMOVE);
    }

    private Line getNearestLine(final Point p, final LinePosition linePosition) {
        Line leftLine = null;
        Line rightLine = null;
        Line upperLine = null;
        Line lowerLine = null;
        for (final ContentsLine cl : _contents) {
            if (null != cl._line) {
                final Line line = cl._line;
                if (line.isVertical()) {
                    if (line._start._y <= p._y && p._y <= line._end._y) {
                        if (line._start._x < p._x && (null == leftLine || Math.abs(p._x - leftLine._start._x) > Math.abs(p._x - line._start._x))) {
                            leftLine = line;
                        } else if (line._start._x > p._x && (null == rightLine || Math.abs(rightLine._start._x - p._x) > Math.abs(line._start._x - p._x))) {
                            rightLine = line;
                        }
                    }
                } else if (line.isHorizontal()) {
                    if (line._start._x <= p._x && p._x <= line._end._x) {
                        if (line._start._y < p._y && (null == upperLine || Math.abs(p._y - upperLine._start._y) > Math.abs(p._y - line._start._y))) {
                            upperLine = line;
                        } else if (line._start._y > p._y && (null == lowerLine || Math.abs(lowerLine._start._y - p._y) > Math.abs(line._start._y - p._y))) {
                            lowerLine = line;
                        }
                    }
                }
//    		} else if (null != cl._box || null != cl._record && null != cl._record._subForm || null != cl._subForm) {
            } else if (null != cl._box) {
                final List<Line> lines;
//    			if (null != cl._box) {
                    final Box b = cl._box;
                    final Point p1 = b._upperLeft, p2 = new Point(b._lowerRight._x, b._upperLeft._y), p3 = b._lowerRight, p4 = new Point(b._upperLeft._x, b._lowerRight._y);
                    lines = Arrays.asList(new Line(b._lineWidth, p1, p2), new Line(b._lineWidth, p2, p3), new Line(b._lineWidth, p1, p4), new Line(b._lineWidth, p4, p3));
//    			} else if (null != cl._record && null!= cl._record._subForm) {
//    				final Record rec = cl._record;
//        			final Point p1 = rec._point1, p2 = new Point(rec._point1._x, rec._point2._y), p3 = rec._point2, p4 = new Point(rec._point2._x, rec._point1._y);
//        			lines = Arrays.asList(new Line(1, p1, p2), new Line(1, p2, p3), new Line(1, p1, p4), new Line(1, p4, p3));
//    			} else {
//        			final SubForm sf = cl._subForm;
//        			final Point p1 = sf._point1, p2 = new Point(sf._point1._x, sf._point2._y), p3 = sf._point2, p4 = new Point(sf._point2._x, sf._point1._y);
//        			lines = Arrays.asList(new Line(1, p1, p2), new Line(1, p2, p3), new Line(1, p1, p4), new Line(1, p4, p3));
//    			}
                for (final Line line : lines) {
                    if (line.isVertical()) {
                        if (line._start._y <= p._y && p._y <= line._end._y) {
                            if (line._start._x < p._x && (null == leftLine || Math.abs(p._x - leftLine._start._x) > Math.abs(p._x - line._start._x))) {
                                leftLine = line;
                            } else if (line._start._x > p._x && (null == rightLine || Math.abs(rightLine._start._x - p._x) > Math.abs(line._start._x - p._x))) {
                                rightLine = line;
                            }
                        }
                    } else if (line.isHorizontal()) {
                        if (line._start._x <= p._x && p._x <= line._end._x) {
                            if (line._start._y < p._y && (null == upperLine || Math.abs(p._y - upperLine._start._y) > Math.abs(p._y - line._start._y))) {
                                upperLine = line;
                            } else if (line._start._y > p._y && (null == lowerLine || Math.abs(lowerLine._start._y - p._y) > Math.abs(line._start._y - p._y))) {
                                lowerLine = line;
                            }
                        }
                    }
                }
            }
        }
        if (linePosition == LinePosition.UPPER) {
            return upperLine;
        } else if (linePosition == LinePosition.LOWER) {
            return lowerLine;
        } else if (linePosition == LinePosition.RIGHT) {
            return rightLine;
        } else if (linePosition == LinePosition.LEFT) {
            return leftLine;
        }
        return null;
    }

    public Line getNearestUpperLine(final Point p) {
        return getNearestLine(p, LinePosition.UPPER);
    }

    public Line getNearestLowerLine(final Point p) {
        return getNearestLine(p, LinePosition.LOWER);
    }

    public Line getNearestRightLine(final Point p) {
        return getNearestLine(p, LinePosition.RIGHT);
    }

    public Line getNearestLeftLine(final Point p) {
        return getNearestLine(p, LinePosition.LEFT);
    }

    private static boolean inRange(final int v, final int v1, final int v2) {
        return v1 <= v && v <= v2;
    }

    public List<Line> getCrossedLineList(final Line line) {
        final List<Line> list = new ArrayList<Line>();
        for (final Line lineC : getElementList(Line.class)) {
            if (line.isHorizontal()) {
                if (lineC.isVertical() && inRange(lineC._start._x, line._start._x, line._end._x) && inRange(line._start._y, lineC._start._y, lineC._end._y)) {
                    list.add(lineC);
                }
            } else if (line.isVertical()) {
                if (lineC.isHorizontal() && inRange(lineC._start._y, line._start._y, line._end._y) && inRange(line._start._x, lineC._start._x, lineC._end._x)) {
                    list.add(lineC);
                }
            }
        }
        return list;
    }

    public List<KoteiMoji> getKoteiMojiListWithRegex(final String regex) {
        final List<KoteiMoji> rtn = new ArrayList<KoteiMoji>();
        for (final KoteiMoji km : getElementList(KoteiMoji.class)) {
            if (Pattern.matches("\"" + regex + "\"", km._moji)) {
                rtn.add(km);
            }
        }
        return rtn;
    }

    public List<KoteiMoji> getKoteiMojiListWithText(final String moji) {
        final List<KoteiMoji> rtn = new ArrayList<KoteiMoji>();
        for (final KoteiMoji km : getElementList(KoteiMoji.class)) {
            if (km._moji.equals("\"" + moji + "\"")) {
                rtn.add(km);
            }
        }
        return rtn;
    }

    private <T extends Element> void removeContents(final Class<T> c, final T o) {
        final int idx = getElementList(c).indexOf(o);
        if (-1 == idx) {
            log.warn(" not found " + c + " : " + o);
        } else {
            getContentRemoveList(c).add(o);
        }
    }

    public void addLine(final Line line) {
        if (_debug) {
            log.info(" add line " + line);
        }
        getContentAddList(Line.class).add(line);
    }

    public void removeLine(final Line line) {
        if (_debug) {
            log.info(" remove line " + line);
        }
        removeContents(Line.class, line);
    }

    public void move(final Line from, final Line to) {
        if (_debug) {
            log.info(" move " + from + " to " + to);
        }
        removeLine(from);
        addLine(to);
    }

    public void addField(final Field field) {
        if (_debug) {
            log.info(" add field " + field);
        }
        getContentAddList(Field.class).add(field);
    }

    public void removeField(final Field field) {
        if (_debug) {
            log.info(" remove field " + field);
        }
        removeContents(Field.class, field);
    }

    public void addKoteiMoji(final KoteiMoji koteiMoji) {
        if (_debug) {
            log.info(" add  koteiMoji " + koteiMoji);
        }
        getContentAddList(KoteiMoji.class).add(koteiMoji);
    }

    public void removeKoteiMoji(final KoteiMoji koteiMoji) {
        if (_debug) {
            log.info(" remove  koteiMoji " + koteiMoji);
        }
        removeContents(KoteiMoji.class, koteiMoji);
    }

    public void move(final KoteiMoji from, final KoteiMoji to) {
        if (_debug) {
            log.info(" move " + from + " to " + to);
        }
        removeKoteiMoji(from);
        addKoteiMoji(to);
    }

    public ImageField getImageField(final String fieldname) {
        ImageField imageField = null;
        for (final ImageField image : getElementList(ImageField.class)) {
            if (image._fieldname.equals(fieldname)) {
                imageField = image;
                break;
            }
        }
        if (null == imageField) {
            log.warn(" no such field : " + fieldname);
        }
        return imageField;
    }

    public void addImageField(final ImageField image) {
        if (_debug) {
            log.info(" add image " + image);
        }
        getContentAddList(ImageField.class).add(image);
    }

    public void removeImageField(final ImageField image) {
        if (_debug) {
            log.info(" remove image " + image);
        }
        removeContents(ImageField.class, image);
    }

    public void move(final ImageField from, final ImageField to) {
        if (_debug) {
            log.info(" move " + from + " to " + to);
        }
        removeImageField(from);
        addImageField(to);
    }

    public void addBox(final Box box) {
        if (_debug) {
            log.info(" add box " + box);
        }
        getContentAddList(Box.class).add(box);
    }

    public void removeBox(final Box box) {
        if (_debug) {
            log.info(" remove box " + box);
        }
        removeContents(Box.class, box);
    }

    public void move(final Box from, final Box to) {
        if (_debug) {
            log.info(" move " + from + " to " + to);
        }
        removeBox(from);
        addBox(to);
    }

    public Field getField(final String fieldname) {
        Field field = null;
        for (final Field f : getElementList(Field.class)) {
            if (f._fieldname.equals(fieldname)) {
                field = f;
                break;
            }
        }
        if (null == field) {
            log.warn(" no such field : " + fieldname);
        }
        return field;
    }

    public Map<String, Field> getFieldNameMap() {
        final Map<String, Field> map = new TreeMap<String, Field>();
        for (final Field f : getElementList(Field.class)) {
            map.put(f._fieldname, f);
        }
        return map;
    }

    public SubForm getSubForm(final String subFormName) {
        SubForm subForm = null;
        for (final SubForm f : getElementList(SubForm.class)) {
            if (f._name.equals(subFormName)) {
                subForm = f;
                break;
            }
        }
        if (null == subForm) {
            log.warn(" no such subform : " + subFormName);
        }
        return subForm;
    }

    public void addSubForm(final SubForm subForm) {
        if (_debug) {
            log.info(" add subForm " + subForm);
        }
        getContentAddList(SubForm.class).add(subForm);
    }

    public void removeSubForm(final SubForm subForm) {
        if (_debug) {
            log.info(" remove subForm " + subForm);
        }
        removeContents(SubForm.class, subForm);
        for (final Record r : getElementList(Record.class)) {
            if (r._subForm == subForm) {
                removeContents(Record.class, r);
            }
        }
    }

    public Record getRecord(final String recordName) {
        Record record = null;
        for (final Record r : getElementList(Record.class)) {
            if (r._name.equals(recordName)) {
                record = r;
                break;
            }
        }
        if (null == record) {
            log.warn(" no such record : " + recordName);
        }
        return record;
    }

    public Record getRecordOfField(final Field field) {
        if (null == field) {
            return null;
        }
        for (final Record record : getElementList(Record.class)) {
            final boolean containsField = between(field._position._x, record.getAbsPoint1()._x, record.getAbsPoint2()._x) && between(field._position._y, record.getAbsPoint1()._y, record.getAbsPoint2()._y);
            if (containsField) {
                return record;
            }
        }
        return null;
    }

    public List<Record> getAddRemoveSubFormRecordList(final SubForm subForm) {
        final List<Record> list = new ArrayList<Record>();
        for (final Record r : getAddRemoveResultList(Record.class)) {
            if (r._subForm == subForm) {
                list.add(r);
            }
        }
        return list;
    }

    public void addSubFormRecords(final SubForm taregetSubForm, final Record ...records) {
        for (final Record record : records) {
            record.setSubForm(taregetSubForm);
            getContentAddList(Record.class).add(record);
        }
    }

    public void removeRecord(final Record record) {
        record.setSubForm(null);
        removeContents(Record.class, record);
    }

    public Repeat getRepeat(final String repeatNo) {
        Repeat repeat = null;
        for (final Repeat r : getElementList(Repeat.class)) {
            if (r._no.equals(repeatNo)) {
                repeat = r;
                break;
            }
        }
        if (null == repeat) {
            log.warn(" no such repeat : " + repeatNo);
        }
        return repeat;
    }

    public void addRepeat(final Repeat repeat) {
        getContentAddList(Repeat.class).add(repeat);
    }

    public void removeRepeat(final Repeat repeat) {
        removeContents(Repeat.class, repeat);
    }

    public <T extends Element> List<T> getElementList(final Class<T> clazz) {
        final List<T> list = new ArrayList<T>();
        for (final ContentsLine cl : _contents) {
            T t = cl.get(clazz);
            if (null != t) {
                list.add(t);
            }
        }
        return list;
    }

    public List<Element> getAllElementList() {
        final List<Element> list = new ArrayList<Element>();
        for (final ContentsLine cl : _contents) {
            Element e = cl.get();
            if (null != e) {
                list.add(e);
            }
        }
        return list;
    }

    private void addElement(final Element e) {
        if (e instanceof Field) {
            addField((Field) e);
        } else if (e instanceof Line) {
            addLine((Line) e);
        } else if (e instanceof KoteiMoji) {
            addKoteiMoji((KoteiMoji) e);
        } else if (e instanceof ImageField) {
            addImageField((ImageField) e);
        } else if (e instanceof Box) {
            addBox((Box) e);
        } else if (e instanceof SubForm) {
            addSubForm((SubForm) e);
        } else if (e instanceof Record) {
            log.warn("use addRecord(SubForm subForm, Record ... recordList) instead.");
        } else {
            log.warn(" no method to add element : " + e);
        }
    }

    public void removeElement(final Element e) {
        if (e instanceof Field) {
            removeField((Field) e);
        } else if (e instanceof Line) {
            removeLine((Line) e);
        } else if (e instanceof KoteiMoji) {
            removeKoteiMoji((KoteiMoji) e);
        } else if (e instanceof ImageField) {
            removeImageField((ImageField) e);
        } else if (e instanceof Box) {
            removeBox((Box) e);
        } else if (e instanceof SubForm) {
            removeSubForm((SubForm) e);
        } else if (e instanceof Record) {
            removeRecord((Record) e);
        } else {
            log.warn(" no method to add element : " + e);
        }
    }

    public void addAllElement(final List<Element> elements) {
        for (final Element e : elements) {
            addElement(e);
        }
    }

    private static boolean between(final double v, final double b1, final double b2) {
        if (b2 <= b1) {
            return b2 <= v && v <= b1;
        }
        return b1 <= v && v <= b2;
    }

    public static Map<String, List<Element>> diff(final SvfForm svfForm1, final SvfForm svfForm2) {
        final String SAME = "SAME";
        final String ONLY1 = "ONLY1";
        final String ONLY2 = "ONLY2";
        final String SMALLDIFF1 = "SMALLDIFF1";
        final String SMALLDIFF2 = "SMALLDIFF2";
        final Map<String, List<Element>> diffs = new TreeMap<String, List<Element>>();
        diffs.put(SAME, new ArrayList<Element>());
        diffs.put(ONLY1, new ArrayList<Element>());
        diffs.put(ONLY2, new ArrayList<Element>());
        final List<Element> cls1 = new ArrayList<Element>();
        final List<Element> cls2 = new ArrayList<Element>();
        for (final ContentsLine e : svfForm1._contents) {
            if (null != e.get()) {
                cls1.add(e.get());
            }
        }
        for (final ContentsLine e : svfForm2._contents) {
            if (null != e.get()) {
                cls2.add(e.get());
            }
        }
        for (final Iterator<Element> it1 = cls1.iterator(); it1.hasNext();) {
            final Element cl1 = it1.next();
            if (cls2.contains(cl1)) {
                diffs.get(SAME).add(cl1);
                cls2.remove(cl1);
            } else {
                diffs.get(ONLY1).add(cl1);
            }
            it1.remove();
        }
        diffs.get(ONLY2).addAll(cls2);
        final List<List<Element>> smallDiffs = smallDiffs(diffs.get(ONLY1), diffs.get(ONLY2));
        diffs.put(SMALLDIFF1, smallDiffs.get(0));
        diffs.get(ONLY1).removeAll(diffs.get(SMALLDIFF1));
        diffs.put(SMALLDIFF2, smallDiffs.get(1));
        diffs.get(ONLY2).removeAll(diffs.get(SMALLDIFF2));
        return diffs;
    }

    private static List<List<Element>> smallDiffs(final Collection<Element> cs1_, final Collection<Element> cs2_) {
        final Class[] cs = {Point.class, Line.class, Box.class, KoteiMoji.class, ImageField.class, Field.class, SubForm.class, Record.class, Repeat.class};
        final Map<Class, Map<String, List<Element>>> grouped = new HashMap();
        for (final Element e : cs1_) {
            for (final Class c : cs) {
                if (null != cast(c, e)) {
                    getMappedList(getMappedMap(grouped, c), "1").add(e);
                    break;
                }
            }
        }
        for (final Element e : cs2_) {
            for (final Class c : cs) {
                if (null != cast(c, e)) {
                    getMappedList(getMappedMap(grouped, c), "2").add(e);
                    break;
                }
            }
        }

        final List<Element> smallDiff1s = new ArrayList<Element>();
        final List<Element> smallDiff2s = new ArrayList<Element>();

        for (final Map.Entry<Class, Map<String, List<Element>>> e : grouped.entrySet()) {
            final Class c = e.getKey();
            final List<Element> cs1 = getMappedList(e.getValue(), "1");
            final List<Element> cs2 = getMappedList(e.getValue(), "2");
            final List<Element> csmallDiff1s = new ArrayList<Element>();
            final List<Element> csmallDiff2s = new ArrayList<Element>();
            try {
                for (final Iterator<Element> it1 = cs1.iterator(); it1.hasNext();) {
                    final Element e1 = it1.next();
                    Element diffE2 = null;
                    for (final Iterator<Element> it2 = cs2.iterator(); it2.hasNext();) {
                        final Element e2 = it2.next();
                        if (castDiff(c, e1, e2)) {
                            diffE2 = e2;
                            break;
                        }
                    }
                    if (null != diffE2) {
                        csmallDiff1s.add(e1);
                        csmallDiff2s.add(diffE2);
                        it1.remove();
                        cs2.remove(diffE2);
                    }
                }
                for (final Iterator<Element> it2 = cs2.iterator(); it2.hasNext();) {
                    final Element e2 = it2.next();
                    Element diffE1 = null;
                    for (final Iterator<Element> it1 = cs1.iterator(); it1.hasNext();) {
                        final Element e1 = it1.next();
                        if (castDiff(c, e2, e1)) {
                            diffE1 = e1;
                            break;
                        }
                    }
                    if (null != diffE1) {
                        csmallDiff1s.add(diffE1);
                        csmallDiff2s.add(e2);
                        cs1.remove(diffE1);
                        it2.remove();
                    }
                }
            } catch (Exception de) {
                log.error("diff exception!", de);
            }
            log.info(" class " + c.getSimpleName() + " diff1 = " + csmallDiff1s.size() + ", diff2 = " + csmallDiff2s.size() + " / rest size = " + cs1.size() + ", " + cs2.size());
            smallDiff1s.addAll(csmallDiff1s);
            smallDiff2s.addAll(csmallDiff2s);
        }
        log.info(" total diff1 = " + smallDiff1s.size() + ", diff2 = " + smallDiff2s.size() + " / source size = " + cs1_.size() + ", " + cs2_.size());
        return Arrays.asList(smallDiff1s, smallDiff2s);
    }

    private static <C> C cast(final Class<C> c, final Object o) {
        try {
            return c.cast(o);
        } catch (Exception e) {
        }
        return null;
    }

    private static <C extends Comparable<C>> boolean castDiff(final Class<C> c, final Object o1, final Object o2) throws Exception {
        final C c1 = cast(c, o1);
        final C c2 = cast(c, o2);
        if (null != c1 && null != c2) {
            try {
                return Math.abs(c1.compareTo(c2)) <= 5;
            } catch (Exception e) {
                log.error("compare exception!", e);
                throw e;
            }
        }
        return false;
    }

}

// eof

