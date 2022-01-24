<?php

require_once('for_php7.php');

class knjl016cModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ
    var $isGojou;
    var $sdfName;

    function knjl016cModel()
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
                             "TESTDIV"        =>  VARS::post("TESTDIV"),            //入試区分
                             "SHDIV"          =>  VARS::post("SHDIV"),              //専併区分
                             "SH_SCHOOLCD"    =>  VARS::post("SH_SCHOOLCD"),        //併願校コード（高校コード）
                             "SHIFT_DESIRE_FLG" => VARS::post("SHIFT_DESIRE_FLG"),  //カレッジ併願
                             "SELECT_SUBCLASS_DIV" => VARS::post("SELECT_SUBCLASS_DIV"),  //選択受験科目
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
                             "SEX"            =>  VARS::post("SEX"),                //性別
                             "ERACD"          =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"        =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"        =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"        =>  VARS::post("BIRTH_D"),            //生年月日日
                             "BIRTHDAY"       =>  VARS::post("BIRTHDAY"),           //生年月日（西暦）
                             "FS_NATPUBPRIDIV" => VARS::post("FS_NATPUBPRIDIV"),    //国公私立
                             "FS_CD"          =>  VARS::post("FINSCHOOLCD"),        //出身学校コード
                             "FS_NAME"        =>  VARS::post("FINSCHOOLNAME"),      //出身学校名
                             "FS_AREA_CD"     =>  VARS::post("FS_AREA_CD"),         //出身学校地区コード
                             "FS_ERACD"       =>  VARS::post("FS_ERACD"),           //卒業元号
                             "FS_WNAME"       =>  VARS::post("FS_WNAME"),           //卒業元号・和暦名
                             "FS_Y"           =>  VARS::post("FS_Y"),               //卒業年
                             "FS_M"           =>  VARS::post("FS_M"),               //卒業月
                             "PICTURE_ERACD"  =>  VARS::post("PICTURE_ERACD"),      //撮影元号
                             "PICTURE_WNAME"  =>  VARS::post("PICTURE_WNAME"),      //撮影元号・和暦名
                             "PICTURE_Y"      =>  VARS::post("PICTURE_Y"),          //撮影年
                             "PICTURE_M"      =>  VARS::post("PICTURE_M"),          //撮影月
                             "PRISCHOOLCD"    =>  VARS::post("PRISCHOOLCD"),        //塾コード
                             "REMARK1"        =>  VARS::post("REMARK1"),            //備考１
                             "REMARK2"        =>  VARS::post("REMARK2"),            //備考２
                             "RECOM_EXAMNO"   =>  VARS::post("RECOM_EXAMNO"),       //受験番号
                             "BUS_USE"          =>  VARS::post("BUS_USE"),          //学園バス利用フラグ
                             "STATIONDIV"       =>  VARS::post("STATIONDIV"),       //乗降地
                             "BUS_USER_COUNT"   =>  VARS::post("BUS_USER_COUNT"),   //ご利用人数
                             "PRE_RECEPTNO"     =>  VARS::post("PRE_RECEPTNO"),     //ﾌﾟﾚﾃｽﾄ受験番号
                             "DORMITORY_FLG"    =>  VARS::post("DORMITORY_FLG"),    //入寮フラグ
                             "SLIDE_FLG"        =>  VARS::post("SLIDE_FLG"),        //特進併願希望フラグ
                             "SPORTS_FLG"       =>  VARS::post("SPORTS_FLG"),       //S組フラグ
                             "GENERAL_FLG"      =>  VARS::post("GENERAL_FLG"),      //スポーツコースコンボ
                             "PERSONAL_FLG"     =>  VARS::post("PERSONAL_FLG"),     //個人成績希望フラグ
                             "FAMILY_REGISTER"  =>  VARS::post("FAMILY_REGISTER"),  //都道府県(本籍地)
                             "PREF_CD"          =>  VARS::post("PREF_CD"),          //都道府県(志願者)
                             "GPREF_CD"         =>  VARS::post("GPREF_CD"),         //都道府県(保護者)
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
                             "EDBOARDCD"      =>  VARS::post("EDBOARDCD"),          //所在地教育委員会
                             "WNAME"          =>  VARS::post("WNAME")               //生年月日元号・和暦名
                            );
        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl016cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" ? true : false;
        $this->sdfName = ($this->isGojou) ? "カレッジ併願" : "移行希望";
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
        if ($this->field["TESTDIV"] == "1" || $this->field["TESTDIV"] == "2" || $this->field["TESTDIV"] == "6" || $this->field["TESTDIV"] == "7") {
            if ($this->field["APPLICANTDIV"] == "2") {
                $this->setWarning("MSG901", "( 入試制度または入試区分 )");
                return false;
            }
        } else {
            if ($this->field["APPLICANTDIV"] == "1") {
                $this->setWarning("MSG901", "( 入試制度または入試区分 )");
                return false;
            }
        }
        if (!($this->field["SHDIV"])) {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if ($this->field["SHDIV"] == "2" && $this->field["TESTDIV"] != "3" && !$this->isGojou) {
            $this->setWarning("MSG901", "併願は、高校・編入コースのみ選択可能です。( 専併区分 )");
            return false;
        }
        //選択受験科目チェック
        $cnt = knjl016cQuery::getExistSelectSubclass($this);
        if (strlen($this->field["SELECT_SUBCLASS_DIV"]) == 0 && 0 < $cnt) {
            $this->setWarning("MSG310", "( 選択受験科目 )");
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
        //学園バスを利用する時、利用人数は必須
        if (strlen($this->field["BUS_USE"]) && !strlen($this->field["BUS_USER_COUNT"]) && $this->isGojou) {
            $this->setWarning("MSG301", "( 利用人数 )\\n学園バスを利用する時、利用人数は必須です。");
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
            $day = knjl016cQuery::CheckDate($this);
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
        $row = knjl016cQuery::Existdata($this);
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
            $Row = knjl016cQuery::getFinschoolName($this->field["FS_CD"]);
            $this->field["FS_NAME"] = $Row["FINSCHOOL_NAME"];
            $this->field["FS_AREA_CD"] = $Row["DISTRICTCD"];
        }
        if (strlen($this->field["FS_NAME"]) > 45) {
            $this->field["FS_NAME"] = substr($this->field["FS_NAME"],0,45);
        }
        if (strlen($this->field["SH_SCHOOLCD"]) > 7) {
            $this->setWarning("MSG915","併願校コードは7バイトまでです。");
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
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","兄弟情報は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 120) {
            $this->setWarning("MSG915","備考は120バイトまでです。");
            return false;
        }
        //重複受験番号チェック
        if ($this->field["APPLICANTDIV"] != "1" && strlen($this->field["RECOM_EXAMNO"])) {
            $this->setWarning("MSG901","入力可能なのは、中学のみです。（重複受験番号）");
            return false;
        }
/***
        //重複受験番号の存在チェック
        if (strlen($this->field["RECOM_EXAMNO"])) {
            //存在チェック
            $testdiv = ($this->field["TESTDIV"] == "1") ? "2" : "1";
            $retCnt = knjl016cQuery::checkRecomExamno($this->year, $testdiv, $this->field["RECOM_EXAMNO"]);
            if (0 < $retCnt) {
                //重複チェック
                $retCnt2 = knjl016cQuery::checkRecomExamno2($this->year, $this->field["TESTDIV"], $this->field["RECOM_EXAMNO"], $this->examno);
                if (0 < $retCnt2) {
                    $this->setWarning("MSG901", "入力された値は既に登録されています。（重複受験番号）");
                    return false;
                }
            } else {
                $this->setWarning("MSG901", "入力された値のデータは存在しません。（重複受験番号）");
                return false;
            }
        }
***/
        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl016cQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl016cQuery::getInsertUpdateQuery($this);
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
        $row = knjl016cQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl016cQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
    //追加処理
    function &getMaxExamno()
    {
        //最後の受験番号＋１
        $this->examno = knjl016cQuery::getMaxExamnoQuery($this);
        if (strlen($this->examno)) {
            $this->examno = sprintf("%04d",$this->examno);
        }
        return;
    }

}
?>