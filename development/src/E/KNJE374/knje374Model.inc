<?php

require_once('for_php7.php');

class knje374Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knje374Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "STUDENT"          =>  VARS::post("STUDENT"),          //対象生徒選択
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //学校種別
                             "GRD_YEAR"         =>  VARS::post("GRD_YEAR"),         //卒業年度
                             "REC_SEMESTER"     =>  VARS::post("REC_SEMESTER"),     //学期・学年末成績
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "STANDARD"         =>  VARS::post("STANDARD"),         //順位の基準点
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力内容
                             "ATTEND_SDATE"     =>  VARS::post("ATTEND_SDATE"),     //出欠集計範囲（開始日付）
                             "ATTEND_EDATE"     =>  VARS::post("ATTEND_EDATE"),     //出欠集計範囲（終了日付）
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    function clean()
    {
    }
}
?>
