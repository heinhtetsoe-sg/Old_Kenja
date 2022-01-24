<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm020mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;

    /**
     * メンバ変数の初期化
     */
    function knjm020mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array(
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //生徒の選択のラジオボタン
                            "S_SCHREGNO"        =>  VARS::post("S_SCHREGNO"),       //学生番号　始め
                            "E_SCHREGNO"        =>  VARS::post("E_SCHREGNO"),       //学生番号　終り
                            "BUSU"              =>  VARS::post("BUSU"),             //科目コード
                            "FORM_ONLY"         =>  VARS::post("FORM_ONLY"),        //フォームのみ出力
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
