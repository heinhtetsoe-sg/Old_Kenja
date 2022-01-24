<?php

require_once('for_php7.php');

//メニュー画面
class knjl620iModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $majorcd;
    public $testdiv;
    public $outputSort;
    public $field;

    public function init()
    {
        $this->examyear         = CTRL_YEAR + 1;
        $this->cmd              = VARS::request("cmd");
        $this->applicantdiv     = "2";
        $this->majorcd          = VARS::request("MAJORCD");
        $this->testdiv          = VARS::request("TESTDIV");
        $this->outputSort       = VARS::request("OUTPUT_SORT");
        $this->outputChkArray   = VARS::post("OUTPUT_CHK");
        $this->field = array();

        if (is_array($this->courseData)) {
            foreach ($this->courseData as $index => $course) {
                //チェックボックス
                $this->field["OUTPUT_CHK"][$course] = $this->outputChkArray[$index];
                //合格点
                $this->field["BORDER_SCORE"][$course] = VARS::post("BORDER_SCORE-".$course);
            }
        }
    }
    //入力項目チェック
    public function validateInput()
    {
        if (!$this->majorcd) {
            $this->setWarning("MSG301", "（ 学科 ）");
            return false;
        }
        if (!$this->testdiv) {
            $this->setWarning("MSG301", "（ 入試区分 ）");
            return false;
        }

        foreach ($this->field["BORDER_SCORE"] as $course => $score) {
            if ($score == "") {
                $this->setWarning("MSG301", "全てのコースに合格点を入力してください。");
                return false;
            }
        }

        return true;
    }
    /**
     * メイン表示用のモデルを返します
    */
    public function &getMainModel()
    {
    }
    // 仮判定のモデルを返します
    public function &getSimModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->sim();
        }
        $this->setMessage("MSG201");
    }
    public function &sim()
    {
        //割り振り実行
        knjl620iQuery::sim($this);
    }
    // 確定のモデルを返します
    public function &getDecisionModel()
    {
        if ($this->validateInput()) {
            $this->decision();
        }
        $this->setMessage("MSG201");
    }
    public function &decision()
    {
        //割り振り実行
        knjl620iQuery::decision($this);
    }
    // 仮判定・確定データすべて削除（初期化）
    public function &getClearModel()
    {
        if ($this->validateInput()) {
            $this->clear();
        }
        $this->setMessage("MSG201");
    }
    public function &clear()
    {
        //初期化
        knjl620iQuery::clear($this);
    }
}
