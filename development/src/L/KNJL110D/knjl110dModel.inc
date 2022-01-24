<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');
class knjl110dModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $visitNo;           //事前相談番号
    var $shdiv;
    var $checkVisit;
    var $field = array();           //フィールドデータ

    function knjl110dModel() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->visitNo);
            unset($this->year);
            unset($this->checkVisit);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd = VARS::request("cmd");
        $this->year         = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->visitNo      = VARS::post("VISIT_NO");

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->visitNo      = VARS::get("SEND_VISIT_NO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //フィールド
        $this->field = array("VISIT_DATE"           =>  VARS::post("VISIT_DATE"),           //相談日
                             "NAME"                 =>  VARS::post("NAME"),                 //氏名(志願者)
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),            //氏名かな(志願者)
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "ERACD"                =>  VARS::post("ERACD"),                //元号（生年月日）
                             "BIRTH_Y"              =>  VARS::post("BIRTH_Y"),              //年
                             "BIRTH_M"              =>  VARS::post("BIRTH_M"),              //月
                             "BIRTH_D"              =>  VARS::post("BIRTH_D"),              //日
                             "FS_CD"                =>  VARS::post("FINSCHOOLCD"),          //出身学校
                             "FS_ERACD"             =>  VARS::post("FS_ERACD"),             //卒業元号
                             "FS_Y"                 =>  VARS::post("FS_Y"),                 //卒業年
                             "FS_M"                 =>  VARS::post("FS_M"),                 //卒業月
                             "ZIPCD"                =>  VARS::post("ZIPCD"),                //郵便番号
                             "ADDRESS1"             =>  VARS::post("ADDRESS1"),             //住所
                             "ADDRESS2"             =>  VARS::post("ADDRESS2"),             //方書
                             "TELNO"                =>  VARS::post("TELNO")                 //電話番号
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        //DB接続
        $db = Query::dbCheckOut();

        //空文字チェック
        if (!($this->visitNo)) {
            $this->setWarning("MSG301", "( 事前相談番号 )");
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

        //ゼロ埋め
        $this->visitNo = sprintf("%03d", $this->visitNo);

        //キー値変更チェック
        if ($this->cmd != "add" && $this->visitNo != $this->checkVisit) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }

        //文字数チェック
        //志願者
        if (strlen($this->visitNo) > 3) {
            $this->setWarning("MSG915","事前相談番号は3バイトまでです。");
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
            $this->setWarning("MSG915","出身学校は7バイトまでです。");
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

        //重複チェック
        $cnt = $db->getOne(knjl110dQuery::getExistdataCnt($this));
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
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $result = knjl110dQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $result = knjl110dQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->visitNo == "") {
            $this->setWarning("MSG304", "( 事前相談番号 )");
            return false;
        }
        if ($this->visitNo != $this->checkVisit) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        //DB接続
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjl110dQuery::getExistdataCnt($this));
        //DB切断
        Query::dbCheckIn($db);
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        knjl110dQuery::getDeleteQuery($this);
        $this->visitNo = "";
        $this->setMessage("MSG201");
    }
 
}
?>