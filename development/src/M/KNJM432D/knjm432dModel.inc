<?php

require_once('for_php7.php');

//メニュー画面
class knjm432dModel extends model {
    var $data       = array();
    var $cmd;                       //一覧表示時のcmd
    var $semester;                  //学期コード
    var $subclassCd;                //科目コード
    var $chairCd;                   //講座コード
    var $testCd;                    //テストコード
    var $order;                     //表示順（学籍番号順／クラス番号順）

    function knjm432dModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->semester);
            unset($this->subclassCd);
            unset($this->chairCd);
            unset($this->testCd);
        }

        $this->cmd        = VARS::request("cmd");
        $this->semester   = VARS::post("SEMESTER");
        $this->chairCd    = VARS::post("CHAIRCD");
        $this->subclassCd = VARS::post("SUBCLASSCD");
        $this->order      = VARS::post("ORDER");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregNo = $this->data["SCHREGNO"][$i];

            if ($this->fields["VALUE"][$schregNo] != "" && ($this->fields["VALUE"][$schregNo] < 1 || 5 < $this->fields["VALUE"][$schregNo])) {
                $this->setWarning("MSG901", "評定は「1～5」を入力して下さい。");
                return false;
            }
        }

        return true;
    }

    // 編集完了用のモデルを返します
    function getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregNo = $this->data["SCHREGNO"][$i];

            $this->fields["SCORE"][$schregNo]      = VARS::post("SCORE-".$schregNo);
            $this->fields["VALUE"][$schregNo]      = VARS::post("VALUE-".$schregNo);
            $this->fields["GET_CREDIT"][$schregNo] = VARS::post("GET_CREDIT-".$schregNo);
        }

        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function update() {
        $result = knjm432dQuery::updateQuery($this);
        return $result;
    }
}
?>
