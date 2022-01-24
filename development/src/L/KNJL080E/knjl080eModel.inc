<?php

require_once('for_php7.php');

class knjl080eModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $ObjYear;
    var $applicantdiv;
    var $passcourse;

    //メンバ変数の初期化
    function knjl080eModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->passcourse);
            unset($this->selectdata);
            unset($this->selectdata2);
        }

        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");    //手続者一覧
        $this->selectdata2          = VARS::post("selectdata2");   //合格者一覧
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->passcourse           = VARS::post("PASSCOURSE");

    }

    function validateInput() {
        //必須チェック
        if (strlen($this->applicantdiv) == 0) {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if (strlen($this->passcourse) == 0) {
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    function &update() {
        knjl080eQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }

}
?>
