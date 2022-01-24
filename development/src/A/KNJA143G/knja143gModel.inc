<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja143gModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knja143gModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "RIREKI_CODE"      =>  VARS::post("RIREKI_CODE"),       //履修登録日
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),        //発行日
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),        //有効期限
                             "RISHUU"           =>  VARS::post("RISHUU"),            //履修登録者出力
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        $this->cmd         = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->field["RISHUU"] = '';
        }

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
