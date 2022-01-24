<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd220Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd220Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							"GAKKI"				=>	VARS::post("GAKKI"),	        //学期コード
							"GAKUNEN"			=>	VARS::post("GAKUNEN"),	        //学年
							"YURYO_OVER"		=>  VARS::post("YURYO_OVER"),		//評定平均（以上）
							"YURYO_UNDER"		=>  VARS::post("YURYO_UNDER"),		//評定平均（未満）
							"FURYO_S"			=>  VARS::post("FURYO_S"),			//成績不良科目数
							"DATE"				=>  VARS::post("DATE"),				//皆出席基準日付
							"KINTAI1_S"			=>  VARS::post("KINTAI1_S"),		//遅刻
							"KINTAI2_S"			=>  VARS::post("KINTAI2_S"),		//欠席
							"KINTAI3_S"			=>  VARS::post("KINTAI3_S"),		//欠課
							"KINTAI4_S"			=>  VARS::post("KINTAI4_S"),		//早退   //add  04/07/13  yamauchi
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
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
