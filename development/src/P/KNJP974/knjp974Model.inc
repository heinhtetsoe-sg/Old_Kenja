<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp974Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp974Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "MONTH"        =>  VARS::post("MONTH"),
                             "FROM_DATE"    =>  VARS::post("FROM_DATE"),
                             "TO_DATE"      =>  VARS::post("TO_DATE")

                     );

        $this->cmd = VARS::request("cmd");

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
