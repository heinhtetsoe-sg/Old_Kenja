// kanji=漢字
/*
 * $Id: Student.java 74553 2020-05-27 04:44:53Z maeshiro $
 *
 * 作成日: 2004/11/24 18:43:20 - JST
 * 作成者: tamura
 *
 * Copyright(C) 2004 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.common.domain;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.lang.enums.ValuedEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.lang.enums.MyEnum;

/**
 * 生徒。
 * @author tamura
 * @version $Id: Student.java 74553 2020-05-27 04:44:53Z maeshiro $
 */
public final class Student extends MyEnum<String, Student> implements DomainItem {
    private static final Log log = LogFactory.getLog(Student.class);
    private static final Class<Student> MYCLASS = Student.class;

    private final String _inOutCd;
    private final String _nameShow;
    private final String _nameKana;
    private final String _nameEnglish;
    private final Gender _gender;
    private final GrdDiv _grdDiv;
    private final KenjaDateImpl _grdDate;
    private final HomeRoom _homeRoom;
    private final String _attendNo;

    private CourseInfo _courseInfo;

    private final List<Transfer> _transfers = new LinkedList<Transfer>();
    private final List<Transfer> _unModTransfers = Collections.unmodifiableList(_transfers);

    private final String _str;
    private final String _info;

    private boolean _countable;

    /*
     */
    private Student(
            final Category category,
            final String code,
            final String inOutCd,
            final String nameShow,
            final String nameKana,
            final String nameEnglish,
            final Gender gender,
            final GrdDiv grdDiv,
            final KenjaDateImpl grdDate,
            final HomeRoom homeRoom,
            final String attendNo,
            final boolean countable
    ) {
        super(category, Category.NULL == category, code);

        _inOutCd = inOutCd;
        _nameShow = nameShow;
        _nameKana = nameKana;
        _nameEnglish = nameEnglish;
        _gender = gender;
        _grdDate = grdDate;
        _grdDiv = grdDiv;
        _homeRoom = homeRoom;
        _attendNo = attendNo;
        //
        _str = getCode() + ":" + _nameShow;
        _info = _homeRoom.getAbbr() + "-" + _attendNo + " : " + _nameShow;
        //
        _homeRoom.addStudent(this);

        _countable = countable;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final Student that) {
        int rtn = 0;

        rtn = this._homeRoom.compareTo(that._homeRoom);
        if (0 != rtn) { return rtn; }

        rtn = this._attendNo.compareTo(that._attendNo);
        if (0 != rtn) { return rtn; }

        return rtn;
    }

    /**
     * コース情報を設定する。
     * @param courseInfo コース情報
     */
    public void setCourseInfo(final CourseInfo courseInfo) {
        checkAlive();
        _courseInfo = courseInfo;
    }

    /**
     * 生徒のコース情報を得る。
     * @return コース情報
     */
    public CourseInfo getCourseInfo() {
        checkAlive();
        return _courseInfo;
    }

    /**
     * 異動データを追加する。
     * @param transferCd 異動区分
     * @param sdate 異動期間開始日付
     * @param edate 異動期間終了日付
     */
    public void addTransfer(
            final TransferCd transferCd,
            final KenjaDateImpl sdate,
            final KenjaDateImpl edate
    ) {
        checkAlive();
        _transfers.add(new Transfer(transferCd, sdate, edate));
    }

    /**
     * 異動データのリストを得る。
     * @return 異動データのリスト。<code>List&lt;Student.Transfer&gt;</code>
     */
    public List<Transfer> getTransfers() {
        checkAlive();
        return _unModTransfers;
    }

    /**
     * 指定した日付に「在籍」しているか判定する。
     * @param date 日付
     * @return 「在籍」しているなら<code>true</code>を返す
     */
    public boolean isActive(final KenjaDateImpl date) {
        if (GrdDiv.NORMAL != getGrdDiv() && GrdDiv.EXPECTED_TO_GRADUATE != getGrdDiv()) {
            final KenjaDateImpl grdDate = getGrdDate();
            if (null != grdDate && grdDate.compareTo(date) < 0) {
                // 除籍（卒業）
                return false;
            }
        }

        for (final Transfer transfer : _unModTransfers) {
            if (!transfer.isActive(date)) {
                // 異動
                return false;
            }
        }

        // 在籍
        return true;
    }

