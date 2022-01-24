<?php

require_once('for_php7.php');

class knjd185wModel extends Model {
    var $field = array();
    var $cmd;
    var $selectdata;

    function knjd185wModel() {}

    function init() {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
