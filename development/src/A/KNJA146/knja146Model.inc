<?php

require_once('for_php7.php');

// kanji=漢字
class knja146Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt  = array();
    var $selectleft;
    var $selectleftval;

    function knja146Model()
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
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),       //有効期限(開始)
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),       //有効期限(終了)
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
    }

    function clean()
    {
    }
}
?>
