<?php

require_once('for_php7.php');
//メニュー画面
class knjl080kModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $examno;                //開始受験番号
    var $applicantdiv;
    var $testdiv;
    var $field;
    var $view_examno;           //一覧表示されているすべての受験番号
    var $score;                 //入力された得点
    var $end_examno;            //終了受験番号
    function knjl080kModel()
    {
    }
        
    function init()
    {
        $this->cmd    = VARS::request("cmd");

        $this->year   = CTRL_YEAR + 1;

        if ($this->cmd == "read") {
            $this->examno = sprintf("%04d",VARS::post("EXAMNO"));
        }

        $this->testdiv = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");

        $this->a_score = VARS::post("A_SCORE");
        $this->view_examno = VARS::post("HID_EXAMNO");
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
        if ($this->cmd == "reset")
            $this->cmd = "read";

        if ($this->cmd == "back2") {
            $this->examno = sprintf("%04d",((int)$this->examno - 20));
            if ((int)$this->examno < 1) {
                $this->examno = "0001";
            } elseif ((int)$this->examno >= 9999) {
                $this->examno = "9999";
            }
        }
        if ($this->cmd == "next2") {
            $this->examno = sprintf("%04d",((int)$this->examno + 20));
            if ((int)$this->examno < 1) {
                $this->examno = "0001";
            } elseif ((int)$this->examno >= 9999) {
                $this->examno = "9999";
            }
        }

    }


    //入力項目チェック 2006/02/08
    function validateInput()
    {
        //得点を再配置
        $tmp = explode(",",$this->view_examno);
        unset($this->score);    //NO001
        foreach ($tmp as $key => $examno)
        {
            $this->score[$examno]["A_SCORE"] = $this->a_score[$key];
            $this->score[$examno]["B_SCORE"] = VARS::post("B_SCORE".$examno);
            # 得点入力チェック 2006/02/08
            #   両方得点ありまたは両方nullはOK、片方nullはエラー
            if (($this->score[$examno]["A_SCORE"] == '' && $this->score[$examno]["B_SCORE"] != '') ||
                ($this->score[$examno]["A_SCORE"] != '' && $this->score[$examno]["B_SCORE"] == '')) {
                $this->setWarning("MSG901","配点Aもしくは、配点Bのどちらかが未入力です。（ 受験番号：" . $examno ."）");
                return false;
           }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック 2006/02/08
        if ($this->validateInput()){
            knjl080kQuery::getUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201","もし偏差値計算及び合否処理が実行済みであれば再度実行してください。");
            }
            if ($this->cmd == "back") {
                $this->examno = sprintf("%04d",((int)$this->examno - 20));
                if ((int)$this->examno < 1) {
                    $this->examno = "0001";
                } elseif ((int)$this->examno >= 9999) {
                    $this->examno = "9999";
                }
            }
            if ($this->cmd == "next") {
                $this->examno = sprintf("%04d",((int)$this->examno + 20));
                if ((int)$this->examno < 1) {
                    $this->examno = "0001";
                } elseif ((int)$this->examno >= 9999) {
                    $this->examno = "9999";
                }
            }
        }
        return;
    }
}
?>
