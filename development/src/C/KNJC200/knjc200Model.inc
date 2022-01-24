<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc200Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjc200Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),                //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //ソート選択
                             "HR_NAME"          =>  VARS::post("HR_NAME")            //クラス選択
                            );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");

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
