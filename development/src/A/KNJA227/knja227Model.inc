<?php

require_once('for_php7.php');


class knja227Model extends Model {
    var $field      = array();
    var $cmd;

    function knja227Model() {
    }

    function init() {
        $this->field = array("SEARCH_PRISCHOOLCD"       =>  VARS::post("SEARCH_PRISCHOOLCD"),       //塾コード
                             "SEARCH_PRISCHOOL_NAME"    =>  VARS::post("SEARCH_PRISCHOOL_NAME"),    //塾名
                             "SEARCH_PRISCHOOL_CLASS_NAME"  =>  VARS::post("SEARCH_PRISCHOOL_CLASS_NAME"),  //教室名
                             "DBNAME"                   =>  VARS::post("DBNAME")                    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
