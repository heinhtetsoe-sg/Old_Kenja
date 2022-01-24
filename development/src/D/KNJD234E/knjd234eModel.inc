<?php

require_once('for_php7.php');

class knjd234eModel extends Model {
    var $field;
    var $semester;
    var $dispSemester;
    var $dispDataDiv;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd234eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        // $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");
        $this->dispSemester = VARS::post("DISP_SEMESTER");
        $this->dispDataDiv = VARS::post("DISP_DATE_DIV");

        if ($this->cmd == '') {
            unset($this->semester);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "EDATE"                =>  VARS::post("EDATE"),                //出欠集計範囲・終了日付
                             "DATE_DIV"             =>  VARS::post("DATE_DIV"),             //出欠集計範囲（累計・学期）ラジオボタン
//                             "KESSEKI"              =>  VARS::post("KESSEKI"),              //出席状況　欠席
//                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
//                             "YURYO"                =>  VARS::post("YURYO"),                //優良者
                             "FUSHIN"               =>  VARS::post("FUSHIN"),               //不振者
//                             "HYOKA_HEIKIN"         =>  VARS::post("HYOKA_HEIKIN"),         //評価平均
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

}
?>
