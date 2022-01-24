<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh563Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclassGroup;
    var $control;        //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjh563Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "PROFICIENCYDIV"       =>  VARS::post("PROFICIENCYDIV"),       //データ種別
                             "PROFICIENCYCD"        =>  VARS::post("PROFICIENCYCD"),        //テスト名称
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //クラス
                             "TESTCD"               =>  VARS::post("TESTCD"),               //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //グループ
                             "JUNI"                 =>  VARS::post("JUNI"),                 //順位
                             "DEVIATION_PRINT"      =>  VARS::post("DEVIATION_PRINT"),      //偏差値出力
                             "JUNI_PRINT"           =>  VARS::post("JUNI_PRINT"),           //順位出力
                             "HOGOSHA_PRINT"        =>  VARS::post("HOGOSHA_PRINT"),        //保護者欄印刷
                             "SUBMIT_DATE"          =>  VARS::post("SUBMIT_DATE"),          //提出日
                             "HYOSHI_PRINT"         =>  VARS::post("HYOSHI_PRINT"),         //表紙を印刷しない
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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
