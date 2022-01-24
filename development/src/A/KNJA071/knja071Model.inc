<?php

require_once('for_php7.php');

class knja071Model extends Model
{
    public $fields      = array();
    public $cmd;
    public $hr_class;        //primary
    public $grade;
    public $term;
    public $term2;
    public $control;
    public $check;

    public function knja071Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::request("term") != "") {
            $this->term  = VARS::request("term");
        }
        if (VARS::request("term2") != "") {
            $this->term2 = VARS::request("term2");
        }
        /*
        if ((VARS::request("PROGRAMID") != "") && (CTRL_SEMESTER != $this->control["学期数"])) {
            $semester = CTRL_SEMESTER +1;
            $this->term  = CTRL_YEAR. "-" .$semester;
        }
        if ((VARS::request("PROGRAMID") != "") && (CTRL_SEMESTER == $this->control["学期数"])) {
            $year = CTRL_YEAR + 1;
            $semester = 1;
            $this->term  = $year. "-" .$semester;
        }
        */

        if (VARS::get("PROGRAMID") != "") {
            unset($this->term);
        }

        $this->fields = array("GRADE"           =>  VARS::post("GRADE"),
                              "HR_CLASS"        =>  VARS::post("HR_CLASS"),
                              "COURSEMAJOR"     =>  VARS::post("COURSEMAJOR"),
                              "COURSECODE"      =>  VARS::post("COURSECODE"),
                              "UPDATED"         =>  VARS::post("UPDATED")
                             );

        $this->cmd          = VARS::request("cmd");
        $temp = VARS::get("HR_CLASS");
        if (isset($temp)) {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        $temp = VARS::get("GRADE");
        if (isset($temp)) {
            $this->grade    = VARS::get("GRADE");
        }
        if (VARS::get("init") == "1") {
            unset($this->grade);
            unset($this->hr_class);
        }
        if (VARS::post("check") != "") {
            $this->check    = VARS::request("check");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->hr_class == "" || $this->grade == "") {
            $this->setWarning("MSG308");
            return false;
        }

        if ($this->fields["COURSEMAJOR"] == "") {
            $this->setWarning("MSG301", "(課程学科)");
            return false;
        }

        if ($this->fields["COURSEMAJOR"] == "") {
            $this->setWarning("MSG301", "(コース)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //１レコード取得
            $row = knja071Query::getRow($this->term, $this->grade, $this->hr_class);
            if (is_array($row)) {
                //更新日付チェック
                if ($row["UPDATED"] != $this->fields["UPDATED"]) {
                    $this->setWarning("MSG307");
                    return false;
                }
                $this->update();
            } else {
                $this->insert();
            }
            $this->setMessage("MSG201");
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->grade = $this->fields["GRADE"];
            $this->hr_class = $this->fields["HR_CLASS"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->grade) || !strlen($this->hr_class)) {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->hr_class != $this->fields["HR_CLASS"] || $this->grade != $this->fields["GRADE"]) {
            $this->setWarning("MSG308");
            return false;
        }
        //１レコード取得
        $row = knja071Query::getRow($this->term, $this->fields["GRADE"], $this->fields["HR_CLASS"]);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        $ret_val = knja071Query::getDeleteQuery($this);
        if ($ret_val == "class_formation") {
            $this->warning = "生徒が既に割り振られています。生徒HR割振り処理で削除してから再度実行してください。";
            return false;
        } elseif ($ret_val == "schreg_regd") {
            $this->warning = "クラスが存在するので削除できません。";
            return false;
        }
        unset($this->grade);
        unset($this->hr_class);
        $this->setMessage("MSG201");
    }

    //INSERT
    public function &insert()
    {
        $result = knja071Query::getInsertQuery($this);
        return $result;
    }

    //UPDATE
    public function &update()
    {
        $result = knja071Query::getUpdateQuery($this);
        return $result;
    }

    //コピーボタン押し時の処理
    public function &getInsertCopyModel()
    {
        $db = Query::dbCheckOut();
        $cnt = $db->getOne(knja071Query::diteilHdatCount($this, $this->term));
        Query::dbCheckIn($db);
        //入力チェック
        if ($this->term == $this->term2) {
            $this->setWarning("MSG300", "同年度、同学期のコピーは出来ません。");
            return false;
        } elseif ($cnt > 0) {
            $this->warning = "対象年度にデータが存在します。コピーできません。";
            return false;
        } else {
            knja071Query::getInsertCopyQuery($this, $this->term, $this->term2);
            $this->setMessage("MSG201");
        }
        return;
    }
}
