<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja143wModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knja143wModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "GRADE"            =>  VARS::post("GRADE"),             //学年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),        //発行日
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),        //有効期限（教職員のみ）
                             "TAISHOUSHA"       =>  VARS::post("TAISHOUSHA"),        //1:生徒, 2:教職員
                             "SEL_CLASSTYPE"    =>  VARS::post("SEL_CLASSTYPE"),     //1:クラス選択、2:個人選択
                             "SCHKIND"          =>  VARS::post("SCHKIND"),           //校種
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

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

