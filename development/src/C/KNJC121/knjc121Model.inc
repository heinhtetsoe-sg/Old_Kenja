<?php

require_once('for_php7.php');

class knjc121Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $testTable;

    public function knjc121Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HRCLASS"    =>  VARS::post("GRADE_HRCLASS"),    //学年
                             "PRINT_MINYURYOKU" =>  VARS::post("PRINT_MINYURYOKU"), //未入力数を出力する
                             "PRINT_SAKIKAMOKU" =>  VARS::post("PRINT_SAKIKAMOKU"), //合併先科目を出力する
                             "SDATE"            =>  VARS::post("SDATE"),            //対象日(開始)
                             "EDATE"            =>  VARS::post("EDATE"),            //対象日(終了)
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "TYUI_TYOUKA"      =>  VARS::post("TYUI_TYOUKA"),      //注意・超過ラジオボタン
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        if (strlen($this->Properties["useTestCountflg"])) {
            $this->testTable = $this->Properties["useTestCountflg"];
        } else {
            $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
        }
    }

    public function clean()
    {
    }
}
