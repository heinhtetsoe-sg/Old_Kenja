<?php

require_once('for_php7.php');

//定数定義

class knjl082bModel extends Model {
    var $cmd;
    var $selectdata;
    var $selectdataLabel;
    var $selectdata2;
    var $csv_data;

    //メンバ変数の初期化
    function knjl082bModel()
    {
    }
    function init()
    {
        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");         //対象者一覧
        $this->selectdataLabel      = VARS::post("selectdataLabel");    //対象者一覧(表示用)
        $this->selectdata2          = VARS::post("selectdata2");        //合格者一覧
        $this->appli_type           = VARS::post("APPLI_TYPE");
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->changeSingleTestFlg  = VARS::post("CHANGE_SINGLE_TEST_FLG");
        $this->testdiv              = VARS::post("TESTDIV");
        $this->pro_date             = VARS::post("PRO_DATE");
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
            unset($this->selectdata);
            unset($this->selectdataLabel);
        }
    }

    function &update()
    {
        knjl082bQuery::getUpdateQuery($this);
        //受付番号振りなおし
        knjl082bQuery::getUpdateRenbanQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }
}
?>
