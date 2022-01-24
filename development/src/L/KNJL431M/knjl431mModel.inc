<?php

require_once('for_php7.php');

class knjl431mModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl431mModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "EXAM_SCHOOL_KIND" =>  VARS::post("EXAM_SCHOOL_KIND"), //校種
                             "SORT_DIV"         =>  VARS::post("SORT_DIV")          //並び順
                     );

        $this->cmd = VARS::request("cmd");
    }
}
