<?php

require_once('for_php7.php');

//定数定義

class knjl280gModel extends Model
{
    public $cmd;
    public $selectdata;
    public $selectdata2;
    public $csv_data;

    //メンバ変数の初期化
    public function knjl280gModel()
    {
    }
    public function init()
    {
        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");    //対象者一覧
        $this->selectdata2          = VARS::post("selectdata2");   //合格者一覧
        $this->appli_type           = VARS::post("APPLI_TYPE");
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->testdiv              = VARS::post("TESTDIV");
        $this->pro_date             = VARS::post("PRO_DATE");
        $this->shdiv                = VARS::post("SHDIV");
    }

    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    public function &update()
    {
        knjl280gQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }
}
