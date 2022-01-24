<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj140Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata = array();

    /**
     * メンバ変数の初期化
     */
    function knjj140Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("DATE"         =>  VARS::post("DATE"),     //処理年月日
                             "YEAR"         =>  VARS::post("YEAR"),     //年度
                             "GAKKI"        =>  VARS::post("GAKKI"),    //学期コード
                             "hogosya"      =>  VARS::post("hogosya"),  //保護者、住所、電話番号
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");

        $this->selectdata   = VARS::post("selectdata");
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
