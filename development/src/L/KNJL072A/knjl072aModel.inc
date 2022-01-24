<?php

require_once('for_php7.php');

//メニュー画面
class knjl072aModel extends Model
{
    public $cmd;
    public $year;
    public $applicantdiv;
    public $testdiv;
    public $shdiv;
    public $wish_course;
    public $examType;

    public function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->shdiv        = VARS::request("SHDIV");
        $this->wish_course  = VARS::request("WISH_COURSE");
        $this->examType     = 1;

        $this->jogen_score = array();
        $this->kagen_score = array();
        if ($this->cmd == "sim" || $this->cmd == "decision") {
            foreach ($this->borderScoreList as $key => $row) {
                $this->jogen_score[$key] = VARS::post("JOGEN_SCORE"."-".$key);
                $this->kagen_score[$key] = VARS::post("KAGEN_SCORE"."-".$key);
            }
        }
    }
    //入力項目チェック
    public function validateInput()
    {
        if (!$this->applicantdiv) {
            $this->setWarning("MSG301", "（ 受験校種 ）");
            return false;
        }
        if (!$this->testdiv) {
            $this->setWarning("MSG301", "（ 試験区分 ）");
            return false;
        }
        if (!$this->shdiv) {
            $this->setWarning("MSG301", "（ 専併区分 ）");
            return false;
        }
        if (!$this->wish_course) {
            $this->setWarning("MSG301", "（ 受験コース ）");
            return false;
        }
        foreach ($this->borderScoreList as $key => $row) {
            if (!$this->jogen_score[$key] || !$this->kagen_score[$key]) {
                $this->setWarning("MSG301", "（ 上限点 ）（ 下限点 ）");
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
    // シミュレーションのモデルを返します
    public function &getSimModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->sim();
            $this->setMessage("", "確定ボタンを押下しないとデータは保存されません。");
        }
    }
    public function &sim()
    {
        //割り振り実行
        knjl072aQuery::sim($this);
    }
    // 確定のモデルを返します
    public function &getDecisionModel()
    {
        if ($this->validateInput()) {
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl072aQuery::selectQuerySuccessCnt($this, "ALL", "");
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["SUCCESS_CNT"] + $passingRow["SUCCESS_CNT_NO"] + $passingRow["KESSEKI_CNT"];
            Query::dbCheckIn($db);
            if (0 < $rtnCnt) {
                //確定
                $this->decision();
                $this->setMessage("MSG201");
            } else {
                $this->setWarning("MSG203", "（ 確定するデータが存在しません。シミュレーションを実行して下さい。）");
                return false;
            }
        }
    }
    public function &decision()
    {
        //割り振り実行
        knjl072aQuery::decision($this);
    }
    // 起動時に、仮判定データすべて削除（初期化）
    public function &getJudgeTmpClearModel()
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP ";
        $db->query($query);
        Query::dbCheckIn($db);
    }
}
