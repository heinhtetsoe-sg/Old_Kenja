<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd325Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd325Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							"GAKKI"				=>	VARS::post("GAKKI"),	        //学期コード
//							"CLASS"				=>	VARS::post("CLASS"),	        //CLASS
//							"COURSESHOW"		=>	VARS::post("COURSESHOW"),	    //クラスコースコード
							"TESTKINDCD"		=>	VARS::post("TESTKINDCD"),	    //テスト種別コード
//							"COURSE_CD"			=>	VARS::post("COURSE_CD"),	    //コースCD
							"DATE"	            =>  VARS::post("DATE"),             //異動対象日付
							"FINCD"	            =>  VARS::post("FINCD"),            //Ｆ表示有り---2005.05.12
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


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
