<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd106cModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjd106cModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "JUNI"                 =>  VARS::post("JUNI"),                 //順位
                             "SELECT_DIV"           =>  VARS::post("SELECT_DIV"),           //選択区分
                             "USE_ADDRESS"          =>  VARS::post("USE_ADDRESS"),          //住所選択
                             "FORM_DIV"             =>  VARS::post("FORM_DIV"),             //フォーム選択
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "ASSESS_LEVEL_PRINT"   =>  VARS::post("ASSESS_LEVEL_PRINT"),   //段階値出力チェック
                             "DEVIATION_PRINT"      =>  VARS::post("DEVIATION_PRINT"),      //偏差値出力
                             "JUNI_PRINT"           =>  VARS::post("JUNI_PRINT"),           //順位出力
                             "SUBMIT_DATE"          =>  VARS::post("SUBMIT_DATE"),          //提出日
                             "SCHOOLKIND"           =>  VARS::post("SCHOOLKIND"),           //学校種別
                             "DBNAME"               =>  VARS::post("DBNAME"),               //DB変更に備えて
                             "MOCKCD"               =>  VARS::post("MOCKCD")                //テスト名称
                            );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        //初期値の設定
        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
        $this->useKnjd106cJuni1 = (strlen($this->Properties["useKnjd106cJuni1"])) ? $this->Properties["useKnjd106cJuni1"] : 1;
        $this->useKnjd106cJuni2 = (strlen($this->Properties["useKnjd106cJuni2"])) ? $this->Properties["useKnjd106cJuni2"] : 2;
        $this->useKnjd106cJuni3 = (strlen($this->Properties["useKnjd106cJuni3"])) ? $this->Properties["useKnjd106cJuni3"] : 3;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
