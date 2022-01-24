<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd624vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjd624vModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),         //データ区分
                             "TESTCD"           =>  VARS::post("TESTCD"),           //テスト
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "MAJOR"            =>  VARS::post("MAJOR"),            //学科
                             "SUBSEL"           =>  VARS::post("SUBSEL"),           //選択
                             "PRINT_ORDER_SCORE" =>  VARS::post("PRINT_ORDER_SCORE"), //成績順に出力する
                             "RADIO"            =>  VARS::post("RADIO"),            //年組科目選択
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
	    $this->selectdata = VARS::post("selectdata");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>