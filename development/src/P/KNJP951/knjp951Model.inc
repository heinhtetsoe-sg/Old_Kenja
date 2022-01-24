<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp951Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp951Model()
    {
    }

    function init()
    {
        $this->field = array("INCOME_L_M_CD"    =>  VARS::post("INCOME_L_M_CD"),    //収入科目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読み込み
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