    /**
     * 除籍（卒業）または移動の文字列を得る。
     * @param date 日付
     * @return 文字列
     */
    public String getActiveString(final KenjaDateImpl date) {
        if (GrdDiv.NORMAL != getGrdDiv() && GrdDiv.EXPECTED_TO_GRADUATE != getGrdDiv()) {
            final KenjaDateImpl grdDate = getGrdDate();
            if (null != grdDate && grdDate.compareTo(date) < 0) {
                // 除籍（卒業）
                return getGrdDiv().toString() + "@" + grdDate.toString();
            }
        }

        for (final Transfer transfer : _unModTransfers) {
            if (!transfer.isActive(date)) {
                // 異動
                return transfer.getActiveString(date);
            }
        }

        // 在籍
        return "在籍";
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        checkAlive();
        return _str;
    }

    /**
     * 生徒の情報文字列を得る。
     * 年組略称+出席番号+氏名表示用
     * @return 生徒の情報文字列
     */
    public String getInfoStudent() {
        checkAlive();
        return _info;
    }

    /**
     * 学籍番号を得る。
     * @return 学籍番号
     */
    public String getCode() {
        final String key = getKey();
        if (key instanceof String) {
            return (String) key;
        }

        log.error("key'" + key + "'がStringではない");
        throw new IllegalStateException("key'" + key + "'がStringではない");
    }

    /**
     * 内外区分を得る。
     * @return 内外区分
     */
    public String getInOutCd() { checkAlive(); return _inOutCd; }

    /**
     * 生徒氏名表示用を得る。
     * @return 生徒氏名表示用
     */
    public String getNameShow() { checkAlive(); return _nameShow; }

    /**
     * 生徒氏名カナを得る。
     * @return 生徒氏名カナ
     */
    public String getNameKana() { checkAlive(); return _nameKana; }

    /**
     * 生徒氏名英字を得る。
     * @return 生徒氏名英字
     */
    public String getNameEnglish() { checkAlive(); return _nameEnglish; }

    /**
     * 性別を得る。
     * @return 性別
     */
    public Gender getGender() { checkAlive(); return _gender; }

    /**
     * 除籍（卒業）区分を得る。
     * @return 除籍（卒業）区分
     */
    public GrdDiv getGrdDiv() { checkAlive(); return _grdDiv; }

    /**
     * 除籍（卒業）日付を得る。
     * @return 除籍（卒業）日付
     */
    public KenjaDateImpl getGrdDate() { checkAlive(); return _grdDate; }

    /**
     * 年組を得る。
     * @return 年組
     */
    public HomeRoom getHomeRoom() { checkAlive(); return _homeRoom; }

    /**
     * 出席番号を得る。
     * @return 出席番号
     */
    public String getAttendNo() { checkAlive(); return _attendNo; }


    /**
     * カウント可能か判定する。
     * @return カウント可能なら<code>true</code>
     */
    public boolean isCountable() { checkAlive(); return _countable; }

    /*
     */
    private static void checkArgs(
            final String code,
            final String inOutCd,
            final String nameShow,
            final String nameKana,
            final String nameEnglish,
            final Gender gender,
            final GrdDiv grdDiv,
            final KenjaDateImpl grdDate,
            final HomeRoom homeRoom,
            final String attendNo
    ) {
        if (null == code)           { throw new IllegalArgumentException("引数が不正(code)"); }
        if (null == inOutCd)        { throw new IllegalArgumentException("引数が不正(inOutCd)"); }
        if (null == nameShow)       { throw new IllegalArgumentException("引数が不正(nameShow)"); }
//        if (null == nameKana)       { throw new IllegalArgumentException("引数が不正(nameKana)"); }
//        if (null == nameEnglish)    { throw new IllegalArgumentException("引数が不正(nameEnglish)"); }
        if (null == gender)         { throw new IllegalArgumentException("引数が不正(gender)"); }
        if (null == grdDiv)         { throw new IllegalArgumentException("引数が不正(grdDiv)"); }
//        if (null == grdDate)        { throw new IllegalArgumentException("引数が不正(grdDate)"); }
        if (null == homeRoom)       { throw new IllegalArgumentException("引数が不正(homeRoom)"); }
        if (null == attendNo)       { throw new IllegalArgumentException("引数が不正(attendNo)"); }
    }

