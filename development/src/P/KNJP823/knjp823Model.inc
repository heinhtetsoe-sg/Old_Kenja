<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp823Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjp823Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
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

        $this->field = array("SELYEAR"     =>  VARS::post("SELYEAR") ? VARS::post("SELYEAR") : CTRL_YEAR,     //年度
                             "SELSCHKIND"  =>  VARS::post("SELSCHKIND"),  //校種
                             "SELGRADE"    =>  VARS::post("SELGRADE"),    //学年
                             "HEADER"      =>  VARS::post("HEADER")       //ヘッダ有
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

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        $header["SCHREGNO"]             = '学籍番号';
        $header["SGL_SCHOLARSHIP_CD"]   = '減免分類CD';
        $header["SGL_SCHOLARSHIP_DIV"]  = '減免区分';

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //減免区分複数登録チェック
        $setWarnigMsg = $sep = '';
        $query = knjp823Query::selectCheckQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($row['CNT'] > 1) {
                $setWarnigMsg .= $sep.$row['HR_NAME_ATTNO'].'　'.$row['NAME'];
                $sep = '\n';
            }
        }
        if ($setWarnigMsg != '') {
            $this->warning = '減免区分を複数登録している生徒がいます。\n'.$setWarnigMsg;
            return false;
        }

        //データ
        $query = knjp823Query::selectQuery($this);

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
            $couse = "学籍情報一括登録・減免区分情報";
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
