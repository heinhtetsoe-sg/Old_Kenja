<?php

require_once('for_php7.php');

//メニュー画面
class knjl451hModel extends Model
{
    public $cmd;                   //一覧表示時のcmd
    public $year;
    public $applicantdiv;
    public $testdiv;

    public function init()
    {
        $this->year                 = CTRL_YEAR + 1;
        $this->cmd                  = VARS::request("cmd");
        $this->applicantdiv         = VARS::request("APPLICANTDIV");
        $this->testdiv              = VARS::request("TESTDIV");
        $this->coursecd             = VARS::request("EXAMCOURSECD");
        $this->examtype             = VARS::request("EXAM_TYPE");
        $this->shdiv                = VARS::request("SHDIV");
        if ($this->cmd == 'sim' || $this->cmd == 'decision') {
            $this->field["BORDER_SCORE"]        = VARS::post("BORDER_SCORE");
            $this->field["GOUKAKU_CNT"]         = VARS::post("GOUKAKU_CNT");
            $this->field["NAIDAKU_CNT"]         = VARS::post("NAIDAKU_CNT");
        } else {
            $this->field = array();
        }
    }
    //入力項目チェック
    public function validateInput()
    {
        if (!$this->applicantdiv) {
            $this->setWarning("MSG301", "（ 入試制度 ）");
            return false;
        }
        if (!$this->testdiv) {
            $this->setWarning("MSG301", "（ 試験回数 ）");
            return false;
        }
        if (!$this->coursecd) {
            $this->setWarning("MSG301", "（ 志望コース ）");
            return false;
        }
        if (!$this->examtype) {
            $this->setWarning("MSG301", "（ 受験型 ）");
            return false;
        }
        if (!$this->shdiv) {
            $this->setWarning("MSG301", "（ 出願区分 ）");
            return false;
        }
        if (!$this->field["BORDER_SCORE"]) {
            $this->setWarning("MSG301", "（ 合格点 ）");
            return false;
        }

        return true;
    }
    /**
     * メイン表示用のモデルを返します
    */
    public function &getMainModel()
    {
    }
    // シミュレーションのモデルを返します
    public function &getSimModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->sim();
            $this->setMessage("MSG201", "確定ボタンを押下しないとデータは保存されません。");
        }
    }
    public function &sim()
    {
        //割り振り実行
        knjl451hQuery::sim($this);
    }
    // 確定のモデルを返します
    public function &getDecisionModel()
    {
        if ($this->validateInput()) {
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl451hQuery::getCntJudge($this);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["GOUKAKU_CNT"] + $passingRow["FUGOUKAKU_CNT"];

            if ($rtnCnt < 1) {
                $this->setWarning("MSG203", "（ 確定するデータが存在しません。シミュレーションを実行して下さい。）");
                return false;
            }
            
            Query::dbCheckIn($db);

            //確定
            $this->decision();
            $this->setMessage("MSG201");
        }
    }
    public function &decision()
    {
        //割り振り実行
        knjl451hQuery::decision($this);
    }

    // 起動時に、仮判定データすべて削除（初期化）
    public function &getJudgeTmpClearModel()
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP TMP";
        $db->query($query);
        Query::dbCheckIn($db);
    }
}
