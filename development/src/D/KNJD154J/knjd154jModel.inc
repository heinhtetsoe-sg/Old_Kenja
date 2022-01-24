<?php

require_once('for_php7.php');

class knjd154jModel extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $tutisyoTokubetuKatudo;

    function knjd154jModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),               //学期
                             "AVG_PRINT"              =>  VARS::post("AVG_PRINT"),              //平均値出力選択チェックボックス
                             "AVG_DIV"                =>  VARS::post("AVG_DIV"),                //平均値ラジオボタン
                             "DATE_DIV"               =>  VARS::post("DATE_DIV"),               //出欠集計範囲（累計・学期）ラジオボタン
                             "DATE"                   =>  VARS::post("DATE"),                   //出欠集計範囲（終了日付）
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
