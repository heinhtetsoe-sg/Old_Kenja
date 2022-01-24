<?php

require_once('for_php7.php');

class knjl010fModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $checkexam;
    public $field = array();           //フィールドデータ

    public function knjl010fModel()
    {
    }

    public function init()
    {
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
                             "TEST_L_DIV"     =>  VARS::post("TEST_L_DIV"),         //入試区分大分類コンボ　1:帰国生入試 2:一般入試
                             "EXAMCOURSE"     =>  VARS::post("EXAMCOURSE"),         //志望区分
                             "RECEPTDATE"     =>  VARS::post("RECEPTDATE"),         //受付日付
                             "GANSHO_YUUSOU"  =>  VARS::post("GANSHO_YUUSOU"),      //願書郵送
                             "SPECIAL_REASON_DIV"   =>  VARS::post("SPECIAL_REASON_DIV"),         //特別措置者
                             "NAME"           =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"      =>  VARS::post("NAME_KANA"),          //氏名かな(志願者)
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

                             "EXAM_PAY_DIV"         =>  VARS::post("EXAM_PAY_DIV"),         //受験料・入金方法
                             "EXAM_PAY_DATE"        =>  VARS::post("EXAM_PAY_DATE"),        //受験料・入金日
                             "EXAM_PAY_CHAK_DATE"   =>  VARS::post("EXAM_PAY_CHAK_DATE"),   //受験料・着金日
                             "TOKU_TEST_FLG"        =>  VARS::post("TOKU_TEST_FLG"),        //特別入試対象者
                             "RECRUIT_NO"           =>  VARS::post("RECRUIT_NO"),           //事前番号

                             "MOTHER_NAME"          =>  VARS::post("MOTHER_NAME"),          //校友会卒業時氏名
                             "MOTHER_NENDO"         =>  VARS::post("MOTHER_NENDO"),         //校友会卒業年度
                             "MOTHER_HR_CLASS"      =>  VARS::post("MOTHER_HR_CLASS"),      //校友会卒業クラス
                             "MOTHER_BIRTHDAY"      =>  VARS::post("MOTHER_BIRTHDAY"),      //校友会生年月日
                             "KOUYUU_DIV"           =>  VARS::post("KOUYUU_DIV"),           //校友会関係
                             "KOUYUU_SCHOOL_KIND"   =>  VARS::post("KOUYUU_SCHOOL_KIND"),   //校友会中高
                             "KOUYUU_COLLEGE"       =>  VARS::post("KOUYUU_COLLEGE"),       //校友会大学・学部
                             "EIKEN_SHUTOKU_KYUU"   =>  VARS::post("EIKEN_SHUTOKU_KYUU"),   //英検取得級

                             "SOUDAN"         =>  VARS::post("SOUDAN")              //教育相談
                            );

        //010:入試区分,011:受験型,012:受験番号,013:帰国生Ｂ方式選択科目
        $this->testdivArray = knjl010fQuery::getTestdivArray($this);
        foreach ($this->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            $this->field["TESTDIV".$testdiv]    = VARS::post("TESTDIV".$testdiv);
            $this->field["EXAM_TYPE".$testdiv]  = (VARS::post("EXAM_TYPE".$testdiv) == "10") ? "A" : VARS::post("EXAM_TYPE".$testdiv);
            $this->field["RECEPTNO".$testdiv]   = VARS::post("RECEPTNO".$testdiv);
            $this->field["TESTSUBCLASSCD".$testdiv]  = VARS::post("TESTSUBCLASSCD".$testdiv);
        }
    }

    //追加処理
    public function &getMaxExamno()
    {
        //MAX受験番号＋１
        $this->examno = knjl010fQuery::getMaxExamnoQuery($this);
        return;
    }

    //受験番号範囲チェック（2017年度入試までの仕様 ===> 2018年度入試から受験番号範囲マスタに変更）
    public function checkNumber($testdiv, $exam_type, $receptno)
    {
        $check = array();
        $check[] = array("TESTDIV" => "1", "EXAM_TYPE" => "1", "MIN_NO" => "1001", "MAX_NO" => "1499");
        $check[] = array("TESTDIV" => "1", "EXAM_TYPE" => "2", "MIN_NO" => "1501", "MAX_NO" => "1999");
        $check[] = array("TESTDIV" => "2", "EXAM_TYPE" => "1", "MIN_NO" => "2001", "MAX_NO" => "2499");
        $check[] = array("TESTDIV" => "2", "EXAM_TYPE" => "2", "MIN_NO" => "2501", "MAX_NO" => "2999");
        $check[] = array("TESTDIV" => "3", "EXAM_TYPE" => "1", "MIN_NO" => "3001", "MAX_NO" => "3499");
        $check[] = array("TESTDIV" => "3", "EXAM_TYPE" => "2", "MIN_NO" => "3501", "MAX_NO" => "3999");
        $check[] = array("TESTDIV" => "4", "EXAM_TYPE" => "1", "MIN_NO" => "4001", "MAX_NO" => "4499");
        $check[] = array("TESTDIV" => "4", "EXAM_TYPE" => "2", "MIN_NO" => "4501", "MAX_NO" => "4999");
        $check[] = array("TESTDIV" => "5", "EXAM_TYPE" => "3", "MIN_NO" => "5001", "MAX_NO" => "5499");
        $check[] = array("TESTDIV" => "6", "EXAM_TYPE" => "4", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "6", "EXAM_TYPE" => "5", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "7", "EXAM_TYPE" => "4", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "7", "EXAM_TYPE" => "5", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "8", "EXAM_TYPE" => "1", "MIN_NO" => "6001", "MAX_NO" => "6499");
        $check[] = array("TESTDIV" => "8", "EXAM_TYPE" => "2", "MIN_NO" => "6501", "MAX_NO" => "6999");
        $check[] = array("TESTDIV" => "8", "EXAM_TYPE" => "3", "MIN_NO" => "6001", "MAX_NO" => "6499");
        $check[] = array("TESTDIV" => "9", "EXAM_TYPE" => "1", "MIN_NO" => "8001", "MAX_NO" => "8499");
        $check[] = array("TESTDIV" => "9", "EXAM_TYPE" => "2", "MIN_NO" => "8501", "MAX_NO" => "8999");
        $check[] = array("TESTDIV" => "9", "EXAM_TYPE" => "3", "MIN_NO" => "8001", "MAX_NO" => "8499");
        foreach ($check as $key => $val) {
            if ($testdiv == $val["TESTDIV"] && $exam_type == $val["EXAM_TYPE"]) {
                if ($receptno < $val["MIN_NO"] || $receptno > $val["MAX_NO"]) {
                    $msg = " (".$val["MIN_NO"]."～".$val["MAX_NO"].")";
                    return $msg;
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
                if (0 == $db->getOne(knjl010fQuery::getEntexamNoDatCnt($this, $testdiv, $exam_type, $receptno))) {
                    $this->setWarning("MSG305", "( 受験番号:{$receptno} )\\n受験番号範囲マスタが未設定です。");
                    return false;
                }
                //受験番号重複チェック
                $query = knjl010fQuery::checkNumber($this, $testdiv, $receptno);
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
        /***
                if (!($this->field["SHDIV"])) {
                    $this->setWarning("MSG301", "( 専併区分 )");
                    return false;
                }
                if (!($this->field["EXAMCOURSE"])) {
                    $this->setWarning("MSG301", "( 志望区分 )");
                    return false;
                }
        ***/
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
        if (!($this->field["TELNO"])) {
            $this->setWarning("MSG301", "( 志願者 電話番号 )");
            return false;
        }
        if (!($this->field["GNAME"])) {
            $this->setWarning("MSG301", "( 保護者 氏名 )");
            return false;
        }
        if (!($this->field["GKANA"])) {
            $this->setWarning("MSG301", "( 保護者 ふりがな )");
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
        $query = knjl010fQuery::existData($this);
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
            $this->setWarning("MSG915", "管理番号は5バイトまでです。");
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
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl010fQuery::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl010fQuery::getUpdateQuery($this);
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
        $row = knjl010fQuery::existData($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        knjl010fQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
}
