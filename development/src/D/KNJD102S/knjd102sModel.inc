<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd102sModel extends Model {
    var $field       = array();
    var $select_data = array();
    var $cmd;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd102sModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"           =>  VARS::post("DATE"),           //異動対象日付
                             "YEAR"           =>  VARS::post("YEAR"),           //年度
                             "SEMESTER"       =>  VARS::post("SEMESTER"),       //学期コード
                             "TESTCD"         =>  VARS::post("TESTCD"),         //テスト名
                             "OUTPUT_KIJUN"   =>  VARS::post("OUTPUT_KIJUN"),   //順位の基準点
                             "MODE1"          =>  VARS::post("MODE1"),          //順位・平均対象
                             "MODE2"          =>  VARS::post("MODE2"),          //分布票種類
                             "DISABLE_MOSI"   =>  VARS::post("DISABLE_MOSI"),   //模試データ出力
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"), //年組
                             "DBNAME"         =>  VARS::post("DBNAME")          //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

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
