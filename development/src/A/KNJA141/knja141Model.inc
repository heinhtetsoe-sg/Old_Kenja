<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja141Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード
	var $select_opt  = array();	//セレクトデータ//---2005.07.15
    var $selectleft;//---2005.07.15

    /**
     * メンバ変数の初期化
     */
    function knja141Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							 "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
							 "GAKKI"			=>	VARS::post("GAKKI"),	        //学期
							 "OUTPUT"			=>	VARS::post("OUTPUT"),	        //フォーム
							 "DISP"			    =>	VARS::post("DISP"),	            //表示選択---2005.07.15
							 "TERM_SDATE"		=>	VARS::post("TERM_SDATE"),	    //有効期限(開始)
							 "TERM_EDATE"		=>	VARS::post("TERM_EDATE"),	    //有効期限(終了)
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        //---2005.07.15
        $this->selectleft = VARS::post("selectleft");
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
