<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl223rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)
        
    /**
     * メンバ変数の初期化
     */
    function knjl223rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "S_KESSEKI"    =>  VARS::post("S_KESSEKI"),      //欠席日数開始
                             "E_KESSEKI"    =>  VARS::post("E_KESSEKI"),      //欠席日数終了
                             "S2_KESSEKI"   =>  VARS::post("S2_KESSEKI")      //二つ目の条件の欠席日数開始
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
