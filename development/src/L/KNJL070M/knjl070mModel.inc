<?php

require_once('for_php7.php');

//メニュー画面
class knjl070mModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $year;
    var $exam_type;
    var $applicantdiv;
        
    function knjl070mModel()
    {
    }
    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = "1";
        $this->exam_type    = "1";
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->course       = VARS::request("COURSE");
        if (VARS::get("SHDIV")){
            $this->shdiv        = VARS::get("SHDIV");
        }
        $this->field = array("BORDER_SCORE" =>  VARS::post("BORDER_SCORE"),
                             "BACK_RATE"    =>  100,
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
        if (!$this->course){
            $this->setWarning("MSG301","（ 学科・コース ）");
            return false;
        }
        if (!is_numeric($this->field["BORDER_SCORE_CANDI"])){
            $this->setWarning("MSG301","（ 候補点 ）");
            return false;
        }
        if ($this->field["BORDER_SCORE"] < $this->field["BORDER_SCORE_CANDI"]){
            $this->setWarning("MSG901","（ 候補点 ）\\n「合格点」以下を入力して下さい。");
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
//            $this->setMessage("MSG201","確定ボタンを押下しないとデータは保存されません。");
        }
    }
    function &sim()
    {
        //割り振り実行
        knjl070mQuery::sim($this);
    }    
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        if ($this->validateInput()){
            // シミュレーション
            $this->sim();
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl070mQuery::selectQueryJudge_tmp($this);
            $rtnCnt = $db->getOne($query);
            Query::dbCheckIn($db);
            if (0 < $rtnCnt){
                //合格者数などの取得
                $db = Query::dbCheckOut();
                $query  = knjl070mQuery::selectQuerySuccess_cnt($this);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $this->field["SUCCESS_CNT"] = is_numeric($row["SUCCESS_CNT"]) ? $row["SUCCESS_CNT"] : 0;
                $this->field["SUCCESS_CNT_CANDI"] = is_numeric($row["SUCCESS_CNT_CANDI"]) ? $row["SUCCESS_CNT_CANDI"] : 0;
                Query::dbCheckIn($db);
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
        knjl070mQuery::decision($this);
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
