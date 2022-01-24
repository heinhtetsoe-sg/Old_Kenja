<?php

require_once('for_php7.php');

class knjl511fModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl511fModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
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
        
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),           //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),                //入試区分
                             "EXAMCOURSE"       =>  VARS::post("EXAMCOURSE"),             //志望区分
                             "EXAMCOURSE2"      =>  VARS::post("EXAMCOURSE2"),            //志望区分2
                             "RECEPTDATE"       =>  VARS::post("RECEPTDATE"),             //受付日付
                             "TESTDIV1"         =>  VARS::post("TESTDIV1"),               //受験科目数
                             "SEQ005_R1"        =>  VARS::post("SEQ005_R1"),              //英語見なし得点
                             "SEQ005_R2"        =>  VARS::post("SEQ005_R2"),              //奨学区分
                             "SEQ005_R5"        =>  VARS::post("SEQ005_R5"),              //特別条件
                             "SEQ005_R6"        =>  VARS::post("SEQ005_R6"),              //注意を要する
                             "SEQ005_R7"        =>  VARS::post("SEQ005_R7"),              //別室受験
                             "SEQ004_R9"        =>  VARS::post("SEQ004_R9"),              //事前相談CD
                             "SEQ004_R10"       =>  VARS::post("SEQ004_R10"),             //事前相談TEXT
                             "SPECIAL_MEASURES" =>  VARS::post("SPECIAL_MEASURES"),       //特別措置者
                             "NAME"             =>  VARS::post("NAME"),                   //氏名(志願者)
                             "NAME_KANA"        =>  VARS::post("NAME_KANA"),              //氏名かな(志願者)
                             "SEX"              =>  VARS::post("SEX"),                    //性別
                             "BIRTHDAY"         =>  VARS::post("BIRTHDAY"),               //生年月日
                             "FS_CD"            =>  VARS::post("FINSCHOOLCD"),            //出身学校コード
                             "FS_ERACD"         =>  VARS::post("FS_ERACD"),               //卒業元号
                             "FS_WNAME"         =>  VARS::post("FS_WNAME"),               //卒業元号・和暦名
                             "FS_Y"             =>  VARS::post("FS_Y"),                   //卒業年
                             "FS_M"             =>  VARS::post("FS_M"),                   //卒業月
                             "FS_GRDDIV"        =>  VARS::post("FS_GRDDIV"),              //卒業区分
                             "ZIPCD"            =>  VARS::post("ZIPCD"),                  //郵便番号(志願者)
                             "ADDRESS1"         =>  VARS::post("ADDRESS1"),               //住所(志願者)
                             "ADDRESS2"         =>  VARS::post("ADDRESS2"),               //住所かな(志願者)
                             "TELNO"            =>  VARS::post("TELNO"),                  //電話番号(志願者)
                             "GNAME"            =>  VARS::post("GNAME"),                  //氏名(保護者)
                             "RELATIONSHIP"     =>  VARS::post("RELATIONSHIP"),           //続柄
                             "GKANA"            =>  VARS::post("GKANA"),                  //氏名かな(保護者)
                             "GZIPCD"           =>  VARS::post("GZIPCD"),                 //郵便番号(保護者)
                             "GADDRESS1"        =>  VARS::post("GADDRESS1"),              //住所(保護者)
                             "GADDRESS2"        =>  VARS::post("GADDRESS2"),              //住所かな(保護者)
                             "GTELNO"           =>  VARS::post("GTELNO"),                 //電話番号(保護者)
                             "SHDIV"            =>  VARS::post("SHDIV")                   //専併
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
    function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl511fQuery::getMaxExamnoQuery($this);
        return;
    }

    //入力項目チェック
    function validateInput()
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
        if (!($this->field["TESTDIV1"])) {
            $this->setWarning("MSG301", "( 受験科目数 )");
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
        if (0 == knjl511fQuery::getEntexamNoDatCnt($db, $this)) {
            $this->setWarning("MSG305", "( 受験番号 )\\n受験番号範囲マスタが未設定です。");
            return false;
        }

        //存在チェック
        $query = knjl511fQuery::Existdata($this);
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
            $this->setWarning("MSG915","受験番号は4バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 90) {
            $this->setWarning("MSG915","志願者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 90) {
            $this->setWarning("MSG915","志願者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( ふりがな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS1"]) > 90) {
            $this->setWarning("MSG915","保護者( 住所 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 90) {
            $this->setWarning("MSG915","保護者( 方書 )は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915","保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["SEQ005_R5"]) > 120) {
            $this->setWarning("MSG915","特別条件は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["SEQ004_R10"]) > 30) {
            $this->setWarning("MSG915","事前相談は30バイトまでです。");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl511fQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl511fQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        if ($this->examno == "") {
            $this->setWarning("MSG304", "( 受験番号 )");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $query = knjl511fQuery::Existdata($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //DB切断
        Query::dbCheckIn($db);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl511fQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>