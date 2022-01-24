<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd231Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjd231Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							"ASSESS"			=>	VARS::post("ASSESS"),		    //評価区分選択
							"GAKKI"				=>	VARS::post("GAKKI"),	        //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     		);

	    $this->selectdata = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
    }


    function getDownloadModel()
    {

		$grade_class = array();
		$grade_class = explode(",", $this->selectdata);	//年組

        $db = Query::dbCheckOut();

        $contents = "";	//CSVデータ出力用
        $data = false;

		//ヘッダ
        $wrk_year = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);	//年度
        $wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);	//作成日

        $contents .= ",,".$wrk_year."　　".$this->control["学期名"][$this->field["GAKKI"]] ."　　成績不振者一覧\n\n";
        $contents .= "＊評価が".$this->field["ASSESS"]."以下の生徒　　".$wrk_date."\n";
	//2004/07/09
        $contents .= "学年,組,出席番号,学籍番号,名前,総単位数,科目,評価,単位数,欠課,(うち無届)\n";
		for ($i=0; $i<get_count($grade_class); $i++){

/*	//2004/07/09
	        $hr_name = "";
			if ($this->field["GAKKI"]=="9"){
				$semester = $this->control["学期数"];
			} else {
				$semester = $this->field["GAKKI"];
			}

		    //組名称取得
	   	    $query = knjd231Query::getHr_Name($semester,$grade_class[$i]);
    	   	$hr_name = $db->getOne($query);
*/
		    //成績不振者一覧取得
			$attendno = "0";
	   	    $query = knjd231Query::pre_stat($this,$grade_class[$i]);
    	   	$result = $db->query($query);
        	while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
				//データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
	       	    $row = str_replace(",",".",$row);

			//	出席番号のブレイク
				if( $row["ATTENDNO"] != $attendno ){
//			        $contents .= $hr_name .",";
			        $contents .= substr($grade_class[$i],0,2) .",";	//2004/07/09
			        $contents .= substr($grade_class[$i],2) .",";	//2004/07/09
			        $contents .= $row["ATTENDNO"] .",";
			        $contents .= $row["SCHREGNO"] .",";
			        $contents .= $row["NAME"] .",";
			        $contents .= $row["A_CREDITS"] .",";

					$attendno = $row["ATTENDNO"];
				} else {
			        $contents .= ",,,,,,";
				}
		        $contents .= $row["SUBCLASSNAME"] .",";
		        $contents .= $row["VALUATION"] .",";
		        $contents .= $row["CREDITS"] .",";
		        $contents .= $row["ABSENT"] .",";
		        $contents .= $row["ABSENT2"] ."\n";

//		        $contents .= implode($row, ",") ."\n";
	    	    $data = true;
	       	}
    	    $result->free();

		}//for

   	    Query::dbCheckIn($db);

        if ($data){
	        $couse="成績不振者一覧";
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
