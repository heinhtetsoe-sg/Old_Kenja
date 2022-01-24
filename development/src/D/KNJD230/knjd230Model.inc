<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd230Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd230Model()
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
							"GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //年組
							"KINTAI"			=>  VARS::post("KINTAI"),           //勤怠
							"IDO"				=>  VARS::post("IDO"),			    //異動
							"ALL"				=>  VARS::post("ALL"),		        //皆出席者
							"DATE"				=>  VARS::post("DATE"),		        //皆出席者基準日付
							"SEISEKI"			=>  VARS::post("SEISEKI"),		    //成績優秀者
							"H_OVER"			=>  VARS::post("H_OVER"),			//評定平均（以上）
							"H_UNDER"			=>  VARS::post("H_UNDER"),		    //評定平均（未満）
							"HORYU"			    =>  VARS::post("HORYU"),		    //単位保留懸念者
							"ASSESS"			=>  VARS::post("ASSESS"),		    //評価
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
    }


    function getDownloadModel()
    {

        if ($data){
	        $couse="単位保留懸念者";
            $filename = sprintf("%s.csv",$couse);
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }//getDownloadModelの綴り


    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
