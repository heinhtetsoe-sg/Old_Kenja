<?php
/**
 * 定数定義
*/
class knjh140cModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjh140cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                             );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->file = VARS::file("FILE");

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
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjh140cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjh140cQuery::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("NAME"            => array(120,"氏名"),
                               "SCHREGNO"        => array(8,"学籍番号"),
                               "ROSEN_1"         => array(45,"交通機関名1"),
                               "JOSYA_1"         => array(45,"通学区間1自"),
                               "GESYA_1"         => array(45,"通学区間1至"),
                               "KEIYU_1"         => array(45,"経由1"),
                               "ROSEN_2"         => array(45,"交通機関名2"),
                               "JOSYA_2"         => array(45,"通学区間2自"),
                               "GESYA_2"         => array(45,"通学区間2至"),
                               "KEIYU_2"         => array(45,"経由2"),
                               "ROSEN_3"         => array(45,"交通機関名3"),
                               "JOSYA_3"         => array(45,"通学区間3自"),
                               "GESYA_3"         => array(45,"通学区間3至"),
                               "KEIYU_3"         => array(45,"経由3"),
                               "ROSEN_4"         => array(45,"交通機関名4"),
                               "JOSYA_4"         => array(45,"通学区間4自"),
                               "GESYA_4"         => array(45,"通学区間4至"),
                               "KEIYU_4"         => array(45,"経由4"),
                               $this->lastColumn => array(10,$this->lastColumn)
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('SCHREG_ENVIR_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"]=="on") {
                        $header = true;
                        $line++; //ヘッダ行を対象外とする
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("NAME"            => $tmp[0],
                                  "SCHREGNO"        => $tmp[1],
                                  "ROSEN_1"         => $tmp[2],
                                  "JOSYA_1"         => $tmp[3],
                                  "GESYA_1"         => $tmp[4],
                                  "KEIYU_1"         => $tmp[5],
                                  "ROSEN_2"         => $tmp[6],
                                  "JOSYA_2"         => $tmp[7],
                                  "GESYA_2"         => $tmp[8],
                                  "KEIYU_2"         => $tmp[9],
                                  "ROSEN_3"         => $tmp[10],
                                  "JOSYA_3"         => $tmp[11],
                                  "GESYA_3"         => $tmp[12],
                                  "KEIYU_3"         => $tmp[13],
                                  "ROSEN_4"         => $tmp[14],
                                  "JOSYA_4"         => $tmp[15],
                                  "GESYA_4"         => $tmp[16],
                                  "KEIYU_4"         => $tmp[17],
                                  $this->lastColumn => $tmp[18]
                                  );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスターエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスターエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep           = "";
        $existenceSep       = "";
        $lengthSep          = "";
        $masterSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //学籍番号の存在チェック
            if (in_array($key, array("SCHREGNO"))) {
                $cnt = $db->getOne("SELECT COUNT(*) FROM SCHREG_REGD_DAT WHERE YEAR = '".CTRL_YEAR."' AND SCHREGNO = '".trim($data[$key])."' ");
                if ($cnt == 0) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスターチェック:", $masterSep, "学籍在籍データに存在しない学籍番号");
                }
            }

            //文字数チェック
            if (in_array($key, array("SCHREGNO","ROSEN_1","JOSYA_1","GESYA_1","KEIYU_1","ROSEN_2","JOSYA_2","GESYA_2","KEIYU_2","ROSEN_3","JOSYA_3","GESYA_3","KEIYU_3","ROSEN_4","JOSYA_4","GESYA_4","KEIYU_4"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } elseif ($this->field["SHORI_MEI"] == "1") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh140cQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $contents = "";    //クリア
        $col = array();
        if ($this->field["OUTPUT"]=="3") {
            //ヘッダ(エラー出力)
            $col[]["MSGROW"]     = "行番号";
            $col[]["MSGREMARK"]  = "メッセージ";
            $col[]["VIEW"]       = "観点";
            $col[]["REMARK1"]    = "コメント";

            $headerDuplicate = array("SCHREGNO"   => "学籍番号");
            $tables = array();
            $tables = array("1" => "SCHREG_ENVIR_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
        } else {
            //ヘッダ(データ出力)
            $col[]["NAME"]            = "氏名";
            $col[]["SCHREGNO"]       = "学籍番号";
            $col[]["ROSEN_1"]         = "交通機関名1";
            $col[]["JOSYA_1"]         = "通学区間1自";
            $col[]["GESYA_1"]         = "通学区間1至";
            $col[]["KEIYU_1"]         = "経由1";
            $col[]["ROSEN_2"]         = "交通機関名2";
            $col[]["JOSYA_2"]         = "通学区間2自";
            $col[]["GESYA_2"]         = "通学区間2至";
            $col[]["KEIYU_2"]         = "経由2";
            $col[]["ROSEN_3"]         = "交通機関名3";
            $col[]["JOSYA_3"]         = "通学区間3自";
            $col[]["GESYA_3"]         = "通学区間3至";
            $col[]["KEIYU_3"]         = "経由3";
            $col[]["ROSEN_4"]         = "交通機関名4";
            $col[]["JOSYA_4"]         = "通学区間4自";
            $col[]["GESYA_4"]         = "通学区間4至";
            $col[]["KEIYU_4"]         = "経由4";
            $col[][$this->lastColumn] = $this->lastColumn;
        }

        $header_data = false;   //出力フラグ

        //ヘッダフラグ
        if ($this->field["HEADER"]!="on") {
            $headerFlg = true;
        }

        $dataCol = array();
        //ヘッダを設定
        $titleSep = "";
        foreach ($col as $renBan => $fieldVal) {
            foreach ($fieldVal as $field => $val) {
                if (!$headerFlg) {
                    $contents .= $titleSep.$val;
                }
                $titleSep = ",";
                $dataCol[$field] =  $val;
            }
        }
        if (!$headerFlg) {
            $contents .= "\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ出力(見本)
            //サンプルデータを設定
            $contents .= "見本氏名,00000001,路線1,乗車1,下車1,○○経由1,路線2,乗車2,下車2,○○経由2,路線3,乗車3,下車3,○○経由3,路線4,乗車4,下車4,○○経由4,".$this->lastColumn."\n";
            $header_data = true;
        } else {
            //DB接続
            $db = Query::dbCheckOut();

            //エラー
            $query = $this->selectCsvErrQuery(PROGRAMID);

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //値をセット
                $sep = "";
                foreach ($dataCol as $field => $val) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {
                $couse = "エラー出力";
            } else {
                $couse = "通学経路CSV";
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
