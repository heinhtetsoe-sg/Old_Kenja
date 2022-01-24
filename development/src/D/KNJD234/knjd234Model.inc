<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd234Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd234Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init()
    {
        $this->field = array("YEAR"             => VARS::post("YEAR"),              //年度
                             "NOW_SEM"          => VARS::post("NOW_SEM"),           //処理学期
                             "COMBO_SEM"        => VARS::post("COMBO_SEM"),         //選択学期
                             "GRADE"            => VARS::post("GRADE"),             //学年
                             "ASSESS1"          => VARS::post("ASSESS1"),           //評定平均（成績優良者）
                             "ASSESS1_DETAIL"   => VARS::post("ASSESS1_DETAIL"),    //詳細リスト（成績優良者）
                             "ASSESS2"          => VARS::post("ASSESS2"),           //評定平均（成績不振者）
                             "ASSESS2_DETAIL"   => VARS::post("ASSESS2_DETAIL"),    //詳細リスト（成績不振者）
                             "VALUE"            => VARS::post("VALUE"),             //評定（成績不振者）
                             "COUNT"            => VARS::post("COUNT"),             //科目数（成績不振者）
                             "UNSTUDY"          => VARS::post("UNSTUDY"),           //未履修（成績不振者）
                             "DATE"             => VARS::post("DATE"),              //異動対象日付
                             "LOGIN_DATE"       => VARS::post("LOGIN_DATE"),        //処理日付
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
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
