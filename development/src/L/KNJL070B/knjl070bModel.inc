<?php

require_once('for_php7.php');

//メニュー画面
class knjl070bModel extends Model
{
    var $cmd;                   //一覧表示時のcmd
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $judgment_div; //判定区分
    var $shdiv;     //事前相談内諾区分

    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->shdiv        = VARS::request("SHDIV");
        $this->judgment_div = VARS::request("JUDGMENT_DIV");

        if ($this->cmd == 'sim' || $this->cmd == 'decision') {
            $this->field["BORDER_DEVIATION"] = VARS::post("BORDER_DEVIATION");
            $this->field["SUCCESS_CNT"] = VARS::post("SUCCESS_CNT");
            $this->field["SUCCESS_CNT_SPECIAL"] = VARS::post("SUCCESS_CNT_SPECIAL");
            $this->field["SUCCESS_CNT_SPECIAL2"] = VARS::post("SUCCESS_CNT_SPECIAL2");
        } else {
            $this->field = array();
        }
    }
    //入力項目チェック
    function validateInput()
    {
        if (!$this->applicantdiv) {
            $this->setWarning("MSG301","（ 入試制度 ）");
            return false;
        }
        if (!$this->testdiv) {
            $this->setWarning("MSG301","（ 入試区分 ）");
            return false;
        }
        if (!$this->shdiv) {
            $this->setWarning("MSG301","（ 処理区分 ）");
            return false;
        }
        if (!$this->judgment_div) {
            $this->setWarning("MSG301","（ 判定区分 ）");
            return false;
        }
        if (!strlen($this->field["BORDER_DEVIATION"])) {
            $this->setWarning("MSG301","（ 合格点 ）");
            return false;
        }
        return true;
    }
    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
    }
    // シミュレーションのモデルを返します
    function &getSimModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->sim();
            $this->setMessage("MSG201","確定ボタンを押下しないとデータは保存されません。");
        }
    }
    function &sim()
    {
        //割り振り実行
        knjl070bQuery::sim($this);
    }    
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        if ($this->validateInput()){
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl070bQuery::selectQuerySuccess_cnt($this);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["SUCCESS_CNT"] + $passingRow["SUCCESS_CNT_NO"] + $passingRow["SUCCESS_CNT_SPECIAL"] + $passingRow["SUCCESS_CNT_SPECIAL2"];
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
    function &decision()
    {
        //割り振り実行
        knjl070bQuery::decision($this);
    }
    // 起動時に、仮判定データすべて削除（初期化）
    function &getJudgeTmpClearModel()
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM entexam_judge_tmp ";
        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>
