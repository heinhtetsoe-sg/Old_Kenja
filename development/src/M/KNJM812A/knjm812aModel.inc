<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm812aModel extends Model {
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
    function knjm812aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //印刷対象
                             "OUTPUT2"          =>  VARS::post("OUTPUT2"),          //出力順        NO001
                             "CHECK1"           =>  VARS::post("CHECK1"),           //生徒名出力    NO001
                             "CHECK2"           =>  VARS::post("CHECK2"),           //学籍番号出力  NO001
							 "GRDDIV"   		=>  VARS::post("GRDDIV"),    		//出力条件
                             "POROW"            =>  VARS::post("POROW"),            //行
                             "POCOL"            =>  VARS::post("POCOL")             //列
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class') $this->select_opt = array();
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
