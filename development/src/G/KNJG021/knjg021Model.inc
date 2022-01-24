<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg021Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjg021Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("SCHKIND"           =>  VARS::post("SCHKIND"),          //校種
                             "YEAR"              =>  VARS::post("YEAR"),             //年度
                             "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                             "OUTPUT_CERTIF_NO"  =>  VARS::post("OUTPUT_CERTIF_NO"), //発行番号を出力する
                             "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
