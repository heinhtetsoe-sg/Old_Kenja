<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc181kModel extends Model {

    var $cmd;       //一覧表示時のcmd
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjc181kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {               
        $this->field = array("OUTPUT"   =>  VARS::post("OUTPUT"),       //集計範囲
                             "DBNAME"   =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");         //一覧表示時のcmd
    }
}
?>
