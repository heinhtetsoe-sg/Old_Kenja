<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd233Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjd233Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "OUTPUT_DIV"   =>  VARS::post("OUTPUT_DIV"),   //出力種別
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
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
