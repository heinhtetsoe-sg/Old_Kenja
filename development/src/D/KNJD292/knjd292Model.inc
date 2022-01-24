<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd292Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjd292Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "YEAR_DIV"         =>  VARS::post("YEAR_DIV"),         //出力年度区分
                             "PRINT_SHUKEI"     =>  VARS::post("PRINT_SHUKEI"),     //集計を印字する
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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
