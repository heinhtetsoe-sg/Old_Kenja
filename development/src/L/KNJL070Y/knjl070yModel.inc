<?php

require_once('for_php7.php');

//メニュー画面
class knjl070yModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;

    function knjl070yModel()
    {
    }
    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->course       = VARS::request("COURSE");
        $this->exam_type    = "1";
        $this->shdiv        = VARS::request("SHDIV");
        $this->kikoku       = VARS::request("KIKOKU");
        $this->field = array("BORDER_SCORE" =>  VARS::post("BORDER_SCORE"),
                             "BACK_RATE"    =>  VARS::post("BACK_RATE"),
                             "SUCCESS_CNT"  =>  VARS::post("SUCCESS_CNT"),
                             "CAPA_CNT"     =>  VARS::post("CAPA_CNT"),
                             "BORDER_SCORE_CANDI" =>  VARS::post("BORDER_SCORE_CANDI"),
                             "SUCCESS_CNT_CANDI"  =>  VARS::post("SUCCESS_CNT_CANDI"),
                             "BORDER_DEVIATION"     =>  VARS::post("BORDER_DEVIATION"),
                             "SUCCESS_CNT_SPECIAL"  =>  VARS::post("SUCCESS_CNT_SPECIAL"),
                             "SUCCESS_CNT_SPECIAL2" =>  VARS::post("SUCCESS_CNT_SPECIAL2"),
                             "SUCCESS_CNT_CHALLENGE" =>  VARS::post("SUCCESS_CNT_CHALLENGE")
                            );
    }
    //入力項目チェック
    function validateInput()
    {
        if (!$this->applicantdiv){
            $this->setWarning("MSG301","（ 入試制度 ）");
            return false;
        }
        if (!$this->testdiv){
            $this->setWarning("MSG301","（ 入試区分 ）");
            return false;
        }
        if (!$this->course){
            $this->setWarning("MSG301","（ 学科・コース ）");
            return false;
        }
        if (!$this->shdiv){
            $this->setWarning("MSG301","（ 専併区分 ）");
            return false;
        }
        if (!$this->kikoku){
            $this->setWarning("MSG301","（ 対象者 ）");
            return false;
        }
        if (!is_numeric($this->field["BORDER_SCORE"])){
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
        if ($this->validateInput()){
            $this->sim();
            $this->setMessage("MSG201","確定ボタンを押下しないとデータは保存されません。");
        }
    }
    function &sim()
    {
        //割り振り実行
        knjl070yQuery::sim($this);
    }    
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        if ($this->validateInput()){
            // シミュレーション
//            $this->sim();
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl070yQuery::selectQueryJudge_tmp($this);
            $rtnCnt = $db->getOne($query);
            Query::dbCheckIn($db);
            if (0 < $rtnCnt){
                //合格者数などの取得
                $db = Query::dbCheckOut();
                $query  = knjl070yQuery::selectQuerySuccess_cnt($this);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $this->field["SUCCESS_CNT"] = is_numeric($row["SUCCESS_CNT"]) ? $row["SUCCESS_CNT"] : 0;
                $this->field["SUCCESS_CNT_CANDI"] = is_numeric($row["SUCCESS_CNT_CANDI"]) ? $row["SUCCESS_CNT_CANDI"] : 0;
                $this->field["SUCCESS_CNT_SPECIAL"] = is_numeric($row["SUCCESS_CNT_SPECIAL"]) ? $row["SUCCESS_CNT_SPECIAL"] : 0;
                $this->field["SUCCESS_CNT_SPECIAL2"] = is_numeric($row["SUCCESS_CNT_SPECIAL2"]) ? $row["SUCCESS_CNT_SPECIAL2"] : 0;
                $this->field["SUCCESS_CNT_CHALLENGE"] = is_numeric($row["SUCCESS_CNT_CHALLENGE"]) ? $row["SUCCESS_CNT_CHALLENGE"] : 0;
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
        knjl070yQuery::decision($this);
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
