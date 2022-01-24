<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp828Model extends Model {
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
    function knjp828Model() {
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

        $this->field = array("DIRECT_DEBIT"     =>  VARS::post("DIRECT_DEBIT"),     //引落日
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

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
            knjp828Query::getUpdateQuery($this);
        }
        return;
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        $header["SEQ"]                  = '連番';
        $header["ACCOUNTNAME"]          = '引落先名（ｶﾅ）';
        $header["ACCOUNTNAME_KANJI"]    = '引落先名（漢字）';
        $header["SCHREGNO"]             = '学籍番号';
        $header["PLAN_MONEY"]           = '引落金額';
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

        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjp828Query::selectMainQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(array("\r\n", "\r", "\n"),"↓",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $array = $header;

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    $contents .= $comma.$row[$key];
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }
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
