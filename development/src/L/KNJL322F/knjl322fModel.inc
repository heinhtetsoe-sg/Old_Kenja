<?php

require_once('for_php7.php');

class knjl322fModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl322fModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "TESTCOUNT"    =>  VARS::post("TESTCOUNT"),        //入試回数
                             "COURSEDIV"    =>  VARS::post("COURSEDIV"),        //コース区分
                             "SORT"         =>  VARS::post("SORT"),             // 出力順
                             "ADVANCE_AMIKAKE"    =>  VARS::post("ADVANCE_AMIKAKE"),        //アドバンス変更希望者に網掛け
                             "ADVANCE_SCORE"=>  VARS::post("ADVANCE_SCORE"),        //アドバンス変更希望者に網掛け ○点以上
                             "NOT_SHOW_FORCE"   =>  VARS::post("NOT_SHOW_FORCE"),   //学力診断テストを表示しない
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
