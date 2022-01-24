<?php

require_once('for_php7.php');
class knjd411Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $field = array();
    var $control;           //コントロールマスタ

    function knjd411Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->getSetSubclasscd);
            unset($this->getUnitcd);
            unset($this->exp_year);
            unset($this->exp_semester);
        }

        $this->field = array("UNITNAME"       =>  VARS::post("UNITNAME"),
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name       = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade       = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class    = VARS::get("HR_CLASS");
        }
        //取得パラメータ
        if (VARS::get("SET_SUBCLASSCD") != "") {
            $this->getSetSubclasscd    = VARS::get("SET_SUBCLASSCD");
        }
        $this->getUnitcd    = VARS::get("UNITCD");

        if (VARS::post("UNITCD") != "") {
            $this->getUnitcd    = VARS::post("UNITCD");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->getSetSubclasscd);
            unset($this->getUnitcd);
        }
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //学籍番号を選択し直した場合
        if ($this->cmd === 'edit') {
            unset($this->getSetSubclasscd);
            unset($this->getUnitcd);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "" || $this->getSetSubclasscd == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->getSetSubclasscd != "" && $this->getUnitcd == "") {
            $this->setWarning("MSG305", "単元が設定されていません。");
            return false;
        }
        if ($this->cmd === 'update') {
            if (strlen($this->field["UNITNAME"]) == 0 ) {
                $this->setWarning("MSG301", "(単元名称)");
                return false;
            }
            if (strlen($this->field["UNITNAME"]) > 90 ) {
                $this->setWarning("MSG915", "(単元名称) 90バイトまで");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        $db = Query::dbCheckOut();
        if ($this->cmd === 'update') {
            knjd411Query::getInsertQuery($db, $this);
        } else if ($this->cmd === 'delete') {
            knjd411Query::getDeleteQuery($db, $this);
        }
        return $result;

        Query::dbCheckIn($db);
    }
}
?>
