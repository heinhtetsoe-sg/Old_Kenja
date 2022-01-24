<?php

require_once('for_php7.php');

class knjd192qModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $useSlumpD048;

    function knjd192qModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "SUB_TESTCD"           =>  VARS::post("SUB_TESTCD"),           //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "SDATE"                =>  VARS::post("SDATE"),                 //出欠集計範囲(累計･学期)ラジオボタン
                             "DATE"                 =>  VARS::post("DATE"),                 //出欠集計範囲(累計･学期)ラジオボタン
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "NOT_PRINT_RANK"       =>  VARS::post("NOT_PRINT_RANK"),       //順位を出力しない
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->Properties["checkKettenDiv"] = '';
    }

    function clean() {}
}
?>
