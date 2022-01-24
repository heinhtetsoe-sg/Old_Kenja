<?php
class knjl432iModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $testdiv;

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = VARS::post("APPLICANTDIV");
        $this->testdiv          = VARS::post("TESTDIV");
    }

    public function &getMainModel()
    {
    }
}
