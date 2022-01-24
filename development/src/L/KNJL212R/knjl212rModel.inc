<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl212rModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl212rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("TESTDIV"      =>  VARS::post("TESTDIV"),   //入試区分
                             "OUTKANA"      =>  VARS::post("OUTKANA"),   //氏名かな
                             "OUTSCHL"      =>  VARS::post("OUTSCHL"),   //出身学校
                             "OUTSEX"       =>  VARS::post("OUTSEX")     //性別
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
