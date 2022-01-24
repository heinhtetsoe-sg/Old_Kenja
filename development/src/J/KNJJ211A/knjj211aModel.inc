<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj211aModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjj211aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("DATE"          =>  VARS::post("DATE"),          //処理年月日
                             "SCHOOL_KIND"   =>  VARS::post("SCHOOL_KIND"),   //校種
                             "YEAR"          =>  VARS::post("YEAR"),          //年度
                             "DIV"           =>  VARS::post("DIV"),           //区分
                             "GAKKI"         =>  VARS::post("GAKKI"),         //学期コード
                             "NAME_NASI"     =>  VARS::post("NAME_NASI"),     //名前無し
                             "NAMELIST_ONLY" =>  VARS::post("NAMELIST_ONLY"), //名簿のみ
                             "NAME_KANA"     =>  VARS::post("NAME_KANA"),     //かな氏名
                             "DBNAME"        =>  VARS::post("DBNAME")         //DB変更に備えて
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
