<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd615lModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd615lModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //出欠集計日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト種別
                             "OUTPUT_RANK"      =>  VARS::post("OUTPUT_RANK"),      //総合順位出力ラジオボタン
                             "OUTPUT_KIJUN"     =>  VARS::post("OUTPUT_KIJUN"),     //順位の基準点ラジオボタン
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //単位保留チェック
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),          //総合的な学習の時間チェック
                             "TAKESEME"         =>  VARS::post("TAKESEMES"),        //前/後期科目選択（変動型のみ）
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