<?php

require_once('for_php7.php');

class knjl055eModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV"),
                             "HEADER"       =>  VARS::post("HEADER")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $cnt = knjl055eQuery::updateQueryCsv($this, $this->data_arr);
            $this->setMessage("MSG202","処理件数：{$cnt}件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString() {
        $db = Query::dbCheckOut();
        $query = knjl055eQuery::getStudent($this);
        $this->studentArray = array();
        $result = $db->query($query);
        while ($stdRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->studentArray[$stdRow["EXAMNO"]] = "1";
        }
        $result->free();

        Query::dbCheckIn($db);

        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;
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
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $cnt = 1;
                    $data = array();
                    $data["EXAMNO"] = $tmp[$cnt++];
                    $data["SCORE1"] = $tmp[$cnt++];
                    $data["SCORE2"] = $tmp[$cnt++];
                    $data["SCORE3"] = $tmp[$cnt++];

                    //項目数によるファイルチェック
                    if ($this->studentArray[$data["EXAMNO"]] != "1") {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の受験番号が不正です。";
                        return;
                    }

                    $temp_arr[$i] = $data;
                    $i++;
                    $line++;
                }
            }
            fclose($fp);
        }

        return $temp_arr;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

}
?>
