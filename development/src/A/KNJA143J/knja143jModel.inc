<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja143jModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knja143jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),                 //年度
                             "GRADE_HR_CLASS"     =>  VARS::post("GRADE_HR_CLASS"),       //年組
                             "SEMESTER"           =>  VARS::post("SEMESTER"),             //学期
                             "OUTPUT"             =>  VARS::post("OUTPUT"),               //フォーム
                             "TERM_SDATE"         =>  VARS::post("TERM_SDATE"),           //発行日
                             "PRINT_SCHOOL_STAMP" =>  VARS::post("PRINT_SCHOOL_STAMP"),   //校印を印刷する
                             "PRINT_STATION"      =>  VARS::post("PRINT_STATION"),        //通学区間
                             "DBNAME"             =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
