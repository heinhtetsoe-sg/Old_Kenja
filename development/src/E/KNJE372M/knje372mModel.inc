<?php

/**
 * 定数定義
*/
class knje372mModel extends Model {
    var $cmd;
    var $control;

    /**
     * メンバ変数の初期化
     */
    function knje372mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->field["TOROKU_DATE"]     = VARS::post("TOROKU_DATE");    //登録日

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }


    /**
     * 実行モデルを返します
    */
    function &getExecModel() {
        $db = Query::dbCheckOut();
        knje372mQuery::getUpdateQuery($db, $this);
        Query::dbCheckIn($db);
        $this->setMessage("MSG202");
    }
}
?>
