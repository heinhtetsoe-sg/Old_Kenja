<?php

require_once('for_php7.php');

class knjl517jModel extends model {
    var $field      = array();
    var $cmd;
    var $applicantdiv;
    var $testdiv;        //入試種別
    var $year;

    function knjl517jModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->year = CTRL_YEAR + 1;

        $this->cmd = VARS::request("cmd");

        $this->field = array("HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "PROCEDUREDATE"    =>  VARS::post("PROCEDUREDATE")      //手続年月日
                            );

        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("TESTDIV");
        if (isset($temp)) {
            $this->testdiv = VARS::request("TESTDIV");
        }

        $this->selectdata = VARS::post("selectdata");

        $this->file = VARS::file("FILE");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
        knjl517jQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //CSV取込
    function &getCsvInputModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjl517jQuery::updateQueryCsv($this, $this->data_arr);

            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".get_count($this->err_data)."件\\n".$erraDatas);
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]       = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["EXAMNO"]            = array(10,     "HISSU",    "",     "受験番号");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) $tmp[$t] = trim($tmp[$t]);

                    $cnt = 0;
                    $data = array();
                    $data["MIRAI_SCHOOLCD"]           = $tmp[$cnt++];
                    $data["ENTEXAMYEAR"]              = $tmp[$cnt++];
                    $data["APPNO"]                    = $tmp[$cnt++];
                    $data["SHIGANSYA_ID"]             = $tmp[$cnt++];
                    $data["EXAMNO"]                   = $tmp[$cnt++];
                    $data["EXAM_NAME_SEI"]            = $tmp[$cnt++];
                    $data["EXAM_NAME_MEI"]            = $tmp[$cnt++];
                    $data["SEX"]                      = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME"]            = $tmp[$cnt++];
                    $data["ITEM_GROUP"]               = $tmp[$cnt++];
                    $data["ITEM_ID"]                  = $tmp[$cnt++];
                    $data["ITEM_NAME"]                = $tmp[$cnt++];
                    $data["BULK_MONEY"]               = $tmp[$cnt++];
                    $data["DELAY_PAY_LUMP"]           = $tmp[$cnt++];
                    $data["ACCEPT_START_DATE"]        = $tmp[$cnt++];
                    $data["PAY_DUE_DATE_MST"]         = $tmp[$cnt++];
                    $data["DELAY_DUE_DATE"]           = $tmp[$cnt++];
                    $data["ITEM_EXPLAIN"]             = $tmp[$cnt++];
                    $data["PAY_DEC"]                  = $tmp[$cnt++];
                    $data["PAY_DEC_DATE"]             = $tmp[$cnt++];
                    $data["PAY_DEC_REASON"]           = $tmp[$cnt++];
                    $data["PAY_DEC_REASON_DETAIL"]    = $tmp[$cnt++];
                    $data["MEMO_MST"]                 = $tmp[$cnt++];
                    $data["ITEM_PAY_DIV"]             = $tmp[$cnt++];
                    $data["PAY_TYPE_CD"]              = $tmp[$cnt++];
                    $data["APP_EXAMNO"]               = $tmp[$cnt++];
                    $data["NAME_SEI"]                 = $tmp[$cnt++];
                    $data["NAME_MEI"]                 = $tmp[$cnt++];
                    $data["NAME_KANA_SEI"]            = $tmp[$cnt++];
                    $data["NAME_KANA_MEI"]            = $tmp[$cnt++];
                    $data["BIRTHDAY"]                 = $tmp[$cnt++];
                    $data["TELNO"]                    = $tmp[$cnt++];
                    $data["MAIL"]                     = $tmp[$cnt++];
                    $data["SETTLE_MONEY"]             = $tmp[$cnt++];
                    $data["DEMAND_TOTAL_MONEY"]       = $tmp[$cnt++];
                    $data["FEES"]                     = $tmp[$cnt++];
                    $data["PAY_INFO_DIV"]             = $tmp[$cnt++];
                    $data["APP_DATED"]                = $tmp[$cnt++];
                    $data["PAY_DUE_DATE"]             = $tmp[$cnt++];
                    $data["PAY_DATED"]                = $tmp[$cnt++];
                    $data["ENT_DEC"]                  = $tmp[$cnt++];
                    $data["ENT_DEC_DATE"]             = $tmp[$cnt++];
                    $data["ENT_DEC_REASON"]           = $tmp[$cnt++];
                    $data["ENT_DEC_REASON_DETAIL"]    = $tmp[$cnt++];
                    $data["MEMO_PAY"]                 = $tmp[$cnt++];

                    $dataCnt = get_count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $otherErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherErrMsg     = "";       //その他エラーメッセージ

        $hissuSep       = "";
        $existenceSep   = "";
        $existenceSep   = "";
        $numericSep     = "";
        $lengthSep      = "";
        $otherSep       = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //年度
            if ($key == "ENTEXAMYEAR" && $this->year != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl517jQuery::checkExamNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //CSV出力
    function getCsvModel() {

        //ヘッダ
        $header = array();
        $header["MIRAI_SCHOOLCD"]           = "学校コード";
        $header["ENTEXAMYEAR"]              = "年度";
        $header["APPNO"]                    = "入学金納入申込番号";
        $header["SHIGANSYA_ID"]             = "志願者ID";
        $header["EXAMNO"]                   = "受験番号";
        $header["EXAM_NAME_SEI"]            = "氏名（姓）_受験者";
        $header["EXAM_NAME_MEI"]            = "氏名（名）_受験者";
        $header["SEX"]                      = "性別";
        $header["MIRAI_FS_NAME"]            = "出身校";
        $header["MIRAI_PS_NAME"]            = "塾";
        $header["ITEM_GROUP"]               = "商品グループ";
        $header["ITEM_ID"]                  = "商品ＩＤ";
        $header["ITEM_NAME"]                = "商品名";
        $header["BULK_MONEY"]               = "一括金額";
        $header["DELAY_PAY_LUMP"]           = "延納一時金";
        $header["ACCEPT_START_DATE"]        = "受付開始日";
        $header["PAY_DUE_DATE_MST"]         = "支払期限日(マスタ)";
        $header["DELAY_DUE_DATE"]           = "延納期限日";
        $header["ITEM_EXPLAIN"]             = "商品説明";
        $header["PAY_DEC"]                  = "支払辞退";
        $header["PAY_DEC_DATE"]             = "支払辞退日";
        $header["PAY_DEC_REASON"]           = "支払辞退理由";
        $header["PAY_DEC_REASON_DETAIL"]    = "支払辞退理由詳細";
        $header["MEMO_MST"]                 = "メモ(マスタ変更_管理者)";
        $header["ITEM_PAY_DIV"]             = "商品支払区分";
        $header["PAY_TYPE_CD"]              = "支払種別コード";
        $header["APP_EXAMNO"]               = "申込受験番号";
        $header["NAME_SEI"]                 = "氏名（姓）_漢字_受験者";
        $header["NAME_MEI"]                 = "氏名（名）_漢字_受験者";
        $header["NAME_KANA_SEI"]            = "氏名（姓）_カナ_受験者";
        $header["NAME_KANA_MEI"]            = "氏名（名）_カナ_受験者";
        $header["BIRTHDAY"]                 = "生年月日";
        $header["TELNO"]                    = "電話番号_受験者";
        $header["MAIL"]                     = "メールアドレス_受験者";
        $header["SETTLE_MONEY"]             = "決済金額";
        $header["DEMAND_TOTAL_MONEY"]       = "請求合計金額";
        $header["FEES"]                     = "事務手数料";
        $header["PAY_INFO_DIV"]             = "入金情報区分";
        $header["APP_DATED"]                = "申込日時";
        $header["PAY_DUE_DATE"]             = "支払期限日";
        $header["PAY_DATED"]                = "支払日時";
        $header["ENT_DEC"]                  = "入学辞退";
        $header["ENT_DEC_DATE"]             = "入学辞退日";
        $header["ENT_DEC_REASON"]           = "入学辞退理由";
        $header["ENT_DEC_REASON_DETAIL"]    = "入学辞退理由詳細";
        $header["MEMO_PAY"]                 = "メモ(支払_管理者)";

        //ヘッダ例
        $header_rei = array();
        $header_rei["MIRAI_SCHOOLCD"]           = "";
        $header_rei["ENTEXAMYEAR"]              = $this->year;
        $header_rei["APPNO"]                    = "";
        $header_rei["SHIGANSYA_ID"]             = "";
        $header_rei["EXAMNO"]                   = "1001";
        $header_rei["EXAM_NAME_SEI"]            = "";
        $header_rei["EXAM_NAME_MEI"]            = "";
        $header_rei["SEX"]                      = "";
        $header_rei["MIRAI_FS_NAME"]            = "";
        $header_rei["MIRAI_PS_NAME"]            = "";
        $header_rei["ITEM_GROUP"]               = "1";
        $header_rei["ITEM_ID"]                  = "A07";
        $header_rei["ITEM_NAME"]                = "入学金（第４回入試）";
        $header_rei["BULK_MONEY"]               = "290000";
        $header_rei["DELAY_PAY_LUMP"]           = "";
        $header_rei["ACCEPT_START_DATE"]        = "";
        $header_rei["PAY_DUE_DATE_MST"]         = "";
        $header_rei["DELAY_DUE_DATE"]           = "";
        $header_rei["ITEM_EXPLAIN"]             = "";
        $header_rei["PAY_DEC"]                  = "辞退しない";
        $header_rei["PAY_DEC_DATE"]             = "";
        $header_rei["PAY_DEC_REASON"]           = "";
        $header_rei["PAY_DEC_REASON_DETAIL"]    = "";
        $header_rei["MEMO_MST"]                 = "";
        $header_rei["ITEM_PAY_DIV"]             = "一括納入";
        $header_rei["PAY_TYPE_CD"]              = "";
        $header_rei["APP_EXAMNO"]               = "1001";
        $header_rei["NAME_SEI"]                 = "";
        $header_rei["NAME_MEI"]                 = "";
        $header_rei["NAME_KANA_SEI"]            = "";
        $header_rei["NAME_KANA_MEI"]            = "";
        $header_rei["BIRTHDAY"]                 = "";
        $header_rei["TELNO"]                    = "";
        $header_rei["MAIL"]                     = "";
        $header_rei["SETTLE_MONEY"]             = "";
        $header_rei["DEMAND_TOTAL_MONEY"]       = "";
        $header_rei["FEES"]                     = "";
        $header_rei["PAY_INFO_DIV"]             = "完了";
        $header_rei["APP_DATED"]                = "";
        $header_rei["PAY_DUE_DATE"]             = "";
        $header_rei["PAY_DATED"]                = "";
        $header_rei["ENT_DEC"]                  = "未辞退";
        $header_rei["ENT_DEC_DATE"]             = "";
        $header_rei["ENT_DEC_REASON"]           = "";
        $header_rei["ENT_DEC_REASON_DETAIL"]    = "";
        $header_rei["MEMO_PAY"]                 = "";

        //初期化
        $contents = "";
        $header_data = false;

        //ヘッダ出力
        $header_data = true;
        $contents .= implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        //CSV出力
        if ($header_data) {
            $title = $this->year."年度"."手続終了者入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
