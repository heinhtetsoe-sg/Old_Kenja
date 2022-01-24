<?php

require_once('for_php7.php');

class knja611Model extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $grade;
    public $hrClass;
    public $gradeHrClass;

    public function knja611Model()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->grade);
            unset($this->hrClass);
            unset($this->gradeHrClass);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->schoolKindJ = 'J';

        $db = Query::dbCheckOut();
        $this->grade = $db->getOne(knja611Query::getGrade($this));
        Query::dbCheckIn($db);

        $this->gradeHrClass = VARS::request("HR_CLASS");
        if ($this->gradeHrClass != "") {
            $this->hrClass = substr($this->gradeHrClass, 2, 3);
        } else {
            $this->hrClass = '';
        }
        $this->schregnos = explode(',', VARS::request("schregnos"));

        $this->cnt = 0;
        $this->data = array();
        if (VARS::request("cnt") != "") {
            $this->cnt = VARS::request("cnt");
            for ($i = 0; $i < $this->cnt; $i++) {
                $row = array();
                $row['SCHREGNO'] = $this->schregnos[$i];
                $row['IQ'] = VARS::request('IQ-'.$i);
                $this->data[] = $row;
            }
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $db = Query::dbCheckOut();
        for ($i = 0; $i < get_count($this->data); $i++) {
            $db->query(knja611Query::deleteSchregIqDat($this, $this->data[$i]['SCHREGNO']));
            knja611Query::insertSchregIqDat($db, $this, $this->data[$i]);
        }

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
