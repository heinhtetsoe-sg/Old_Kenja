<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp986Model extends Model {
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
    function knjp986Model() {
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

        $this->field = array("FROM_DATE"        =>  VARS::post("FROM_DATE"),     //決済期間from
                             "TO_DATE"          =>  VARS::post("TO_DATE"),       //決済期間to
                             "HEADER"           =>  VARS::post("HEADER")         //ヘッダ有
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

    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header = array();
        $header["GROUP_NO"]         = 'グループNo.';
        $header["SCHREGNO"]         = '学籍番号';
        $header["YEAR"]             = '学費年度';
        $header["SEMESTER"]         = '学費学期';
        $header["DIV"]              = '収支区分';
        $header["KIND_CD"]          = '種別';
        $header["DEPOSIT_CD"]       = '内訳費目コード';
        $header["OUTGO_DATE"]       = '支払日';
        $header["OUTGO_MONEY"]      = '利用金額';
        $header["REMARK"]           = '摘要';
        $header["HASUU"]            = '端数金額';

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //学期マスタ取得
        $semArr = array();
        $query  = knjp986Query::getSemesterMst();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semArr[$row["SEMESTER"]] = array("SDATE" => $row["SDATE"],
                                              "EDATE" => $row["EDATE"]);
        }

        //データ
        $query = knjp986Query::selectMainQuery($this);

        //SQL文発行
        if ($query) {
            $befReqestNo = '';
            $groupNo = 0;
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(array("\r\n", "\r", "\n"),"↓",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                //グループNo、端数をセット
                if ($befReqestNo != $row["REQUEST_NO"]) {
                    $groupNo++;
                } else {
                    $row["HASUU"] = '0';// 同伝票番号の最初の人のみ端数セット
                }
                $row["GROUP_NO"] = $groupNo;

                //指定学期セット
                foreach ($semArr as $semster => $dateArr) {
                    if ($dateArr["SDATE"] <= $row["OUTGO_DATE"]) {
                        $row["SEMESTER"] = $semster;
                    }
                }

                //支払日
                $row["OUTGO_DATE"] = str_replace('-', '', $row["OUTGO_DATE"]);

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($header as $key => $headerName) {
                    $contents .= $comma.$row[$key];
                    $comma = ",";
                }
                $contents .= "\n";

                $befReqestNo = $row["REQUEST_NO"];
                $header_data = true;
            }
            $result->free();
        }
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "預り金一括登録";
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
