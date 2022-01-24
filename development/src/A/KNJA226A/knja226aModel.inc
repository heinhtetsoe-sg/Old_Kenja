<?php

require_once('for_php7.php');

class knja226aModel extends Model {
    var $field = array();
    var $cmd;

    function knja226aModel() {
    }

    function init() {
        $this->field = array("SCHOOL_KIND"      => VARS::post("SCHOOL_KIND"),       //校種
                             "GRADE"            => VARS::post("GRADE"),             //学年
                             "PRINT_FURIGANA"   => VARS::post("PRINT_FURIGANA"),    //ふりがなを出力する
                             "KAIPAGE_GRADE"    => VARS::post("KAIPAGE_GRADE"),     //学年毎に改ページする
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //変数をクリアします
    function clean() {
    }
}
?>
