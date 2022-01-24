<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd617iModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd617iModel()
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
                             "OUTPUT_ORDER"     =>  VARS::post("OUTPUT_ORDER"),     //出力順ラジオボタン
                             "OUTPUT4"          =>  VARS::post("OUTPUT4"),          //単位保留チェック
                             "OUTPUT5"          =>  VARS::post("OUTPUT5"),          //総合的な学習の時間チェック
                             "OUTPUT6"          =>  VARS::post("OUTPUT6"),          //欠点に追指導を含むチェック
                             "OUTPUT7"          =>  VARS::post("OUTPUT7"),          //欠点者数に欠査者を含めないチェック
                             "OUTPUT_BIKO"      =>  VARS::post("OUTPUT_BIKO"),      //備考欄出力（出欠備考を出力）チェック
                             "OUTPUT_STUDENT_JISU" =>  VARS::post("OUTPUT_STUDENT_JISU"), //欠課時数を分数表記する
                             "BIKO_KIND"        =>  VARS::post("BIKO_KIND"),        //備考欄出力選択 全て/学期から/年間まとめ
                             "TAKESEME"         =>  VARS::post("TAKESEMES"),        //前/後期科目選択（変動型のみ）
                             "PRINT_KEKKA0"     =>  VARS::post("PRINT_KEKKA0"),     //欠席0欠課0を表記する
                             "PRINT_REPORT"     =>  VARS::post("PRINT_REPORT"),     //全クラスの成績表を出力する
                             "NOT_EMPTY_LINE"   =>  VARS::post("NOT_EMPTY_LINE"),   //空行を詰めて印字
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>