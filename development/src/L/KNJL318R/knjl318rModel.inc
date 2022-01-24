<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl318rModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $fixApplicantDiv;

    /**
     * メンバ変数の初期化
     */
    function knjl318rModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV"),
                             "CONSENT"      =>  VARS::post("CONSENT"),
                             "OUTKEISYA"    =>  VARS::post("OUTKEISYA"),
                             "TARGET"       =>  VARS::post("TARGET"),
                             "INC_KASAN"    =>  VARS::post("INC_KASAN"),
                             "NAIDAKU_PAGE" =>  VARS::post("NAIDAKU_PAGE")
                            );

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
