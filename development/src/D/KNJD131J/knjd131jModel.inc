<?php

require_once('for_php7.php');
class knjd131jModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $grade_hrclass;
    public $field = array();
    public $selectdata;

    public function knjd131jModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("DIV1_REMARK"     =>  VARS::post("DIV1_REMARK"),     //総合的な学習の時間
                             "DIV2_REMARK"     =>  VARS::post("DIV2_REMARK"),     //道徳
                             "DIV3_REMARK"     =>  VARS::post("DIV3_REMARK"),     //活動と奉仕の記録
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::post("selectdata") != "") {
            $this->selectdata = VARS::post("selectdata");
        } elseif (VARS::post("cmd") != "replace_update") {
            unset($this->selectdata);
        }

        $this->grade    = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["DIV1_REMARK"], (30 * 2)) > 7) {
            $this->setWarning("MSG915", "( 総合的な学習の時間は7行までです )");
            return false;
        }
        if ($this->validate_row_cnt($this->field["DIV2_REMARK"], (30 * 2)) > 7) {
            $this->setWarning("MSG915", "( 道徳は7行までです )");
            return false;
        }
        if ($this->validate_row_cnt($this->field["DIV3_REMARK"], (30 * 2)) > 20) {
            $this->setWarning("MSG915", "( 活動と奉仕の記録は20行までです )");
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
        knjd131jQuery::getInsertQuery($this->field, $this->schregno);
        
        return $result;
    }
}
