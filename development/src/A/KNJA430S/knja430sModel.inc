<?php

require_once('for_php7.php');


class knja430sModel extends Model {
    var $field      = array();
    var $cmd;
    var $staffcd;
    var $staffYear;
    var $searchCode;
    var $searchName;
    var $searchKana;
    var $stampNo;
    var $maxStampNo;

    /**
     * メンバ変数の初期化
     */
    function knja430sModel()
    {
    }
    function init()
    {              
        if (VARS::get("PROGRAMID")){
            unset($this->staffcd);
            unset($this->stampNo);
        }
        $this->field = array("STAFFCD"          =>  VARS::post("STAFFCD"),

                             "START_DATE"       =>  VARS::post("START_DATE"),
                             "START_REASON"     =>  VARS::post("START_REASON"),

                             "DIST"             =>  VARS::post("DIST"),

                             "STOP_DATE"        =>  VARS::post("STOP_DATE"),
                             "STOP_REASON"      =>  VARS::post("STOP_REASON"),

                             "UPDATED"          =>  VARS::post("UPDATED")
                             );
        $this->staffYear = VARS::post("STAFFYEAR");
        $this->searchCode = VARS::post("SEARCH_CODE");
        $this->searchName = VARS::post("SEARCH_NAME");
        $this->searchKana = VARS::post("SEARCH_KANA");
        //印鑑番号
        $temp = VARS::get("STAMP_NO");
        if (isset($temp)){
            $this->stampNo = VARS::get("STAMP_NO");
        } else {
            $this->stampNo = VARS::post("HID_STAMP_NO");
        }

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->staffcd);
        }
        $temp = VARS::get("STAFFCD");
        if (isset($temp)){
            $this->staffcd  = VARS::get("STAFFCD");
        }
        if ($this->cmd == "edit") {
            unset($this->maxStampNo);
        }
    }

    function &getMaxStampNoModel()
    {
        $this->maxStampNo = knja430sQuery::getMaxStampNo($this->field["STAFFCD"]);
        unset($this->stampNo);
    }

    function validateInput()
    {
        //キーのチェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if (!strlen($this->staffcd)) {
                $this->setWarning('職員を左のリストから選択して下さい。');
                return false;
            }
            if (!strlen($this->maxStampNo) && $this->cmd == "add") {
                $this->setWarning('新規印鑑番号を選択して下さい。');
                return false;
            }
            if (!strlen($this->stampNo) && ($this->cmd == "update" || $this->cmd == "delete")) {
                $this->setWarning('印鑑番号を上のリストから選択して下さい。');
                return false;
            }
        }
        //文字数のチェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (30 < strlen($this->field["START_REASON"])) {
                $this->setWarning('MSG901', "(登録事由) 30バイトまで");
                return false;
            } elseif (30 < strlen($this->field["STOP_REASON"])) { 
                $this->setWarning('MSG901', "(除印事由) 30バイトまで");
                return false;
            }
        }
        //印影登録済みは削除できない。
        //走査日付ありの場合、印影登録済みと判断する。TODO：仕様はこれでいいか？？？
        if ($this->cmd == "delete") {
            if (strlen($this->field["DATE"])) {
                $this->setWarning('印影登録済みは削除できません。');
                return false;
            }
        }

        return true;
    }

    function &getInsertModel()
    {
        if ($this->validateInput()){
            knja430sQuery::getInsertQueryInkan($this);
            $this->stampNo = $this->maxStampNo;
            unset($this->maxStampNo);
            $this->setMessage('MSG201');
        }
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()){
            knja430sQuery::getUpdateQueryInkan($this);
            $this->setMessage('MSG201');
        }
    }

    function &getDeleteModel()
    {
        if ($this->validateInput()){
            knja430sQuery::getDeleteQueryInkan($this);
            unset($this->stampNo);
            $this->setMessage('MSG201');
        }
    }

    function clean()
    {
    }
}
?>
