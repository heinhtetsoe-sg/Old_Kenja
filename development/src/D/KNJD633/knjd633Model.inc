<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd633Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjd633Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"     =>  VARS::post("YEAR"),     //年度
                             "SEMESTER" =>  VARS::post("SEMESTER"), //学期
                             "GRADE"    =>  VARS::post("GRADE"),    //学年
                             "DATA_DIV" =>  VARS::post("DATA_DIV"), //出力データ
                             "DATA_YEAR" =>  VARS::post("DATA_YEAR"), //出力データ(年度)
                             "SORT"     =>  VARS::post("SORT"),     //出力順
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
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
