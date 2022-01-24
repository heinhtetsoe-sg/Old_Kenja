<?php

require_once('for_php7.php');

class knjl011nModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl011nModel()
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
                             "SHDIV"          =>  VARS::post("SHDIV"),              //専併区分
                             "EXAMCOURSE"     =>  VARS::post("EXAMCOURSE"),         //志望区分
                             "EXAMCOURSE2"    =>  VARS::post("EXAMCOURSE2"),        //志望区分（第二志望）
                             "RECEPTDATE"     =>  VARS::post("RECEPTDATE"),         //受付日付
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "BIRTHDAY"       =>  VARS::post("BIRTHDAY"),           //生年月日
                             "FS_CD"          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_AREA_CD"     =>  VARS::post("FS_AREA_CD"),         //出身学校地区コード
                             "ZIPCD"          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"       =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "FS_DAY"         =>  VARS::post("FS_DAY"),             //卒業年月日
                             "FS_GRDDIV"      =>  VARS::post("FS_GRDDIV"),          //卒業年
                             "GNAME"          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "RELATIONSHIP"   =>  VARS::post("RELATIONSHIP"),       //続柄
                             "GKANA"          =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"      =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"         =>  VARS::post("GTELNO"),             //電話番号(保護者)
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
        $this->field["TOTAL_ALL"] = VARS::post("TOTAL_ALL");
        $this->field["TOTAL5"] = VARS::post("TOTAL5");
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
        $this->examno = knjl011nQuery::getMaxExamnoQuery($this);
        return;
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
        if (strlen($this->field["EXAMCOURSE"]) && strlen($this->field["EXAMCOURSE2"]) && $this->field["EXAMCOURSE"] == $this->field["EXAMCOURSE2"]) {
            $this->setWarning("MSG901", "第一志望コースと第二志望コースが重複しています。");
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
            $this->setWarning("MSG301", "( 志願者 氏名かな )");
            return false;
        }
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
            return false;
        }
        if (strlen($this->field["GNAME"]) != 0 && strlen($this->field["GZIPCD"]) == 0) {
            $this->setWarning("MSG301", "( 保護者郵便番号 )");
            return false;
        }
        if (strlen($this->field["GNAME"]) != 0 && strlen($this->field["GADDRESS1"]) == 0) {
            $this->setWarning("MSG301", "( 保護者住所 )");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //存在チェック
        $query = knjl011nQuery::existData($this);
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
            $this->setWarning("MSG915", "志願者( 氏名かな )は120バイトまでです。");
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
            $this->setWarning("MSG915", "保護者( 氏名かな )は120バイトまでです。");
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
            $result = knjl011nQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011nQuery::getUpdateQuery($this);
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
        $row = knjl011nQuery::existData($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        knjl011nQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
}
