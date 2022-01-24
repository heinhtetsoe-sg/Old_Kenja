<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc166cModel extends Model {
    var $field = array();
    var $cmd;
    var $control;    //コントロールマスタ
    var $staffcd;    //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjc166cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"        => VARS::post("YEAR"),      //年度
                             "GRADE"       => VARS::post("GRADE"),     //学年
                             "KAIKIN_CD"   => VARS::post("KAIKIN_CD"), //皆勤コード
                             "DBNAME"      => VARS::post("DBNAME")     //DB変更に備えて
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
