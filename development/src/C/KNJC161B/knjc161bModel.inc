<?php

require_once('for_php7.php');

class knjc161bModel extends Model
{
    public $cmd;
    public $year;
    public $month;
    public $dispType;
    public $limit_date;
    public $ctrl = array();
    public $selectdata = array();
    public $cmd2;
    
    public function knjc161bModel()
    {
        common::GetControlMaster_Fnc($this->ctrl);
    }
    
    public function init()
    {
        $this->year       = VARS::post("year");
        $this->month      = VARS::post("month");
        $this->dispType   = VARS::post("selDispType");
        $this->cmd        = VARS::request("cmd");
        $this->cmd2       = VARS::post("cmd2");
        $this->DBNAME     = VARS::post("DBNAME");
        $this->selGrHrCls = VARS::post("GRADE_HR_CLASS");

        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
