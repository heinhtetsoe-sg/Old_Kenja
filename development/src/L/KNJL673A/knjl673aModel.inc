<?php

require_once('for_php7.php');

class knjl673aModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;

    public function knjl673aModel()
    {
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");

        $this->examyear     = CTRL_YEAR + 1;
        $this->applicantdiv = "2";  //固定値
        $this->exam_type    = "1";  //固定値
    }
}
