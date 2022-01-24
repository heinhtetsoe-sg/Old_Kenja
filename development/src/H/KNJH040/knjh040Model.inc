<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh040Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $useSchregRegdHdat; //フラグ

    /**
     * メンバ変数の初期化
     */
    function knjh040Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init() {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),     //年度
                             "GAKUNEN"          =>  VARS::post("GAKUNEN"),  //学年
                             "GAKKI"            =>  VARS::post("GAKKI"),    //今学期
                             "DBNAME"           =>  VARS::post("DBNAME")    //DB変更に備えて
                             );


        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->useSchregRegdHdat = ($this->Properties["useSchregRegdHdat"] == '1') ? $this->Properties["useSchregRegdHdat"] : 0;
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
