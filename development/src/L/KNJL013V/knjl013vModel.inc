<?php

require_once('for_php7.php');
class knjl013vModel extends Model
{
    public $cmd;
    public $year;
    public $semester;
    public $exp_year;
    public $examnoStart;
    public $examnoEnd;
    public $examSchoolKind;
    public $applicantDiv;
    public $frequency;
    public $receptnoStart;
    public $receptnoEnd;
    public $finschoolcd;
    public $data;
    public $field;
    public $maxcnt;
    public $selectPattern;

    public function knjl013vModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->year = CTRL_YEAR + 1;
            $this->semester= CTRL_SEMESTER;
            $this->exp_year = CTRL_YEAR + 1;
            unset($this->examnoStart);
            unset($this->examnoEnd);
            unset($this->examSchoolKind);
            unset($this->applicantDiv);
            unset($this->frequency);
            unset($this->receptnoStart);
            unset($this->receptnoEnd);
            unset($this->finschoolcd);
            unset($this->data);
            unset($this->field);
            unset($this->maxcnt);
            $this->selectPattern = 1;
        }
        $this->cmd = VARS::request("cmd");

        //フィールド
        $this->names = array("EXAMNO",
                            "NAME",
                            "JAPANESE1",
                            "MATH1",
                            "SOCIETY1",
                            "SCIENCE1",
                            "ENGLISH1",
                            "HEALTH_PHYSICAL1",
                            "TECH_HOME1",
                            "MUSIC1",
                            "ART1",
                            "TOTAL31",
                            "TOTAL51",
                            "TOTAL91",
                            "JAPANESE2",
                            "MATH2",
                            "SOCIETY2",
                            "SCIENCE2",
                            "ENGLISH2",
                            "HEALTH_PHYSICAL2",
                            "TECH_HOME2",
                            "MUSIC2",
                            "ART2",
                            "TOTAL32",
                            "TOTAL52",
                            "TOTAL92",
                            "JAPANESE3",
                            "MATH3",
                            "SOCIETY3",
                            "SCIENCE3",
                            "ENGLISH3",
                            "HEALTH_PHYSICAL3",
                            "TECH_HOME3",
                            "MUSIC3",
                            "ART3",
                            "TOTAL33",
                            "TOTAL53",
                            "TOTAL93",
                            "JAPANESE4",
                            "MATH4",
                            "SOCIETY4",
                            "SCIENCE4",
                            "ENGLISH4",
                            "HEALTH_PHYSICAL4",
                            "TECH_HOME4",
                            "MUSIC4",
                            "ART4",
                            "TOTAL34",
                            "TOTAL54",
                            "TOTAL94",
                            "JAPANESE5",
                            "MATH5",
                            "SOCIETY5",
                            "SCIENCE5",
                            "ENGLISH5",
                            "HEALTH_PHYSICAL5",
                            "TECH_HOME5",
                            "MUSIC5",
                            "ART5",
                            "TOTAL35",
                            "TOTAL55",
                            "TOTAL95",
                            "JAPANESE6",
                            "MATH6",
                            "SOCIETY6",
                            "SCIENCE6",
                            "ENGLISH6",
                            "HEALTH_PHYSICAL6",
                            "TECH_HOME6",
                            "MUSIC6",
                            "ART6",
                            "TOTAL36",
                            "TOTAL56",
                            "TOTAL96",
                            "SPECIAL_ACT1",
                            "SPECIAL_ACT2",
                            "SPECIAL_ACT3",
                            "SPECIAL_ACT4",
                            "SPECIAL_ACT5",
                            "SPECIAL_ACT6",
                            "SPECIAL_ACT7",
                            "SPECIAL_ACT8",
                            "SPECIAL_ACT9",
                            "SPECIAL_ACT10",
                            "ACT_TOTAL",
                            "ATTENDANCE1",
                            "ATTENDANCE2",
                            "ATTENDANCE3",
                            "ATTENDANCE4",
                            "ATTENDANCE5",
                            "ATTENDANCE6",
                            "SPECIAL_REMARK",
                            "REMARK1",
                            "REMARK2",
                            "REMARK3"
                            );

        $this->sikenId = VARS::post("SIKEN_ID");
        if ($this->sikenId != '') {
            list($this->examSchoolKind, $this->applicantDiv, $this->courseDiv, $this->frequency) = explode('-', $this->sikenId);
        } else {
            $this->examSchoolKind = '';
            $this->applicantDiv = '';
            $this->courseDiv = '';
            $this->frequency = '';
        }

        $this->examSchoolKind = VARS::post("EXAM_SCHOOL_KIND");
        $this->examnoStart = VARS::post("EXAMNO_START");
        $this->examnoEnd = VARS::post("EXAMNO_END");
        $this->receptnoStart = VARS::post("RECEPTNO_START");
        $this->receptnoEnd = VARS::post("RECEPTNO_END");
        $this->finschoolcd = VARS::post("FINSCHOOLCD");
        $this->selectPattern = VARS::post("SELECT_PATTERN");
        if (VARS::post("examnos")) {
            $this->examnos = explode(',', VARS::post("examnos"));
        } else {
            $this->examnos = array();
        }
        if (VARS::post("TOTAL3")) {
            $this->total3 = explode(',', VARS::post("TOTAL3"));
        } else {
            $this->total3 = array();
        }
        if (VARS::post("TOTAL5")) {
            $this->total5 = explode(',', VARS::post("TOTAL5"));
        } else {
            $this->total5 = array();
        }
        if (VARS::post("TOTAL9")) {
            $this->total9 = explode(',', VARS::post("TOTAL9"));
        } else {
            $this->total9 = array();
        }

        if ($this->cmd == 'update') {
            $this->maxcnt = get_count($this->examnos);
            $this->data = array();
            for ($i = 0; $i < $this->maxcnt; $i++) {
                for ($j = 0; $j < get_count($this->names); $j++) {
                    $this->data[$i][$this->names[$j]] = VARS::post($this->names[$j] . '-' . $i);
                }
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
        if ($this->selectPattern == '6') {
            for ($i = 0; $i < get_count($this->data); $i++) {
                if ($this->validate_row_cnt($this->data[$i]['SPECIAL_REMARK'], (20 * 2)) > 4) {
                    $this->setWarning("MSG915", "(特記事項は4行までです)");
                    return false;
                }
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
        knjl013vQuery::updateData($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
}
