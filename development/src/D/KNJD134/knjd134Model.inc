<?php

require_once('for_php7.php');

class knjd134Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd134Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }
    function init() {
        $this->field = array("YEAR"                     =>  VARS::post("YEAR"),                     //年度
                             "SEMESTER"                 =>  VARS::post("SEMESTER"),                 //学期
                             "GRADE"                    =>  VARS::post("GRADE"),                    //学年
                             "FORM_SELECT"              =>  VARS::post("FORM_SELECT"),              //フォーム選択
                             "TYUI_TYOUKA"              =>  VARS::post("TYUI_TYOUKA"),              //注意・超過ラジオボタン
                             "DATE_DIV"                 =>  VARS::post("DATE_DIV"),                 //出欠集計範囲（累計・学期）ラジオボタン
                             "SDATE"                    =>  VARS::post("SDATE"),                    //出欠集計開始日付
                             "EDATE"                    =>  VARS::post("EDATE"),                    //出欠集計終了日付
                             "DBNAME"                   =>  VARS::post("DBNAME")                    //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
