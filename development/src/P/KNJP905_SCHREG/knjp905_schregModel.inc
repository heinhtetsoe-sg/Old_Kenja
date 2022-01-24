<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp905_schregModel extends Model
{
    public $field      = array();
    public $data_arr   = array();
    public $cmd;
    public $getAuth;
    public $getPrgId;
    public $getSchoolKind;
    public $getYear;
    public $getOutgoLcd;
    public $getOutgoMcd;
    public $getOutgoLMcd;
    public $getRequestNo;
    public $getIncomeLMcd;
    public $getLineNo;
    public $getRequestDate;
    public $auth;
    public $year;
    public $control;        //コントロールマスタ
    public $staffcd;        //職員コード
    public $selectStudent;
    public $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    public function knjp905_schregModel()
    {
    }

    public function init()
    {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getSchoolKind   = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getOutgoLcd     = VARS::get("SEND_OUTGO_L_CD");
            $this->getOutgoMcd     = VARS::get("SEND_OUTGO_M_CD");
            $this->getOutgoLMcd    = VARS::get("SEND_OUTGO_L_M_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
            $this->getIncomeLMcd   = VARS::get("SEND_INCOME_LM_CD");
            $this->getLineNo       = VARS::get("SEND_LINE_NO");
            $this->getRequestDate  = VARS::get("SEND_REQUEST_DATE");
        }

        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("WARIHURI_DIV"         =>  VARS::post("WARIHURI_DIV"),         //1:生徒、2:生徒以外
                             "OUTGO_L_M_S_CD"       =>  VARS::post("OUTGO_L_M_S_CD"),       //会計細目コンボ
                             "OUTGO_NAME"           =>  VARS::post("OUTGO_NAME"),           //会計細目テキスト
                             "REPAY_DIV"            =>  VARS::post("REPAY_DIV"),            //返金可・不可
                             "BENEFIT"              =>  VARS::post("BENEFIT"),              //給付対象checkbox
                             "BUDGET_L_M_S_CD"      =>  VARS::post("BUDGET_L_M_S_CD"),      //予算細目
                             "NOT_MINUS_FLG"        =>  VARS::post("NOT_MINUS_FLG"),        //予算から生徒単価を引かないフラグ
                             "COMMODITY_PRICE"      =>  VARS::post("COMMODITY_PRICE"),      //支出総額
                             "IN_TAX"               =>  VARS::post("IN_TAX"),               //税込
                             "COMMODITY_CNT"        =>  VARS::post("COMMODITY_CNT"),        //数量
                             "TOTAL_TAX"            =>  VARS::post("TOTAL_TAX"),            //消費税
                             "TOTAL_PRICE_ZEINUKI"  =>  VARS::post("TOTAL_PRICE_ZEINUKI"),  //合計金額(税抜き)
                             "TOTAL_PRICE"          =>  VARS::post("TOTAL_PRICE"),          //合計金額(税込)
                             "TRADER_SEIKYU_NO"     =>  VARS::post("TRADER_SEIKYU_NO"),     //請求番号
                             "SEIKYU_MONTH"         =>  VARS::post("SEIKYU_MONTH"),         //請求月
                             "REMARK"               =>  VARS::post("REMARK"),               //摘要
                             "SCH_CNT"              =>  VARS::post("SCH_CNT"),              //生徒数
                             "SCH_PRICE"            =>  VARS::post("SCH_PRICE"),            //生徒単価
                             "HASUU"                =>  VARS::post("HASUU"),                //端数
                             "HR_CLASS_HYOUJI_FLG"  =>  VARS::post("HR_CLASS_HYOUJI_FLG"),  //年組番表示
                             "CHK_TRANSFER"         =>  VARS::post("CHK_TRANSFER"),         //異動者除外
                             "SYORI"                =>  VARS::post("SYORI"),                //処理名
                             "HEADER"               =>  VARS::post("HEADER"),               //ヘッダー
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        //年組表示を初期値とする
        if (VARS::get("SEND_AUTH")) {
            $this->field["HR_CLASS_HYOUJI_FLG"] = "1";
            $this->field["CHK_TRANSFER"] = "1";
        }
        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentRight = VARS::post("selectStudentRight");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");
        $this->transferList = VARS::post("transferList");

        $this->cmd         = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    //入力項目チェック
    public function validateInput($db)
    {
        //必須チェック
        if ($this->field["OUTGO_L_M_S_CD"] == "") {
            $this->setWarning("MSG301", "( 会計細目 )");
            return false;
        }
        if ($this->field["COMMODITY_CNT"] == "") {
            $this->setWarning("MSG301", "( 数量 )");
            return false;
        }
        //桁数チェック
        if (strlen($this->field["COMMODITY_PRICE"]) > 8) {
            $this->setWarning("MSG901", "( 支出単価 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["COMMODITY_CNT"]) > 8) {
            $this->setWarning("MSG901", "( 数量 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["TRADER_SEIKYU_NO"]) > 10) {
            $this->setWarning("TRADER_SEIKYU_NO", "( 請求番号 ) 10バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK"]) > 30) {
            $this->setWarning("MSG901", "( 摘要 ) 30バイトまで");
            return false;
        }

        //更新時予算細目チェック
        if ($this->Properties["LevyBudget"] == "1" && $this->field["BUDGET_L_M_S_CD"] == "") {
            $this->setWarning("MSG301", "( 予算細目 )");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            knjp905_schregQuery::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();

        knjp905_schregQuery::getDeleteQuery($db, $this);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);

        return;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        $errorMsg = '';
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString($errorMsg);

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $this->setMessage("取込生徒：".get_count($this->data_arr)."件\\n取込対象外\\n{$errorMsg}");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function GetFileString(&$errorMsg)
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;
            $header = false;
            $kai = '';
            while (!feof($fp)) {
                $buffer = fgets($fp, 8192);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $cnt = 0;
                    $data = array();

                    $data["SCHREGNO"] = sprintf("%08d", $tmp[$cnt++]);

                    //リスト内の生徒かチェック
                    if (in_array($data["SCHREGNO"], $this->schList, true)) {
                        $temp_arr[$i] = $data["SCHREGNO"];
                        $i++;
                    } else {
                        $errorMsg .= $kai."{$line}行目：{$data["SCHREGNO"]}";
                        $kai = "\\n";
                    }
                    $line++;
                }
            }
            fclose($fp);
        }
        return $temp_arr;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }
}
?>

