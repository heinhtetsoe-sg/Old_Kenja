<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh343Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh343Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATA_DIV"           =>  VARS::post("DATA_DIV"),           //データ種別
                             "CATEGORY_IS_CLASS"  =>  VARS::post("CATEGORY_IS_CLASS"),  //表示区分
                             "MOCKCD"             =>  VARS::post("MOCKCD"),             //テスト名称
                             "GRADE"              =>  VARS::post("GRADE"),              //学年
                             "HR_CLASS"           =>  VARS::post("HR_CLASS"),           //組
                             "JUNI"               =>  VARS::post("JUNI"),               //順位の基準点
                             "SELECT_DIV"         =>  VARS::post("SELECT_DIV"),         //選択区分
                             "DBNAME"             =>  VARS::post("DBNAME")              //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
//        $this->getPropertiesAll();
//        //初期値の設定
//        $this->subclassGroup    = (strlen($this->Properties["useSubclassGroup"])) ? $this->Properties["useSubclassGroup"] : 0;
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