<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja142bModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード
    public $selectStudent;
    public $selectStudentLabel;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knja142bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"           => VARS::post("YEAR"),          //年度
                             "GRADE_HR_CLASS" => VARS::post("GRADE_HR_CLASS"),//年組
                             "GAKKI"          => VARS::post("GAKKI"),         //学期
                             "TERM_SDATE"     => VARS::post("TERM_SDATE"),    //発行日
                             "TERM_EDATE"     => VARS::post("TERM_EDATE"),    //有効期限
                             "DBNAME"         => VARS::post("DBNAME")         //DB変更に備えて
        );

        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");
        $this->outDiv = VARS::post("OUTDIV"); //出力区分(1:A4用紙, 2:ラベル)

        //学校を判断
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knja142bQuery::getSchoolName());
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->field["RISHUU"] = '';
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
