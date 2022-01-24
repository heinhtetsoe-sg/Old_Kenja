<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl381jModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl381jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
    }

    function init()
    {               
        $this->field = array("TITLE"    =>  VARS::post("TITLE"),       //種類
                             "FLG1"     =>  VARS::post("FLG1"),        //提出書類
                             "FLG2"     =>  VARS::post("FLG2"));       //電話番号

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