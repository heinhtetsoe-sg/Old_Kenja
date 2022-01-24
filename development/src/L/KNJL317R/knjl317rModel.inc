<?php

require_once('for_php7.php');

class knjl317rModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
    var $fixApplicantDiv;

    function knjl317rModel()
    {
    }

    function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),      //入試区分
                             "OUTKEISYA"    =>  VARS::post("OUTKEISYA"),    //傾斜
                             "INC_KASAN"    =>  VARS::post("INC_KASAN"),    //加算点含む
                             "MAX_SCORE"    =>  VARS::post("MAX_SCORE"),    //最高点
                             "MIN_SCORE"    =>  VARS::post("MIN_SCORE"),    //最低点
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");
    }
}
?>
