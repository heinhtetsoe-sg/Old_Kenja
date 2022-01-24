<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd291vModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $useSchregRegdHdat; //フラグ

    /**
     * メンバ変数の初期化
     */
    function knjd291vModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "DATE"             =>  VARS::post("DATE"),         //処理年月日
                             "OUTPUT"           =>  VARS::post("OUTPUT")        //出力
                     );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
