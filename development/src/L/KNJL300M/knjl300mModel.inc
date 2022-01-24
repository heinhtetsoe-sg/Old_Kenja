<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl300mModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl300mModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),      		//年度
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDV"           =>  VARS::post("TESTDV"),     		//入試区分
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE"),     	//受験型
                             "NOINF_ST"        	=>  VARS::post("NOINF_ST"),     	//受付番号開始
                             "NOINF_ED"        	=>  VARS::post("NOINF_ED"),     	//受付番号終了
							 "POROW"   			=>  VARS::post("POROW"),    		//行
							 "POCOL"   			=>  VARS::post("POCOL")    			//列
                     );

        $this->cmd = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
