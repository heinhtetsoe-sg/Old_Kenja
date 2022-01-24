<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm432Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjm432Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"     =>  VARS::post("YEAR"),     //年度
                             "SEMESTER" =>  VARS::post("SEMESTER"), //学期
                     );
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
