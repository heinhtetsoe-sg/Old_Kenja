<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja143pModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knja143pModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種
                             "DISP"             =>  VARS::post("DISP"),              //表示選択
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),        //発行日
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),        //有効期限
                             "SORT_DIV"         =>  VARS::post("SORT_DIV"),          //ソート順
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->selectleft    = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");

        $this->cmd         = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->field["RISHUU"] = '';
        }

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

