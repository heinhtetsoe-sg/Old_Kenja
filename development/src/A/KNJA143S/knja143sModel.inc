<?php

require_once('for_php7.php');

// kanji=漢字
class knja143sModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt  = array();
    var $selectleft;
    var $selectleftval;

    function knja143sModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //フォーム
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //発行日
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),       //提出期限
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean()
    {
    }
}
?>
