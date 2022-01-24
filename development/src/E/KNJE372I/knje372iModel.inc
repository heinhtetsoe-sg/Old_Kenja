<?php

require_once('for_php7.php');

class knje372iModel extends Model
{
    public $semester;
    public $hrClass;
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata;

    public function knje372iModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("DISP"             =>  VARS::post("DISP"),              //表示選択 1:クラス 2:個人
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),             //年
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),         //グループ区分
                             "ITEM_DIV"         =>  VARS::post("ITEM_DIV"),          //出力内容
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),        //テスト
                             "DBNAME"           =>  VARS::post("DBNAME"),             //DB変更に備えて
                             "OUTPUTDATE"       =>  VARS::post("OUTPUTDATE")         //出力日付
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

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

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
