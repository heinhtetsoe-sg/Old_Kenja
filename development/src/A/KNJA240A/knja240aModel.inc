<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja240aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knja240aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "DATE"         =>  VARS::post("DATE"),         //処理年月日
                             "FORM"         =>  VARS::post("FORM"),         //フォーム選択
                             "NO_SEIBETSU"  =>  VARS::post("NO_SEIBETSU"), //性別欄無し
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useSchregRegdHdat"] = ($this->Properties["useSchregRegdHdat"] == '1') ? $this->Properties["useSchregRegdHdat"] : 0;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
