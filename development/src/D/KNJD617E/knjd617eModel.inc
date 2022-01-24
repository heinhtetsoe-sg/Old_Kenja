<?php

require_once('for_php7.php');

class knjd617eModel extends Model
{
    public $semester;
    public $hrClass;
    public $selectdata;
    public $selectdata2;
    public $cmd;
    public $control;        //コントロールマスタ
    public $tutisyoTokubetuKatudo;

    public function knjd617eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
    }

    public function init()
    {
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
                             "DATE_DIV"                    =>  VARS::post("DATE_DIV"),                     //出欠集計範囲（累計・学期）ラジオボタン
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }
        $this->selectdata = array();
        $this->selectdata = explode(",", VARS::post("selectdata"));
        $this->selectdata2 = array();
        $this->selectdata2 = explode(",", VARS::post("selectdata2"));

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
