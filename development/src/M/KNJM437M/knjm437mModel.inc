<?php

require_once('for_php7.php');

//メニュー画面
class knjm437mModel extends model
{
    var $error_flg = false; //入力エラーフラグ

    var $cmd;
    var $sub;
    var $test_kind;
    var $input_date;

    function knjm437mModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->sub = VARS::post("SELSUB");
        $this->test_kind = VARS::post("TEST_KIND");
        $this->input_date = VARS::post("INPUT_DATE");

        $linecounts = VARS::post("DATA_CNT");
        if ($this->cmd == "update") {
            for ($ini = 1 ; $ini <= $linecounts ; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO_".$ini);
                $this->setdata["CHK_JUKEN"][$ini] = VARS::post("CHK_JUKEN_".$ini);
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
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function update() {
        $result = knjm437mQuery::getUpdateQuery($this);
        return $result;
    }


}
?>
