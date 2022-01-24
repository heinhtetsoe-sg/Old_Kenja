<?php

require_once('for_php7.php');

class knje080Model extends Model {
    var $field       = array();
    var $select_data = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knje080Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SEKI"             =>  VARS::post("SEKI"),             //記載責任者
                             "FORM6"            =>  VARS::post("FORM6"),            //６年用フォーム選択
                             "DBNAME"           =>  VARS::post("DBNAME"),           //DB変更に備えて
                             "LANGUAGE"         =>  VARS::post("LANGUAGE"),         //学業成績証明書書類
                             "KOTYO"            =>  VARS::post("KOTYO"),            //校長名印刷
                            );


        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->Knje080UseAForm = ($this->Properties["Knje080UseAForm"] == '1') ? $this->Properties["Knje080UseAForm"] : 0;
    }
}
?>
