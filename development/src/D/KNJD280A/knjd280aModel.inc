<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd280aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
        
    /**
     * メンバ変数の初期化
     */
    function knjd280aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),     //年度
                             "NINTEI"           =>  VARS::post("NINTEI"),
                             "NINTEI_SEME1"     =>  VARS::post("NINTEI_SEME1"),
                             "NINTEI_SEME2"     =>  VARS::post("NINTEI_SEME2"),
                             "KARI"             =>  VARS::post("KARI"),
                             "KARI_SEME1"       =>  VARS::post("KARI_SEME1"),
                             "KARI_SEME2"       =>  VARS::post("KARI_SEME2"),
                             "CLASS_SELECTED"   =>  VARS::post("CLASS_SELECTED"),
                             "DATE"             =>  VARS::post("DATE"),
                             "DBNAME"           =>  VARS::post("DBNAME"),   //DB変更に備えて
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


    /**
     * 実行モデルを返します
    */
    function &getExecModel() {
        // 学年成績
        knjd280aQuery::execute($this);
        //実行履歴
        knjd280aQuery::executeRireki($this);
    }
}
?>
