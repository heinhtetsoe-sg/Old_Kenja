<?php

require_once('for_php7.php');

class knjl011aModel extends Model
{
    public $cmd;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $testdiv;           //受験種別
    public $checkexam;
    public $checkKey;
    public $field = array();   //フィールドデータ
    public $field2 = array();  //フィールドデータ（下画面）

    public function knjl011aModel()
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
            unset($this->honorDiv1);
            unset($this->honorDiv2);
            unset($this->honorDiv3);
            unset($this->clubCd);
            unset($this->seq005R1);
        }
        $this->cmd = VARS::request("cmd");
        $this->year          = CTRL_YEAR + 1;
        $this->semester      = CTRL_SEMESTER;
        $this->applicantdiv  = VARS::post("APPLICANTDIV");
        $this->testdiv       = VARS::post("TESTDIV");
        $this->examno        = VARS::post("EXAMNO");
        $this->search_examno = VARS::post("SEARCH_EXAMNO");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->testdiv      = VARS::get("SEND_TESTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "ERACD"                =>  VARS::post("ERACD"),                //元号（生年月日）
                             "BIRTH_Y"              =>  VARS::post("BIRTH_Y"),              //年
                             "BIRTH_M"              =>  VARS::post("BIRTH_M"),              //月
                             "BIRTH_D"              =>  VARS::post("BIRTH_D"),              //日
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身学校コード
                             "FS_ERACD"             =>  VARS::post("FS_ERACD"),             //卒業元号
                             "FS_Y"                 =>  VARS::post("FS_Y"),                 //卒業年
                             "FS_M"                 =>  VARS::post("FS_M"),                 //卒業月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),            //卒業区分

                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名かな(保護者)
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //続柄(保護者)
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号(志願者)
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所(志願者)
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書(志願者)
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号(志願者)
                             "EMERGENCYTELNO"       =>  VARS::post("EMERGENCYTELNO"),       //緊急連絡先(保護者)

                             "REMARK1"              =>  VARS::post("REMARK1"),              //備考

                             "PRISCHOOLCD1"         =>  VARS::post("PRISCHOOLCD1"),         //塾１
                             "PRISCHOOL_CLASS_CD1"  =>  VARS::post("PRISCHOOL_CLASS_CD1"),  //教室１
                             "PRISCHOOLCD2"         =>  VARS::post("PRISCHOOLCD2"),         //塾２
                             "PRISCHOOL_CLASS_CD2"  =>  VARS::post("PRISCHOOL_CLASS_CD2"),  //教室２
                             "PRISCHOOLCD3"         =>  VARS::post("PRISCHOOLCD3"),         //塾３
                             "PRISCHOOL_CLASS_CD3"  =>  VARS::post("PRISCHOOL_CLASS_CD3")   //教室３
                            );

        $db = Query::dbCheckOut();
        //年度変換(和暦->西暦)

        $seirekiBirthYear = $this->convertEraFmt($db, $this->field["ERACD"], $this->field["BIRTH_Y"]);
        $this->field["BIRTHDAY"] = $seirekiBirthYear."-".$this->field["BIRTH_M"]."-".$this->field["BIRTH_D"];

        $seirekiFsYear = $this->convertEraFmt($db, $this->field["FS_ERACD"], $this->field["FS_Y"]);
        $this->field["FS_GRDYEAR"] = $seirekiFsYear;

        Query::dbCheckIn($db);

        //フィールド
        $this->checkKey = VARS::post("CHECK_KEY"); //リストから選択したか
        $this->field2 = array("TESTDIV"             =>  VARS::post("TESTDIV"),              //受験種別
                             "RECEPTNO"             =>  VARS::post("RECEPTNO"),             //受験番号
                             "SHDIV"                =>  VARS::post("SHDIV"),                //専併区分
                             "DESIREDIV"            =>  VARS::post("DESIREDIV"),            //志望コース
                             "SUBCLASS_TYPE"        =>  VARS::post("SUBCLASS_TYPE")         //受験科目
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //和暦年を西暦年に変換
    public function convertEraFmt($db, $eraCd, $eraYear)
    {
        $query = knjl011aQuery::getNameCd($this->year, "L007", $eraCd);

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $sYear = $row["NAMESPARE1"];
        return $sYear + $eraYear - 1;
    }

    //入力項目チェック
    public function validateInput()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (!($this->examno) && $this->cmd != "add") {
            $this->setWarning("MSG301", "( 志願者SEQ )");
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
        if (!($this->field["ERACD"]) || !($this->field["BIRTH_Y"]) || !($this->field["BIRTH_M"]) || !($this->field["BIRTH_D"])) {
            $this->setWarning("MSG301", "( 生年月日 )");
            return false;
        }
        if (!($this->field["ZIPCD"])) {
            $this->setWarning("MSG301", "( 志願者郵便番号 )");
            return false;
        }
        if (!($this->field["ADDRESS1"])) {
            $this->setWarning("MSG301", "( 志願者住所 )");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 10) {
            $this->setWarning("MSG915", "志願者SEQは10バイトまでです。");
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
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915", "出身学校コードは7バイトまでです。");
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
        if (strlen($this->field["ADDRESS1"]) > 150) {
            $this->setWarning("MSG915", "志願者( 住所 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["ADDRESS2"]) > 150) {
            $this->setWarning("MSG915", "志願者( 方書 )は150バイトまでです。");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915", "志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["EMERGENCYTELNO"]) > 14) {
            $this->setWarning("MSG915", "保護者( 緊急連絡先 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 240) {
            $this->setWarning("MSG915", "( 備考 )は240バイトまでです。");
            return false;
        }
        //追加ボタンで、志願者SEQ(Hで始まる以下連番)を取得
        if ($this->cmd == "add") {
            $this->examno = knjl011aQuery::getMaxExamno($db, $this);
        }
        //重複チェック
        $cnt = $db->getOne(knjl011aQuery::getExistdataCnt($this));
        if ($cnt > 0 && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        //存在チェック
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
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
            $result = knjl011aQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011aQuery::getUpdateQuery($this);
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
            $this->setWarning("MSG304", "( 志願者SEQ )");
            return false;
        }
        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //DB接続
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjl011aQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl011aQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }

    /***** 追加更新処理（下画面） *****/

    //入力項目チェック
    public function validateInput2()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!strlen($this->examno)) {
            $this->setWarning("MSG301", "( 志願者SEQ )");
            return false;
        }
        if (!strlen($this->applicantdiv)) {
            $this->setWarning("MSG301", "( 受験校種 )");
            return false;
        }
        if (!strlen($this->field2["TESTDIV"])) {
            $this->setWarning("MSG301", "( 試験区分 )");
            return false;
        }
        if (!strlen($this->field2["RECEPTNO"])) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!strlen($this->field2["DESIREDIV"])) {
            $this->setWarning("MSG301", "( 志望コース )");
            return false;
        }
        if (!strlen($this->field2["SUBCLASS_TYPE"])) {
            $this->setWarning("MSG301", "( 受験科目 )");
            return false;
        }
        if (!strlen($this->field2["SHDIV"])) {
            $this->setWarning("MSG301", "( 専併区分 )");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add2" && $this->checkKey != $this->field2["TESTDIV"]."_".$this->field2["RECEPTNO"]) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //重複チェック
        $cnt1 = $db->getOne(knjl011aQuery::getReceptCheck1($this));
        $cnt2 = $db->getOne(knjl011aQuery::getReceptCheck2($this));
        if ($cnt1 > 0 && $this->cmd == "add2") {
            $this->setWarning("MSG302", "同じ試験には追加できません。");
            return false;
        }
        if ($cnt2 > 0 && $this->cmd == "add2") {
            $this->setWarning("MSG302");
            return false;
        }
        //存在チェック
        if ($cnt2 == 0 && $this->cmd != "add2") {
            $this->setWarning("MSG303");
            return false;
        }
        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //追加処理
    public function &getInsertModel2()
    {
        //入力チェック
        if ($this->validateInput2()) {
            $result = knjl011aQuery::getUpdateQuery2($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel2()
    {
        if ($this->validateInput2()) {
            $result = knjl011aQuery::getUpdateQuery2($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    public function &getDeleteModel2()
    {
        if ($this->validateInput2()) {
            $result = knjl011aQuery::getDeleteQuery2($this);
            $this->setMessage("MSG201");
            //$this->field2 = array();
            $this->field2["RECEPTNO"] = "";
        }
        return;
    }
}
