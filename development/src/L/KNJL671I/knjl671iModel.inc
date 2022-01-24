<?php
require_once('for_php7.php');

//定数定義
class knjl671iModel extends Model
{
    public $field = array();
    public $histField = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $examno;
    public $finschoolcd;
    public $name;
    public $sort;
    public $file;

    public $sendPrgid;
    public $sendCmd; //insert, update, updateAll, search

    public $itemArray = array(); //項目名
    public $updItemArray = array(); //更新対象項目
    public $gyoArray = array(); //行数
    public $ketaArray = array(); //桁数
    public $maxSeq;

    public $csvFields = []; // CSV項目

    //メンバ変数の初期化
    public function knjl671iModel()
    {
    }

    public function init()
    {
        $this->field = array("STAFFCD"          =>  VARS::post("STAFFCD"),          //担任
                             "FINSCHOOLCD"      =>  VARS::post("FINSCHOOLCD"),      //出身学校
                             "FINSCHOOL_DISTCD" =>  VARS::post("FINSCHOOL_DISTCD"), //地区
                             "NAME"             =>  VARS::post("NAME"),             //氏名
                             "EXCLUSION"        =>  VARS::post("EXCLUSION"),        //除外フラグ

                             "SEQ001_REMARK1"   =>  VARS::post("SEQ001_REMARK1"),   //詳細 試験区分
                             "SEQ002_REMARK1"   =>  VARS::post("SEQ002_REMARK1"),   //詳細 相談コース
                             "SEQ003_REMARK1"   =>  VARS::post("SEQ003_REMARK1"),   //詳細 共通テスト
                             "SEQ004_REMARK1"   =>  VARS::post("SEQ004_REMARK1"),   //詳細 特待 理由
                             "SEQ004_REMARK2"   =>  VARS::post("SEQ004_REMARK2"),   //詳細 特待 記号
                             "SEQ005_REMARK1"   =>  VARS::post("SEQ005_REMARK1"),   //詳細 部活動
                             "SEQ006_REMARK1"   =>  VARS::post("SEQ006_REMARK1"),   //詳細 備考
                             "SEQ007_REMARK1"   =>  VARS::post("PRISCHOOLCD"),      //詳細 塾
                             "SEQ008_REMARK1"   =>  VARS::post("SEQ008_REMARK1"),   //詳細 欠席 日数
                             "SEQ008_REMARK2"   =>  VARS::post("SEQ008_REMARK2"),   //詳細 欠席 理由書No.
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //CSV処理 ラジオボタン
                             "HEADER"           =>  VARS::post("HEADER")            //CSV処理 ヘッダー有チェックボックス
                            );

        //変更履歴用フィールド
        $this->histField = array("CHANGE_DATE"          =>  VARS::post("CHANGE_DATE"),          //変更履歴 変更日時
                                 "NAME_FLG"             =>  VARS::post("NAME_FLG"),             //変更履歴 氏名
                                 "TESTDIV_FLG"          =>  VARS::post("TESTDIV_FLG"),          //変更履歴 試験区分
                                 "COURSE_FLG"           =>  VARS::post("COURSE_FLG"),           //変更履歴 相談コース
                                 "STANDARD_EXAM_FLG"    =>  VARS::post("STANDARD_EXAM_FLG"),    //変更履歴 共通テスト
                                 "HONOR_FLG"            =>  VARS::post("HONOR_FLG"),            //変更履歴 特待
                                 "OTHER_FLG"            =>  VARS::post("OTHER_FLG"),            //変更履歴 その他
                                 "CHANGE_TEXT"          =>  VARS::post("CHANGE_TEXT"),          //変更履歴 備考
                                 "CLIENT_STAFFCD"       =>  VARS::post("CLIENT_STAFFCD"),       //変更履歴 依頼者
                                 "EDIT_STAFFCD"         =>  VARS::post("EDIT_STAFFCD")          //変更履歴 編集者
                                );
        // CSV項目
        $this->csvFields = [
            'ENTEXAMYEAR'       => ['※年度' ,'数字4桁','4'],
            'EXAMNO'            => ['※登録番号','数字4桁','4'],
            //'STAFFCD'         => ['担当者CD','数字',''],
            'NAME'              => ['※氏名','全角40文字','40'],
            'FS_CD'             => ['出身校CD','数字7桁','7'],      // FS_CD  FINSCHOOLCD
            //'FS_AREA_CD'      => ['地区CD','数字4桁','4'],        // FS_AREA_CD  FINSCHOOL_DISTCD
            'SEQ001_REMARK1'    => ['試験区分CD','数字2桁(01～99)','2'],
            'SEQ002_REMARK1'    => ['相談コースCD','数字1桁(1～9)','1'],
            'SEQ003_REMARK1'    => ['共通テスト','5文字','5'],
            'SEQ004_REMARK2'    => ['特待記号CD','数字1桁(1～99)','2'],
            'SEQ004_REMARK1'    => ['特待理由CD','数字2桁(01～99)','2'],
            'SEQ006_REMARK1'    => ['備考','全角100文字','100'],
        ];

        // アップロードファイル
        $this->file = VARS::file("FILE");

        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "2";
        $this->maxSeq = 8;

        if (VARS::get("PROGRAMID")) {
            $this->examno = "";
            $this->finschoolcd = "";
            $this->name = "";
            $this->sort   = "";
        }

        $this->finschoolcd = VARS::post("FINSCHOOLCD");
        $this->name = VARS::post("NAME");

        if (VARS::get("EXAMNO")!="") {
            $this->examno   = VARS::get("EXAMNO");
        } else {
            $this->examno   = VARS::post("EXAMNO");
        }

        if (VARS::get("HID_SORT")!="") {
            $this->sort   = VARS::get("HID_SORT");
        } else {
            $this->sort   = VARS::post("SORT");
        }

        //行数の設定
        $this->gyoArray["SEQ006_REMARK1"] = 2;
        $this->gyoArray["CHANGE_TEXT"] = 2;

        //桁数の設定
        $this->ketaArray["EXAMNO"] = 4;
        $this->ketaArray["FINSCHOOLCD"] = 7;
        $this->ketaArray["NAME"] = 40;
        $this->ketaArray["SEQ003_REMARK1"] = 5;
        $this->ketaArray["SEQ006_REMARK1"] = 50;
        $this->ketaArray["SEQ007_REMARK1"] = 7;
        $this->ketaArray["SEQ008_REMARK1"] = 3;
        $this->ketaArray["SEQ008_REMARK2"] = 5;
        $this->ketaArray["CHANGE_TEXT"] = 50;

        //初回起動時のみ、更新対象項目の設定を行う
        if ($this->cmd == "") {
            $this->setUpdItemArray();
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //更新対象項目の設定
    public function setUpdItemArray()
    {
        //他画面からの呼び出し
        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgid = VARS::get("SEND_PRGID");
            $this->sendCmd   = VARS::get("SEND_CMD");
        } else {
            $this->sendPrgid = "KNJL671I";
            $this->sendCmd   = "updateAll";
        }

        //更新対象項目の設定 FINSCHOOL_DISTCD
        $this->itemArray = array("EXAMNO"       =>  "INTEGER",  //番号
                                 "FINSCHOOLCD"  =>  "INTEGER",  //出身学校
                                 "NAME"         =>  "TEXT",     //氏名
                                 "EXCLUSION"    =>  "CHECK",    //除外フラグ
                                 "SEQ001_REMARK1"   =>  "COMBO",    //詳細 試験区分
                                 "SEQ002_REMARK1"   =>  "COMBO",    //詳細 相談コース
                                 "SEQ003_REMARK1"   =>  "TEXT",     //詳細 共通テスト
                                 "SEQ004_REMARK1"   =>  "COMBO",    //詳細 特待 理由
                                 "SEQ004_REMARK2"   =>  "COMBO",    //詳細 特待 記号
                                 "SEQ005_REMARK1"   =>  "COMBO",    //詳細 部活動
                                 "SEQ006_REMARK1"   =>  "TEXTAREA", //詳細 備考
                                 "SEQ007_REMARK1"   =>  "INTEGER",  //詳細 塾
                                 "SEQ008_REMARK1"   =>  "TEXT",  //詳細 欠席 日数
                                 "SEQ008_REMARK2"   =>  "TEXT"   //詳細 欠席 理由書No.
                             );
        //詳細項目
        $this->updItemArray = array();
        if ($this->sendCmd == "insert" || $this->sendCmd == "update" || $this->sendCmd == "updateAll") {
            //更新or新規の場合、更新対象項目を設定
            foreach ($this->itemArray as $key => $val) {
                if (VARS::get($key) || $this->sendCmd == "insert" || $this->sendCmd == "updateAll") {
                    $this->updItemArray[$key] = $val; //渡された項目を更新対象とする
                }
            }
        }

        //新規の場合、一部項目は更新不可
        if ($this->sendCmd == "insert") {
            unset($this->updItemArray["SEQ005_REMARK1"]);    //部活動
            unset($this->updItemArray["SEQ007_REMARK1"]);    //塾
            unset($this->updItemArray["SEQ008_REMARK1"]);    //欠席 日数
            unset($this->updItemArray["SEQ008_REMARK2"]);    //欠席 理由書No.
            unset($this->updItemArray["EXCLUSION"]); //除外フラグ
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        $db = Query::dbCheckOut();
        //１レコード取得
        $query = knjl671iQuery::getSelectQuery($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->examno=="") {
                $this->setWarning("MSG308");
                return false;
            }

            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        //重複チェック
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        if ($kind=="update" || $kind=="insert") {
            //必須チェック
            if (trim($this->examno) == "") {
                $this->setWarning("MSG301", "(番号)");
                return false;
            }
            if ($this->sendCmd == "insert" || $this->sendCmd == "updateAll") {
                if (trim($this->field["NAME"]) == "") {
                    $this->setWarning("MSG301", "(氏名)");
                    return false;
                }
            }

            //桁数チェック
            $this->field["EXAMNO"]  = $this->examno;
            $this->ketaChkNameArray["EXAMNO"]         = "番号";
            $this->ketaChkNameArray["FINSCHOOLCD"]    = "出身学校コード";
            $this->ketaChkNameArray["NAME"]           = "氏名";
            $this->ketaChkNameArray["SEQ003_REMARK1"] = "共通テスト";
            $this->ketaChkNameArray["SEQ006_REMARK1"] = "備考";
            $this->ketaChkNameArray["SEQ007_REMARK1"] = "塾コード";
            $this->ketaChkNameArray["SEQ008_REMARK1"] = "欠席日数";
            $this->ketaChkNameArray["SEQ008_REMARK2"] = "理由書No";
            if ($this->sendCmd != "updateAll") {
                //桁数チェック用に、変更履歴を追加
                $this->ketaChkNameArray["CHANGE_TEXT"] = "備考（変更履歴）";
                $this->updItemArray["CHANGE_TEXT"] = "TEXTAREA";
            } else {
                //変更履歴を削除
                unset($this->updItemArray["CHANGE_TEXT"]);
            }
            foreach ($this->ketaArray as $key => $val) {
                if ($this->updItemArray[$key] == "TEXT" || $this->updItemArray[$key] == "TEXTAREA" || $this->updItemArray[$key] == "INTEGER") {
                    $byte = $val*3; //桁数 * バイト
                    $errMsg = "バイト";
                    if ($this->updItemArray[$key] == "TEXTAREA") {
                        $byte = $val*$this->gyoArray[$key]*3; //桁数 * 行数 * バイト
                    } elseif ($this->updItemArray[$key] == "INTEGER") {
                        $byte = $val; //桁数
                        $errMsg = "桁";
                    }
                    if (strlen($this->field[$key]) > $byte) {
                        $this->setWarning("MSG915", "(".($this->ketaChkNameArray[$key])."は".($byte).($errMsg)."までです。)");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        $kind = ($this->sendCmd == "insert") ? "insert" : "update";
        if ($this->validateInput($kind)) {
            $result = knjl671iQuery::getUpdateQuery($this);
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
                $this->histField = array(); //更新完了後、変更履歴用フィールドをクリア
            }
            return $result;
        }
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            if (!isset($this->examno)) {
                $this->setWarning("MSG303");
                return;
            }
            $result = knjl671iQuery::getDeleteQuery($this);
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
                $this->field = array();
            }
        }
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }

    // CSVヘッダー見本出力
    public function getHeaderCsvModel()
    {
        $contents = '';
        //ヘッダ取得
        $header = '';
        $sep='';
        foreach ($this->csvFields as $key => $val) {
            if ($header != '') {
                $sep = ',';
            }
            $header     .= $sep.$val[0];
            $contents   .= $sep.$val[1];
            ;
            $sep = ',';
        }
        $contents .= "\n";
        //ヘッダセット
        if ($this->field["HEADER"]) {
            $contents = $header."\n".$contents;
        }

        if ($contents) {
            $couse = $this->year."年度入試 中学生情報ヘッダー(見本)";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVファイル（ダウンロード）
    public function &getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        //ヘッダありの場合
        $header = '';
        if ($this->field["HEADER"]) {
            //ヘッダ取得
            $sep='';
            foreach ($this->csvFields as $key => $val) {
                if ($header != '') {
                    $sep = ',';
                }
                $header     .= $sep.$val[0];
                $sep = ',';
            }
            $header .= "\n";
        }

        $query = knjl671iQuery::getCsvData($this);
        $result = $db->query($query);

        $contents = '';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $sep = '';
            foreach ($this->csvFields as $key => $val) {
                $contents .= $sep . $row[$key];
                $sep = ',';
            }
            $contents .= "\n";
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($contents) {
            $contents = $header . $contents;
            $couse = $this->year."年度入試 中学生情報データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVファイル（取込）
    public function &getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();
        //データの整合性チェック
        if ($this->data_arr === null) {
            $this->setMessage(MSG202, "CSVの列数に誤りがあります");
        } elseif ($this->isDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $cnt = knjl671iQuery::insertQueryCsv($db, $this, $this->data_arr);
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '". $this->sendPrgid ."' ");
            Query::dbCheckIn($db);
            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query  = knjl671iQuery::deleteQueryErr($this);
            $result = $db->query($query);

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
                    if (get_count($tmp) !=  get_count($this->csvFields)) {
                        Query::dbCheckIn($db);
                        fclose($fp);
                        return null;
                    }

                    // data配列にセット
                    $data = [];
                    $tmpNo = 0;
                    foreach ($this->csvFields as $key => $val) {
                        $data[$key] = $tmp[$tmpNo];
                        $tmpNo++;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $this->csvFields, $line)) {
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
    public function errCheck($db, &$data, $checkdata, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $existenceErrFlg1   = false;    //存在エラーフラグ
        $existenceErrFlg2   = false;    //存在エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $existenceErrMsg1   = "";       //存在エラーメッセージ
        $existenceErrMsg2   = "";       //存在エラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $existenceSep1      = "";
        $existenceSep2      = "";
        $otherSep           = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            $arychkflg = false;
            if (
                in_array($key, array("ENTEXAMYEAR", "EXAMNO", "NAME"))
            ) {
                $arychkflg = true;
            }
            if ($arychkflg) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[0]);
                }
            }

            //数値チェック
            if (
                in_array($key, array("ENTEXAMYEAR", "FS_CD", "SEQ001_REMARK1", "SEQ002_REMARK1",
                                    "SEQ004_REMARK1", "SEQ004_REMARK2"))
            ) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[0]);
                }
            }

            // 年度チェック
            if ($key == "ENTEXAMYEAR") {
                if ($data[$key] != $this->year) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:年度は".$this->year."固定です", $otherSep, $val[0]);
                }
            }

            // 試験区分存在チェック
            if ($key == "SEQ001_REMARK1" && ($data[$key] != "")) {
                //チェック用にゼロ埋め
                if ($data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[2]}d", $data[$key]);
                }

                $query = knjl671iQuery::getEntexamTestdivMst($this, $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※試験区分存在チェック:", $existenceSep1, $data[$key]);
                }
            }

