<?php

require_once('for_php7.php');
class knjd186sModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd186sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠開始日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "BIKO_TERM_TYPE"   =>  VARS::post("BIKO_TERM_TYPE"),   //出欠集計単位
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
