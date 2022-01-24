<?php

require_once('for_php7.php');

class knjl214gModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
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
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl214gQuery::updateQueryCsv($this, $this->data_arr);
            $this->setMessage("MSG202", "処理件数：{$cnt}件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if ($line < 7) { // 7行目から取込
                        if ($line == 1) {
                            //文字コードを判別、改行コードを削除
                            $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                            $tmp    = str_replace('"', '', common::csv2array($buffer));
                            $chkYear = $tmp[2];
                        }
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = str_replace('"', '', common::csv2array($buffer));
            
                    $cnt = 1; // 2列目(入試区分)から
                    $data = array();
                    $data["AID_TESTDIV"]            = sprintf("%02d", $tmp[$cnt++]);
                    $data["AID_TESTDIV_NAME"]       = $tmp[$cnt++];
                    $data["EXAMCD"]                 = $tmp[$cnt++];
                    $data["EXAM_NAME"]              = $tmp[$cnt++];
                    $data["EXAMNO"]                 = $tmp[$cnt++];
                    $data["HOPECOURSE1"]            = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["HOPECOURSE2"]            = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["NAME_SEI"]               = $tmp[$cnt++];
                    $data["NAME_MEI"]               = $tmp[$cnt++];
                    $data["GAIJI_CD"]               = $tmp[$cnt++];
                    $data["NAME_KANA_SEI"]          = $tmp[$cnt++];
                    $data["NAME_KANA_MEI"]          = $tmp[$cnt++];
                    $data["SEX"]                    = $tmp[$cnt++];
                    $data["BIRTHDAY"]               = $tmp[$cnt++];
                    $data["ZIPCD1"]                 = $tmp[$cnt++];
                    $data["ZIPCD2"]                 = $tmp[$cnt++];
                    $data["PREF_CITY_BANCHI_NAME"]  = $tmp[$cnt++];
                    $data["ADDRESS2_1"]             = $tmp[$cnt++];
                    $data["ADDRESS2_2"]             = $tmp[$cnt++];
                    $data["PREF_CD"]                = $tmp[$cnt++];
                    $data["TELNO"]                  = $tmp[$cnt++];
                    $data["EMAIL"]                  = $tmp[$cnt++];
                    $data["SHIGAN_DATE"]            = $tmp[$cnt++];
                    $data["GNAME_SEI"]              = $tmp[$cnt++];
                    $data["GNAME_MEI"]              = $tmp[$cnt++];
                    $data["GKANA_SEI"]              = $tmp[$cnt++];
                    $data["GKANA_MEI"]              = $tmp[$cnt++];
                    $data["RELATIONSHIP"]           = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["GZIPCD1"]                = $tmp[$cnt++];
                    $data["GZIPCD2"]                = $tmp[$cnt++];
                    $data["GPREF_CITY_BANCHI_NAME"] = $tmp[$cnt++];
                    $data["GADDRESS2_1"]            = $tmp[$cnt++];
                    $data["GADDRESS2_2"]            = $tmp[$cnt++];
                    $data["GPREF_CD"]               = $tmp[$cnt++];
                    $data["GTELNO"]                 = $tmp[$cnt++];
                    $data["EMERGENCYCALL"]          = $tmp[$cnt++];
                    $data["AID_FS_CD"]              = $tmp[$cnt++];
                    $data["AID_FS_NAME"]            = $tmp[$cnt++];
                    $data["FS_GRDYEAR"]             = $tmp[$cnt++];
                    $data["REASON"]                 = $tmp[$cnt++];
                    $data["SH_SCHOOL_NAME"]         = $tmp[$cnt++];
                    $data["SH_PASS_DATE"]           = $tmp[$cnt++];
                    $data["SCHOLARSHIP"]            = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["SCHOLARSHIP_REASON"]     = $tmp[$cnt++];
                    $data["ENT_MONEY_EXEMPT"]       = $tmp[$cnt++];
                    $data["P_REGD_DIV"]             = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["P_GRD_YEAR"]             = $tmp[$cnt++];
                    $data["P_GRD_COURSE"]           = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["P_NAME"]                 = $tmp[$cnt++];
                    $data["P_RELATIONSHIP"]         = ($tmp[$cnt] != '') ? sprintf("%02d", $tmp[$cnt++]): $tmp[$cnt++];
                    $data["PRISCHOOL_NAME"]         = $tmp[$cnt++];
                    $data["PRISCHOOL_CLASS_NAME"]   = $tmp[$cnt++];

                    //項目数によるファイルチェック
                    if ($this->ObjYear != $chkYear) {
                        $this->warning = "CSVファイルを確認して下さい。\\n入試年度が不一致です。";
                        return;
                    }
                    /*
                                        if ($this->field["TESTDIV"] != $data["AID_TESTDIV"]) {
                                            $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の入試区分が不一致です。";
                                            return;
                                        }
                    */

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
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    public function &getUpdateModel()
    {
        knjl214gQuery::updateQuery($this);
        $this->setMessage("MSG201");
    }
}
