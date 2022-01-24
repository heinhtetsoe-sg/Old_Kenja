<?php

require_once('for_php7.php');
class knjl082rModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        $this->ObjYear   = CTRL_YEAR + 1;
        $this->exam_type = '1'; //固定

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),
                             "PROCEDUREDATE"    =>  VARS::post("PROCEDUREDATE"),
                             "HEADER"           =>  VARS::post("HEADER")
                             );

        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $header = array();

        $header[] = "ユーザ番号";
        $header[] = "入試年度";
        $header[] = "入試制度";
        $header[] = "入試区分(10 or 20)";
        $header[] = "受験区分(10)";
        $header[] = "学科コース（志望区分）";
        $header[] = "専願/併願";
        $header[] = "受験番号";
        $header[] = "試験会場名";
        $header[] = "試験教室";
        $header[] = "氏名";
        $header[] = "フリガナ";
        $header[] = "性別";
        $header[] = "生年月日";
        $header[] = "郵便番号";
        $header[] = "志願者住所（都道府県市町村）";
        $header[] = "志願者住所（丁目、番地、号）";
        $header[] = "志願者住所（マンション名など）";
        $header[] = "電話番号";
        $header[] = "保護者電話番号（携帯）";
        $header[] = "保護者電話番号";
        $header[] = "出身学校コード";
        $header[] = "出身学校名";
        $header[] = "出身校電話番号";
        $header[] = "出身校郵便番号";
        $header[] = "出身校住所";
        $header[] = "卒業年月日";
        $header[] = "既/卒";
        $header[] = "保護者氏名";
        $header[] = "保護者ふりがな";
        $header[] = "保護者郵便番号";
        $header[] = "保護者住所（都道府県市町村）";
        $header[] = "保護者住所（丁目、番地、号）";
        $header[] = "保護者住所（マンション名など）";
        $header[] = "保護者続柄";
        $header[] = "入学金支払い状況";
        $header[] = "塾名";
        $header[] = "教室名";
        $header[] = "自由項目01";
        $header[] = "自由項目02";
        $header[] = "自由項目03";
        $header[] = "自由項目04";
        $header[] = "自由項目05";
        $header[] = "自由項目06";
        $header[] = "自由項目07";
        $header[] = "自由項目08";
        $header[] = "自由項目09";
        $header[] = "自由項目10";
        $header[] = $this->lastColumn;

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //ファイル名
        $title = $this->ObjYear.'年度 手続者CSVヘッダデータ';

        //ＣＳＶ出力
        $filename = sprintf("%s.csv", $title);
        common::downloadContents($contents, $filename);
        return true;
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
            $cnt = knjl082rQuery::updateQueryCsv($this, $this->data_arr);
            $upCnt = knjl082rQuery::updateQuery($this);
            $this->setMessage("MSG201", "処理件数：{$upCnt}件");
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
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $buffer = str_replace('"', '', $buffer);
                    $tmp    = common::csv2array($buffer);

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

                    //ユーザ番号(EXAMNO)を5桁0埋めで取り込み
                    if (trim($data["LOGIN_ID"]) != "") {
                        $data["LOGIN_ID"] = sprintf("%05d", $data["LOGIN_ID"]);
                    }

                    //項目数によるファイルチェック
                    if ($this->ObjYear != $data["ENTEXAMYEAR"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の入試年度が不一致です。";
                        return;
                    } elseif ($this->field["APPLICANTDIV"] != $data["APPLICANTDIV"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の入試制度が不一致です。";
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
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }
}
