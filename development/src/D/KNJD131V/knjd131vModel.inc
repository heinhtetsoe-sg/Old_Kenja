<?php

require_once('for_php7.php');
class knjd131vModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $grade_hrclass;
    public $field = array();

    public function knjd131vModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("COMMUNICATION"     =>  VARS::post("COMMUNICATION"),     //通信欄
                             "SEMESTER"          =>  VARS::post("SEMESTER")           //学期
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name     = VARS::get("NAME");
        }

        if (VARS::get("GRADE")!="") {
            $this->grade    = VARS::get("GRADE");
        }
        $this->hr_class = VARS::get("HR_CLASS");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種を取得
        $db = Query::dbCheckOut();
        $query = knjd131vQuery::getSchoolKind($this->grade);
        $this->setSchKind = $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        Query::dbCheckIn($db);

        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = 50;
            $this->getPro["COMMUNICATION"]["gyou"] = 3;
        }

        if ($this->Properties["Knjd131v_Teikei_Button_Hyouji"] == "1") {
            //定型文選択
            if (VARS::get("SEM") != "") {
                $this->sem = VARS::get("SEM");
            }
            if (VARS::get("SCHNO") != "") {
                $this->schno = VARS::get("SCHNO");
            }
            if (VARS::get("NAME") != "") {
                $this->name = VARS::get("NAME");
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915", "(通信欄は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
            return false;
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    public function &update()
    {
        if ($this->cmd == 'update') {
            knjd131vQuery::getDeleteQuery($this->schregno, $this);
            knjd131vQuery::getInsertQuery($this->field, $this->schregno);
        }
        return $result;
    }
}
