<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja200aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja200aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("DATA_DIV"         =>  VARS::post("DATA_DIV"),
                             "DATA_CMB"         =>  VARS::post("DATA_CMB"),
                             "DATE"             =>  VARS::post("DATE"),
                             "DATEFT"           =>  VARS::post("DATEFT"),
                             "YEAR"             =>  VARS::post("YEAR"),
                             "SCHKIND"          =>  VARS::post("SCHKIND"),
                             "SEMESTER"         =>  VARS::post("SEMESTER"),
                             "TEL"              =>  VARS::post("TEL"),
                             "DBNAME"           =>  VARS::post("DBNAME"),
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->getPrgDefaultVal("KNJA200A", SCHOOLCD, SCHOOLKIND);
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
