<?php

require_once('for_php7.php');

class knjd615fModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $schoolName;

    function knjd615fModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テストコード
                             "DATE_DIV"         =>  VARS::post("DATE_DIV"),         //出欠集計範囲（累計・学期）ラジオボタン
                             "DATE"             =>  VARS::post("DATE"),             //出欠集計終了日付
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "ORDER"            =>  VARS::post("ORDER"),            //表示順
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //チェックボックス選択データ
                             "OUTPUT_SOUGOU"    =>  VARS::post("OUTPUT_SOUGOU"),    //チェックボックス選択データ
                             "OUTPUT_KEKKA"     =>  VARS::post("OUTPUT_KEKKA"),     //チェックボックス選択データ
                             "OUTPUT_RANK"      =>  VARS::post("OUTPUT_RANK"),      //総合順位出力ラジオボタン
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }
}
?>
