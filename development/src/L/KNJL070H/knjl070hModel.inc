<?php

require_once('for_php7.php');

//メニュー画面
class knjl070hModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $year;
    var $exam_type;
    var $applicantdiv;
    var $sex;
        
    function knjl070hModel()
    {
    }
    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->sex          = VARS::request("SEX");
        $this->exam_type    = VARS::request("EXAM_TYPE");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->course       = VARS::request("COURSE");
        if (VARS::get("SHDIV")){
            $this->shdiv        = VARS::get("SHDIV");
        }
        $this->field = array("BORDER_SCORE" =>  VARS::post("BORDER_SCORE"),
                             "BACK_RATE"    =>  VARS::post("BACK_RATE"),
                             "SUCCESS_CNT"  =>  VARS::post("SUCCESS_CNT"),
                             "CAPA_CNT"     =>  VARS::post("CAPA_CNT"),
                             "BORDER_SCORE_CANDI" =>  VARS::post("BORDER_SCORE_CANDI"),
                             "SUCCESS_CNT_CANDI"  =>  VARS::post("SUCCESS_CNT_CANDI")
                            );
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->field["BORDER_SCORE"])){
            $this->setWarning("MSG301","（ 合格点 ）");
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
//        if ($this->cmd == "sim" && $this->exam_type == 2){
//            $tmp = $this->exam_type;
//            $this->exam_type = 1;
//            $db           = Query::dbCheckOut();
//            //合格点マスタ
//            $query = knjl070hQuery::selectQueryPassingmark($this);
//            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
//            Query::dbCheckIn($db);
//            $this->exam_type = $tmp;
//            if (!is_array($Row)){
//                $this->setWarning("MSG300","（ 判定対象：２科目型が実行されていません。）");
//                return false;
//            }
//        }
        if (($this->applicantdiv == '1' || $this->applicantdiv == '2') && !is_numeric($this->field["BORDER_SCORE_CANDI"])){
            $this->setWarning("MSG301","（ 繰上合格候補点 ）");
            return false;
        }
        if (($this->applicantdiv == '1' || $this->applicantdiv == '2') && $this->field["BORDER_SCORE"] < $this->field["BORDER_SCORE_CANDI"]){
            $this->setWarning("MSG901","（ 繰上合格候補点 ）\\n「合格点」以下を入力して下さい。");
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
        knjl070hQuery::sim($this);
    }    
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        $db           = Query::dbCheckOut();
        //仮判定データ
        $query = knjl070hQuery::selectQueryJudge_tmp($this);
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
        knjl070hQuery::decision($this);
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
