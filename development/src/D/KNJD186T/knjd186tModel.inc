<?php

require_once('for_php7.php');

class knjd186tModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd186tModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
?>
