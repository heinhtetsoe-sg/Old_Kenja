<?php

require_once('for_php7.php');

class knja143tModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knja143tModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),             //学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "ISSUED_DATE"      =>  VARS::post("ISSUED_DATE"),       //発行日
                             "YUKOU_KIGEN"      =>  VARS::post("YUKOU_KIGEN"),       //有効期限
                             "POROW"            =>  VARS::post("POROW"),             //行
                             "POCOL"            =>  VARS::post("POCOL"),             //列
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
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
    function clean() {
    }
}
?>

