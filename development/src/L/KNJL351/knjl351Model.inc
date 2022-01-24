<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl351Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl351Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        $this->field = array("OUTPUT"   		=>  VARS::post("OUTPUT")    		//印刷対象
                     );

        $this->cmd         = VARS::request("cmd");
    }
	/**
	 * ＣＳＶ出力
	 */
    function getDownloadModel()
    {
        //CSVデータ抽出処理
        //DB接続
        $db = Query::dbCheckOut();

        //入試区分
        $testdivArray = array();
        $result = $db->query(knjl351Query::getTestdivMst($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivArray[] = $row["TESTDIV"];
        }
        $result->free();

        $contents = "";	//CSVデータ出力用
        $data = false;
        $out_flg = $this->field["OUTPUT"];
		$nendo = $this->ObjYear."/04/01";
		$nendo = common::DateConv1($nendo,10);								//年度
		$ctrl_date = common::DateConv1(str_replace("-","/",CTRL_DATE),0);	//作成日
		$title = $nendo."入学試験成績資料";
		$contents .= $nendo." 入学試験成績資料(総合)"."\r\n";
		$contents .= "作成日 ： ".$ctrl_date."\r\n";

        $field = ($this->field["OUTPUT"] == 2) ? "２" : "４";

        $contents .= "受験番号,氏名,氏名かな,性別,地区コード,出身校,";
        foreach ($testdivArray as $num => $testdiv) {
            $kai = $num + 1;
            $contents .= $kai ."回目合否"   .",";
            $contents .= $kai ."回目合計" .",";
        }
        $contents .= "成績(合計平均),受験,加点,合否,特別アップ,手続,入学,備考１"."\r\n";
        //CSVデータ設定
		$result = $db->query(knjl351Query::getcsvdata($this));
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
       	    $row = str_replace(",",".",$row);
			$contents .= $row["EXAMNO"].",";
			$contents .= $row["NAME"].",";
			$contents .= $row["NAME_KANA"].",";
			$contents .= $row["SEX"].",";
            $contents .= $row["FS_AREA_CD"].",";
			$contents .= $row["FS_NAME"].",";
            foreach ($testdivArray as $num => $testdiv) {
                $contents .= $row["GOUHI" . $testdiv] .",";
                $contents .= $row["FOUR"  . $testdiv] .",";
            }
			$contents .= $row["REC_AVG"].",";
			$contents .= $row["RECEPT"].",";
			$contents .= $row["KATEN"].",";
			$contents .= $row["GOUHI"].",";
			$contents .= $row["SPECIAL_MEASURES3"].",";
			$contents .= $row["PROCEDUREDIV"].",";
			$contents .= $row["ENTDIV"].",";
            if ($row["REMARK2"] != "") $contents .= $row["REMARK2"];//NO001
            if ($row["REMARK1"] != "" && $row["REMARK2"] != "") $contents .= "、";//NO001
            if ($row["REMARK1"] != "") $contents .= $row["REMARK1"];//NO001
			$contents .= ",";//NO001
//			$contents .= $row["REMARK1"].",";
   	        $contents .= "\r\n";
			if($contents!="") $data = true;
		}
		$result->free();

        //DB切断
   	    Query::dbCheckIn($db);

        if ($data){
	        $couse = $nendo."入学試験成績資料";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
