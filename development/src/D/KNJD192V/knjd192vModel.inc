<?php

require_once('for_php7.php');

class knjd192vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $useSlumpD048;

    function knjd192vModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "SUB_TESTCD"           =>  VARS::post("SUB_TESTCD"),           //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "DATE_DIV"             =>  VARS::post("DATE_DIV"),             //出欠集計範囲(累計･学期)ラジオボタン
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //平均・席次
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "COUNT_SURU"           =>  VARS::post("COUNT_SURU"),           //欠席をカウントするかのフラグ
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "SUBCLASS_MAX"         =>  VARS::post("SUBCLASS_MAX"),         //最大科目数
                             "TYUI_TYOUKA"          =>  VARS::post("TYUI_TYOUKA"),          //注意・超過ラジオボタン
                             "TEST_ONLY"            =>  VARS::post("TEST_ONLY"),            //試験科目のみ出力する
                             "NOT_PRINT_RANK"       =>  VARS::post("NOT_PRINT_RANK"),       //順位を出力しない
                             "NOT_PRINT_AVG"        =>  VARS::post("NOT_PRINT_AVG"),        //平均点を出力しない
                             "NOT_PRINT_KEKKA"      =>  VARS::post("NOT_PRINT_KEKKA"),      //欠課数を出力しない
                             "KEKKA_AS_INT"         =>  VARS::post("KEKKA_AS_INT"),         //整数で表示する
                             "SORT_CUTTING"         =>  VARS::post("SORT_CUTTING"),         //裁断用にソートして出力する
                             "ORDER_USE_ELECTDIV"   =>  VARS::post("ORDER_USE_ELECTDIV"),   //選択科目を後に出力する
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
        $rtnRow = knjd192vQuery::getNameMstD048();
        $this->useSlumpD048 = $rtnRow["NAMESPARE1"];
    }

    function clean() {}
}
?>
