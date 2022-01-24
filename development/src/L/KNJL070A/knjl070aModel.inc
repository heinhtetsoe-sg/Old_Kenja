<?php

require_once('for_php7.php');
class knjl070aModel extends model
{
    public $cmd;
    public $ObjYear;
    public $examType;
    public $applicantdiv;
    public $testdiv;
    public $sort;
    public $shdiv;
    public $pass_div;
    public $wish_course;
    public $upd_shdiv;
    public $upd_course;
    public $header;
    public $lastColumn;//CSVの最後の列の値

    public function knjl070aModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->hid_ReceptNo);
        }

        $this->ObjYear  = CTRL_YEAR+1;
        $this->examType = 1;    //固定

        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");

        $this->sort         = VARS::post("SORT");
        $this->shdiv        = VARS::post("SHDIV");
        $this->pass_div     = VARS::post("PASS_DIV");
        $this->wish_course  = VARS::post("WISH_COURSE");

        $this->upd_shdiv    = VARS::post("UPD_SHDIV");
        $this->upd_course   = VARS::post("UPD_COURSE");

        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");
        $this->chkReceptNo  = VARS::post("CHECKED");

        $this->header       = VARS::post("HEADER");

        if (VARS::get("PROGRAMID")) {
            $this->header = "on";
        }

        $this->cmd = VARS::request("cmd");

        $this->file = VARS::file("FILE");

        if ($this->cmd == "update") {
            $this->applicantdiv = VARS::post("HIDDEN_APPLICANTDIV");
            $this->testdiv      = VARS::post("HIDDEN_TESTDIV");
            $this->sort         = VARS::post("HIDDEN_SORT");
            $this->shdiv        = VARS::post("HIDDEN_SHDIV");
            $this->pass_div     = VARS::post("HIDDEN_PASS_DIV");
            $this->wish_course  = VARS::post("HIDDEN_WISH_COURSE");
            $this->upd_shdiv    = VARS::post("HIDDEN_UPD_SHDIV");
            $this->upd_course   = VARS::post("HIDDEN_UPD_COURSE");
            $this->header       = VARS::post("HIDDEN_HEADER");
            $this->bikou = array();
            foreach ($this->chkReceptNo as $ReceptNo) {
                $this->bikou[$ReceptNo] = VARS::post("BIKOU-".$ReceptNo);
            }
        }

        //校種変換用
        if ($this->applicantdiv == "") {
            $db = Query::dbCheckOut();
            $this->skArray = array();
            $query = knjl070aQuery::getNameMst($this->ObjYear, 'L003');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->skArray[$row["VALUE"]] = $row["NAMESPARE3"];
            }
            Query::dbCheckIn($db);
        }

        //プロパティファイル
        $this->getPropertiesAll();
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
        knjl070aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        list($this->data_arr, $errMaxLine) = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl070aQuery::insertQueryCsv($this, $this->data_arr);

            $errDatas = "";
            $errCnt = 0;
            $keta = (strlen($errMaxLine)) ? strlen($errMaxLine) : 1;
            foreach ($this->err_data as $errKey => $errVal) {
                $errCnt++;
                if ($errCnt <= 10) {
                    if (strlen($errKey) != $keta) {
                        for ($sp=1; $sp<=($keta-strlen($errKey)); $sp++) {
                            $errDatas .= "\x20\x20";
                        }
                    }
                    $errDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
                }
            }
            if (($errCnt - 10) > 0) {
                $errDatas .= "…\\n";
            }
            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数       ：".get_count($this->err_data)."件\\n".$errDatas);
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック対象
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]       = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["APPLICANTDIV"]      = array(1,      "HISSU",    "",     "受験校種");
            $checkdata["TESTDIV"]           = array(1,      "HISSU",    "",     "試験区分");
            $checkdata["UPD_SHDIV"]         = array(1,      "HISSU",    "",     "更新対象");
            $checkdata["UPD_COURSE"]        = array(1,      "",         "",     "合格コース");
            $checkdata["RECEPTNO"]          = array(5,      "HISSU",    "",     "受験番号");
            $checkdata["BIKOU"]             = array(60,     "",         "",     "備考");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $errMaxLine = "";
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->header == "on") {
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
                    $data["TESTDIV"]        = $tmp[2];
                    $data["UPD_SHDIV"]      = $tmp[3];
                    $data["UPD_COURSE"]     = $tmp[4];
                    $data["RECEPTNO"]       = $tmp[5];
                    $data["BIKOU"]          = $tmp[13];

                    $dataCnt = 15;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    } else {
                        $errMaxLine = $line;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return array($temp_arr, $errMaxLine);
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

        $checkNameMstCd = array("UPD_SHDIV"         => "L006",
                                "UPD_COURSE"        => "L".$this->skArray[$data["APPLICANTDIV"]]."13");

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //年度
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験校種
            if ($key == "APPLICANTDIV" && $this->applicantdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //試験
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl070aQuery::getNameMst($this->ObjYear, $checkNameMstCd[$key], $data[$key], "cnt");
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }

            //受験番号
            if ($key == "RECEPTNO" && $data[$key] != "") {
                $query = knjl070aQuery::checkReceptNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //更新対象
            if ($key == "UPD_SHDIV" && $data[$key] != "") {
                $shdiv = $db->getOne(knjl070aQuery::getShdiv($this, $data["RECEPTNO"]));
                if ($data[$key] > $shdiv) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "更新対象外");
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

    //CSV出力
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]          = "※入試年度";
        $header["APPLICANTDIV"]         = "※受験校種";
        $header["TESTDIV"]              = "※試験区分";
        $header["UPD_SHDIV"]            = "※更新対象";
        $header["UPD_COURSE"]           = "合格コース";
        $header["RECEPTNO"]             = "※受験番号";
        $header["NAME"]                 = "氏名";
        $header["FINSCHOOL_NAME_ABBV"]  = "出身学校";
        $header["SHDIVNAME"]            = "出願";
        $header["COURSENAME"]           = "志望コース";
        $header["SCORE"]                = "成績";
        $header["COURSENAME1"]          = "専願合格";
        $header["COURSENAME2"]          = "併願合格";
        $header["BIKOU"]                = "備考";
        $header[$this->lastColumn]      = $this->lastColumn;

        //ヘッダ出力
        if ($this->header == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;
        $query = knjl070aQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["UPD_SHDIV"]       = $this->upd_shdiv;
            $row["UPD_COURSE"]      = ($this->upd_course == "NO_DATA") ? "" : $this->upd_course;
            $row[$this->lastColumn] = $this->lastColumn;

            //データセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //名称取得
        $applicantdiv   = $db->getRow(knjl070aQuery::getNameMst($this->ObjYear, 'L003', $this->applicantdiv), DB_FETCHMODE_ASSOC);
        $testdiv        = $db->getRow(knjl070aQuery::getTestdivMst($this, $this->testdiv), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->ObjYear."年度 ".$applicantdiv["NAME1"]." ".$testdiv["TESTDIV_NAME"]." 合否入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
