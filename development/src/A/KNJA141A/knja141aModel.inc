<?php

require_once('for_php7.php');

class knja141aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;               //コントロールマスタ
    var $select_opt  = array(); //セレクトデータ
    var $selectleft;

    function knja141aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //フォーム
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),       //有効期限(開始)
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),       //有効期限(終了)
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') $this->select_opt = array();
        $this->getPropertiesAll();
    }

    function clean() {
    }
}
?>
