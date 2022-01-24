<?php

require_once('for_php7.php');

//メニュー画面
class knjl070pModel extends Model
{
    var $cmd;                   //一覧表示時のcmd
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $desirediv; //受験区分
    var $shdiv;     //事前相談内諾区分
    var $mensetsudiv; //面接点 1:面接なし 2:面接あり

    function init() {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->desirediv    = VARS::request("DESIREDIV");
        $this->shdiv        = VARS::request("SHDIV");
        $this->mensetsudiv  = VARS::request("MENSETSUDIV");

        if ($this->cmd == 'sim' || $this->cmd == 'decision') {
            $this->field["BORDER_SCORE"] = VARS::post("BORDER_SCORE");
            $this->field["BORDER_DEVIATION"] = VARS::post("BORDER_DEVIATION");
            for ($s = 0; $s <= 2; $s++) {
                $sex = ($s == 0) ? 9 : $s;
                $this->field["SHUTUGAN_CNT" .$sex] = VARS::post("SHUTUGAN_CNT".$sex);
                $this->field["KESSEKI_CNT"  .$sex] = VARS::post("KESSEKI_CNT".$sex);
                $this->field["GOUKAKU_CNT"  .$sex] = VARS::post("GOUKAKU_CNT".$sex);
                $this->field["FUGOUKAKU_CNT".$sex] = VARS::post("FUGOUKAKU_CNT".$sex);
                $this->field["JITUGOUKAKU_RITU".$sex] = VARS::post("JITUGOUKAKU_RITU".$sex);
            }
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
        if (!$this->mensetsudiv) {
            $this->setWarning("MSG301","（ 面接点 ）");
            return false;
        }
        if (!$this->field["BORDER_SCORE"] && !$this->field["BORDER_DEVIATION"]) {
            $this->setWarning("MSG301","（ 合格点 ）");
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
        knjl070pQuery::sim($this);
    }
    // 確定のモデルを返します
    function &getDecisionModel() {
        if ($this->validateInput()){
            //仮判定データ
            $db = Query::dbCheckOut();
            //$query  = knjl070pQuery::selectQuerySuccess_cnt($this);
            $query  = knjl070pQuery::getCntPassingmark($this);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["GOUKAKU_CNT9"] + $passingRow["FUGOUKAKU_CNT9"];
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
        knjl070pQuery::decision($this);
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
