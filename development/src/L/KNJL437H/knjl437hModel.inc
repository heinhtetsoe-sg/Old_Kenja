<?php

require_once('for_php7.php');

class knjl437hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl437hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"         =>  VARS::post("EXAMTYPE")          //受験型
                     );

        $this->cmd = VARS::request("cmd");
    }
}
