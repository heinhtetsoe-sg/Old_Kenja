<?php

require_once('for_php7.php');

//メニュー画面
class knjm430mModel extends model
{
    var $error_flg = false; //入力エラーフラグ
    
    //M.I
    var $cmd;                           //一覧表示時のcmd
    var $sub;                           //講座コード+科目コード
    var $order;                         //表示順（学籍番号順／クラス番号順）
    var $line;                          //カレントページの１行目の全体の中での行数
    var $linecounts;                    //カレントページの行数
    var $admin;                         //編集可能学期
    var $adm_array = array();           //編集可能学期（配列）
    
    function knjm430mModel() {
    }

    function init() {
        $this->cmd    = VARS::request("cmd");

        $this->sub = VARS::post("SELSUB");
        $this->order = VARS::post("ORDER");

        $this->line = VARS::post("line");

        $this->linecounts = VARS::post("linecounts");

        $this->admin = VARS::post("admin");

        //前ページ
        if ($this->cmd == "pre" ) {
            $this->line -=500;
            $this->cmd = "update";
        }
        //次ページ
        if ($this->cmd == "next" ) {
            $this->line +=500;
            $this->cmd = "update";
        }

        if ($this->cmd == "update" and $this->admin != "") {
            $this->adm_array = explode(",",$this->admin);
            for ($ini = 1 ; $ini <= $this->linecounts ; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO_".$ini);
                //編集可能になっている値のみ取得
                $this->setdata["SEM1_INTR_VALUE"][$ini] = VARS::post("SEM1_INTR_VALUE_".$ini);
                $this->setdata["SEM1_TERM_VALUE"][$ini] = VARS::post("SEM1_TERM_VALUE_".$ini);
                $this->setdata["SEM2_INTR_VALUE"][$ini] = VARS::post("SEM2_INTR_VALUE_".$ini);
                $this->setdata["SEM2_TERM_VALUE"][$ini] = VARS::post("SEM2_TERM_VALUE_".$ini);
                $this->setdata["GRAD_VALUE2"][$ini] = VARS::post("GRAD_VALUE2_".$ini);
                $this->setdata["REMARK"][$ini] = VARS::post("REMARK_".$ini);
            }
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * メイン表示用のモデルを返します
    */
    function getMainModel() {
        $this->setdata = array();               //表示データ配列
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    // 編集完了用のモデルを返します
    function getUpdateModel() {
        if ($this->admin != "") {            //編集可能欄がない時は何もしない
            //入力チェック
            if ($this->validateInput()) {
                $this->update();
                $this->setMessage("MSG201");
            }
        }
    }

    function update() {
        $result = knjm430mQuery::getUpdateQuery($this);
        return $result;
    }


}
?>
