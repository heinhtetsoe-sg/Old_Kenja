<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjs340aModel extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjs340aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),  //校種
                             "DATE_FROM"        =>  VARS::post("DATE_FROM"),    //開始日付
                             "DATE_TO"          =>  VARS::post("DATE_TO"),      //終了日付
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>