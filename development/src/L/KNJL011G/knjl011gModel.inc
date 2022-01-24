<?php

require_once('for_php7.php');

class knjl011gModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ
    public $isKeiai;
    public $isKasiwara;

    public function knjl011gModel()
    {
    }

    public function init()
    {
        //Ajax用
        $this->compatible();
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
                             "EXAMCOURSE"     =>  VARS::post("EXAMCOURSE"),         //第１志望
                             "SHDIV"          =>  VARS::post("SHDIV"),              //専併区分
                             "EXAMCOURSE2"    =>  VARS::post("EXAMCOURSE2"),        //第２志望
                             "SHDIV2"         =>  VARS::post("SHDIV2"),             //専併区分
                             "RECEPTDATE"     =>  VARS::post("RECEPTDATE"),         //受付日付
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

                             "JIZEN_SOUDAN_CD"          =>  VARS::post("JIZEN_SOUDAN_CD"),          //事前相談
                             "JIZEN_SOUDAN_TEXT"        =>  VARS::post("JIZEN_SOUDAN_TEXT"),        //その他
                             "CLUB_CD"                  =>  VARS::post("CLUB_CD"),                  //クラブ名
                             "CLUB_RANK"                =>  VARS::post("CLUB_RANK"),                //クラブランク
                             "HOPE_CLUB_CD"             =>  VARS::post("HOPE_CLUB_CD"),                  //クラブ名
                             "SINZOKU_NAME"             =>  VARS::post("SINZOKU_NAME"),             //親族氏名
                             "SINZOKU_KYUSEI"           =>  VARS::post("SINZOKU_KYUSEI"),           //親族旧姓
                             "SINZOKU_ZOKUGARA"         =>  VARS::post("SINZOKU_ZOKUGARA"),         //親族続柄
                             "SINZOKU_GAKKOUMEI"        =>  VARS::post("SINZOKU_GAKKOUMEI"),        //親族学校名
                             "SINZOKU_GAKKA"            =>  VARS::post("SINZOKU_GAKKA"),            //親族学科・科
                             "SINZOKU_ZAI_GRD_YEAR"     =>  VARS::post("SINZOKU_ZAI_GRD_YEAR"),     //親族在学・卒業年度
                             "SINZOKU_ZAI_GRD_FLG"      =>  VARS::post("SINZOKU_ZAI_GRD_FLG"),      //親族在学・卒業フラグ
                             "SINZOKU_FUTAGO_NAME"      =>  VARS::post("SINZOKU_FUTAGO_NAME"),      //双生児氏名
                             "SINZOKU_FUTAGO_ZOKUGARA"  =>  VARS::post("SINZOKU_FUTAGO_ZOKUGARA"),  //双生児続柄
                             "INTERVIEW_VALUE"          =>  VARS::post("INTERVIEW_VALUE"),          //面接評価
                             "INTERVIEW_REMARK"         =>  VARS::post("INTERVIEW_REMARK"),         //面接情報
                             "REMARK1"                  =>  VARS::post("REMARK1")                   //監督者情報
                            );

        //学校コードを取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjl011gQuery::getNameMstZ010();
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";
    }

    //追加処理
    public function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl011gQuery::getMaxExamnoQuery($this);
        return;
    }

    //受験番号範囲チェック
    public function checkNumber()
    {
        $check = array();

        if ($this->isKasiwara == "1") {
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "3000", "MIN_NO" => "0001", "MAX_NO" => "0199"); //キャリアアシスト
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0201", "MAX_NO" => "0599"); //キャリアアシスト以外
            $check[] = array("TESTDIV" => "2", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0601", "MAX_NO" => "0699");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0701", "MAX_NO" => "0799");
            $check[] = array("TESTDIV" => "1", "SHDIV" => "2", "EXAMCOURSE" => "NO",   "MIN_NO" => "1001", "MAX_NO" => "2999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "2", "EXAMCOURSE" => "NO",   "MIN_NO" => "3001", "MAX_NO" => "3999");

            $course_array = array();
            $course_array = explode("-", $this->field["EXAMCOURSE"]);

            foreach ($check as $key => $val) {
                if ($this->field["TESTDIV"] == $val["TESTDIV"] && $this->field["SHDIV"] == $val["SHDIV"] && ($course_array[2] == $val["EXAMCOURSE"] || "NO" == $val["EXAMCOURSE"])) {
                    if ($this->examno >= $val["MIN_NO"] && $this->examno <= $val["MAX_NO"]) {
                        break;
                    } else {
                        $msg = " (".$val["MIN_NO"]."～".$val["MAX_NO"].")";
                        return $msg;
                    }
                }
            }
        } else {
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "MIN_NO" => "1001", "MAX_NO" => "1999", "MIN_NO2" => "4001", "MAX_NO2" => "4999");
            $check[] = array("TESTDIV" => "1", "SHDIV" => "2", "MIN_NO" => "2001", "MAX_NO" => "2999", "MIN_NO2" => "5001", "MAX_NO2" => "5999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "1", "MIN_NO" => "6001", "MAX_NO" => "6999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "2", "MIN_NO" => "7001", "MAX_NO" => "7999");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "1", "MIN_NO" => "8001", "MAX_NO" => "8999");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "2", "MIN_NO" => "8001", "MAX_NO" => "8999");

            foreach ($check as $key => $val) {
                if ($this->field["TESTDIV"] == $val["TESTDIV"] && $this->field["SHDIV"] == $val["SHDIV"]) {
                    if ($this->examno >= $val["MIN_NO"] && $this->examno <= $val["MAX_NO"]) {
                        break;
                    } elseif (strlen($val["MIN_NO2"]) && $this->examno >= $val["MIN_NO2"] && $this->examno <= $val["MAX_NO2"]) {
                        break;
                    } else {
                        $msg = " (".$val["MIN_NO"]."～".$val["MAX_NO"].")";
                        if (strlen($val["MIN_NO2"])) {
                            $msg .= " または (".$val["MIN_NO2"]."～".$val["MAX_NO2"].")";
                        }
                        return $msg;
                    }
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
            $this->setWarning("MSG301", "( 第１志望 )");
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

        //受験番号範囲チェック
        $msg = $this->checkNumber();
        if ($msg != "") {
            $this->setWarning("MSG916", "( 受験番号:{$this->examno} )".$msg);
            return false;
        }

        //存在チェック
        $query = knjl011gQuery::existdata($this);
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
        if (strlen($this->field["INTERVIEW_REMARK"]) > 90) {
            $this->setWarning("MSG915", "面接情報は90バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 90) {
            $this->setWarning("MSG915", "監督者情報は90バイトまでです。");
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
            $result = knjl011gQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011gQuery::getUpdateQuery($this);
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
        //DB接続
        $db = Query::dbCheckOut();
        $query = knjl011gQuery::existdata($this);
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
        knjl011gQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }

    //Ajax用
    public function compatible()
    {
        if (!function_exists("json_encode")) {
            function json_encode($param)
            {
                require_once("JSON.php");
                $json = new Services_JSON();
                return $json->encode($param);
            }
            function json_decode($param, $assoc = false)
            {
                require_once("JSON.php");
                $json = new Services_JSON($assoc ? SERVICES_JSON_LOOSE_TYPE : 0);
                return $json->decode($param);
            }
        }
    }
}
