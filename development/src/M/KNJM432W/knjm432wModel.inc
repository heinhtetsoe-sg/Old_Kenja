<?php
//メニュー画面
class knjm432wModel extends model
{
    public $error_flg = false; //入力エラーフラグ

    //M.I
    public $cmd;                           //一覧表示時のcmd
    public $semecmb;                       //学期コード
    public $sub;                           //講座コード+科目コード
    public $order;                         //表示順（学籍番号順／クラス番号順）
    public $line;                          //カレントページの１行目の全体の中での行数
    public $linecounts;                    //カレントページの行数
    public $testcdArray = array();
    public $setdata = array();

    public function knjm432wModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->testcdArray = array();
        }

        $this->cmd = VARS::request("cmd");
        $this->semecmb = VARS::post("SEMECMB");
        $this->sub = VARS::post("SELSUB");
        $this->subclasscd = VARS::post("SUBCLASSCD");
        $this->order = VARS::post("ORDER");
        $this->line = VARS::post("line");
        $this->linecounts = VARS::post("linecounts");
        //科目（講座）が変わったとき
        if ($this->cmd == "change" ) {
            $this->testcdArray = array();
        }
        //前ページ
        if ($this->cmd == "pre" ) {
            $this->line -=50;
        }
        //次ページ
        if ($this->cmd == "next" ) {
            $this->line +=50;
        }
        //出力順が変わったら、科目（講座）リストはクリアされる
        if ($this->cmd == "change_order" ) {
            $this->sub = "";
        }
        //評価・評定を入力しない科目(講座）・・・修得単位、履修単位を入力する
        $chrsub_array = array();
        $chrsub_array = explode("-", $this->sub);
        $this->inputCreditOnly = $chrsub_array[1] == "90" ? "1" : "";
        //更新
        if ($this->cmd == "update" || $this->cmd == "updatePrint") {
            for ($ini = 1; $ini <= $this->linecounts; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO_".$ini);
                if ($this->inputCreditOnly == "1") {
                    $this->setdata["GET_CREDIT"][$ini] = VARS::post("GET_CREDIT_".$this->setdata["SCHREGNO"][$ini]);
                    $this->setdata["COMP_CREDIT"][$ini] = VARS::post("COMP_CREDIT_".$this->setdata["SCHREGNO"][$ini]);
                } elseif ($this->Properties["useHyoutei"] == "1") {
                    $this->setdata["HYOUKA"][$ini] = VARS::post("HYOUKA_".$this->setdata["SCHREGNO"][$ini]);
                } elseif ($this->Properties["useHyoukaHyoutei"] == "1") {
                    $this->setdata["HYOUKA"][$ini] = VARS::post("HYOUKA_".$this->setdata["SCHREGNO"][$ini]);
                    $this->setdata["HYOUTEI"][$ini] = VARS::post("HYOUTEI_".$this->setdata["SCHREGNO"][$ini]);
                } else {
                    $this->setdata["HYOUTEI"][$ini] = VARS::post("HYOUTEI_".$this->setdata["SCHREGNO"][$ini]);
                }
                $this->setdata["COMP_CONTINUE"][$ini] = VARS::post("COMP_CONTINUE_".$this->setdata["SCHREGNO"][$ini]);
                $this->setdata["NINTEI"][$ini] = VARS::post("NINTEI_".$this->setdata["SCHREGNO"][$ini]);
                $this->setdata["PRINT"][$ini] = VARS::post("PRINT_".$this->setdata["SCHREGNO"][$ini]);
            }
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * メイン表示用のモデルを返します
    */
    public function getMainModel()
    {
        $this->setdata = array();               //表示データ配列
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    // 編集完了用のモデルを返します
    public function getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    public function update()
    {
        $result = knjm432wQuery::updateQuery($this);
        return $result;
    }

    public function getUpdatePrintModel()
    {

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $sep = "";
        $this->printPara = "";
        for ($i = 1; $i <= count($this->setdata["SCHREGNO"]); $i++) {
            //skip
            if ($this->setdata["PRINT"][$i] != "1") {
                continue;
            }
            $schno = $this->setdata["SCHREGNO"][$i];
            $query = knjm432wQuery::getCertifSchool();
            $certif_no_div = $db->getOne($query);
            if ($certif_no_div == 0) {
                $get_certif_maxnumchk = knjm432wQuery::get_certif_maxnumber($db, $this, $schno);
            }
            $retPara = knjm432wQuery::getUpdateIssueQuery(
                $db,
                $schno,
                knjm432wQuery::getissuername($db),
                $get_certif_maxnumchk,
                CTRL_DATE,
                $certif_no_div,
                $this
            );
            $this->printPara .= $sep.$retPara;
            $sep = ",";
        }

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }
}
