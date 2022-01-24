<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd232Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd232Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),                //年度
                            "GAKKI"                =>    VARS::post("GAKKI"),            //学期コード
                            "GAKKI2"            =>    VARS::post("GAKKI2"),            //学期コンボ
                            "GRADE"                =>    VARS::post("GRADE"),            //学年
                            "SEME"                =>    VARS::post("SEME"),                //学期
                            "OUTPUT1"            =>  VARS::post("OUTPUT1"),            //成績優良者
                            "ASSESS1"            =>  VARS::post("ASSESS1"),            //評定平均（以上）
                            "OUTPUT8"            =>  VARS::post("OUTPUT8"),            //成績優良者　詳細リスト
                            "OUTPUT2"            =>  VARS::post("OUTPUT2"),            //成績不振者
                            "ASSESS2"            =>  VARS::post("ASSESS2"),            //成績不良評定
                            "COUNT2"            =>  VARS::post("COUNT2"),            //成績不良科目数
                            "UNSTUDY2"            =>  VARS::post("UNSTUDY2"),            //成績不良未履修科目数
                            "OUTPUT7"            =>  VARS::post("OUTPUT7"),            //成績不良　詳細リスト
                            "OUTPUT3"            =>  VARS::post("OUTPUT3"),            //皆勤者
                            "OUTPUT4"            =>  VARS::post("OUTPUT4"),            //出欠状況（優良）
                            "DATE"                =>  VARS::post("DATE"),                //皆出席基準日付
                            "LATE4"                =>  VARS::post("LATE4"),            //遅刻
                            "ABSENT4"            =>  VARS::post("ABSENT4"),            //欠席
                            "SUBCLASS_ABSENT4"    =>  VARS::post("SUBCLASS_ABSENT4"),    //欠課
                            "EARLY4"            =>  VARS::post("EARLY4"),            //早退   //add  04/07/13  yamauchi
                            "LATE5"                =>  VARS::post("LATE5"),            //遅刻
                            "ABSENT5"            =>  VARS::post("ABSENT5"),            //欠席
                            "SUBCLASS_ABSENT5"    =>  VARS::post("SUBCLASS_ABSENT5"),    //欠課
                            "EARLY5"            =>  VARS::post("EARLY5"),            //早退
                            "ORDER1"            =>  VARS::post("ORDER1"),            //遅刻ソート順
                            "ORDER2"            =>  VARS::post("ORDER2"),            //早退ソート順
                            "ORDER3"            =>  VARS::post("ORDER3"),            //欠席ソート順
                            "ORDER4"            =>  VARS::post("ORDER4"),            //欠課ソート順
                            "KYOUKA_SOUGOU1"    =>  VARS::post("KYOUKA_SOUGOU1"),   //教科・科目
                            "KYOUKA_SOUGOU2"    =>  VARS::post("KYOUKA_SOUGOU2"),   //総合的な時間
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
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
