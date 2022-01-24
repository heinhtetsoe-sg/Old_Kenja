<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');
class knjl511hModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $distinctId;        //入試判別
    var $testdiv;
    var $shdiv;
    var $desirediv;
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl511hModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->applicantdiv);
            unset($this->distinctId);
            unset($this->checkexam);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd  = VARS::request("cmd");
        $temp = VARS::request("YEAR");
        if (isset($temp)) {
            $this->year = VARS::request("YEAR");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("DISTINCT_ID");
        if (isset($temp)) {
            $this->distinctId = VARS::request("DISTINCT_ID");
        }
        $this->testdiv      = 1;//1固定
        $this->shdiv        = 1;//1固定
        $this->desirediv    = 1;//1固定
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
                             "DISTINCT_ID"          =>  $this->distinctId,                  //入試判別

                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "ERACD"                =>  VARS::post("ERACD"),                //元号（生年月日）
                             "BIRTH_Y"              =>  VARS::post("BIRTH_Y"),              //年
                             "BIRTH_M"              =>  VARS::post("BIRTH_M"),              //月
                             "BIRTH_D"              =>  VARS::post("BIRTH_D"),              //日
                             "FS_CD"                =>  VARS::post("FS_CD"),                //出身校
                             "FS_GRDYEAR"           =>  VARS::post("FS_GRDYEAR"),           //卒業年
                             "FS_M"                 =>  VARS::post("FS_M"),                 //卒業月
                             "FS_GRDDIV"            =>  VARS::post("FS_GRDDIV"),            //卒業区分
                             "DORMITORY_FLG"        =>  VARS::post("DORMITORY_FLG"),        //入寮希望
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書
                             "TELNO"                =>  VARS::post("TELNO"),                //電話番号

                             "GNAME"                =>  VARS::post("GNAME"),                //氏名(保護者)
                             "GKANA"                =>  VARS::post("GKANA"),                //氏名かな(保護者)
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //続柄
                             "GTELNO"               =>  VARS::post("GTELNO"),               //緊急連絡先(保護者)

                             "RELANAME1"            =>  VARS::post("RELANAME1"),            //氏名1
                             "WORKPLACE1"           =>  VARS::post("WORKPLACE1"),           //勤務先・在学校1
                             "RELATIONSHIP1"        =>  VARS::post("RELATIONSHIP1"),        //続柄1
                             "RELA_AGE1"            =>  VARS::post("RELA_AGE1"),            //年齢1
                             "RELANAME2"            =>  VARS::post("RELANAME2"),            //氏名2
                             "WORKPLACE2"           =>  VARS::post("WORKPLACE2"),           //勤務先・在学校2
                             "RELATIONSHIP2"        =>  VARS::post("RELATIONSHIP2"),        //続柄2
                             "RELA_AGE2"            =>  VARS::post("RELA_AGE2"),            //年齢2
                             "RELANAME3"            =>  VARS::post("RELANAME3"),            //氏名3
                             "WORKPLACE3"           =>  VARS::post("WORKPLACE3"),           //勤務先・在学校3
                             "RELATIONSHIP3"        =>  VARS::post("RELATIONSHIP3"),        //続柄3
                             "RELA_AGE3"            =>  VARS::post("RELA_AGE3"),            //年齢3
                             "RELANAME4"            =>  VARS::post("RELANAME4"),            //氏名4
                             "WORKPLACE4"           =>  VARS::post("WORKPLACE4"),           //勤務先・在学校4
                             "RELATIONSHIP4"        =>  VARS::post("RELATIONSHIP4"),        //続柄4
                             "RELA_AGE4"            =>  VARS::post("RELA_AGE4"),            //年齢4
                             "RELANAME5"            =>  VARS::post("RELANAME5"),            //氏名5
                             "WORKPLACE5"           =>  VARS::post("WORKPLACE5"),           //勤務先・在学校5
                             "RELATIONSHIP5"        =>  VARS::post("RELATIONSHIP5"),        //続柄5
                             "RELA_AGE5"            =>  VARS::post("RELA_AGE5")             //年齢5

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
        if (!($this->field["DISTINCT_ID"])) {
            $this->setWarning("MSG301", "( 入試判別 )");
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
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 氏名かな )");
            return false;
        }
        if (!($this->field["SEX"])) {
            $this->setWarning("MSG301", "( 性別 )");
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
        //家族情報
        for ($i = 1; $i <= 5; $i++) {
            if (strlen($this->field["RELANAME{$i}"]) > 60) {
                $this->setWarning("MSG915","( 家族情報{$i}氏名 )は60バイトまでです。");
                return false;
            }
            if (strlen($this->field["WORKPLACE{$i}"]) > 120) {
                $this->setWarning("MSG915","( 家族情報{$i}勤務先・在学校 )は120バイトまでです。");
                return false;
            }
        }

        //存在チェック
        $cnt = $db->getOne(knjl511hQuery::getExistdataCnt($this));
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
            $result = knjl511hQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }
}
?>