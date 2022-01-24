<?php

require_once('for_php7.php');

class knjc162aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;

    function knjc162aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER"    =>  VARS::post("SEMESTER"),    //学期
                             "GRADE"       =>  VARS::post("GRADE"),       //学年
                             "SDATE"       =>  VARS::post("SDATE"),       //対象日(開始)
                             "EDATE"       =>  VARS::post("EDATE"),       //対象日(終了)
                             "DBNAME"      =>  VARS::post("DBNAME"),      //DB変更に備えて
                             "TYUI_TYOUKA" =>  VARS::post("TYUI_TYOUKA")  //注意・超過ラジオボタン
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();

        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }
    }

    function clean() {}
}
?>
