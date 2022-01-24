<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjp826Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjp826Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("CSV_DIV"          =>  VARS::post("CSV_DIV"),          //CSV区分
                             "DIRECT_DEBIT"     =>  VARS::post("DIRECT_DEBIT"),     //引落日
                             "REDISTER_DISP"    =>  VARS::post("REDISTER_DISP"),    //登録済を表示
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "PLAN_MONTH"       =>  VARS::post("PLAN_MONTH"),            //対象月
                             "COLLECT_L_CD"     =>  VARS::post("COLLECT_L_CD")     //対象科目
                             );

        $dateArray = explode(":", $this->field["DIRECT_DEBIT"]);
        $this->field["PAID_DATE"] = $dateArray[0];
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }

    function &getMainModel() {
    }

    //入力チェック
    function validateInput() {

        return true;
    }

    //Update
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            if ($this->field["CSV_DIV"] == '1' && $this->Properties["use_collect_zengin"] == "1") {
                knjp826Query::getUpdateZenginQuery($db, $this);
            } else {
                knjp826Query::getUpdatePaidPlanQuery($db, $this);
            }
            $db->commit();
            Query::dbCheckIn($db);
        }
        return;
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {

        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();

        $payDivName     =   ($this->field["CSV_DIV"] == '1') ? "引落" : "振込" ;
        $payDivName2    =   ($this->field["CSV_DIV"] == '1') ? "引落先" : "振込人" ;

        $header["SEQ"]                  = '連番';
        $header["ACCOUNTNAME"]          = "{$payDivName2}名（ｶﾅ）";
        $header["ACCOUNTNAME_KANJI"]    = "{$payDivName2}名（漢字）";
        $header["SCHREGNO"]             = '顧客番号';
        $header["PLAN_MONEY"]           = "{$payDivName}金額";
        $header["TOTAL_MONEY"]          = '一括入力金額';
        $header["BANKCD"]               = '銀行コード';
        $header["BANKNAME"]             = '銀行名（ｶﾅ）';
        $header["BANKNAME_KANJI"]       = '銀行名（漢字）';
        $header["BRANCHCD"]             = '支店コード';
        $header["BRANCHNAME"]           = '支店名（ｶﾅ）';
        $header["BRANCHNAME_KANJI"]     = '支店名（漢字）';
        $header["DEPOSIT_DIV"]          = '預金種目';
        $header["ACCOUNTNO"]            = '口座番号';
        $header["RESULT_CD"]            = '処理結果';
        $header["SUMMARY"]              = '摘要内容';
        $header["TOROKUNO"]             = '登録番号';

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        //ヘッダー情報セット
        if ($this->field["HEADER"]=="on") {
            $contents .= "\"振替結果明細表（金額詳細あり）\"\r\n";
            if ($this->field["CSV_DIV"] == '1' && $this->Properties["use_collect_zengin"] == "1") {
                $setDate = substr($this->field["DIRECT_DEBIT"], 0, 2).'月'.substr($this->field["DIRECT_DEBIT"], 2, 2).'日';
            } else {
                $setDate = substr($this->field["DIRECT_DEBIT"], 5, 2).'月'.substr($this->field["DIRECT_DEBIT"], 8, 2).'日';
            }
            $contents .= "\"{$payDivName}日          {$setDate}\"\r\n";

            $schBankMst = array();
            $query = knjp826Query::getCollectSchoolBankMst();
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $schBankMst["ACCOUNTNAME_KANA"] = $row["ACCOUNTNAME_KANA"];
                $schBankMst["ACCOUNT_CD"]       = $row["ACCOUNT_CD"];
                $schBankMst["BANK_CD"]          = $row["BANK_CD"];
                $schBankMst["BANKNAME_KANA"]    = $row["BANKNAME_KANA"];
                $schBankMst["BRANCHCD"]         = $row["BRANCHCD"];
                $schBankMst["BRANCHNAME_KANA"]  = $row["BRANCHNAME_KANA"];
                $schBankMst["DEPOSIT_TYPE"]     = $row["DEPOSIT_TYPE"];
                $schBankMst["ACCOUNTNO"]        = sprintf("%07d", $row["ACCOUNTNO"]);
                $schBankMst["BANKNAME"]         = $row["BANKNAME"];
                $schBankMst["BRANCHNAME"]       = $row["BRANCHNAME"];
            }
            $contents .= "\"会社名・コード  {$schBankMst["ACCOUNTNAME_KANA"]}   {$schBankMst["ACCOUNT_CD"]}\"\r\n\"\"\r\n";
            $contents .= "\"口座情報        {$schBankMst["BANK_CD"]} {$schBankMst["BANKNAME_KANA"]}   {$schBankMst["BRANCHCD"]} {$schBankMst["BRANCHNAME_KANA"]}";
            $contents .= "   {$schBankMst["DEPOSIT_TYPE"]} {$schBankMst["ACCOUNTNO"]}\"\r\n";
            $contents .= "\"        {$schBankMst["BANKNAME"]}   {$schBankMst["BRANCHNAME"]}\"\r\n\"\"\r\n";

            $contents .= "\"".implode($header, "\",\"")."\"\r\n";
        }

        //データ
        if ($this->field["CSV_DIV"] == '1' && $this->Properties["use_collect_zengin"] == "1") {
            $query = knjp826Query::selectQuery($this);

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(array("\r\n", "\r", "\n"),"↓",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $customerHead             = substr($row["CUSTOMER_CD_HEAD"], 0, 10);
                    $customerCd               = str_pad($customerHead.$row["CUSTOMER_SCHREG"], 20, 0, STR_PAD_RIGHT);
                    $row["SCHREGNO"]    = $customerCd;
                    $row["ACCOUNTNAME_KANJI"] = "";
                    $row["BANKNAME_KANJI"]    = "";
                    $row["BRANCHNAME_KANJI"]  = "";
                    $row["SUMMARY"]           = "";
                    $row["TOROKUNO"]          = "";

                    $array = $header;

                    //ヘッダ順に値をセット
                    $comma = "";
                    foreach ($array as $key => $headerName) {
                        if (in_array($key, array("SEQ", "PLAN_MONEY", "TOTAL_MONEY"))) {
                            $contents .= $comma.$row[$key];
                        } else {
                            $contents .= $comma."\"".$row[$key]."\"";
                        }
                        $comma = ",";
                    }
                    $contents .= "\r\n";

                    $header_data = true;
                }
                $result->free();
            }

            // 合計情報セット
            $query = knjp826Query::getTotalInfo($this);
            $totalInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= "\"                  振替予定合計       振替済合計     振替不能合計\"\r\n";
            $contents .= "\"一括入力金額       {$totalInfo["YOTEI_TOTAL"]}円      {$totalInfo["ZUMI_TOTAL"]}円       {$totalInfo["FUNOU_TOTAL"]}円\"\r\n";
            $contents .= "\"【 合計件数 】          {$totalInfo["YOTEI_COUNT"]}件           {$totalInfo["ZUMI_COUNT"]}件            {$totalInfo["FUNOU_COUNT"]}件\"\r\n";
            $contents .= "\"【 合計金額 】     {$totalInfo["YOTEI_TOTAL"]}円      {$totalInfo["ZUMI_TOTAL"]}円       {$totalInfo["FUNOU_TOTAL"]}円\"\r\n";
            $contents .= "\"［ 資金不足 ］        {$totalInfo["FUNOU_COUNT"]}件       {$totalInfo["FUNOU_TOTAL"]}円\"\r\n";
        } else {

            if ($this->cmd == 'csv') {
                knjp826Query::getUpdatePaidPlanQuery($db, $this, "noUpd");
            }

            $totalMoney = 0;
            $totalCnt = 0;
            $moneyStr = ($this->field["COLLECT_L_CD"]) ? "PAIDM_MONEY" : "PLAN_PAID_MONEY" ; //※振込 で科目が指定された場合にのみ明細の金額を表示
            //SQL文発行
            if (get_count($this->csvData) > 0) {
                $setSeq = 1;
                foreach ($this->csvData as $schregNo => $setData) {
                    $contents .= $setSeq.",";
                    $contents .= "\"".$setData["ACCOUNTNAME"]."\",";
                    $contents .= "\"\",";
                    $contents .= "\"".$setData["CUSTOMER_NUMBER"]."\",";
                    $contents .= $setData[$moneyStr].",";
                    $contents .= $setData[$moneyStr].",";
                    $contents .= "\"".$setData["BANKCD"]."\",";
                    $contents .= "\"".$setData["BANKNAME_KANA"]."\",";
                    $contents .= "\"".$setData["BANKNAME"]."\",";
                    $contents .= "\"".$setData["BRANCHCD"]."\",";
                    $contents .= "\"".$setData["BRANCHNAME_KANA"]."\",";
                    $contents .= "\"".$setData["BRANCHNAME"]."\",";
                    $contents .= "\"".$setData["DEPOSIT_TYPE"]."\",";
                    $contents .= "\"".sprintf("%07d", $setData["ACCOUNTNO"])."\",";
                    $contents .= "\"振替済\",";
                    $contents .= "\"\",";
                    $contents .= "\"\"";
                    $contents .= "\r\n";

                    $setSeq++;
                    $totalMoney += $setData[$moneyStr];
                    $totalCnt++;
                    $header_data = true;
                }
            }

            // 合計情報セット
            $totalInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= "\"                  振替予定合計       振替済合計     振替不能合計\"\r\n";
            $contents .= "\"一括入力金額       {$totalMoney}円      {$totalMoney}円       0円\"\r\n";
            $contents .= "\"【 合計件数 】          {$totalCnt}件           {$totalCnt}件            0件\"\r\n";
            $contents .= "\"【 合計金額 】     {$totalMoney}円      {$totalMoney}円       0円\"\r\n";
            $contents .= "\"［ 資金不足 ］        0件       0円\"\r\n";
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "預金口座振替情報";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }
}
?>
