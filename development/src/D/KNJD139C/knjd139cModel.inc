<?php

require_once('for_php7.php');

class knjd139cModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $field = array();
    var $moral_moji = 58;
    var $moral_gyou = 2;
    var $comm_moji = 55;
    var $comm_gyou = 4;

    function knjd139cModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        $this->field = array("MORAL"                =>  VARS::post("MORAL"),                //道徳
                             "COMMUNICATION"        =>  VARS::post("COMMUNICATION")         //備考
                             );

        if (VARS::post("SEMESTER") != "") {
            $this->semester = VARS::post("SEMESTER");
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade    = VARS::get("GRADE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種を取得
        $db = Query::dbCheckOut();
        $this->school_kind = $db->getOne(knjd139cQuery::getSchoolKind($this->grade));
        if (!strlen($this->school_kind)) $this->school_kind = "H";
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (!strlen($this->semester)) {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }

        //文字数チェック
        if ($this->validate_row_cnt($this->field["MORAL"], ($this->moral_moji * 2)) > $this->moral_gyou) {
            $this->setWarning("MSG915","(道徳は{$this->moral_gyou}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->comm_moji * 2)) > $this->comm_gyou) {
            $this->setWarning("MSG915","(備考は{$this->comm_gyou}行までです)");
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
        knjd139cQuery::getUpdateQuery($this, $this->field, $this->schregno);
        return;
    }
}
?>
