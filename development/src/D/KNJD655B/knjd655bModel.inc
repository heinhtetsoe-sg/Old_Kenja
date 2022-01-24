<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd655bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjd655bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTCD"           =>  VARS::post("TESTCD"),           //テスト
                             "SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "EDATE"            =>  VARS::post("EDATE"),            //出欠集計終了日付
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),         //順位ラジオボタン
                             "RANK_KIJUN"       =>  VARS::post("RANK_KIJUN"),       //順位の基準点ラジオボタン
                             "RANK_NO"          =>  VARS::post("RANK_NO"),          //成績上位者
                             "KANSAN"           =>  VARS::post("KANSAN"),           //100点に換算する
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
