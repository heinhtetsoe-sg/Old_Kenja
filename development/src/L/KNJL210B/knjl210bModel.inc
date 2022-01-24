<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl210bModel extends Model {
    var $data     = array();
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $data_arr = array();
    var $cmd;
    var $file;
    var $field    = array();
    var $controls = array();
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl210bModel()
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

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),   //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),        //入試区分
                             "DATADIV"      =>  VARS::post("DATADIV"),        //データ種類
                             "OUTPUT"       =>  VARS::post("OUTPUT"),         //出力取込種別
                             "HEADER"       =>  VARS::post("HEADER"),         //ヘッダ有
                             "SHORI_MEI"    =>  VARS::post("SHORI_MEI")       //処理名
                            );

        if (VARS::get("PROGRAMID")) {
            $this->field["HEADER"]  = "1";
        } else {
            $this->field["HEADER"]  = VARS::post("HEADER");
        }
        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR+1;
        $this->file = VARS::file("FILE");
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjl210bQuery::InsertQuery($this,$this->data_arr);
            } else if ($this->field["SHORI_MEI"] == "2") {
                $cnt = knjl210bQuery::UpdateQuery($this,$this->data_arr);
            } else {
                $cnt = knjl210bQuery::DeleteQuery($this,$this->data_arr);
            }
            //エラー件数取得
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210B' ");
            Query::dbCheckIn($db);
            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //ファイルの文字列の取得
    function GetFileString()
    {
        $temp_arr = array();
        if (is_uploaded_file($this->file["tmp_name"])) {

            //項目チェック用の配列
            //願書データ
            if ($this->field["DATADIV"] === '1') {
                $data_each = array( "SHORI_MEI"         => array(1,  "処理名"),
                                    "ENTEXAMYEAR"       => array(4,  "入試年度"),
                                    "APPLICANTDIV"      => array(1,  "入試制度"),
                                    "TESTDIV"           => array(1,  "入試区分"),
                                    "TESTDIV1"          => array(1,  "受験科目数コード"),
                                    "SELECT_SUBCLASS_DIV" => array(1,  "希望コース"),
                                    "EXAMNO"            => array(5,  "受験番号"),
                                    "NAME"              => array(60, "志願者氏名"),
                                    "NAME_KANA"         => array(120, "志願者かな氏名"),
                                    "BIRTHDAY"          => array(10, "志願者生年月日"),
                                    "SEX"               => array(1,  "志願者性別"),
                                    "FS_CD"             => array(7,  "中学校コード"),
                                    "FS_DAY"            => array(10, "中学校卒業日付"),
                                    "FS_GRDDIV"         => array(1,  "志願時状態"),
                                    //"??"         => array(?,  "単願/併願"),
                                    "ZIPCD"             => array(8,  "郵便番号"),
                                    //"??"         => array(?,  "都道府県"),
                                    //"??"         => array(?,  "市区町村"),
                                    "ADDRESS1"          => array(75, "住所1"),
                                    "ADDRESS2"          => array(75, "住所2"),
                                    "TELNO"             => array(14, "電話番号"),
                                    "GNAME"             => array(60, "保護者漢字氏名"),
                                    "GKANA"             => array(120,"保護者カナ氏名"),
                                    "RECEPTDATE"        => array(10, "志願受付日"),
                                    //"??"         => array(?,  "検定料収納日"),
                                    //"??"         => array(?,  "推薦"),
                                    "REMARK3"           => array(3,  "実力テスト1"),
                                    "REMARK4"           => array(3,  "実力テスト2"),
                                    "REMARK8"           => array(2,  "確約区分"),
                                    //"REMARK1"           => array(30, "クラブ名"),
                                    "REMARK2"           => array(90, "推薦理由"));
                                    //"??"         => array(?,  "当日のチェック"),
                                    //"??"         => array(?,  "最大志望数"),
                                    //"??"         => array(?,  "入試所属1"),
                                    //"??"         => array(?,  "備考"),
            //調査書
            } else {
                $data_each = array( "SHORI_MEI"             => array(1,  "処理名"),
                                    "ENTEXAMYEAR"           => array(4,  "入試年度"),
                                    "EXAMNO"                => array(5,  "受験番号"),
                                    "CONFIDENTIAL_RPT01"    => array(2,  "国語"),
                                    "CONFIDENTIAL_RPT02"    => array(2,  "社会"),
                                    "CONFIDENTIAL_RPT03"    => array(2,  "数学"),
                                    "CONFIDENTIAL_RPT04"    => array(2,  "理科"),
                                    "CONFIDENTIAL_RPT05"    => array(2,  "音楽"),
                                    "CONFIDENTIAL_RPT06"    => array(2,  "美術"),
                                    "CONFIDENTIAL_RPT07"    => array(2,  "保体"),
                                    "CONFIDENTIAL_RPT08"    => array(2,  "技家"),
                                    "CONFIDENTIAL_RPT09"    => array(2,  "外国"),
                                    "CONFIDENTIAL_RPT10"    => array(2,  "教科10"),
                                    "AVERAGE_ALL"           => array(7,  "全教科平均"),
                                    "TOTAL_ALL"             => array(3,  "全教科合計"),
                                    "ABSENCE_DAYS"          => array(3,  "欠席日数（1年）"),
                                    "ABSENCE_DAYS2"         => array(3,  "欠席日数（2年）"),
                                    "ABSENCE_DAYS3"         => array(3,  "欠席日数（3年）"),
                                    "ABSENCE_REMARK"        => array(90, "主な欠席理由（1年）"),
                                    "ABSENCE_REMARK2"       => array(90, "主な欠席理由（2年）"),
                                    "ABSENCE_REMARK3"       => array(90, "主な欠席理由（3年）"),
                                    "BASE_FLG"              => array(1,  "基本フラグ"),
                                    "HEALTH_FLG"            => array(1,  "健康フラグ"),
                                    "ACTIVE_FLG"            => array(1,  "自主フラグ"),
                                    "RESPONSIBLE_FLG"       => array(1,  "責任フラグ"),
                                    "ORIGINAL_FLG"          => array(1,  "創意フラグ"),
                                    "MIND_FLG"              => array(1,  "思いフラグ"),
                                    "NATURE_FLG"            => array(1,  "自然フラグ"),
                                    "WORK_FLG"              => array(1,  "勤労フラグ"),
                                    "JUSTICE_FLG"           => array(1,  "公正フラグ"),
                                    "PUBLIC_FLG"            => array(1,  "公共フラグ"),
                                    "REMARK_CLUB"           => array(30, "クラブ名"),
                                    "REMARK1"               => array(240,"特活・部活・特記事項要約"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl210bQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "1") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    if ($this->field["DATADIV"] === '1' && $checkcount != 25) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    } else if ($this->field["DATADIV"] === '2' && $checkcount != 32) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }
                    //願書
                    if ($this->field["DATADIV"] === '1') {
                        $data = array ( "SHORI_MEI"     => $tmp[0],//テーブルにはセットしない
                                        "ENTEXAMYEAR"   => $tmp[1],
                                        "APPLICANTDIV"  => $tmp[2],
                                        "TESTDIV"       => $tmp[3],
                                        "TESTDIV1"      => $tmp[4],
                                        "SELECT_SUBCLASS_DIV" => (strlen(trim($tmp[5]))) ? trim($tmp[5]) : "",
                                        "EXAMNO"        => sprintf("%05d",$tmp[6]),
                                        "NAME"          => $tmp[7],
                                        "NAME_KANA"     => $tmp[8],
                                        "BIRTHDAY"      => (strlen(trim($tmp[9]))) ? str_replace("/", "-", $tmp[9]) : "", 
                                        "SEX"           => (strlen(trim($tmp[10]))) ? trim($tmp[10]) : "",
                                        "FS_CD"         => (strlen(trim($tmp[11]))) ? sprintf("%07d",$tmp[11]) : "",
                                        "FS_DAY"        => (strlen(trim($tmp[12]))) ? str_replace("/", "-", $tmp[12]) : "", 
                                        "FS_GRDDIV"     => (strlen(trim($tmp[13]))) ? trim($tmp[13]) : "",
                                        "ZIPCD"         => (strlen(trim($tmp[14]))) ? trim($tmp[14]) : "",
                                        "ADDRESS1"      => $tmp[15],
                                        "ADDRESS2"      => $tmp[16],
                                        "TELNO"         => $tmp[17],
                                        "GNAME"         => $tmp[18],
                                        "GKANA"         => $tmp[19],
                                        "RECEPTDATE"    => (strlen(trim($tmp[20]))) ? str_replace("/", "-", $tmp[20]) : "", 
                                        "REMARK3"       => $tmp[21],
                                        "REMARK4"       => $tmp[22],
                                        "REMARK8"       => sprintf("%02d",$tmp[23]),
                                        //"REMARK1"       => $tmp[23],
                                        "REMARK2"       => $tmp[24]);
                    //調査書
                    } else {
                        $data = array ( "SHORI_MEI"          => $tmp[0],//テーブルにはセットしない
                                        "ENTEXAMYEAR"         => $tmp[1],
                                        "EXAMNO"              => sprintf("%05d",$tmp[2]),
                                        "CONFIDENTIAL_RPT01"  => $tmp[3],
                                        "CONFIDENTIAL_RPT02"  => $tmp[4],
                                        "CONFIDENTIAL_RPT03"  => $tmp[5],
                                        "CONFIDENTIAL_RPT04"  => $tmp[6], 
                                        "CONFIDENTIAL_RPT05"  => $tmp[7],
                                        "CONFIDENTIAL_RPT06"  => $tmp[8],
                                        "CONFIDENTIAL_RPT07"  => $tmp[9],
                                        "CONFIDENTIAL_RPT08"  => $tmp[10],
                                        "CONFIDENTIAL_RPT09"  => $tmp[11],
                                        "AVERAGE_ALL"         => round((($tmp[3] + $tmp[4] + $tmp[5] + $tmp[6] + $tmp[7] + $tmp[8] + $tmp[9] + $tmp[10] + $tmp[11]) / 9), 2), //$tmp[12],
                                        "TOTAL_ALL"           => ($tmp[3] + $tmp[4] + $tmp[5] + $tmp[6] + $tmp[7] + $tmp[8] + $tmp[9] + $tmp[10] + $tmp[11]), //$tmp[13],
                                        "ABSENCE_DAYS"        => $tmp[14],
                                        "ABSENCE_DAYS2"       => $tmp[15],
                                        "ABSENCE_DAYS3"       => $tmp[16],
                                        "ABSENCE_REMARK"      => $tmp[17],
                                        "ABSENCE_REMARK2"     => $tmp[18],
                                        "ABSENCE_REMARK3"     => $tmp[19],
                                        "BASE_FLG"            => (strlen($tmp[20])) ? $tmp[20] : "0",
                                        "HEALTH_FLG"          => (strlen($tmp[21])) ? $tmp[21] : "0",
                                        "ACTIVE_FLG"          => (strlen($tmp[22])) ? $tmp[22] : "0",
                                        "RESPONSIBLE_FLG"     => (strlen($tmp[23])) ? $tmp[23] : "0",
                                        "ORIGINAL_FLG"        => (strlen($tmp[24])) ? $tmp[24] : "0",
                                        "MIND_FLG"            => (strlen($tmp[25])) ? $tmp[25] : "0",
                                        "NATURE_FLG"          => (strlen($tmp[26])) ? $tmp[26] : "0",
                                        "WORK_FLG"            => (strlen($tmp[27])) ? $tmp[27] : "0",
                                        "JUSTICE_FLG"         => (strlen($tmp[28])) ? $tmp[28] : "0",
                                        "PUBLIC_FLG"          => (strlen($tmp[29])) ? $tmp[29] : "0",
                                        "REMARK_CLUB"         => $tmp[30],
                                        "REMARK1"             => $tmp[31]);
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $data_each, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);

        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErr1Flg = false;   //存在エラーフラグ
        $existenceErr2Flg = false;   //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dateErrFlg      = false;    //日付エラーフラグ
        $etcErr1flg      = false;    //その他エラーフラグ
        $etcErr2flg      = false;    //その他エラーフラグ
        
        
        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErr1Msg = "";      //存在エラーメッセージ
        $existenceErr2Msg = "";      //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrFlgMsg   = "";       //日付エラーメッセージ
        $etcErr1flgMsg   = "";       //その他エラーメッセージ
        $etcErr2flgMsg   = "";       //その他エラーメッセージ
        
        $hissuSep     = "";
        $existence1Sep = "";
        $existence2Sep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dateSep      = "";
        $etc1Sep       = "";
        $etc2Sep       = "";
        
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("SHORI_MEI","ENTEXAMYEAR","EXAMNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //追加、更新時のチェック
            if ($this->field["SHORI_MEI"] != "3") {
                //願書データのみの必須項目チェック
                if ($this->field["DATADIV"] === '1') {
                    if (in_array($key,array("APPLICANTDIV","TESTDIV","TESTDIV1","NAME","NAME_KANA"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                }
                //入試年度チェック
                if (in_array($key,array("ENTEXAMYEAR"))) {
                    if ($data[$key] != $this->ObjYear) {
                        $returnFlg = $this->errSet($etcErr1flg, $etcErr1flgMsg, "※不一致:", $etc1Sep, $val[1]);
                    }
                }
                //数値チェック
                if (in_array($key,array("SHORI_MEI","ENTEXAMYEAR","APPLICANTDIV","TESTDIV","TESTDIV1","REMARK3","REMARK4","REMARK8","SEX","FS_CD","FS_GRDDIV",
                                        "CONFIDENTIAL_RPT01","CONFIDENTIAL_RPT02","CONFIDENTIAL_RPT03","CONFIDENTIAL_RPT04","CONFIDENTIAL_RPT05",
                                        "CONFIDENTIAL_RPT06","CONFIDENTIAL_RPT07","CONFIDENTIAL_RPT0","CONFIDENTIAL_RPT09","CONFIDENTIAL_RPT10",
                                        "ABSENCE_DAYS","ABSENCE_DAYS2","ABSENCE_DAYS3","BASE_FLG","HEALTH_FLG","ACTIVE_FLG","RESPONSIBLE_FLG","ORIGINAL_FLG",
                                        "MIND_FLG","NATURE_FLG","WORK_FLG","JUSTICE_FLG","PUBLIC_FLG"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //フラグチェック
                if (in_array($key,array("BASE_FLG","HEALTH_FLG","ACTIVE_FLG","RESPONSIBLE_FLG","ORIGINAL_FLG",
                                        "MIND_FLG","NATURE_FLG","WORK_FLG","JUSTICE_FLG","PUBLIC_FLG"))) {
                    if ($data[$key] != "" && ($data[$key] > 1)) {
                        $returnFlg = $this->errSet($etcErr2flg, $etcErr2flgMsg, "※数値不正(0か1):", $etc2Sep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                
                //日付チェック
                if (in_array($key,array("BIRTHDAY","FS_DAY","RECEPTDATE"))) {
                    //日付がセットされている時のみ
                    if (strlen($data[$key])) {
                        $data_check = $this->date_rep($data[$key]);
                        if ($data_check) {
                            $data[$key] = $data_check;
                        } else {
                            $returnFlg = $this->errSet($dateErrFlg, $dateErrFlgMsg, "※日付:", $dateSep, $val[1]);
                        }
                    }
                }
            //削除時は必須項目の数値、文字数チェック
            } else {
                if (in_array($key,array("SHORI_MEI","ENTEXAMYEAR"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                if (in_array($key,array("SHORI_MEI","ENTEXAMYEAR","EXAMNO"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }
        
        //処理名チェック
        if ($this->field["SHORI_MEI"] != $data["SHORI_MEI"]) {
            $returnFlg = $this->errSet($etcErr1flg, $etcErr1flgMsg, "※不一致:", $etc1Sep, "処理名");
        }
        
        //願書データ
        if ($this->field["DATADIV"] === '1') {
            //入試制度チェック
            if ($this->field["APPLICANTDIV"] != $data["APPLICANTDIV"]) {
                $returnFlg = $this->errSet($etcErr1flg, $etcErr1flgMsg, "※不一致:", $etc1Sep, "入試制度");
            }
            //入試区分チェック
            if ($this->field["TESTDIV"] != $data["TESTDIV"]) {
                $returnFlg = $this->errSet($etcErr1flg, $etcErr1flgMsg, "※不一致:", $etc1Sep, "入試区分");
            }
            //追加、更新時のチェック
            if ($this->field["SHORI_MEI"] != "3") {
                //受験型チェック
                //名称マスタチェック
                if (0 == $db->getOne(knjl210bQuery::dataCntTestDiv1Sql($this, $data["TESTDIV1"]))) {
                    $returnFlg = $this->errSet($existenceErr1Flg, $existenceErr1Msg, "※存在しない:", $existence1Sep, "受験科目数コード");
                } else {
                    //推薦の時は3教科のみ
                    if ($this->field["TESTDIV"] === '1' && $this->field["TESTDIV"] != $data["TESTDIV1"]) {
                        $returnFlg = $this->errSet($etcErr1flg, $etcErr1flgMsg, "※不一致:", $etc1Sep, "受験科目数コード");
                    }
                }
                //存在チェック中学校(追加、更新のみ)
                if (strlen(trim($data["FS_CD"]))) {
                    if (0 == $db->getOne(knjl210bQuery::dataCntSchoolSql($this, $data["FS_CD"]))) {
                        $returnFlg = $this->errSet($existenceErr1Flg, $existenceErr1Msg, "※存在しない:", $existence1Sep, "中学校コード");
                    }
                }
                //存在チェック希望コース
                if (strlen(trim($data["SELECT_SUBCLASS_DIV"]))) {
                    if (0 == $db->getOne(knjl210bQuery::dataCntNameMstL033Sql($this, $data["SELECT_SUBCLASS_DIV"]))) {
                        $returnFlg = $this->errSet($existenceErr1Flg, $existenceErr1Msg, "※存在しない:", $existence1Sep, "希望コース");
                    }
                }
                //存在チェック
                if (strlen(trim($data["ENTEXAMYEAR"])) && strlen(trim($data["APPLICANTDIV"])) && strlen(trim($data["TESTDIV"])) && strlen(trim($data["EXAMNO"]))) {
                    //追加
                    if ($this->field["SHORI_MEI"] === '1') {
                        if ($data["SHORI_MEI"] == "1" && 0 < $db->getOne(knjl210bQuery::dataCntGanshoSql($this, $data["ENTEXAMYEAR"], $data["EXAMNO"],""))) {
                            $returnFlg = $this->errSet($existenceErr2Flg, $existenceErr2Msg, "※存在:", $existence2Sep, "受験番号");
                        }
                    //更新
                    } else {
                        if ($data["SHORI_MEI"] == "2" && 0 == $db->getOne(knjl210bQuery::dataCntGanshoSql($this, $data["ENTEXAMYEAR"], $data["EXAMNO"],""))) {
                            $returnFlg = $this->errSet($existenceErr1Flg, $existenceErr1Msg, "※存在しない:", $existence1Sep, "受験番号");
                        }
                    }
                }
            }
        //調査書データ
        } else {
            //追加、更新時のチェック
            //存在チェック
            if ($this->field["SHORI_MEI"] !== '3' && strlen(trim($data["ENTEXAMYEAR"])) && strlen(trim($data["EXAMNO"]))) {
                //志願者基礎データチェック
                /*if (0 == $db->getOne(knjl210bQuery::dataCntGanshoSql($this, $data["ENTEXAMYEAR"], $data["EXAMNO"],"CHECK"))) {
                    $returnFlg = $this->errSet($existenceErr1Flg, $existenceErr1Msg, "※存在しない:", $existence1Sep, "志願者基礎データ");
                }*/
                //追加
                if ($this->field["SHORI_MEI"] === '1') {
                    if ($data["SHORI_MEI"] == "1" && 0 < $db->getOne(knjl210bQuery::dataCntChosashoSql($this, $data["ENTEXAMYEAR"], $data["EXAMNO"]))) {
                        $returnFlg = $this->errSet($existenceErr2Flg, $existenceErr2Msg, "※存在:", $existence2Sep, "受験番号");
                    }
                //更新
                } else {
                    if ($data["SHORI_MEI"] == "2" && 0 == $db->getOne(knjl210bQuery::dataCntChosashoSql($this, $data["ENTEXAMYEAR"], $data["EXAMNO"]))) {
                        $returnFlg = $this->errSet($existenceErr1Flg, $existenceErr1Msg, "※存在しない:", $existence1Sep, "受験番号");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErr1Msg.$existenceErr2Msg.$numericErrMsg.$lengthErrMsg.$dateErrFlgMsg.$etcErr1flgMsg.$etcErr2flgMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl210bQuery::insertQueryErr($db, $line, $check_error);
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

    //テンプレートの書き出し
    function OutputDataFile()
    {
        if ($this->field["DATADIV"] === '1') {
            //願書
            $header = array("SHORI_MEI"       => "※処理名",
                            "ENTEXAMYEAR"     => "※入試年度",
                            "APPLICANTDIV"    => "※入試制度",
                            "TESTDIV"         => "※入試区分",
                            "TESTDIV1"        => "※受験科目数コード",
                            "SELECT_SUBCLASS_DIV" => "希望コース",
                            "EXAMNO"          => "※受験番号",
                            "NAME"            => "※志願者氏名",
                            "NAME_KANA"       => "※志願者かな氏名",
                            "BIRTHDAY"        => "志願者生年月日",
                            "SEX"             => "志願者性別",
                            "FS_CD"           => "中学校コード",
                            "FS_DAY"          => "中学校卒業日付",
                            "FS_GRDDIV"       => "志願時状態",
                            "ZIPCD"           => "郵便番号",
                            "ADDRESS1"        => "住所1",
                            "ADDRESS2"        => "住所2",
                            "TELNO"           => "電話番号",
                            "GNAME"           => "保護者漢字氏名",
                            "GKANA"           => "保護者カナ氏名",
                            "RECEPTDATE"      => "志願受付日",
                            "REMARK3"         => "実力テスト1",
                            "REMARK4"         => "実力テスト2",
                            "REMARK8"         => "確約区分",
                            //"REMARK1"         => "クラブ名",
                            "REMARK2"         => "推薦理由");

            $header_rei = array("SHORI_MEI"       => "1",
                                "ENTEXAMYEAR"     => "2014",
                                "APPLICANTDIV"    => "1",
                                "TESTDIV"         => "1",
                                "TESTDIV1"        => "1",
                                "SELECT_SUBCLASS_DIV" => "A",
                                "EXAMNO"          => "10001",
                                "NAME"            => "山田　太郎",
                                "NAME_KANA"       => "やまだ　たろう",
                                "BIRTHDAY"        => "1998-05-05",
                                "SEX"             => "1",
                                "FS_CD"           => "1234567",
                                "FS_DAY"          => "2014-03-31",
                                "FS_GRDDIV"       => "1",
                                "ZIPCD"           => "124-0000",
                                "ADDRESS1"        => "東京都葛飾区999番地",
                                "ADDRESS2"        => "○○マンション　201号",
                                "TELNO"           => "123-456-7890",
                                "GNAME"           => "山田　幸太郎",
                                "GKANA"           => "やまだ　こうたろう",
                                "RECEPTDATE"      => "2014-01-30",
                                "REMARK3"         => "450",
                                "REMARK4"         => "550",
                                "REMARK8"         => "10",
                                //"REMARK1"         => "水泳",
                                "REMARK2"         => "中体連　優勝");
        } else {
            //調査書
            $header = array("SHORI_MEI"                 => "※処理名",
                            "ENTEXAMYEAR"               => "※入試年度",
                            "EXAMNO"                    => "※受験番号",
                            "CONFIDENTIAL_RPT01"        => "国語",
                            "CONFIDENTIAL_RPT02"        => "社会",
                            "CONFIDENTIAL_RPT03"        => "数学",
                            "CONFIDENTIAL_RPT04"        => "理科",
                            "CONFIDENTIAL_RPT05"        => "音楽",
                            "CONFIDENTIAL_RPT06"        => "美術",
                            "CONFIDENTIAL_RPT07"        => "保体",
                            "CONFIDENTIAL_RPT08"        => "技家",
                            "CONFIDENTIAL_RPT09"        => "外国",
                            "AVERAGE_ALL"               => "全教科平均",
                            "TOTAL_ALL"                 => "全教科合計",
                            "ABSENCE_DAYS"              => "欠席日数（1年）",
                            "ABSENCE_DAYS2"             => "欠席日数（2年）",
                            "ABSENCE_DAYS3"             => "欠席日数（3年）",
                            "ABSENCE_REMARK"            => "主な欠席理由（1年）",
                            "ABSENCE_REMARK2"           => "主な欠席理由（2年）",
                            "ABSENCE_REMARK3"           => "主な欠席理由（3年）",
                            "BASE_FLG"                  => "基本フラグ",
                            "HEALTH_FLG"                => "健康フラグ",
                            "ACTIVE_FLG"                => "自主フラグ",
                            "RESPONSIBLE_FLG"           => "責任フラグ",
                            "ORIGINAL_FLG"              => "創意フラグ",
                            "MIND_FLG"                  => "思いフラグ",
                            "NATURE_FLG"                => "自然フラグ",
                            "WORK_FLG"                  => "勤労フラグ",
                            "JUSTICE_FLG"               => "公正フラグ",
                            "PUBLIC_FLG"                => "公共フラグ",
                            "REMARK_CLUB"               => "クラブ名",
                            "REMARK1"                   => "特活・部活・特記事項要約");

            $header_rei = array("SHORI_MEI"                 => "1",
                                "ENTEXAMYEAR"               => "2014",
                                "EXAMNO"                    => "10001",
                                "CONFIDENTIAL_RPT01"        => "5",
                                "CONFIDENTIAL_RPT02"        => "4",
                                "CONFIDENTIAL_RPT03"        => "5",
                                "CONFIDENTIAL_RPT04"        => "3",
                                "CONFIDENTIAL_RPT05"        => "5",
                                "CONFIDENTIAL_RPT06"        => "5",
                                "CONFIDENTIAL_RPT07"        => "4",
                                "CONFIDENTIAL_RPT08"        => "3",
                                "CONFIDENTIAL_RPT09"        => "5",
                                "AVERAGE_ALL"               => "4.33",
                                "TOTAL_ALL"                 => "39",
                                "ABSENCE_DAYS"              => "2",
                                "ABSENCE_DAYS2"             => "13",
                                "ABSENCE_DAYS3"             => "4",
                                "ABSENCE_REMARK"            => "特になし",
                                "ABSENCE_REMARK2"           => "入院治療の為",
                                "ABSENCE_REMARK3"           => "特になし",
                                "BASE_FLG"                  => "1",
                                "HEALTH_FLG"                => "0",
                                "ACTIVE_FLG"                => "1",
                                "RESPONSIBLE_FLG"           => "1",
                                "ORIGINAL_FLG"              => "0",
                                "MIND_FLG"                  => "0",
                                "NATURE_FLG"                => "1",
                                "WORK_FLG"                  => "0",
                                "JUSTICE_FLG"               => "1",
                                "PUBLIC_FLG"                => "1",
                                "REMARK_CLUB"               => "水泳",
                                "REMARK1"                   => "全日本代表に選出");
        }

        $contents = "";
        $header_data = false;
        
        //DB接続
        $db = Query::dbCheckOut();
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= "行番号,メッセージ\n";
            $query = knjl210bQuery::selectCsvErrQuery();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {
                if ($this->field["DATADIV"] == "1") {
                    $filename = sprintf("%s.csv","願書データ取込エラー");
                } else {
                    $filename = sprintf("%s.csv","調査書データ取込エラー");
                }
            } else {                                
                //ヘッダ出力
                if ($this->field["DATADIV"] == "1") {
                    $filename = sprintf("%s.csv","願書データフォーマット");
                } else {
                    $filename = sprintf("%s.csv","調査書データフォーマット");
                }
            }
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }
}
?>
