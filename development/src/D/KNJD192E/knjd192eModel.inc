<?php

require_once('for_php7.php');

class knjd192eModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $useSlumpD048;

    function knjd192eModel() {
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
                             "SUBCLASS_MAX"         =>  VARS::post("SUBCLASS_MAX"),         //最大科目数
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
        $rtnRow = knjd192eQuery::getNameMstD048();
        $this->useSlumpD048 = $rtnRow["NAMESPARE1"];
    }

    function clean() {}
}
?>
