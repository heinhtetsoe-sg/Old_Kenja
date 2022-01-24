<?php
//定数定義
class knjl610hModel extends Model
{
    public $field = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $examno;
    public $maxExamno;
    public $sort;

    public $nameKeta;
    public $nameKanaKeta;
    public $fsCdKeta;
    public $remark8Keta;
    public $remark9Keta;
    public $remark10Keta;

    public $hid_5kyokaseq;
    public $hid_3kyokaseq;

    //メンバ変数の初期化
    public function knjl610hModel()
    {
    }
    public function init()
    {
        $this->field = array("RECEPTDATE"           =>  VARS::post("RECEPTDATE"),
                             "DECLINE"              =>  VARS::post("DECLINE"),
                             "TESTDIV"              =>  VARS::post("TESTDIV"),
                             "SLIDE_FLG"            =>  VARS::post("SLIDE_FLG"),
                             "TESTDIV1"             =>  VARS::post("TESTDIV1"),
                             "NAME"                 =>  VARS::post("NAME"),
                             "NAME_KANA"            =>  VARS::post("NAME_KANA"),
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),
                             "SEX"                  =>  VARS::post("SEX"),
                             "FINSCHOOLCD"          =>  VARS::post("FINSCHOOLCD"),
                             "REMARK8"              =>  VARS::post("REMARK8"),
                             "REMARK9"              =>  VARS::post("REMARK9"),
                             "REMARK10"             =>  VARS::post("REMARK10"),
                             "JUDGEMENT"            =>  VARS::post("JUDGEMENT"),
                             "CONFIDENTIAL_RPT01"   =>  VARS::post("CONFIDENTIAL_RPT01"),
                             "CONFIDENTIAL_RPT02"   =>  VARS::post("CONFIDENTIAL_RPT02"),
                             "CONFIDENTIAL_RPT03"   =>  VARS::post("CONFIDENTIAL_RPT03"),
                             "CONFIDENTIAL_RPT04"   =>  VARS::post("CONFIDENTIAL_RPT04"),
                             "CONFIDENTIAL_RPT05"   =>  VARS::post("CONFIDENTIAL_RPT05"),
                             "TOTAL_ALL"            =>  VARS::post("TOTAL_ALL"),
                             "OUTPUT"               =>  VARS::post("OUTPUT"),
                             "HEADER"               =>  VARS::post("HEADER")
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "2";

        if (VARS::get("PROGRAMID")) {
            $this->examno = "";
            $this->sort   = "";
        }
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

        //登録番号の最大取得
        $db = Query::dbCheckOut();
        $query = knjl610hQuery::getMaxExamno($this, $this->year);
        $this->maxExamno = $db->getOne($query);
        $this->maxExamno = $this->maxExamno + 1;
        Query::dbCheckIn($db);

        //桁数の設定
        $this->nameKeta = 40;
        $this->nameKanaKeta = 80;
        $this->fsCdKeta = 7;
        $this->remark8Keta = 80;
        $this->remark9Keta = 80;
        $this->remark10Keta = 80;

        //合計の対象教科を取得
        $db = Query::dbCheckOut();
        $arr_5kyokaseq = array();
        $arr_3kyokaseq = array();
        $query = knjl610hQuery::getEntexamSettingMst($this, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            //5教科
            if ($row["NAMESPARE1"] == "1") {
                $arr_5kyokaseq[] = $row["SEQ"];
            }
            //3教科
            if ($row["NAMESPARE3"] == "1") {
                $arr_3kyokaseq[] = $row["SEQ"];
            }
        }
        $result->free();
        Query::dbCheckIn($db);
        $this->hid_5kyokaseq = implode(",", $arr_5kyokaseq); //5教科コード配列
        $this->hid_3kyokaseq = implode(",", $arr_3kyokaseq); //3教科コード配列

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    public function getHeaderCsvModel()
    {
        $data = false;

        $db = Query::dbCheckOut();

        /* 関数 */
        $getMstString = function ($db, $query) {
            $rtnStr = "";
            $sep = "";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rtnStr .= $sep.$row["LABEL"];
                $sep = " ";
            }

            return $rtnStr;
        };
        
        //「類別」例取得
        $query = knjl610hQuery::getEntexamClassifyMst($this);
        $entexamClassifyMstStr = $getMstString($db, $query);

        //「入試日程」例取得
        $query = knjl610hQuery::getTestDiv($this);
        $testdivMstStr = $getMstString($db, $query);

        Query::dbCheckIn($db);

        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        $csvItem = array("ENTEXAMYEAR"              =>  array("※年度",'数字4桁'),
                         "EXAMNO"                   =>  array("登録番号",'数字4桁'),
                         "TESTDIV1"                 =>  array("※類別",$entexamClassifyMstStr),
                         "SEX"                      =>  array("※性別",'1:男 2:女'),
                         "NAME"                     =>  array("※氏名",'全角40文字'),
                         "NAME_KANA"                =>  array("※氏名かな",'全角80文字'),
                         "BIRTHDAY"                 =>  array("※生年月日",'yyyy-mm-dd'),
                         "FINSCHOOLCD"              =>  array("※出身学校コード",'数字7桁'),
                         "FINSCHOOL_NAME"           =>  array("出身学校名",'全角25文字'),
                         "CONFIDENTIAL_RPT01"       =>  array("教科1",'数字1桁（1～5）'),
                         "CONFIDENTIAL_RPT02"       =>  array("教科2",'数字1桁（1～5）'),
                         "CONFIDENTIAL_RPT03"       =>  array("教科3",'数字1桁（1～5）'),
                         "CONFIDENTIAL_RPT04"       =>  array("教科4",'数字1桁（1～5）'),
                         "CONFIDENTIAL_RPT05"       =>  array("教科5",'数字1桁（1～5）'),
                         "TOTAL_ALL"                =>  array("9教科合計",'数字2桁以内'),
                         "REMARK8"                  =>  array("資格",'全角80文字'),
                         "REMARK9"                  =>  array("併願校",'全角80文字'),
                         "REMARK10"                 =>  array("備考",'全角80文字'),
                         "TESTDIV"                  =>  array("※入試日程",$testdivMstStr),
                         "RECEPTDATE"               =>  array("※受付年月日",'yyyy-mm-dd'),
                         "SLIDE_FLG"                =>  array("チャレンジ受験する",'1:チャレンジする')
                        );

        $contents = "";
        //ヘッダ取得
        $header = "";
        $sep='';
        foreach ($csvItem as $key => $val) {
            $header     .= ($header == "") ? $val[0] : ",".$val[0];
            $contents   .= $sep.$csvItem[$key][1];
            $sep = ',';
            $data = true;
        }
        $contents .= "\n";
        //ヘッダセット
        if ($this->field["HEADER"]) {
            $contents = $header."\n".$contents;
        }

        if ($data) {
            $couse = $this->year."年度入試 受験希望者データ(見本)";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVファイル（書出）
    public function getDownloadCsvModel()
    {
        $data = false;
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
            $csvItem = array("ENTEXAMYEAR"              =>  array("※年度"),
                             "EXAMNO"                   =>  array("登録番号"),
                             "TESTDIV1"                 =>  array("※類別"),
                             "SEX"                      =>  array("※性別"),
                             "NAME"                     =>  array("※氏名"),
                             "NAME_KANA"                =>  array("※氏名かな"),
                             "BIRTHDAY"                 =>  array("※生年月日"),
                             "FINSCHOOLCD"              =>  array("※出身学校コード"),
                             "FINSCHOOL_NAME"           =>  array("出身学校名"),
                             "CONFIDENTIAL_RPT01"       =>  array("教科1"),
                             "CONFIDENTIAL_RPT02"       =>  array("教科2"),
                             "CONFIDENTIAL_RPT03"       =>  array("教科3"),
                             "CONFIDENTIAL_RPT04"       =>  array("教科4"),
                             "CONFIDENTIAL_RPT05"       =>  array("教科5"),
                             "TOTAL_ALL"                =>  array("9教科合計"),
                             "REMARK8"                  =>  array("資格"),
                             "REMARK9"                  =>  array("併願校"),
                             "REMARK10"                 =>  array("備考"),
                             "TESTDIV"                  =>  array("※入試日程"),
                             "RECEPTDATE"               =>  array("※受付年月日"),
                             "SLIDE_FLG"                =>  array("チャレンジ受験する")
                            );

            $contents = "";
            //ヘッダ取得
            $header = "";
            foreach ($csvItem as $key => $val) {
                $header     .= ($header == "") ? $val[0] : ",".$val[0];
            }
            //ヘッダセット
            if ($this->field["HEADER"]) {
                $contents .= $header."\n";
            }
            $query = knjl610hQuery::getCsvData($this);
        }

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            if ($this->field["OUTPUT"] == "3") {
                $cnt = 0;
                foreach ($csvItem as $key => $val) {
                    $contents .= ($cnt == 0) ? $row[$key] : ",".$row[$key];
                    $cnt++;
                }
            } else {
                $contents .= implode($row, ",");
            }

            $contents .= "\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $couse = $this->year."年度入試 受験希望者データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVファイル（取込）
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();
        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $cnt = knjl610hQuery::insertQueryCsv($db, $this, $this->data_arr);
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
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
            $checkdata = array("ENTEXAMYEAR"                =>  array(4,   "年度"),
                               "EXAMNO"                     =>  array(4,   "登録番号"),
                               "TESTDIV1"                   =>  array(2,   "類別"),
                               "SEX"                        =>  array(1,   "性別"),
                               "NAME"                       =>  array(120,  "氏名"),
                               "NAME_KANA"                  =>  array(240, "氏名かな"),
                               "BIRTHDAY"                   =>  array(10,  "生年月日"),
                               "FINSCHOOLCD"                =>  array(7,   "出身学校コード"),
                               "FINSCHOOL_NAME"             =>  array(999, "出身学校名"),
                               "CONFIDENTIAL_RPT01"         =>  array(1,   "教科1"),
                               "CONFIDENTIAL_RPT02"         =>  array(1,   "教科2"),
                               "CONFIDENTIAL_RPT03"         =>  array(1,   "教科3"),
                               "CONFIDENTIAL_RPT04"         =>  array(1,   "教科4"),
                               "CONFIDENTIAL_RPT05"         =>  array(1,   "教科5"),
                               "TOTAL_ALL"                  =>  array(2,   "9教科合計"),
                               "REMARK8"                    =>  array(240, "資格"),
                               "REMARK9"                    =>  array(240, "併願校"),
                               "REMARK10"                   =>  array(240, "備考"),
                               "TESTDIV"                    =>  array(2,   "入試日程"),
                               "RECEPTDATE"                 =>  array(10,  "受付年月日"),
                               "SLIDE_FLG"                  =>  array(1,   "チャレンジ受験する")
                            );
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query  = knjl610hQuery::deleteQueryErr();
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
                    $data = array("ENTEXAMYEAR"         => $tmp[0],
                                  "EXAMNO"              => $tmp[1],
                                  "TESTDIV1"            => $tmp[2],
                                  "SEX"                 => $tmp[3],
                                  "NAME"                => $tmp[4],
                                  "NAME_KANA"           => $tmp[5],
                                  "BIRTHDAY"            => $tmp[6],
                                  "FINSCHOOLCD"         => $tmp[7],
                                  "FINSCHOOL_NAME"      => $tmp[8],
                                  "CONFIDENTIAL_RPT01"  => $tmp[9],
                                  "CONFIDENTIAL_RPT02"  => $tmp[10],
                                  "CONFIDENTIAL_RPT03"  => $tmp[11],
                                  "CONFIDENTIAL_RPT04"  => $tmp[12],
                                  "CONFIDENTIAL_RPT05"  => $tmp[13],
                                  "TOTAL_ALL"           => $tmp[14],
                                  "REMARK8"             => $tmp[15],
                                  "REMARK9"             => $tmp[16],
                                  "REMARK10"            => $tmp[17],
                                  "TESTDIV"             => $tmp[18],
                                  "RECEPTDATE"          => $tmp[19],
                                  "SLIDE_FLG"           => $tmp[20]
                                  );
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
                            //ゼロ埋めしてセット
                            if (in_array($key, array("TESTDIV1", "TESTDIV")) && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                            }
                        }

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
    public function errCheck($db, $data, $checkdata, $line)
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

        $checkNameMstCd = array("SEX"   => "Z002");

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            $arychkflg = false;
            if (
                in_array($key, array("ENTEXAMYEAR", "EXAMNO", "TESTDIV1", "SEX", "NAME", "NAME_KANA",
                                    "BIRTHDAY", "FINSCHOOLCD","TESTDIV", "RECEPTDATE"))
            ) {
                $arychkflg = true;
            }
            if ($arychkflg) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (
                in_array($key, array("ENTEXAMYEAR", "FINSCHOOLCD", "CONFIDENTIAL_RPT01", "CONFIDENTIAL_RPT02",
                                    "CONFIDENTIAL_RPT03", "CONFIDENTIAL_RPT04", "CONFIDENTIAL_RPT05", "TOTAL_ALL", "SLIDE_FLG"))
            ) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //存在チェック（類別マスタ）
            if (in_array($key, array("TESTDIV1"))) {
                //チェック用にゼロ埋め
                if ($data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }

                $query = knjl610hQuery::getEntexamClassifyMst($this, $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※存在(類別マスタ):", $existenceSep1, $data[$key]);
                }
            }

            //存在チェック（名称マスタ）
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl610hQuery::getNameMst($this, $checkNameMstCd[$key], $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※存在(名称マスタ):", $existenceSep1, $data[$key]);
                }
            }

