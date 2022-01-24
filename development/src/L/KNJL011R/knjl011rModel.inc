<?php

require_once('for_php7.php');

class knjl011rModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011rModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->examno       = VARS::post("EXAMNO");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV"        =>  VARS::post("TESTDIV"),            //入試区分
                             "TESTDIV1"       =>  VARS::post("TESTDIV1"),           //志望区分1
                             "TESTDIV2"       =>  VARS::post("TESTDIV2"),           //志望区分2
                             "TESTDIV3"       =>  VARS::post("TESTDIV3"),           //志望区分3
                             "TESTDIV4"       =>  VARS::post("TESTDIV4"),           //志望区分4
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名カナ(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "ERACD"          =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"        =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"        =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"        =>  VARS::post("BIRTH_D"),            //生年月日日
                             "FS_CD"          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_AREA_CD"     =>  VARS::post("FS_AREA_CD"),         //出身学校地区コード
                             "FS_ERACD"       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"           =>  VARS::post("FS_M"),               //卒業月
                             "ZIPCD"          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"       =>  VARS::post("ADDRESS2"),           //住所方書(志願者)
                             "TELNO"          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "FS_GRDYEAR"     =>  VARS::post("FS_GRDYEAR"),         //卒業年
                             "GNAME"          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "RELATIONSHIP"   =>  VARS::post("RELATIONSHIP"),       //続柄
                             "GKANA"          =>  VARS::post("GKANA"),              //氏名カナ(保護者)
                             "GZIPCD"         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"      =>  VARS::post("GADDRESS2"),          //住所方書(保護者)
                             "GTELNO"         =>  VARS::post("GTELNO"),             //電話番号(保護者)
                             "TOTAL_ALL"      =>  VARS::post("TOTAL_ALL"),          //合計点
                             "ABSENCE_DAYS3"  =>  VARS::post("ABSENCE_DAYS3"),      //欠席日数
                             "WNAME"          =>  VARS::post("WNAME")               //和暦名
                            );
    }

    //追加処理
    function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl011rQuery::getMaxExamnoQuery($this);
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
        if (!strlen($this->field["TESTDIV0"]) && 
            !$this->field["TESTDIV1"] && 
            !$this->field["TESTDIV2"] && 
            !$this->field["TESTDIV3"] && 
            !$this->field["TESTDIV4"]) {
            $this->setWarning("MSG301", "( 志望区分 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 氏名カナ )");
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
        //日付チェック
        if (strlen($this->field["ERACD"]) || strlen($this->field["BIRTH_Y"]) ||
            strlen($this->field["BIRTH_M"]) || strlen($this->field["BIRTH_D"]))
        {
            $day = knjl011rQuery::CheckDate($db, $this);
            $yy = ((int)$day["NAMESPARE1"]) + ((int)$this->field["BIRTH_Y"] - 1);
            $mm = (int)$this->field["BIRTH_M"];
            $dd = (int)$this->field["BIRTH_D"];
            //日付として正しいかチェック
            if(!checkdate($mm, $dd, $yy)) {
                $this->setWarning("MSG901", "日付を正しく入力して下さい。");
                return false;
            }
            $check_date = $yy.sprintf("%02d",$mm).sprintf("%02d",$dd);
            $start_date = str_replace("/", "", $day["NAMESPARE2"]);
            $last_date = str_replace("/", "", $day["NAMESPARE3"]);
            if ((int)$check_date < (int)$start_date || (int)$check_date > (int)$last_date) {
                $this->setWarning("MSG901", "日付を正しく入力して下さい。");
                return false;
            }
        }

        //存在チェック
        $query = knjl011rQuery::Existdata($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (isset($row["EXAMNO"]) && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        //志望区分必須
        $wishCnt = 0;
        for ($code = 1; $code <= 4; $code++) {
            if (strlen($this->field["TESTDIV".$code])) {
                $wishCnt++;
            }
        }
        if ($wishCnt <= 0) {
            $this->setWarning("MSG301","(志望区分)");
            return false;
        }

        //志望区分
        $wishArray = array();
        $result = $db->query(knjl011rQuery::getWishMst($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $wishArray[$row["DESIREDIV"]][$row["EXAMCOURSECD"]] = $row["EXAMCOURSECD"] * 1;
        }
        $result->free();
        foreach ($wishArray as $key => $val) {
            $errFlg = false;
            for ($code = 1; $code <= 4; $code++) {
                if (strlen($this->field["TESTDIV".$code]) && !in_array($code, $val)) {
                    $errFlg = true;
                }
                if (!strlen($this->field["TESTDIV".$code]) && in_array($code, $val)) {
                    $errFlg = true;
                }
            }
            if (!$errFlg) {
                break;
            }
        }

        if ($errFlg) {
            $this->setWarning("志望区分のパターンが不正です。");
            return false;
        }

        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( 氏名カナ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ERACD"]) > 1) {
            $this->setWarning("MSG915","生年月日元号は1バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIRTH_Y"]) > 2) {
            $this->setWarning("MSG915","生年月日年は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIRTH_M"]) > 2) {
            $this->setWarning("MSG915","生年月日月は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIRTH_D"]) > 2) {
            $this->setWarning("MSG915","生年月日日は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS1"]) > 75) {
            $this->setWarning("MSG915","志願者( 住所 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 75) {
            $this->setWarning("MSG915","志願者( 方書 )は75バイトまでです。");
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
            $this->setWarning("MSG915","保護者( 氏名カナ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS1"]) > 75) {
            $this->setWarning("MSG915","保護者( 住所 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 75) {
            $this->setWarning("MSG915","保護者( 方書 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915","保護者( 電話番号 )は14バイトまでです。");
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
            $result = knjl011rQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011rQuery::getUpdateQuery($this);
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

        $query = knjl011rQuery::Existdata($this);
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
        knjl011rQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>