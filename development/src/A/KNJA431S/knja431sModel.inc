<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja431sModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knja431sModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"     =>  VARS::post("YEAR"),     //年度
                             "GRADE"    =>  VARS::post("GRADE"),    //学年
                             "DBNAME"   =>  VARS::post("DBNAME")    //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
