<?php

require_once('for_php7.php');

class knjd156eModel extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $tutisyoTokubetuKatudo;

    function knjd156eModel() {
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

        $this->field = array("SEMESTER"                    =>  VARS::post("SEMESTER"),                     //学期
                             "OUTPUT_DIV"                  =>  VARS::post("OUTPUT_DIV"),                   //帳票選択
                             "AVG_DIV"                     =>  VARS::post("AVG_DIV"),                      //順位出力
                             "HANREI_SYUTURYOKU_NASI"      =>  VARS::post("HANREI_SYUTURYOKU_NASI")        //凡例出力なし チェックボックス
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
