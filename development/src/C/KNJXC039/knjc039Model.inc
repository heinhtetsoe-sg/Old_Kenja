<?php

require_once('for_php7.php');

class knjc039Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjc039Model() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード

        $this->field = array("GRADE"    =>  VARS::post("GRADE"),  //学年
                             "SDATE"    =>  VARS::post("SDATE"),  //対象日(開始)
                             "EDATE"    =>  VARS::post("EDATE"),  //対象日(終了)
                             "OUTPUT"   =>  VARS::post("OUTPUT"), //出欠状況出力範囲
                             "DI_CD1"   =>  VARS::post("DI_CD1"), //公欠
                             "DI_CD2"   =>  VARS::post("DI_CD2"), //出停
                             "DI_CD3"   =>  VARS::post("DI_CD3"), //忌引
                             "DBNAME"   =>  VARS::post("DBNAME")  //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
