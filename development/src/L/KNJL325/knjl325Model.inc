<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl325Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl325Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("APDIV"        	=>  VARS::post("APDIV"),     		//入試制度
                             "TESTDV"           =>  VARS::post("TESTDV"),     		//入試区分
							 "CHECK1"   		=>  VARS::post("CHECK1"),    		//印刷対象
							 "CHECK2"   		=>  VARS::post("CHECK2")    		//印刷対象
                     );

        $this->cmd         = VARS::request("cmd");
    }
	/**
	 * ＣＳＶ出力
	 */
    function getDownloadModel()
    {
			//ヘッダ
			$title = "入学試験　速報データ";
			$title1 = "志願者数、受験者数、合格者数";
			$title2 = "合格者の最高点、最低点、平均点";
			$nendo = $this->ObjYear."/04/01";
			$nendo = common::DateConv1($nendo,10);								//年度
			$ctrl_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);	//作成日
			//変数の初期値
	       	$contents = "";		//CSV出力フィールド

	        //DB接続
    	    $db = Query::dbCheckOut();

			//SQL文発行（入試区分・入試区分名称・入試日付を取得）
        	$result = $db->query(knjl325Query::getNameMst($this));
			$testname = $testdate = array();
    	    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	            $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
				//入試区分名称
				$testname[$row["NAMECD2"]] = $row["NAME1"];
				//入試日付
				$testdate[$row["NAMECD2"]] = ($row["NAMESPARE1"]!="") ? substr(common::DateConv1(str_replace("-","/",$row["NAMESPARE1"]),0), 12) : "";
    	    }

			//SQL文発行（入試区分取得（CSV用：ループ用））
        	$result = $db->query(knjl325Query::getRoopTestdiv($this));
			$opt_testdiv = array();
        	$opt_testdiv_flg = false;	//ループ用フラグ
    	    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	            $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
				//入試区分
				$opt_testdiv[] = $row["TESTDIV"];
	        	$opt_testdiv_flg = true;	//ループ用フラグ
    	    }

		//入試区分ループ
		if ($opt_testdiv_flg) {
		for ($ia = 0; $ia < get_count($opt_testdiv); $ia++) {
//-------------------------------------
			//１．速報データ（人数）
			//ヘッダ
			$header  = $nendo."　".$testname[$opt_testdiv[$ia]]."　".$title."\n";
			$header .= "入試日 ： ".$testdate[$opt_testdiv[$ia]]."\n";
			$header .= "作成日 ： ".$ctrl_date."\n";
			$header .= $title1."\n";
			$header .= ",人数\n";
			$contents .= $this->setCsvNumber($db, $header, $opt_testdiv[$ia]);	//速報データ（人数）のメソッド
			//２．速報データ（得点）
			//ヘッダ
			$header  = $nendo."　".$testname[$opt_testdiv[$ia]]."　".$title."\n";
			$header .= "入試日 ： ".$testdate[$opt_testdiv[$ia]]."\n";
			$header .= "作成日 ： ".$ctrl_date."\n";
			$header .= $title2."\n";
			$header .= ",国語,算数,社会,理科,４科計\n";
			$contents .= $this->setCsvScore($db, $header, $opt_testdiv[$ia]);	//速報データ（人数）のメソッド
//-------------------------------------
		}//for
		}//if
	        $result->free();
        	Query::dbCheckIn($db);

			//対象データ無し
			if ($contents == ""){
        	    $this->setWarning("MSG303");
    	        return false;
	        }

        $couse="入学試験　速報データ";
        $filename = sprintf("%s.csv",$couse);
        common::downloadContents($contents, $filename);
        return true;
    }

    /**
     * 速報データ（人数）
     */
    function setCsvNumber($db, $header, $testdiv)
    {
			$contents = "";
       	    $data = false;
			//ヘッダ
			$contents .= $header;
			//明細データ保管用配列の初期化
			for ($ig=1; $ig<10; $ig++) 			//行
				for ($ir=1; $ir<2; $ir++) 		//列
					$meisai[$ig][$ir] = "";		//明細データ保管用配列
			//明細データ取得
        	$result = $db->query(knjl325Query::getCsvNumber($this, $testdiv));
    	    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	            $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
				if ($row["TYPE_CNT"]=="A") $gyo = 1;		//志願者開始行
				if ($row["TYPE_CNT"]=="B") $gyo = 4;		//受験者開始行
				if ($row["TYPE_CNT"]=="C") $gyo = 7;		//合格者開始行
				for ($ib=1; $ib<4; $ib++) {
					$meisai[$gyo][1] = $row["TOTAL".$ib];	//人数
					$gyo++;
				}
        	    $data = true;
    	    }
			//明細データ保管用配列のセット
			for ($ig=1; $ig<10; $ig++) {		//行
				if (0 < $ig && $ig < 4) $contents .= "志願者";
				if (3 < $ig && $ig < 7) $contents .= "受験者";
				if (6 < $ig && $ig < 10) $contents .= "合格者";
				if ($ig==1 || $ig==4 || $ig==7) $contents .= "男子,";
				if ($ig==2 || $ig==5 || $ig==8) $contents .= "女子,";
				if ($ig==3 || $ig==6 || $ig==9) $contents .= "計,";
				for ($ir=1; $ir<2; $ir++) 		//列
					$contents .= $meisai[$ig][$ir].",";
				$contents .= "\n";
			}
			$contents .= "\n";

			if (!$data) $contents = "";//データがなければブランクを返す

        return $contents;
    }
    /**
     * 速報データ（得点）
     */
    function setCsvScore($db, $header, $testdiv)
    {
			$contents = "";
       	    $data = false;
			//ヘッダ
			$contents .= $header;
			//明細データ保管用配列の初期化
			for ($ig=1; $ig<6; $ig++) 			//行
				for ($ir=1; $ir<6; $ir++) 		//列
					$meisai[$ig][$ir] = "";		//明細データ保管用配列
			//明細データ取得
        	$result = $db->query(knjl325Query::getJudgeAvarage($this, $testdiv));
    	    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	            $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
				if ($row["TESTSUBCLASSCD"]=="1") $retsu = 1;		//国語
				if ($row["TESTSUBCLASSCD"]=="2") $retsu = 2;		//算数
				if ($row["TESTSUBCLASSCD"]=="3") $retsu = 3;		//社会
				if ($row["TESTSUBCLASSCD"]=="4") $retsu = 4;		//理科
				if ($row["TESTSUBCLASSCD"]=="A") $retsu = 5;		//４科計

				$meisai[1][$retsu] = ( $row["AVARAGE_MEN"] == "" ) ? "－" : number_format($row["AVARAGE_MEN"],1);	//平均点（男子）
				$meisai[2][$retsu] = ( $row["AVARAGE_WOMEN"] == "" ) ? "－" : number_format($row["AVARAGE_WOMEN"],1);	//平均点（女子）
				$meisai[3][$retsu] = number_format($row["AVARAGE_TOTAL"],1);	//平均点（合計）
				$meisai[4][$retsu] = $row["MAX_SCORE"];	//最高点
				if ($retsu < 5) {
					$meisai[5][$retsu] = ( $this->field["CHECK2"] != "" ) ? "－" : $row["MIN_SCORE"];	//最低点
				} else {
					$meisai[5][$retsu] = $row["MIN_SCORE"];	//最低点
				}

        	    $data = true;
    	    }
			//明細データ保管用配列のセット
			for ($ig=1; $ig<6; $ig++) {		//行
				if ($ig == 1) $contents .= "平均点男子";
				if ($ig == 2) $contents .= "平均点女子";
				if ($ig == 3) $contents .= "平均点合計";
				if ($ig == 4) $contents .= "最高点";
				if ($ig == 5) $contents .= "最低点";
				for ($ir=1; $ir<6; $ir++) 		//列
					$contents .= ",".$meisai[$ig][$ir];
				$contents .= "\n";
			}
			$contents .= "\n";

			if (!$data) $contents = "";//データがなければブランクを返す

        return $contents;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
