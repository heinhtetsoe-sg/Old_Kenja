<?php

require_once('for_php7.php');

class knjh441cModel extends Model {
    var $field;
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolCode;

    function knjh441cModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->mock_cd  = VARS::post("MOCKCD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd_before);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "MOCKCD"               =>  VARS::post("MOCKCD"),               //模試コードコンボ
                             "YURYO"                =>  VARS::post("YURYO"),                //優良者
                             "FUSHIN"               =>  VARS::post("FUSHIN"),               //不振者
                             "OUTPUT_DOSUBUPU"      =>  VARS::post("OUTPUT_DOSUBUPU"),      //度数分布を出力
                             "OUTPUT_YURYO"         =>  VARS::post("OUTPUT_YURYO"),         //優良者を出力
                             "OUTPUT_FUSHIN"        =>  VARS::post("OUTPUT_FUSHIN"),        //不振者を出力
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
