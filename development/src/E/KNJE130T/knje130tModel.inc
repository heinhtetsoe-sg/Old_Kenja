<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje130tModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje130tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                =>  VARS::post("YEAR"),             //年度
                            "GAKKI"                =>  VARS::post("GAKKI"),            //学期コード
                            "GRADE"                =>  VARS::post("GRADE"),            //学年
                            "OUT_DIV"              =>  VARS::post("OUT_DIV"),          //対象ラジオボタン 
                            "INCLUDE_MIRISYUU"     =>  VARS::post("INCLUDE_MIRISYUU"), //1なら未履修を含む
                            "HYOTEI_YOMIKAE"       =>  VARS::post("HYOTEI_YOMIKAE"),   //評定1の場合は2で処理する
                            "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
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
