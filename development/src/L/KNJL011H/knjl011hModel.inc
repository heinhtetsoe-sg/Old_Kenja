<?php

require_once('for_php7.php');

class knjl011hModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011hModel()
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
            $this->examno = sprintf("%04d",$this->examno);
        }
        
        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV1"       =>  VARS::post("TESTDIV1"),           //入試区分1
                             "TESTDIV2"       =>  VARS::post("TESTDIV2"),           //入試区分2
                             "TESTDIV3"       =>  VARS::post("TESTDIV3"),           //入試区分3
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
                             "FS_NATPUBPRIDIV" => VARS::post("FS_NATPUBPRIDIV"),    //国公私立
                             "PRISCHOOLCD"    =>  VARS::post("PRISCHOOLCD"),        //塾コード
                             "REMARK1"        =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"        =>  VARS::post("REMARK2"),            //備考２
                             "RECOM_EXAMNO"   =>  VARS::post("RECOM_EXAMNO"),       //推薦受験番号
                             "ZIPCD"          =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"       =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"       =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"          =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "FS_GRDYEAR"     =>  VARS::post("FS_GRDYEAR"),         //卒業年
                             "GNAME"          =>  VARS::post("GNAME"),              //氏名(保護者)
                             "GKANA"          =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"         =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"      =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"      =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"         =>  VARS::post("GTELNO"),             //電話番号(保護者)
                             "WNAME"          =>  VARS::post("WNAME"),              //和暦名
                             "CONFIDENTIAL_RPT01" => VARS::post("CONFIDENTIAL_RPT01"),
                             "CONFIDENTIAL_RPT02" => VARS::post("CONFIDENTIAL_RPT02"),
                             "CONFIDENTIAL_RPT03" => VARS::post("CONFIDENTIAL_RPT03"),
                             "CONFIDENTIAL_RPT04" => VARS::post("CONFIDENTIAL_RPT04"),
                             "CONFIDENTIAL_RPT05" => VARS::post("CONFIDENTIAL_RPT05"),
                             "CONFIDENTIAL_RPT06" => VARS::post("CONFIDENTIAL_RPT06"),
                             "CONFIDENTIAL_RPT07" => VARS::post("CONFIDENTIAL_RPT07"),
                             "CONFIDENTIAL_RPT08" => VARS::post("CONFIDENTIAL_RPT08"),
                             "CONFIDENTIAL_RPT09" => VARS::post("CONFIDENTIAL_RPT09"),
                             "CONFIDENTIAL_RPT10" => VARS::post("CONFIDENTIAL_RPT10"),
                             "CONFIDENTIAL_RPT11" => VARS::post("CONFIDENTIAL_RPT11"),
                             "CONFIDENTIAL_RPT12" => VARS::post("CONFIDENTIAL_RPT12"),
                             "TOTAL_ALL"          => VARS::post("TOTAL_ALL"),
                             "KASANTEN_ALL"       => VARS::post("KASANTEN_ALL"),
                             "ABSENCE_DAYS"       => VARS::post("ABSENCE_DAYS")
                            );
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (!($this->field["TESTDIV1"]) && !($this->field["TESTDIV2"]) && !($this->field["TESTDIV3"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
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
        $row = knjl011hQuery::Existdata($this);
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
            $this->setWarning("MSG915","志願者( 氏名かな )は120バイトまでです。");
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
            $Row = knjl011hQuery::getFinschoolName($this->field["FS_CD"]);
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
        if (strlen($this->field["FS_GRDYEAR"]) > 4) {
            $this->setWarning("MSG915","卒業年は4バイトまでです。");
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
        //推薦受験番号チェック
        if ($this->field["APPLICANTDIV"] != "2" && strlen($this->field["RECOM_EXAMNO"])) {
            $this->setWarning("MSG901","入力可能なのは、高校一般入試のみです。（推薦受験番号）");
            return false;
        }
        //推薦受験番号の存在チェック
        if (strlen($this->field["RECOM_EXAMNO"])) {
            //存在チェック
            $retCnt = knjl011hQuery::checkRecomExamno($this, $this->field["RECOM_EXAMNO"]);
            if (0 < $retCnt) {
                //重複チェック
                $retCnt2 = knjl011hQuery::checkRecomExamno2($this, $this->field["RECOM_EXAMNO"], $this->examno);
                if (0 < $retCnt2) {
                    $this->setWarning("MSG901", "入力された推薦受験番号は既に登録されています。（推薦受験番号）");
                    return false;
                }
            } else {
                $this->setWarning("MSG901", "入力された推薦受験番号のデータは存在しません。（推薦受験番号）");
                return false;
            }
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011hQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011hQuery::getInsertUpdateQuery($this);
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
        $row = knjl011hQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl011hQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>