<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd160Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjd160Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),				//年度
                             "SEMESTER"			=>  VARS::post("SEMESTER"),			//学期
							 "TEST"				=>	VARS::post("TEST"),	            //テスト名 NO002
                             "GRADE_HR_CLASS"	=>  VARS::post("GRADE_HR_CLASS"),	//年組
							 "DATE"	            =>  VARS::post("DATE"),             //異動対象日付 /* NO004 */
//                             "OUTPUT"			=>  VARS::post("OUTPUT"),			//出力形式    04/06/21  yamauchi
                             "DBNAME"			=>  VARS::post("DBNAME"),			//DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
