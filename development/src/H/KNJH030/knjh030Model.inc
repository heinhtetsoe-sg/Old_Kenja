<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh030Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjh030Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),        //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "SEMESTER"            =>  VARS::post("SEMESTER"),    //学期コード
                             "DATE"               =>  VARS::post("DATE"),    //調査日付
                             "OUTPUT"          =>  VARS::post("OUTPUT"),        //フォーム選択
                             "PRINT_SCHREGNO"          =>  VARS::post("PRINT_SCHREGNO"),        //学籍番号を印字する
                             "DOCUMENTROOT"       =>  VARS::post("DOCUMENTROOT"),    //
//                           "DOCUMENTTYPE"       =>  VARS::post("DOCUMENTTYPE"),    //
                             "DBNAME"          =>  VARS::post("DBNAME")            //DB変更に備えて
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
