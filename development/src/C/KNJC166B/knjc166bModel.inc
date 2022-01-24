<?php

require_once('for_php7.php');

class knjc166bModel extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;

    function knjc166bModel()
    {
        $this->field = array();
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array(
            "GRADE"            => VARS::post("GRADE"),        //学年
            "HR_CLASS"         => VARS::post("HR_CLASS"),     //年組
            "KAIKIN_CD"        => VARS::post("KAIKIN_CD"),    //皆勤区分
        );

        if ($this->cmd == "changeGrade") {
            $this->field["HR_CLASS"] = "";
        }

    }

    /* 入力項目チェック */
    function validateInput()
    {

    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        knjc166bQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }

}
?>
