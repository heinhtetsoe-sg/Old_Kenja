<?php

require_once('for_php7.php');

// kanji=漢字
class knja142aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt  = array();
    var $selectleft;
    var $selectleftval;

    function knja142aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SORT_DIV"         =>  VARS::post("SORT_DIV"),         //出力順
                             "DISP"             =>  VARS::post("DISP"),             //表示選択
                             "TERM_SDATE"       =>  VARS::post("TERM"),             //有効期限
                             "ROSEN"            =>  VARS::post("ROSEN"),            //経路
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
        $this->getPropertiesAll();
    }

    function clean()
    {
    }
}
?>
