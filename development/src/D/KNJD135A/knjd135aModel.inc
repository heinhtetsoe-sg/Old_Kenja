<?php

require_once('for_php7.php');

class knjd135aModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();

    function knjd135aModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }
        if (VARS::get("PROGRAMID")!="") {
            unset($this->name);
        }
        $this->field = array("COMMUNICATION" =>  VARS::post("COMMUNICATION") //通信欄
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name = VARS::get("NAME");
        }

        $this->grade    = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["COMMUNICATION"], (30 * 2)) > 5) {
            $alert_title = $this->Properties["tutisyoSyokenTitle"] ? $this->Properties["tutisyoSyokenTitle"] : '通信欄';
            $this->setWarning("MSG915","({$alert_title}は5行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd135aQuery::getInsertQuery($this->field,$this->schregno);
        return $result;
    }
}
?>
