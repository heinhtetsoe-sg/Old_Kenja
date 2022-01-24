<?php

require_once('for_php7.php');
class knjj520Model extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $control;
    var $data  = array();

    function knjj520Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->field["DATE"]  = VARS::post("DATE");
        $this->field["VALUE"] = VARS::post("VALUE");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //必須入力チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["DATE"] == "") {
            $this->setWarning("MSG304");
            return false;
        }

        foreach ($this->data["ITEMCD"] as $code => $col) {
            if ($this->fields["RECORD-".$col] >= 1000) {
                $this->setWarning("MSG915","(記録は0～999.999までです)");
                return false;
            }
        }

        return true;
    }

    function &getUpdateModel()
    {
        $this->fields = array();
        foreach ($this->data["ITEMCD"] as $code => $col) {
            $post_name = VARS::post("RECORD-".$col);
            if (isset($post_name)) $this->fields["RECORD-".$col] = $post_name;
        }

        if (!$this->validateInput()) {
            return;
        }

        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjj520Query::getDeleteQuery($this);
        knjj520Query::getInsertQuery($this);
        return ;
    }
}
?>
