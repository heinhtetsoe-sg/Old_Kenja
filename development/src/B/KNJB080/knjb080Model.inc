<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjb080Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjb080Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }
    function init()
    {               
        $this->field = array("TITLE"        =>  VARS::post("TITLE"),        //時間割コンボボックス選択データ
                             "FACCD_NAME1"  =>  VARS::post("FACCD_NAME1"),  //施設コンボボックス１
                             "FACCD_NAME2"  =>  VARS::post("FACCD_NAME2"),  //施設コンボボックス２
                             "DBNAME"       =>  VARS::post("DBNAME"),       //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
