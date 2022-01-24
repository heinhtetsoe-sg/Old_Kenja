<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx030bModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjx030bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

		$this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),			//出力取込種別
        					 "HEADER"			=>  VARS::post("HEADER"),			//ヘッダ有
        					 "SHORI_MEI"		=>  VARS::post("SHORI_MEI")			//処理名
                     		);

        $this->cmd          = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
		//データ取込
   	    $this->fileUpload();
    }

	//取込
    function fileUpload()
    {
        if (is_uploaded_file ($this->field["FILE"]["tmp_name"])){		//HTTP POSTによりアップロードされたファイルかどうかを調べる

			$db = Query::dbCheckOut();
			$db->autoCommit(false);

			//エラーデータの削除
           	$query = knjx030bQuery::deleteQueryErr();
	    	$result = $db->query($query);

			$record_no = 1;			//行番号(初期値)

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");		//ファイルまたはURLをオープンする
            while (!feof ($fp)) {										//ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);								//ファイルポインタから1行取得する
                if (trim($buffer) != ''){
					//１万件を超えたらループから抜ける。
    	    		if( $record_no > 10000 ) break;
					//ヘッダ有なら１行目は無視
    	    		if( $this->field["HEADER"]=="on" && $record_no<2 ){
						$record_no++;
						continue;
					}
                    $buffer = i18n_convert($buffer,"UTF-8","SJIS-win");		//文字列を指定コードに変換する

//2004/03/26 nakamoto del	$tmp = explode("\t", $buffer);
                    $tmp = explode(",", $buffer);		//2004/03/26 nakamoto add

                    $data = array("SCHREGNO"    	=> trim($tmp[0]),
                                  "ISSUEDATE"  		=> trim($tmp[1]),
                                  "EXPIREDATE"  	=> trim($tmp[2]),
                                  "ZIPCD" 			=> trim($tmp[3]),
                                  "AREACD" 			=> trim($tmp[4]),
                                  "ADDR1"   		=> trim($tmp[5]),
                                  "ADDR2"       	=> trim($tmp[6]),
                                  "ADDR1_ENG"    	=> trim($tmp[7]),
                                  "ADDR2_ENG"    	=> trim($tmp[8]),
                                  "TELNO"    		=> trim($tmp[9]),
                                  "FAXNO" 			=> trim($tmp[10]),
                                  "EMAIL"    		=> trim($tmp[11]));
//                                  "EMERGENCYCALL"   => trim($tmp[12]),   2005.10.18 m-yama
//                                  "EMERGENCYTELNO"  => trim($tmp[13]));  2005.10.18 m-yama

					$koumoku_error = $sonzai_error = "";	//エラー出力用

		            //項目チェック
					$koumoku_flg = true;
		//更新
		if($this->field["SHORI_MEI"]=="1")
		{
					//桁数チェック
					if (strlen($data["SCHREGNO"]) > 8 || 
						strlen($data["ISSUEDATE"]) > 10 || 
						strlen($data["EXPIREDATE"]) > 10 || 
						strlen($data["ZIPCD"]) > 8 || 
						strlen($data["AREACD"]) > 2 || 
						strlen($data["ADDR1"]) > 75 || 
						strlen($data["ADDR2"]) > 75 || 
						strlen($data["ADDR1_ENG"]) > 50 || 
						strlen($data["ADDR2_ENG"]) > 50 || 
						strlen($data["TELNO"]) > 14 || 
						strlen($data["FAXNO"]) > 14 || 
						strlen($data["EMAIL"]) > 20)
//						strlen($data["EMERGENCYCALL"]) > 60 ||  2005.10.18 m-yama
//						strlen($data["EMERGENCYTELNO"]) > 14)   2005.10.18 m-yama
					{
						$koumoku_flg = false;
				        $koumoku_error .= "桁数エラー。";
					}
					//必須チェック
					if ($data["SCHREGNO"]=="" || $data["ISSUEDATE"]=="" || $data["ADDR1"]=="")
					{
						$koumoku_flg = false;
				        $koumoku_error .= "必須項目エラー。";
					}
					//数字チェック
					if ( !$this->suuji_check($data["AREACD"]) )
					{
						$koumoku_flg = false;
			        	$koumoku_error .= "数字項目エラー。";
					}
		//削除
		} else {
					//桁数チェック
					if ( strlen($data["SCHREGNO"]) > 8 || strlen($data["ISSUEDATE"]) > 10 )
					{
						$koumoku_flg = false;
				        $koumoku_error .= "桁数エラー。";
					}
					//必須チェック
					if ( $data["SCHREGNO"]=="" || $data["ISSUEDATE"]=="" )
					{
						$koumoku_flg = false;
				        $koumoku_error .= "必須項目エラー。";
					}
		}

		            //存在チェック(項目チェックがＯＫの場合)
					if($koumoku_flg)
					{
			            //日付変換
					if ($data["ISSUEDATE"]!="")
					{
						$data_check = $this->date_rep($data["ISSUEDATE"]);
						if($data_check)
						{
							$data["ISSUEDATE"] = $data_check;
						} else {
					        $sonzai_error = "日付エラー。";
						}
					}
		//更新
		if($this->field["SHORI_MEI"]=="1")
		{
					if ($data["EXPIREDATE"]!="")
					{
						$data_check = $this->date_rep($data["EXPIREDATE"]);
						if($data_check)
						{
							$data["EXPIREDATE"] = $data_check;
						} else {
					        $sonzai_error = "日付エラー。";
						}
					}
		}
			            //０埋め
						$data["SCHREGNO"] 		= $this->zero_ume($data["SCHREGNO"],8);
						$data["AREACD"] 		= $this->zero_ume($data["AREACD"],2);
		//更新
		if($this->field["SHORI_MEI"]=="1")
		{
			            //基礎マスタ
						$table = "schreg_base_mst";
						$where = "where SCHREGNO = '".$data["SCHREGNO"]."'";
						$sonzai_flg = $this->sonzai_check($db,$data["SCHREGNO"],$table,$where);
						if(!$sonzai_flg) $sonzai_error .= "基礎マスタなし。";
			            //名称マスタ
						$table = "name_mst";
						$where = "where NAMECD1 = 'A020' AND NAMECD2 = '".$data["AREACD"]."'";
						$sonzai_flg = $this->sonzai_check($db,$data["AREACD"],$table,$where);
						if(!$sonzai_flg) $sonzai_error .= "（地区コード）名称マスタなし。";
		}
					}//$koumoku_flgの綴り

		            //取り込み処理（エラーの場合、エラーＤＢへの追加）
					if($koumoku_error!="" || $sonzai_error!="")
					{
				        $check_error = "";
				        $check_error = $koumoku_error .$sonzai_error;
                   		knjx030bQuery::insertQueryErr($db, $record_no, $check_error);
                    } else {
						$table = "schreg_address_dat";
						$where = "where SCHREGNO = '".$data["SCHREGNO"]."' AND ISSUEDATE = '".$data["ISSUEDATE"]."'";
						$query = knjx030bQuery::getMasterCheck($table,$where);
						$result = $db->query($query);
						$flg = false;
						while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
							$flg = true;
						}
		//更新
		if($this->field["SHORI_MEI"]=="1")
		{
						if($flg){	//ＤＢにあれば、update
           	       			knjx030bQuery::updateQueryCsv($db, $data, $this->staffcd);
						} else {	//ＤＢになければ、insert
                   			knjx030bQuery::insertQueryCsv($db, $data, $this->staffcd);
						}
		//削除
		} else {
						//ＤＢにあれば、delete
						if($flg) knjx030bQuery::deleteQueryCsv($db, $data);
		}
                    }//ifの綴り
                }//ifの綴り
				$record_no++;	//行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            $this->message = "CSV登録に成功しました。";
			//１万件を超えたら強制終了する。
    		if( $record_no > 10000 ) $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
        }else{
            $this->warning = "ファイル名が不正です。";
        }

    }


	//ＣＳＶ出力
    function getDownloadModel()
    {
		//項目
        $header_err = array("MSGROW"    		=> "行番号",
                        	"MSGREMARK" 		=> "メッセージ");
		$header = array("SCHREGNO"    	=> "※学籍番号",
                        "ISSUEDATE"  	=> "※有効期間開始年月日",
                        "EXPIREDATE"  	=> "有効期間終了年月日",
                        "ZIPCD" 		=> "郵便番号",
                        "AREACD" 		=> "地区コード",
                        "ADDR1"   		=> "※住所1",
                        "ADDR2"       	=> "住所2",
                        "ADDR1_ENG"    	=> "住所1英字",
                        "ADDR2_ENG"    	=> "住所2英字",
                        "TELNO"    		=> "電話番号",
                        "FAXNO" 		=> "FAX番号",
                        "EMAIL"    		=> "E-Mail",
//                        "EMERGENCYCALL"   => "急用連絡先",   2005.10.18 m-yama
//                        "EMERGENCYTELNO"  => "急用電話番号", 2005.10.18 m-yama
                        "DUMMY"       		=> "DUMMY");//2005.09.28nakamoto
		$header_rei = array("SCHREGNO"    	=> "12345678",
                        	"ISSUEDATE"  	=> "2004-04-04",
                        	"EXPIREDATE"  	=> "2007-03-31",
                        	"ZIPCD" 		=> "900-0032",
                        	"AREACD" 		=> "10",
                        	"ADDR1"   		=> "沖縄県那覇市松山 1-1-14",
                        	"ADDR2"       	=> "那覇共同ビル",
                        	"ADDR1_ENG"    	=> "nahashi matsuyama",
                        	"ADDR2_ENG"    	=> "nahakyoudou",
                        	"TELNO"    		=> "098-869-1781",
                        	"FAXNO" 		=> "098-869-1785",
                        	"EMAIL"    		=> "aaa123@bbb.co.jp",
//                        	"EMERGENCYCALL"   => "アルプ商会",   2005.10.18 m-yama
//                        	"EMERGENCYTELNO"  => "098-869-1788", 2005.10.18 m-yama
                        "DUMMY"       		=> "DUMMY");//2005.09.28nakamoto

        $contents  = "";	//クリア

		if($this->field["OUTPUT"]=="1")				//ヘッダ
		{
	        $contents .= implode($header, ",")."\n";
	        $contents .= implode($header_rei, ",")."\n";
		} else if($this->field["OUTPUT"]=="3"){		//エラー
	        $contents .= implode($header_err, ",")."\n";
		} else if($this->field["OUTPUT"]=="4"){		//データ
	        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
		}
		if($this->field["OUTPUT"]=="1")			//ヘッダ
		{
	        $header_data = true;
		} else {								//ヘッダ以外
	        $header_data = false;
	        //DB接続
	        $db = Query::dbCheckOut();
			if($this->field["OUTPUT"]=="3")				//エラー
			{
				$table = "w_csvmsg_dat";
				$where = "";
			} else if($this->field["OUTPUT"]=="4"){		//データ
				$table = "schreg_address_dat";
				$where = "ORDER BY SCHREGNO,ISSUEDATE";
			}
	        //SQL文発行
			$query = knjx030bQuery::getMasterSQL($table,$where);
	        $result = $db->query($query);
	        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
				if($this->field["OUTPUT"]=="4"){				//データ
		            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
					$row["REGISTERCD"] = $row["UPDATED"] = "";
        			$row["REGISTERCD"] = "DUMMY";//2005.09.28nakamoto
				}
		        $contents .= implode($row, ",") ."\n";
	            $header_data = true;
	        }
	        $result->free();
	        Query::dbCheckIn($db);
		}

		//ＣＳＶ出力
		if($header_data)
		{
			if($this->field["OUTPUT"]=="3"){		//エラー
				$couse = "エラー出力";
			} else {								//エラー以外
				$couse = "学籍住所データ";
			}
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
            $this->warning = "該当データは、ありません。";
            return false;
		}

