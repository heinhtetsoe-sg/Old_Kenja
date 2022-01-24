<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd233vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjd233vModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "MAJOR"        =>  VARS::post("MAJOR"),        //学科
                             "OUTPUT_DIV1"  =>  VARS::post("OUTPUT_DIV1"),  //出力 卒業認定対象者一覧
                             "OUTPUT_DIV2"  =>  VARS::post("OUTPUT_DIV2"),  //出力 卒業認定見込みが立たない対象者一覧
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