            //存在チェック（入試日程マスタ）
            if (in_array($key, array("TESTDIV"))) {
                //チェック用にゼロ埋め
                if ($data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }

                $query = knjl610hQuery::getTestDiv($this, $data[$key]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※存在(入試日程マスタ):", $existenceSep1, $data[$key]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg1.$existenceErrMsg2.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl610hQuery::insertQueryErr($db, $line, $check_error);
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

    //入力項目チェック
    public function validateInput($kind)
    {
        $db = Query::dbCheckOut();
        //１レコード取得
        $query = knjl610hQuery::getRow($this);
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
            //新規ボタンを押されていない
            if ($this->examno == "" && $kind == "insert") {
                $this->setWarning("追加は新規ボタンを押下してから行ってください。");
                return false;
            }

            //必須チェック
            if (trim($this->field["RECEPTDATE"]) == "") {
                $this->setWarning("MSG301", "(受付日)");
                return false;
            }
            if (trim($this->field["TESTDIV1"]) == "") {
                $this->setWarning("MSG301", "(類別)");
                return false;
            }
            if (trim($this->field["NAME"]) == "") {
                $this->setWarning("MSG301", "(氏名)");
                return false;
            }
            if (trim($this->field["NAME_KANA"]) == "") {
                $this->setWarning("MSG301", "(氏名かな)");
                return false;
            }
            if (trim($this->field["BIRTHDAY"]) == "") {
                $this->setWarning("MSG301", "(生年月日)");
                return false;
            }
            if (trim($this->field["SEX"]) == "") {
                $this->setWarning("MSG301", "(性別)");
                return false;
            }
            if (trim($this->field["FINSCHOOLCD"]) == "") {
                $this->setWarning("MSG301", "(出身学校)");
                return false;
            }
            if (trim($this->field["TESTDIV"]) == "") {
                $this->setWarning("MSG301", "(入試日程)");
                return false;
            }

            //桁数チェック
            if (strlen($this->field["NAME"]) > $this->nameKeta*3) {
                $this->setWarning("MSG915", "(氏名は".($this->nameKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["NAME_KANA"]) > $this->nameKanaKeta*3) {
                $this->setWarning("MSG915", "(氏名かなは".($this->nameKanaKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["FINSCHOOLCD"]) > $this->fsCdKeta*3) {
                $this->setWarning("MSG915", "(出身学校コードは".($this->fsCdKeta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REMARK8"]) > $this->remark8Keta*3) {
                $this->setWarning("MSG915", "(資格は".($this->remark8Keta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REMARK9"]) > $this->remark9Keta*3) {
                $this->setWarning("MSG915", "(併願校は".($this->remark9Keta*3)."バイトまでです。)");
                return false;
            }
            if (strlen($this->field["REMARK10"]) > $this->remark10Keta*3) {
                $this->setWarning("MSG915", "(備考は".($this->remark10Keta*3)."バイトまでです。)");
                return false;
            }

            $kyoka5_array = explode(",", $this->hid_5kyokaseq);
            $kyoka3_array = explode(",", $this->hid_3kyokaseq);
            $total5 = 0;
            foreach ($kyoka5_array as $kyokaseq) {
                $total5 += (int)VARS::post("CONFIDENTIAL_RPT".$kyokaseq);
            }
            $total3 = 0;
            foreach ($kyoka3_array as $kyokaseq) {
                $total3 += (int)VARS::post("CONFIDENTIAL_RPT".$kyokaseq);
            }
            $this->field["TOTAL5"] = $total5;
            $this->field["TOTAL3"] = $total3;
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    public function &delete()
    {
        if (!isset($this->examno)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjl610hQuery::getDeleteQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    public function &insert()
    {
        $result = knjl610hQuery::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            //実行後の再描画で必要
            $this->examno = sprintf("%04d", $this->maxExamno);
            $this->maxExamno = "";
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl610hQuery::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    public function clean()
    {
    }
}
