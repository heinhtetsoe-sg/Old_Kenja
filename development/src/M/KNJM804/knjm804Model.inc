<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm804Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $select_opt = array();  //セレクトデータ
    var $selectleft;
    var $selectleftval;

    /**
     * メンバ変数の初期化
     */
    function knjm804Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "COLLECT_GRP_CD"   =>  VARS::post("COLLECT_GRP_CD"),   //会計グループCD
                             "OUTPUTDATE"       =>  VARS::post("OUTPUTDATE"),       //処理日
                             "OUTPUT_NO"        =>  VARS::post("OUTPUT_NO")         //帳票NO
                             );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
