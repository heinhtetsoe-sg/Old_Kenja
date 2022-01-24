<?php

require_once('for_php7.php');

class knjl331aModel extends Model
{
    public $cmd;
    public $examyear;
    public $selected_data;
    public $examType;
    public $field;

    public function knjl331aModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->examType = 1;    //固定

        $this->field = array("APPLICANTDIV"    =>  VARS::post("APPLICANTDIV"),    //受験校種
                             "TESTDIV"         =>  VARS::post("TESTDIV"),         //入試区分
                             "SHDIV"           =>  VARS::post("SHDIV"),           //専併区分
                             "ENTER_COURSE"    =>  VARS::post("ENTER_COURSE"),    //入学コース
                             "PASS_COURSE"     =>  VARS::post("PASS_COURSE")      //合格コース
                              );
    }
}
