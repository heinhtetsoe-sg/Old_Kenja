<?php

require_once('for_php7.php');

//メニュー画面
class knjl100kModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $year;
    var $msg;

    function knjl100kModel()
    {
        $this->judge_col    = 1;
        $this->year         = CTRL_YEAR + 1;
    }
    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->course       = VARS::request("COURSE");
        $this->shdiv        = VARS::request("SHDIV");
        if (VARS::request("JUDGE_COL")){
            $this->judge_col    = VARS::request("JUDGE_COL");
        }
        $this->field = array("A_BORDER_SCORE" =>  VARS::post("A_BORDER_SCORE"),
                             "B_BORDER_SCORE" =>  VARS::post("B_BORDER_SCORE"),
                             "BORDER_SCORE"   =>  VARS::post("BORDER_SCORE"),
                             "BACK_RATE"      =>  VARS::post("BACK_RATE"),
                             "CAPA_CNT"       =>  VARS::post("CAPA_CNT")            # 2005/11/10 arakaki
                            );
    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->field["BORDER_SCORE"])){
            $this->setWarning("MSG301","（ 基本 ）");
            return false;
        }
        if (!is_numeric($this->field["A_BORDER_SCORE"])){
            $this->setWarning("MSG301","（ Ａ判定 ）");
            return false;
        }
        if (!is_numeric($this->field["B_BORDER_SCORE"])){
            $this->setWarning("MSG301","（ Ｂ判定 ）");
            return false;
        }
        if (!is_numeric($this->field["BACK_RATE"])){
            $this->setWarning("MSG301","（ 戻り率 ）");
            return false;
        }
        if (!$this->course){
            $this->setWarning("MSG301","課程学科、コースコード、コース名は必須項目です。");
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

            //上位志望区分確定データ確認 2005/08/31
            $db = Query::dbCheckOut();
            $query = knjl100kQuery::selectQueryRankhFixChk($this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (is_array($Row)){
                $this->setWarning("MSG300","（ 上位コースが処理されていません。スライド合格の判定に不整合が生じます！）");
                return false;
            }
            //シミュレート実行
            knjl100kQuery::sim($this);
            $this->setMessage("MSG201");

            //仮判定データ
            $db           = Query::dbCheckOut();
            $query = knjl100kQuery::selectQueryJudge_tmp($this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (!is_array($Row)){
                $this->setWarning("MSG202","\\n対象者が存在しない為、データは作成されませんでした。\\n");
            }else{
                $this->setMessage("MSG201","\\n確定ボタンを押下しないとデータは保存されません。\\n");
            }
        }
    }


    // 確定のモデルを返します
    function &getDecisionModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            //仮判定データ
            $query = knjl100kQuery::selectQueryJudge_tmp($this);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            if (!is_array($Row)){
                $this->setWarning("MSG203","（ 確定するデータが存在しません。シミュレーションを実行して下さい。）");
                return false;
            }
            $this->decision();
            $this->setMessage("MSG201");
        }
    }
    function &decision()
    {
        //確定実行
        knjl100kQuery::decision($this);
        $this->setMessage("MSG201");
    }    
    //通知モデルを返します
    function &getNoticeModel()
    {
        //通知実行
        knjl100kQuery::notice($this);
        $this->setMessage("MSG201");
    }
    
    //終了時に再処理フラグチェック
    function getCloseModel()
    {
        $db = Query::dbCheckOut();
        $result = $db->query(knjl100kQuery::CheckReprocess_flg($this->year));
        $this->msg = "";
        if ($result) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) 
            {
                $this->msg .= $row["TESTDIV"]."　".$row["SHDIV"]."　".$row["EXAMCOURSE_NAME"]."\n";
            }
        }
        Query::dbCheckIn($db);
    }
}
?>
