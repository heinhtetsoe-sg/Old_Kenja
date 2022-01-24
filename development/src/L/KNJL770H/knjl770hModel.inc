<?php

require_once('for_php7.php');

//メニュー画面
class knjl770hModel extends Model
{
    public $cmd;                   //一覧表示時のcmd
    public $year;
    public $applicantdiv;
    public $testdiv;

    public function init()
    {
        $this->year                 = CTRL_YEAR + 1;
        $this->cmd                  = VARS::request("cmd");
        $this->applicantdiv         = VARS::request("APPLICANTDIV");
        $this->testdiv              = VARS::request("TESTDIV");
        $this->sexopt               = VARS::request("SEX_OPT");
        $this->sex                  = ($this->sexopt == "1") ? "9" : (($this->sexopt == "2") ? "1" : "2"); //1:男, 2:女, 9:共通
        if ($this->cmd == 'sim' || $this->cmd == 'decision') {
            $this->field["BORDER_SCORE"]        = VARS::post("BORDER_SCORE");
            $this->field["GOUKAKU_CNT"]         = VARS::post("GOUKAKU_CNT");
            $this->field["BORDER_SCORE_CANDI"]  = VARS::post("BORDER_SCORE_CANDI");
            $this->field["SUCCESS_CNT_CANDI"]   = VARS::post("SUCCESS_CNT_CANDI");
        } elseif ($this->cmd == 'knjl770h') {
            $this->field["BORDER_SCORE"]        = VARS::post("BORDER_SCORE");
            $this->field["BORDER_SCORE_CANDI"]  = VARS::post("BORDER_SCORE_CANDI");
        } else {
            $this->field = array();
        }
    }
    //入力項目チェック
    public function validateInput()
    {
        if (!$this->applicantdiv) {
            $this->setWarning("MSG301", "（ 学校種別 ）");
            return false;
        }
        if (!$this->testdiv) {
            $this->setWarning("MSG301", "（ 入試種別 ）");
            return false;
        }
        if (!$this->field["BORDER_SCORE"]) {
            $this->setWarning("MSG301", "（ 合格点 ）");
            return false;
        }
        //点数の大小チェック
        if ($this->field["BORDER_SCORE"] <= $this->field["BORDER_SCORE_CANDI"]) {
            $this->setWarning("MSG901", "繰上合格点に合格点以上の数値が入力されています。");
            return false;
        }
        return true;
    }
    /**
     * メイン表示用のモデルを返します
    */
    public function &getMainModel()
    {
    }
    // シミュレーションのモデルを返します
    public function &getSimModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->sim();
            $this->setMessage("MSG201", "確定ボタンを押下しないとデータは保存されません。");
        }
    }
    public function &sim()
    {
        //割り振り実行
        knjl770hQuery::sim($this);
    }
    // 確定のモデルを返します
    public function &getDecisionModel()
    {
        if ($this->validateInput()) {
            //仮判定データ
            $db = Query::dbCheckOut();
            $query  = knjl770hQuery::getCntPassingmark($this);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rtnCnt = $passingRow["GOUKAKU_CNT"] + $passingRow["FUGOUKAKU_CNT"];

            if ($rtnCnt < 1) {
                $this->setWarning("MSG203", "（ 確定するデータが存在しません。シミュレーションを実行して下さい。）");
                return false;
            }
            
            //確定情報にどの性別のデータがあるかチェック
            $query  = knjl770hQuery::getDecisionData($this);
            $result = $db->query($query);
            $existsManDataFlg      = false;
            $existsWomanDataFlg    = false;
            $existsCommonDataFlg   = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $existsManDataFlg      = ($row["SEX"] == "1" || $row["SEX"] == "9") ? true : $existsManDataFlg;
                $existsWomanDataFlg    = ($row["SEX"] == "2" || $row["SEX"] == "9") ? true : $existsWomanDataFlg;
            }

            //既にある性別のデータを上書き更新できないようチェックをかける
            if ($existsManDataFlg && ($this->sex == "1" || $this->sex == "9") || $existsWomanDataFlg && ($this->sex == "2" || $this->sex == "9")) {
                $this->setWarning("MSG203", "（ 指定した入試区分・性別の確定情報は既に存在しています。）");
                return false;
            }

            Query::dbCheckIn($db);

            //確定
            $this->decision();
            $this->setMessage("MSG201");
        }
    }
    public function &decision()
    {
        //割り振り実行
        knjl770hQuery::decision($this);
    }
    // 起動時に、仮判定データすべて削除（初期化）
    public function &getJudgeTmpClearModel()
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM ENTEXAM_JUDGE_TMP TMP";
        if ($this->cmd == "clear") { //取消ボタン押下時は指定した条件で削除
            $query .= " WHERE ";
            $query .= "         TMP.ENTEXAMYEAR   = '{$this->year}' ";
            $query .= "     AND TMP.APPLICANTDIV  = '{$this->applicantdiv}' ";
            $query .= "     AND TMP.TESTDIV       = '{$this->testdiv}' ";
            if ($this->sex != "9") {
                $query .= "     AND EXISTS ( ";
                $query .= "             SELECT ";
                $query .= "                 'X' ";
                $query .= "             FROM ";
                $query .= "                 ENTEXAM_APPLICANTBASE_DAT BASE ";
                $query .= "             WHERE ";
                $query .= "                 BASE.ENTEXAMYEAR       = TMP.ENTEXAMYEAR ";
                $query .= "                 AND BASE.APPLICANTDIV  = TMP.APPLICANTDIV ";
                $query .= "                 AND BASE.TESTDIV       = TMP.TESTDIV ";
                $query .= "                 AND BASE.EXAMNO        = TMP.EXAMNO ";
                $query .= "                 AND BASE.SEX           = '{$this->sex}' ";
                $query .= "         ) ";
            }
        }

        $db->query($query);
        Query::dbCheckIn($db);
    }

    // 確定情報をクリア
    public function &getDecisionClearModel()
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_PASSINGMARK_MST KAKUTEI";
        $query .= " WHERE ";
        $query .= "         KAKUTEI.ENTEXAMYEAR   = '{$this->year}' ";
        $query .= "     AND KAKUTEI.APPLICANTDIV  = '{$this->applicantdiv}' ";
        $query .= "     AND KAKUTEI.TESTDIV       = '{$this->testdiv}' ";
        $query .= "     AND KAKUTEI.SHDIV         = '{$this->sex}' "; //SHDIVは性別コードとして代用している

        $db->query($query);
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
