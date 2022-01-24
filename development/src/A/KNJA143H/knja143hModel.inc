<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja143hModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;
    var $schoolName;

    /**
     * メンバ変数の初期化
     */
    function knja143hModel()
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
                             "TERM_EDATE"       =>  VARS::post("TERM_EDATE"),        //有効期限（教職員のみ）
                             "RISHUU"           =>  VARS::post("RISHUU"),            //履修登録者出力
                             "TAISHOUSHA"       =>  VARS::post("TAISHOUSHA"),        //1:生徒, 2:教職員
                             "PRINT_STAMP"      =>  VARS::post("PRINT_STAMP"),       //印影を出力する
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                     );

        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");
        $this->outDiv = VARS::post("OUTDIV"); //出力区分(1:A4用紙, 2:ラベル)

        //学校を判断
        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knja143hQuery::getSchoolName());
        Query::dbCheckIn($db);
        

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

