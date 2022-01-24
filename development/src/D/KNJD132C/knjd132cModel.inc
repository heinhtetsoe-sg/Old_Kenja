<?php

require_once('for_php7.php');

class knjd132cModel extends Model {
    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $grade;
    var $title;
    var $field = array();
    var $record = array();

    function knjd132cModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->title);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "REMARK2"              =>  VARS::post("MORAL"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        //行動の記録
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                $this->record["RECORD"][$key] = VARS::post("RECORD".$key);
            }
        }

        $this->cmd = VARS::request("cmd");

        //左フレームから取得
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
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("TITLE") != "") {
            $this->title = VARS::get("TITLE");
        }
        if (VARS::get("TEIKEI_CMD") != "") {
            $this->field["TEIKEI_CMD"] = VARS::get("TEIKEI_CMD");
        }
        $this->getPro["MORAL"]["moji"] = 32;
        $this->getPro["MORAL"]["gyou"] = 2;

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304","（学期）");
            return false;
        }
        if (strlen($this->field["REMARK2"]) > 0) {
            if ($this->validate_row_cnt($this->field["REMARK2"], ($this->getPro["MORAL"]["moji"] * 2)) > $this->getPro["MORAL"]["gyou"]) {
                $gyoStr = $this->getPro["MORAL"]["gyo"] > 1 ? $this->getPro["MORAL"]["gyo"]."行" : "";
                $this->setWarning("MSG915","(道徳は{$gyoStr}{$this->getPro["MORAL"]["moji"]}文字までです)");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        //BEHAVIOR_SEMES_DAT
        knjd132cQuery::getInsertQuery2($this);

        return ;
    }
}
?>
