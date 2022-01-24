<?php

require_once('for_php7.php');

class knjp805Model extends Model {
    var $cmd;
    var $schFlg;
    var $arr_schregData = array();

    function knjp805Model() {
    }
 
    function init() {
        $this->cmd = VARS::request("cmd");

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        //フィールド
        $this->field = array("PAID_LIMIT_MONTH"         => VARS::post("PAID_LIMIT_MONTH"),
                             "REMINDER_STAFFCD"         => VARS::post("REMINDER_STAFFCD"),
                             "GRADE"                    => VARS::post("GRADE"),
                             "PAID_DATE1"               => VARS::post("PAID_DATE1"),
                             "PAID_DATE2"               => VARS::post("PAID_DATE2"),
                             "GRADE_HR_CLASS"           => VARS::post("GRADE_HR_CLASS"),
                             "DEFAULT_DOCUMENT"         => VARS::post("DEFAULT_DOCUMENT")
                            );

        $this->windowHeight = VARS::post("windowHeight");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->cmd == "update" || $this->cmd == "updateTest") {
            foreach ($this->arr_schregData as $key => $val) {
                $this->field["GO_PRINT:".$val]       = VARS::post("GO_PRINT:".$val);
                $this->field["NO_COUNT_UP-".$val]    = VARS::post("NO_COUNT_UP-".$val);
                $this->field["REMINDER_COUNT-".$val] = VARS::post("REMINDER_COUNT-".$val);
                $this->field["DOCUMENTCD-".$val]     = VARS::post("DOCUMENTCD-".$val);
                $this->field["REMINDER_MONEY-".$val] = VARS::post("REMINDER_MONEY-".$val);
            }
        }
    }

    //データチェック
    function dataCheck() {
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if (!$this->dataCheck()) {
            return;
        }
        if ($this->cmd == "update") {
            knjp805Query::getUpdateQuery($this);
        } else {
            knjp805Query::getUpdateTestQuery($this);
        }
        return;
    }
}
?>
