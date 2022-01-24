<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx120Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    function knjx120Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//登録者コード
    }
    function init()
    {
        $this->field["OUTPUT"] = VARS::post("OUTPUT");        //取り込み種別
        $this->cmd          = VARS::request("cmd");
    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */

	//ＣＳＶへエラー出力
    function getDownloadModel()
    {
		if($this->field["OUTPUT"]=="1")
		{
	        $header = array("YEAR"    			=> "年度",
                    	    "SEMESTER"    		=> "学期",
                    	    "CHAIRCD"    		=> "講座コード",
                    	    "CHAIRNAME"    		=> "講座名称",
                    	    "SUBCLASSCD"    	=> "科目コード",
                    	    "GROUPCD"    		=> "群コード",
                    	    "GROUPSEQ"    		=> "群SEQ",
                    	    "TAKESEMES"    		=> "履修学期",
                    	    "REGISTERCD"    	=> "登録者コード",
                        	"UPDATED" 			=> "更新日付");
		}
		if($this->field["OUTPUT"]=="2")
		{
	        $header = array("YEAR"    			=> "年度",
                    	    "SEMESTER"    		=> "学期",
                    	    "CHAIRCD"    		=> "講座コード",
                    	    "STAFFCD"    		=> "職員コード",
                    	    "CHARGEDIV"    		=> "担任区分",
                    	    "REGISTERCD"    	=> "登録者コード",
                        	"UPDATED" 			=> "更新日付");
		}
		if($this->field["OUTPUT"]=="3")
		{
	        $header = array("YEAR"    			=> "年度",
                    	    "SEMESTER"    		=> "学期",
                    	    "CHAIRCD"    		=> "講座コード",
                    	    "TRGTGRADE"    		=> "対象年",
                    	    "TRGTCLASS"    		=> "対象組",
                    	    "REGISTERCD"    	=> "登録者コード",
                        	"UPDATED" 			=> "更新日付");
		}
		if($this->field["OUTPUT"]=="4")
		{
	        $header = array("YEAR"    			=> "年度",
                    	    "SEMESTER"    		=> "学期",
                    	    "CHAIRCD"    		=> "講座コード",
                    	    "SCHREGNO"    		=> "学籍番号",
                    	    "ROW"    			=> "行",
                    	    "COLUMN"    		=> "列",
                    	    "REGISTERCD"    	=> "登録者コード",
                        	"UPDATED" 			=> "更新日付");
		}
		if($this->field["OUTPUT"]=="5")
		{
	        $header = array("EXECUTEDATE"    	=> "実施日付",
    	                    "PERIODCD"    		=> "校時コード",
        	                "CHAIRCD"    		=> "講座コード",
            	            "EXECUTED"    		=> "実施区分",
            	            "DATADIV"    		=> "データ区分",
                	        "YEAR"    			=> "年度",
                	        "ATTESTOR"    		=> "出欠確認者コード",
                    	    "REGISTERCD"    	=> "登録者コード",
                        	"UPDATED" 			=> "更新日付");
		}
		if($this->field["OUTPUT"]=="6")
		{
	        $header = array("EXECUTEDATE"    	=> "実施日付",
    	                    "PERIODCD"    		=> "校時コード",
        	                "CHAIRCD"    		=> "講座コード",
            	            "FACCD"    			=> "施設コード",
                	        "YEAR"    			=> "年度",
                    	    "REGISTERCD"    	=> "登録者コード",
                        	"UPDATED" 			=> "更新日付");
		}

        $contents  = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjx120Query::selectCsvQuery($this);
        $result = $db->query($query);
        $err_data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
			//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);
	        $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


		if($err_data)
		{
			if($this->field["OUTPUT"]=="1") $couse = "講座データ";
			if($this->field["OUTPUT"]=="2") $couse = "講座担当職員データ";
			if($this->field["OUTPUT"]=="3") $couse = "講座受講クラスデータ";
			if($this->field["OUTPUT"]=="4") $couse = "講座受講生徒データ";
			if($this->field["OUTPUT"]=="5") $couse = "時間割講座データ";
			if($this->field["OUTPUT"]=="6") $couse = "時間割施設データ";
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
            $this->warning = "該当データは、ありません。";
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
//var_dump($_REQUEST);
?>
