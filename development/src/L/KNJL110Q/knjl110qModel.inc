<?php

require_once('for_php7.php');
class knjl110qModel extends Model
{
    public $cmd;
    public $selectdata;
    public $selectdata2;
    public $ObjYear;
    public $applicantdiv;
    public $temporary_class;
    public $lastColumn;//CSVの最後の列の値

    //メンバ変数の初期化
    public function knjl110qModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
            unset($this->applicantdiv);
            unset($this->temporary_class);
            unset($this->selectdata);
            unset($this->selectdata2);
        }

        $this->cmd                  = VARS::request("cmd");
        $this->ObjYear              = CTRL_YEAR + 1;
        $this->selectdata           = VARS::post("selectdata");    //仮クラス対象者一覧
        $this->selectdata2          = VARS::post("selectdata2");   //入学者一覧
        $this->applicantdiv         = VARS::post("APPLICANTDIV");
        $this->temporary_class      = VARS::post("TEMPORARY_CLASS");//仮クラス
        $this->csv                  = VARS::post("csv");            //radio(1:取込 2:書出)
        $this->header               = VARS::post("HEADER");         //ヘッダ有

        $this->field = array("HEADER"         =>  VARS::post("HEADER")         //ヘッダ有
                             );

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        $this->temporary_class_mst = array();
        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjl110qQuery::getNameMst($this->ObjYear, 'L063');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->temporary_class_mst[] = $row["VALUE"];
        }
        $result->free();
        Query::dbCheckIn($db);

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

    public function validateInput()
    {
        //必須チェック
        if (strlen($this->applicantdiv) == 0) {
            $this->setWarning("MSG301", "( 入試制度 )");
            return false;
        }
        if (strlen($this->temporary_class) == 0) {
            $this->setWarning("MSG301", "( 仮クラス )");
            return false;
        }

        return true;
    }

    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    public function &update()
    {
        knjl110qQuery::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return $result;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl110qQuery::udpateQueryCsv($this, $this->data_arr);

            $this->setMessage("MSG202", "処理済件数　：{$cnt}件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
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
                    $tmp    = str_replace('"', '', common::csv2array($buffer));
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $cnt = 0;
                    $data = array();
                    $data["ENTEXAMYEAR"]        = $tmp[$cnt++];
                    $data["EXAMNO"]             = sprintf("%05d", $tmp[$cnt++]);
                    $data["NAME"]               = $tmp[$cnt++];
                    $data["FINSCHOOL_NAME"]     = $tmp[$cnt++];
                    $data["TEMPORARY_CLASS"]    = $tmp[$cnt++];

                    $dataCnt = 6;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //年度チェック
                    if ($this->ObjYear != $data["ENTEXAMYEAR"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の入試年度が不一致です。";
                        return;
                    }

                    //仮クラスチェック
                    if ($data["TEMPORARY_CLASS"] && !in_array($data["TEMPORARY_CLASS"], $this->temporary_class_mst)) {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の仮クラスが不正です。";
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

    /**************************/
    /* データの整合性チェック */
    /**************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダー
        $header = array();
        $header["ENTEXAMYEAR"]      = "入試年度";
        $header["EXAMNO"]           = "受験番号";
        $header["NAME"]             = "氏名";
        $header["FINSCHOOL_NAME"]   = "中学校名";
        $header["TEMPORARY_CLASS"]  = "仮クラス";
        $header[$this->lastColumn]  = $this->lastColumn;

        //初期化
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        //ヘッダーセット
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ
        $query = knjl110qQuery::selectMainQuery($this);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $sep = '';
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ',';
            }
            $contents .= "\n";

            $header_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "仮クラス割振り未指定入学者データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
