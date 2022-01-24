<?php

require_once('for_php7.php');

class knjl434hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl434hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType = 1;

        $this->field = array("APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "EXAMTYPE"        =>  VARS::post("EXAMTYPE"),         //受験型
                             "SHDIV"           =>  VARS::post("SHDIV"),            //専併区分
                             "TESTSUBCLASSCD"  =>  VARS::post("TESTSUBCLASSCD"),   //入試科目
                             "TESTDIV"         =>  VARS::post("TESTDIV")           //回数
                     );

        $this->cmd = VARS::request("cmd");
    }
}
