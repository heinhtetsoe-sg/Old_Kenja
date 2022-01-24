<?php

require_once('for_php7.php');

class knjp907Model extends Model {
    var $semester;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjp907Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHOOLKIND"       =>  VARS::post("SCHOOLKIND"),           //校種
                             "TENTAI_DATE"      =>  VARS::post("TENTAI_DATE"),          //転退学日コンボ
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),       //対象クラス
                             "HENKIN_KAKUTEI"   =>  VARS::post("HENKIN_KAKUTEI"),       //返金確定日
                             "OUTPUT_DIV"       =>  VARS::post("OUTPUT_DIV"),           //出力対象
                             "ADDRESS"          =>  VARS::post("ADDRESS"),              //住所
                             "ADDR_DIV"         =>  VARS::post("ADDR_DIV")              //ラジオ（1:生徒 2:保護者）
                             );

        $this->cmd = VARS::request("cmd");
        $this->updSelected = VARS::post("updSelected");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //返金実行
    function updateExe() {
        $updSchregArray = explode(',', $this->updSelected);
        foreach ($updSchregArray as $key => $schregNo) {
            knjp907Query::updLevyOutgo($this, $schregNo);
        }
        $this->setMessage("MSG201");
    }

    //返金キャンセル
    function cancelExe() {
        $updSchregArray = explode(',', $this->updSelected);

        foreach ($updSchregArray as $key => $schregNo) {
            knjp907Query::cancelLevyOutgo($this, $schregNo);
        }
        $this->setMessage("MSG201");
    }
}
?>
