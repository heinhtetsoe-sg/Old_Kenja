<?php

require_once('for_php7.php');

class knjg102aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knjg102aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("HR_CLASS_TYPE"                =>  VARS::post("HR_CLASS_TYPE"),                //クラス方式
                            "GRADE"                         =>  VARS::post("GRADE"),                        //学年
                            "DATE"                          =>  VARS::post("DATE"),                         //異動対象日付
                            "KISAI_DATE"                    =>  VARS::post("KISAI_DATE"),                   //記載日付
                            "KAIKINSYA"                     =>  VARS::post("KAIKINSYA"),                    //皆勤者ラジオ
                            "KAIKIN_KESSEKI"                =>  VARS::post("KAIKIN_KESSEKI"),               //皆勤者欠席
                            "SEIKIN_KESSEKI"                =>  VARS::post("SEIKIN_KESSEKI"),               //精勤者欠席
                            "DBNAME"                        =>  VARS::post("DBNAME")                        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>