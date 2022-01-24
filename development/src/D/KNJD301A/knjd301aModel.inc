<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd301aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd301aModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "OUTPUT_KIJUN"     =>  VARS::post("OUTPUT_KIJUN"),     //順位の基準点ラジオボタン
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND")       //校種
                            );

        $this->field['OUTPUT_PATERN'] = 4;

        $this->cmd         = VARS::request("cmd");
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
