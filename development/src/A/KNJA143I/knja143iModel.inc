<?php

require_once('for_php7.php');

// kanji=漢字
class knja143iModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt  = array();
    var $selectleft;
    var $selectleftval;

    function knja143iModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //新入生/在籍
                             "SORT_DIV"         =>  VARS::post("SORT_DIV"),         //出力順
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),       //有効期限(開始)
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),       //有効期限(終了)
                             "BUSUU"            =>  VARS::post("BUSUU"),            //部数
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
        if ($this->cmd == 'change_year') {
            unset($this->field["TERM_SDATE"]);
            unset($this->field["TERM_EDATE"]);
        }
        $this->getPropertiesAll();
    }

    function clean()
    {
    }
}
?>
