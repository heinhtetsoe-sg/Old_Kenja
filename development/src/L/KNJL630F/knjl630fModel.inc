<?php
class knjl630fModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $testdiv;
    public $file;

    public function knjl630fModel()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : "1";
        // APPLICANTDIV が 1:中学 なら TESTDIV は 2:中学内部入試
        // APPLICANTDIV が 2:高校 なら TESTDIV は 6:高校内部入試
        $this->testdiv = $this->applicantdiv == "1" ? "2" : "6";

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //実行種別
                             "RECEPTDATE"       =>  VARS::post("RECEPTDATE"),       //受付日付
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                        );

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");        //ファイルのアップロード
            $this->file["name"] = i18n_convert($this->file["name"], "UTF-8", "SJIS-win");
        }
    }

    public function clean()
    {
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        if ($this->validateInput()) {
            //ファイルの文字列の取得
            $data_arr = array();
            $this->err_count = 0;
            $data_arr = $this->getFileString();

            //データの整合性チェック
            if ($this->isDataMatched($data_arr)) {
                $cnt = knjl630fQuery::updateQueryCsv($this, $data_arr);

                $this->setMessage("MSG202", "処理済件数　：" . $cnt . "件\\nエラー件数    ：" . $this->err_count."件\\n" . $erraDatas);
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
            $checkdata = array();
            $checkdata["SCHREGNO"]           = array(8,  "HISSU", "INT",  "学籍番号");
            $checkdata["NAME"]               = array(20, "HISSU", "CHAR", "氏名");
            $checkdata["RECEPTNO"]           = array(4,  "HISSU", "INT",  "受験番号");
            if ($this->applicantdiv == "2") {
                $checkdata["COURSE_J"]  = array(4,  "HISSU", "INT",  "中３コース");
            }
            $checkdata["EXAMCOURSE"]         = array(4,  "HISSU", "INT",  "第一志望");
            $checkdata["EXAMCOURSE2"]        = array(4,  "",      "INT",  "第二志望");
            if ($this->applicantdiv == "1") {
                $checkdata["EXAM_TYPE"]          = array(1,  "",      "INT",  "受験科目数");
            } else {
                $checkdata["EXAM_TYPE"]          = array(1,  "HISSU", "INT",  "受験科目数");
            }
            $checkdata["EIKEN_SHUTOKU_KYUU"] = array(1,  "",      "INT",  "英語見なし得点");
            $checkdata["SHOUGAKU"]           = array(2,  "",      "INT",  "奨学区分");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl630fQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    $cnt = 0;
                    $data = array();
                    $data["SCHREGNO"]           = $tmp[$cnt++];
                    $data["GRADE"]              = $tmp[$cnt++];
                    $data["ATTENDANCE_NUMBER"]  = $tmp[$cnt++];
                    $data["NAME"]               = $tmp[$cnt++];
                    if ($this->applicantdiv == "2") {
                        $data["COURSE_J"]       = $tmp[$cnt++];
                    }
                    $data["RECEPTNO"]           = $tmp[$cnt++];
                    $data["EXAMCOURSE"]         = $tmp[$cnt++];
                    $data["EXAMCOURSE2"]        = $tmp[$cnt++];
                    $data["EXAM_TYPE"]          = $tmp[$cnt++];
                    $data["EIKEN_SHUTOKU_KYUU"] = $tmp[$cnt++];
                    $data["SHOUGAKU"]           = $tmp[$cnt++];

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $intaviewList)) {
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
    public function errCheck($db, $data, $data_each, $line, $intaviewList)
    {
        $errMessage = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (strlen($errMessage) == 0 && $val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $errMessage = "※必須：".$val[3];
            }

            //数値チェック
            if (strlen($errMessage) == 0 && ($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $errMessage = "※数値：".$val[3];
            }

            //文字数チェック
            if (strlen($errMessage) == 0 && $val[2] == "CHAR" && $val[0] != "" && mb_strlen($data[$key]) > $val[0]) {
                $errMessage = "※サイズ：".$val[3];
            }

            //学籍番号
            if (strlen($errMessage) == 0 && $key == "SCHREGNO" && $data[$key] != "") {
                $query = knjl630fQuery::checkSchreg($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = "学籍基礎未登録の学籍番号";
                }
            }

            //名前
            if (strlen($errMessage) == 0 && $key == "NAME" && $data[$key] != "") {
                $query = knjl630fQuery::checkSchreg($this, $data["SCHREGNO"], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = "氏名が学籍基礎データと一致しません";
                }
            }

            //中３コース
            if (strlen($errMessage) == 0 && $key == "COURSE_J" && $data[$key] != "") {
                $query = knjl630fQuery::checkSchregCourse($data["SCHREGNO"], CTRL_YEAR, CTRL_SEMESTER, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = $val[3]."コード間違い";
                }
            }

            //第一志望
            if (strlen($errMessage) == 0 && $key == "EXAMCOURSE" && $data[$key] != "") {
                $query = knjl630fQuery::checkExamCourse($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = $val[3]."コード間違い";
                }
            }

            //第二志望
            if (strlen($errMessage) == 0 && $key == "EXAMCOURSE2" && $data[$key] != "") {
                $query = knjl630fQuery::checkExamCourse($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = $val[3]."コード間違い";
                }
            }

            //受験科目数
            if (strlen($errMessage) == 0 && $key == "EXAM_TYPE" && $data[$key] != "") {
                $query = knjl630fQuery::checkExamType($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = $val[3]."コード間違い";
                }
            }

            //受験番号の範囲チェック
            if (strlen($errMessage) == 0 && $key == "RECEPTNO" && $data[$key] != "") {
                $query = knjl630fQuery::checkReceptNo($this, $data["EXAM_TYPE"], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = "受験番号が受験型に一致していない";
                }
            }

            //英語見なし得点
            if (strlen($errMessage) == 0 && $key == "EIKEN_SHUTOKU_KYUU" && $data[$key] != "") {
                $query = knjl630fQuery::checkEiken($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = $val[3]."コード間違い";
                }
            }

            //奨学区分
            if (strlen($errMessage) == 0 && $key == "SHOUGAKU" && $data[$key] != "") {
                $query = knjl630fQuery::checkShougaku($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $errMessage = $val[3]."コード間違い";
                }
            }
        }

        //取り込み処理（エラーの場合、エラーＤＢへの追加）
        $returnFlg = strlen($errMessage) == 0 ? true : false;
        if (!$returnFlg) {
            $this->err_count++;
            knjl630fQuery::insertQueryErr($db, $line, $errMessage);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //１万件を超えたら制限エラー
        if (count($data_arr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：" . count($data_arr) . "件");
            return false;
        }
        return true;
    }

    //ヘッダ出力
    public function getHeaderModel()
    {
        $header = array();
        $header += array("SCHREGNO"           => "学籍番号");
        $header += array("GRADE"              => "年組名称");
        $header += array("ATTENDANCE_NUMBER"  => "出席番号");
        $header += array("NAME"               => "氏名");
        if ($this->applicantdiv == "2") {
            $header += array("COURSE_J"           => "中３コース");
        }
        $header += array("RECEPTNO"           => "受験番号");
        $header += array("EXAMCOURSE"         => "第一志望");
        $header += array("EXAMCOURSE2"        => "第二志望");
        $header += array("EXAM_TYPE"          => "受験科目数");
        $header += array("EIKEN_SHUTOKU_KYUU" => "英語見なし得点");
        $header += array("SHOUGAKU"           => "奨学区分");

        $header_rei = array();
        if ($this->applicantdiv == "1") {
            $header_rei += array("SCHREGNO"           => "00145018");
            $header_rei += array("GRADE"              => "6年A組");
            $header_rei += array("ATTENDANCE_NUMBER"  => "013");
            $header_rei += array("NAME"               => "橋本　次郎");
            $header_rei += array("RECEPTNO"           => "1008");
            $header_rei += array("EXAMCOURSE"         => "1001");
            $header_rei += array("EXAMCOURSE2"        => "1002");
            $header_rei += array("EXAM_TYPE"          => "2");
            $header_rei += array("EIKEN_SHUTOKU_KYUU" => "5");
            $header_rei += array("SHOUGAKU"           => "");
        } else {
            $header_rei += array("SCHREGNO"           => "00174022");
            $header_rei += array("GRADE"              => "中学3年B組");
            $header_rei += array("ATTENDANCE_NUMBER"  => "013");
            $header_rei += array("NAME"               => "橋本　太郎");
            $header_rei += array("COURSE_J"           => "1001");
            $header_rei += array("RECEPTNO"           => "0023");
            $header_rei += array("EXAMCOURSE"         => "2002");
            $header_rei += array("EXAMCOURSE2"        => "2004");
            $header_rei += array("EXAM_TYPE"          => "3");
            $header_rei += array("EIKEN_SHUTOKU_KYUU" => "4");
            $header_rei += array("SHOUGAKU"           => "");
        }

        $contents  = implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        $header_data = true;

        $couse = "出願データ";
        $filename = sprintf("%s.csv", $couse);
        common::downloadContents($contents, $filename);

        return true;
    }

    //ＣＳＶへエラー出力
    public function getCsvModel()
    {
        $header = array("MSGROW"     => "行番号",
                        "MSGREMARK"  => "メッセージ");

        $contents  = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjl630fQuery::selectCsvQuery();
        $result = $db->query($query);
        $err_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($err_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function validateInput()
    {
        if (!($this->field["APPLICANTDIV"])) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }

        if (!($this->field["TESTDIV"])) {
            $this->setWarning("MSG301", "( 入試区分 )");
            return false;
        }

        if (!($this->field["RECEPTDATE"])) {
            $this->setWarning("MSG301", "( 受付日付 )");
            return false;
        }

        return true;
    }
}
