<?php

require_once('for_php7.php');

class knjl432mModel extends model
{
    public $cmd;
    public $ObjYear;
    public $examSchoolKind;
    public $examId;
    public $wkExamId;
    public $sort;
    public $lowerScore;
    public $upperScore;
    public $judgement;
    public $keyArray;
    public $examnoArray;
    public $receptnoArray;
    public $advanceCheckArray;
    public $field = array();
    public $file;

    public function knjl432mModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->examSchoolKind);
            unset($this->examId);
            unset($this->wkExamId);
            unset($this->sort);
            unset($this->lowerScore);
            unset($this->upperScore);
            unset($this->judgement);
        }

        $this->cmd = VARS::request("cmd");
        $this->objYear = CTRL_YEAR + 1;
        $this->examSchoolKind = VARS::post("EXAM_SCHOOL_KIND");
        $this->examId = VARS::post("EXAM_ID");
        $this->sort = VARS::post("SORT");
        $this->lowerScore = VARS::post("LOWER_EXAM_SCORE");
        $this->upperScore = VARS::post("UPPER_EXAM_SCORE");
        $this->judgement = VARS::post("JUDGEMENT");
        $this->examnoArray = array();
        $this->receptnoArray = array();
        $this->advanceCheckArray = array();

        //CSV出力
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                             );

        $this->file = VARS::file("FILE");
    }

    //入力項目チェック
    public function validateInput()
    {
        $returnFlg = false;
        $msg = "";
        if ($this->cmd == "allpass") {
            //全員合格
            $this->judgement = "1";
        }

        foreach ($this->keyArray as $idx => $key) {
            $check = VARS::post("CHECK_{$key}");
            $advanceCheck = VARS::post("ADVANCE_CHECK_{$key}");

            //更新対象チェック
            if (strlen($check) || $this->cmd == "allpass") {
                $returnFlg = true;
                $this->examnoArray[$key] = VARS::post("EXAMNO_{$key}");
                $this->receptnoArray[$key] = VARS::post("RECEPTNO_{$key}");
                $this->advanceCheckArray[$key] = $advanceCheck;
            }

            //繰り上げ合格チェック
            if (strlen($advanceCheck) && $this->judgement != "1") {
                $receptno = VARS::post("RECEPTNO_{$key}");
                $msg  = "\\繰り上げ合格にチェックが入っています。";
                $msg .= "\\n不合格の場合、繰り上げ合格は選択できません。";
                $msg .= "\\n( 受験番号:{$receptno} )";
                $this->setWarning("MSG203", $msg);
                return false;
            }
        }

        if (!$returnFlg) {
            $msg .= "\\n更新対象者が存在しません。";
            $this->setWarning("MSG203", $msg);
        }
        return $returnFlg;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjl432mQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjl432mQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjl432mQuery::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"           => array(4,"年度"),
                               "EXAM_ID"        => array(9,"試験ID"),   // 9桁固定
                               "EXAM_NAME"      => array(60,"試験名"),
                               "EXAMNO"         => array(8,"※統一志願者No"),
                               "RECEPTNO"       => array(4,"受験番号"),
                               "NAME"           => array(120,"氏名"),
                               "JUDGEMENT"      => array(8,"合否"),
                               "SUC_COURSECODE" => array(8,"合格コース"),
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $title = true;
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('ENTEXAM_STD_RECEPT_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$title) {
                        $title = true;
                        $line++; //タイトル行を対象外とする
                        continue;
                    }

                    if (!$header && $this->field["HEADER"]=="on") {
                        $header = true;
                        $line++; //ヘッダ行を対象外とする
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("YEAR"            => $tmp[0],
                                  "EXAM_ID"         => $tmp[1],
                                  "EXAM_NAME"       => $tmp[2],
                                  "EXAMNO"          => $tmp[3],
                                  "RECEPTNO"        => $tmp[4],
                                  "NAME"            => $tmp[5],
                                  "JUDGEMENT"       => $tmp[6],
                                  "SUC_COURSECODE"  => $tmp[7],
                                  );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","EXAM_ID","EXAMNO","RECEPTNO","JUDGEMENT"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //文字数チェック
            if (in_array($key, array("YEAR","EXAMNO","RECEPTNO","JUDGEMENT","SUC_COURSECODE"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } elseif ($key == "EXAM_ID") {
                // 試験IDは固定桁数（半角9桁）
                if (strlen($data[$key]) != $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※固定サイズ:", $lengthSep, $val[1]);
                }
            } elseif ($this->field["SHORI_MEI"] == "1") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjl432mQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        $header = array();
        if ($this->field["OUTPUT"]=="3") {
            //ヘッダ(エラー出力)
            $header["MSGROW"]     = "行番号";
            $header["MSGREMARK"]  = "メッセージ";
            $header["VIEW"]       = "観点";
            $header["REMARK1"]    = "コメント";
        } else {
            //ヘッダ(データ出力)
            $header["YEAR"]             = "※年度";
            $header["EXAM_ID"]          = "※試験ID";
            $header["EXAM_NAME"]        = "試験名";
            $header["EXAMNO"]           = "※統一志願者No";
            $header["RECEPTNO"]         = "※受験番号";
            $header["NAME"]             = "氏名";
            $header["JUDGEMENT"]        = "※合否";
            $header["SUC_COURSECODE"]   = "合格コース";
        }

        //ヘッダ出力
        if ($this->field["HEADER"] == "on" || $this->field["OUTPUT"]=="1") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $query = knjl432mQuery::selectCsvQuery($this); //データ出力
            if ($this->field["OUTPUT"]=="3") {
                $query = $this->selectCsvErrQuery(PROGRAMID); //エラー出力
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
                $data = true;
            }
            $result->free();
        } else {
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $title = "エラー出力";
            } else {                                //エラー以外
                $title = $this->objYear."年度合否入力";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
