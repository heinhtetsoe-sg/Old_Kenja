<?php

require_once('for_php7.php');

function getmicrotime(){ 
   list($usec, $sec) = explode(" ",microtime()); 
   return ((float)$sec + (float)$usec); 
} 

class knjp111kModel extends Model {
    var $cmd;
    var $file;
    var $applicationcd;
    var $jhflg;	//NO001
    var $nocnt;	//NO001
    
    function knjp111kModel()
    {
        common::GetControlMaster_Fnc($this->controls);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
        }

        $this->cmd          = VARS::request("cmd");
        $this->file         = VARS::file("FILE");
        if (VARS::post("APPLICATIONCD")){
            $this->applicationcd   = VARS::post("APPLICATIONCD");
        }

		$this->jhflg = VARS::request("JHFLG");		//NO001
		if (VARS::request("NOCNT")){
			$this->nocnt = VARS::request("NOCNT");	//NO001
		}else {
			$this->nocnt = 0;	//NO001
		}
    }
    //申込入金データ取込処理
    function &getInsertModel()
    {
        if (is_uploaded_file($this->file["tmp_name"])){
            $db = Query::dbCheckOut();
            //年号の最初の年を取得
            $year = $db->getOne(knjp111kQuery::selectQueryYear());        

            $db->autoCommit(false);
            $fp = fopen ($this->file["tmp_name"], "r");
            $db->expectError('*');
            $buffer = "";
            while (!feof ($fp))
            {
                //文字コードを判別、改行コードを削除
                $buffer = str_replace("\r","",$buffer);
                $buffer = str_replace("\n","",$buffer);
                $buffer .= fgets($fp, 4096);
            }
            $buffer =  mb_convert_encoding($buffer, i18n_http_output(), "SJIS-win");
            //エラー、一時テーブル削除
            $db->query(knjp111kQuery::DeleteQuery("APPL_RESULT_ERR_DATA"));        
            $db->query(knjp111kQuery::DeleteQuery("APPL_RESULT_TMP_DATA"));        
            $d["FILE_LINE_NUMBER"] = 1;
            for($i = 0; $i < mbstrlen($buffer); $i+=200){
                if (mbsubstr($buffer, $i, 1) == "1"){ //データ区分（ヘッダ部）
                    $d = array();

                    #1振込入金通知レコード・フォーマット 2005/06/24
                    $d["MAKE_DATE"]     = $this->chgDate($year, mbsubstr($buffer, $i+4, 6));      //作成日
                    $d["BANKCD"]        = mbsubstr($buffer, $i+22, 4);     //銀行コード
                    $d["BRANCHCD"]      = mbsubstr($buffer, $i+41, 3);     //支店コード
                    $d["DEPOSIT_ITEM"]  = mbsubstr($buffer, $i+59, 1);     //預金種目
                    $d["ACCOUNTNO"]     = mbsubstr($buffer, $i+60, 7);     //口座番号
                    $d["ACCOUNTNAME"]   = mb_convert_kana(trim(mbsubstr($buffer, $i+73, 40)),"KV","utf-8");    //口座名
                    $d["FILE_LINE_NUMBER"]++;
                }else if (mbsubstr($buffer, $i, 1) == "2"){ //データ区分（データ部）
                    $d["APPLI_PAID_DATE"]   = $this->chgDate($year, mbsubstr($buffer, $i+7, 6));      //入金日
                    $d["INOUT_FG"]      = "0";      //入払区分
                    $d["JOB_FG"]        = "00";      //取引区分
                    $d["APPLI_PAID_MONEY"]  = (int) mbsubstr($buffer, $i+19, 10);      //入金額
                    $d["REFERENCE_NUMBER"]  = mbsubstr($buffer, $i+39, 10);      //整理番号
                    $d["CLIENT_NAME"]       = mb_convert_kana(trim(mbsubstr($buffer, $i+49, 48)),"HVS","utf-8"); //振込依頼人名
                    $d["CLIENT_NAME_ORG"]       = trim(mbsubstr($buffer, $i+49, 48)); //振込依頼人名

                    #2.入出金取引明細レコード・フォーマット
#                    $d["MAKE_DATE"]     = $this->chgDate($year, mbsubstr($buffer, $i+4, 6));      //作成日
#                    $d["BANKCD"]        = mbsubstr($buffer, $i+22, 4);     //銀行コード
#                    $d["BRANCHCD"]      = mbsubstr($buffer, $i+41, 3);     //支店コード
#                    $d["DEPOSIT_ITEM"]  = mbsubstr($buffer, $i+62, 1);     //預金種目
#                    $d["ACCOUNTNO"]     = mbsubstr($buffer, $i+63, 10);    //口座番号
#                    $d["ACCOUNTNAME"]   = mb_convert_kana(trim(mbsubstr($buffer, $i+73, 40)),"KV","utf-8");    //口座名
#                    $d["FILE_LINE_NUMBER"]++;
#                }else if (mbsubstr($buffer, $i, 1) == "2"){ //データ区分（データ部）
#                    $d["APPLI_PAID_DATE"]   = $this->chgDate($year, mbsubstr($buffer, $i+15, 6));      //入金日
#                    $d["INOUT_FG"]      = mbsubstr($buffer, $i+21, 1);      //入払区分
#                    $d["JOB_FG"]        = mbsubstr($buffer, $i+22, 2);      //取引区分
#                    $d["APPLI_PAID_MONEY"]  = (int) mbsubstr($buffer, $i+24, 12);      //入金額
#                    $d["REFERENCE_NUMBER"]  = mbsubstr($buffer, $i+71, 10);      //整理番号
#                    $d["CLIENT_NAME"]       = mb_convert_kana(trim(mbsubstr($buffer, $i+81, 48)),"HVS","utf-8"); //振込依頼人名
#                    $d["CLIENT_NAME_ORG"]       = trim(mbsubstr($buffer, $i+81, 48)); //振込依頼人名


                    //全角スペースを削除
//                    $d["CLIENT_NAME"]       = str_replace("　","",$d["CLIENT_NAME"]);
					//NO001-->
					if ( ($this->jhflg == "1" && substr($d["REFERENCE_NUMBER"],2,2) == "61") OR 
						 ($this->jhflg == "2" && substr($d["REFERENCE_NUMBER"],2,2) != "61") ){
	                    $query = knjp111kQuery::InsertQuery($d);
    	                $result = $db->query($query);
        	            //エラー処理
            	        if (PEAR::isError($result)){
                	        //数値項目エラー
                    	    if ($result->getCode() == DB_ERROR_INVALID_NUMBER){
                        	    $err = "MSG907 入力された値は不正です。「数字」を入力してください。";
                            	$col = "APPLI_PAID_MONEY";
	                        }else{
    	                        $err = "MSG902 入力された値は不正です。「日付」を入力してください。";
        	                    if (!$this->chkDate($d["MAKE_DATE"])){
            	                    $col = "MAKE_DATE";
                	            }else if (!$this->chkDate($d["APPLI_PAID_DATE"])){
                    	            $col = "APPLI_PAID_DATE";
                        	    }
	                        }
    	                    $err_data = array("PROCESSCD"           => "1",
        	                                  "FILE_LINE_NUMBER"    => $d["FILE_LINE_NUMBER"],
            	                              "COLNAME"             => $col,
                	                          "ERR_LEVEL"           => "8",
                    	                      "ERR_MSG"             => $err);
                        	//エラー処理登録
	                        $db->query(knjp111kQuery::ErrDataInsert($err_data));
    	                }
        	            $d["FILE_LINE_NUMBER"]++;
					}else {
						$this->nocnt++;
					}
					//NO001<--
                }else if (mbsubstr($buffer, $i, 1) == "8"){ //データ区分（トレーラ部）
                    $d["FILE_LINE_NUMBER"]++;
                }else if (mbsubstr($buffer, $i, 1) == "9"){ //データ区分（エンド部）
                }
            }

            $db->popExpect();
            $db->commit();

            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
    //和暦ー＞西暦変換
    function chgDate($year, $date){
        $y = (int) substr($date, 0, 2);
        $m = (int) substr($date, 2, 2);
        $d = (int) substr($date, 4, 2);
        
        $y = $year+$y-1;
        return sprintf("%04d-%02d-%02d", $y,$m,$d);
    }
    //日付チェック
    function chkDate($date){
        $y = (int) substr($date, 0, 4);
        $m = (int) substr($date, 5, 2);
        $d = (int) substr($date, 8, 2);
        return checkdate ($m, $d, $y);
    }
    //銀行入金データ消込処理
    function &getUpdateModel2()
    {
        set_time_limit(0);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //エラーテーブルの処理区分が消込処理のものを削除
        $db->query(knjp111kQuery::ErrDataDelete("2"));

        $err_data = array("PROCESSCD"           => "2",
                          "FILE_LINE_NUMBER"    => 0,
                          "COLNAME"             => 'APPLICATIONCD',
                          "ERR_LEVEL"           => "0",
                          "ERR_MSG"             => $this->applicationcd
                          );
        //エラー処理登録
        $db->query(knjp111kQuery::ErrDataInsert($err_data));
        //振込銀行情報チェック
//            knjp111kQuery::InsertBankcdErrors($db, $this);
        //データOK NO004
        knjp111kQuery::InsertOk($db, $this);
        //整理番号エラーチェック
        knjp111kQuery::InsertReference_NumberErrors($db, $this);
        //振込依頼人名エラーチェック
        knjp111kQuery::InsertClientNameErrors($db, $this);
        //入金済エラーチェック
        knjp111kQuery::InsertAppli_Paid_MoneyErrors($db, $this);
        //振込依頼人名+整理番号エラーチェック NO004
        knjp111kQuery::InsertClientReferenceErrors($db, $this);
        //入金額エラーチェック
        knjp111kQuery::InsertAppli_Paid_MoneyErrors2($db, $this);
        //消込処理
        knjp111kQuery::UpdateAppricationDat($db, $this);
        //正常データ削除
        $db->query(knjp111kQuery::ErrDataDelete2("2", "OK"));
        //一時保管データの取引情報を更新
        knjp111kQuery::UpdateStatus($db, $this);

        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);


    }

}
?>
