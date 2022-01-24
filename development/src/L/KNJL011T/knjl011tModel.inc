<?php

require_once('for_php7.php');

class knjl011tModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受検番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011tModel()
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

        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }

        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV2"       =>  VARS::post("TESTDIV2"),           //追検査
                             "SHIFT_DESIRE_FLG" =>  VARS::post("SHIFT_DESIRE_FLG"), //推薦区分
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "ERACD"          =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"        =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"        =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"        =>  VARS::post("BIRTH_D"),            //生年月日日
                             "BIRTHDAY"       =>  VARS::post("BIRTHDAY"),           //生年月日（西暦）
                             "FS_CD"          =>  VARS::post("FS_CD"),              //出身学校コード
                             "FS_NAME"        =>  VARS::post("FS_NAME"),            //出身学校名
                             "FS_AREA_CD"     =>  VARS::post("FS_AREA_CD"),         //出身学校地区コード
                             "FS_ERACD"       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"           =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDDIV"      =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "REMARK1"        =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"        =>  VARS::post("REMARK2"),            //備考２
                             "ZIPCD"          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"       =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "GNAME"          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "GKANA"          =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"      =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"         =>  VARS::post("GTELNO"),             //電話番号(保護者)
                             "WNAME"          =>  VARS::post("WNAME")               //生年月日元号・和暦名
                            );
        for ($m = 1; $m <= 6; $m++) {
            $this->field["MAJORLCD" .$m] = VARS::post("MAJORLCD" .$m);
            $this->field["MAJORSCD" .$m] = VARS::post("MAJORSCD" .$m);
        }
        for ($m = 1; $m <= 5; $m++) {
            $this->field["HOPEFLG" .$m]      = VARS::post("HOPEFLG" .$m);
            $this->field["INC_HOPEFLG" .$m]  = VARS::post("INC_HOPEFLG" .$m);
        }
        //内申
        $this->field["CONFIDENTIAL_RPT01"] = VARS::post("CONFIDENTIAL_RPT01");
        $this->field["CONFIDENTIAL_RPT02"] = VARS::post("CONFIDENTIAL_RPT02");
        $this->field["CONFIDENTIAL_RPT03"] = VARS::post("CONFIDENTIAL_RPT03");
        $this->field["CONFIDENTIAL_RPT04"] = VARS::post("CONFIDENTIAL_RPT04");
        $this->field["CONFIDENTIAL_RPT05"] = VARS::post("CONFIDENTIAL_RPT05");
        $this->field["CONFIDENTIAL_RPT06"] = VARS::post("CONFIDENTIAL_RPT06");
        $this->field["CONFIDENTIAL_RPT07"] = VARS::post("CONFIDENTIAL_RPT07");
        $this->field["CONFIDENTIAL_RPT08"] = VARS::post("CONFIDENTIAL_RPT08");
        $this->field["CONFIDENTIAL_RPT09"] = VARS::post("CONFIDENTIAL_RPT09");
        $this->field["CONFIDENTIAL_RPT10"] = VARS::post("CONFIDENTIAL_RPT10");
        $this->field["TOTAL3"] = VARS::post("TOTAL3");
        $this->field["TOTAL5"] = VARS::post("TOTAL5");
        $this->field["TOTAL9"] = VARS::post("TOTAL9");
        $this->field["AVERAGE_ALL"]  = VARS::post("AVERAGE_ALL");
        $this->field["ABSENCE_DAYS1"] = VARS::post("ABSENCE_DAYS1");
        $this->field["ABSENCE_DAYS2"] = VARS::post("ABSENCE_DAYS2");
        $this->field["ABSENCE_DAYS3"] = VARS::post("ABSENCE_DAYS3");
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受検番号 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
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
        for ($m = 1; $m <= 6; $m++) {
            $msgno = ($this->field["APPLICANTDIV"] == "1") ? "" : "( " .$m .".学科・コース )";
            if (0 < strlen($this->field["MAJORLCD" .$m]) && strlen($this->field["MAJORSCD" .$m]) < 1) {
                $this->setWarning("MSG301", "( 志望区分 )" .$msgno);
                return false;
            }
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
            $day = knjl011tQuery::CheckDate($this);
            $yy = ((int)$day["NAMESPARE1"]) + ((int)$this->field["BIRTH_Y"] - 1);
            $mm = (int)$this->field["BIRTH_M"];
            $dd = (int)$this->field["BIRTH_D"];
            //日付として正しいかチェック
            if(!checkdate($mm, $dd, $yy)) {
                $this->setWarning("MSG901", "日付を正しく入力して下さい。( 生年月日 )");
                return false;
            }
            $check_date = $yy.sprintf("%02d",$mm).sprintf("%02d",$dd);
            $start_date = str_replace("/", "", $day["NAMESPARE2"]);
            $last_date = str_replace("/", "", $day["NAMESPARE3"]);
            if ((int)$check_date < (int)$start_date || (int)$check_date > (int)$last_date) {
                $this->setWarning("MSG901", "日付を正しく入力して下さい。( 生年月日 )");
                return false;
            }
        }
        if (strlen($this->field["FS_Y"]) || strlen($this->field["FS_M"]) || strlen($this->field["FS_GRDDIV"]))
        {
            if (!strlen($this->field["FS_ERACD"]) || !strlen($this->field["FS_Y"]) || !strlen($this->field["FS_M"])) {
                $this->setWarning("MSG301", "( 卒業年月 )");
                return false;
            }
            if (!strlen($this->field["FS_GRDDIV"])) {
                $this->setWarning("MSG301", "( 卒業年月 )( 卒業区分 )");
                return false;
            }
        }
        //存在チェック
        $row = knjl011tQuery::Existdata($this);
        if (isset($row["EXAMNO"]) && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受検番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( 氏名かな )は120バイトまでです。");
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
        $this->field["FS_NAME"] = "";
        $this->field["FS_AREA_CD"] = "";
        if (strlen($this->field["FS_CD"])) {
            $Row = knjl011tQuery::getFinschoolName($this->field["FS_CD"]);
            $this->field["FS_NAME"] = $Row["FINSCHOOL_NAME"];
            $this->field["FS_AREA_CD"] = $Row["DISTRICTCD"];
        }
        if (strlen($this->field["FS_NAME"]) > 45) {
            $this->field["FS_NAME"] = substr($this->field["FS_NAME"],0,45);
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
            $this->setWarning("MSG915","保護者( 氏名かな )は120バイトまでです。");
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
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","備考１は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 60) {
            $this->setWarning("MSG915","備考２は60バイトまでです。");
            return false;
        }
        //重複データチェック(同じ受検番号が違う入試制度に在る場合、重複エラー）
        $existdata = knjl011tQuery::CheckData($this);
        if (is_array($existdata)) {
            $msg  = "この受検番号は違う入試制度に既に登録されています。　　\\n\\n";
            $msg .= "受検番号 ： ".$existdata["EXAMNO"]."\\n";
            $msg .= "入試制度 ： ".$existdata["NAME1"];
            $this->setWarning("MSG302", $msg);
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011tQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011tQuery::getInsertUpdateQuery($this);
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
            $this->setWarning("MSG304", "( 受検番号 )");
            return false;
        }
        $row = knjl011tQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl011tQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
    //追加処理
    function &getMaxExamno()
    {
        //最後の受検番号＋１
        $this->examno = knjl011tQuery::getMaxExamnoQuery($this->year);
        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }
        return;
    }

}
?>