<?php

require_once('for_php7.php');

//メニュー画面
class knjm430kModel extends model
{
    var $error_flg = false; //入力エラーフラグ
    
    //M.I
    var $cmd;                           //一覧表示時のcmd
    var $sub;                           //講座コード+科目コード
    var $chair;                         //学級・講座コード
    var $order;                         //表示順（学籍番号順／クラス番号順）
    var $line;                          //カレントページの１行目の全体の中での行数
    var $linecounts;                    //カレントページの行数
    
    var $testcdArray = array();
    var $setdata = array();

    function knjm430kModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->testcdArray = array();
        }

        $this->cmd = VARS::request("cmd");
        $this->sub = VARS::post("SELSUB");
        $this->chair = VARS::post("CHAIRCD");
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
        //更新
        if ($this->cmd == "update") {
            for ($ini = 1 ; $ini <= $this->linecounts ; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO_".$ini);
                //編集可能になっている値のみ取得
                foreach ($this->testcdArray as $key => $codeArray) {
                    $this->setdata["SCORE".$key][$ini] = VARS::post("SCORE".$key."_".$ini);
                    $this->setdata["HIDDEN".$key][$ini] = VARS::post("HIDDEN".$key."_".$ini);
                    $this->setdata["PASS_SCORE".$key][$ini] = VARS::post("PASS_SCORE".$key."_".$ini);
                }
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
        for ($ini = 1 ; $ini <= $this->linecounts ; $ini++) {
            //編集可能になっている値のみ取得
            foreach ($this->testcdArray as $key => $codeArray) {
                if($this->setdata["HIDDEN".$key][$ini]){
                    //未入力チェック
                    if ($this->setdata["SCORE".$key][$ini] == "") {
                        $this->setWarning("MSG301");
                        return false;
                    }

                    //数値もしくは*
                    if ($this->setdata["SCORE".$key][$ini] != '*') {
                        if(!is_numeric($this->setdata["SCORE".$key][$ini])){
                            $this->setWarning("0点～100点以内もしくは*を入力してください。");
                            return false;
                        }
                        if($this->setdata["SCORE".$key][$ini] > 100){
                            $this->setWarning("MSG914");
                            return false;
                        }
                        if($this->setdata["SCORE".$key][$ini] < 0){
                            $this->setWarning("MSG914");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function update() {
        $result = knjm430kQuery::getUpdateQuery($this);
        return $result;
    }


}
?>
