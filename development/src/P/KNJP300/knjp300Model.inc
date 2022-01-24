<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp300Model extends Model {
    var $field       = array();
    var $select_data = array();
    var $select_opt  = array();    //セレクトデータ
    var $cmd;
    var $control;    //コントロールマスタ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjp300Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("DATE"            =>  VARS::post("DATE"),            //納入期限日
                             "YEAR"            =>  VARS::post("YEAR"),            //年度
                             "APPLICATION"     =>  VARS::post("APPLICATION"),     //申込み費目
                             "OUTPUT"          =>  VARS::post("OUTPUT"),          //出力種類
                             "OUTPUT2"         =>  VARS::post("OUTPUT2"),         //用紙種類
                             "TAISYOSYA"       =>  VARS::post("TAISYOSYA"),       //対象者
                             "GRADE_HR_CLASS"  =>  VARS::post("GRADE_HR_CLASS"),  //年組
                             "DBNAME"          =>  VARS::post("DBNAME")           //DB変更に備えて
                     );
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd        = VARS::request("cmd");
        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') {
            $this->select_opt = array();
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
