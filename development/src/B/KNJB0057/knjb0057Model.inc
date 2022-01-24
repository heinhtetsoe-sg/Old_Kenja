<?php

require_once('for_php7.php');

class knjb0057Model extends Model {
    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;
    var $year;

    function knjb0057Model() {
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
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /* 入力項目チェック */
    function validateInput() {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel() {
        if ($this->validateInput()){
            //更新
            knjb0057Query::getUpdateQuery($this);

            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
