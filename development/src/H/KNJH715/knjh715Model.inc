<?php

require_once('for_php7.php');

class knjh715Model extends Model
{
    public $field  = array();
    public $cmd;
    public $select_opt = array();
    public $selectleft;
    public $selectleftval;

    public function knjh715Model()
    {
    }

    public function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SCHOOL_KIND"        => VARS::post("SCHOOL_KIND"),       //校種
                             "SEMESTER"           => VARS::post("SEMESTER"),          //学期
                             "GRADE_NAME"         => VARS::post("GRADE_NAME"),        //学年名
                             "GRADE_HRCLASS_NAME" => VARS::post("GRADE_HRCLASS_NAME"),//学年クラス名
                             "DBNAME"             => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        if ($this->cmd != 'change') {
            $this->select_opt = array();
        }
    }
}
