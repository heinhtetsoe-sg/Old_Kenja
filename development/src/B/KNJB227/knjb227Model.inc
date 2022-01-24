<?php

require_once('for_php7.php');

class knjb227Model extends Model {
    var $field      = array();
    var $selectdata = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb227Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
        $this->year = CTRL_YEAR;
        $this->field = array("CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //対象一覧区分
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "OUTPUT"               =>  VARS::post("OUTPUT"),               //出力ラジオ 1:予定講座 2:講座
                             "DATE"                 =>  VARS::post("DATE"),                 //講座日付
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    function clean() {}
}
?>