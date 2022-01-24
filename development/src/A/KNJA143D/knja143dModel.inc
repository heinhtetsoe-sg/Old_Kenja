<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knja143dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード
    public $selectStudent;
    public $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    public function knja143dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "TERM_SDATE"       =>  VARS::post("TERM_SDATE"),       //発行日
                             "LIMIT_DATE"       =>  VARS::post("LIMIT_DATE"),       //有効期限
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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
    public function clean()
    {
    }
}
