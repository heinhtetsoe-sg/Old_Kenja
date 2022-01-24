<?php

require_once('for_php7.php');

class knjl011oModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $brother = array();  //兄弟受験画面

    function knjl011oModel()
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
                             "TESTDIV0"       =>  VARS::post("TESTDIV0"),           //入試区分0
                             "TESTDIV1"       =>  VARS::post("TESTDIV1"),           //入試区分1
                             "TESTDIV2"       =>  VARS::post("TESTDIV2"),           //入試区分2
                             "TESTDIV3"       =>  VARS::post("TESTDIV3"),           //入試区分3
                             "TESTDIV4"       =>  VARS::post("TESTDIV4"),           //入試区分4
                             "TESTDIV5"       =>  VARS::post("TESTDIV5"),           //入試区分5
                             "TESTDIV6"       =>  VARS::post("TESTDIV6"),           //入試区分6
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "ERACD"          =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"        =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"        =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"        =>  VARS::post("BIRTH_D"),            //生年月日日
                             "FS_CD"          =>  VARS::post("FS_CD"),              //出身学校コード
                             "FS_NAME"        =>  VARS::post("FS_NAME"),            //出身学校名
                             "FS_AREA_CD"     =>  VARS::post("FS_AREA_CD"),         //出身学校地区コード
                             "REMARK1"        =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"        =>  VARS::post("REMARK2"),            //備考２
                             "RECOM_EXAMNO1"  =>  VARS::post("RECOM_EXAMNO1"),      //重複受験番号
                             "RECOM_EXAMNO2"  =>  VARS::post("RECOM_EXAMNO2"),      //重複受験番号
                             "RECOM_EXAMNO3"  =>  VARS::post("RECOM_EXAMNO3"),      //重複受験番号
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
                             "PS_CD"          =>  VARS::post("PS_CD"),              //塾コード
                             "CLASS_ROOM"     =>  VARS::post("CLASS_ROOM"),         //教室名
                             "PS_CD2"         =>  VARS::post("PS_CD2"),             //塾コード2
                             "CLASS_ROOM2"    =>  VARS::post("CLASS_ROOM2")         //教室名2
                            );

        //兄弟受験画面
        $this->brother = array("BROTHER_EXAMNO"     =>  VARS::request("BROTHER_EXAMNO"),    //受験番号
                               "BROTHER_NAME"       =>  VARS::post("BROTHER_NAME"),         //受験生氏名
                               "BROTHER_SEQ"        =>  "005",                              //SEQ
                               "BROTHER_REMARK1"    =>  VARS::post("BROTHER_REMARK1"),      //第１志望
                               "BROTHER_REMARK2"    =>  VARS::post("BROTHER_REMARK2"),      //在校生氏名
                               "BROTHER_REMARK3"    =>  VARS::post("BROTHER_REMARK3"),      //在籍クラス
                               "BROTHER_REMARK4"    =>  VARS::post("BROTHER_REMARK4")       //併願予定校
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
        if (!strlen($this->field["TESTDIV0"]) && 
            !$this->field["TESTDIV1"] && 
            !$this->field["TESTDIV2"] && 
            !$this->field["TESTDIV3"] && 
            !$this->field["TESTDIV4"] && 
            !$this->field["TESTDIV5"] && 
            !$this->field["TESTDIV6"]) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        //同時受験不可チェック用 同じ入試日の入試区分は同時受験不可
        $testdivArray = array();
        for ($code = 0; $code <= 6; $code++) {
            if (strlen($this->field["TESTDIV".$code])) {
                $testdivArray[] = $code;
            }
        }
        $testDayCheck = knjl011oQuery::testDayCheck($this->year,$testdivArray);
        if (1 < get_count($testDayCheck)) {
            $msg  = "( 入試区分 )";
            $msg .= "\\n\\n「" .implode("」と「", $testDayCheck) ."」を同時に受験できません。\\nどちらかを選択してください。";
            $this->setWarning("MSG901", $msg);
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
        if (strlen($this->field["PS_CD"]) == 0 && strlen($this->field["CLASS_ROOM"]) != 0) {
            $this->setWarning("MSG301", "( 塾コード )");
            return false;
        }
        if (strlen($this->field["PS_CD"]) != 0) {
            $db = Query::dbCheckOut();
            $query = knjl011oQuery::getPrischoolcd($this->year, sprintf("%07d", $this->field["PS_CD"]));
            $priRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (!isset($priRow["PRISCHOOL_NAME"])) {
                $this->setWarning("MSG303", "( 塾コード )");
                return false;
            }
        }
        if (strlen($this->field["PS_CD2"]) == 0 && strlen($this->field["CLASS_ROOM2"]) != 0) {
            $this->setWarning("MSG301", "( 塾コード )");
            return false;
        }
        if (strlen($this->field["PS_CD2"]) != 0) {
            $db = Query::dbCheckOut();
            $query = knjl011oQuery::getPrischoolcd($this->year, sprintf("%07d", $this->field["PS_CD2"]));
            $priRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (!isset($priRow["PRISCHOOL_NAME"])) {
                $this->setWarning("MSG303", "( 塾コード )");
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
            $day = knjl011oQuery::CheckDate($this);
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
        $row = knjl011oQuery::Existdata($this);
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
        if (strlen($this->field["ADDRESS1"]) > 150) {
            $this->setWarning("MSG915","志願者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 150) {
            $this->setWarning("MSG915","志願者( 方書 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_NAME"]) > 45) {
            $this->setWarning("MSG915","出身学校名は45バイトまでです。");
            return false;
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
        if (strlen($this->field["GADDRESS1"]) > 150) {
            $this->setWarning("MSG915","保護者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 150) {
            $this->setWarning("MSG915","保護者( 方書 )は150バイトまでです。");
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
        if (strlen($this->field["PS_CD"]) > 7) {
            $this->setWarning("MSG915","塾コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["CLASS_ROOM"]) > 30) {
            $this->setWarning("MSG915","教室名は30バイトまでです。");
            return false;
        }
        if (strlen($this->field["PS_CD2"]) > 7) {
            $this->setWarning("MSG915","塾コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["CLASS_ROOM2"]) > 30) {
            $this->setWarning("MSG915","教室名は30バイトまでです。");
            return false;
        }
        
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011oQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011oQuery::getUpdateQuery($this);
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
        $row = knjl011oQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl011oQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }

    //入力項目チェック
    function validateInputBrother() {
        if (strlen($this->brother["BROTHER_REMARK2"]) > 60) {
            $this->setWarning("MSG915","在校生氏名は60バイトまでです。");
            return false;
        }
        if (strlen($this->brother["BROTHER_REMARK3"]) > 24) {
            $this->setWarning("MSG915","在籍クラスは24バイトまでです。");
            return false;
        }
        if (strlen($this->brother["BROTHER_REMARK4"]) > 92) {
            $this->setWarning("MSG915","併願予定校は92バイトまでです。");
            return false;
        }
        return true;
    }

    //更新処理(兄弟受験画面)
    function &getBrotherUpdateModel()
    {
        if ($this->validateInputBrother()) {
            $result = knjl011oQuery::getBrotherUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

}
?>