<?php
class knjl781hModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examYear;
    public $selectdata;

    public function knjl781hModel()
    {
    }

    public function init()
    {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->examYear = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        if (VARS::request("TESTDIV") != "") {
            $this->testdiv = VARS::request("TESTDIV");
        }

        $this->selectdata = VARS::post("selectdata");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        knjl781hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
