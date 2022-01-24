<?php

require_once('for_php7.php');


class knjh302Model extends Model {

        var $field        = array();
        var $clear        = array();
        var $cmd;
        var $schregno;
        var $control_data = array();    //コントロールマスタデータを保持
        var $sec_competence;            //セキュリティーチェック

    	function knjh302Model()
    	{
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    	}

        function init()
        {
        		$this->cmd = VARS::request("cmd");

				$this->field = array("DETAIL_SDATE"  =>  VARS::post("DETAIL_SDATE"),        //登録日付
    	        					 "DETAIL_DIV"    =>  VARS::post("DETAIL_DIV"),          //詳細区分
            	                     "DETAILCD"      =>  VARS::post("DETAILCD"),            //罰則
                	                 "CONTENT"       =>  VARS::post("CONTENT"),             //賞罰内容
                    	             "REMARK"        =>  VARS::post("REMARK"),              //備考
                                	 );

                $this->schregno   = VARS::request("SCHREGNO");

        }


}
?>
