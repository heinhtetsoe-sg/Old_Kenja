<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd615rModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd615rModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "OUTPUT_PATERN"    =>  VARS::post("OUTPUT_PATERN"),    //帳票パターンラジオボタン
                             "OUTPUT_RANK"      =>  VARS::post("OUTPUT_RANK"),      //総合順位出力ラジオボタン
                             "OUTPUT_KIJUN"     =>  VARS::post("OUTPUT_KIJUN"),     //順位の基準点ラジオボタン
                             "KKESSEKI"         =>  VARS::post("KKESSEKI"),         //皆勤出欠指定
                             "KCHIKOKU_SOUTAI"  =>  VARS::post("KCHIKOKU_SOUTAI"),  //皆勤出欠指定
                             "KCHIKOKU"         =>  VARS::post("KCHIKOKU"),         //皆勤出欠指定
                             "KSOUTAI"          =>  VARS::post("KSOUTAI"),          //皆勤出欠指定
                             "KKEKKA"           =>  VARS::post("KKEKKA"),           //皆勤出欠指定
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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