<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp822Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    public function knjp822Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
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

    public function &getMainModel()
    {
    }

    //入力チェック
    public function validateInput()
    {
        return true;
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        $header["SIGEL_NO"]             = '生徒個人番号';
        $header["SCHREGNO_SUB"]         = '学籍番号';
        $header["GUARANTOR_TYPE"]       = '保証人種別';
        $header["GUARANTOR_PRVNO"]      = '保証人個人番号';
        $header["SEI"]                  = '姓';
        $header["MEI"]                  = '名';
        $header["KANA_SEI"]             = '姓（カナ）';
        $header["KANA_MEI"]             = '名（カナ）';
        $header["ENG_SEI"]              = '姓（英語）';
        $header["ENG_MEI"]              = '名（英語）';
        $header["SEX"]                  = '性別';
        $header["BIRTHDAY"]             = '生年月日';
        $header["NENREI"]               = '5月1日年齢';
        $header["HANDICAP_TYPE"]        = '身障者区分';
        $header["COUNTRY_CD"]           = '国籍CD';
        $header["COME_FROM_PREF"]       = '出身都道府県CD';
        $header["REGION"]               = '地域';
        $header["CHG_CD"]               = '変更CD';
        $header["ZIP_NO"]               = '郵便番号';
        $header["PREF_CD"]              = '都道府県CD';
        $header["ADDRESS1"]             = '住所１';
        $header["ADDRESS2"]             = '住所２';
        $header["ADDRESS3"]             = '住所３';
        $header["TELNO"]                = '電話番号';
        $header["MOBILE_TELNO"]         = '携帯番号';
        $header["FAXNO1"]               = 'FAX番号1';
        $header["FAXNO2"]               = 'FAX番号2';
        $header["MAILADDRESS1"]         = 'メールアドレス1';
        $header["MAILADDRESS2"]         = 'メールアドレス2';
        $header["PHOTO"]                = '写真';
        $header["REMARK"]               = '備考';
        $header["VER_NO"]               = 'バージョン番号';

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        if ($this->field["HEADER"]=="on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjp822Query::selectQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(array("\r\n", "\r", "\n"), "↓", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $array = $header;
                $row["GNAME"] = str_replace(" ", "　", $row["GNAME"]);
                $row["GKANA"] = str_replace(" ", "　", $row["GKANA"]);

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    if (array_key_exists($key, $row)) {
                        if ($key === "ZIP_NO") {
                            $row[$key] = str_replace("-", "", $row[$key]);
                        } elseif ($key === "ADDRESS1") {
                            $row[$key] = str_replace($row["PREF_NAME"], "", $row[$key]);
                        }
                        $contents .= $comma.$row[$key];
                    } else {
                        if ($key === "SEI") {
                            $cutwk = explode("　", $row["GNAME"]);
                            $setSei = trim($cutwk[0]);
                            $contents .= $comma.$setSei;
                        } elseif ($key === "MEI") {
                            $cutwk = explode("　", $row["GNAME"], 2);
                            $cutwk[1] = str_replace("　", " ", $cutwk[1]);
                            $setMei = trim($cutwk[1]);
                            $contents .= $comma.$setMei;
                        } elseif ($key === "KANA_SEI") {
                            $cutwk = explode("　", $row["GKANA"]);
                            $setKanaSei = trim($cutwk[0]);
                            $contents .= $comma.$setKanaSei;
                        } elseif ($key === "KANA_MEI") {
                            $cutwk = explode("　", $row["GKANA"], 2);
                            $cutwk[1] = str_replace("　", " ", $cutwk[1]);
                            $setKanaMei = trim($cutwk[1]);
                            $contents .= $comma.$setKanaMei;
                        } else {
                            $contents .= $comma;
                        }
                    }
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
            $couse = "学籍情報一括登録・保証人情報 出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
