<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp952Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp952Model()
    {
    }

    function init()
    {
        $this->field = array("OUTGO_L_M_CD"     =>  VARS::post("OUTGO_L_M_CD"),     //支出科目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "CHANGE_CLASS"     =>  VARS::post("CHANGE_CLASS"),     //クラス毎に出力
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
