<?php

require_once('for_php7.php');

class knjl011jModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011jModel()
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
        $this->field = array("APPLICANTDIV"       =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "RECEPTDATE"         =>  VARS::post("RECEPTDATE"),         //受付日（出願受付日）
                             "TESTDIV1"           =>  VARS::post("TESTDIV1"),           //入試区分1
                             "TESTDIV2"           =>  VARS::post("TESTDIV2"),           //入試区分2
                             "TESTDIV3"           =>  VARS::post("TESTDIV3"),           //入試区分3
                             "TESTDIV4"           =>  VARS::post("TESTDIV4"),           //入試区分4
                             "TESTDIV5"           =>  VARS::post("TESTDIV5"),           //入試区分5
                             "TESTDIV6"           =>  VARS::post("TESTDIV6"),           //入試区分6
                             "EXAM_TYPE1"         =>  VARS::post("EXAM_TYPE1"),         //受験型1
                             "EXAM_TYPE2"         =>  VARS::post("EXAM_TYPE2"),         //受験型2
                             "EXAM_TYPE3"         =>  VARS::post("EXAM_TYPE3"),         //受験型3
                             "EXAM_TYPE4"         =>  VARS::post("EXAM_TYPE4"),         //受験型4
                             "EXAM_TYPE5"         =>  VARS::post("EXAM_TYPE5"),         //受験型5
                             "EXAM_TYPE6"         =>  VARS::post("EXAM_TYPE6"),         //受験型6
                             "SPECIAL_REASON_DIV" =>  VARS::post("SPECIAL_REASON_DIV"), //特殊事情区分
                             "NAME"               =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"          =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"                =>  VARS::post("SEX"),                //性別
                             "ERACD"              =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"            =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"            =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"            =>  VARS::post("BIRTH_D"),            //生年月日日
                             "BIRTHDAY"           =>  VARS::post("BIRTHDAY"),           //生年月日（西暦）
                             "FS_NATPUBPRIDIV"    =>  VARS::post("FS_NATPUBPRIDIV"),    //国公私立
                             "FS_CD"              =>  VARS::post("FS_CD"),              //出身学校コード
                             "FS_NAME"            =>  VARS::post("FS_NAME"),            //出身学校名
                             "FS_AREA_CD"         =>  VARS::post("FS_AREA_CD"),         //出身学校地区コード
                             "PRISCHOOLCD"        =>  VARS::post("PRISCHOOLCD"),        //塾コード
                             "REMARK1"            =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"            =>  VARS::post("REMARK2"),            //備考２
                             "ZIPCD"              =>  VARS::post("ZIPCD"),              //郵便番号(志願者)
                             "ADDRESS1"           =>  VARS::post("ADDRESS1"),           //住所(志願者)
                             "ADDRESS2"           =>  VARS::post("ADDRESS2"),           //住所かな(志願者)
                             "TELNO"              =>  VARS::post("TELNO"),              //電話番号(志願者)
                             "FS_GRDYEAR"         =>  VARS::post("FS_GRDYEAR"),         //卒業年
                             "GNAME"              =>  VARS::post("GNAME"),              //氏名(保護者)
                             "GKANA"              =>  VARS::post("GKANA"),              //氏名かな(保護者)
                             "GZIPCD"             =>  VARS::post("GZIPCD"),             //郵便番号(保護者)
                             "GADDRESS1"          =>  VARS::post("GADDRESS1"),          //住所(保護者)
                             "GADDRESS2"          =>  VARS::post("GADDRESS2"),          //住所かな(保護者)
                             "GTELNO"             =>  VARS::post("GTELNO"),             //電話番号(保護者)
                             "GFAXNO"             =>  VARS::post("GFAXNO"),             //FAX番号(保護者)
                             "RELATIONSHIP"       =>  VARS::post("RELATIONSHIP"),       //続柄(保護者)
                             "EMERGENCYCALL"      =>  VARS::post("EMERGENCYCALL"),      //緊急連絡先名称(保護者)
                             "EMERGENCYTELNO"     =>  VARS::post("EMERGENCYTELNO"),     //緊急連絡先電話番号(保護者)
                             "WNAME"              =>  VARS::post("WNAME")               //和暦名
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
        if (!($this->field["RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日 )");
            return false;
        }
        if (($this->field["RECEPTDATE"])) {
            $day = knjl011jQuery::CheckReceptDate($this);
            $sdate = str_replace("-", "/", $day["ABBV2"]);
            $edate = str_replace("-", "/", $day["ABBV3"]);
            if ($this->field["RECEPTDATE"] < $sdate || $this->field["RECEPTDATE"] > $edate) {
                $this->setWarning("MSG914", "出願期間内の日付を入力して下さい。( 受付日 )\\n出願期間：" .$sdate ."～" .$edate);
                return false;
            }
        }
        if (!($this->field["TESTDIV1"]) && !($this->field["TESTDIV2"]) && !($this->field["TESTDIV3"]) && !($this->field["TESTDIV4"]) && !($this->field["TESTDIV5"]) && !($this->field["TESTDIV6"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        $typeflg = false;
        for ($i = 1; $i < 7; $i++) {
            if (($this->field["TESTDIV" .$i]) && !($this->field["EXAM_TYPE" .$i])) {
                $typeflg = true;
            }
        }
        if ($typeflg) {
            $this->setWarning("MSG301", "( 受験型 )");
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
        //日付チェック
        if (strlen($this->field["ERACD"]) || strlen($this->field["BIRTH_Y"]) ||
            strlen($this->field["BIRTH_M"]) || strlen($this->field["BIRTH_D"]))
        {
            $day = knjl011jQuery::CheckDate($this);
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
        //存在チェック
        $row = knjl011jQuery::Existdata($this);
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
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
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
            $Row = knjl011jQuery::getFinschoolName($this->field["FS_CD"]);
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
        if (strlen($this->field["GFAXNO"]) > 14) {
            $this->setWarning("MSG915","保護者( FAX番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["RELATIONSHIP"]) > 2) {
            $this->setWarning("MSG915","保護者( 続柄 )は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["EMERGENCYCALL"]) > 30) {
            $this->setWarning("MSG915","保護者( 緊急連絡先名称 )は30バイトまでです。");
            return false;
        }
        if (strlen($this->field["EMERGENCYTELNO"]) > 14) {
            $this->setWarning("MSG915","保護者( 緊急連絡先電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","備考１は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 120) {
            $this->setWarning("MSG915","備考２は120バイトまでです。");
            return false;
        }
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011jQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011jQuery::getInsertUpdateQuery($this);
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
        $row = knjl011jQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl011jQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
    //追加処理
    function &getMaxExamno()
    {
        //最後の受験番号＋１
        $this->examno = knjl011jQuery::getMaxExamnoQuery($this->year);
        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }
        return;
    }

}
?>