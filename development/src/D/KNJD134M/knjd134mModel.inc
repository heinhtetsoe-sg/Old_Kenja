<?php

require_once('for_php7.php');

class knjd134mModel extends Model
{
    public $field  = array();
    public $cmd;
    public $year;
    public $semester;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knjd134mModel()
    {
    }

    public function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
