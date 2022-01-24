<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl672aModel extends Model
{
    public $cmd;
    public $field      = array();

    public $ObjYear;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード

    public $file;

    /**
     * メンバ変数の初期化
     */
    public function knjl672aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->cmd         = VARS::request("cmd");

        $this->field = array(
                        "APPLICANTDIV"  =>  VARS::post("APPLICANTDIV")  // 入試制度(校種)
                        , "TESTDIV"     =>  VARS::post("TESTDIV")       // 入試区分
                        , "COURSEMAJOR" =>  VARS::post("COURSEMAJOR")   // 入学課程学科
                        , "COURSECODE"  =>  VARS::post("COURSECODE")    // 入学コース
                        , "CSV_INOUT"   =>  VARS::post("CSV_INOUT")     // 取り込み／書き出し
                        , "HEADER"      =>  VARS::post("HEADER")        // ヘッダー有り
                    );

        if ($this->cmd == 'update') {
            $this->examNoList = array();
            if (VARS::post("ENTRY_EXAMNO")) {
                // 入学決定者
                $temp = explode(",", VARS::post("ENTRY_EXAMNO"));
                for ($i=0; $i < get_count($temp); $i++) {
                    $examNo = $temp[$i];
                    $this->examNoList[] = array("EXAMNO" => $examNo, "SEL" => "1");
                }
            }
            if (VARS::post("NONENTRY_EXAMNO")) {
                // 未入学
                $temp = explode(",", VARS::post("NONENTRY_EXAMNO"));
                for ($i=0; $i < get_count($temp); $i++) {
                    $examNo = $temp[$i];
                    $this->examNoList[] = array("EXAMNO" => $examNo, "SEL" => "0");
                }
            }
        }

        $this->file = VARS::file("FILE");
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新
            $result = knjl672aQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }


    /***********/
    /* CSV出力 */
    /***********/
    public function &getCsvOutputModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]      = "※入試年度";
        $header["APPLICANTDIV"]     = "※入試制度";
        $header["TESTDIV"]          = "※入試区分";
        $header["COURSECD"]         = "※課程コード";
        $header["MAJORCD"]          = "※学科コード";
        $header["COURSECODE"]       = "※入学コースコード";
        $header["COURSECODENAME"]   = "入学コース名";
        $header["EXAMNO"]           = "※受験番号";
        $header["NAME"]             = "氏名";
        $header["SEXNAME"]          = "性別";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;
        $query = knjl672aQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // 入学決定者のみ出力
            if ($row['PROCEDUREDIV'] != "1" || $row['ENTDIV'] != "1") {
                continue;
            }
            $sep = "";
            foreach ($header as $field => $label) {
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
            $title = $this->ObjYear."年入学決定者";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
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
            $cnt = knjl672aQuery::insertQueryCsv($this, $this->data_arr);

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
            $checkdata["ENTEXAMYEAR"]       = array(4,      "HISSU",    "",  "入試年度");
            $checkdata["APPLICANTDIV"]      = array(1,      "HISSU",    "",  "入試制度");
            $checkdata["TESTDIV"]           = array(1,      "HISSU",    "",  "入試区分");
            $checkdata["COURSECD"]          = array(1,      "HISSU",    "",  "課程コード");
            $checkdata["MAJORCD"]           = array(3,      "HISSU",    "",  "学科コード");
            $checkdata["COURSECODE"]        = array(4,      "HISSU",    "",  "入学コースコード");
            // $checkdata["COURSECODENAME"]    = array(60,     "",         "",  "入学コース名");
            $checkdata["EXAMNO"]            = array(4,      "HISSU",    "",  "受験番号");
            // $checkdata["NAME"]              = array(60,     "",         "",  "氏名");
            // $checkdata["SEXNAME"]           = array(60,     "",         "",  "性別");


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
                    $data["TESTDIV"]        = $tmp[2];
                    $data["COURSECD"]       = $tmp[3];
                    $data["MAJORCD"]        = $tmp[4];
                    $data["COURSECODE"]     = $tmp[5];
                    // $data["COURSECODENAME"] = $tmp[6];
                    $data["EXAMNO"]         = $tmp[7];
                    // $data["NAME"]           = $tmp[8];
                    // $data["SEXNAME"]        = $tmp[9];

                    $dataCnt = 10;
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
        $existNameErrFlg = false;    //存在エラーフラグ(名称マスタ)
        $existCourseMajorErrFlg = false; //存在エラーフラグ(課程学科マスタ)
        $existCourseCodeErrFlg = false;  //存在エラーフラグ(コースコードマスタ)
        $otherCourseErrFlg = false;  //別コース登録済みフラグ

        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $existNameErrMsg = "";       //存在エラーメッセージ(名称マスタ)
        $existCourseMajorErrMsg = ""; //存在エラーメッセージ(課程学科マスタ)
        $existCourseCodeErrMsg = "";  //存在エラーメッセージ(コースコードマスタ)
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherCourseErrMsg = "";     //別コース登録済みエラーメッセージ

        $hissuSep       = "";
        $existenceSep   = "";
        $existNameSep   = "";
        $existCourseMajorSep = "";
        $existCourseCodeSep  = "";
        $otherCourseSep = "";
        $numericSep     = "";
        $lengthSep      = "";

        $checkNameMstCd = array();
        $checkNameMstCd["APPLICANTDIV"] = "L003";
        $checkNameMstCd["TESTDIV"] = ($this->field["APPLICANTDIV"] == "1") ? "L024" : "L004";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
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
            if ($key == "APPLICANTDIV" && $this->field['APPLICANTDIV'] != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試区分
            if ($key == "TESTDIV" && $this->field['TESTDIV'] != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl672aQuery::nameCntSql($this, $checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existNameErrFlg, $existNameErrMsg, "※存在(名称マスタ):", $existNameSep, $val[3]);
                }
            }

            //課程学科
            if ($key == "COURSECD" && $data[$key] != "") {
                $courseMajor = $data['COURSECD'].$data['MAJORCD'];
                if ($courseMajor != $this->field['COURSEMAJOR']) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            // 画面の課程学科でエラーチェックする為、マスタの確認は不要
            // //課程学科マスタ
            // if ($key == "COURSECD" && $data[$key] != "") {
            //     $query = knjl672aQuery::getCourseMajorCntSql($this, $data['COURSECD'], $data['MAJORCD']);
            //     $rowCount = $db->getOne($query);
            //     if ($rowCount == "0") {
            //         $returnFlg = $this->errSet($existCourseMajorErrFlg, $existCourseMajorErrMsg, "※存在(課程学科マスタ):", $existCourseMajorSep, $val[3]);
            //     }
            // }

            // //コースコードマスタ
            // if ($key == "COURSECODE" && $data[$key] != "") {
            //     $query = knjl672aQuery::getCourseCodeCntSql($this, $data[$key]);
            //     $rowCount = $db->getOne($query);
            //     if ($rowCount == "0") {
            //         $returnFlg = $this->errSet($existCourseCodeErrFlg, $existCourseCodeErrMsg, "※存在(コースコードマスタ):", $existCourseCodeSep, $val[3]);
            //     }
            // }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl672aQuery::getEntexamApplicantBaseCntSql($this, $data['APPLICANTDIV'], $data['TESTDIV'], $data['EXAMNO']);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //別コース登録済み
            if ($key == "COURSECODE" && $data[$key] != "") {
                $query = knjl672aQuery::getEntexamApplicantBaseDetailSql($this, $data['APPLICANTDIV'], $data['EXAMNO']);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row) {
                    if ($row['COURSECD'] != "" && $row['MAJORCD'] != "" && $row['COURSECODE'] != "") {
                        if ($data['COURSECD'] != $row['COURSECD'] || $data['MAJORCD'] != $row['MAJORCD'] || $data['COURSECODE'] != $row['COURSECODE']) {
                            $returnFlg = $this->errSet($otherCourseErrFlg, $otherCourseErrMsg, "※別コース登録済:", $otherCourseSep, $val[3]);
                        }
                    }
                }
            }

            //別入試区分で登録済み
            if ($key == "TESTDIV" && $data[$key] != "") {
                $query = knjl672aQuery::getEntexamApplicantBaseDetailSql($this, $data['APPLICANTDIV'], $data['EXAMNO']);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row) {
                    if ($row['TESTDIV'] != "") {
                        if ($data['TESTDIV'] != $row['TESTDIV']) {
                            $returnFlg = $this->errSet($otherCourseErrFlg, $otherCourseErrMsg, "※別入試区分で登録済:", $otherCourseSep, $val[3]);
                        }
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$existNameErrMsg.$existCourseMajorErrMsg.$existCourseCodeErrMsg;
            $check_error .= $numericErrMsg.$lengthErrMsg.$otherCourseErrMsg;
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
}
