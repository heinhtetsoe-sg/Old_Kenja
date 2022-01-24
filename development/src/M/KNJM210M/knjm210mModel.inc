<?php

require_once('for_php7.php');


class knjm210mModel extends Model {

    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjm210mModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {  
        $this->field = array("SCHREGNO"     =>  VARS::post("SCHREGNO"),     //学籍番号
                             "NAME"         =>  VARS::post("NAME"),         //氏名（漢字）
                             "NAME_KANA"    =>  VARS::post("NAME_KANA"),    //氏名（かな）
                             "GRADE"        =>  VARS::post("GRADE")         //学年
                     );
        if ($this->field["GRADE"]) 
            $this->field["GRADE"] = sprintf("%02d",$this->field["GRADE"]);
        $this->cmd         = VARS::request("cmd");
    }
}
?>
