<?php

require_once('for_php7.php');

class knjb1257Model extends Model {
    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;
    var $year;
    var $rirekiCode;

    function knjb1257Model() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->field = $_POST;
        /* 学年・学級に分割 */
        list($this->field["GRADE"],$this->field["HR_CLASS"]) = explode("_",$this->field["GRADE_CLASS"]);
        $this->field["SCHREGNO_ARRAY"] = explode(",",$this->field["SCHREGNO_ALL"]);
        //年度
        $this->year = VARS::post("YEAR");
        //学期
        $this->semester = VARS::post("SEMESTER");
        //履修履歴コード
        $this->rirekiCode = VARS::post("RIREKI_CODE");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /* 入力項目チェック */
    function validateInput() {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel() {
        if ($this->validateInput()){
            //更新
            knjb1257Query::getUpdateQuery($this);

            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
