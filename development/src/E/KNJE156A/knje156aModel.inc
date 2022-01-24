<?php

require_once('for_php7.php');

class knje156aModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $grade;
    public $hrClass;
    public $gradeHrClass;

    public function knje156aModel()
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

        $this->gradeHrClass = VARS::request("HR_CLASS");
        if ($this->gradeHrClass != "") {
            $this->grade = substr($this->gradeHrClass, 0, 2);
            $this->hrClass = substr($this->gradeHrClass, 2, 3);
        } else {
            $this->grade = '';
            $this->hrClass = '';
        }
        $this->schregnos = explode(',', VARS::request("schregnos"));
        $this->specialActRemark = VARS::request("SPECIALACTREMARK");
        $this->classAct = VARS::request("CLASSACT");
        $this->studentAct = VARS::request("STUDENTACT");
        $this->schoolEvent = VARS::request("SCHOOLEVENT");

        $this->cnt = 0;
        $this->data = array();
        if (VARS::request("cnt") != "") {
            $this->cnt = VARS::request("cnt");
            for ($i = 0; $i < $this->cnt; $i++) {
                $row = array();
                $row['SCHREGNO'] = $this->schregnos[$i];
                for ($j = 1; $j < 14; $j++) {
                    $row['REC'.$j] = VARS::request("REC".$j.'_'.$i);
                }
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
            $annual = $db->getOne(knje156aQuery::getAnnual($this, $this->data[$i]['SCHREGNO']));
            $db->query(knje156aQuery::deleteBehaviorDat($this, $this->data[$i]['SCHREGNO']));
            knje156aQuery::insertBehaviorDat($db, $this, $this->data[$i], $annual);
        }

        knje156aQuery::updateSpecialActRemark($db, $this);
        knje156aQuery::updateClassAct($db, $this);
        knje156aQuery::updateStudentAct($db, $this);
        knje156aQuery::updateSchoolEvent($db, $this);

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
