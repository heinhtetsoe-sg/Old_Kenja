<?php

require_once('for_php7.php');

//定数定義

class knjl080fModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl080fModel()
    {
    }
    function init()
    {
        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2          = VARS::post("selectdata2");   //合格者一覧
        $this->appli_type           = VARS::post("APPLI_TYPE");
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->testdiv              = VARS::post("TESTDIV");
        $this->pay_div              = VARS::post("PAY_DIV");
        $this->pay_date             = VARS::post("PAY_DATE");
        $this->pay_chak_date        = VARS::post("PAY_CHAK_DATE");
        $this->testdiv0             = VARS::post("TESTDIV0");
    }

    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        }
    }

    function &update()
    {
        knjl080fQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }
}
?>
