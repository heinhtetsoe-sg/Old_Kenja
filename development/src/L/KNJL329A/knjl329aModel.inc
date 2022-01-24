<?php
class knjl329aModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl329aModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType = 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //試験回
                     );

        $this->cmd = VARS::request("cmd");
    }
}
