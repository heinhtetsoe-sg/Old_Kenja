<?php

require_once('for_php7.php');

class knjl510fModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl510fModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->fubanMukou);
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
        $this->field = array("APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),       //入試制度
                             "RECEPTDATE"     =>  VARS::post("RECEPTDATE"),         //受付日付
                             "SHDIV"          =>  VARS::post("SHDIV"),              //専併区分
                             "EXAMCOURSE"     =>  VARS::post("EXAMCOURSE"),         //志望区分
                             "EXAMCOURSE2"    =>  VARS::post("EXAMCOURSE2"),        //志望区分2
                             "EIKEN_SHUTOKU_KYUU"   =>  VARS::post("EIKEN_SHUTOKU_KYUU"),   //英検取得級
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

                             "SHOUGAKU"       =>  VARS::post("SHOUGAKU"),           //奨学区分
                             "TOKU_KATU"      =>  VARS::post("TOKU_KATU"),          //特別活動
                             "BIKOU"          =>  VARS::post("BIKOU"),              //備考

                             "SOUDAN"         =>  VARS::post("SOUDAN")              //教育相談
                            );

        //プレテスト得点
        for ($kai = 1; $kai <= 2; $kai++) {
            $this->field["PRE_KOKUGO".$kai] = VARS::post("PRE_KOKUGO".$kai);
            $this->field["PRE_SANSUU".$kai] = VARS::post("PRE_SANSUU".$kai);
            $this->field["PRE_EIGO".$kai]   = VARS::post("PRE_EIGO".$kai);
        }

        //010:入試区分,011:受験型,012:受験番号
        $this->testdivArray = knjl510fQuery::getTestdivArray($this);
        foreach ($this->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            $this->field["TESTDIV".$testdiv]    = VARS::post("TESTDIV".$testdiv);
            $this->field["EXAM_TYPE".$testdiv]  = VARS::post("EXAM_TYPE".$testdiv);
            $this->field["RECEPTNO".$testdiv]   = VARS::post("RECEPTNO".$testdiv);
        }
    }

    //追加処理
    function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl510fQuery::getMaxExamnoQuery($this);
        //MAX更新
        knjl510fQuery::insEntexamKanriNoDat($this, $this->examno);
        return;
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 管理番号 )");
            return false;
        }
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        $testFlg = false;
        foreach ($this->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            if ($this->field["TESTDIV".$testdiv]) {
                $testFlg = true;
                if (!$this->field["EXAM_TYPE".$testdiv]) {
                    $this->setWarning("MSG301", "( 受験科目 )");
                    return false;
                }
                if (!$this->field["RECEPTNO".$testdiv]) {
                    $this->setWarning("MSG301", "( 受験番号 )");
                    return false;
                }
                //受験番号範囲チェック
                $exam_type = $this->field["EXAM_TYPE".$testdiv];
                $receptno = $this->field["RECEPTNO".$testdiv];
                if (0 == $db->getOne(knjl510fQuery::getEntexamNoDatCnt($this, $testdiv, $exam_type, $receptno))) {
                    $this->setWarning("MSG305", "( 受験番号:{$receptno} )\\n受験番号範囲マスタが未設定です。");
                    return false;
                }
                //受験番号重複チェック
                $query = knjl510fQuery::checkNumber($this, $testdiv, $receptno);
                if (0 < $db->getOne($query)) {
                    $this->setWarning("MSG302", "( 受験番号:{$receptno} )");
                    return false;
                }
            }
        }
        if (!$testFlg) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }
        if (!($this->field["RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日付 )");
            return false;
        }
        if (!($this->field["SHDIV"])) {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        if (!($this->field["EXAMCOURSE"])) {
            $this->setWarning("MSG301", "( 第一志望コース )");
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
            $this->setWarning("MSG301", "( 志願者 性別 )");
            return false;
        }
        if (!($this->field["GNAME"])) {
            $this->setWarning("MSG301", "( 保護者 氏名 )");
            return false;
        }
        if (!($this->field["GKANA"])) {
            $this->setWarning("MSG301", "( 保護者 氏名かな )");
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

        //存在チェック
        $query = knjl510fQuery::Existdata($this);
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
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","管理番号は5バイトまでです。");
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
            $this->setWarning("MSG915","保護者( 氏名かな )は120バイトまでです。");
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
        if (strlen($this->field["TOKU_KATU"]) > 120) {
            $this->setWarning("MSG915","( 特別活動 )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIKOU"]) > 120) {
            $this->setWarning("MSG915","( 備考 )は120バイトまでです。");
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
            $result = knjl510fQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl510fQuery::getUpdateQuery($this);
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
        $query = knjl510fQuery::Existdata($this);
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
        knjl510fQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>