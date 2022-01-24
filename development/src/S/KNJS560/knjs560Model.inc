<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjs560Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjs560Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "FACCD"            =>  VARS::post("FACCD"),            //施設一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                $errFlg = $this->checkCsvErrQuery(PROGRAMID);
                if ($errFlg) {
                    knjs560Query::delInsQueryCsv($this, $this->data_arr);
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
                return true;
            } else {
                $this->getDownloadModel();
                return true;
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("YEAR"                   => array(4, "年度"),
                                   "GRADE"                  => array(2, "学年"),
                                   "SUBCLASSCD"             => array(13, "教科コード-学校校種-教育課程コード-科目コード"), //99-P-2-999999の形
                                   "ISSUECOMPANYCD"         => array(4, "出版社（発行者）コード"),
                                   "DIV"                    => array(1, "種類"),
                                   "ALLOTMENT_MONTH"        => array(2, "配当月"),
                                   "UNIT_L_NAME"            => array(90, "大単元"),
                                   "UNIT_M_NAME"            => array(90, "中単元"),
                                   "UNIT_S_NAME"            => array(90, "小単元"),
                                   "ALLOTMENT_TIME"         => array(2, "配当時間"),
                                   "UNIT_ARRIVAL_TARGET"    => array(450, "単元到達目標"),
                                   "UNIT_DIV"               => array(1, "テスト区分"));
            } else {
                $checkdata = array("YEAR"                   => array(4, "年度"),
                                   "GRADE"                  => array(2, "学年"),
                                   "SUBCLASSCD"             => array(6, "科目コード"),
                                   "ISSUECOMPANYCD"         => array(4, "出版社（発行者）コード"),
                                   "DIV"                    => array(1, "種類"),
                                   "ALLOTMENT_MONTH"        => array(2, "配当月"),
                                   "UNIT_L_NAME"            => array(90, "大単元"),
                                   "UNIT_M_NAME"            => array(90, "中単元"),
                                   "UNIT_S_NAME"            => array(90, "小単元"),
                                   "ALLOTMENT_TIME"         => array(2, "配当時間"),
                                   "UNIT_ARRIVAL_TARGET"    => array(450, "単元到達目標"),
                                   "UNIT_DIV"               => array(1, "テスト区分"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $line = 1;         //行番号(初期値)
            $dataCntErr = false;

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            $firstRow = array();
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $line < 2) {
                        $line++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    if ($this->field["SHORI_MEI"] == "1") {
                        $data = array("YEAR"                => $tmp[0],
                                      "GRADE"               => $tmp[1],
                                      "SUBCLASSCD"          => $tmp[2],
                                      "ISSUECOMPANYCD"      => $tmp[3],
                                      "SYUPPAN"             => $tmp[5],
                                      "DIV"                 => $tmp[6],
                                      "ALLOTMENT_MONTH"     => $tmp[7],
                                      "L_TITOL"             => $tmp[8],
                                      "UNIT_L_NAME"         => $tmp[9],
                                      "UNIT_M_NAME"         => $tmp[10],
                                      "UNIT_S_NAME"         => $tmp[11],
                                      "UNIT_DATA"           => $tmp[12],
                                      "ALLOTMENT_TIME"      => $tmp[13],
                                      "UNIT_ARRIVAL_TARGET" => $tmp[14],
                                      "UNIT_DIV"            => $tmp[15],
                                      "TEXT_PAGE"           => $tmp[16]);
                        //エラーチェック
                        if ($this->errCheck($db, $data, $checkdata, $line)) {
                            $temp_arr[] = $data;
                        }
                    } else {
                        $data = array("SENSEI"              => $tmp[0],
                                      "GRADE"               => (strlen(trim($tmp[1]))) ? sprintf("%02d", $tmp[1]) : "",
                                      "HENSEI"              => $tmp[2],
                                      "KURASU"              => $tmp[3],
                                      "KYOUKA"              => $tmp[4],
                                      "SYUPPAN"             => $tmp[5],
                                      "DIV"                 => $tmp[6],
                                      "ALLOTMENT_MONTH"     => $tmp[7],
                                      "L_TITOL"             => $tmp[8],
                                      "UNIT_L_NAME"         => $tmp[9],
                                      "UNIT_M_NAME"         => $tmp[10],
                                      "UNIT_S_NAME"         => $tmp[11],
                                      "UNIT_DATA"           => $tmp[12],
                                      "ALLOTMENT_TIME"      => $tmp[13],
                                      "UNIT_ARRIVAL_TARGET" => $tmp[14],
                                      "UNIT_DIV"            => $tmp[15],
                                      "TEXT_PAGE"           => $tmp[16]);
                        $temp_arr[] = $data;
                    }
                    if (get_count($firstRow) < 1) {
                        $firstRow = $data;
                    }

                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        //CSVデータすべての行がエラーになった場合、フォーマットチェックできるため
        if (get_count($temp_arr) < 1) {
            $temp_arr[] = $firstRow;
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            $hissu = array("YEAR","GRADE","SUBCLASSCD","ISSUECOMPANYCD","ALLOTMENT_MONTH","UNIT_L_NAME","ALLOTMENT_TIME","UNIT_DIV");
            if (in_array($key, $hissu)) {
                if (!strlen($data[$key])) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                //数値チェック
                if (in_array($key, array("YEAR", "GRADE", "ISSUECOMPANYCD", "ALLOTMENT_MONTH", "ALLOTMENT_TIME", "UNIT_DIV"))
                ) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                //数値チェック
                if (in_array($key, array("YEAR", "GRADE", "SUBCLASSCD", "ISSUECOMPANYCD", "ALLOTMENT_MONTH", "ALLOTMENT_TIME", "UNIT_DIV"))
                ) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjs560Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //1万件を超えたら制限エラー
        if (get_count($data_arr) > 10000) {
            $this->setWarning(MSG915, "処理件数を１万件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        if (get_count($data_arr[0]) < 16) {
            $this->message = "ファイルフォーマットが不正です。";
            return false;
        }

        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array("YEAR"                => "※年度",
                        "GRADE"               => "※学年",
                        "SUBCLASSCD"          => "※教科コード",
                        "ISSUECOMPANYCD"      => "※出版社（発行者）コード",
                        "SUBCLASSNAME"        => "教科名",
                        "SYUPPAN"             => "出版社",
                        "DIV"                 => "種類",
                        "ALLOTMENT_MONTH"     => "配当月",
                        "L_TITOL"             => "大単元表題",
                        "UNIT_L_NAME"         => "大単元",
                        "UNIT_M_NAME"         => "中単元",
                        "UNIT_S_NAME"         => "小単元",
                        "UNIT_DATA"           => "項目",
                        "ALLOTMENT_TIME"      => "配当時間",
                        "UNIT_ARRIVAL_TARGET" => "単元到達目標",
                        "UNIT_DIV"            => "テスト区分",
                        "TEXT_PAGE"           => "教科書ページ",
                        $this->lastColumn     => $this->lastColumn);
                        
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header_rei = array("YEAR"                => "9999",
                                "GRADE"               => "99",
                                "SUBCLASSCD"          => "99-P-2-999999",
                                "ISSUECOMPANYCD"      => "9999",
                                "SUBCLASSNAME"        => "全角5文字",
                                "SYUPPAN"             => "全角15文字",
                                "DIV"                 => "半角1文字",
                                "ALLOTMENT_MONTH"     => "99",
                                "L_TITOL"             => "99",
                                "UNIT_L_NAME"         => "全角30文字",
                                "UNIT_M_NAME"         => "全角30文字",
                                "UNIT_S_NAME"         => "全角30文字",
                                "UNIT_DATA"           => "全角30文字",
                                "ALLOTMENT_TIME"      => "99",
                                "UNIT_ARRIVAL_TARGET" => "全角150文字",
                                "UNIT_DIV"            => "9",
                                "TEXT_PAGE"           => "9999",
                                $this->lastColumn     => $this->lastColumn);
        } else {
            $header_rei = array("YEAR"                => "9999",
                                "GRADE"               => "99",
                                "SUBCLASSCD"          => "999999",
                                "ISSUECOMPANYCD"      => "9999",
                                "SUBCLASSNAME"        => "全角5文字",
                                "SYUPPAN"             => "全角15文字",
                                "DIV"                 => "半角1文字",
                                "ALLOTMENT_MONTH"     => "99",
                                "L_TITOL"             => "99",
                                "UNIT_L_NAME"         => "全角30文字",
                                "UNIT_M_NAME"         => "全角30文字",
                                "UNIT_S_NAME"         => "全角30文字",
                                "UNIT_DATA"           => "全角30文字",
                                "ALLOTMENT_TIME"      => "99",
                                "UNIT_ARRIVAL_TARGET" => "全角150文字",
                                "UNIT_DIV"            => "9",
                                "TEXT_PAGE"           => "9999",
                                $this->lastColumn     => $this->lastColumn);
        }

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {       //エラー
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "2") {       //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"] == "1") {
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->field["OUTPUT"] == "3") {
                $query = $this->selectCsvErrQuery(PROGRAMID);
                //SQL文発行
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {              //データ
                        $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $row["REGISTERCD"] = $row["UPDATED"] = "";
                        $row["REGISTERCD"] = $this->lastColumn;
                    }
                    $contents .= implode($row, ",") ."\n";
                    $header_data = true;
                }
                $result->free();
            } else {
                foreach ($this->data_arr as $key => $val) {
                    $header_data = true;

                    $setVal = str_replace(",", ".", $val);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $setVal = str_replace("\r", "", $setVal);
                    $setVal = str_replace("\n", "", $setVal);

                    $query = knjs560Query::getSubclassCd($this, $setVal["KYOUKA"]);
                    $subclassCd = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $setSubCd = $subclassCd["MAX_CD"] == $subclassCd["MIN_CD"] ? $subclassCd["MAX_CD"] : "";

                    $query = knjs560Query::getIssuecompanyCd($this, $setVal["SYUPPAN"]);
                    $issuecompanyCd = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    $setIssueCd = $issuecompanyCd["MAX_CD"] == $issuecompanyCd["MIN_CD"] ? $issuecompanyCd["MAX_CD"] : "";
                    
                    $contents .= CTRL_YEAR.",";
                    $contents .= $setVal["GRADE"].",";
                    $contents .= $setSubCd.",";
                    $contents .= $setIssueCd.",";
                    $contents .= $setVal["KYOUKA"].",";
                    $contents .= $setVal["SYUPPAN"].",";
                    $contents .= $setVal["DIV"].",";
                    $contents .= $setVal["ALLOTMENT_MONTH"].",";
                    $contents .= $setVal["L_TITOL"].",";
                    $contents .= $setVal["UNIT_L_NAME"].",";
                    $contents .= $setVal["UNIT_M_NAME"].",";
                    $contents .= $setVal["UNIT_S_NAME"].",";
                    $contents .= $setVal["UNIT_DATA"].",";
                    $contents .= $setVal["ALLOTMENT_TIME"].",";
                    $contents .= $setVal["UNIT_ARRIVAL_TARGET"].",";
                    $contents .= $setVal["UNIT_DIV"].",";
                    $contents .= $setVal["TEXT_PAGE"].",";
                    $contents .= $this->lastColumn.",";
                    $contents .= "\n";
                }
            }
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "教科書単元配当表_変換";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
