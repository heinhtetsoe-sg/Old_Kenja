<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl999Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    function knjl999Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
		$this->staffcd = STAFFCD;							//登録者コード
    }
    function init()
    {
		$this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),			//出力取込種別
        					 "STAFFCD"			=>  VARS::post("STAFFCD"),			//職員コード
        					 "HEADER"			=>  VARS::post("HEADER"),			//ヘッダ有
        					 "SHORI_MEI"		=>  VARS::post("SHORI_MEI")			//処理名
                     		);

        $this->cmd          = VARS::request("cmd");
        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }
    function &getMainModel()
    {
    }

    /**
     * データ取込
    */
    function &getExecModel()
    {
		//更新
        $this->fileUpload();
    }

	//更新(データ取込)
    function fileUpload()
    {
        if (is_uploaded_file ($this->field["FILE"]["tmp_name"])){		//HTTP POSTによりアップロードされたファイルかどうかを調べる

			$db = Query::dbCheckOut();
			$db->autoCommit(false);

			//エラーデータの削除
           	$query = knjl999Query::deleteQueryErr();
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

                    $data = array("ENTEXAMYEAR"    		=> trim($tmp[0]),
                                  "APPLICANTDIV"  		=> trim($tmp[1]),
                                  "APPLICANTNAME"   	=> trim($tmp[2]),
                                  "DESIREDIV" 			=> trim($tmp[3]),
                                  "DESIRENAME" 			=> trim($tmp[4]),
                                  "EXAMNO"       		=> trim($tmp[5]),
                                  "NAME"       			=> trim($tmp[6]),
                                  "NAME_KANA"     		=> trim($tmp[7]),
                                  "SEX"   				=> trim($tmp[8]),
                                  "SEXNAME"       		=> trim($tmp[9]),
                                  "FS_CD"   			=> trim($tmp[10]),
                                  "FINSCHOOL_NAME"		=> trim($tmp[11]),
                                  "WISHNO"    			=> trim($tmp[12]),
                                  "WISHNAME"    		=> trim($tmp[13]));

					$koumoku_error = $sonzai_error = "";	//エラー出力用

		            //項目チェック
					$koumoku_error = $this->koumoku_check($data);

		            //取り込み処理（エラーの場合、エラーＤＢへの追加）
					if($koumoku_error!="" || $sonzai_error!="")
					{
				        $check_error = "";
				        $check_error = $koumoku_error .$sonzai_error;
                   		knjl999Query::insertQueryErr($db, $record_no, $check_error);
                    } else {
						$table = "FINSCHOOL_MST";
						$where = "where FINSCHOOL_NAME = '".$data["FINSCHOOL_NAME"]."'";
						$query = knjl999Query::getMasterSQL($table,$where);
						$result = $db->query($query);
						$flg = false;
						while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
							$flg = true;
						}
						if(!$flg){	//ＤＢになければ、insert
							$query = knjl999Query::getMaxSQL($table);
							$result = $db->query($query);
							while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
								$data["FS_CD"] = $row["FS_CD"];
							}
							$data["FS_CD"] 		= $this->zero_ume($data["FS_CD"],7);
                   			knjl999Query::insertQueryCsv($db, $data, $this->staffcd);
						}
                    }//$ifの綴り
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


	//ＣＳＶへエラー出力
    function getDownloadModel()
    {
        $header = array("MSGROW"    		=> "行番号",
                        "MSGREMARK" 		=> "メッセージ");

        $contents  = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjl999Query::selectCsvQuery();
        $result = $db->query($query);
        $err_data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	        $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


		if($err_data)
		{
			$couse = "エラー出力";
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

    }

    /**
     * 項目チェック
     */
    function koumoku_check($data)
    {
		$koumoku_error = "";		//項目エラー用
		//桁数チェック
		if (strlen($data["FS_CD"]) > 7 || 
			strlen($data["FINSCHOOL_NAME"]) > 45)
		{
				$koumoku_error .= "桁数エラー。";
		}
		//必須チェック
		if ($data["FINSCHOOL_NAME"]=="")
		{
				$koumoku_error .= "必須項目エラー。";
		}
		//数字チェック
		if (!$this->suuji_check($data["FS_CD"]))
		{
			   	$koumoku_error .= "数字項目エラー。";
		}

		return $koumoku_error;	//項目エラー用
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

	//値チェック
    function atai_check($data,$atai)
    {
		$flg = true;

		if ( $data!="" )
		{
			if ( !in_array($data,$atai) ) $flg = false;
		}

		return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
//		if ($data!="")
//		{
			$data = common::DateConv1(str_replace("-","/",$data),4);
			if($data) $data = str_replace("/","-",$data);
//		}

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
			$query = knjl999Query::getMasterSQL($table,$where);
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
