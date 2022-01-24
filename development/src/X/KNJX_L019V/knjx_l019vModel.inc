<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_l019vModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $year;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    public function knjx_l019vModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),         //処理名
                             "EXAM_SCHOOL_KIND" =>  VARS::post("EXAM_SCHOOL_KIND"),  //校種
                             "EXAM_ID"          =>  VARS::post("EXAM_ID"),           //試験ID
                             "PLACE_ID"         =>  VARS::post("PLACE_ID"),          //会場
                             "EXAM_SUBCLASS"    =>  VARS::post("EXAM_SUBCLASS"),     //科目
                             );

        $this->cmd = VARS::request("cmd");
        $this->year = VARS::request("YEAR") ? VARS::request("YEAR") : "";
        $this->file = VARS::file("FILE");
    }

    public function &getMainModel()
    {
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
                    knjx_l019vQuery::insertQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"            => array(4, "年度"),
                               "EXAM_ID"         => array(9, "試験ID"),
                               "RECEPTNO"        => array(4, "受験番号"),
                               "EXAM_SUBCLASS"   => array(2, "科目コード"),
                               "SCORE"           => array(3, "得点"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('ENTEXAM_STD_SCORE_DAT');
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

                    $tmpcnt = 0;
                    $data = array("YEAR"                 => $tmp[$tmpcnt++],
                                  "EXAM_ID"              => $tmp[$tmpcnt++],
                                  "EXAM_NAME"            => $tmp[$tmpcnt++],
                                  "RECEPTNO"             => $tmp[$tmpcnt++],
                                  "NAME"                 => $tmp[$tmpcnt++],
                                  "EXAM_SUBCLASS"        => $tmp[$tmpcnt++],
                                  "EXAM_SUBCLASS_NAME"   => $tmp[$tmpcnt++],
                                  "SCORE"                => $tmp[$tmpcnt++]);

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
        $returnFlg = true;

        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $itemErrFlg      = false;
        $sonzaiErrFlg    = false;    //存在エラーフラグ
        $scoreErrFlg     = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $itemErrMsg      = "";
        $sonzaiErrMsg    = "";       //存在エラーメッセージ
        $scoreErrMsg     = "";

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $itemSep      = "";
        $sonzaiSep    = "";
        $scoreSep     = "";

        //重複(CSV)エラー フラグ：メッセージ：セパレータ
        $duplicateCsvErrFlg = false;
        $duplicateCsvErrMsg = "";
        $duplicateCsvSep = "";

        $dupFlg = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "EXAM_ID", "RECEPTNO", "EXAM_SUBCLASS"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "RECEPTNO", "EXAM_SUBCLASS", "SCORE"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //チェック
            if (in_array($key, array("YEAR"))) {
                if ($data[$key] != "" && $data[$key] != $this->year) {
                    $returnFlg = $this->errSet($itemErrFlg, $itemErrMsg, "※不一致:", $itemSep, $val[1]);
                }
            }
        }

        //存在チェック(項目チェックがＯＫの場合)
        if ($returnFlg) {
            //試験ID
            $cntCheck = $db->getOne(knjx_l019vQuery::checkExamId($this, $data));
            if ($cntCheck == 0) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "試験ID");
            }

            //科目コード
            $result = $db->query(knjx_l019vQuery::checkExamSubclass($this, $data));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rowSubclass[] = $row;
            }

            if (!is_array($rowSubclass)) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "科目コード");
            } else {
                if ($data["SCORE"] != "") {
                    $hyokaFlg = false;
                    foreach ($rowSubclass as $subClass) {
                        if ($subClass["STEPS"] == "0") {
                            $returnFlg = $this->errSet($scoreErrFlg, $scoreErrMsg, "※その他:"."満点マスタが確認できません:", $scoreSep, "得点/評価");
                            $hyokaFlg = true;
                        } elseif ($subClass["STEPS"] == "A") {
                            //満点チェック
                            if ($data["SCORE"] > (int)$subClass["VALUE"]) {
                                $returnFlg = $this->errSet($scoreErrFlg, $scoreErrMsg, "※満点:".$subClass["VALUE"]."以下で入力してください:", $scoreSep, "得点");
                            }
                            $hyokaFlg = true;
                        } else {
                            //評価チェック
                            if ($data["SCORE"] == $subClass["STEPS"] && !$hyokaFlg) {
                                $hyokaFlg = true;
                            }
                        }
                    }
                    if (!$hyokaFlg) {
                        $returnFlg = $this->errSet($scoreErrFlg, $scoreErrMsg, "※評価:"."満点マスタの段階値と一致しません:", $scoreSep, "評価");
                    }
                }
            }

            //受験番号 欠席者除く
            $cntCheck = $db->getOne(knjx_l019vQuery::checkReceptno($this, $data));
            if ($cntCheck == 0) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "受験番号");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$itemErrMsg.$sonzaiErrMsg.$scoreErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_l019vQuery::insertQueryErr($db, $line, $checkError);
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダデータ
        $header = array("YEAR"                => "※年度",
                        "EXAM_ID"             => "※試験ID",
                        "EXAM_NAME"           => "試験名",
                        "RECEPTNO"            => "※受験番号",
                        "NAME"                => "氏名",
                        "EXAM_SUBCLASS"       => "※科目コード",
                        "EXAM_SUBCLASS_NAME"  => "科目名",
                        "SCORE"               => "得点/評価",
                        "HYOKA_LABEL"         => "評価 表示記号",
                        "HYOKA_VALUE"         => "評価 値");

        //DB接続
        $db = Query::dbCheckOut();

        $header_rei = array("YEAR"                => $this->year,
                            "EXAM_ID"             => "半角9桁",
                            "EXAM_NAME"           => "一般入試 4科目",
                            "RECEPTNO"            => "数字4桁",
                            "NAME"                => "アルプ太郎",
                            "EXAM_SUBCLASS"       => "数字2桁以内",
                            "EXAM_SUBCLASS_NAME"  => "国語",
                            "SCORE"               => "数字3桁以内",
                            "HYOKA_LABEL"         => "A",
                            "HYOKA_VALUE"         => "1");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_l019vQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ
                if ($this->field["OUTPUT"] == "4") {
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    if ($row["PERFECTDIV"] == "0" && $row["SCORE"] != "") {
                        //評価取得
                        $query = knjx_l019vQuery::getExamSubclassHyoka($this, $row["EXAM_ID"], $row["EXAM_SUBCLASS"], $row["SCORE"]);
                        $hyokaRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $row["HYOKA_LABEL"] = $hyokaRow["LABEL"];
                        $row["HYOKA_VALUE"] = $hyokaRow["VALUE"];
                    }

                    //ヘッダ順に値をセット
                    $sep = "";
                    foreach ($header as $field => $label) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }

                    $contents .= "\n";
                } else {
                    $contents .= implode(",", $row) ."\n";
                }

                $header_data = true;
            }
            $result->free();
        }

        $schoolKindName = "";
        $examName = "";
        $hollName = "";
        $subclassName = "";

        //校種
        $query = knjx_l019vQuery::getNameMst($this->year, "L003", $this->field["EXAM_SCHOOL_KIND"]);
        $schoolKindRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($schoolKindRow["NAME1"] != "") {
            $schoolKindName = "_".$schoolKindRow["NAME1"];
        }
        //試験名
        $query = knjx_l019vQuery::getExamId($this, $this->field["EXAM_ID"]);
        $examRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($examRow["EXAM_NAME"] != "") {
            $examName = "_".$examRow["EXAM_NAME"];
        }
        //会場名
        $query = knjx_l019vQuery::getPlaceId($this, $this->field["PLACE_ID"]);
        $hollRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($hollRow["PLACE_NAME"] != "") {
            $hollName = "_".$hollRow["PLACE_NAME"];
        }
        //科目名
        $query = knjx_l019vQuery::getExamSubclass($this, $this->field["EXAM_SUBCLASS"]);
        $subclassRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($subclassRow["SUBCLASS_NAME"] != "") {
            $subclassName = "_".$subclassRow["SUBCLASS_NAME"];
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $fname = "エラー出力";
            } elseif ($this->field["OUTPUT"] == "1") {
                $fname = $this->year."年度入試"."　試験結果入力";
            } else {                                //エラー以外
                $fname  = $this->year."年度入試"."　試験結果入力";
                $fname .= $schoolKindName;
                $fname .= $examName;
                $fname .= $hollName;
                $fname .= $subclassName;
            }
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
