<?php

require_once('for_php7.php');

class knjl011fModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl011fModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->examno       = VARS::post("EXAMNO");
        
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV"        =>  VARS::post("TESTDIV"),            //入試区分
                             "TESTDIV0"       =>  VARS::post("TESTDIV0"),           //入試回数
                             "EXAMCOURSE"     =>  VARS::post("EXAMCOURSE"),         //志望区分
                             "RECEPTDATE"     =>  VARS::post("RECEPTDATE"),         //受付日付
                             "GANSHO_YUUSOU"  =>  VARS::post("GANSHO_YUUSOU"),      //願書郵送
                             "SPECIAL_REASON_DIV"   =>  VARS::post("SPECIAL_REASON_DIV"),         //特別措置者
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "BIRTHDAY"       =>  VARS::post("BIRTHDAY"),           //生年月日
                             "FS_CD"          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_ERACD"       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"           =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDDIV"      =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "ZIPCD"          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"       =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "GNAME"          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "RELATIONSHIP"   =>  VARS::post("RELATIONSHIP"),       //続柄
                             "GKANA"          =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"      =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"         =>  VARS::post("GTELNO"),             //電話番号(保護者)

                             "EXAM_PAY_DIV"         =>  VARS::post("EXAM_PAY_DIV"),         //受験料・入金方法
                             "EXAM_PAY_DATE"        =>  VARS::post("EXAM_PAY_DATE"),        //受験料・入金日
                             "EXAM_PAY_CHAK_DATE"   =>  VARS::post("EXAM_PAY_CHAK_DATE"),   //受験料・着金日

                             "GAKU_TEST_FLG"        =>  VARS::post("GAKU_TEST_FLG"),        //学力診断テスト対象者
                             "RECRUIT_NO"           =>  VARS::post("RECRUIT_NO"),           //事前番号

                             "SHDIV"                =>  VARS::post("SHDIV"),                //第１志望
                             "SHIFT_DESIRE_FLG"     =>  VARS::post("SHIFT_DESIRE_FLG"),     //延期願
                             "SH_SCHOOLNAME"        =>  VARS::post("SH_SCHOOLNAME"),        //併願校名
                             "SH_JUDGEMENT_DATE"    =>  VARS::post("SH_JUDGEMENT_DATE"),    //併願校合格発表日
                             "SLIDE_FLG"            =>  VARS::post("SLIDE_FLG"),            //アドバンストへの変更
                             "SELECT_SUBCLASS_DIV"  =>  VARS::post("SELECT_SUBCLASS_DIV"),  //受験科目

                             "MOTHER_NAME"          =>  VARS::post("MOTHER_NAME"),          //校友会卒業時氏名
                             "MOTHER_NENDO"         =>  VARS::post("MOTHER_NENDO"),         //校友会卒業年度
                             "MOTHER_HR_CLASS"      =>  VARS::post("MOTHER_HR_CLASS"),      //校友会卒業クラス
                             "MOTHER_BIRTHDAY"      =>  VARS::post("MOTHER_BIRTHDAY"),      //校友会生年月日
                             "KOUYUU_DIV"           =>  VARS::post("KOUYUU_DIV"),           //校友会関係
                             "KOUYUU_SCHOOL_KIND"   =>  VARS::post("KOUYUU_SCHOOL_KIND"),   //校友会中高
                             "KOUYUU_COLLEGE"       =>  VARS::post("KOUYUU_COLLEGE"),       //校友会大学・学部

                             "REMARK1"        =>  VARS::post("REMARK1"),            //備考1
                             "REMARK2"        =>  VARS::post("REMARK2"),            //備考2
                             "REMARK3"        =>  VARS::post("REMARK3"),            //備考3
                             "REMARK4"        =>  VARS::post("REMARK4"),            //備考4
                             "REMARK5"        =>  VARS::post("REMARK5"),            //備考5
                             "REMARK6"        =>  VARS::post("REMARK6"),            //備考6
                             "SHOUGAKU1"      =>  VARS::post("SHOUGAKU1"),          //備考1 奨学生コンボ
                             "SHOUGAKU5"      =>  VARS::post("SHOUGAKU5"),          //備考5 奨学生コンボ
                             "SOUDAN"         =>  VARS::post("SOUDAN")              //教育相談
                            );
        //調査書
        for ($i = 1; $i <= 10; $i++) {
            $num = sprintf("%02d", $i);
            $this->field["CONFIDENTIAL_RPT".$num] = VARS::post("CONFIDENTIAL_RPT".$num);
        }
        $this->field["TOTAL3"] = VARS::post("TOTAL3");
        $this->field["TOTAL5"] = VARS::post("TOTAL5");
        $this->field["TOTAL_ALL"] = VARS::post("TOTAL_ALL");
        $this->field["KASANTEN_ALL"] = VARS::post("KASANTEN_ALL");
        $this->field["ABSENCE_DAYS"] = VARS::post("ABSENCE_DAYS");
        $this->field["ABSENCE_DAYS2"] = VARS::post("ABSENCE_DAYS2");
        $this->field["ABSENCE_DAYS3"] = VARS::post("ABSENCE_DAYS3");
        $this->field["ABSENCE_REMARK"] = VARS::post("ABSENCE_REMARK");
        $this->field["ABSENCE_REMARK2"] = VARS::post("ABSENCE_REMARK2");
        $this->field["ABSENCE_REMARK3"] = VARS::post("ABSENCE_REMARK3");
        $this->field["CONFRPT_REMARK1"] = VARS::post("CONFRPT_REMARK1");
        $this->field["DETAIL4_REMARK1"] = VARS::post("DETAIL4_REMARK1");
    }

    //追加処理
    public function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl011fQuery::getMaxExamnoQuery($this);
        return;
    }

    //受験番号範囲チェック（2017年度入試までの仕様 ===> 2018年度入試から受験番号範囲マスタに変更）
    public function checkNumber()
    {
        $check = array();
        //Ａ推薦
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "1001", "MAX_NO" => "1099");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "1101", "MAX_NO" => "1199");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "1201", "MAX_NO" => "1299");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "1301", "MAX_NO" => "1399");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "1501", "MAX_NO" => "1599");
        //Ｂ推薦
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "3001", "MAX_NO" => "3099");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "3101", "MAX_NO" => "3199");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "3201", "MAX_NO" => "3299");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "3301", "MAX_NO" => "3399");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "3501", "MAX_NO" => "3599");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "4001", "MAX_NO" => "4099");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "4101", "MAX_NO" => "4199");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "4201", "MAX_NO" => "4299");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "4301", "MAX_NO" => "4399");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "4501", "MAX_NO" => "4599");
        //一般（併願優遇含む）
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "5001", "MAX_NO" => "5099");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "5101", "MAX_NO" => "5199");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "5201", "MAX_NO" => "5299");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "5301", "MAX_NO" => "5399");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "5501", "MAX_NO" => "5599");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "6001", "MAX_NO" => "6099");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "6101", "MAX_NO" => "6199");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "6201", "MAX_NO" => "6299");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "6301", "MAX_NO" => "6399");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "6501", "MAX_NO" => "6599");
        //第３回・第４回一般（併願優遇含む）
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "3", "EXAMCOURSECD" => "1001", "MIN_NO" => "6001", "MAX_NO" => "6099");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "3", "EXAMCOURSECD" => "1002", "MIN_NO" => "6101", "MAX_NO" => "6199");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "3", "EXAMCOURSECD" => "2001", "MIN_NO" => "6201", "MAX_NO" => "6299");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "3", "EXAMCOURSECD" => "2002", "MIN_NO" => "6301", "MAX_NO" => "6399");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "3", "EXAMCOURSECD" => "3001", "MIN_NO" => "6501", "MAX_NO" => "6599");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "4", "EXAMCOURSECD" => "1001", "MIN_NO" => "6001", "MAX_NO" => "6099");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "4", "EXAMCOURSECD" => "1002", "MIN_NO" => "6101", "MAX_NO" => "6199");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "4", "EXAMCOURSECD" => "2001", "MIN_NO" => "6201", "MAX_NO" => "6299");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "4", "EXAMCOURSECD" => "2002", "MIN_NO" => "6301", "MAX_NO" => "6399");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "4", "EXAMCOURSECD" => "3001", "MIN_NO" => "6501", "MAX_NO" => "6599");
        //帰国生Ａ方式
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        //帰国生Ｂ方式
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        //一貫生
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $course_array = explode("-", $this->field["EXAMCOURSE"]);
        foreach ($check as $key => $val) {
            if ($this->field["TESTDIV"] == $val["TESTDIV"] && $this->field["TESTDIV0"] == $val["TESTDIV0"] && $course_array[2] == $val["EXAMCOURSECD"]) {
                if ($this->examno < $val["MIN_NO"] || $this->examno > $val["MAX_NO"]) {
                    $msg = " (".$val["MIN_NO"]."～".$val["MAX_NO"].")";
                    return $msg;
                }
            }
        }
        return "";
    }

    //入力項目チェック
    public function validateInput()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->field["SHDIV"])) {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if (!($this->field["EXAMCOURSE"])) {
            $this->setWarning("MSG301", "( 志望区分 )");
            return false;
        }
        if (!($this->field["RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日付 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 ふりがな )");
            return false;
        }
        if (!($this->field["TELNO"])) {
            $this->setWarning("MSG301", "( 志願者 電話番号 )");
            return false;
        }
        if (!($this->field["GNAME"])) {
            $this->setWarning("MSG301", "( 保護者 氏名 )");
            return false;
        }
        if (!($this->field["GKANA"])) {
            $this->setWarning("MSG301", "( 保護者 ふりがな )");
            return false;
        }
        if (!($this->field["RELATIONSHIP"])) {
            $this->setWarning("MSG301", "( 保護者 続柄 )");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //受験番号範囲チェック
        if ($this->field["TESTDIV"] != "6" && 0 == $db->getOne(knjl011fQuery::getEntexamNoDatCnt($this))) {
            $this->setWarning("MSG305", "( 受験番号 )\\n受験番号範囲マスタが未設定です。");
            return false;
        }

        //存在チェック
        $query = knjl011fQuery::existData($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (isset($row["EXAMNO"]) && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }

        //文字数チェック
        if (strlen($this->examno) > 4) {
            $this->setWarning("MSG915", "受験番号は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915", "志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915", "志願者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 90) {
            $this->setWarning("MSG915", "志願者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 90) {
            $this->setWarning("MSG915", "志願者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915", "出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915", "志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915", "保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915", "保護者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS1"]) > 90) {
            $this->setWarning("MSG915", "保護者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 90) {
            $this->setWarning("MSG915", "保護者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915", "備考1は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 75) {
            $this->setWarning("MSG915", "備考2は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK3"]) > 75) {
            $this->setWarning("MSG915", "備考3は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK4"]) > 75) {
            $this->setWarning("MSG915", "備考4は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK5"]) > 60) {
            $this->setWarning("MSG915", "備考5は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK6"]) > 75) {
            $this->setWarning("MSG915", "備考6は75バイトまでです。");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl011fQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011fQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        if ($this->examno == "") {
            $this->setWarning("MSG304", "( 受験番号 )");
            return false;
        }
        $row = knjl011fQuery::existData($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        knjl011fQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
}
