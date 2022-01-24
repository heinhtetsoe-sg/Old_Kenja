<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm510Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $subcd;     //科目コード
    var $chacd;     //口座コード
        
    /**
     * メンバ変数の初期化
     */
    function knjm510Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "OUTPUT"           =>  VARS::post("OUTPUT")            //印刷対象
                     );
        $this->subcd = substr($this->field["SUBCLASS"],0,6);
        $this->chacd = substr($this->field["SUBCLASS"],6,7);

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
