<?php

require_once('for_php7.php');

class knjd131pModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd131pModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }
    function init() {
        $this->field = array("YEAR"                     =>  VARS::post("YEAR"),                     //年度
                             "SEMESTER"                 =>  VARS::post("SEMESTER"),                 //学期
                             "GRADE"                    =>  VARS::post("GRADE"),                    //学年
                             "DBNAME"                   =>  VARS::post("DBNAME")                    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
