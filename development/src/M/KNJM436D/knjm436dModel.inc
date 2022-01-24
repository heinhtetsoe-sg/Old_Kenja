<?php

require_once('for_php7.php');

//メニュー画面
class knjm436dModel extends model {
    var $error_flg = false; //入力エラーフラグ
    var $data       = array();
    var $cmd;                       //一覧表示時のcmd
    var $semester;                  //学期コード
    var $subclassCd;                //科目コード
    var $chairCd;                   //講座コード
    var $testCd;                    //テストコード
    var $order;                     //表示順（学籍番号順／クラス番号順）
    var $testcdArray = array();
    var $setdata = array();

    function knjm436dModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->testcdArray = array();
        }

        $this->semester = "9";
        $this->testCd = "99-00-09";

        $this->cmd        = VARS::request("cmd");
        $this->chairCd    = VARS::post("CHAIRCD");
        $this->subclassCd = VARS::post("SUBCLASSCD");
        $this->order      = VARS::post("ORDER");
        //科目（講座）が変わったとき
        if ($this->cmd == "change" ) {
            $this->testcdArray = array();
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    // 編集完了用のモデルを返します
    function getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregNo = $this->data["SCHREGNO"][$i];

            $this->fields["VALUE"][$schregNo]      = VARS::post("VALUE-".$schregNo);
            $this->fields["GET_CREDIT"][$schregNo] = VARS::post("GET_CREDIT-".$schregNo);
            $this->fields["COMP_CREDIT"][$schregNo] = VARS::post("COMP_CREDIT-".$schregNo);
        }

        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function update() {
        $result = knjm436dQuery::updateQuery($this);
        return $result;
    }
}
?>
