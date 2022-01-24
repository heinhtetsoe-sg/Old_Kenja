<?php

require_once('for_php7.php');

class knja224gModel extends Model
{
    public $field      = array();
    public $cmd;
    public $selectdata;

    public function knja224gModel()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "GRADE"             =>  VARS::post("GRADE"),            //学年
                             "DATE_FROM"         =>  VARS::post("DATE_FROM"),        //日付指定範囲（FROM）
                             "DATE_TO"           =>  VARS::post("DATE_TO"),          //日付指定範囲（TO）
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
