<?php
//メニュー画面
class knjm437wModel extends model
{
    var $error_flg = false; //入力エラーフラグ

    var $cmd;
    var $sub;
    var $test_date;

    function knjm437wModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->sub = VARS::post("SELSUB");
        $this->testcd = VARS::post("TESTCD");
        $this->test_date = VARS::post("TEST_DATE");

        $linecounts = VARS::post("DATA_CNT");
        if ($this->cmd == "update") {
            for ($ini = 1 ; $ini <= $linecounts ; $ini++) {
                $this->setdata["SCHREGNO"][$ini] = VARS::post("SCHREGNO_".$ini);
                $this->setdata["CHK_JUKEN"][$ini] = VARS::post("CHK_JUKEN_".$ini);
                $this->setdata["CHK_DIS"][$ini] = VARS::post("CHK_DIS_".$ini);
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
        $result = knjm437wQuery::getUpdateQuery($this);
        return $result;
    }


}
?>
