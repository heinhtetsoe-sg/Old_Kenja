<?php

require_once('for_php7.php');

class knjl011yModel extends Model
{

    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ
    public $attend = array();  //欠席画面用

    public function knjl011yModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->examno       = VARS::post("EXAMNO");
        $this->exam_type    = '1';

        //欠席画面用
        $this->attend["ATTEND_APPLICANTDIV"] = VARS::request("ATTEND_APPLICANTDIV");
        $this->attend["ATTEND_EXAMNO"]       = VARS::request("ATTEND_EXAMNO");
        $testdivMax = ($this->attend["ATTEND_APPLICANTDIV"] == "2") ? 3 : 5;
        for ($testdiv = 1; $testdiv <= $testdivMax; $testdiv++) {
            $name = "ATTEND_FLG" .$testdiv;
            $this->attend[$name] = VARS::post($name);
            $name = "ATTEND_REASON" .$testdiv;
            $this->attend[$name] = VARS::post($name);
        }

        //一般入試希望の入試区分
        $this->general_testdiv = "3";
        $this->general_testdiv2 = "4";
        $this->general_testdiv3 = "5"; //適性検査型

        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d", $this->examno);
        }

        //フィールド
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "TESTDIV"        =>  VARS::post("TESTDIV"),            //入試区分
                             "DESIREDIV"      =>  VARS::post("DESIREDIV"),          //志望区分
                             "RECOM_KIND"     =>  VARS::post("RECOM_KIND"),         //推薦区分
                             "SHDIV"          =>  VARS::post("SHDIV"),              //専併区分
                             "SH_SCHOOLCD"    =>  VARS::post("SH_SCHOOLCD"),        //併願校コード（高校コード）
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "ERACD"          =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"        =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"        =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"        =>  VARS::post("BIRTH_D"),            //生年月日日
                             "BIRTHDAY"       =>  VARS::post("BIRTHDAY"),           //生年月日（西暦）
                             "FS_CD"          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_NAME"        =>  VARS::post("FS_NAME"),            //出身学校名
                             "FS_ERACD"       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"           =>  VARS::post("FS_M"),               //卒業月
                             "FS_GRDYEAR"     =>  VARS::post("FS_GRDYEAR"),         //卒業年(西暦)
                             "FS_GRDDIV"      =>  VARS::post("FS_GRDDIV"),          //卒業区分
                             "REMARK1"        =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"        =>  VARS::post("REMARK2"),            //備考２
                             "JUDGEDIV"       =>  VARS::post("JUDGEDIV"),           //一般入試希望可能条件(不合格)
                             "INTERVIEW_ATTEND_FLG"  =>  VARS::post("INTERVIEW_ATTEND_FLG"),      //帰国生
                             "SELECT_SUBCLASS_DIV"  =>  VARS::post("SELECT_SUBCLASS_DIV"),  //特進チャレンジ
                             "GENERAL_FLG"    =>  VARS::post("GENERAL_FLG"),        //一般入試希望
                             "GENERAL_FLG2"   =>  VARS::post("GENERAL_FLG2"),       //一般入試希望2
                             "GENERAL_FLG3"   =>  VARS::post("GENERAL_FLG3"),       //一般入試希望3
                             "SLIDE_FLG"      =>  VARS::post("SLIDE_FLG"),          //スライド希望
                             "SHIFT_DESIRE_FLG" =>  VARS::post("SHIFT_DESIRE_FLG"), //特別判定希望
                             "SPORTS_FLG"     =>  VARS::post("SPORTS_FLG"),         //スポ優秀希望
                             "SPORTS_FLG2"    =>  VARS::post("SPORTS_FLG2"),        //T特奨希望
                             "DORMITORY_FLG"  =>  VARS::post("DORMITORY_FLG"),      //入寮希望
                             "GENERAL_DESIREDIV"  =>  VARS::post("GENERAL_DESIREDIV"),  //一般入試志望区分
                             "GENERAL_SHDIV"      =>  VARS::post("GENERAL_SHDIV"),      //一般入試専併区分
                             "GENERAL_DESIREDIV2" =>  VARS::post("GENERAL_DESIREDIV2"), //一般入試志望区分2
                             "GENERAL_SHDIV2"     =>  VARS::post("GENERAL_SHDIV2"),     //一般入試専併区分2
                             "GENERAL_DESIREDIV3" =>  VARS::post("GENERAL_DESIREDIV3"), //一般入試志望区分3
                             "GENERAL_SHDIV3"     =>  VARS::post("GENERAL_SHDIV3"),     //一般入試専併区分3
                             "SLIDE_DESIREDIV"   =>  VARS::post("SLIDE_DESIREDIV"),   //スライド志望区分
                             "RECOM_ITEM1"    =>  VARS::post("RECOM_ITEM1"),        //推薦事項(1)
                             "RECOM_ITEM2"    =>  VARS::post("RECOM_ITEM2"),        //推薦事項(2)
                             "RECOM_ITEM3"    =>  VARS::post("RECOM_ITEM3"),        //推薦事項(3)
                             "RECOM_ITEM4"    =>  VARS::post("RECOM_ITEM4"),        //推薦事項(4)
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
        $this->field["AVERAGE5"]  = VARS::post("AVERAGE5");
        $this->field["ABSENCE_DAYS1"] = VARS::post("ABSENCE_DAYS1");
        $this->field["ABSENCE_DAYS2"] = VARS::post("ABSENCE_DAYS2");
        $this->field["ABSENCE_DAYS3"] = VARS::post("ABSENCE_DAYS3");
        $this->getPropertiesAll();
    }


    //入力項目チェック
    public function validateInput()
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
        if (!($this->field["DESIREDIV"])) {
            $this->setWarning("MSG301", "( 志望区分 )");
            return false;
        }
        if (!($this->field["SHDIV"])) {
            $this->setWarning("MSG301", "( 専併区分 )");
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
        //一般入試希望チェック(高校は選択させる)
        if (strlen($this->field["GENERAL_FLG"]) && $this->field["APPLICANTDIV"] == "2") {
            if (!strlen($this->field["GENERAL_DESIREDIV"])) {
                $this->setWarning("MSG301", "( 一般入試希望の志望区分 )");
                return false;
            }
        }
        //第二回一般希望チェック(中学はDESIREDIV,SHDIVをセットする)
        if (strlen($this->field["GENERAL_FLG"]) && $this->field["APPLICANTDIV"] == "1") {
            if (!strlen($this->field["GENERAL_DESIREDIV"]) || !strlen($this->field["GENERAL_SHDIV"])) {
                $this->field["GENERAL_DESIREDIV"]   = $this->field["DESIREDIV"];
                $this->field["GENERAL_SHDIV"]       = $this->field["SHDIV"];
            }
        }
        //一般入試希望チェック(高校は選択させる)
        if (strlen($this->field["GENERAL_FLG2"]) && $this->field["APPLICANTDIV"] == "2") {
            if (!strlen($this->field["GENERAL_DESIREDIV2"])) {
                $this->setWarning("MSG301", "( 一般入試希望の志望区分 )");
                return false;
            }
        }
        //第三回一般希望チェック(中学はDESIREDIV,SHDIVをセットする)
        if (strlen($this->field["GENERAL_FLG2"]) && $this->field["APPLICANTDIV"] == "1") {
            if (!strlen($this->field["GENERAL_DESIREDIV2"]) || !strlen($this->field["GENERAL_SHDIV2"])) {
                $this->field["GENERAL_DESIREDIV2"]   = $this->field["DESIREDIV"];
                $this->field["GENERAL_SHDIV2"]       = $this->field["SHDIV"];
            }
        }
        //適性検査型希望チェック(中学はDESIREDIV,SHDIVをセットする)
        if (strlen($this->field["GENERAL_FLG3"]) && $this->field["APPLICANTDIV"] == "1") {
            if (!strlen($this->field["GENERAL_DESIREDIV3"]) || !strlen($this->field["GENERAL_SHDIV3"])) {
                $this->field["GENERAL_DESIREDIV3"]   = $this->field["DESIREDIV"];
                $this->field["GENERAL_SHDIV3"]       = $this->field["SHDIV"];
            }
        }
        //第二回一般と適性検査型希望チェック(どちらか１つを選択)
        if (strlen($this->field["GENERAL_FLG"]) && strlen($this->field["GENERAL_FLG3"]) && $this->field["APPLICANTDIV"] == "1") {
            $this->setWarning("MSG901", "( 希望欄 )\\n【第二回一般】と【適性検査型】の両方は選択できません。");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //卒業年月チェック
        if (strlen($this->field["FS_GRDYEAR"]) || strlen($this->field["FS_M"]) || strlen($this->field["FS_GRDDIV"])) {
            if (!strlen($this->field["FS_GRDYEAR"]) || !strlen($this->field["FS_M"])) {
                $this->setWarning("MSG301", "( 卒業年月 )");
                return false;
            }
            if (!strlen($this->field["FS_GRDDIV"])) {
                $this->setWarning("MSG301", "( 卒業年月 )( 卒業区分 )");
                return false;
            }
        }
        //存在チェック
        $row = knjl011yQuery::Existdata($this);
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
            $this->setWarning("MSG915", "受験番号は5バイトまでです。");
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
        if (strlen($this->field["ADDRESS1"]) > 75) {
            $this->setWarning("MSG915", "志願者( 住所 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 75) {
            $this->setWarning("MSG915", "志願者( 方書 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915", "出身学校コードは7バイトまでです。");
            return false;
        }
        $this->field["FS_NAME"] = "";
        if (strlen($this->field["FS_CD"])) {
            $Row = knjl011yQuery::getFinschoolName($this->field["FS_CD"]);
            $this->field["FS_NAME"] = $Row["FINSCHOOL_NAME"];
        }
        if (strlen($this->field["FS_NAME"]) > 45) {
            $this->field["FS_NAME"] = substr($this->field["FS_NAME"], 0, 45);
        }
        if (strlen($this->field["SH_SCHOOLCD"]) > 7) {
            $this->setWarning("MSG915", "併願校コードは7バイトまでです。");
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
        if (strlen($this->field["GADDRESS1"]) > 75) {
            $this->setWarning("MSG915", "保護者( 住所 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["GADDRESS2"]) > 75) {
            $this->setWarning("MSG915", "保護者( 方書 )は75バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["RELATIONSHIP"]) > 2) {
            $this->setWarning("MSG915", "保護者( 続柄 )は2バイトまでです。");
            return false;
        }
        /*if (strlen($this->field["REMARK1"]) > 246) {
            $this->setWarning("MSG915","備考１は246バイトまでです。");
            return false;
        }*/
        if ($this->validate_row_cnt($this->field["REMARK1"], (20 * 2)) > 4) {
            $this->setWarning("MSG915", "(備考１は4行までです)");
            return false;
        }
        /*if (strlen($this->field["REMARK2"]) > 122) {
            $this->setWarning("MSG915","備考２は122バイトまでです。");
            return false;
        }*/
        if ($this->validate_row_cnt($this->field["REMARK2"], (20 * 2)) > 2) {
            $this->setWarning("MSG915", "(備考２は2行までです)");
            return false;
        }
        //重複データチェック(同じ受験番号が違う入試制度に在る場合、重複エラー）
        $existdata = knjl011yQuery::CheckData($this);
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
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl011yQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011yQuery::getInsertUpdateQuery($this);
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
        $row = knjl011yQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        knjl011yQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }

    //追加処理
    public function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl011yQuery::getMaxExamnoQuery($this);
        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d", $this->examno);
        }
        return;
    }

    //更新処理(欠席フラグ)
    public function &getAttendUpdateModel()
    {
//        if ($this->validateInput()) {
            $result = knjl011yQuery::getAttendUpdateQuery($this);
            $this->setMessage("MSG201");
//        }
        return;
    }
}