<?php

require_once('for_php7.php');

class knjd139bModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $field = array();

    function knjd139bModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->semester);
        }

        $this->field = array("SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),     //特別活動
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
        $this->school_kind = $db->getOne(knjd139bQuery::getSchoolKind($this->grade));
        if (!strlen($this->school_kind)) $this->school_kind = "H";
        Query::dbCheckIn($db);

        //特別活動
        if ($this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_".$this->school_kind]);
            $this->getPro["SPECIALACTREMARK"]["moji"] = (int)trim($moji);
            $this->getPro["SPECIALACTREMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["SPECIALACTREMARK"]["moji"] = 40;
            $this->getPro["SPECIALACTREMARK"]["gyou"] = 14;
        }

        //備考
        if ($this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_".$this->school_kind]);
            $this->getPro["COMMUNICATION"]["moji"] = (int)trim($moji);
            $this->getPro["COMMUNICATION"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["COMMUNICATION"]["moji"] = ($this->school_kind == "J") ? 40 : 32;
            $this->getPro["COMMUNICATION"]["gyou"] = ($this->school_kind == "J") ? 7 : 5;
        }
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
        if ($this->validate_row_cnt($this->field["SPECIALACTREMARK"], ($this->getPro["SPECIALACTREMARK"]["moji"] * 2)) > $this->getPro["SPECIALACTREMARK"]["gyou"]) {
            $this->setWarning("MSG915","(特別活動は{$this->getPro["SPECIALACTREMARK"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($this->getPro["COMMUNICATION"]["moji"] * 2)) > $this->getPro["COMMUNICATION"]["gyou"]) {
            $this->setWarning("MSG915","(備考は{$this->getPro["COMMUNICATION"]["gyou"]}行までです)");
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
        knjd139bQuery::getUpdateQuery($this, $this->field, $this->schregno);
        return;
    }
}
?>
