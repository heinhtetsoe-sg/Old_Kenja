<?php

require_once('for_php7.php');

class knjl436hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl436hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"         =>  VARS::post("TESTDIV")           //回数
                     );

        $this->cmd = VARS::request("cmd");
    }
}
