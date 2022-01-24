<?php

require_once('for_php7.php');
class knjd138fModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $grade_hrclass;
    public $school_kind;
    public $field = array();
    public $getPro;

    public function knjd138fModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "REMARK1"      =>  VARS::post("REMARK1"),      //生徒会／HR役員／係など
                             "REMARK2"      =>  VARS::post("REMARK2"),      //所属クラブ
                             "REMARK3"      =>  VARS::post("REMARK3")       //備考
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
        $query = knjd138fQuery::getSchoolKind($this->grade);
        $this->school_kind = $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        Query::dbCheckIn($db);

        //生徒会／HR役員／係など
        if ($this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_".$school_kind]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 20;
            $this->getPro["REMARK1"]["gyou"] = 5;
        }

        //所属クラブ
        if ($this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK2_SIZE_".$school_kind]);
            $this->getPro["REMARK2"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK2"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK2"]["moji"] = 20;
            $this->getPro["REMARK2"]["gyou"] = 5;
        }

        //備考
        if ($this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_".$school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_REMARK3_SIZE_".$school_kind]);
            $this->getPro["REMARK3"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK3"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK3"]["moji"] = 15;
            $this->getPro["REMARK3"]["gyou"] = 7;
        }

        $this->getPro["REMARK1"]["name"] = "生徒会／HR役員／係など";
        $this->getPro["REMARK2"]["name"] = "所属クラブ";
        $this->getPro["REMARK3"]["name"] = "備考";
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //行数チェック
        if ($this->validate_row_cnt($this->field["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915", "({$this->getPro["REMARK1"]["name"]}は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["REMARK2"]["moji"] * 2)) > $this->getPro["REMARK2"]["gyou"]) {
            $this->setWarning("MSG915", "({$this->getPro["REMARK2"]["name"]}は{$this->getPro["REMARK2"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], ($this->getPro["REMARK3"]["moji"] * 2)) > $this->getPro["REMARK3"]["gyou"]) {
            $this->setWarning("MSG915", "({$this->getPro["REMARK3"]["name"]}は{$this->getPro["REMARK3"]["gyou"]}行までです)");
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
        knjd138fQuery::getDeleteQuery($this->schregno, $this);
        knjd138fQuery::getInsertQuery($this->field, $this->schregno);
    }
}
