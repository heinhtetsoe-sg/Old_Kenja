<?php

require_once('for_php7.php');
class knjl012vModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $applicantDiv;
    public $examSchoolKind;
    public $data;
    public $field;
    public $exp_year;

    public function knjl012vModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->year = CTRL_YEAR + 1;
            $this->semester= CTRL_SEMESTER;
            unset($this->applicantDiv);
            unset($this->examSchoolKind);
            unset($this->data);
            unset($this->field);
            unset($this->exp_year);
        }
        $this->cmd = VARS::request("cmd");

        if (VARS::post("EXAMNO")) {
            //フィールド
            $this->field = array("EXAMNO"                 =>  VARS::post("EXAMNO"),
                                 "JAPANESE1"              =>  VARS::post("JAPANESE1"),
                                 "MATH1"                  =>  VARS::post("MATH1"),
                                 "SOCIETY1"               =>  VARS::post("SOCIETY1"),
                                 "SCIENCE1"               =>  VARS::post("SCIENCE1"),
                                 "ENGLISH1"               =>  VARS::post("ENGLISH1"),
                                 "HEALTH_PHYSICAL1"       =>  VARS::post("HEALTH_PHYSICAL1"),
                                 "TECH_HOME1"             =>  VARS::post("TECH_HOME1"),
                                 "MUSIC1"                 =>  VARS::post("MUSIC1"),
                                 "ART1"                   =>  VARS::post("ART1"),
                                 "TOTAL31"                =>  VARS::post("TOTAL31"),
                                 "TOTAL51"                =>  VARS::post("TOTAL51"),
                                 "TOTAL91"                =>  VARS::post("TOTAL91"),
                                 "JAPANESE2"              =>  VARS::post("JAPANESE2"),
                                 "MATH2"                  =>  VARS::post("MATH2"),
                                 "SOCIETY2"               =>  VARS::post("SOCIETY2"),
                                 "SCIENCE2"               =>  VARS::post("SCIENCE2"),
                                 "ENGLISH2"               =>  VARS::post("ENGLISH2"),
                                 "HEALTH_PHYSICAL2"       =>  VARS::post("HEALTH_PHYSICAL2"),
                                 "TECH_HOME2"             =>  VARS::post("TECH_HOME2"),
                                 "MUSIC2"                 =>  VARS::post("MUSIC2"),
                                 "ART2"                   =>  VARS::post("ART2"),
                                 "TOTAL32"                =>  VARS::post("TOTAL32"),
                                 "TOTAL52"                =>  VARS::post("TOTAL52"),
                                 "TOTAL92"                =>  VARS::post("TOTAL92"),
                                 "JAPANESE3"              =>  VARS::post("JAPANESE3"),
                                 "MATH3"                  =>  VARS::post("MATH3"),
                                 "SOCIETY3"               =>  VARS::post("SOCIETY3"),
                                 "SCIENCE3"               =>  VARS::post("SCIENCE3"),
                                 "ENGLISH3"               =>  VARS::post("ENGLISH3"),
                                 "HEALTH_PHYSICAL3"       =>  VARS::post("HEALTH_PHYSICAL3"),
                                 "TECH_HOME3"             =>  VARS::post("TECH_HOME3"),
                                 "MUSIC3"                 =>  VARS::post("MUSIC3"),
                                 "ART3"                   =>  VARS::post("ART3"),
                                 "TOTAL33"                =>  VARS::post("TOTAL33"),
                                 "TOTAL53"                =>  VARS::post("TOTAL53"),
                                 "TOTAL93"                =>  VARS::post("TOTAL93"),
                                 "JAPANESE4"              =>  VARS::post("JAPANESE4"),
                                 "MATH4"                  =>  VARS::post("MATH4"),
                                 "SOCIETY4"               =>  VARS::post("SOCIETY4"),
                                 "SCIENCE4"               =>  VARS::post("SCIENCE4"),
                                 "ENGLISH4"               =>  VARS::post("ENGLISH4"),
                                 "HEALTH_PHYSICAL4"       =>  VARS::post("HEALTH_PHYSICAL4"),
                                 "TECH_HOME4"             =>  VARS::post("TECH_HOME4"),
                                 "MUSIC4"                 =>  VARS::post("MUSIC4"),
                                 "ART4"                   =>  VARS::post("ART4"),
                                 "TOTAL34"                =>  VARS::post("TOTAL34"),
                                 "TOTAL54"                =>  VARS::post("TOTAL54"),
                                 "TOTAL94"                =>  VARS::post("TOTAL94"),
                                 "JAPANESE5"              =>  VARS::post("JAPANESE5"),
                                 "MATH5"                  =>  VARS::post("MATH5"),
                                 "SOCIETY5"               =>  VARS::post("SOCIETY5"),
                                 "SCIENCE5"               =>  VARS::post("SCIENCE5"),
                                 "ENGLISH5"               =>  VARS::post("ENGLISH5"),
                                 "HEALTH_PHYSICAL5"       =>  VARS::post("HEALTH_PHYSICAL5"),
                                 "TECH_HOME5"             =>  VARS::post("TECH_HOME5"),
                                 "MUSIC5"                 =>  VARS::post("MUSIC5"),
                                 "ART5"                   =>  VARS::post("ART5"),
                                 "TOTAL35"                =>  VARS::post("TOTAL35"),
                                 "TOTAL55"                =>  VARS::post("TOTAL55"),
                                 "TOTAL95"                =>  VARS::post("TOTAL95"),
                                 "JAPANESE6"              =>  VARS::post("JAPANESE6"),
                                 "MATH6"                  =>  VARS::post("MATH6"),
                                 "SOCIETY6"               =>  VARS::post("SOCIETY6"),
                                 "SCIENCE6"               =>  VARS::post("SCIENCE6"),
                                 "ENGLISH6"               =>  VARS::post("ENGLISH6"),
                                 "HEALTH_PHYSICAL6"       =>  VARS::post("HEALTH_PHYSICAL6"),
                                 "TECH_HOME6"             =>  VARS::post("TECH_HOME6"),
                                 "MUSIC6"                 =>  VARS::post("MUSIC6"),
                                 "ART6"                   =>  VARS::post("ART6"),
                                 "TOTAL36"                =>  VARS::post("TOTAL36"),
                                 "TOTAL56"                =>  VARS::post("TOTAL56"),
                                 "TOTAL96"                =>  VARS::post("TOTAL96"),
                                 "SPECIAL_ACT1"           =>  VARS::post("SPECIAL_ACT1"),
                                 "SPECIAL_ACT2"           =>  VARS::post("SPECIAL_ACT2"),
                                 "SPECIAL_ACT3"           =>  VARS::post("SPECIAL_ACT3"),
                                 "SPECIAL_ACT4"           =>  VARS::post("SPECIAL_ACT4"),
                                 "SPECIAL_ACT5"           =>  VARS::post("SPECIAL_ACT5"),
                                 "SPECIAL_ACT6"           =>  VARS::post("SPECIAL_ACT6"),
                                 "SPECIAL_ACT7"           =>  VARS::post("SPECIAL_ACT7"),
                                 "SPECIAL_ACT8"           =>  VARS::post("SPECIAL_ACT8"),
                                 "SPECIAL_ACT9"           =>  VARS::post("SPECIAL_ACT9"),
                                 "SPECIAL_ACT10"          =>  VARS::post("SPECIAL_ACT10"),
                                 "ACT_TOTAL"              =>  VARS::post("ACT_TOTAL"),
                                 "ATTENDANCE1"            =>  VARS::post("ATTENDANCE1"),
                                 "ATTENDANCE2"            =>  VARS::post("ATTENDANCE2"),
                                 "ATTENDANCE3"            =>  VARS::post("ATTENDANCE3"),
                                 "ATTENDANCE4"            =>  VARS::post("ATTENDANCE4"),
                                 "ATTENDANCE5"            =>  VARS::post("ATTENDANCE5"),
                                 "ATTENDANCE6"            =>  VARS::post("ATTENDANCE6"),
                                 "SPECIAL_REMARK"         =>  VARS::post("SPECIAL_REMARK"),
                                 "REMARK1"                =>  VARS::post("REMARK1"),
                                 "REMARK2"                =>  VARS::post("REMARK2"),
                                 "REMARK3"                =>  VARS::post("REMARK3")
                                );
        }
        if ($this->cmd == 'left') {
            $this->applicantDiv = VARS::post("APPLICANT_DIV");
            $this->examSchoolKind = VARS::post("EXAM_SCHOOL_KIND");
        }
        if ($this->cmd == 'move') {
            $this->year = VARS::get("YEAR");
            $this->applicantDiv = VARS::get("APPLICANT_DIV");
            $this->examSchoolKind = VARS::get("EXAM_SCHOOL_KIND");
        }

        if (VARS::get("EXAMNO")) {
            $this->field['EXAMNO'] = VARS::get("EXAMNO");
        }
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }

        if ($this->cmd == 'init') {
            $this->year = CTRL_YEAR + 1;
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    //入力項目チェック
    public function validateInput()
    {
        if ($this->field['EXAMNO'] == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->validate_row_cnt($this->field['SPECIAL_REMARK'], (20 * 2)) > 4) {
            $this->setWarning("MSG915", "(特記事項は4行までです)");
            return false;
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }

        $db = Query::dbCheckOut();
        knjl012vQuery::updateData($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        knjl012vQuery::deleteData($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
