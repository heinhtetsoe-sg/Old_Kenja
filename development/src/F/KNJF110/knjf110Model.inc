<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf110Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjf110Model()
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
		$title = "保健室利用者傷病集計";
		$wrk_date = common::DateConv1(str_replace("-","/",$this->control["学籍処理日"]),0);

        $contents = $wrk_nendo ."," .$title .",," ."作成日：" .$wrk_date ."\n";

		//ヘッダの作成//
        $header = array("KUBUN"       => "区分",
                        "NAMECD2"     => "傷病名",
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
                        "NINZUU"      => "計",
                        );

        $contents .= implode($header, ",") ."\n";

        //DB接続
        $db = Query::dbCheckOut();

		//外科的症状////////////////////////////////////////////////////////////////
		//SQL文発行（来室理由の取得）
        $query = knjf110Query::getSelectName("F730");
        $result = $db->query($query);
		$opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
		//出力データの作成//
        //SQL文発行
        $query = knjf110Query::selectCsvQuery1($this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（外科）
		$geka_kei = array();
		for($i=0;$i<13;$i++) $geka_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
	        $contents .= implode($row, ",") ."\n";
	        //月毎の小計（外科）を計算
	        $geka_kei[0]  = $geka_kei[0]  + $row["NINZUU4"];
	        $geka_kei[1]  = $geka_kei[1]  + $row["NINZUU5"];
	        $geka_kei[2]  = $geka_kei[2]  + $row["NINZUU6"];
	        $geka_kei[3]  = $geka_kei[3]  + $row["NINZUU7"];
	        $geka_kei[4]  = $geka_kei[4]  + $row["NINZUU8"];
	        $geka_kei[5]  = $geka_kei[5]  + $row["NINZUU9"];
	        $geka_kei[6]  = $geka_kei[6]  + $row["NINZUU10"];
	        $geka_kei[7]  = $geka_kei[7]  + $row["NINZUU11"];
	        $geka_kei[8]  = $geka_kei[8]  + $row["NINZUU12"];
	        $geka_kei[9]  = $geka_kei[9]  + $row["NINZUU1"];
	        $geka_kei[10] = $geka_kei[10] + $row["NINZUU2"];
	        $geka_kei[11] = $geka_kei[11] + $row["NINZUU3"];
	        $geka_kei[12] = $geka_kei[12] + $row["NINZUU"];

            $data = true;
        }
        //月毎の小計（外科）の行を出力
        $contents .= "外科的症状" ."," ."外科合計" .",";
        $contents .= implode($geka_kei, ",") ."\n";

		//内科的症状////////////////////////////////////////////////////////////////
		//SQL文発行（来室理由の取得）
        $query = knjf110Query::getSelectName("F720");
        $result = $db->query($query);
		$opt_name1 = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_name1[$row["NAMECD2"]] = $row["NAME1"];
        }
		//出力データの作成//
        //SQL文発行
        $query = knjf110Query::selectCsvQuery2($this);
        $result = $db->query($query);
        $data = false;
        //月毎の小計（内科）
		$naika_kei = array();
		for($i=0;$i<13;$i++) $naika_kei[$i] = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row["NAMECD2"] = $opt_name1[$row["NAMECD2"]];
	        $contents .= implode($row, ",") ."\n";
	        //月毎の小計（内科）を計算
	        $naika_kei[0]  = $naika_kei[0]  + $row["NINZUU4"];
	        $naika_kei[1]  = $naika_kei[1]  + $row["NINZUU5"];
	        $naika_kei[2]  = $naika_kei[2]  + $row["NINZUU6"];
	        $naika_kei[3]  = $naika_kei[3]  + $row["NINZUU7"];
	        $naika_kei[4]  = $naika_kei[4]  + $row["NINZUU8"];
	        $naika_kei[5]  = $naika_kei[5]  + $row["NINZUU9"];
	        $naika_kei[6]  = $naika_kei[6]  + $row["NINZUU10"];
	        $naika_kei[7]  = $naika_kei[7]  + $row["NINZUU11"];
	        $naika_kei[8]  = $naika_kei[8]  + $row["NINZUU12"];
	        $naika_kei[9]  = $naika_kei[9]  + $row["NINZUU1"];
	        $naika_kei[10] = $naika_kei[10] + $row["NINZUU2"];
	        $naika_kei[11] = $naika_kei[11] + $row["NINZUU3"];
	        $naika_kei[12] = $naika_kei[12] + $row["NINZUU"];

            $data = true;
        }
        //月毎の小計（内科）の行を出力
        $contents .= "内科的症状" ."," ."内科合計" .",";
        $contents .= implode($naika_kei, ",") ."\n";

        //月毎の総合計の行を出力
		$sougou_kei = array();
		for($i=0;$i<13;$i++) $sougou_kei[$i] = $geka_kei[$i] + $naika_kei[$i];
        $contents .= "," ."総合計" .",";
        $contents .= implode($sougou_kei, ",") ."\n";

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
	        $couse="保健室利用者傷病集計";
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
