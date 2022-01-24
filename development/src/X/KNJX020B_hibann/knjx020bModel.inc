<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx020bModel extends Model {
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
    function knjx020bModel()
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
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),         //学籍番号
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd          = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }
    function &getMainModel()
    {
    }

    /**
     * データ取込
    */
    function &getExecModel()
    {
        //更新
        if($this->field["SHORI_MEI"]=="1")
        {
            $this->fileUpload();
        //削除
        } else {
            $this->fileUpload_del();
        }
    }

    //更新(データ取込)
    function fileUpload()
    {
        if (is_uploaded_file ($this->field["FILE"]["tmp_name"])){       //HTTP POSTによりアップロードされたファイルかどうかを調べる

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx020bQuery::deleteQueryErr();
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof ($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != ''){
                    //１万件を超えたらループから抜ける。
                    if( $record_no > 10000 ) break;
                    //ヘッダ有なら１行目は無視
                    if( $this->field["HEADER"]=="on" && $record_no<2 ){
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer,"UTF-8","SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("SCHREGNO"            => trim($tmp[0]),
                                  "INOUTCD"             => trim($tmp[1]),
                                  "NAME"                => trim($tmp[2]),
                                  "NAME_SHOW"           => trim($tmp[3]),
                                  "NAME_KANA"           => trim($tmp[4]),
                                  "NAME_ENG"            => trim($tmp[5]),
                                  "BIRTHDAY"            => trim($tmp[6]),
                                  "SEX"                 => trim($tmp[7]),
                                  "BLOODTYPE"           => trim($tmp[8]),
                                  "BLOOD_RH"            => trim($tmp[9]),
                                  "HANDICAP"            => trim($tmp[10]),
                                  "NATIONALITY"         => trim($tmp[11]),
                                  "FINSCHOOLCD"         => trim($tmp[12]),
                                  "FINISH_DATE"         => trim($tmp[13]),
                                  "PRISCHOOLCD"         => trim($tmp[14]),
                                  "ENT_DATE"            => trim($tmp[15]),
                                  "ENT_DIV"             => trim($tmp[16]),
                                  "ENT_REASON"          => trim($tmp[17]),
                                  "ENT_SCHOOL"          => trim($tmp[18]),
                                  "ENT_ADDR"            => trim($tmp[19]),
                                  "GRD_DATE"            => trim($tmp[20]),
                                  "GRD_DIV"             => trim($tmp[21]),
                                  "GRD_REASON"          => trim($tmp[22]),
                                  "GRD_SCHOOL"          => trim($tmp[23]),
                                  "GRD_ADDR"            => trim($tmp[24]),
                                  "GRD_NO"              => trim($tmp[25]),
                                  "GRD_TERM"            => trim($tmp[26]),
                                  "REMARK1"             => trim($tmp[27]),
                                  "REMARK2"             => trim($tmp[28]),
                                  "REMARK3"             => trim($tmp[29]),
                                  "EMERGENCYCALL"       => trim($tmp[30]),
                                  "EMERGENCYNAME"       => trim($tmp[31]),
                                  "EMERGENCYRELA_NAME"  => trim($tmp[32]),
                                  "EMERGENCYTELNO"      => trim($tmp[33]),
                                  "EMERGENCYCALL2"      => trim($tmp[34]),
                                  "EMERGENCYNAME2"      => trim($tmp[35]),
                                  "EMERGENCYRELA_NAME2" => trim($tmp[36]),
                                  "EMERGENCYTELNO2"     => trim($tmp[37]));

                    $koumoku_error = $sonzai_error = "";    //エラー出力用

                    //項目チェック
                    $koumoku_error = $this->koumoku_check($data);

                    //存在チェック(項目チェックがＯＫの場合)
                    if($koumoku_error=="")
                    {
                        $date_flg = true;
                        //日付変換
                        if ($data["BIRTHDAY"]!="")
                        {
                            $data_check = $this->date_rep($data["BIRTHDAY"]);
                            if($data_check)
                            {
                                $data["BIRTHDAY"] = $data_check;
                            } else {
                                $date_flg = false;
                            }
                        }
                        if ($data["FINISH_DATE"]!="")
                        {
                            $data_check = $this->date_rep($data["FINISH_DATE"]);
                            if($data_check)
                            {
                                $data["FINISH_DATE"] = $data_check;
                            } else {
                                $date_flg = false;
                            }
                        }
                        if ($data["ENT_DATE"]!="")
                        {
                            $data_check = $this->date_rep($data["ENT_DATE"]);
                            if($data_check)
                            {
                                $data["ENT_DATE"] = $data_check;
                            } else {
                                $date_flg = false;
                            }
                        }
                        if ($data["GRD_DATE"]!="")
                        {
                            $data_check = $this->date_rep($data["GRD_DATE"]);
                            if($data_check)
                            {
                                $data["GRD_DATE"] = $data_check;
                            } else {
                                $date_flg = false;
                            }
                        }
                        if( !$date_flg ) $koumoku_error .= "日付エラー。";
                        //０埋め
                        $data["SCHREGNO"]       = $this->zero_ume($data["SCHREGNO"],8);
                        $data["FINSCHOOLCD"]    = $this->zero_ume($data["FINSCHOOLCD"],7);
                        $data["PRISCHOOLCD"]    = $this->zero_ume($data["PRISCHOOLCD"],7);
                        //その他
                        $table = "name_mst";
                        $where = "where NAMECD1='A025' AND NAMECD2 = '".$data["HANDICAP"]."'";
                        $sonzai_flg = $this->sonzai_check($db,$data["HANDICAP"],$table,$where);
                        if(!$sonzai_flg) $sonzai_error .= "(その他)名称マスタなし。";
                        //国籍
                        $table = "name_mst";
                        $where = "where NAMECD1='A024' AND NAMECD2 = '".$data["NATIONALITY"]."'";
                        $sonzai_flg = $this->sonzai_check($db,$data["NATIONALITY"],$table,$where);
                        if(!$sonzai_flg) $sonzai_error .= "(国籍)名称マスタなし。";
                        //出身校マスタ
                        $table = "finschool_mst";
                        $where = "where FINSCHOOLCD = '".$data["FINSCHOOLCD"]."'";
                        $sonzai_flg = $this->sonzai_check($db,$data["FINSCHOOLCD"],$table,$where);
                        if(!$sonzai_flg) $sonzai_error .= "出身校マスタなし。";
                        //塾マスタ
                        $table = "prischool_mst";
                        $where = "where PRISCHOOLCD = '".$data["PRISCHOOLCD"]."'";
                        $sonzai_flg = $this->sonzai_check($db,$data["PRISCHOOLCD"],$table,$where);
                        if(!$sonzai_flg) $sonzai_error .= "塾マスタなし。";
                        //名称マスタ(入学区分)---2005.05.09
                        $table = "name_mst";
                        $where = "where NAMECD1='A002' AND NAMECD2 = '".$data["ENT_DIV"]."'";
                        $sonzai_flg = $this->sonzai_check($db,$data["ENT_DIV"],$table,$where);
                        if(!$sonzai_flg) $sonzai_error .= "(入学区分)名称マスタなし。";
                        //名称マスタ(卒業区分)---2005.05.09
                        $table = "name_mst";
                        $where = "where NAMECD1='A003' AND NAMECD2 = '".$data["GRD_DIV"]."'";
                        $sonzai_flg = $this->sonzai_check($db,$data["GRD_DIV"],$table,$where);
                        if(!$sonzai_flg) $sonzai_error .= "(卒業区分)名称マスタなし。";
                    }//$ifの綴り

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if($koumoku_error!="" || $sonzai_error!="")
                    {
                        $check_error = "";
                        $check_error = $koumoku_error .$sonzai_error;
                        knjx020bQuery::insertQueryErr($db, $record_no, $check_error);
                    } else {
                        $table = "schreg_base_mst";
                        $where = "where SCHREGNO = '".$data["SCHREGNO"]."'";
                        $query = knjx020bQuery::getMasterSQL($table,$where);
                        $result = $db->query($query);
                        $flg = false;
                        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                            $flg = true;
                        }
                        if($flg){   //ＤＢにあれば、update
                            knjx020bQuery::updateQueryCsv($db, $data, $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx020bQuery::insertQueryCsv($db, $data, $this->staffcd);
                        }
                    }//$ifの綴り
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            $this->message = "CSV登録に成功しました。";
            //１万件を超えたら強制終了する。
            if( $record_no > 10000 ) $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
        }else{
            $this->warning = "ファイル名が不正です。";
        }

    }


    //削除(データ取込)
    function fileUpload_del()
    {
        if (is_uploaded_file ($this->field["FILE"]["tmp_name"])){       //HTTP POSTによりアップロードされたファイルかどうかを調べる

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx020bQuery::deleteQueryErr();
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof ($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != ''){
                    //１万件を超えたらループから抜ける。
                    if( $record_no > 10000 ) break;
                    //ヘッダ有なら１行目は無視
                    if( $this->field["HEADER"]=="on" && $record_no<2 ){
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer,"UTF-8","SJIS-win");     //文字列を指定コードに変換する

//2004/03/26 nakamoto del   $tmp = explode("\t", $buffer);
                    $tmp = explode(",", $buffer);       //2004/03/26 nakamoto add

                    $data = array("SCHREGNO"            => trim($tmp[0]));

                    $koumoku_error = "";    //エラー出力用

                    //項目チェック
                    $koumoku_error = "";        //項目エラー用
                    //桁数チェック
                    if ( strlen($data["SCHREGNO"]) > 8 ) $koumoku_error .= "桁数エラー。";
                    //必須チェック
                    if ( $data["SCHREGNO"]=="" ) $koumoku_error .= "必須項目エラー。";

                    //存在チェック(項目チェックがＯＫの場合)
                    if($koumoku_error=="")
                    {
                        //０埋め
                        $data["SCHREGNO"]       = $this->zero_ume($data["SCHREGNO"],8);
                    }//$ifの綴り

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if($koumoku_error!="")
                    {
                        $check_error = "";
                        $check_error = $koumoku_error;
                        knjx020bQuery::insertQueryErr($db, $record_no, $check_error);
                    } else {
                        $table = "schreg_base_mst";
                        $where = "where SCHREGNO = '".$data["SCHREGNO"]."'";
                        $query = knjx020bQuery::getMasterSQL($table,$where);
                        $result = $db->query($query);
                        $flg = false;
                        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                            $flg = true;
                        }
                        //ＤＢにあれば、delete
                        if($flg) knjx020bQuery::deleteQueryCsv($db, $data);
                    }//$ifの綴り
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            $this->message = "CSV登録に成功しました。";
            //１万件を超えたら強制終了する。
            if( $record_no > 10000 ) $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
        }else{
            $this->warning = "ファイル名が不正です。";
        }

    }


    //ＣＳＶへエラー出力
    function getDownloadModel()
    {
        $header = array("MSGROW"            => "行番号",
                        "MSGREMARK"         => "メッセージ");

        $contents  = implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjx020bQuery::selectCsvQuery();
        $result = $db->query($query);
        $err_data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if($err_data)
        {
            $couse = "エラー出力";
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
        }else{
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ヘッダ出力 2004/03/24 nakamoto add
    function getHeaderModel()
    {
        $header = array("SCHREGNO"              => "※学籍番号",
                        "INOUTCD"               => "内外区分",
                        "NAME"                  => "※生徒氏名",
                        "NAME_SHOW"             => "生徒氏名表示用",
                        "NAME_KANA"             => "生徒氏名かな",
                        "NAME_ENG"              => "生徒氏名英字",
                        "BIRTHDAY"              => "生年月日",
                        "SEX"                   => "性別",
                        "BLOODTYPE"             => "血液型",
                        "BLOOD_RH"              => "血液RH型",
                        "HANDICAP"              => "その他",
                        "NATIONALITY"           => "国籍",
                        "FINSCHOOLCD"           => "出身校コード",
                        "FINISH_DATE"           => "出身校卒業年月日",
                        "PRISCHOOLCD"           => "塾コード",
                        "ENT_DATE"              => "入学日付",
                        "ENT_DIV"               => "入学区分",
                        "ENT_REASON"            => "入学理由",
                        "ENT_SCHOOL"            => "入学学校",
                        "ENT_ADDR"              => "入学住所",
                        "GRD_DATE"              => "除籍(卒業)日付",
                        "GRD_DIV"               => "除籍(卒業)区分",
                        "GRD_REASON"            => "除籍(卒業)事由",
                        "GRD_SCHOOL"            => "除籍(卒業)学校",
                        "GRD_ADDR"              => "除籍(卒業)住所",
                        "GRD_NO"                => "卒業生台帳番号",
                        "GRD_TERM"              => "卒業期",
                        "REMARK1"               => "備考１",
                        "REMARK2"               => "備考２",
                        "REMARK3"               => "備考３",
                        "EMERGENCYCALL"         => "急用連絡先",        //2005.10.18 m-yama
                        "EMERGENCYNAME"         => "急用連絡先名",      //2005.10.18 m-yama
                        "EMERGENCYRELA_NAME"    => "急用連絡先続柄",    //2005.10.18 m-yama
                        "EMERGENCYTELNO"        => "急用電話番号",      //2005.10.18 m-yama
                        "EMERGENCYCALL2"        => "急用連絡先２",      //2005.10.18 m-yama
                        "EMERGENCYNAME2"        => "急用連絡先名２",    //2005.10.18 m-yama
                        "EMERGENCYRELA_NAME2"   => "急用連絡先続柄２",  //2005.10.18 m-yama
                        "EMERGENCYTELNO2"       => "急用電話番号２",    //2005.10.18 m-yama
                        "DUMMY"                 => "DUMMY");//2005.09.28nakamoto

        $header_rei = array("SCHREGNO"          => "12345678",
                        "INOUTCD"               => "1",
                        "NAME"                  => "アルプ　太郎",
                        "NAME_SHOW"             => "アルプ　太郎",
                        "NAME_KANA"             => "あるぷ　たろう",
                        "NAME_ENG"              => "Tarou Alp",
                        "BIRTHDAY"              => "1996-03-01",
                        "SEX"                   => "1",
                        "BLOODTYPE"             => "A",
                        "BLOOD_RH"              => "-",
                        "HANDICAP"              => "001",
                        "NATIONALITY"           => "001",
                        "FINSCHOOLCD"           => "1234567",
                        "FINISH_DATE"           => "2010-03-31",
                        "PRISCHOOLCD"           => "1234567",
                        "ENT_DATE"              => "2010-04-01",
                        "ENT_DIV"               => "2",
                        "ENT_REASON"            => "入学",
                        "ENT_SCHOOL"            => "ＸＸＸ中学",
                        "ENT_ADDR"              => "東京都ＸＸＸ区ＸＸＸ町Ｘ丁目",
                        "GRD_DATE"              => "2013-03-31",
                        "GRD_DIV"               => "1",
                        "GRD_REASON"            => "卒業",
                        "GRD_SCHOOL"            => "ＸＸＸ高校",
                        "GRD_ADDR"              => "東京都ＸＸＸ区ＸＸＸ町Ｘ丁目",
                        "GRD_NO"                => "99998888",
                        "GRD_TERM"              => "7",
                        "REMARK1"               => "備考１",
                        "REMARK2"               => "備考２",
                        "REMARK3"               => "備考３",
                        "EMERGENCYCALL"         => "急用連絡先",        //2005.10.18 m-yama
                        "EMERGENCYNAME"         => "急用連絡先名",      //2005.10.18 m-yama
                        "EMERGENCYRELA_NAME"    => "1",                 //2005.10.18 m-yama
                        "EMERGENCYTELNO"        => "090-0909-0909",     //2005.10.18 m-yama
                        "EMERGENCYCALL2"        => "急用連絡先２",      //2005.10.18 m-yama
                        "EMERGENCYNAME2"        => "急用連絡先名２",    //2005.10.18 m-yama
                        "EMERGENCYRELA_NAME2"   => "2",                 //2005.10.18 m-yama
                        "EMERGENCYTELNO2"       => "090-0909-0909",     //2005.10.18 m-yama
                        "DUMMY"                 => "DUMMY");//2005.09.28nakamoto

        $contents  = implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        $header_data = true;

        if($header_data)
        {
            $couse = "学籍基礎マスタ";
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
        }else{
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //データ出力
    function getDataModel()
    {
        $header = array("SCHREGNO"              => "※学籍番号",
                        "INOUTCD"               => "内外区分",
                        "NAME"                  => "※生徒氏名",
                        "NAME_SHOW"             => "生徒氏名表示用",
                        "NAME_KANA"             => "生徒氏名かな",
                        "NAME_ENG"              => "生徒氏名英字",
                        "BIRTHDAY"              => "生年月日",
                        "SEX"                   => "性別",
                        "BLOODTYPE"             => "血液型",
                        "BLOOD_RH"              => "血液RH型",
                        "HANDICAP"              => "その他",
                        "NATIONALITY"           => "国籍",
                        "FINSCHOOLCD"           => "出身校コード",
                        "FINISH_DATE"           => "出身校卒業年月日",
                        "PRISCHOOLCD"           => "塾コード",
                        "ENT_DATE"              => "入学日付",
                        "ENT_DIV"               => "入学区分",
                        "ENT_REASON"            => "入学理由",
                        "ENT_SCHOOL"            => "入学学校",
                        "ENT_ADDR"              => "入学住所",
                        "GRD_DATE"              => "除籍(卒業)日付",
                        "GRD_DIV"               => "除籍(卒業)区分",
                        "GRD_REASON"            => "除籍(卒業)事由",
                        "GRD_SCHOOL"            => "除籍(卒業)学校",
                        "GRD_ADDR"              => "除籍(卒業)住所",
                        "GRD_NO"                => "卒業生台帳番号",
                        "GRD_TERM"              => "卒業期",
                        "REMARK1"               => "備考１",
                        "REMARK2"               => "備考２",
                        "REMARK3"               => "備考３",
                        "EMERGENCYCALL"         => "急用連絡先",        //2005.10.18 m-yama
                        "EMERGENCYNAME"         => "急用連絡先名",      //2005.10.18 m-yama
                        "EMERGENCYRELA_NAME"    => "急用連絡先続柄",    //2005.10.18 m-yama
                        "EMERGENCYTELNO"        => "急用電話番号",      //2005.10.18 m-yama
                        "EMERGENCYCALL2"        => "急用連絡先２",      //2005.10.18 m-yama
                        "EMERGENCYNAME2"        => "急用連絡先名２",    //2005.10.18 m-yama
                        "EMERGENCYRELA_NAME2"   => "急用連絡先続柄２",  //2005.10.18 m-yama
                        "EMERGENCYTELNO2"       => "急用電話番号２",    //2005.10.18 m-yama
                        "DUMMY"                 => "DUMMY");//2005.09.28nakamoto

        $contents  = "";
        if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $table = "schreg_base_mst";
        //学籍番号
        if( $this->field["SCHREGNO"]!="" )
        {
            $where = "where SCHREGNO = '".$this->field["SCHREGNO"]."' ORDER BY SCHREGNO";
        } else {
            $where = "ORDER BY SCHREGNO";
        }
        $query = knjx020bQuery::getMasterSQL($table,$where);
        $result = $db->query($query);
        $header_data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["REGISTERCD"] = $row["UPDATED"] = "";
            $row["REGISTERCD"] = "DUMMY";//2005.09.28nakamoto
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


        if($header_data)
        {
            $couse = "学籍基礎マスタ";
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
        }else{
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

        //桁数チェック NO001
        if (strlen($data["SCHREGNO"]) > 8 || 
            strlen($data["INOUTCD"]) > 1 || 
            strlen($data["NAME"]) > 60 || 
            strlen($data["NAME_SHOW"]) > 30 || 
            strlen($data["NAME_KANA"]) > 120 || 
            strlen($data["NAME_ENG"]) > 40 || 
            strlen($data["BIRTHDAY"]) > 10 || 
            strlen($data["SEX"]) > 1 || 
            strlen($data["BLOODTYPE"]) > 2 || 
            strlen($data["BLOOD_RH"]) > 1 || 
            strlen($data["HANDICAP"]) > 3 || 
            strlen($data["NATIONALITY"]) > 3 || 
            strlen($data["FINSCHOOLCD"]) > 7 || 
            strlen($data["FINISH_DATE"]) > 10 || 
            strlen($data["PRISCHOOLCD"]) > 7 || 
            strlen($data["ENT_DATE"]) > 10 || 
            strlen($data["ENT_DIV"]) > 1 || 
            strlen($data["ENT_REASON"]) > 75 || 
            strlen($data["ENT_SCHOOL"]) > 75 || 
            strlen($data["ENT_ADDR"]) > 75 || 
            strlen($data["GRD_DATE"]) > 10 || 
            strlen($data["GRD_DIV"]) > 1 || 
            strlen($data["GRD_REASON"]) > 75 || 
            strlen($data["GRD_SCHOOL"]) > 75 || 
            strlen($data["GRD_ADDR"]) > 75 || 
            strlen($data["GRD_NO"]) > 8 || 
            strlen($data["GRD_TERM"]) > 4 || 
            strlen($data["REMARK1"]) > 75 || 
            strlen($data["REMARK2"]) > 75 || 
            strlen($data["REMARK3"]) > 75 ||
            strlen($data["EMERGENCYCALL"]) > 60 ||
            strlen($data["EMERGENCYNAME"]) > 60 ||
            strlen($data["EMERGENCYRELA_NAME"]) > 30 ||
            strlen($data["EMERGENCYTELNO"]) > 14 ||
            strlen($data["EMERGENCYCALL2"]) > 60 ||
            strlen($data["EMERGENCYNAME2"]) > 60 ||
            strlen($data["EMERGENCYRELA_NAME2"]) > 30 ||
            strlen($data["EMERGENCYTELNO2"]) > 14)
        {
                $koumoku_error .= "桁数エラー。";
        }
        //必須チェック
        if ( $data["SCHREGNO"]=="" || $data["NAME"]=="" )
        {
                $koumoku_error .= "必須項目エラー。";
        }
        //数字チェック
        if (!$this->suuji_check($data["INOUTCD"])       || 
            !$this->suuji_check($data["SEX"])           || 
            !$this->suuji_check($data["HANDICAP"])      || 
            !$this->suuji_check($data["NATIONALITY"])   || 
            !$this->suuji_check($data["FINSCHOOLCD"])   || 
            !$this->suuji_check($data["PRISCHOOLCD"])   || 
            !$this->suuji_check($data["ENT_DIV"])       || 
            !$this->suuji_check($data["GRD_DIV"]))
        {
                $koumoku_error .= "数字項目エラー。";
        }
        //値チェック
        if (!$this->atai_check($data["SEX"]         ,array("1","2"))            || 
//---2005.05.09
//          !$this->atai_check($data["ENT_DIV"]     ,array("1","2","3","4"))    || 
//          !$this->atai_check($data["GRD_DIV"]     ,array("1","2","3"))        || 
            !$this->atai_check($data["BLOODTYPE"]   ,array("A","B","O","AB"))   || 
            !$this->atai_check($data["BLOOD_RH"]    ,array("-","+")))
        {
                $koumoku_error .= "値エラー。";
        }

        return $koumoku_error;  //項目エラー用
    }

    //数字チェック
    function suuji_check($data)
    {
        $flg = true;

        if ( $data!="" )
        {
            if ( !is_numeric($data) ) $flg = false;
        }

        return $flg;
    }

    //値チェック
    function atai_check($data,$atai)
    {
        $flg = true;

        if ( $data!="" )
        {
            if ( !in_array($data,$atai) ) $flg = false;
        }

        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
        if ($data!="")
        {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    function zero_ume($data,$ketasuu)
    {
        $shosiki = "%0".$ketasuu."d";
        if ($data!="")
        {
            if (strlen($data) < $ketasuu) $data = sprintf($shosiki, $data);
        }

        return $data;
    }

    /**
     * マスタの存在チェック
     */
    function sonzai_check(&$db,$data,$table,$where)
    {
        $sonzai_flg = true;
        if( $data!="" ){
            $sonzai_flg = false;
            $query = knjx020bQuery::getMasterSQL($table,$where);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $sonzai_flg = true;
            }
        }
        return $sonzai_flg;
    }

}
//var_dump($_REQUEST);
?>
