<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh120Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjh120Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //年組
                            "HS_KUBUN_FROM"     =>  VARS::post("SUBCLASSCD_FROM"),    //科目区分選択（FROM）
                            "HS_KUBUN_TO"       =>  VARS::post("SUBCLASSCD_TO"),      //科目区分選択（TO）
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        //プロパティファイル
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
