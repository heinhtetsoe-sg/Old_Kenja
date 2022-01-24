<?php

require_once('for_php7.php');

class knjd410mModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $semester;
    public $regdSemester;
    public $gakubu_school_kind;
    public $hukusiki_radio;
    public $hr_class;
    public $hr_class2;
    public $hr_class_list = array();
    public $hr_class2_list = array();
    public $list_sort;

    public function knjd410mModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->semester);
            unset($this->regdSemester);
            unset($this->gakubu_school_kind);
            unset($this->hukusiki_radio);
            unset($this->hr_class);
            unset($this->hr_class2);
            unset($this->hr_class_list);
            unset($this->hr_class2_list);
            unset($this->list_sort);
            $this->field = array();
        }

        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->semester = "9";
        $this->regdSemester = CTRL_SEMESTER;

        $this->gakubu_school_kind = VARS::request("GAKUBU_SCHOOL_KIND");
        $this->hukusiki_radio     = VARS::request("HUKUSIKI_RADIO");
        $this->hr_class           = VARS::request("HR_CLASS");
        $this->hr_class2          = VARS::request("HR_CLASS2");
        $this->list_sort          = VARS::post("LIST_SORT") ? VARS::post("LIST_SORT") : "hr_class_attendno";
        $this->selectdata         = VARS::post("selectdata");
        $this->checked            = VARS::post("CHECKED");

        $this->field = array(
                             "CONDITION_GROUPCD" => VARS::post("CONDITION_GROUPCD"),
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($cmd)
    {
        if ($cmd == "delete") {
            //削除
            //必須項目チェック
            if (!get_count($this->checked)) {
                $this->setWarning("チェックボックスを選択してください");
                return false;
            }
            if ($this->gakubu_school_kind == "") {
                $this->setWarning("MSG304");
                return false;
            }
        } elseif ($this->cmd == "update") {
            //必須項目チェック
            if (!$this->field["CONDITION_GROUPCD"]) {
                $this->setWarning("MSG310", "( 科目グループ )");
                return false;
            }
            if (!strlen($this->selectdata)) {
                $this->setWarning("MSG304", "( 対象者 )");
                return false;
            }
            if ($this->gakubu_school_kind == "") {
                $this->setWarning("MSG304");
                return false;
            }
        }

        return true;
    }

    //削除
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            if ($this->delete()) {
                $this->setMessage("MSG201");
                return;
            }
        }
    }
    public function &delete()
    {
        $result = knjd410mQuery::getDeleteQuery($this);
        return $result;
    }

    //更新
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            if ($this->update()) {
                $this->setMessage("MSG201");
                return;
            }
        }
    }
    public function &update()
    {
        $result = knjd410mQuery::getUpdateQuery($this);
        return $result;
    }
}
