<?php

require_once('for_php7.php');

class knjl211yModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $attend = array();  //欠席画面用

    function knjl211yModel()
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

        //欠席画面用
        $this->attend["ATTEND_APPLICANTDIV"] = VARS::request("ATTEND_APPLICANTDIV");
        $this->attend["ATTEND_EXAMNO"]       = VARS::request("ATTEND_EXAMNO");
        for ($testdiv = 1; $testdiv <= 3; $testdiv++) {
            $name = "ATTEND_FLG" .$testdiv;
            $this->attend[$name] = VARS::post($name);
            $name = "ATTEND_REASON" .$testdiv;
            $this->attend[$name] = VARS::post($name);
        }

        if (VARS::get("SEND_EXAMNO")) {
            $this->examno = VARS::get("SEND_EXAMNO");
        }

        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }

        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV"        =>  VARS::post("TESTDIV"),            //入試区分(出願１回目)
                             "GENERAL_FLG"    =>  VARS::post("GENERAL_FLG"),        //再受験フラグ
                             "GENERAL_TESTDIV" =>  VARS::post("GENERAL_TESTDIV"),     //入試区分(出願２回目)(再受験)
                             "JUDGEDIV"       =>  VARS::post("JUDGEDIV"),           //入試区分(出願１回目)合否
                             "JUDGEDIV_NAME"  =>  VARS::post("JUDGEDIV_NAME"),      //入試区分(出願１回目)合否
                             //志願者
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
                             //特徴
                             "CHARACTER_REMARK1"    =>  VARS::post("CHARACTER_REMARK1"),    //志願理由
                             "CHARACTER_REMARK2"    =>  VARS::post("CHARACTER_REMARK2"),    //ご家庭で大切にしていること
                             "CHARACTER_REMARK3"    =>  VARS::post("CHARACTER_REMARK3"),    //本人の長所
                             "CHARACTER_REMARK4"    =>  VARS::post("CHARACTER_REMARK4"),    //本人の短所
                             //保護者
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
                             "RELATIONSHIP"   =>  VARS::post("RELATIONSHIP"),       //続柄(保護者)
                             "WNAME"          =>  VARS::post("WNAME")               //生年月日元号・和暦名
                            );

        $this->getPropertiesAll();
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
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (strlen($this->field["TESTDIV"]) && strlen($this->field["JUDGEDIV"])) {
            $row = knjl211yQuery::ExistdataTestdiv($this);
            if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
                $this->setWarning("MSG901", "( 入試区分 )\\n合否がある入試は変更できません。");
                return false;
            }
        }
        //連続２回まで
        if (strlen($this->field["TESTDIV"]) && strlen($this->field["GENERAL_FLG"]) && strlen($this->field["GENERAL_TESTDIV"])) {
            $general_testdiv = (int) $this->field["TESTDIV"] + 1;
            if ($this->field["GENERAL_TESTDIV"] != $general_testdiv && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
                $this->setWarning("MSG901", "( 再受験 )\\n入試は連続２回までです。");
                return false;
            }
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
        $row = knjl211yQuery::Existdata($this);
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
        if (strlen($this->field["ADDRESS1"]) > 75) {
            $this->setWarning("MSG915","志願者( 住所 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 75) {
            $this->setWarning("MSG915","志願者( 方書 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身幼稚園等コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_NAME"]) > 75) {
            $this->field["FS_NAME"] = substr($this->field["FS_NAME"],0,75);
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
        if (strlen($this->field["RELATIONSHIP"]) > 2) {
            $this->setWarning("MSG915","保護者( 続柄 )は2バイトまでです。");
            return false;
        }
        //特徴
        if ($this->validate_row_cnt($this->field["CHARACTER_REMARK1"], (40 * 2)) > 6) {
            $this->setWarning("MSG915","(志願理由は6行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CHARACTER_REMARK2"], (40 * 2)) > 6) {
            $this->setWarning("MSG915","(ご家庭で大切にしていることは6行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CHARACTER_REMARK3"], (40 * 2)) > 3) {
            $this->setWarning("MSG915","(本人の長所は3行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["CHARACTER_REMARK4"], (40 * 2)) > 3) {
            $this->setWarning("MSG915","(本人の短所は3行までです)");
            return false;
        }
        //重複データチェック(同じ受験番号が違う入試制度に在る場合、重複エラー）
        $existdata = knjl211yQuery::CheckData($this);
        if (is_array($existdata)) {
            $msg  = "この受験番号は違う入試制度に既に登録されています。　　\\n\\n";
            $msg .= "受験番号 ： ".$existdata["EXAMNO"]."\\n";
            $msg .= "入試制度 ： ".$existdata["NAME1"];
            $this->setWarning("MSG302", $msg);
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl211yQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl211yQuery::getInsertUpdateQuery($this);
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
        $row = knjl211yQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl211yQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
    //追加処理
    function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl211yQuery::getMaxExamnoQuery($this);
        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }
        return;
    }

    //更新処理(欠席フラグ)
    function &getAttendUpdateModel()
    {
//        if ($this->validateInput()) {
            $result = knjl211yQuery::getAttendUpdateQuery($this);
            $this->setMessage("MSG201");
//        }
        return;
    }

}
?>