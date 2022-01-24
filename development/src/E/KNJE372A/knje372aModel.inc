<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje372aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje372aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),             //年度
                            "GAKKI"                =>  VARS::post("GAKKI"),            //学期コード
                            "DATE"                 =>  VARS::post("DATE"),             //対象年月日
                            "DATE"                 =>  VARS::post("DATE2"),            //提出期限
                            "OUT_DIV"              =>  VARS::post("OUT_DIV"),          //希望ラジオボタン
                            "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
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
