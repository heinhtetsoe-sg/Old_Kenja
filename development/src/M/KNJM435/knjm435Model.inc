<?php

require_once('for_php7.php');

//メニュー画面
class knjm435Model extends model
{
    var $error_flg = false; //入力エラーフラグ

    //M.I
    var $cmd;                           //一覧表示時のcmd
    var $sub;                           //講座コード+科目コード
    var $order;                         //表示順（学籍番号順／クラス番号順）
    var $selcla;                        //講座受講クラス
    var $line;                          //カレントページの１行目の全体の中での行数
    var $linecounts;                    //カレントページの行数
    var $admin;                          //編集可能学期
    var $adm_array = array();            //編集可能学期（配列）
    
    function knjm435Model()
    {
    }
        
    function init()
    {

        $this->cmd    = VARS::request("cmd");

        $this->sub = VARS::post("SELSUB");
        $this->order = VARS::post("ORDER");
        $this->selcla = VARS::post("SELCLA");

        $this->line = VARS::post("line");

        $this->linecounts = VARS::post("linecounts");

        $this->admin = VARS::post("admin");

        $this->assessmax = VARS::post("ASSESSMAX");
        $this->assessmin = VARS::post("ASSESSMIN");

        //前ページ
        if ($this->cmd == "pre" ) {
            $this->line -=50;
            $this->cmd = "update";
        }
        //次ページ
        if ($this->cmd == "next" ) {
            $this->line +=50;
            $this->cmd = "update";
        }

        if ($this->cmd == "update" and $this->admin != "") {         //編集可能欄がない時は何もしない
            $this->adm_array = explode(",",$this->admin);
            for ($ini = 1 ; $ini <= $this->linecounts ; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO-".$ini);
                //編集可能になっている値のみ取得
                if (in_array("0111",$this->adm_array) == true) {
                    $this->setdata["SEM1_TERM_SCORE"][$ini] = VARS::post("SEM1_TERM_SCORE-".$ini);
                }
                    $this->setdata["SEM1_VALUE"][$ini] = VARS::post("SEM1_VALUE-".$ini);
                if (in_array("0211",$this->adm_array) == true) {
                    $this->setdata["SEM2_TERM_SCORE"][$ini] = VARS::post("SEM2_TERM_SCORE-".$ini);
                }
                $this->setdata["SEM2_VALUE"][$ini] = VARS::post("SEM2_VALUE-".$ini);
                $this->setdata["GRAD_VALUE"][$ini] = VARS::post("GRAD_VALUE-".$ini);
                $this->setdata["CREDITS"][$ini] = VARS::post("CREDITS-".$ini);
                $this->setdata["AUTHORIZE_FLG"][$ini] = VARS::post("AUTHORIZE_FLG-".$ini);
            }
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * メイン表示用のモデルを返します
    */
    function getMainModel()
    {
        $this->setdata = array();               //表示データ配列
    }

    //入力項目チェック
    function validateInput()
    {
        //評定マスタを参照して評定の範囲チェック
        if (CTRL_YEAR >= '2013') {
            if ((in_array("0882",$this->adm_array) == true) ||
               (in_array("0882",$this->adm_array) == false && in_array("0212",$this->adm_array) == true)) {

                for ($i=1; $i<=get_count($this->setdata["SCHREGNO"]); $i++) {
                    if ($this->setdata["GRAD_VALUE"][$i] != "" && ($this->setdata["GRAD_VALUE"][$i] < $this->assessmin || $this->setdata["GRAD_VALUE"][$i] > $this->assessmax)) {
                        $this->setWarning("MSG914",$this->assessmin."～".$this->assessmax."以内で入力してください。");
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    // 編集完了用のモデルを返します
    function getUpdateModel()
    {
        if ($this->admin != ""){            //編集可能欄がない時は何もしない
            //入力チェック
            if ($this->validateInput()) {
                $this->update();
                $this->setMessage("MSG201");
            }
        }
    }

    function update()
    {
        $result = knjm435Query::getUpdateQuery($this);

        return $result;
    }
}
?>
