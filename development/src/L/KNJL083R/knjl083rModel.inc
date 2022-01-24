<?php

require_once('for_php7.php');

class knjl083rModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $ObjYear;
    var $applicantdiv;
    var $testdiv;
    var $pro_date;

    //メンバ変数の初期化
    function knjl083rModel() {}

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->pro_date);
            unset($this->selectdata);
            unset($this->selectdata2);
        }

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");    //手続者一覧
        $this->selectdata2          = VARS::post("selectdata2");   //合格者一覧
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->testdiv              = VARS::post("TESTDIV");
        $this->pro_date             = VARS::post("PRO_DATE");

    }

    function validateInput() {
        //必須チェック
        if (strlen($this->applicantdiv) == 0) {
            $this->setWarning("MSG301","( 入試制度 )");
            return false;
        }
        if (strlen($this->testdiv) == 0) {
            $this->setWarning("MSG301","( 入試区分 )");
            return false;
        }
        if (strlen($this->pro_date) == 0) {
            $this->setWarning("MSG301","( 手続年月日 )");
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
        knjl083rQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }

}
?>
