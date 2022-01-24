<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja133eModel extends Model {
    var $field          = array();
    var $select_data    = array();
    var $cmd;
    var $control;       //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knja133eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //印刷日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>