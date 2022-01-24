<?php

require_once('for_php7.php');

class knjl511jModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $testdiv;           //入試種別
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl511jModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::request("TESTDIV");
        }
        $this->examno       = VARS::post("EXAMNO");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->testdiv      = VARS::get("SEND_TESTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("ENTEXAMYEAR"          =>  $this->year,                        //入試年度
                             "APPLICANTDIV"         =>  $this->applicantdiv,                //学校種別
                             "TESTDIV"              =>  $this->testdiv,                     //入試種別

                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日(西暦)
                             "FS_CD"                =>  VARS::post("FS_CD"),                //出身校
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号

                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名かな(保護者)
                             "GTELNO"               =>  VARS::post("GTELNO"),               //緊急連絡先(保護者)

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
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 学校種別 )");
            return false;
        }
        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試種別 )");
            return false;
        }
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
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

        //キー値変更チェック
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新は検索を実行してから行ってください。");
            return false;
        }

        //文字数チェック
        //志願者
        if (strlen($this->examno) > 10) {
            $this->setWarning("MSG915","受験番号は10バイトまでです。");
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
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身校は7バイトまでです。");
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
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","志願者( 電話番号 )は14バイトまでです。");
            return false;
        }
        //保護者
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( 氏名かな )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["GTELNO"]) > 14) {
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

        //存在チェック
        $cnt = $db->getOne(knjl511jQuery::getExistdataCnt($this));
        if ($cnt == 0 && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl511jQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>