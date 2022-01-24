<?php
/**
 * 定数定義
*/
class knjl622aModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $file;
        
    /**
     * メンバ変数の初期化
     */
    function knjl622aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV")    //入試制度
                           , "TESTDIV"      =>  VARS::post("TESTDIV")         //入試区分
                           , "EXAMNO_FROM"  =>  VARS::post("EXAMNO_FROM")     // 受験番号
                           , "EXAMNO_TO"    =>  VARS::post("EXAMNO_TO")       //受験番号
                           , "EXAMHALLCD"   =>  VARS::post("EXAMHALLCD")      //会場番号
                           , "HEADER"       =>  VARS::post("HEADER")          //ヘッダ有
                     );

        if ($this->cmd == 'update') {
            $this->line = array(
                  "RECEPTNO"   =>  VARS::post("RECEPTNO")    //受付番号
                , "COURSE1"    =>  VARS::post("COURSE1")     //志望コース(第1志望)
                , "COURSE2"    =>  VARS::post("COURSE2")     //志望コース(第2志望)
                , "SENBATU"    =>  VARS::post("SENBATU")     //選抜方式
                , "HALLSEATCD" =>  VARS::post("HALLSEATCD")  //座席番号
                , "RESERVE1"   =>  VARS::post("RESERVE1")    //面接番号
                , "RESERVE2"   =>  VARS::post("RESERVE2")    //予備２
                , "RESERVE3"   =>  VARS::post("RESERVE3")    //予備３
                , "RESERVE4"   =>  VARS::post("RESERVE4")    //予備４
                , "RESERVE5"   =>  VARS::post("RESERVE5")    //予備５
            );
        }
        $this->file = VARS::file("FILE");
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        // 会場の情報取得
        $hoolList = array();
        $result = $db->query(knjl622aQuery::getHall($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hoolList[$row["EXAMHALLCD"]] = $row;
        }
        // 会場のキャパチェック
        for ($i=0; $i < count($this->line["RECEPTNO"]); $i++) { 
            $receptNo   = $this->line["RECEPTNO"][$i];
            $hallSeatCd = $this->line["HALLSEATCD"][$i];
            $detail = $db->getOne(knjl622aQuery::getReceDetailCnt($this, 'REMARK1', $receptNo, '010'));
            // 座席番号が既に登録されていば場合は入力必須
            if ($detail && !$hallSeatCd) {
                $this->setWarning("MSG301", "座席番号を入力してください。");
                return false;
            }
            if (!$hallSeatCd) {
                continue;
            }
            $hallCd = substr($hallSeatCd, 0, 2);
            $seatCd = substr($hallSeatCd, 2);
            // 会場なし
            if (!$hoolList[$hallCd]) {
                $this->setWarning("MSG901", "指定された会場がありません。会場番号：".$hallCd);
                return false;
            }
            // 座席番号数値チェック
            if ($seatCd &&!is_numeric($seatCd)) {
                $this->setWarning("MSG913", "座席番号：".$hallSeatCd);
                return false;
            }
            // 会場キャパオーバー
            if ($hoolList[$hallCd]["CAPA_CNT"]) {
                if ($hoolList[$hallCd]["CAPA_CNT"] < intval($seatCd)) {
                    $this->setWarning("MSG913", "収容人数より大きな座席番号が入力されています。最大座席番号：".$hoolList[$hallCd]["CAPA_CNT"]);
                    return false;
                }
            }
            // 座席の重複チェック
            $dupcnt = $db->getOne(knjl622aQuery::getDupHallSeat($this, $hallCd, $seatCd));
            if ($dupcnt > 0) {
                $this->setWarning("MSG302", "座席番号：".$hallSeatCd);
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //更新
            $result = knjl622aQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        }
    }
    
    /***********/
    /* CSV取込 */
    /***********/
    function &getCsvInputModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjl622aQuery::insertQueryCsv($this, $this->data_arr);
            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            $this->setMessage(MSG202,"処理済件数　：".$cnt."件\\nエラー件数    ：".count($this->err_data)."件\\n".$erraDatas);
        }
    }
    
    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]       = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["APPLICANTDIV"]      = array(1,      "HISSU",    "INT",  "入試制度");
            $checkdata["TESTDIV"]           = array(1,      "HISSU",    "INT",  "入試区分");
            $checkdata["SEATNO"]            = array(4,      "HISSU",    "",     "座席番号");
            $checkdata["EXAMNO"]            = array(10,     "HISSU",    "",     "受験番号");
            if ($this->field["APPLICANTDIV"] == '2') {
                $checkdata["SENBATU"]       = array(2,      "HISSU",    "",     "選抜方式");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            
            
            // 会場の情報取得
            $hoolList = array();
            $result = $db->query(knjl622aQuery::getHall($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $hoolList[$row["EXAMHALLCD"]] = $row;
            }

            $fp = fopen ($this->file["tmp_name"], "r");
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
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = count($tmp);

                    if ($this->field["APPLICANTDIV"] == '1') {
                        $data = array();
                        $data["ENTEXAMYEAR"]    = $tmp[0];
                        $data["APPLICANTDIV"]   = $tmp[1];
                        $data["TESTDIV"]        = $tmp[2];
                        $data["EXAMNO"]         = $tmp[3];
                        $data["COURSE1"]        = $tmp[5];
                        $data["COURSE2"]        = $tmp[6];
                        $data["SEATNO"]         = $tmp[7];
                        $data["REMARK1"]        = $tmp[8];
                        $data["REMARK2"]        = $tmp[9];
                        $data["REMARK3"]        = $tmp[10];
                        $data["REMARK4"]        = $tmp[11];
                        $data["REMARK5"]        = $tmp[12];

                        $dataCnt = 13;
                    }
                    
                    if ($this->field["APPLICANTDIV"] == '2') {
                        $data["ENTEXAMYEAR"]    = $tmp[0];
                        $data["APPLICANTDIV"]   = $tmp[1];
                        $data["TESTDIV"]        = $tmp[2];
                        $data["EXAMNO"]         = $tmp[3];
                        $data["COURSE1"]        = $tmp[5];
                        $data["COURSE2"]        = $tmp[6];
                        $data["SENBATU"]        = $tmp[7];
                        $data["SEATNO"]         = $tmp[8];
                        $data["REMARK1"]        = $tmp[9];
                        $data["REMARK2"]        = $tmp[10];
                        $data["REMARK3"]        = $tmp[11];
                        $data["REMARK4"]        = $tmp[12];
                        $data["REMARK5"]        = $tmp[13];

                        $dataCnt = 14;
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line,$hoolList)) {
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
    function errCheck($db, $data, $data_each, $line,$hoolList) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep       = "";
        $existenceSep   = "";
        $existenceSep   = "";
        $numericSep     = "";
        $lengthSep      = "";

        $tesetdiv = $this->applicantdiv == "1" ? "L024" : "L004";
        $checkNameMstCd = array("APPLICANTDIV"      => "L003",
                                "TESTDIV"           => $tesetdiv,
                                "TESTSUBCLASSCD"    => "L009");

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                if ($key == "SCORE" && $data[$key] == "*") {
                } else {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
            }

            //年度
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試制度
            if ($key == "APPLICANTDIV" && $this->field["APPLICANTDIV"] != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試区分
            if ($key == "TESTDIV" && $this->field["TESTDIV"] != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl622aQuery::nameCntSql($this, $checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
            
            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl622aQuery::getReceptNo($data, "COUNT");
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $data[$key]);
                }
            }
            
            //座席番号
            if ($key == "SEATNO" && $data[$key] != "") {
                // 会場のキャパチェック
                $receptNo   = $data["EXAMNO"];
                $receptNo .= $data["TESTDIV"];
                $hallSeatCd = $data["SEATNO"];
                $detail = $db->getOne(knjl622aQuery::getReceDetailCnt($this, 'REMARK1', $receptNo, '010'));

                $hallCd = substr($hallSeatCd, 0, 2);
                $seatCd = substr($hallSeatCd, 2);
                // 会場なし
                if (!$hoolList[$hallCd]) {
                    $returnFlg = $this->errSet($seatErrFlg, $seatErrMsg, "※指定された会場が見つかりません:", $existenceSep, $hallCd);
                }
                // 座席番号数値チェック
                if ($seatCd &&!is_numeric($seatCd)) {
                    $returnFlg = $this->errSet($seatErrFlg, $seatErrMsg, "※座席番号は数値で入力してください:", $existenceSep, $hallSeatCd);
                }
                // 会場キャパオーバー
                if ($hoolList[$hallCd]["CAPA_CNT"]) {
                    if ($hoolList[$hallCd]["CAPA_CNT"] < intval($seatCd)) {
                        $returnFlg = $this->errSet($seatErrFlg, $seatErrMsg, "※収容人数より大きな座席番号が入力されています:", $existenceSep, $seatCd);
                    }
                }
                //重複チェック
                $query = knjl622aQuery::seatCntSql($data);
                $rowCount = $db->getOne($query);
                if ($rowCount > 0) {
                    $returnFlg = $this->errSet($seatErrFlg, $seatErrMsg, "※座席番号重複:", $existenceSep, $hallSeatCd);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$seatErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
        }
        return $returnFlg;
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
        if (count($data_arr) > 5000) {
            $this->setWarning(MSG915,"処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;

    }
    
    /***********/
    /* CSV出力 */
    /***********/
    function getCsvModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]      = "※入試年度";
        $header["APPLICANTDIV"]     = "※入試制度";
        $header["TESTDIV"]          = "※入試区分";
        $header["EXAMNO"]           = "※受験番号";
        $header["NAME"]             = "氏名";
        $header["COURSE1"]          = "第1志望コース";
        $header["COURSE2"]          = "第2志望コース";
        if ($this->field["APPLICANTDIV"] == '2') {
            $header["SENBATU"]      = "選抜方式";
        }
        $header["HALLSEATCD"]       = "※座席番号";
        $header["RESERVE1"]         = "面接番号";
        $header["RESERVE2"]         = "予備２";
        $header["RESERVE3"]         = "予備３";
        $header["RESERVE4"]         = "予備４";
        $header["RESERVE5"]         = "予備５";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;
        $query = knjl622aQuery::SelectQuery($this, false, true);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {

                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->ObjYear."年度座席番号処理";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
