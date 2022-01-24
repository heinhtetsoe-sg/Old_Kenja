<?php

require_once('for_php7.php');

class knjl052nModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control; //コントロールマスタ
    var $staffcd; //登録者コード
    var $fieldSize = array();
    var $test_year;

    function init() {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //出力取込種別
                             "APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"       =>  VARS::post("TESTDIV"),       //入試区分
                             "TOTALCD"       =>  VARS::post("TOTALCD"),       //志望区分
                             "TESTSUBCLASSCD"=>  VARS::post("TESTSUBCLASSCD"),//受験科目
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    /***************/
    /* CSVの取込み */
    /***************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                $cnt = knjl052nQuery::udpateQueryCsv($this,$this->data_arr);
            } else {
//                $cnt = knjl052nQuery::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata =  array("ENTEXAMYEAR"   => array(4, "※テスト番号1"),//頭に20を付けて4桁でチェック
                                "TESTSUBCLASSCD"=> array(1, "※テスト番号2"),//頭の0をカットしてチェック
                                "EDABAN"        => array(2,  "枝番"),
                                "EXAMNO"        => array(4,  "番号"),
                                "SCORE"         => array(3,  "得点"),
                                "STD_SCORE"     => array(5,  "偏差値"),
                                "RANK"          => array(4,  "順位")
                                );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl052nQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("ENTEXAMYEAR"      => (strlen(trim($tmp[0])))  ? "20".$tmp[0] : "",
                                  "TESTSUBCLASSCD"   => (strlen(trim($tmp[1])))  ? sprintf("%01d",$tmp[1]) : "",
                                  "EDABAN"           => $tmp[2],
                                  "EXAMNO"           => $tmp[3],
                                  //$tmp[4]の氏名は取り込まない
                                  "SCORE"            => $tmp[5],
                                  "STD_SCORE"        => $tmp[6],
                                  "RANK"             => $tmp[7]
                                  );

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

    /*****************/
    /*エラーチェック */
    /*****************/
    function errCheck($db, $data, $checkdata, $line) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $etcErrFlg       = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $etcErrMsg       = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $etcSep    = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("ENTEXAMYEAR","TESTSUBCLASSCD","EDABAN","EXAMNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //存在チェック
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                if ($key == 'ENTEXAMYEAR' && strlen($data[$key]) && $data[$key] != $this->test_year) {
                    $returnFlg = $this->errSet($etcErrFlg, $etcErrMsg, "※不一致:", $etcSep, $val[1]);
                }
                if ($key == 'TESTSUBCLASSCD' && strlen($data[$key]) && $data[$key] != sprintf("%02d",$this->field["TESTSUBCLASSCD"])) {
                    $returnFlg = $this->errSet($etcErrFlg, $etcErrMsg, "※不一致:", $etcSep, $val[1]);
                }
                if ($key == 'EXAMNO' && strlen($data[$key]) && 0 == $db->getOne(knjl052nQuery::selectMainQuery($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("ENTEXAMYEAR","TESTSUBCLASSCD","EDABAN","EXAMNO","SCORE","STD_SCORE","RANK"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$etcErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl052nQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /****************************/
    /* エラーメッセージをセット */
    /****************************/
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /**************************/
    /* データの整合性チェック */
    /**************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //出力用ヘッダー
        $header_output = array("EXAMNO"        => "受験番号",
                               "NAME"          => "氏名");
        
        //取込用ヘッダー1
        $header1 = array("ENTEXAMYEAR"       => "※テスト番号1",
                         "TESTSUBCLASSCD"    => "※テスト番号2",
                         "EDABAN"            => "枝番",
                         "EXAMNO"            => "※番号",
                         "NAME"              => "氏名",
                         "SCORE"             => "得点",
                         "STD_SCORE"         => "偏差値",
                         "RANK"              => "順位"
                         );
                            
        $header_rei1 = array("ENTEXAMYEAR"      => "15",
                            "TESTSUBCLASSCD"    => "01",
                            "EDABAN"            => "00",
                            "EXAMNO"            => "1234",
                            "NAME"              => "アルプ太郎",
                            "SCORE"             => "72",
                            "STD_SCORE"         => "60.01",
                            "RANK"              => "69"
                            );
        //取込用ヘッダー2
        $header2 = "";
        $header_rei2 = "";
        $conma = ',';
        $setNumber = 1;
        for ($i = 1;$i <= 51; $i++) {
            if ($setNumber == "6") {
                $setNumber = "1";
            }
            if ($i < 51) {
                $header2 .= $conma.'問'.$i;
                $header_rei2 .= $conma.$setNumber;
            } else {
                $header2 .= $conma.'読み込み時刻';
                $header_rei2 .= $conma.'2016/02/10 13:07:41';
            }
            $setNumber++;
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header1, ",").$header2."\n";
            $contents .= implode($header_rei1, ",").$header_rei2."\n";
        } else if ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") {
            // データ出力
            if( $this->field["HEADER"]=="on" ) {
                $contents .= implode($header_output, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjl052nQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjl052nQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode(",", $row) ."\n";
                $header_data = true;
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"]=="3"){            //エラー
                $couse = "エラー出力";
            } else if ($this->field["OUTPUT"]=="4") {   //エラー以外
                $couse = "志願者名簿データ";
            } else {
                $couse = "MarkViewフォーマット";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}

?>
