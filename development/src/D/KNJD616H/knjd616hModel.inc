<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjd616hModel extends Model {
    var $field    = array();
    var $cmd;

    function knjd616hModel() {
    }

    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "DATA_DIV"         =>  VARS::post("DATA_DIV"),          //ラジオ
                             "RANK"             =>  VARS::post("RANK"),              //順位
                             "HYOUTEI"          =>  VARS::post("HYOUTEI"),           //評定（上位）
                             "HYOUTEI_2"        =>  VARS::post("HYOUTEI_2"),         //評定（下位）
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
