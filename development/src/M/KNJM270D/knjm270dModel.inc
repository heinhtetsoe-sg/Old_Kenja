<?php

require_once('for_php7.php');

//メニュー画面
class knjm270dModel extends model {
    var $error_flg = false; //入力エラーフラグ
    var $data       = array();
    var $cmd;                           //一覧表示時のcmd
    var $semester;                       //学期コード
    var $sub;                           //講座コード+科目コード
    var $order;                         //表示順（学籍番号順／クラス番号順）

    function knjm270dModel() {
    }

    function init() {
        $this->cmd      = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER");
        $this->subclassCd  = VARS::post("SUBCLASSCD");
        $this->chairCd  = VARS::post("CHAIRCD");
        $this->order    = VARS::post("ORDER");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");

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

            $this->fields["REPORT_CNT"][$schregNo] = VARS::post("REPORT_CNT-".$schregNo);
        }

        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function update() {
        $result = knjm270dQuery::updateQuery($this);
        return $result;
    }
}
?>
