<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl222rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)
        
    /**
     * メンバ変数の初期化
     */
    function knjl222rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV")         //入試区分
                     );

        $this->cmd         = VARS::request("cmd");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>