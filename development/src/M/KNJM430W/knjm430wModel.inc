<?php

require_once('for_php7.php');

//メニュー画面
class knjm430wModel extends model
{
    public $error_flg = false; //入力エラーフラグ

    //M.I
    public $cmd;                           //一覧表示時のcmd
    public $sub;                           //講座コード+科目コード
    public $order;                         //表示順（学籍番号順／クラス番号順）
    public $line;                          //カレントページの１行目の全体の中での行数
    public $linecounts;                    //カレントページの行数

    public $testcdArray = array();
    public $setdata = array();

    public function knjm430wModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->testcdArray = array();
        }

        $this->cmd = VARS::request("cmd");
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
        //更新
        if ($this->cmd == "update") {
            for ($ini = 1; $ini <= $this->linecounts; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO_".$ini);
                //編集可能になっている値のみ取得
                foreach ($this->testcdArray as $key => $codeArray) {
                    $this->setdata["SCORE".$key][$ini] = VARS::post("SCORE".$key."_".$ini);
                    $this->setdata["SEM_PASS_FLG".$key][$ini] = VARS::post("SEM_PASS_FLG".$key."_".$ini);
                    $this->setdata["PASS_SCORE".$key][$ini] = VARS::post("PASS_SCORE".$key."_".$ini);
                }
                $this->setdata["REMARK"][$ini] = VARS::post("REMARK_".$ini);
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
        $result = knjm430wQuery::getUpdateQuery($this);
        return $result;
    }
}
