<?php

require_once('for_php7.php');

class knjd234gModel extends Model {
    var $field;
    var $file;
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolCode;

    function knjd234gModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "EDATE"                =>  VARS::post("EDATE"),                //出欠集計範囲・終了日付
                             "DATE_DIV"             =>  VARS::post("DATE_DIV"),             //出欠集計範囲（累計・学期）ラジオボタン
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "FUSHIN_SU"            =>  VARS::post("FUSHIN_SU"),            //不振科目数
                             "FUSHIN_SCORE"         =>  VARS::post("FUSHIN_SCORE"),         //不振科目得点
                             "FUSHIN_HEIKIN"        =>  VARS::post("FUSHIN_HEIKIN"),        //不振科目平均
                             "KESSEKI"              =>  VARS::post("KESSEKI"),              //出席状況　欠席
                             "CHIKOKU"              =>  VARS::post("CHIKOKU"),              //出席状況　遅刻
                             "SOUTAI"               =>  VARS::post("SOUTAI"),               //出席状況　早退
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        $this->file = VARS::file("FILE");
    }

}
?>
