<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl320hModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl320hModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APDIV"		=>  VARS::post("APDIV"),     		//入試制度
                             "TESTDV"		=>  VARS::post("TESTDV"),     		//入試区分
                             "TESTSCR"		=>  VARS::post("TESTSCR"),     		//指定範囲
                             "SORT"		    =>  VARS::post("SORT"),     		//印刷順序選択
                             "NAME_OUTPUT"	=>  VARS::post("NAME_OUTPUT")     	//氏名印字選択
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
