<?php

require_once('for_php7.php');
class knjl011vModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $applicantDiv;
    public $examSchoolKind;
    public $data;
    public $field;
    public $exp_year;

    public function knjl011vModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->year = CTRL_YEAR + 1;
            $this->exp_year = CTRL_YEAR + 1;
            $this->semester= CTRL_SEMESTER;
            unset($this->applicantDiv);
            unset($this->examSchoolKind);
            unset($this->data);
            unset($this->field);
        }
        $this->cmd = VARS::request("cmd");

        if ($this->cmd != 'move') {
            //フィールド
            if (VARS::post("EXAMNO")) {
                $this->field = array("EXAMNO"                  =>  VARS::post("EXAMNO"),
                                     "NAME"                    =>  VARS::post("NAME"),
                                     "NAME_KANA"               =>  VARS::post("NAME_KANA"),
                                     "SEX"                     =>  VARS::post("SEX"),
                                     "BIRTHDAY"                =>  VARS::post("BIRTHDAY"),
                                     "FINSCHOOLCD"             =>  VARS::post("FINSCHOOLCD"),
                                     "FINSCHOOL_NAME"          =>  VARS::post("FINSCHOOL_NAME"),
                                     "FINISH_DATE"             =>  VARS::post("FINISH_DATE"),
                                     "ZIPCD"                   =>  VARS::post("ZIPCD"),
                                     "ADDR1"                   =>  VARS::post("ADDR1"),
                                     "ADDR2"                   =>  VARS::post("ADDR2"),
                                     "TELNO"                   =>  VARS::post("TELNO"),
                                     "EMAIL"                   =>  VARS::post("EMAIL"),
                                     "GUARD_NAME"              =>  VARS::post("GUARD_NAME"),
                                     "GUARD_NAME_KANA"         =>  VARS::post("GUARD_NAME_KANA"),
                                     "RELATION"                =>  VARS::post("RELATION"),
                                     "GUARD_ZIP"               =>  VARS::post("GUARD_ZIP"),
                                     "GUARD_ADDR1"             =>  VARS::post("GUARD_ADDR1"),
                                     "GUARD_ADDR2"             =>  VARS::post("GUARD_ADDR2"),
                                     "GUARD_TELNO"             =>  VARS::post("GUARD_TELNO"),
                                     "REMARK1"                 =>  VARS::post("REMARK1"),
                                     "REMARK2"                 =>  VARS::post("REMARK2"),
                                     "REMARK3"                 =>  VARS::post("REMARK3"),
                                     "REMARK4"                 =>  VARS::post("REMARK4"),
                                     "REMARK5"                 =>  VARS::post("REMARK5"),
                                     "RECEPTNO1"               =>  VARS::post("RECEPTNO1"),
                                     "RECEPTNO2"               =>  VARS::post("RECEPTNO2"),
                                     "RECEPTNO3"               =>  VARS::post("RECEPTNO3"),
                                     "RECEPTNO4"               =>  VARS::post("RECEPTNO4"),
                                     "RECEPTNO5"               =>  VARS::post("RECEPTNO5")
                                );
            }
            if ($this->cmd == 'left') {
                $this->applicantDiv = VARS::post("APPLICANT_DIV");
                $this->examSchoolKind = VARS::post("EXAM_SCHOOL_KIND");
            }
            if ($this->cmd == 'number') {
                $this->applicantDiv2 = $this->applicantDiv;
                $this->examSchoolKind2 = $this->examSchoolKind;
            }
            if ($this->cmd == 'number2') {
                $this->applicantDiv2 = VARS::post("APPLICANT_DIV2");
                $this->examSchoolKind2 = VARS::post("EXAM_SCHOOL_KIND2");
            }

            $this->receptData = array();
            $this->param = VARS::post("param");
            if ($this->param != '') {
                $paramList = explode(',', $this->param);
                for ($i = 0; $i < get_count($paramList); $i++) {
                    $paramListParts = explode(':', $paramList[$i]);
                    $this->receptData[] = array('COURSE_DIV' => $paramListParts[0], 'FREQUENCY' => $paramListParts[1], 'RECEPTNO' => VARS::post("RECEPTNO-" . $i));
                }
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
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knjl011vQuery::getMemberCount($this));
        Query::dbCheckIn($db);
        if ($cnt > 0) {
            if ($this->cmd == 'add') {
                $this->setWarning("MSG918", "統一志願者IDが重複しています。");
                return false;
            }
        } else {
            if ($this->cmd == 'update') {
                $this->setWarning("MSG308", "統一志願者IDは変更できません。");
                return false;
            }
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
        if ($this->cmd == 'add') {
            knjl011vQuery::insertMember($db, $this);
        }
        if ($this->cmd == 'update') {
            knjl011vQuery::updateMember($db, $this);
        }
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    //更新処理
    public function &getReceptUpdateModel()
    {
        $db = Query::dbCheckOut();
        knjl011vQuery::insertUpdateRecept($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        knjl011vQuery::deleteMember($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
