<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf120Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjf120Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),			//年度
                            "DBNAME"          =>  VARS::post("DBNAME")          //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
    }

	/**
	 * ＣＳＶ出力
	 */
    function getDownloadModel()
    {
        //タイトル行 2004/01/26 add nakamoto
		$wrk_nendo = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),10);
		$title = "保健室利用者の推移";
		$wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        $contents = $wrk_nendo ."," .$title .",," ."作成日：" .$wrk_date ."\n";

		//ヘッダの作成//
        $header = array("KUBUN"       => "区分",
                        "NINZUU4"     => "4月",
                        "NINZUU5"     => "5月",
                        "NINZUU6"     => "6月",
                        "NINZUU7"     => "7月",
                        "NINZUU8"     => "8月",
                        "NINZUU9"     => "9月",
                        "NINZUU10"    => "10月",
                        "NINZUU11"    => "11月",
                        "NINZUU12"    => "12月",
                        "NINZUU1"     => "1月",
                        "NINZUU2"     => "2月",
                        "NINZUU3"     => "3月",
                        );

        $contents .= implode($header, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

		//SQL文発行（区分の取得）
        $query = knjf120Query::getSelectName($this);
        $result = $db->query($query);
		$opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_name1[$row["NAMECD"]] = $row["NAME"];
        }
		//出力データの作成//
        //SQL文発行
        $query = knjf120Query::selectCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        //月毎の合計（男女）
		$dansei_kei = $josei_kei = array();
		for($i=0;$i<12;$i++){
			$dansei_kei[$i] = 0;
			$josei_kei[$i] = 0;
		}
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["KUBUN"] = $opt_name1[$row["KUBUN"]];
	        $contents .= implode($row, ",") ."\n";

	        //月毎の合計（男女）を計算
			if($row["KUBUN"]=="内科男" || $row["KUBUN"]=="外科男"){
		        $dansei_kei[0]  = $dansei_kei[0]  + $row["NINZUU4"];
		        $dansei_kei[1]  = $dansei_kei[1]  + $row["NINZUU5"];
	    	    $dansei_kei[2]  = $dansei_kei[2]  + $row["NINZUU6"];
	        	$dansei_kei[3]  = $dansei_kei[3]  + $row["NINZUU7"];
		        $dansei_kei[4]  = $dansei_kei[4]  + $row["NINZUU8"];
		        $dansei_kei[5]  = $dansei_kei[5]  + $row["NINZUU9"];
	    	    $dansei_kei[6]  = $dansei_kei[6]  + $row["NINZUU10"];
	        	$dansei_kei[7]  = $dansei_kei[7]  + $row["NINZUU11"];
		        $dansei_kei[8]  = $dansei_kei[8]  + $row["NINZUU12"];
		        $dansei_kei[9]  = $dansei_kei[9]  + $row["NINZUU1"];
	    	    $dansei_kei[10] = $dansei_kei[10] + $row["NINZUU2"];
	        	$dansei_kei[11] = $dansei_kei[11] + $row["NINZUU3"];
			}
			if($row["KUBUN"]=="内科女" || $row["KUBUN"]=="外科女"){
		        $josei_kei[0]  = $josei_kei[0]  + $row["NINZUU4"];
		        $josei_kei[1]  = $josei_kei[1]  + $row["NINZUU5"];
	    	    $josei_kei[2]  = $josei_kei[2]  + $row["NINZUU6"];
	        	$josei_kei[3]  = $josei_kei[3]  + $row["NINZUU7"];
		        $josei_kei[4]  = $josei_kei[4]  + $row["NINZUU8"];
		        $josei_kei[5]  = $josei_kei[5]  + $row["NINZUU9"];
	    	    $josei_kei[6]  = $josei_kei[6]  + $row["NINZUU10"];
	        	$josei_kei[7]  = $josei_kei[7]  + $row["NINZUU11"];
		        $josei_kei[8]  = $josei_kei[8]  + $row["NINZUU12"];
		        $josei_kei[9]  = $josei_kei[9]  + $row["NINZUU1"];
	    	    $josei_kei[10] = $josei_kei[10] + $row["NINZUU2"];
	        	$josei_kei[11] = $josei_kei[11] + $row["NINZUU3"];
			}

            $data = true;
        }
        //月毎の合計（男女）の行を出力
        $contents .= "合計男" .",";
        $contents .= implode($dansei_kei, ",") ."\n";
        $contents .= "合計女" .",";
        $contents .= implode($josei_kei, ",") ."\n";

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
	        $couse="保健室利用者の推移";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
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

    }
}
?>
