<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg030Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjg030Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //年組
                            "DATE2"             =>  VARS::post("DATE2"),            //学籍処理日
                            "DATE"              =>  VARS::post("DATE"),             //記載日（証明日）
                            "SEMESTER"          =>  VARS::post("SEMESTER"),         //学期コード
                            "SEME_DATE"         =>  VARS::post("SEME_DATE"),        //学期開始日
                            "SEKI"              =>  VARS::post("SEKI"),             //記載責任者
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
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
