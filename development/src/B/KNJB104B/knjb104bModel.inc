<?php

require_once('for_php7.php');

class knjb104bModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $startDate;
    public $endDate;
    public $chairCd;

    public function knjb104bModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->startDate);
            unset($this->endDate);
            unset($this->chairCd);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->startDate = CTRL_DATE;
        $this->semester = VARS::post("SEMESTER");
        $this->grade = VARS::post("GRADE");
        $this->kousa = VARS::post("KOUSA");

        $this->chaircds = array();
        if (VARS::request("chaircds") != "") {
            $this->chaircds = explode(',', VARS::request("chaircds"));
        }
        $this->data = $this->data2 = array();
        for ($i = 0; $i < get_count($this->chaircds); $i++) {
            $this->data[$this->chaircds[$i]][] = VARS::request('FACILITY_'.$i);
            $this->data2['FACILITY_'.$i] = VARS::request('FACILITY_'.$i);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->semester == '') {
            $this->setWarning("MSG301", "(学期)");
            return false;
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $db = Query::dbCheckOut();
        knjb104bQuery::updateFacility($db, $this, $this->data);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