    /**
     * なければ、生徒のインスタンスを作成する。
     * すでに同じ学籍番号のインスタンスがあれば、既存のインスタンスを返す。
     * @param category カテゴリー
     * @param code 学籍番号
     * @param inOutCd 内外区分
     * @param nameShow 生徒氏名表示用
     * @param nameKana 生徒氏名かな
     * @param nameEnglish 生徒氏名英字
     * @param gender 性別
     * @param grdDiv 除籍（卒業）区分
     * @param grdDate 除籍（卒業）日付
     * @param homeRoom 年組
     * @param attendNo 出席番号
     * @param countable カウント可能か否か
     * @return 生徒のインスタンス
     */
    public static Student create(
            final Category category,
            final String code,
            final String inOutCd,
            final String nameShow,
            final String nameKana,
            final String nameEnglish,
            final Gender gender,
            final GrdDiv grdDiv,
            final KenjaDateImpl grdDate,
            final HomeRoom homeRoom,
            final String attendNo,
            final boolean countable
    ) {
        if (null == category)   { throw new IllegalArgumentException("引数が不正(category)"); }
        if (null == code)       { throw new IllegalArgumentException("引数が不正(code)"); }

        final Student found = getInstance(category, code);
        if (null != found) {
            return found;
        }

        checkArgs(code, inOutCd, nameShow, nameKana, nameEnglish, gender, grdDiv, grdDate, homeRoom, attendNo);

        return new Student(category, code, inOutCd, nameShow, nameKana, nameEnglish, gender, grdDiv, grdDate, homeRoom, attendNo, countable);
    }

    /**
     * 学籍番号から生徒のインスタンスを得る。
     * @param category カテゴリー
     * @param code 学籍番号
     * @return 生徒
     */
    public static Student getInstance(
            final Category category,
            final String code
    ) {
        return getEnum(category, MYCLASS, code);
    }

    /**
     * 生徒の列挙のListを得る。
     * @param category カテゴリー
     * @return <code>List&lt;Student&gt;</code>
     */
    public static List<Student> getEnumList(final Category category) {
        return getEnumList(category, MYCLASS);
    }

    /**
     * 生徒の列挙のMapを得る。
     * @param category カテゴリー
     * @return <code>Map&lt;学籍番号, Student&gt;</code>
     */
    public static Map<String, Student> getEnumMap(final Category category) {
        return getEnumMap(category, MYCLASS);
    }

    /**
     * 生徒の数を得る。
     * @param category カテゴリー
     * @return 生徒の数
     */
    public static int size(final Category category) {
        return size(category, MYCLASS);
    }

    /**
     * 生徒の列挙をクリアする。
     * @param category カテゴリー
     */
    public static void clearAll(final Category category) {
        clear(category, MYCLASS);
    }

    /**
     * 指定日付に在籍する全生徒のListを得る。
     * @param category カテゴリー
     * @param date 日付
     * @return 在籍する全生徒のList
     */
    public static List<Student> getActiveList(
            final Category category,
            final KenjaDateImpl date
    ) {
        final List<Student> list = new LinkedList<Student>(getEnumList(category));

        for (final Iterator<Student> it = list.iterator(); it.hasNext();) {
            final Student std = it.next();
            if (!std.isActive(date)) {
                it.remove();
            }
        }

        return list;
    }

    //========================================================================

    /**
     * 異動データ。
     */
    public static class Transfer {
        private final TransferCd _transferCd;
        private final KenjaDateImpl _sdate;
        private final KenjaDateImpl _edate;

        private final String _str;

        /*pkg*/Transfer(
                final TransferCd transferCd,
                final KenjaDateImpl sdate,
                final KenjaDateImpl edate
        ) {
            _transferCd = transferCd;
            _sdate = sdate;
            _edate = edate;

            final StringBuffer sb = new StringBuffer(80);
            sb.append(_transferCd.toString()).append('/');
            sb.append(_sdate.toString());
            if (TransferCd.TRANSFER != _transferCd) {
                sb.append('-');
                sb.append(_edate.toString());
            }
            _str = sb.toString();
        }

