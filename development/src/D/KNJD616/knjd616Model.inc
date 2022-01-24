<?php

require_once('for_php7.php');

class knjd616Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;

    function knjd616Model() {}
    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード

        $this->field = array("YEAR"           =>  VARS::post("YEAR"),           //年度
                             "SEMESTER"       =>  VARS::post("SEMESTER"),       //学期
                             "GRADE"          =>  VARS::post("GRADE"),          //学年
                             "TESTKINDCD"     =>  VARS::post("TESTKINDCD"),     //テスト
                             "OUT_PUT_KEKKA"  =>  VARS::post("OUT_PUT_KEKKA"),  //(1日欠席の時は欠課を集計する)チェックボックス
                             "KARAGYOU_NASI"  =>  VARS::post("KARAGYOU_NASI"),  //(空行を詰めて印字)チェックボックス
                             "HYOUTEI_HEIKIN" =>  VARS::post("HYOUTEI_HEIKIN"), //評定平均
                             "JORETU_DIV"     =>  VARS::post("JORETU_DIV"),     //序列
                             "JORETU_BASE"    =>  VARS::post("JORETU_BASE"),    //序列の基準点
                             "DBNAME"         =>  VARS::post("DBNAME")          //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    function clean() {}
}
?>
