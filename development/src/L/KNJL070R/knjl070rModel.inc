<?php

require_once('for_php7.php');

//メニュー画面
class knjl070rModel extends Model
{
    var $cmd;                   //一覧表示時のcmd
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $desirediv; //受験区分
    var $shdiv;     //事前相談内諾区分

    function init()
    {
        $this->year         = CTRL_YEAR + 1;
        $this->cmd          = VARS::request("cmd");
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->testdiv      = VARS::request("TESTDIV");
        $this->desirediv    = VARS::request("DESIREDIV");
        $this->shdiv        = VARS::request("SHDIV");

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        if ($this->cmd == 'sim' || $this->cmd == 'decision') {
            foreach ($this->desiredivArray[$this->desirediv] as $wishno => $examcourseArray) {
                $course = $examcourseArray["EXAMCOURSE"];
                $this->field["BORDER_SCORE-".$course] = VARS::post("BORDER_SCORE-".$course);
                $this->field["SUCCESS_CNT-" .$course] = VARS::post("SUCCESS_CNT-" .$course);
                $this->field["SUCCESS_CNT_SPECIAL-" .$course] = VARS::post("SUCCESS_CNT_SPECIAL-" .$course);
                $this->field["SUCCESS_CNT_SPECIAL2-" .$course] = VARS::post("SUCCESS_CNT_SPECIAL2-" .$course);

                if ($this->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                    $course = $this->hoketuCourse;
                    $this->field["BORDER_SCORE-".$course] = VARS::post("BORDER_SCORE-".$course);
                    $this->field["SUCCESS_CNT-" .$course] = VARS::post("SUCCESS_CNT-" .$course);
                    $this->field["SUCCESS_CNT_SPECIAL-" .$course] = VARS::post("SUCCESS_CNT_SPECIAL-" .$course);
                    $this->field["SUCCESS_CNT_SPECIAL2-" .$course] = VARS::post("SUCCESS_CNT_SPECIAL2-" .$course);
                }
            }
        } else {
            $this->field = array();
        }
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
        if (!$this->shdiv){
            $this->setWarning("MSG301","（ 事前相談内諾区分 ）");
            return false;
        }
        if (!$this->desirediv){
            $this->setWarning("MSG301","（ 受験区分 ）");
            return false;
        }
        foreach ($this->desiredivArray[$this->desirediv] as $wishno => $examcourseArray) {
            $course = $examcourseArray["EXAMCOURSE"];
            if (!is_numeric($this->field["BORDER_SCORE-".$course])){
                $this->setWarning("MSG301","（ 合格点 ）");
                return false;
            }
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
        knjl070rQuery::sim($this);
    }    
    // 確定のモデルを返します
    function &getDecisionModel()
    {
        if ($this->validateInput()){
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl070rQuery::selectQuerySuccess_cnt($this);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["SUCCESS_CNT"] + $passingRow["SUCCESS_CNT_NO"];
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
        knjl070rQuery::decision($this);
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
