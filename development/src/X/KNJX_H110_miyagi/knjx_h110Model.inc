<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx_h110Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjx_h110Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),           //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
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
        if ($this->IsDataMatched($this->data_arr)) {
//            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjx_h110Query::insertQueryCsv($this,$this->data_arr);
//            } else {
//                $cnt = knjx_h110Query::DeleteQueryCsv($this,$this->data_arr);
//            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata = array("YEAR"               => array(4,"年度"),
                               "SCHREGNO"           => array(8,"学籍番号"),
                               "REGDDATE"           => array(10,"取得日付"),
                               "SUBCLASSCD"         => array(6,"科目コード"),
                               "CONDITION_DIV"      => array(1,"区分コード"),
                               "CONTENTS"           => array(2,"資格内容コード"),
                               "REMARK"             => array(90,"備考"),
                               "CREDITS"            => array(2,"単位数"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx_h110Query::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '')
                {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("YEAR"                => (strlen(trim($tmp[4])))  ? sprintf("%04d",$tmp[4])  : "",
                                  "SCHREGNO"            => (strlen(trim($tmp[5])))  ? sprintf("%08d",$tmp[5])  : "",
                                  "REGDDATE"            => $tmp[6],
                                  "SUBCLASSCD"          => (strlen(trim($tmp[7])))  ? sprintf("%06d",$tmp[7])  : "",
                                  "CONDITION_DIV"       => $tmp[9],
                                  "CONTENTS"            => (strlen(trim($tmp[11]))) ? sprintf("%02d",$tmp[11]) : "",
                                  "REMARK"              => $tmp[13],
                                  "CREDITS"             => $tmp[14]);

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
    function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $sonotaErrFlg    = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $sonotaErrMsg    = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $existSep     = "";
        $sonotaSep    = "";

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("YEAR","SCHREGNO","REGDDATE","SUBCLASSCD","CONDITION_DIV","CONTENTS"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("YEAR","SCHREGNO","SUBCLASSCD","CREDITS"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //値チェック
            if (in_array($key,array("CONDITION_DIV"))) {
                if ($data[$key] != "" && $data[$key] != "1") {
                    $returnFlg = $this->errSet($sonotaErrFlg, $sonotaErrMsg, "※その他:", $sonotaSep, $val[1]);
                }
            }

            //日付チェック
            if (in_array($key,array("REGDDATE"))) {
                if($data[$key] != "" && !$this->date_rep($data[$key])) {
                    $returnFlg = $this->errSet($sonotaErrFlg, $sonotaErrMsg, "※その他:", $sonotaSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック
            if (in_array($key,array("CONTENTS"))) {
                if ($data[$key] != "") {
                    $query = "SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'H305' AND NAMECD2 = '".$data[$key]."'";
                    $cnt = $db->getOne($query);
                    if ($cnt < 1) {
                        $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existErrMsg.$numericErrMsg.$lengthErrMsg.$sonotaErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx_h110Query::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
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
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        //ヘッダデータ
        $header = array("GRADE"                 => "学年",
                        "HR_CLASS"              => "クラス",
                        "ATTENDNO"              => "出席番号",
                        "NAME"                  => "氏名",
                        "YEAR"                  => "※年度",
                        "SCHREGNO"              => "※学籍番号",
                        "REGDDATE"              => "※取得日付",
                        "SUBCLASSCD"            => "※科目コード",
                        "SUBCLASSNAME"          => "科目名称",
                        "CONDITION_DIV"         => "※区分コード",
                        "CONDITION_DIV_NAME"    => "区分名称",
                        "CONTENTS"              => "※資格内容コード",
                        "CONTENTS_NAME"         => "資格内容",
                        "REMARK"                => "備考",
                        "CREDITS"               => "単位数",
                        "DUMMY"                 => "DUMMY");
        //ヘッダ例
        $header_rei = array("GRADE"                 => "01",
                            "HR_CLASS"              => "001",
                            "ATTENDNO"              => "001",
                            "NAME"                  => "アルプ太郎",
                            "YEAR"                  => "2006",
                            "SCHREGNO"              => "20060001",
                            "REGDDATE"              => "2006/07/24",
                            "SUBCLASSCD"            => "200001",
                            "SUBCLASSNAME"          => "情報Ａ",
                            "CONDITION_DIV"         => "1",
                            "CONDITION_DIV_NAME"    => "増加単位認定",
                            "CONTENTS"              => "06",
                            "CONTENTS_NAME"         => "簿記検定2級",
                            "REMARK"                => "備考",
                            "CREDITS"               => "1",
                            "DUMMY"                 => "DUMMY");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjx_h110Query::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_h110Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "資格データ（増加単位）";
            }
            $filename = sprintf("%s.csv",$couse);
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

}
//var_dump($_REQUEST);
?>
