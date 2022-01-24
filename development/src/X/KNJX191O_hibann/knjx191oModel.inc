<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx191oModel extends Model {
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
    function knjx191oModel()
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

        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
            $this->mode       = VARS::request("mode")       ? VARS::request("mode")       : 'ungrd';
        }
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
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjx191oQuery::insertQueryCsv($this,$this->data_arr);
            } else {
                $cnt = knjx191oQuery::DeleteQueryCsv($this,$this->data_arr);

            }
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

            $checkdata = array("SCHREGNO"          => array(8,"学籍番号"),
                               "COMMENTEX_A_CD"    => array(1,"概評特A区分"),
                               "TOTALSTUDYACT"     => array(246,"総合学習活動"),
                               "TOTALSTUDYVAL"     => array(369,"総合学習評価"),
                               "REMARK"            => array(615,"備考"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx191oQuery::deleteQueryErr();
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

                    $data = array("SCHREGNO"       => (strlen(trim($tmp[4]))) ? sprintf("%08d",$tmp[4]) : "",
                                  "COMMENTEX_A_CD" => $tmp[5],
                                  "TOTALSTUDYACT"  => $tmp[6],
                                  "TOTALSTUDYVAL"  => $tmp[7],
                                  "REMARK"         => $tmp[8]);

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
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $existSep     = "";

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key,array("SCHREGNO"))) {
                $query = knjx191oQuery::countSchregNo($data[$key]);
                $cnt = $db->getOne($query);
                if ($cnt < 1) {
                    $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx191oQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
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

        $header = array("GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "SCHREGNO"          => "※学籍番号",
                        "COMMENTEX_A_CD"    => "概評特A区分",
                        "TOTALSTUDYACT"     => "総合学習活動",
                        "TOTALSTUDYVAL"     => "総合学習評価",
                        "REMARK"            => "備考",
                        "DUMMY"             => "DUMMY");

        $header_rei = array("GRADE"             => "99",
                            "HR_CLASS"          => "999",
                            "ATTENDNO"          => "999",
                            "NAME"              => "アルプ太郎",
                            "SCHREGNO"          => "12345678",
                            "COMMENTEX_A_CD"    => "9",
                            "TOTALSTUDYACT"     => "全角82文字",
                            "TOTALSTUDYVAL"     => "全角123文字",
                            "REMARK"            => "全角205文字考",
                            "DUMMY"             => "DUMMY");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") {
            //データ
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjx191oQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx191oQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
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
            if($this->field["OUTPUT"]=="3"){        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "調査書進学用所見ヘッダデータ";
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
