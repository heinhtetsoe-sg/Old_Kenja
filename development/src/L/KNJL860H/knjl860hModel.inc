<?php
class knjl860hModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;

    public function knjl860hModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"          =>  VARS::post("APPLICANTDIV"),          //学校種別
                             "TESTDIV"               =>  VARS::post("TESTDIV"),               //入試区分
                             "OUTPUT"                =>  VARS::post("OUTPUT"),                //帳票種類
                            );
    }
}
