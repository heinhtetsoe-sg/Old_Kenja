<?php

require_once('for_php7.php');

class knjd294Model extends Model {
    var $fields = array();
    var $cmd;
    var $grade_hr_class;
    var $check_all;
    var $data_chk = array();
    var $data_cnt;

    function knjd294Model() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->grade_hr_class);
            unset($this->check_all);
            unset($this->data_chk);
        }

        $this->cmd = VARS::request("cmd");
        $this->grade_hr_class = VARS::post("GRADE_HR_CLASS");

        //更新
        if ($this->cmd == "update") {
            $this->check_all    = VARS::post("CHECKALL");
            $this->data_chk     = VARS::request("CHECKED");
            $this->data_cnt     = VARS::post("DATA_CNT");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {
        if (!$this->grade_hr_class) {
            $this->setWarning("MSG304");
            return false;
        }
        if (!$this->data_cnt) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd294Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