/* 今後のため、保存（カンマ対策用）
		$kanma_check = array("SCHREGNO","ISSUEDATE","EXPIREDATE","ZIPCD","AREACD","ADDR1","ADDR2","ADDR1_ENG","ADDR2_ENG","TELNO","FAXNO","EMAIL","EMERGENCYCALL","EMERGENCYTELNO");
            for($i = 0; $i < get_count($kanma_check); $i++){
				if ( strpos($row[$kanma_check[$i]], ",") )
    	    		$row[$kanma_check[$i]] = "\"" .str_replace('"', '""', $row[$kanma_check[$i]]) ."\"";
			}	*/

    }

	//数字チェック
    function suuji_check($data)
    {
		$flg = true;

		if ( $data!="" )
		{
			if ( !is_numeric($data) ) $flg = false;
		}

		return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
		if ($data!="")
		{
			$data = common::DateConv1(str_replace("-","/",$data),4);
			if($data) $data = str_replace("/","-",$data);
		}

		return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    function zero_ume($data,$ketasuu)
    {
		$shosiki = "%0".$ketasuu."d";
		if ($data!="")
		{
			if (strlen($data) < $ketasuu) $data = sprintf($shosiki, $data);
		}

		return $data;
    }

    /**
     * マスタの存在チェック
     */
    function sonzai_check(&$db,$data,$table,$where)
    {
		$sonzai_flg = true;
		if( $data!="" ){
			$sonzai_flg = false;
			$query = knjx030bQuery::getMasterCheck($table,$where);
			$result = $db->query($query);
			while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
				$sonzai_flg = true;
			}
		}
		return $sonzai_flg;
    }

}
//var_dump($_REQUEST);
?>
