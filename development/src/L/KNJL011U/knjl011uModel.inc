<?php

require_once('for_php7.php');

class knjl011uModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $testdiv;           //入試区分
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011uModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
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
        $this->testdiv      = VARS::post("TESTDIV");
        $this->examno       = VARS::post("EXAMNO");
        
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->testdiv      = VARS::get("SEND_TESTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("APPLICANTDIV"         =>  VARS::post("APPLICANTDIV"),         //入試制度
                             "TESTDIV"              =>  VARS::post("TESTDIV"),              //入試区分

                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身学校コード
                             "MIRAI_FS_SHOZAI_CD"   =>  VARS::post("MIRAI_FS_SHOZAI_CD"),   //出身学校所在地コード

                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号(志願者)
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所(志願者)
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書(志願者)
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号(志願者)
                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名かな(保護者)
                             "GZIPCD"               =>  VARS::post("GZIPCD"),               //郵便番号(保護者)
                             "GADDRESS1"            =>  VARS::post("GADDRESS1"),            //住所(保護者)
                             "GADDRESS2"            =>  VARS::post("GADDRESS2"),            //方書(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),               //電話番号(保護者)
                             "EMERGENCYTELNO"       =>  VARS::post("EMERGENCYTELNO"),       //緊急連絡先(保護者)

                             "PRISCHOOLCD"          =>  VARS::post("PRISCHOOLCD"),          //塾
                             "PRISCHOOL_CLASS_CD"   =>  VARS::post("PRISCHOOL_CLASS_CD"),   //教室
                             "SLIDE_FLG"            =>  VARS::post("SLIDE_FLG"),            //観察者フラグ

                             "REMARK1"              =>  VARS::post("REMARK1"),              //備考1
                             "REMARK2"              =>  VARS::post("REMARK2")               //備考2
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
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
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["BIRTHDAY"])) {
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
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( ふりがな )は120バイトまでです。");
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
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( ふりがな )は120バイトまでです。");
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
        if (strlen($this->field["EMERGENCYTELNO"]) > 14) {
            $this->setWarning("MSG915","保護者( 緊急連絡先 )は14バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 120) {
            $this->setWarning("MSG915","( 備考１ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 120) {
            $this->setWarning("MSG915","( 備考２ )は120バイトまでです。");
            return false;
        }
        //重複チェック
        $cnt = $db->getOne(knjl011uQuery::getExistdataCnt($this));
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
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011uQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011uQuery::getUpdateQuery($this);
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
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        //DB接続
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjl011uQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl011uQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
}
?>