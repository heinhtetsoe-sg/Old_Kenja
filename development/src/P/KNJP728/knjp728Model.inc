<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp728Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjp728Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init() {
        $this->month = array(4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3);

        $this->cmd         = VARS::request("cmd");

        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "DIV"                  =>  VARS::post("DIV"),                  //出力指定
                             "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "PAID_LIMIT_MONTH"     =>  VARS::post("PAID_LIMIT_MONTH"),     //納期限
                             "LIMIT_DATE"           =>  VARS::post("LIMIT_DATE"),           //納入期限
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->year = ($this->field["DIV"] == "1") ? CTRL_YEAR + 1: CTRL_YEAR;
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //プロパティファイルの読み込み
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
