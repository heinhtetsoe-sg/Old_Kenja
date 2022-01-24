<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx100oModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjx100oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR_SEME"        =>  VARS::post("YEAR_SEME"),        //年度＆学期
                             "EXECUTEDATE"      =>  VARS::post("EXECUTEDATE"),      //実施日付一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
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
            //ファイル取り込み
            $this->fileUpload();
    }

    //取込
    function fileUpload()
    {
        if (is_uploaded_file ($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx100oQuery::deleteQueryErr();
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof ($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != ''){
                    //１万件を超えたらループから抜ける。
                    if ($record_no > 10000) {
                        break;
                    }
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer,"UTF-8","SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("EXECUTEDATE" => trim($tmp[0]),
                                  "PERIODCD"    => trim($tmp[1]),
                                  "CHAIRCD"     => trim($tmp[2]),
                                  "EXECUTED"    => "0",
                                  "DATADIV"     => "0",
                                  "YEAR"        => "0",             //実施日付より年度を算出しセットする
                                  "SEMESTER"    => "0",             //実施日付より学期を算出しセットする
                                  "ATTESTOR"    => trim($tmp[3]));

                    $koumoku_error = $sonzai_error = $update_error = "";    //エラー出力用

                    //項目チェック
                    $koumoku_error = $this->koumoku_check($data);

                    //存在チェック(項目チェックがＯＫの場合)
                    if ($koumoku_error == "") {
                        //日付変換
                        $data_check = $this->date_rep($data["EXECUTEDATE"]);
                        if ($data_check) {
                            $data["EXECUTEDATE"] = $data_check;
                        } else {
                            $koumoku_error .= "日付エラー。";
                        }
                        //０埋め
                        $data = $this->zero_ume($data);

                        //存在チェック(更新時のみ)
                        if ($this->field["SHORI_MEI"] == "1") {
                            $sonzai_error = $this->sonzai_check($db,$data);
                        }
                    }

                    //取り込み処理
                    if ($koumoku_error == "" && $sonzai_error == "") {
                        //実施日付より年度・学期を算出しセットする
                        $data["YEAR"]       = $db->getOne("VALUES(Fiscalyear(date('".$data["EXECUTEDATE"]."')))");
                        $data["SEMESTER"]   = $db->getOne("VALUES(Y2t(date('".$data["EXECUTEDATE"]."'),'N'))");
                        //時間割講座データ
                        $query = knjx100oQuery::getExist_Check($data);
                        $result = $db->query($query);
                        $flg = false;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $flg = true;
                            //実施区分のチェック(更新時のみ)---2005.05.25
                            if ($this->field["SHORI_MEI"] == "1") {
                                if ($row["EXECUTED"] == "1") {
                                    $update_error = "更新不可。";  //実施区分のチェック用
                                }
                            }
                        }
                        //更新
                        if ($this->field["SHORI_MEI"] == "1") {
                            if ($flg) {   //ＤＢにあれば、update
                                //実施区分のチェック(実施区分が'1'の場合は更新不可)
                                if ($update_error == "") {
                                    knjx100oQuery::updateQueryCsv($db, $data, $this->staffcd);
                                }
                            } else {    //ＤＢになければ、insert
                                knjx100oQuery::insertQueryCsv($db, $data, $this->staffcd);
                            }
                        //削除
                        } else {
                            //ＤＢにあれば、delete
                            if ($flg) {
                                knjx100oQuery::deleteQueryCsv($db, $data);
                            }
                        }
                    }//ifの綴り

                    //エラーの場合、エラーＤＢへの追加
                    if ($koumoku_error != "" || $sonzai_error != "" || $update_error != "") {
                        $check_error = "";
                        $check_error = $koumoku_error .$sonzai_error .$update_error;
                        knjx100oQuery::insertQueryErr($db, $record_no, $check_error);
                    // SCH_CHR_COUNTFLGの作成
                    } else {
                        knjx100oQuery::createSchChrCountflg($db, $data, $this->staffcd, $this->field["SHORI_MEI"]);
                    }
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            $this->message = "CSV登録に成功しました。";
            //１万件を超えたら強制終了する。
            if ($record_no > 10000 ) {
                $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }

    }


    //ＣＳＶ出力
    function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        $header = array("EXECUTEDATE"   => "※実施日付",
                        "PERIODCD"      => "※校時コード",
                        "CHAIRCD"       => "※講座コード",
                        "ATTESTOR"      => "出欠確認者コード",
                        "DUMMY"             => "DUMMY");//2005.09.28nakamoto
        $header_rei = array("EXECUTEDATE"   => "2004-05-01",
                            "PERIODCD"      => "4",
                            "CHAIRCD"       => "9999999",
                            "ATTESTOR"      => "88888888",
                        "DUMMY"             => "DUMMY");//2005.09.28nakamoto

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"] == "1") {
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"] == "3") {     //エラー
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4") {     //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"] == "1") {
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"] == "3") {
                $query = knjx100oQuery::selectCsvQuery();
            } else if ($this->field["OUTPUT"] == "4") {     //データ
                $query = knjx100oQuery::getselectDataSQL($this);
            }
            $limit_flg = 0;     //行数上限65536
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $limit_flg++;
                if ($limit_flg > 65535) {
                    break;
                }
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row["REGISTERCD"] = "DUMMY";//2005.09.28nakamoto
                }//if
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }//while
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "時間割講座データ";
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

    /**
     * 項目チェック
     */
    function koumoku_check($data)
    {
        $koumoku_error = "";        //項目エラー用
            //桁数チェック
            if (strlen($data["EXECUTEDATE"]) > 10 || 
                strlen($data["PERIODCD"]) > 1 || 
                strlen($data["CHAIRCD"]) > 7 || 
                strlen($data["ATTESTOR"]) > 8)
            {
                $koumoku_error .= "桁数エラー。";
            }
            //必須項目チェック
            if ($data["EXECUTEDATE"] == "" || 
                $data["PERIODCD"] == "" || 
                $data["CHAIRCD"] == "")
            {
                $koumoku_error .= "必須項目エラー。";
            }
            //数字チェック
            if (!$this->suuji_check($data["CHAIRCD"])) {
                $koumoku_error .= "数字項目エラー。";
            }
        return $koumoku_error;  //項目エラー用
    }

    //数字チェック
    function suuji_check($data)
    {
        $flg = true;

        if ($data != "") {
            if (!is_numeric($data)) {
                $flg = false;
            }
        }

        return $flg;
    }

    //値チェック
    function atai_check($data,$atai)
    {
        $flg = true;

        if ($data != "") {
            if (!in_array($data,$atai)) {
                $flg = false;
            }
        }

        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if ($data) {
                $data = str_replace("/","-",$data);
            }
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    function zero_ume($data)
    {
        if ($data["CHAIRCD"] != "") {
            if (strlen($data["CHAIRCD"]) < 7) {
                $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
            }
        }
        if ($data["ATTESTOR"] != "") {
            if (strlen($data["ATTESTOR"]) < 8) {
                $data["ATTESTOR"] = sprintf("%08d", $data["ATTESTOR"]);
            }
        }

        return $data;
    }

    /**
     * マスタの存在チェック 項目ﾁｪｯｸ後に行う。
     */
    function sonzai_check(&$db,$data)
    {
        $sonzai_error = "";     //存在エラー用
        //1.講座データ
        if ($data["CHAIRCD"] != "") {
            $sonzai_flg = false;
            $query = knjx100oQuery::getMasterCheck($data,"1");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzai_flg = true;
            }
            if (!$sonzai_flg) {
                $sonzai_error .= "講座データなし。";
            }
        }
        //2.職員マスタ
        if ($data["ATTESTOR"] != "") {
            $sonzai_flg = false;
            $query = knjx100oQuery::getMasterCheck($data,"2");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzai_flg = true;
            }
            if (!$sonzai_flg) {
                $sonzai_error .= "職員マスタなし。";
            }
        }
        //3.名称マスタ
        if ($data["PERIODCD"] != "") {
            $sonzai_flg = false;
            $query = knjx100oQuery::getMasterCheck($data,"3");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzai_flg = true;
            }
            if (!$sonzai_flg) {
                $sonzai_error .= "校時コード：名称マスタなし。";
            }
        }
        return $sonzai_error;   //存在エラー用
    }
}
//var_dump($_REQUEST);
?>
