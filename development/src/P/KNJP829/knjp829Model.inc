<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp829Model extends Model {
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
    function knjp829Model() {
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

        $this->field = array("HEADER"           =>  VARS::post("HEADER"),
                             "FROM_MONTH"       =>  VARS::post("FROM_MONTH"),
                             "TO_MONTH"         =>  VARS::post("TO_MONTH"),
                             "SEMESTER"         =>  VARS::post("SEMESTER")
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
    }

    function &getMainModel() {
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    function getDownloadModel() {
        //ヘッダー
        $header = array();
        $header["SCHREGNO"]             = '学籍番号';              //学籍番号
        $header["YEAR"]                 = '学費年度';              //年度
        $header["SEMESTER"]             = '学費学期CD';            //入金日の学期
        $header["BUNNOU_NO"]            = '分納No';                //4月1、10月2
        $header["HIMOKU_CD"]            = '奨学費_内訳費目CD';     //授業料：128、入学金：127
        $header["SYOGAKU_MONEY"]        = '奨学費_金額';           //金額
        $header["SYOGAKU_CD"]           = '奨学費_消込対象費目CD'; //授業料：040、入学金：020
        $header["GEN_MONEY"]            = '減額_金額';             //なし
        $header["GEN_CD"]               = '減額_消込対象費目CD';   //なし
        $header["PAY_DATE"]             = '入金日';                //COLLECT_SLIP_PLAN_LIMITDATE_DAT.PAID_LIMIT_DATE
        $header["NOUNYU_CD"]            = '納入方法CD';            //9
        $header["KYOUIKU_CD"]           = '教育ローンCD';          //なし

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjp829Query::selectMainQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row = str_replace(array("\r\n", "\r", "\n"),"↓",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $array = $header;

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    if ($key == "PAY_DATE") {
                        $row[$key] = str_replace("-", "", $row[$key]);
                    }
                    if ($key == "SEMESTER" && $this->field["SEMESTER"]) {
                        $row[$key] = $this->field["SEMESTER"];
                    }
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
            $couse = "奨学費等対象者";
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
