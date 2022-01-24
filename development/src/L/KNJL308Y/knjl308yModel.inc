<?php
class knjl308yModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ

    function knjl308yModel()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init()
    {
        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "HALL_DIV"         =>  VARS::post("HALL_DIV"),         //会場区分
                             "SCH_PTRN"	        =>  VARS::post("SCH_PTRN"),         //時間割パターン
                             "RENBAN"           =>  VARS::post("RENBAN"),           //通し番号連番
                             "APP_HOLD"         =>  VARS::post("APP_HOLD"),         //入試区分
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
