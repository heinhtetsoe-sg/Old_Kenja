<?php

require_once('for_php7.php');

class knjl014eModel extends Model {
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
            $cnt = knjl014eQuery::updateQueryCsv($this, $this->data_arr);
            $this->setMessage("MSG202","処理件数：{$cnt}件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString() {
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
                    $tmp    = str_replace('"', '', common::csv2array($buffer));

                    $cnt = 0;
                    $data = array();
                    $data["LOGIN_ID"]           = $tmp[$cnt++];
                    $data["ENTEXAMYEAR"]        = $tmp[$cnt++];
                    $data["APPLICANTDIV"]       = $tmp[$cnt++];
                    $data["TESTDIV"]            = $tmp[$cnt++];
                    $data["TESTDIV1"]           = $tmp[$cnt++];
                    $data["DESIREDIV"]          = $tmp[$cnt++];
                    $data["SHDIV"]              = $tmp[$cnt++];
                    $data["EXAMNO"]             = $tmp[$cnt++];
                    $data["EXAMHALL_NAME"]      = $tmp[$cnt++];
                    $data["EXAMHALL_CLASSNAME"] = $tmp[$cnt++];
                    $data["NAME"]               = $tmp[$cnt++];
                    $data["NAME_KANA"]          = $tmp[$cnt++];
                    $data["SEX"]                = $tmp[$cnt++];
                    $data["BIRTHDAY"]           = $tmp[$cnt++];
                    $data["ZIPCD"]              = $tmp[$cnt++];
                    $data["ADDRESS1"]           = $tmp[$cnt++];
                    $data["ADDRESS2"]           = $tmp[$cnt++];
                    $data["ADDRESS3"]           = $tmp[$cnt++];
                    $data["TELNO"]              = $tmp[$cnt++];
                    $data["GTELNO"]             = $tmp[$cnt++];
                    $data["GTELNO2"]            = $tmp[$cnt++];
                    $data["FS_CD"]              = $tmp[$cnt++];
                    $data["FS_NAME"]            = $tmp[$cnt++];
                    $data["FS_TELNO"]           = $tmp[$cnt++];
                    $data["FS_ZIPCD"]           = $tmp[$cnt++];
                    $data["FS_ADDR1"]           = $tmp[$cnt++];
                    $data["FS_DAY"]             = $tmp[$cnt++];
                    $data["FS_GRDNAME"]         = $tmp[$cnt++];
                    $data["GNAME"]              = $tmp[$cnt++];
                    $data["GKANA"]              = $tmp[$cnt++];
                    $data["GZIPCD"]             = $tmp[$cnt++];
                    $data["GADDRESS1"]          = $tmp[$cnt++];
                    $data["GADDRESS2"]          = $tmp[$cnt++];
                    $data["GADDRESS3"]          = $tmp[$cnt++];
                    $data["RELATIONSHIP_NAME"]  = $tmp[$cnt++];
                    $data["ENT_MONEY_STATUS"]   = $tmp[$cnt++];
                    $data["PRI_NAME"]           = $tmp[$cnt++];
                    $data["PRI_CLASSNAME"]      = $tmp[$cnt++];
                    $data["REMARK1"]            = $tmp[$cnt++];
                    $data["REMARK2"]            = $tmp[$cnt++];
                    $data["REMARK3"]            = $tmp[$cnt++];
                    $data["REMARK4"]            = $tmp[$cnt++];
                    $data["REMARK5"]            = $tmp[$cnt++];
                    $data["REMARK6"]            = $tmp[$cnt++];
                    $data["REMARK7"]            = $tmp[$cnt++];
                    $data["REMARK8"]            = $tmp[$cnt++];
                    $data["REMARK9"]            = $tmp[$cnt++];
                    $data["REMARK10"]           = $tmp[$cnt++];

                    //項目数によるファイルチェック
                    if ($this->ObjYear != $data["ENTEXAMYEAR"] || $this->field["APPLICANTDIV"] != $data["APPLICANTDIV"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の入試年度/入試制度何れかが不一致です。";
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

    function &getUpdateModel() {
        knjl014eQuery::updateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
