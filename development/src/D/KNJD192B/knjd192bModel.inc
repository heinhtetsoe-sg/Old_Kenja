<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd192bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd192bModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "ASSESS_LEVEL"         =>  VARS::post("ASSESS_LEVEL"),         //段階値出力
                             "ASSESS_LEVEL_REP"     =>  VARS::post("ASSESS_LEVEL_REP"),     //読替あり
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ区分
                             "RANK_DIV"             =>  VARS::post("RANK_DIV"),             //順位の基準点
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
