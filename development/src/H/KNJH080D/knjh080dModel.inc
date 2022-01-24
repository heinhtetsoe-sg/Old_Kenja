<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh080dModel extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjh080dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),               //年度
                            "GRADE_HR_CLASS"      =>  VARS::post("GRADE_HR_CLASS"),     //年組
                            "SEMESTER"            =>  VARS::post("SEMESTER"),           //学期コード
                            "DBNAME"              =>  VARS::post("DBNAME"),             //DB変更に備えて
                            "PRINTPAGE1"          =>  VARS::post("PRINTPAGE1"),         //PDF印刷(基本情報)
                            "PRINTPAGE2"          =>  VARS::post("PRINTPAGE2"),         //PDF印刷(指導情報)
                            "PRINTPAGE3"          =>  VARS::post("PRINTPAGE3"),         //PDF印刷(活動情報)
                            "PROFICIENCY_TYPE"    =>  VARS::post("PROFICIENCY_TYPE"),   //データ種別
                            "PROFICIENCY_SUBJECT" =>  VARS::post("PROFICIENCY_SUBJECT") //実力科目
                     );


        $this->cmd = VARS::request("cmd");
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
