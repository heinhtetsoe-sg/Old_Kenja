<?php

require_once('for_php7.php');

//メニュー画面
class knjl070jModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $year;

    function knjl070jModel()
    {
    }
    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->course       = VARS::request("COURSE");
        if (VARS::get("SHDIV")){
            $this->shdiv        = VARS::get("SHDIV");
        }
        $this->field = array("BORDER_DEVIATION" =>  VARS::post("BORDER_DEVIATION"),
                             "BACK_RATE"        =>  VARS::post("BACK_RATE"),
                             "SUCCESS_CNT"      =>  VARS::post("SUCCESS_CNT"),
                             "CAPA_CNT"         =>  VARS::post("CAPA_CNT")
                            );
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->field["BORDER_DEVIATION"])){
            $this->setWarning("MSG301","（ 合格点（判定偏差値） ）");
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
        knjl070jQuery::sim($this);
    }    
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        $db           = Query::dbCheckOut();
        //仮判定データ
        $query = knjl070jQuery::selectQueryJudge_tmp($this);
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
        knjl070jQuery::decision($this);
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
