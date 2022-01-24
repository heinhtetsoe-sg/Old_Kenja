<?php

require_once('for_php7.php');
class knjl650aModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $testsubclasscd;
    public $hallcd;
    public $sort;
    public $ObjYear;
    public $field      = array();
    public $statno;
    public $lastColumn;//CSVの最後の列の値

    public function knjl650aModel()
    {
    }

    public function init()
    {
        $this->ObjYear  = CTRL_YEAR+1;
        $this->examtype = '1'; //固定

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->hallcd           = strlen(VARS::post("HALLCD")) ? VARS::post("HALLCD") : VARS::post("HID_HALLCD");
        $this->sort             = VARS::post("SORT");
        $this->statno           = strlen(VARS::post("STARTNO")) ? VARS::post("STARTNO") : 1;

        //hidden項目
        $this->hid_receptno     = VARS::post("HID_RECEPTNO");

        $this->field = array("HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                            );

        $this->file = VARS::file("FILE");

        $this->arr_score = array();
        $this->arr_magnifying = array();

        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            //得点
            $this->arr_score[$receptno] = VARS::post("SCORE-".$receptno);
            //得点倍率
            $this->arr_magnifying[$receptno] = VARS::post("MAGNIFYING_".$receptno);
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {

        //更新
        $result = knjl650aQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }

    /***********/
    /* CSV取込 */
    /***********/
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl650aQuery::insertQueryCsv($this, $this->data_arr);

            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".get_count($this->err_data)."件\\n".$erraDatas);
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]       = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["APPLICANTDIV"]      = array(1,      "HISSU",    "INT",  "入試制度");
            $checkdata["TESTDIV"]           = array(1,      "HISSU",    "INT",  "入試区分");
            $checkdata["TESTSUBCLASSCD"]    = array(2,      "HISSU",    "",     "科目コード");
            $checkdata["SEATNO"]            = array(4,      "HISSU",    "",     "座席番号");
            $checkdata["EXAMNO"]            = array(10,     "HISSU",    "",     "受験番号");
            $checkdata["SCORE"]             = array(3,      "",         "INT",  "得点");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $data = array();
                    $data["ENTEXAMYEAR"]    = $tmp[0];
                    $data["APPLICANTDIV"]   = $tmp[1];
                    $data["TESTDIV"]        = $tmp[3];
                    $data["TESTSUBCLASSCD"] = $tmp[5];
                    $data["SEATNO"]         = $tmp[7];
                    $data["EXAMNO"]         = $tmp[8];
                    $data["SCORE"]          = $tmp[10];

                    $dataCnt = 12;

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
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep       = "";
        $existenceSep   = "";
        $existenceSep   = "";
        $numericSep     = "";
        $lengthSep      = "";

        $tesetdiv = $this->applicantdiv == "1" ? "L024" : "L004";
        $checkNameMstCd = array("APPLICANTDIV"      => "L003",
                                "TESTDIV"           => $tesetdiv,
                                "TESTSUBCLASSCD"    => "L009");

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                if ($key == "SCORE" && $data[$key] == "*") {
                } else {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
            }

            //年度
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試制度
            if ($key == "APPLICANTDIV" && $this->applicantdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試区分
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            // //試験科目
            // if ($key == "TESTSUBCLASSCD" && $this->testsubclasscd != $data[$key]) {
            //     $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            // }

            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl650aQuery::nameCntSql($this, $checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl650aQuery::getReceptNo($data, "COUNT");
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //満点チェック
            if ($key == "SCORE" && $data[$key] != "") {
                $query = knjl650aQuery::getPerfectMst($data, "PERFECT");
                $perfect = $db->getOne($query);
                if ($data[$key] > $perfect) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※満点:", $existenceSep, $perfect."以下で入力してください。");
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]          = "※入試年度";
        $header["APPLICANTDIV"]         = "※入試制度";
        $header["APPLICANTDIV_NAME"]    = "入試制度名称";
        $header["TESTDIV"]              = "※入試区分";
        $header["TESTDIV_NAME"]         = "入試区分名称";
        $header["TESTSUBCLASSCD"]       = "※科目コード";
        $header["TESTSUBCLASSCD_NAME"]  = "科目名称";
        $header["SEATNO"]               = "※座席番号";
        $header["EXAMNO"]               = "※受験番号";
        $header["NAME"]                 = "氏名";
        $header["SCORE"]                = "得点";
        $header[$this->lastColumn]      = $this->lastColumn;

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;
        $query = knjl650aQuery::selectQueryCsv2($this, $this->statno);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                if ($field == "SCORE" && $row["ATTEND_FLG"] == "0") {
                    $row[$field] = "*";
                }
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->ObjYear."年度得点入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}