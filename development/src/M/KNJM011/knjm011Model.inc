<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm011Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjm011Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "BUSU"             =>  VARS::post("BUSU"),             //部数
                             "DBNAME"           =>  VARS::post("DBNAME"));          //DB変更に備えて

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
