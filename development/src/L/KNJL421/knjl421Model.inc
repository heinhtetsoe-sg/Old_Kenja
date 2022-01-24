<?php

require_once('for_php7.php');

class knjl421Model extends Model {
    var $auth;
    var $recruit_no;
    var $touroku_date;
    var $event_cd;
    var $media_cd;
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjl421Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->auth);
            unset($this->recruit_no);
            unset($this->touroku_date);
            unset($this->event_cd);
            unset($this->event_class_cd);
            unset($this->media_cd);
        }

        $this->field = array("TOUROKU_DATE"         =>  VARS::post("TOUROKU_DATE"),
                             "EVENT_CLASS_CD"       =>  VARS::post("EVENT_CLASS_CD"),
                             "EVENT_CD"             =>  VARS::post("EVENT_CD"),
                             "MEDIA_CD"             =>  VARS::post("MEDIA_CD"),
                             "STATE_CD"             =>  VARS::post("STATE_CD"),
                             "ATTEND_MEETING_FLG"   =>  VARS::post("ATTEND_MEETING_FLG"),
                             "DOC_REQ_NUMBER"       =>  VARS::post("DOC_REQ_NUMBER"),
                             "REMARK"               =>  VARS::post("REMARK")
                            );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SEND_PRGRID")){
            $this->getPrgId         = VARS::get("SEND_PRGRID");
            $this->sendAuth         = VARS::get("SEND_AUTH");
            $this->recruit_no       = VARS::get("SEND_RECRUIT_NO");
            $this->prischoolCd      = VARS::get("SEND_PRISCHOOLCD");
            $this->prischoolClassCd = VARS::get("SEND_PRISCHOOL_CLASS_CD");
            unset($this->touroku_date);
            unset($this->event_cd);
            unset($this->event_class_cd);
            unset($this->media_cd);
        }
        //権限
        $this->auth = ($this->getPrgId) ? $this->sendAuth : AUTHORITY;

        if (VARS::get("TOUROKU_DATE")) {
            $this->touroku_date = VARS::get("TOUROKU_DATE");
        }
        if (VARS::get("EVENT_CD")) {
            $this->event_cd = VARS::get("EVENT_CD");
        }
        if (VARS::get("EVENT_CLASS_CD")) {
            $this->event_class_cd = VARS::get("EVENT_CLASS_CD");
        }
        if (VARS::get("MEDIA_CD")) {
            $this->media_cd = VARS::get("MEDIA_CD");
        }

        $this->recruitYear = CTRL_YEAR + 1;

        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return false;
        }

        if ($this->field["TOUROKU_DATE"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 登録日付 ）");
            return false;
        }
        if ($this->field["EVENT_CLASS_CD"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 分類 ）");
            return false;
        }
        if ($this->field["EVENT_CD"] == "") {
            $this->setWarning("MSG301", "　　　　　（ イベント ）");
            return false;
        }
        if ($this->field["MEDIA_CD"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 媒体 ）");
            return false;
        }

        if ($this->cmd == "update") {
            if ($this->touroku_date != $this->field["TOUROKU_DATE"] || $this->event_class_cd != $this->field["EVENT_CLASS_CD"] || $this->event_cd != $this->field["EVENT_CD"] || $this->media_cd != $this->field["MEDIA_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        if ($this->cmd == "insert") {
            //存在チェック
            $chk = knjl421Query::getRow_check($this);
            if ($chk > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["REMARK"], (40 * 2)) > 2) {
            $this->setWarning("MSG915","(備考は2行までです)");
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {

        if (!$this->validateInput()) {
            return;
        }

        //更新
        knjl421Query::getUpdateQuery($this, $this->field);
        $this->setMessage("MSG201");

        if ($this->cmd == "insert") {
            $this->touroku_date = $this->field["TOUROKU_DATE"];
            $this->event_class_cd = $this->field["EVENT_CLASS_CD"];
            $this->event_cd = $this->field["EVENT_CD"];
            $this->media_cd = $this->field["MEDIA_CD"];
        }

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {

        if ($this->recruit_no == "") {
            $this->setWarning("MSG309");
            return;
        }
        if ($this->field["TOUROKU_DATE"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 登録日付 ）");
            return;
        }
        if ($this->field["EVENT_CLASS_CD"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 分類 ）");
            return;
        }
        if ($this->field["EVENT_CD"] == "") {
            $this->setWarning("MSG301", "　　　　　（ イベント ）");
            return;
        }
        if ($this->field["MEDIA_CD"] == "") {
            $this->setWarning("MSG301", "　　　　　（ 媒体 ）");
            return;
        }

        if ($this->touroku_date != $this->field["TOUROKU_DATE"] || $this->event_class_cd != $this->field["EVENT_CLASS_CD"] || $this->event_cd != $this->field["EVENT_CD"] || $this->media_cd != $this->field["MEDIA_CD"]) {
            $this->setWarning("MSG308");
            return;
        }

        //削除
        knjl421Query::getDeleteQuery($this);

        $this->field = array();
        unset($this->touroku_date);
        unset($this->event_cd);
        unset($this->event_class_cd);
        unset($this->media_cd);

        $this->setMessage("MSG201");

        return;
    }
}
?>
