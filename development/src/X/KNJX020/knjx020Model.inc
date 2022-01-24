<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx020Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx020Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),         //学籍番号
                             "SCHREGNO_FROM"    =>  VARS::post("SCHREGNO_FROM"),    //学籍番号（開始）
                             "SCHREGNO_TO"      =>  VARS::post("SCHREGNO_TO"),      //学籍番号（終了）
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd          = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * データ取込
    */
    public function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    if ($this->field["SHORI_MEI"] == "1") {
                        //学籍基礎マスタの更新
                        $table = "schreg_base_mst";
                        $where = "where SCHREGNO = '".$this->dataArr[$i]["SCHREGNO"]."'";
                        $query = knjx020Query::getMasterSQL($table, $where);
                        $result = $db->query($query);
                        $flg = false;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $flg = true;
                        }

                        //学籍なしは、最初に作っておく
                        if (!$flg) {
                            knjx020Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd, $hist_data, $this);
                        }

                        //入学卒業履歴データ更新
                        $table = "SCHREG_ENT_GRD_HIST_DAT";
                        $where  = "WHERE SCHREGNO = '".$this->dataArr[$i]["SCHREGNO"]."' AND ";
                        $where .= "      SCHOOL_KIND = '".$this->dataArr[$i]["SCHOOL_KIND"]."'";
                        $query = knjx020Query::getMasterSQL($table, $where);
                        $result = $db->query($query);
                        $hist_flg = false;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $hist_flg = true;
                        }
                        knjx020Query::updateHistQueryCsv($db, $this->dataArr[$i], $this->staffcd, $hist_flg, $this);

                        //入学卒業履歴データの最新データを取得
                        $hist_data = $db->getRow(knjx020Query::getMainSQL($this, $this->dataArr[$i]["SCHREGNO"]), DB_FETCHMODE_ASSOC);

                        //学籍基礎マスタの更新
                        knjx020Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd, $hist_data, $this);
                    } else {
                        //入学卒業履歴データの削除
                        $table = "SCHREG_ENT_GRD_HIST_DAT";
                        $where = "WHERE SCHREGNO = '".$this->dataArr[$i]["SCHREGNO"]."'";
                        $query = knjx020Query::getMasterSQL($table, $where);
                        $result = $db->query($query);
                        $hist_flg = false;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $hist_flg = true;
                        }
                        //ＤＢにあれば、delete
                        if ($hist_flg) {
                            knjx020Query::deleteHistQueryCsv($db, $this->dataArr[$i]);
                        }

                        //学籍基礎マスタの削除
                        $table = "schreg_base_mst";
                        $where = "where SCHREGNO = '".$this->dataArr[$i]["SCHREGNO"]."'";
                        $query = knjx020Query::getMasterSQL($table, $where);
                        $result = $db->query($query);
                        $flg = false;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $flg = true;
                        }
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx020Query::deleteQueryCsv($db, $this->dataArr[$i], $this);
                        }
                    }
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //更新(データ取込)
    public function fileUpload()
    {
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $nameSLen  = $this->Properties["NAME_INPUT_SIZE"] ? intval($this->Properties["NAME_INPUT_SIZE"]) : 40;
            $nameSLen2 = $this->Properties["NAME_INPUT_SIZE"] ? intval($this->Properties["NAME_INPUT_SIZE"]) : 20;
            $examLen   = $this->Properties["examnoLen"] ? $this->Properties["examnoLen"] : "5";
            $nameLen   = $this->Properties["NAME_INPUT_SIZE"] != "" ? intval($nameSLen) : $nameSLen * 3;
            $nameLen2  = $this->Properties["NAME_INPUT_SIZE"] != "" ? intval($nameSLen2) : intval($nameSLen2) * 3;
            $checkData = array("SCHREGNO"            => array(8, "※学籍番号"),
                               "INOUTCD"             => array(1, "内外区分"),
                               "NAME"                => array($nameLen, "※生徒氏名"),
                               "REAL_NAME"           => array($nameLen, "生徒戸籍氏名"),
                               "EMERGENCYNAME"       => array($nameLen2, "急用連絡先名1"),
                               "EMERGENCYNAME2"      => array($nameLen2, "急用連絡先名2"),
                               "ENT_ADDR2"           => array(90, "入学住所2"),
                               "GRD_ADDR2"           => array(90, "除籍(卒業)住所2"),
                               "NAME_SHOW"           => array(30, "生徒氏名表示用"),
                               "NAME_KANA"           => array(240, "生徒氏名かな"),
                               "NAME_ENG"            => array(40, "生徒氏名英字"),
                               "REAL_NAME_KANA"      => array(240, "生徒戸籍氏名かな"),
                               "BIRTHDAY"            => array(10, "生年月日"),
                               "SEX"                 => array(1, "性別"),
                               "BLOODTYPE"           => array(2, "血液型"),
                               "BLOOD_RH"            => array(1, "血液RH型"),
                               "HANDICAP"            => array(3, "その他"),
                               "NATIONALITY"         => array(3, "国籍"),
                               "SCHOOL_KIND"         => array(2, "※校種"),
                               "FINSCHOOLCD"         => array($this->finschoolcdKeta, "出身校コード"),
                               "FINISH_DATE"         => array(10, "出身校卒業年月日"),
                               "PRISCHOOLCD"         => array(7, "塾コード"),
                               "PRISCHOOL_CLASS_CD"  => array(7, "教室コード"),
                               "CURRICULUM_YEAR"     => array(4, "課程入学年度"),
                               "ENT_DATE"            => array(10, "入学日付"),
                               "ENT_DIV"             => array(1, "入学区分"),
                               "ENT_REASON"          => array(120, "入学理由"),
                               "ENT_SCHOOL"          => array(75, "入学学校"),
                               "ENT_ADDR"            => array(90, "入学住所1"),
                               "GRD_DATE"            => array(10, "除籍(卒業)日付"),
                               "GRD_DIV"             => array(1, "除籍(卒業)区分"),
                               "GRD_REASON"          => array(75, "除籍(卒業)事由"),
                               "GRD_SCHOOL"          => array(75, "除籍(卒業)学校"),
                               "GRD_ADDR"            => array(90, "除籍(卒業)住所1"),
                               "GRD_NO"              => array(8, "卒業生台帳番号"),
                               "GRD_TERM"            => array(4, "卒業期"),
                               "REMARK1"             => array(75, "備考１"),
                               "REMARK2"             => array(75, "備考２"),
                               "REMARK3"             => array(75, "備考３"),
                               "EMERGENCYCALL"       => array(60, "急用連絡先1"),
                               "EMERGENCYRELA_NAME"  => array(30, "急用連絡先続柄1"),
                               "EMERGENCYTELNO"      => array(14, "急用電話番号1_1"),
                               "E_TELNO_MEMO"        => array(24, "急用電話番号備考1_1"),
                               "EMERGENCYTELNO_2"    => array(14, "急用電話番号1_2"),
                               "E_TELNO_MEMO_2"      => array(24, "急用電話番号備考1_2"),
                               "EMERGENCYCALL2"      => array(60, "急用連絡先2"),
                               "EMERGENCYRELA_NAME2" => array(30, "急用連絡先続柄2"),
                               "EMERGENCYTELNO2"     => array(14, "急用電話番号2_1"),
                               "E_TELNO_MEMO2"       => array(24, "急用電話番号備考2_1"),
                               "EMERGENCYTELNO2_2"   => array(14, "急用電話番号2_2"),
                               "E_TELNO_MEMO2_2"     => array(24, "急用電話番号備考2_2"),
                               "EXAMNO"              => array($examLen, "受験番号"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)
            $i = 0;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey("SCHREG_BASE_MST");
            $primaryKey2 = $this->getPrimaryKey("SCHREG_ENT_GRD_HIST_DAT");
            $primaryKey3 = $this->getPrimaryKey("SCHREG_BASE_DETAIL_MST");

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);
                    $tmp = str_replace("↓", "\n", $tmp);

                    $dataCnt = 0;
                    $data = array("SCHREGNO"                    => trim($tmp[$dataCnt++]),
                                  "INOUTCD"                     => trim($tmp[$dataCnt++]),
                                  "NAME"                        => trim($tmp[$dataCnt++]),
                                  "NAME_SHOW"                   => trim($tmp[$dataCnt++]),
                                  "NAME_KANA"                   => trim($tmp[$dataCnt++]),
                                  "NAME_ENG"                    => trim($tmp[$dataCnt++]),
                                  "REAL_NAME"                   => trim($tmp[$dataCnt++]),
                                  "REAL_NAME_KANA"              => trim($tmp[$dataCnt++]),
                                  "BIRTHDAY"                    => trim($tmp[$dataCnt++]),
                                  "SEX"                         => trim($tmp[$dataCnt++]),
                                  "BLOODTYPE"                   => trim($tmp[$dataCnt++]),
                                  "BLOOD_RH"                    => trim($tmp[$dataCnt++]),
                                  "HANDICAP"                    => strlen($tmp[$dataCnt++]) > 0 ? sprintf('%03s', trim($tmp[$dataCnt - 1])) : "",
                                  "NATIONALITY"                 => strlen($tmp[$dataCnt++]) > 0 ? sprintf('%03s', trim($tmp[$dataCnt - 1])) : "",
                                  "SCHOOL_KIND"                 => trim($tmp[$dataCnt++]),
                                  "ORIGIN"                      => $this->Properties["Origin_hyouji"] == "1" ? trim($tmp[$dataCnt++]) : "",
                                  "FINSCHOOLCD"                 => trim($tmp[$dataCnt++]),
                                  "FINISH_DATE"                 => trim($tmp[$dataCnt++]),
                                  "PRISCHOOLCD"                 => trim($tmp[$dataCnt++]),
                                  "PRISCHOOL_CLASS_CD"          => trim($tmp[$dataCnt++]),
                                  "CURRICULUM_YEAR"             => trim($tmp[$dataCnt++]),
                                  "ENT_DATE"                    => trim($tmp[$dataCnt++]),
                                  "ENT_DIV"                     => trim($tmp[$dataCnt++]),
                                  "ENT_REASON"                  => trim($tmp[$dataCnt++]),
                                  "ENT_SCHOOL"                  => trim($tmp[$dataCnt++]),
                                  "ENT_ADDR"                    => trim($tmp[$dataCnt++]),
                                  "ENT_ADDR2"                   => $this->Properties["useAddrField2"] == "1" ? trim($tmp[$dataCnt++]) : "",
                                  "GRD_DATE"                    => trim($tmp[$dataCnt++]),
                                  "GRD_DIV"                     => trim($tmp[$dataCnt++]),
                                  "GRD_REASON"                  => trim($tmp[$dataCnt++]),
                                  "GRD_SCHOOL"                  => trim($tmp[$dataCnt++]),
                                  "GRD_ADDR"                    => trim($tmp[$dataCnt++]),
                                  "GRD_ADDR2"                   => $this->Properties["useAddrField2"] == "1" ? trim($tmp[$dataCnt++]) : "",
                                  "GRD_NO"                      => trim($tmp[$dataCnt++]),
                                  "GRD_TERM"                    => trim($tmp[$dataCnt++]),
                                  "REMARK1"                     => trim($tmp[$dataCnt++]),
                                  "REMARK2"                     => trim($tmp[$dataCnt++]),
                                  "REMARK3"                     => trim($tmp[$dataCnt++]),
                                  "EMERGENCYCALL"               => trim($tmp[$dataCnt++]),
                                  "EMERGENCYNAME"               => trim($tmp[$dataCnt++]),
                                  "EMERGENCYRELA_NAME"          => trim($tmp[$dataCnt++]),
                                  "EMERGENCYTELNO"              => trim($tmp[$dataCnt++]),
                                  "E_TELNO_MEMO"                => trim($tmp[$dataCnt++]),
                                  "EMERGENCYTELNO_2"            => trim($tmp[$dataCnt++]),
                                  "E_TELNO_MEMO_2"              => trim($tmp[$dataCnt++]),
                                  "EMERGENCYCALL2"              => trim($tmp[$dataCnt++]),
                                  "EMERGENCYNAME2"              => trim($tmp[$dataCnt++]),
                                  "EMERGENCYRELA_NAME2"         => trim($tmp[$dataCnt++]),
                                  "EMERGENCYTELNO2"             => trim($tmp[$dataCnt++]),
                                  "E_TELNO_MEMO2"               => trim($tmp[$dataCnt++]),
                                  "EMERGENCYTELNO2_2"           => trim($tmp[$dataCnt++]),
                                  "E_TELNO_MEMO2_2"             => trim($tmp[$dataCnt++]),
                                  "TENGAKU_SAKI_ZENJITU"        => trim($tmp[$dataCnt++]),
                                  "NYUGAKUMAE_SYUSSIN_JOUHOU"   => trim($tmp[$dataCnt++]),
                                  "EXAMNO"                      => trim($tmp[$dataCnt++]),
                                  "BASE_SEQ"                    => "");

                    $data["ENT_ADDR2"] = $this->Properties["useAddrField2"] == "1" ? $data["ENT_ADDR2"] : "";
                    $data["GRD_ADDR2"] = $this->Properties["useAddrField2"] == "1" ? $data["GRD_ADDR2"] : "";

                    if ($this->Properties["useAddrField2"] != "1") {
                        unset($data['ENT_ADDR2']);
                        unset($data['GRD_ADDR2']);
                    }

                    if ($this->Properties["Origin_hyouji"] != "1") {
                        unset($data['ORIGIN']);
                    }

                    if ($data["BIRTHDAY"] != "" && $this->dateRep($data["BIRTHDAY"])) {
                        $data["BIRTHDAY"] = $this->dateRep($data["BIRTHDAY"]);
                    }
                    if ($data["FINISH_DATE"] != "" && $this->dateRep($data["FINISH_DATE"])) {
                        $data["FINISH_DATE"] = $this->dateRep($data["FINISH_DATE"]);
                    }
                    if ($data["ENT_DATE"] != "" && $this->dateRep($data["ENT_DATE"])) {
                        $data["ENT_DATE"] = $this->dateRep($data["ENT_DATE"]);
                    }
                    if ($data["GRD_DATE"] != "" && $this->dateRep($data["GRD_DATE"])) {
                        $data["GRD_DATE"] = $this->dateRep($data["GRD_DATE"]);
                    }
                    if ($data["TENGAKU_SAKI_ZENJITU"] != "" && $this->dateRep($data["TENGAKU_SAKI_ZENJITU"])) {
                        $data["TENGAKU_SAKI_ZENJITU"] = $this->dateRep($data["TENGAKU_SAKI_ZENJITU"]);
                    }

                    //０埋め
                    $data["SCHREGNO"] = $this->zeroUme($data["SCHREGNO"], 8);
                    if (is_numeric($data["FINSCHOOLCD"])) {
                        $data["FINSCHOOLCD"] = $this->zeroUme($data["FINSCHOOLCD"], $this->finschoolcdKeta);
                    }
                    if (is_numeric($data["PRISCHOOLCD"])) {
                        $data["PRISCHOOLCD"] = $this->zeroUme($data["PRISCHOOLCD"], 7);
                    }
                    if (is_numeric($data["PRISCHOOL_CLASS_CD"])) {
                        $data["PRISCHOOL_CLASS_CD"] = $this->zeroUme($data["PRISCHOOL_CLASS_CD"], 7);
                    }
                    if ($this->Properties["Origin_hyouji"] == "1") {
                        $data["ORIGIN"] = $this->zeroUme($data["ORIGIN"], 2);
                    }

                    if ($this->Properties["useSpecial_Support_School"] == "1") {
                        $checkData["HANDICAP"][1] = "教育区分";
                    }

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if ($this->errCheck($db, $data, $checkData, $record_no, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }//$ifの綴り
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //ＣＳＶへエラー出力
    public function getDownloadModel()
    {
        $header = array("MSGROW"            => "行番号",
                        "MSGREMARK"         => "メッセージ");
        $headerDuplicate = array("SCHREGNO"    => "※学籍番号",
                                 "SCHOOL_KIND" => "※校種",
                                 "BASE_SEQ"    => "学籍基礎SEQ");
        $tables = array();
        $tables = array("1" => "SCHREG_BASE_MST", "2" => "SCHREG_ENT_GRD_HIST_DAT", "3" => "SCHREG_BASE_DETAIL_MST");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        $err_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($err_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ヘッダ出力 2004/03/24 nakamoto add
    public function getHeaderModel()
    {
        $header = array("SCHREGNO"                  => "※学籍番号",
                        "INOUTCD"                   => "内外区分",
                        "NAME"                      => "※生徒氏名",
                        "NAME_SHOW"                 => "生徒氏名表示用",
                        "NAME_KANA"                 => "生徒氏名かな",
                        "NAME_ENG"                  => "生徒氏名英字",
                        "REAL_NAME"                 => "生徒戸籍氏名",
                        "REAL_NAME_KANA"            => "生徒戸籍氏名かな",
                        "BIRTHDAY"                  => "生年月日",
                        "SEX"                       => "性別",
                        "BLOODTYPE"                 => "血液型",
                        "BLOOD_RH"                  => "血液RH型",
                        "HANDICAP"                  => "その他",
                        "NATIONALITY"               => "国籍",
                        "SCHOOL_KIND"               => "※校種",
                        "ORIGIN"                    => "出身コード",
                        "FINSCHOOLCD"               => "出身校コード",
                        "FINISH_DATE"               => "出身校卒業年月日",
                        "PRISCHOOLCD"               => "塾コード",
                        "PRISCHOOL_CLASS_CD"        => "教室コード",
                        "CURRICULUM_YEAR"           => "課程入学年度",
                        "ENT_DATE"                  => "入学日付",
                        "ENT_DIV"                   => "入学区分",
                        "ENT_REASON"                => "入学理由",
                        "ENT_SCHOOL"                => "入学学校",
                        "ENT_ADDR"                  => "入学住所1",
                        "ENT_ADDR2"                 => "入学住所2",
                        "GRD_DATE"                  => "除籍(卒業)日付",
                        "GRD_DIV"                   => "除籍(卒業)区分",
                        "GRD_REASON"                => "除籍(卒業)事由",
                        "GRD_SCHOOL"                => "除籍(卒業)学校",
                        "GRD_ADDR"                  => "除籍(卒業)住所1",
                        "GRD_ADDR2"                 => "除籍(卒業)住所2",
                        "GRD_NO"                    => "卒業生台帳番号",
                        "GRD_TERM"                  => "卒業期",
                        "REMARK1"                   => "備考１",
                        "REMARK2"                   => "備考２",
                        "REMARK3"                   => "備考３",
                        "EMERGENCYCALL"             => "急用連絡先1",
                        "EMERGENCYNAME"             => "急用連絡先名1",
                        "EMERGENCYRELA_NAME"        => "急用連絡先続柄1",
                        "EMERGENCYTELNO"            => "急用電話番号1_1",
                        "E_TELNO_MEMO"              => "急用電話番号備考1_1",
                        "EMERGENCYTELNO_2"          => "急用電話番号1_2",
                        "E_TELNO_MEMO_2"            => "急用電話番号備考1_2",
                        "EMERGENCYCALL2"            => "急用連絡先2",
                        "EMERGENCYNAME2"            => "急用連絡先名2",
                        "EMERGENCYRELA_NAME2"       => "急用連絡先続柄2",
                        "EMERGENCYTELNO2"           => "急用電話番号2_1",
                        "E_TELNO_MEMO2"             => "急用電話番号備考2_1",
                        "EMERGENCYTELNO2_2"         => "急用電話番号2_2",
                        "E_TELNO_MEMO2_2"           => "急用電話番号備考2_2",
                        "TENGAKU_SAKI_ZENJITU"      => "転学先入学前日(小/中学校専用)",
                        "NYUGAKUMAE_SYUSSIN_JOUHOU" => "出身学校情報(小学校のみ)",
                        "EXAMNO"                    => "受験番号",
                        $this->lastColumn           => $this->lastColumn);//2005.09.28nakamoto

        $header_rei = array("SCHREGNO"              => "12345678",
                        "INOUTCD"                   => "1",
                        "NAME"                      => "アルプ　太郎",
                        "NAME_SHOW"                 => "アルプ　太郎",
                        "NAME_KANA"                 => "あるぷ　たろう",
                        "NAME_ENG"                  => "Tarou Alp",
                        "REAL_NAME"                 => "アルプ　太郎",
                        "REAL_NAME_KANA"            => "あるぷ　たろう",
                        "BIRTHDAY"                  => "1996-03-01",
                        "SEX"                       => "1",
                        "BLOODTYPE"                 => "A",
                        "BLOOD_RH"                  => "-",
                        "HANDICAP"                  => "001",
                        "NATIONALITY"               => "001",
                        "SCHOOL_KIND"               => "J",
                        "ORIGIN"                    => "01",
                        "FINSCHOOLCD"               => ($this->finschoolcdKeta == 12 ? "123456789012" : "1234567"),
                        "FINISH_DATE"               => "2010-03-31",
                        "PRISCHOOLCD"               => "1234567",
                        "PRISCHOOL_CLASS_CD"        => "1234567",
                        "CURRICULUM_YEAR"           => "2010",
                        "ENT_DATE"                  => "2010-04-01",
                        "ENT_DIV"                   => "2",
                        "ENT_REASON"                => "入学",
                        "ENT_SCHOOL"                => "ＸＸＸ中学",
                        "ENT_ADDR"                  => "東京都ＸＸＸ区ＸＸＸ町Ｘ丁目",
                        "ENT_ADDR2"                 => "住所２",
                        "GRD_DATE"                  => "2013-03-31",
                        "GRD_DIV"                   => "1",
                        "GRD_REASON"                => "卒業",
                        "GRD_SCHOOL"                => "ＸＸＸ高校",
                        "GRD_ADDR"                  => "東京都ＸＸＸ区ＸＸＸ町Ｘ丁目",
                        "GRD_ADDR2"                 => "住所２",
                        "GRD_NO"                    => "99998888",
                        "GRD_TERM"                  => "7",
                        "REMARK1"                   => "備考１",
                        "REMARK2"                   => "備考２",
                        "REMARK3"                   => "備考３",
                        "EMERGENCYCALL"             => "急用連絡先1",
                        "EMERGENCYNAME"             => "急用連絡先名1",
                        "EMERGENCYRELA_NAME"        => "1",
                        "EMERGENCYTELNO"            => "090-0909-0909",
                        "E_TELNO_MEMO"              => "職場",
                        "EMERGENCYTELNO_2"          => "080-0808-0808",
                        "E_TELNO_MEMO_2"            => "携帯電話",
                        "EMERGENCYCALL2"            => "急用連絡先2",
                        "EMERGENCYNAME2"            => "急用連絡先名2",
                        "EMERGENCYRELA_NAME2"       => "2",
                        "EMERGENCYTELNO2"           => "090-0909-0909",
                        "E_TELNO_MEMO2"             => "自宅",
                        "EMERGENCYTELNO2_2"         => "080-0808-0808",
                        "E_TELNO_MEMO2_2"           => "携帯電話",
                        "TENGAKU_SAKI_ZENJITU"      => "2010-03-31",
                        "NYUGAKUMAE_SYUSSIN_JOUHOU" => "出身学校情報",
                        "EXAMNO"                    => "99999",
                        $this->lastColumn           => $this->lastColumn);//2005.09.28nakamoto
        if ($this->Properties["useAddrField2"] != "1") {
            unset($header['ENT_ADDR2']);
            unset($header['GRD_ADDR2']);
            unset($header_rei['ENT_ADDR2']);
            unset($header_rei['GRD_ADDR2']);
            $header["ENT_ADDR"] = "入学住所";
            $header["GRD_ADDR"] = "除籍(卒業)住所";
        }

        if ($this->Properties["Origin_hyouji"] != "1") {
            unset($header['ORIGIN']);
            unset($header_rei['ORIGIN']);
        }

        if ($this->Properties["useSpecial_Support_School"] == "1") {
            $header['HANDICAP'] = "教育区分";
        }

        $contents  = implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        $header_data = true;

        if ($header_data) {
            $couse = "学籍基礎マスタ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //データ出力
    public function getDataModel()
    {
        $header = array("SCHREGNO"                  => "※学籍番号",
                        "INOUTCD"                   => "内外区分",
                        "NAME"                      => "※生徒氏名",
                        "NAME_SHOW"                 => "生徒氏名表示用",
                        "NAME_KANA"                 => "生徒氏名かな",
                        "NAME_ENG"                  => "生徒氏名英字",
                        "REAL_NAME"                 => "生徒戸籍氏名",
                        "REAL_NAME_KANA"            => "生徒戸籍氏名かな",
                        "BIRTHDAY"                  => "生年月日",
                        "SEX"                       => "性別",
                        "BLOODTYPE"                 => "血液型",
                        "BLOOD_RH"                  => "血液RH型",
                        "HANDICAP"                  => "その他",
                        "NATIONALITY"               => "国籍",
                        "SCHOOL_KIND"               => "※校種",
                        "ORIGIN"                    => "出身コード",
                        "FINSCHOOLCD"               => "出身校コード",
                        "FINISH_DATE"               => "出身校卒業年月日",
                        "PRISCHOOLCD"               => "塾コード",
                        "PRISCHOOL_CLASS_CD"        => "教室コード",
                        "CURRICULUM_YEAR"           => "課程入学年度",
                        "ENT_DATE"                  => "入学日付",
                        "ENT_DIV"                   => "入学区分",
                        "ENT_REASON"                => "入学理由",
                        "ENT_SCHOOL"                => "入学学校",
                        "ENT_ADDR"                  => "入学住所1",
                        "ENT_ADDR2"                 => "入学住所2",
                        "GRD_DATE"                  => "除籍(卒業)日付",
                        "GRD_DIV"                   => "除籍(卒業)区分",
                        "GRD_REASON"                => "除籍(卒業)事由",
                        "GRD_SCHOOL"                => "除籍(卒業)学校",
                        "GRD_ADDR"                  => "除籍(卒業)住所1",
                        "GRD_ADDR2"                 => "除籍(卒業)住所2",
                        "GRD_NO"                    => "卒業生台帳番号",
                        "GRD_TERM"                  => "卒業期",
                        "REMARK1"                   => "備考１",
                        "REMARK2"                   => "備考２",
                        "REMARK3"                   => "備考３",
                        "EMERGENCYCALL"             => "急用連絡先1",
                        "EMERGENCYNAME"             => "急用連絡先名1",
                        "EMERGENCYRELA_NAME"        => "急用連絡先続柄1",
                        "EMERGENCYTELNO"            => "急用電話番号1_1",
                        "E_TELNO_MEMO"              => "急用電話番号備考1_1",
                        "EMERGENCYTELNO_2"          => "急用電話番号1_2",
                        "E_TELNO_MEMO_2"            => "急用電話番号備考1_2",
                        "EMERGENCYCALL2"            => "急用連絡先2",
                        "EMERGENCYNAME2"            => "急用連絡先名2",
                        "EMERGENCYRELA_NAME2"       => "急用連絡先続柄2",
                        "EMERGENCYTELNO2"           => "急用電話番号2_1",
                        "E_TELNO_MEMO2"             => "急用電話番号備考2_1",
                        "EMERGENCYTELNO2_2"         => "急用電話番号2_2",
                        "E_TELNO_MEMO2_2"           => "急用電話番号備考2_2",
                        "TENGAKU_SAKI_ZENJITU"      => "転学先入学前日(小/中学校専用)",
                        "NYUGAKUMAE_SYUSSIN_JOUHOU" => "出身学校情報(小学校のみ)",
                        "EXAMNO"                    => "受験番号",
                        $this->lastColumn           => $this->lastColumn);//2005.09.28nakamoto
        if ($this->Properties["useAddrField2"] != "1") {
            unset($header['ENT_ADDR2']);
            unset($header['GRD_ADDR2']);
            $header["ENT_ADDR"] = "入学住所";
            $header["GRD_ADDR"] = "除籍(卒業)住所";
        }

        if ($this->Properties["Origin_hyouji"] != "1") {
            unset($header['ORIGIN']);
        }

        if ($this->Properties["useSpecial_Support_School"] == "1") {
            $header["HANDICAP"] = "教育区分";
        }

        $contents  = "";
        if ($this->field["HEADER"]=="on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjx020Query::getMainSQL($this);
        $result = $db->query($query);
        $header_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r", "↓", $row);
            $row = str_replace("\n", "↓", $row);
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);


        if ($header_data) {
            $couse = "学籍基礎マスタ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 項目チェック
     */
    public function errCheck($db, $data, $checkData, $record_no, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $numericErrFlg = false;    //数字エラーフラグ
        $valuesErrFlg  = false;
        $orderErrFlg   = false;
        $dateErrFlg    = false;
        $masterErrFlg  = false;
        $duplicateCsvErrFlg = false; //重複エラーフラグ

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $numericErrMsg = "";       //数字エラーメッセージ
        $valuesErrMsg  = "";
        $orderErrMsg   = "";
        $dateErrMsg    = "";
        $masterErrMsg  = "";
        $duplicateCsvErrMsg = "";  //重複エラーメッセージ

        $hissuSep   = "";
        $lengthSep  = "";
        $numericSep = "";
        $valuesSep  = "";
        $orderSep   = "";
        $dateSep    = "";
        $masterSep  = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }
        if ($this->field["SHORI_MEI"] == "1") {
            if ($this->Properties["chkNameInputHankaku"] == "1") {
                //氏名入力半角チェック
                if (!$this->chkHankaku($data["NAME"])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "氏名");
                } elseif (!$this->chkHankaku($data["REAL_NAME"])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "戸籍氏名");
                } elseif (!$this->chkHankaku($data["EMERGENCYNAME"])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "緊急連絡先1氏名");
                } elseif (!$this->chkHankaku($data["EMERGENCYNAME2"])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "緊急連絡先2氏名");
                }
            }
            foreach ($checkData as $key => $val) {
                //桁数チェック NO001
                if (!in_array($key, array("NAME", "REAL_NAME", "ENT_ADDR2", "GRD_ADDR2", "EMERGENCYNAME", "EMERGENCYNAME2"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                if (in_array($key, array("NAME"))) {
                    if (
                        ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($data[$key], 'UTF-8') > $val[0]) ||
                        ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($data[$key]) > $val[0])
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                if (in_array($key, array("REAL_NAME"))) {
                    if (
                        ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($data[$key], 'UTF-8') > $val[0]) ||
                        ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($data[$key]) > $val[0])
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                if (in_array($key, array("ENT_ADDR2"))) {
                    if ($this->Properties["useAddrField2"] == "1") {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                }
                if (in_array($key, array("GRD_ADDR2"))) {
                    if ($this->Properties["useAddrField2"] == "1") {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                }
                if (in_array($key, array("EMERGENCYNAME"))) {
                    if (
                        ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($data["EMERGENCYNAME"], 'UTF-8') > $val[0]) ||
                        ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($data["EMERGENCYNAME"]) > $val[0])
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                if (in_array($key, array("EMERGENCYNAME2"))) {
                    if (
                        ($this->Properties["NAME_INPUT_SIZE"] != "" && mb_strlen($data["EMERGENCYNAME2"], 'UTF-8') > $val[0]) ||
                        ($this->Properties["NAME_INPUT_SIZE"] == "" && strlen($data["EMERGENCYNAME2"]) > $val[0])
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
                //必須チェック
                if (in_array($key, array("SCHREGNO", "NAME", "SCHOOL_KIND"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //数字チェック
                if (in_array($key, array("INOUTCD", "SEX", "HANDICAP", "NATIONALITY", "FINSCHOOLCD", "PRISCHOOLCD", "PRISCHOOL_CLASS_CD", "ENT_DIV", "GRD_DIV"))) {
                    if (strlen($data[$key]) && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                //校種取得
                $schKindArr = array();
                $result = $db->query(knjx020Query::getSchoolKind($this));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $schKindArr[] = $row["VALUE"];
                }
                //値チェック
                if (in_array($key, array("SEX"))) {
                    if (!$this->ataiCheck($data[$key], array("1","2"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
                if (in_array($key, array("SCHOOL_KIND"))) {
                    if (!$this->ataiCheck($data[$key], $schKindArr)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
                if (in_array($key, array("BLOODTYPE"))) {
                    if (!$this->ataiCheck($data[$key], array("A","B","O","AB"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
                if (in_array($key, array("BLOOD_RH"))) {
                    if (!$this->ataiCheck($data[$key], array("-","+"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
            }
            //存在チェック(項目チェックがＯＫの場合)
            if ($returnFlg) {
                //日付変換
                if ($data["BIRTHDAY"] != "" && !$this->dateRep($data["BIRTHDAY"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "生年月日");
                }
                if ($data["FINISH_DATE"] != "" && !$this->dateRep($data["FINISH_DATE"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "出身校卒業年月日");
                }
                if ($data["ENT_DATE"] != "" && !$this->dateRep($data["ENT_DATE"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "入学日付");
                }
                if ($data["GRD_DATE"] != "" && !$this->dateRep($data["GRD_DATE"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "除籍(卒業)日付");
                }
                if ($data["TENGAKU_SAKI_ZENJITU"] != "" && !$this->dateRep($data["TENGAKU_SAKI_ZENJITU"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "転学先入学前日(小/中学校専用)");
                }
                //その他
                $table = "name_mst";
                $where = "where NAMECD1='A025' AND NAMECD2 = '".$data["HANDICAP"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["HANDICAP"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkData["HANDICAP"][1]);
                }
                //国籍
                $table = "name_mst";
                $where = "where NAMECD1='A024' AND NAMECD2 = '".$data["NATIONALITY"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["NATIONALITY"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "国籍");
                }
                //出身校マスタ
                $table = "finschool_mst";
                $where = "where FINSCHOOLCD = '".$data["FINSCHOOLCD"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["FINSCHOOLCD"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "出身校コード");
                }
                //塾マスタ
                $table = "prischool_mst";
                $where = "where PRISCHOOLCD = '".$data["PRISCHOOLCD"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["PRISCHOOLCD"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "塾コード");
                }
                //塾(教室)マスタ
                $table = "PRISCHOOL_CLASS_MST";
                $where = "where PRISCHOOLCD = '".$data["PRISCHOOLCD"]."' AND PRISCHOOL_CLASS_CD = '".$data["PRISCHOOL_CLASS_CD"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["PRISCHOOL_CLASS_CD"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "教室コード");
                }
                //名称マスタ(入学区分)---2005.05.09
                $table = "name_mst";
                $where = "where NAMECD1='A002' AND NAMECD2 = '".$data["ENT_DIV"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["ENT_DIV"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "入学区分");
                }
                //名称マスタ(卒業区分)---2005.05.09
                $table = "name_mst";
                $where = "where NAMECD1='A003' AND NAMECD2 = '".$data["GRD_DIV"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["GRD_DIV"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "除籍(卒業)区分");
                }
                //名称マスタ(出身コード)
                if ($this->Properties["Origin_hyouji"] == "1") {
                    $table = "name_mst";
                    $where = "where NAMECD1='A053' AND NAMECD2 = '".$data["ORIGIN"]."'";
                    $sonzaiFlg = $this->sonzaiCheck($db, $data["ORIGIN"], $table, $where);
                    if (!$sonzaiFlg) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "出身コード");
                    }
                }
            }//$ifの綴り
        } else {
            //桁数チェック
            if (strlen($data["SCHREGNO"]) > 8) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "学籍番号");
            }
            //必須チェック
            if ($data["SCHREGNO"] == "") {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "学籍番号");
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$orderErrMsg.$numericErrMsg.$valuesErrMsg.$lengthErrMsg.$dateErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx020Query::insertQueryErr($db, $record_no, $checkError);
        }
        return $returnFlg;
    }

    //値チェック
    public function ataiCheck($data, $atai)
    {
        $flg = true;

        if ($data != "") {
            if (!in_array($data, $atai)) {
                $flg = false;
            }
        }

        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data, $ketasuu)
    {
        $shosiki = "%0".$ketasuu."d";
        if ($data != "") {
            if (strlen($data) < $ketasuu) {
                $data = sprintf($shosiki, $data);
            }
        }

        return $data;
    }

    /**
     * マスタの存在チェック
     */
    public function sonzaiCheck(&$db, $data, $table, $where)
    {
        $sonzaiFlg = true;
        if ($data != "") {
            $sonzaiFlg = false;
            $query = knjx020Query::getMasterSQL($table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzaiFlg = true;
            }
        }
        return $sonzaiFlg;
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }
    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
