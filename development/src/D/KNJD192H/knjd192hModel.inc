<?php

require_once('for_php7.php');

class knjd192hModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $useSlumpD048;

    function knjd192hModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "SUB_TESTCD"           =>  VARS::post("SUB_TESTCD"),           //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //平均・席次
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "COUNT_SURU"           =>  VARS::post("COUNT_SURU"),           //欠席をカウントするかのフラグ
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "SUBCLASS_MAX"         =>  VARS::post("SUBCLASS_MAX"),         //最大科目数
                             "TYUI_TYOUKA"          =>  VARS::post("TYUI_TYOUKA"),          //注意・超過ラジオボタン
                             "TEST_ONLY"            =>  VARS::post("TEST_ONLY"),            //試験科目のみ出力する
                             "NOT_PRINT_RANK"       =>  VARS::post("NOT_PRINT_RANK"),       //順位を出力しない
                             "SAME_ATTENDNO_PAGE"   =>  VARS::post("SAME_ATTENDNO_PAGE"),   //同一出席番号をグループで出力
                             "FORM"                 =>  VARS::post("FORM"),                 //出力科目数
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
        $rtnRow = knjd192hQuery::getNameMstD048();
        $this->useSlumpD048 = $rtnRow["NAMESPARE1"];
    }

    function clean() {}
}
?>
