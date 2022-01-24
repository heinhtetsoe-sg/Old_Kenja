<?php
//メニュー画面
class knjm390dModel extends model {
    var $error_flg = false; //入力エラーフラグ
    var $data       = array();
    var $cmd;                           //一覧表示時のcmd
    var $semester;                       //学期コード
    var $sub;                           //講座コード+科目コード
    var $order;                         //表示順（学籍番号順／クラス番号順）

    function knjm390dModel() {
    }

    function init() {
        $this->cmd      = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER");
        $this->chairCd  = VARS::post("CHAIRCD");
        $this->order    = VARS::post("ORDER");

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
        for ($i = 0; $i < count($this->data["SCHREGNO"]); $i++) {
            $schregNo = $this->data["SCHREGNO"][$i];

            $this->fields["SCHOOLIN_CNT"][$schregNo] = VARS::post("SCHOOLIN_CNT-".$schregNo);
        }

        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function update() {
        $result = knjm390dQuery::updateQuery($this);
        return $result;
    }
}
?>
