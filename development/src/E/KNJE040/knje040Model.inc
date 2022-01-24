<?php

require_once('for_php7.php');


// 定数定義
class knje040Model extends Model {
    function knje040Model() {}
    function init() {
        $this->cmd      = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    //入力項目チェック
    function validateInput() {
        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {}

    /**
     * アップデート時の内部メソッド
     */
    function &update() {}

    /**
     * 変数をクリアします
     */
    function clean() {}
}
?>
