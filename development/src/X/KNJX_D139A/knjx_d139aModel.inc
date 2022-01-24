<?php

require_once('for_php7.php');

class knjx_d139aModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;   //コントロールマスタ
    var $schoolCd;
    var $getPrgId;  //コールされたか
//    var $sendAuth;  //親画面の権限
//    var $auth;      //権限

    function knjx_d139aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
//            unset($this->sendAuth);
//            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
//            $this->sendAuth = VARS::get("SEND_AUTH");
        }
//        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "SEMESTER2"        =>  VARS::post("SEMESTER2"),        //学期
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );
        if (VARS::post("SEMESTER")) {
            //ここでは学期選択変更時の時のみを考慮。初期値についてはForm.phpを参照。
            //理由は、CSV出力時に利用するため。
            $this->field["SEMESTER2"] = ($this->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $this->field["SEMESTER"];//初期値
        }

        if (VARS::get("SEND_PRGID")) {
            $this->field["SEMESTER"]   = (VARS::get("SEND_SEMESTER")) ? VARS::get("SEND_SEMESTER") : $this->field["SEMESTER"];
            $this->field["CLASSCD"]    = (VARS::get("SEND_CLASSCD")) ? VARS::get("SEND_CLASSCD") : $this->field["CLASSCD"];
            $this->field["SUBCLASSCD"] = (VARS::get("SEND_SUBCLASSCD")) ? VARS::get("SEND_SUBCLASSCD") : $this->field["SUBCLASSCD"];
            $this->field["CHAIRCD"]    = (VARS::get("SEND_CHAIRCD"))    ? VARS::get("SEND_CHAIRCD")    : $this->field["CHAIRCD"];
        }

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_d139aQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_d139aQuery::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("YEAR"       => array(4,"※年度"),
                                   "SEMESTER"   => array(1,"※学期コード"),
                                   "SCHREGNO"   => array(8,"※学籍番号"),
                                   "SUBCLASSCD" => array(13,"※科目コード"),
                                   "COMMENT"    => array(1140,"コメント"));
            } else {
                $checkdata = array("YEAR"       => array(4,"※年度"),
                                   "SEMESTER"   => array(1,"※学期コード"),
                                   "SCHREGNO"   => array(8,"※学籍番号"),
                                   "SUBCLASSCD" => array(6,"※科目コード"),
                                   "COMMENT"    => array(1140,"コメント"));
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 2;  //行数(最初の行はタイトルなので飛ばす。)
            //先頭行を飛ばすため、1行だけ読込み
            if (!feof ($fp))$buffer = fgets($fp, 4096);
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('JVIEWSTAT_REPORTREMARK_DAT');
            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //ヘッダ指定があれば飛ばす
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);

                    if (get_count($tmp) >= 10) {
                        $data = array("YEAR"       => (strlen(trim($tmp[3]))) ? $tmp[3] : "",
                                      "SEMESTER"   => (strlen(trim($tmp[4]))) ? $tmp[4] : "",
                                      "SCHREGNO"   => (strlen(trim($tmp[5]))) ? $tmp[5] : "",
                                      "SUBCLASSCD" => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                      "COMMENT"    => (strlen(trim($tmp[9]))) ? $tmp[9] : "");
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $cutwk = explode("-", $tmp[6]);
                            $data["CLASSCD"]          = $cutwk[0];
                            $data["SCHOOL_KIND"]      = $cutwk[1];
                            $data["CURRICULUM_CD"]    = $cutwk[2];
                        } else {
                            $data["CLASSCD"]          = substr($tmp[6], 0, 2);
                            $data["SCHOOL_KIND"]      = "";
                            $data["CURRICULUM_CD"]    = "";
                        }
                    } else {
                        $data = array();
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, get_count($tmp), $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $datcnt, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        //データ数チェック
        if ($datcnt < 10) {
            //項目数が不足している場合は、他のチェックをしても意味が無いので、このエラーだけ計上。
            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "項目数不足");
        } else {
            foreach ($data_each as $key => $val) {

                //必須項目チェック
                if (in_array($key, array("YEAR","SEMESTER","SCHREGNO","SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            //存在チェック（在籍）
            $semeschk = true;
            if (1 > $db->getOne(knjx_d139aQuery::checkRegdDat($this, $data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
                $semeschk = false;
            }

            //科目コード存在チェック(SUBCLASSCDの時のみチェック)
            //文字列が取得できるか、チェック。
            $query = knjx_d139aquery::chkSubclassMst($this, $data["SUBCLASSCD"], $data["SEMESTER"]);
            $rwk = $db->getRow($query);
            if (get_count($rwk) < 2) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された科目コードがありません。");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_d139aQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $ttl = "講座別面談資料入力";

        //画面設定
        //科目
        $query = knjx_d139aquery::getSubclassMst($this, $this->field["SUBCLASSCD"]);
        $rwk = $db->getRow($query);
        $subclsnm = $rwk["1"];
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダー
        $header["CLASS_NAME"]  = "クラス名称";
        $header["ATTENDNO"]    = "出席番号";
        $header["NAME"]        = "氏名";
        $header["YEAR"]        = "※年度";
        $header["SEMESTER"]    = "※学期コード";
        $header["SCHREGNO"]    = "※学籍番号";
        $header["SUBCLASSCD"]  = "※科目コード";
        $header["SUBCLSNAME"]  = "科目名";
        $header["VIEWPOINT"]   = "観点";
        $header["COMMENT"]     = "コメント";

        //ヘッダー例
        $header_rei["CLASS_NAME"]  = "6年2組";
        $header_rei["ATTENDNO"]    = "1番";
        $header_rei["NAME"]        = "アルプ　太郎";
        $header_rei["YEAR"]        = "2006";
        $header_rei["SEMESTER"]    = "1";
        $header_rei["SCHREGNO"]    = "20060001";
        $header_rei["SUBCLASSCD"]  = "01-J-2-010001";
        $header_rei["SUBCLSNAME"]  = "国語中学";
        $header_rei["VIEWPOINT"]   = "①C②A";
        $header_rei["COMMENT"]     = "コメント記載";

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= $ttl."\n";
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("YEAR"          => "年度",
                                     "SEMESTER"      => "学期",
                                     "SCHREGNO"      => "学籍番号",
                                     "CLASSCD"       => "教科コード",
                                     "SCHOOL_KIND"   => "学校校種",
                                     "CURRICULUM_CD" => "教育課程コード",
                                     "SUBCLASSCD"    => "科目コード");
            $tables = array();
            $tables = array("1" => "JVIEWSTAT_REPORTREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4") {
            $contents .= $ttl."\n";
            //データ
            if( $this->field["HEADER"] == "on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if ($this->field["OUTPUT"]=="4") {
               //生徒を抽出する日付
                $sdate = str_replace("/","-",$this->control["学期開始日付"][$this->field["SEMESTER"]]);
                $edate = str_replace("/","-",$this->control["学期終了日付"][$this->field["SEMESTER"]]);
                $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;//初期値

                //データ
                $query = knjx_d139aQuery::selectQuery($this, $execute_date);
            }

            //SQL文発行
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $kantenstr = "";
                if($this->field["OUTPUT"] == "4"){              //データ
                    $row = str_replace(","   , "." , $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r"  , "↓", $row);
                    $row = str_replace("\n"  , "↓", $row);

                    //観点取得
                    $dswk = array();
                    $dswk["STATUS"] = array();
                    $kantenstrwk = array();
                    $this->data["STATUS"] = array();
                    $query = knjx_d139aQuery::getGrade($this, $row["SCHREGNO"]);
                    $getGrade = $db->getOne($query);
                    $kanten = $db->query(knjx_d139aQuery::selectViewcdQuery($this,$row["SCHREGNO"], $execute_date, $getGrade));
                    while ($row2 = $kanten->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $dswk["STATUS"][] = $row2["STATUS"];
                        if($dswk["STATUS"] != "") {
                            foreach ($dswk["STATUS"] as $key => $value) {
                                $key = $key + 1;
                                if ($key == 1) {
                                    $number = "①";
                                } else if ($key == 2) {
                                    $number = "②";
                                } else if ($key == 3) {
                                    $number = "③";
                                } else if ($key == 4) {
                                    $number = "④";
                                } else if ($key == 5) {
                                    $number = "⑤";
                                } else if ($key == 6) {
                                    $number = "⑥";
                                }
                                $kantenstrwk[$key] = $number.$value;
                            }
                        }
                    }
                    $cnt = 1;
                    while ($cnt <= get_count($kantenstrwk)) {
                        $kantenstr .= $kantenstrwk[$cnt];
                        $cnt++;
                    }
                    //出力項目を整理してから$contentsに設定
                    $outwk = array();
                    $outwk[] = $row["HR_NAME"];
                    $outwk[] = $row["ATTENDNO"];
                    $outwk[] = $row["NAME"];
                    $outwk[] = CTRL_YEAR;
                    $outwk[] = $this->field["SEMESTER"];
                    $outwk[] = $row["SCHREGNO"];
                    $outwk[] = $this->field["SUBCLASSCD"];
                    $outwk[] = $subclsnm;
                    $outwk[] = $kantenstr;
                    $outwk[] = $row["REMARK1"];
                    $contents .= implode($outwk, ",") ."\n";
                } else {
                    $contents .= implode($row, ",") ."\n";
                }
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"] == "3"){      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = $ttl;
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
