<?php

require_once('for_php7.php');

//メニュー画面
class knjl070uModel extends Model
{
    var $cmd;                   //一覧表示時のcmd
    var $year;
    var $applicantdiv;
    var $testdiv;

    function init() {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->testdiv      = VARS::request("TESTDIV");

        if ($this->cmd == 'sim' || $this->cmd == 'decision') {
            $this->field["BORDER_SCORE"] = VARS::post("BORDER_SCORE");
            $this->field["GOUKAKU_CNT"] = VARS::post("GOUKAKU_CNT");
            $this->field["MODORI_RITU"] = VARS::post("MODORI_RITU");
            $this->field["SHUUYOU_CNT"] = VARS::post("SHUUYOU_CNT");
        } else {
            $this->field = array();
        }
    }
    //入力項目チェック
    function validateInput() {
        if (!$this->applicantdiv) {
            $this->setWarning("MSG301","（ 入試制度 ）");
            return false;
        }
        if (!$this->testdiv) {
            $this->setWarning("MSG301","（ 入試区分 ）");
            return false;
        }
        if (!$this->field["BORDER_SCORE"]) {
            $this->setWarning("MSG301","（ 合格点 ）");
            return false;
        }
        if (!$this->field["MODORI_RITU"]) {
            $this->setWarning("MSG301","（ 戻り率 ）");
            return false;
        }
        return true;
    }
    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel() {
    }
    // シミュレーションのモデルを返します
    function &getSimModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->sim();
            $this->setMessage("MSG201","確定ボタンを押下しないとデータは保存されません。");
        }
    }
    function &sim() {
        //割り振り実行
        knjl070uQuery::sim($this);
    }
    // 確定のモデルを返します
    function &getDecisionModel() {
        if ($this->validateInput()){
            //仮判定データ
            $db = Query::dbCheckOut();
            //$query  = knjl070uQuery::selectQuerySuccess_cnt($this);
            $query  = knjl070uQuery::getCntPassingmark($this);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["GOUKAKU_CNT"] + $passingRow["FUGOUKAKU_CNT"];
            Query::dbCheckIn($db);
            if (0 < $rtnCnt){
                //確定
                $this->decision();
                $this->setMessage("MSG201");
            } else {
                $this->setWarning("MSG203","（ 確定するデータが存在しません。シミュレーションを実行して下さい。）");
                return false;
            }
        }
    }
    function &decision() {
        //割り振り実行
        knjl070uQuery::decision($this);
    }
    // 起動時に、仮判定データすべて削除（初期化）
    function &getJudgeTmpClearModel() {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM entexam_judge_tmp ";
        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
