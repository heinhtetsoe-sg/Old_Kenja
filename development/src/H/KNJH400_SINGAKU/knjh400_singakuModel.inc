<?php

require_once('for_php7.php');

class knjh400_singakuModel extends Model
{
    public $cmd;
    public $schregno;
    public $year;
    public $exp_year;
    public $semester;
    public $exp_semester;

    public function knjh400_singakuModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->year);
            unset($this->exp_year);
            unset($this->semester);
            unset($this->exp_semester);
        }
        $this->cmd = VARS::request("cmd");

        $this->schregno = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->schregno;

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        $this->exp_year = $this->year;

        $this->semester = VARS::request("SEMESTER") != "" ? VARS::request("SEMESTER") : $this->semester;
        $this->exp_semester = $this->semester;

        $this->gouhi = VARS::post("GOUHI");
        $this->sinro = VARS::post("SINRO");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
