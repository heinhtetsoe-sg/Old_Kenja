<?php

require_once('for_php7.php');

class knji100cModel extends Model {
    var $cmd;

    function knji100cModel() {
    }

    function init() {
        $this->getPropertiesAll(); //使用するテーブルを取得
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }
}
?>
