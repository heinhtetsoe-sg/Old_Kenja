<?php

require_once('for_php7.php');

class knjl620aModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $entexamyear;

    function knjl620aModel() {}

    function init() {

        if (VARS::get("PROGRAMID")!="") {
            unset($this->entexamyear);
        }

        $this->entexamyear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),       //入試区分
                             "OUTPUT"           =>  VARS::post("OUTPUT"),        //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),        //ヘッダ有
                             );

        $this->capa = array();   //会場の収容人数チェック用
        $this->seatno = array(); //座席番号の付番用

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();  //プロパティファイル

        $this->file = VARS::file("FILE");
    }

    //ＣＳＶ取込
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjl620aQuery::updateQueryCsv($this, $this->data_arr);
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL620A'");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("TESTDIV"        => array(1,  "入試区分"),
                               "EXAMNO"         => array(4,  "受験番号"),
                               "EXAMHALLCD"     => array(2,  "会場番号"),
                               "TWINS_FLG"      => array(1,  "双子フラグ"),
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl620aQuery::deleteQueryErr();
            $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
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

                    $data = array("TESTDIV"      => $tmp[0],
                                  "EXAMNO"       => $tmp[1],
                                  "EXAMHALLCD"   => $tmp[2],
                                  "TWINS_FLG"    => $tmp[5],
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

    //エラーチェック
    function errCheck($db, $data, $data_each, $line) {
        $returnFlg = true;

        $hissuErrFlg     = false;    //必須エラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $otherErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherErrMsg     = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $otherSep     = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("TESTDIV","EXAMNO","EXAMHALLCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key,array("TESTDIV","EXAMNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        //入試区分チェック
        if ($this->field["TESTDIV"] != $data["TESTDIV"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "入試区分不一致");
        }
        //存在チェック
        //志願者受付データ
        if ($data["TESTDIV"] != "" && $data["EXAMNO"] != "") {
            $query = knjl620aQuery::checkEntexamReceptDat($this, $data["TESTDIV"], $data["EXAMNO"]);
            if (0 == $db->getOne($query)) {
                $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, "受験番号");
            }
        }
        //会場番号
        if ($data["EXAMHALLCD"] != "") {
            $query = knjl620aQuery::checkEntexamHallYdat($this, $data["TESTDIV"], $data["EXAMHALLCD"]);
            if (0 == $db->getOne($query)) {
                $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, "会場番号");
            } else if ($returnFlg) {
                if (array_key_exists($data["EXAMHALLCD"], $this->capa)) {
                    //エラーがなければ収容人数オーバーをチェックし、座席番号を付番
                    if ($this->capa[$data["EXAMHALLCD"]]["CNT"] >= $this->capa[$data["EXAMHALLCD"]]["MAX"]) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "会場の収容人数オーバー");
                    } else {
                        $this->capa[$data["EXAMHALLCD"]]["CNT"]++;
                        $this->seatno[$data["EXAMHALLCD"]][$data["EXAMNO"]] = sprintf('%02d', $this->capa[$data["EXAMHALLCD"]]["CNT"]);
                    }
                } else {
                    //会場の収容人数を取得
                    $this->capa[$data["EXAMHALLCD"]]["MAX"] = $db->getOne(knjl620aQuery::getHallCapa($this, $data["TESTDIV"], $data["EXAMHALLCD"]));
                    $this->capa[$data["EXAMHALLCD"]]["CNT"] = 1;
                    $this->seatno[$data["EXAMHALLCD"]][$data["EXAMNO"]] = sprintf('%02d', $this->capa[$data["EXAMHALLCD"]]["CNT"]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl620aQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目ﾁｪｯｸ後に行う。
    function date_rep($data) {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        //ヘッダデータ
        $header = array("TESTDIV"            => "※入試区分",
                        "EXAMNO"             => "※受験番号",
                        "EXAMHALLCD"         => "※会場番号",
                        "GNAME"              => "保護者氏名",
                        "GTELNO"             => "保護者電話番号",
                        "TWINS_FLG"          => "双子フラグ",
                        );

        $contents  = "";    //クリア

        $csv_output = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "3") {
            //エラー出力
            $contents .= implode($header_err, ",")."\n";
            $query = knjl620aQuery::selectCsvErrQuery();
        } else if ($this->field["OUTPUT"] == "2") {
            //データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
            $query = knjl620aQuery::selectMainQuery($this);
        }

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->field["OUTPUT"] == "2") {                //データ出力
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            }
            $contents .= implode($row, ",") ."\n";
            $csv_output = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($csv_output) {
            if ($this->field["OUTPUT"] == "3") {    //エラー出力
                $couse = "エラー出力";
            } else {                                //データ出力
                $couse = "受験者情報データ";
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