            // 相談コース
            if ($key == "SEQ002_REMARK1" && ($data[$key] != "")) {
                //チェック用にゼロ埋め
                if ($data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[2]}d", $data[$key]);
                }

                $query = knjl671iQuery::getEntexamGeneralMst($this, '02', $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※相談コースCD存在チェック:", $existenceSep1, $data[$key]);
                }
            }

            // 特待記号CD
            if ($key == "SEQ004_REMARK2" && ($data[$key] != "")) {
                // //チェック用にゼロ埋め
                // if ($data[$key] != "" && is_numeric($data[$key])) {
                //     $data[$key] = sprintf("%0{$val[2]}d", $data[$key]);
                // }
                // 特待記号CDは前ゼロしていない

                $query = knjl671iQuery::getEntexamGeneralMst($this, '04', $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※特待記号CD存在チェック:", $existenceSep1, $data[$key]);
                }
            }

            // 特待理由CD
            if ($key == "SEQ004_REMARK1" && ($data[$key] != "")) {
                //チェック用にゼロ埋め
                if ($data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[2]}d", $data[$key]);
                }

                $query = knjl671iQuery::getEntexamGeneralMst($this, '05', $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※特待理由CD存在チェック:", $existenceSep1, $data[$key]);
                }
            }

            //文字数チェック
            if (mb_strlen($data[$key]) > $val[2]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ（文字数）:", $lengthSep, $val[0]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg1.$existenceErrMsg2.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl671iQuery::insertQueryErr($db, $line, $check_error, $this);
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
        if (count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }

        return true;
    }

    // エラーデータダウンロード
    public function getErrorCsvDownloadModel()
    {
        //項目
        $header = array(
            "MSGROW"    => "行番号"
          , "MSGREMARK" => "メッセージ"
        );

        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $isData = false;
        //データ取得
        $query = knjl671iQuery::selectCsvMsgPrgDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $sep = '';
            // $row["DUMMY"] = "DUMMY";
            foreach ($header as $key => $val) {
                $data[] = $sep.$row[$key];
                $sep = ',';
            }
            $contents .= implode(",", $data) ."\n";
            $isData = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //エラーＣＳＶ出力
        if ($isData) {
            $fileName = "エラー出力.csv";
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
