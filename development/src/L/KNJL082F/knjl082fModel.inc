<?php

require_once('for_php7.php');

//定数定義

class knjl082fModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl082fModel()
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
        $this->pro_date             = VARS::post("PRO_DATE");
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
        knjl082fQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }
}
?>