        /**
         * 指定した日付に「在籍」しているか判定する。
         * @param date 日付
         * @return 「在籍」しているなら<code>true</code>を返す
         */
        public boolean isActive(final KenjaDateImpl date) {
            if (TransferCd.TRANSFER != _transferCd) {
                // 1.留学,2.休学,3.出停
                // == 開始日...終了日の範囲外ならtrue
                if (date.compareTo(_sdate) < 0 || _edate.compareTo(date) < 0) {
                    // date < _sdate || _edate < date
                    return true;
                }
            } else {
                // 4.編入
                // == 開始日以降ならtrue
                if (_sdate.compareTo(date) <= 0) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 移動の文字列を得る。
         * @param date 日付
         * @return 文字列
         */
        public String getActiveString(final KenjaDateImpl date) {
            if (TransferCd.TRANSFER != _transferCd) {
                // 1.留学,2.休学,3.出停
                // == 開始日...終了日の範囲外ならtrue
                if (date.compareTo(_sdate) < 0 || _edate.compareTo(date) < 0) {
                    return "";
                } else {
                    // date < _sdate || _edate < date
                    return _transferCd.getName() + "@" + _sdate + "〜" + _edate;
                }
            } else {
                // 4.編入
                // == 開始日以降ならtrue
                if (_sdate.compareTo(date) <= 0) {
                    return "";
                } else {
                    return _transferCd.getName() + "@" + _sdate;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _str;
        }

        /**
         * 異動区分を得る。
         * @return 異動区分
         */
        public TransferCd getTransferCd() { return _transferCd; }

        /**
         * 異動期間開始日付を得る。
         * @return 異動期間開始日付
         */
        public KenjaDateImpl getSdate() { return _sdate; }

        /**
         * 異動期間終了日付を得る。
         * @return 異動期間終了日付
         */
        public KenjaDateImpl getEdate() { return _edate; }
    } // Transfer

    //========================================================================

    /**
     * 異動区分。
     */
    public static final class TransferCd extends ValuedEnum {
        /** 1.留学(study abroad) */
        public static final TransferCd STUDY_ABROAD = new TransferCd("留学", 1);
        /** 2.休学(take off school) */
        public static final TransferCd TAKE_OFF_SCHOOL = new TransferCd("休学", 2);
        /** 3.出停(out-of-school suspension) */
        public static final TransferCd OSS = new TransferCd("出停", 3);
        /** 4.編入(transfer) */
        public static final TransferCd TRANSFER = new TransferCd("編入", 4);

        private final String _str;

        /*
         */
        private TransferCd(
                final String name,
                final int value
        ) {
            super(name, value);

            _str = String.valueOf(value) + ":" + name;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() { return _str; }

        /**
         * コードを文字列で得る。
         * @return コード
         */
        public String getCode() {
            return String.valueOf(getValue());
        }

        /**
         * 異動区分から、異動区分のインスタンスを得る。
         * @param value 異動区分
         * @return 異動区分のインスタンス
         */
        public static TransferCd getInstance(final int value) {
            return (TransferCd) EnumUtils.getEnum(TransferCd.class, value);
        }
    } // TransferCd

    //========================================================================

    /**
     * 除籍（卒業）区分。
     */
    public static final class GrdDiv extends ValuedEnum {
        /** ふつう(除籍（卒業）ではない) */
        public static final GrdDiv NORMAL = new GrdDiv("-", 0);
        /** 1.卒業 */
        public static final GrdDiv GRADUATED = new GrdDiv("卒業", 1);
        /** 2.退学 */
        public static final GrdDiv GAVE_UP = new GrdDiv("退学", 2);
        /** 3.転学 */
        public static final GrdDiv TRANSFERRED = new GrdDiv("転学", 3);
        /** 4.卒業見込み */
        public static final GrdDiv EXPECTED_TO_GRADUATE = new GrdDiv("卒業見込み", 4);
        /** 6.除籍 */
        public static final GrdDiv EXPELLED = new GrdDiv("除籍", 6);
        /** 7.転籍 */
        public static final GrdDiv TRANSFERRED2 = new GrdDiv("転籍", 7);

        /*
         */
        private GrdDiv(
                final String name,
                final int value
        ) {
            super(name, value);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() { return getName(); }

        /**
         * コードを文字列で得る。
         * @return コード
         */
        public String getCode() {
            return String.valueOf(getValue());
        }

        /**
         * 区分コードから、除籍（卒業）区分のインスタンスを得る。
         * @param value 区分コード
         * @return 除籍（卒業）区分のインスタンス
         */
        public static GrdDiv getInstance(final int value) {
            return (GrdDiv) EnumUtils.getEnum(GrdDiv.class, value);
        }
    } // GrdDiv
} // Student

// eof
