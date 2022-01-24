<?php

require_once('for_php7.php');

//メニュー画面
class knjl070oModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $year;
    var $exam_type;
    var $applicantdiv;
        
    function knjl070oModel()
    {
    }
    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->exam_type    = VARS::request("EXAM_TYPE");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->course       = VARS::request("COURSE");
        $this->shdiv        = VARS::request("SHDIV");
        $this->field = array("BORDER_SCORE" =>  VARS::post("BORDER_SCORE"),
                             "BACK_RATE"    =>  VARS::post("BACK_RATE"),
                             "SUCCESS_CNT"  =>  VARS::post("SUCCESS_CNT"),
                             "CAPA_CNT"     =>  VARS::post("CAPA_CNT")
                            );
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->field["BORDER_SCORE"])){
            $this->setWarning("MSG301","（ 合格点 ）");
            return false;
        }
        if (!is_numeric($this->shdiv)){
            $this->setWarning("MSG301","（ ライン種別 ）");
            return false;
        }
        if (!is_numeric($this->field["BACK_RATE"])){
            $this->setWarning("MSG301","（ 戻り率 ）");
            return false;
        }
        if (!$this->course){
            $this->setWarning("MSG301","（ 学科・コース ）");
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
        if ($this->validateInput()){
            $this->sim();
            $this->setMessage("MSG201","確定ボタンを押下しないとデータは保存されません。");
        }
    }
    function &sim()
    {
        //割り振り実行
        knjl070oQuery::sim($this);
    }
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        $db           = Query::dbCheckOut();
        //仮判定データ
        $query = knjl070oQuery::selectQueryJudge_tmp($this);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if (!is_array($Row)){
            $this->setWarning("MSG203","（ 確定するデータが存在しません。シミュレーションを実行して下さい。）");
            return false;
        }
        $this->decision();
        $this->setMessage("MSG201");
    }
    function &decision()
    {
        //割り振り実行
        knjl070oQuery::decision($this);
    }
}
?>
