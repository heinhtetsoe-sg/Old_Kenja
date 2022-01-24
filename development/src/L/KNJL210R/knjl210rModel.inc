<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl210rModel extends Model {
    var $data     = array();
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $data_arr = array();
    var $cmd;
    var $file;
    var $field    = array();
    var $controls = array();
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)

    /**
     * メンバ変数の初期化
     */
    function knjl210rModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "OUTPUT"       =>  VARS::post("OUTPUT"),         //出力取込種別
                             "HEADER"       =>  VARS::post("HEADER"),         //ヘッダ有
                             "SHORI_MEI"    =>  VARS::post("SHORI_MEI")       //処理名
                            );

        if (VARS::get("PROGRAMID")) {
            $this->field["HEADER"]  = "";
        } else {
            $this->field["HEADER"]  = VARS::post("HEADER");
        }
        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR+1;
        $this->file = VARS::file("FILE");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjl210rQuery::InsertQuery($this,$this->data_arr);
            } else {
                $cnt = knjl210rQuery::DeleteQuery($this,$this->data_arr);
            }
            //エラー件数取得
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210R' ");
            Query::dbCheckIn($db);
            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //ファイルの文字列の取得
    function GetFileString()
    {
        $temp_arr = array();
        if (is_uploaded_file($this->file["tmp_name"])) {

            //項目チェック用の配列
            //A専願
            if ($this->field["TESTDIV"] === '1') {
                $data_each = array( "BEFORE_PAGE"           => array(3,  "事前番号-頁"),
                                    "BEFORE_SEQ"            => array(3,  "事前番号-連番"),
                                    "NAIDAKU_KIGOU"         => array(6,  "内諾コース"),
                                    "NAME"                  => array(60, "氏名漢字"),   //姓名あわせて
                                    "NAME_KANA"             => array(120, "氏名カナ"),  //姓名あわせて
                                    "SEX"                   => array(1,  "性別コード"),
                                    "NAISIN2"               => array(3,  "内申75点"),
                                    "NAISIN3"               => array(3,  "内申135点"),
                                    "ATTEND1"               => array(3,  "欠席日数1年次"),
                                    "ATTEND2"               => array(3,  "欠席日数2年次"),
                                    "ATTEND3"               => array(3,  "欠席日数3年次"),
                                    "ATTEND_TOTAL"          => array(3,  "合計"),
                                    "NANKAN_FLG"            => array(1,  "難関コース希望"),
                                    "SCHOLARSHIP"           => array(1,  "奨学生希望コード"),
                                    "RECOM_FLG"             => array(1,  "推薦区分"),
                                    "RECOM_REMARK"          => array(150,"推薦備考"),
                                    "REMARK"                => array(150,"備考"),
                                    "FS_CD"                 => array(7,  "中学校コード"));
            //B専願
            } else {
                $data_each = array( "BEFORE_PAGE"           => array(3,  "事前番号-頁"),
                                    "BEFORE_SEQ"            => array(3,  "事前番号-連番"),
                                    "NAIDAKU_KIGOU"         => array(6,  "内諾コース"),
                                    "NAME"                  => array(60, "氏名漢字"),   //姓名あわせて
                                    "NAME_KANA"             => array(120, "氏名カナ"),  //姓名あわせて
                                    "SEX"                   => array(1,  "性別コード"),
                                    "NAISIN1"               => array(3,  "内申25点"),
                                    "NAISIN2"               => array(3,  "内申75点"),
                                    "NAISIN3"               => array(3,  "内申135点"),
                                    "ATTEND1"               => array(3,  "欠席日数1年次"),
                                    "ATTEND2"               => array(3,  "欠席日数2年次"),
                                    "ATTEND3"               => array(3,  "欠席日数3年次"),
                                    "ATTEND_TOTAL"          => array(3,  "合計"),
                                    "SENBATU1_SCHOOL"       => array(24, "選抜Ⅰ-公立受験校"),
                                    "SENBATU1_MAJOR"        => array(24, "選抜Ⅰ-科"),
                                    "SENBATU2_SCHOOL"       => array(24, "選抜Ⅱ-公立受験校"),
                                    "SENBATU2_MAJOR"        => array(24, "選抜Ⅱ-科"),
                                    "NANKAN_FLG"            => array(1,  "難関コース希望"),
                                    "SCHOLARSHIP"           => array(1,  "奨学生希望コード"),
                                    "REMARK"                => array(150,"備考"),
                                    "FS_CD"                 => array(7,  "中学校コード"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl210rQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "1") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    if ($this->field["TESTDIV"] === '1' && $checkcount != 19) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    } else if ($this->field["TESTDIV"] === '2' && $checkcount != 22) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }
                    $col = -1;
                    $data = array();
                    //A専願
                    if ($this->field["TESTDIV"] === '1') {
                        $data["BEFORE_PAGE"]   = (strlen(trim($tmp[++$col]))) ? sprintf("%03d", $tmp[$col]) : "";
                        $col++;
                        $data["BEFORE_SEQ"]    = (strlen(trim($tmp[++$col]))) ? sprintf("%03d", $tmp[$col]) : "";
                        $data["NAIDAKU_KIGOU"] = $tmp[++$col];
                        $data["NAME"]          = $tmp[++$col];
                        $data["NAME_KANA"]     = $tmp[++$col];
                        $data["SEX"]           = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["NAISIN2"]       = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["NAISIN3"]       = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND1"]       = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND2"]       = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND3"]       = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND_TOTAL"]  = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["NANKAN_FLG"]    = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["SCHOLARSHIP"]   = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["RECOM_FLG"]     = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["RECOM_REMARK"]  = $tmp[++$col];
                        $data["REMARK"]        = $tmp[++$col];
                        $data["FS_CD"]         = (strlen(trim($tmp[++$col]))) ? sprintf("%07d", $tmp[$col]) : "";
                        $data["LINE"]          = $line;
                    //B専願
                    } else {
                        $data["BEFORE_PAGE"]      = (strlen(trim($tmp[++$col]))) ? sprintf("%03d", $tmp[$col]) : "";
                        $col++;
                        $data["BEFORE_SEQ"]       = (strlen(trim($tmp[++$col]))) ? sprintf("%03d", $tmp[$col]) : "";
                        $data["NAIDAKU_KIGOU"]    = $tmp[++$col];
                        $data["NAME"]             = $tmp[++$col];
                        $data["NAME_KANA"]        = $tmp[++$col];
                        $data["SEX"]              = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["NAISIN1"]          = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["NAISIN2"]          = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["NAISIN3"]          = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND1"]          = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND2"]          = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND3"]          = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["ATTEND_TOTAL"]     = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["SENBATU1_SCHOOL"]  = $tmp[++$col];
                        $data["SENBATU1_MAJOR"]   = $tmp[++$col];
                        $data["SENBATU2_SCHOOL"]  = $tmp[++$col];
                        $data["SENBATU2_MAJOR"]   = $tmp[++$col];
                        $data["NANKAN_FLG"]       = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["SCHOLARSHIP"]      = (strlen(trim($tmp[++$col]))) ? trim($tmp[$col]) : "";
                        $data["REMARK"]           = $tmp[++$col];
                        $data["FS_CD"]            = (strlen(trim($tmp[++$col]))) ? sprintf("%07d", $tmp[$col]) : "";
                        $data["LINE"]             = $line;
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $data_each, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);

        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line)
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

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("BEFORE_PAGE","BEFORE_SEQ","SEX","FS_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            
            //必須項目チェック(名前)
            if (in_array($key,array("NAME","NAME_KANA"))) {
                if ($data[$key] === '　') {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            
            //数値チェック
            if (in_array($key,array("BEFORE_PAGE","BEFORE_SEQ","NAISIN1","NAISIN2","NAISIN3","ATTEND1","ATTEND2","ATTEND3","ATTEND_TOTAL","NANKAN_FLG","SCHOLARSHIP"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        //存在チェック内諾コース(追加のみ)
        if (strlen(trim($data["NAIDAKU_KIGOU"]))) {
            //全角文字に変換
            $henkan_kigou = mb_convert_kana($data["NAIDAKU_KIGOU"],R,"utf-8");
            if ($this->field["SHORI_MEI"] == "1" && 0 == $db->getOne(knjl210rQuery::countCourse($this, $henkan_kigou))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在しない:", $existenceSep, "内諾コース");
            }
        }

        //存在チェック中学校(追加のみ)
        if (strlen(trim($data["FS_CD"]))) {
            if ($this->field["SHORI_MEI"] == "1" && 0 == $db->getOne(knjl210rQuery::dataCntSchoolSql($this, $data["FS_CD"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在しない:", $existenceSep, "中学校コード");
            }
        }

        //存在チェック(追加のみ)
        if (strlen(trim($data["BEFORE_PAGE"])) && strlen(trim($data["BEFORE_SEQ"]))) {
            if ($this->field["SHORI_MEI"] == "1" && 0 < $db->getOne(knjl210rQuery::dataCntSql($this, $data["BEFORE_PAGE"], $data["BEFORE_SEQ"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "事前番号-貢/連番");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl210rQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
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
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //テンプレートの書き出し
    function OutputDataFile()
    {
        if ($this->field["TESTDIV"] === '1') {
            //A専願
            $header = array("BEFORE_PAGE"           => "※事前番号-頁",
                            "SCHOOL_NAME"           => "中学校名",
                            "BEFORE_SEQ"            => "※事前番号-連番",
                            "NAIDAKU_KIGOU"         => "※内諾コース",
                            "NAME"                  => "※姓　名", 
                            "NAME_KANA"             => "※フリガナ(姓　名)",
                            "SEX"                   => "※性別コード",
                            "NAISIN2"               => "内申75点",
                            "NAISIN3"               => "内申135点",
                            "ATTEND1"               => "欠席日数1年次",
                            "ATTEND2"               => "欠席日数2年次",
                            "ATTEND3"               => "欠席日数3年次",
                            "ATTEND_TOTAL"          => "合計",
                            "NANKAN_FLG"            => "難関コース希望",
                            "SCHOLARSHIP"           => "奨学生希望コード",
                            "RECOM_FLG"             => "推薦区分",
                            "RECOM_REMARK"          => "推薦備考",
                            "REMARK"                => "備考",
                            "FS_CD"                 => "※中学校コード");

            $header_rei = array("BEFORE_PAGE"           => "001",
                                "SCHOOL_NAME"           => "落合",
                                "BEFORE_SEQ"            => "001",
                                "NAIDAKU_KIGOU"         => "SG",
                                "NAME"                  => "山田　太郎", 
                                "NAME_KANA"             => "ヤマダ　タロウ",
                                "SEX"                   => "1",
                                "NAISIN2"               => "38",
                                "NAISIN3"               => "74",
                                "ATTEND1"               => "3",
                                "ATTEND2"               => "1",
                                "ATTEND3"               => "1",
                                "ATTEND_TOTAL"          => "5",
                                "NANKAN_FLG"            => "1",
                                "SCHOLARSHIP"           => "1",
                                "RECOM_FLG"             => "1",
                                "RECOM_REMARK"          => "野球",
                                "REMARK"                => "特になし",
                                "FS_CD"                 => "1234567");
        } else {
            //B専願
            $header = array("BEFORE_PAGE"           => "※事前番号-頁",
                            "SCHOOL_NAME"           => "中学校名",
                            "BEFORE_SEQ"            => "※事前番号-連番",
                            "NAIDAKU_KIGOU"         => "※内諾コース",
                            "NAME"                  => "※姓　名", 
                            "NAME_KANA"             => "※フリガナ(姓　名)",
                            "SEX"                   => "※性別コード",
                            "NAISIN1"               => "内申25点",
                            "NAISIN2"               => "内申75点",
                            "NAISIN3"               => "内申135点",
                            "ATTEND1"               => "欠席日数1年次",
                            "ATTEND2"               => "欠席日数2年次",
                            "ATTEND3"               => "欠席日数3年次",
                            "ATTEND_TOTAL"          => "合計",
                            "SENBATU1_SCHOOL"       => "選抜Ⅰ-公立受験校",
                            "SENBATU1_MAJOR"        => "選抜Ⅰ-科",
                            "SENBATU2_SCHOOL"       => "選抜Ⅱ-公立受験校",
                            "SENBATU2_MAJOR"        => "選抜Ⅱ-科",
                            "NANKAN_FLG"            => "難関コース希望",
                            "SCHOLARSHIP"           => "奨学生希望コード",
                            "REMARK"                => "備考",
                            "FS_CD"                 => "※中学校コード");

            $header_rei = array("BEFORE_PAGE"           => "001",
                                "SCHOOL_NAME"           => "落合",
                                "BEFORE_SEQ"            => "001",
                                "NAIDAKU_KIGOU"         => "SG",
                                "NAME"                  => "山田　花子", 
                                "NAME_KANA"             => "ヤマダ　ハナコ",
                                "SEX"                   => "2",
                                "NAISIN1"               => "16",
                                "NAISIN2"               => "38",
                                "NAISIN3"               => "74",
                                "ATTEND1"               => "3",
                                "ATTEND2"               => "1",
                                "ATTEND3"               => "1",
                                "ATTEND_TOTAL"          => "5",
                                "SENBATU1_SCHOOL"       => "高陽東",
                                "SENBATU1_MAJOR"        => "総合科",
                                "SENBATU2_SCHOOL"       => "高陽東",
                                "SENBATU2_MAJOR"        => "総合科",
                                "NANKAN_FLG"            => "1",
                                "SCHOLARSHIP"           => "2",
                                "REMARK"                => "特になし",
                                "FS_CD"                 => "1234567");
        }

        $contents = "";
        $header_data = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
            $query = knjl210rQuery::getFormatName($this);
            $format_name = $db->getOne($query);
        } else if ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= "行番号,メッセージ\n";
            $query = knjl210rQuery::selectCsvErrQuery();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv","事前相談取込エラー");
            } else {                                
                //ヘッダ出力
                $filename = sprintf("%s.csv","事前相談取込データ(".$format_name.")フォーマット");
            }
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }
}
?>
