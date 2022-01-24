<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja082aModel extends Model {
    var $field      = array();
    var $cmd;
    var $grade;
    var $nextyear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja082aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->nextyear = CTRL_YEAR + 1;
    }
    function init()
    {
        $this->cmd    = VARS::request("cmd");
        $this->grade  = VARS::post("GRADE");
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
