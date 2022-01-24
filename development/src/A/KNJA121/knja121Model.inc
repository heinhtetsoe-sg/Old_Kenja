<?php

require_once('for_php7.php');
class knja121Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $control;

    function knja121Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("TOTALSTUDYACT"        =>  VARS::post("TOTALSTUDYACT"),
                             "TOTALSTUDYVAL"        =>  VARS::post("TOTALSTUDYVAL"),
                             "SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),//高校のみ
                             "TOTALREMARK"          =>  VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK"     =>  VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        //年度コンボ（通知表所見）
        $this->year_cmb = VARS::request("YEAR_CMB");
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYACT"]) > 184) {
            $this->setWarning("MSG915","(活動内容は184バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYVAL"]) > 400) {
            $this->setWarning("MSG915","(評価は400バイトまでです)");
            return false;
        }
        if (strlen($this->field["SPECIALACTREMARK"]) > 202) {
            $this->setWarning("MSG915","(特別活動所見は202バイトまでです)");//高校のみ
            return false;
        }
        if (strlen($this->field["TOTALREMARK"]) > 796) {
            $this->setWarning("MSG915","(総合所見は796バイトまでです)");
            return false;
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(出欠の記録備考は122バイトまでです)");
            return false;
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
        knja121Query::getDeleteQuery($this->schregno);
        knja121Query::getInsertQuery($this->field,$this->schregno);
        return $result;
    }
}
?>
