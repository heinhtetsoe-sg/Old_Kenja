<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh436Model extends Model {
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
    function knjh436Model()
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


        //駿台甲府学園か確認
        //DB接続
        $db = Query::dbCheckOut();
        $this->CHECK_SCHOOL = $db->getOne(knjh436Query::getCheck_School());
        //DB切断
        Query::dbCheckIn($db);


        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "COMPANY"          =>  VARS::post("COMPANY"),          //模試業者
                             "CSV_DIV"          =>  VARS::post("CSV_DIV"),          //データ区分
                             "MOCKCD"           =>  VARS::post("MOCKCD"),           //模試
                             "GRADE"            =>  VARS::post("GRADE"),            //学年一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "changeCompany") {
            $this->field["CSV_DIV"] = "";
        }
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
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
        if (empty($this->data_arr)) {
            $this->setMessage("志望校数または科目数の設定が間違っているため取り込めませんでした。");
        } else {
            //データの整合性チェック
            if ($this->IsDataMatched($this->data_arr)) {
                $errFlg = $this->checkCsvErrQuery(PROGRAMID);
                if ($errFlg) {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjh436Query::insertQueryCsvHead($this, $this->csvHeadData);
                        knjh436Query::insertQueryCsv($this, $this->data_arr);
                    } else {
                        knjh436Query::DeleteQueryCsv($this, $this->data_arr);
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
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

            $checkdata["YEAR"]      = array(4, "年度");
            if ($this->field["COMPANY"] == "00000001") {
                $checkdata["MOSI_CD"]   = array(4, "模試コード");
                $checkdata["MOSI_NAME"] = array(45, "模試名");
                $checkdata["EXAMNO"]    = array(6, "受験番号");
            } else if ($this->field["COMPANY"] == "00000002") {
                $checkdata["KYOUZAICD"] = array(4, "教材CD");
                $checkdata["BENEID"]    = array(10, "受験番号");
            } else if ($this->field["COMPANY"] == "00000003") {
                $checkdata["MOSI_CD"]   = array(4, "模試コード");
                $checkdata["MOSI_NAME"] = array(45, "模試名");
                $checkdata["HR_CLASS"]    = array(6, "組");
                $checkdata["ATTENDNO"]    = array(6, "番号");
            } else if ($this->field["COMPANY"] == "00000004") {
                $checkdata["MOSI_CD"]   = array(4, "模試コード");
                $checkdata["MOSI_NAME"] = array(45, "模試名");
                $checkdata["HR_CLASS"]    = array(6, "組");
                $checkdata["ATTENDNO"]    = array(6, "番号");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            //希望フィールド数
            $query = knjh436Query::getHopeField($this);
            $this->hopeCnt = $db->getOne($query);

            //得点フィールド数
            $query = knjh436Query::getScoreField($this);
            $this->scoreCnt = $db->getOne($query);

            //読み替え科目
            $query = knjh436Query::getSubclassCd($this);
            $result = $db->query($query);
            $this->replaceSubclass = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->replaceSubclass[$row["FIELD_CNT"]] = $row["MOCK_SUBCLASS_CD"];
            }
            $result->free();

            //2017/08/23追加　模試の種類を取得する
            $this->csv_type = "";
            $this->csv_type = $db->getOne(knjh436Query::getMock_File_Name($this->field["MOCKCD"]));

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            if ($this->field["HEADER"] == "on") {
                $header = false;
            } else {
                $header = true;
            }
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $primaryKey4 = array();
            $primaryKey5 = array();
            $primaryKey6 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();
            $duplicateCsv5 = array();
            $duplicateCsv6 = array();
            if ($this->field["SHORI_MEI"] == "1") {
                if ($this->field["COMPANY"] == "00000001") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_HEAD_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_HOPE_HEAD_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_SCORE_HEAD_DAT');
                    $primaryKey4 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_HDAT');
                    $primaryKey5 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_HOPE_DAT');
                    $primaryKey6 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_SCORE_DAT');
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_BENE_SCORE_HEAD_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_BENE_SCORE_HEAD_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_BENE_SCORE_HDAT');
                    $primaryKey4 = $this->getPrimaryKey('MOCK_CSV_BENE_SCORE_DAT');
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_BENE_HOPE_HEAD_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_BENE_HOPE_HEAD_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_BENE_HOPE_HDAT');
                    $primaryKey4 = $this->getPrimaryKey('MOCK_CSV_BENE_HOPE_DAT');
                } elseif ($this->field["COMPANY"] == "00000003") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_ZKAI_HEAD_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_ZKAI_SCORE_HEAD_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_ZKAI_HOPE_HEAD_DAT');
                    $primaryKey4 = $this->getPrimaryKey('MOCK_CSV_ZKAI_HDAT');
                    $primaryKey5 = $this->getPrimaryKey('MOCK_CSV_ZKAI_SCORE_DAT');
                    $primaryKey6 = $this->getPrimaryKey('MOCK_CSV_ZKAI_HOPE_DAT');
                } elseif ($this->field["COMPANY"] == "00000004") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_KAWAI_HEAD_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_KAWAI_SCORE_HEAD_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_KAWAI_HOPE_HEAD_DAT');
                    $primaryKey4 = $this->getPrimaryKey('MOCK_CSV_KAWAI_HDAT');
                    $primaryKey5 = $this->getPrimaryKey('MOCK_CSV_KAWAI_SCORE_DAT');
                    $primaryKey6 = $this->getPrimaryKey('MOCK_CSV_KAWAI_HOPE_DAT');
                }
            } else {
                if ($this->field["COMPANY"] == "00000001") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_HOPE_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_SUNDAI_SCORE_DAT');
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_BENE_SCORE_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_BENE_SCORE_DAT');
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_BENE_HOPE_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_BENE_HOPE_DAT');
                } elseif ($this->field["COMPANY"] == "00000003") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_ZKAI_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_ZKAI_SCORE_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_ZKAI_HOPE_DAT');
                } elseif ($this->field["COMPANY"] == "00000004") {
                    $primaryKey1 = $this->getPrimaryKey('MOCK_CSV_KAWAI_HDAT');
                    $primaryKey2 = $this->getPrimaryKey('MOCK_CSV_KAWAI_SCORE_DAT');
                    $primaryKey3 = $this->getPrimaryKey('MOCK_CSV_KAWAI_HOPE_DAT');
                }
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 8192);
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $buffer = str_replace("\"", "", $buffer);
                    $tmp = explode(",", $buffer);
                    if (!$header) {
                        $header = true;
                        $line++;
                        $this->csvHeadData = $this->getCsvHeadData($tmp);
                        if (!empty($this->csvHeadData)) {
                            continue;
                        } else {
                            break;
                        }
                    }

                    $tmpCnt = 0;
                    $data = array();
                    if ($this->field["COMPANY"] == "00000001") {
                        $data["YEAR"]           = $tmp[$tmpCnt++];
                        $data["MOSI_CD"]        = $tmp[$tmpCnt++];
                        $data["MOCKCD"]         = $this->field["MOCKCD"];
                        $data["MOSI_NAME"]      = $tmp[$tmpCnt++];
                        $data["KANA"]           = $tmp[$tmpCnt++];
                        $data["HR_CLASS"]       = $tmp[$tmpCnt++];
                        $data["ATTENDNO"]       = $tmp[$tmpCnt++];
                        $data["EXAMNO"]         = $tmp[$tmpCnt++];
                        $data["SCHOOL_CD"]      = $tmp[$tmpCnt++];
                        $data["SCHOOL_EDA"]     = $tmp[$tmpCnt++];
                        $data["SCHOOL_NAME"]    = $tmp[$tmpCnt++];
                        $data["BUNRI_DIV"]      = $tmp[$tmpCnt++];
                        $data["SEX"]            = $tmp[$tmpCnt++];
                        $data["GRADE"]          = $tmp[$tmpCnt++];
                        $data["GEN_SOTU"]       = $tmp[$tmpCnt++];
                        $data["KAMOKU_EIGO"]    = $tmp[$tmpCnt++];
                        $data["KAMOKU_SUUGAKU"] = $tmp[$tmpCnt++];
                        $data["KAMOKU_KOKUGO"]  = $tmp[$tmpCnt++];
                        $data["KAMOKU_RI1"]     = $tmp[$tmpCnt++];
                        $data["KAMOKU_RI2"]     = $tmp[$tmpCnt++];
                        if($this->csv_type == "DI3" || $this->csv_type == "DM1" || $this->csv_type == "DM4" || $this->csv_type == "CC1"){
                            $data["KAMOKU_RI3"]            = $tmp[$tmpCnt++];
                        }
                        $data["KAMOKU_REKIKOU1"]= $tmp[$tmpCnt++];
                        $data["KAMOKU_REKIKOU2"]= $tmp[$tmpCnt++];
                        if($this->csv_type == "DI3" || $this->csv_type == "DM1" || $this->csv_type == "DM4"){
                            $data["RIKA_FIRST"]            = $tmp[$tmpCnt++];
                            $data["REKIKOU_FIRST"]         = $tmp[$tmpCnt++];
                        }
                        for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                            $data["SEQ{$hCnt}"]            = $hCnt;
                            $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                            $data["NITTEI{$hCnt}"]         = $tmp[$tmpCnt++];
                            $data["RANK{$hCnt}"]           = $tmp[$tmpCnt++];
                            $data["CNT{$hCnt}"]            = $tmp[$tmpCnt++];
                            $data["JUDGE_HYOUKA{$hCnt}"]   = $tmp[$tmpCnt++];
                            $data["JUDGE_SUUTI{$hCnt}"]    = $tmp[$tmpCnt++];
                        }
                        for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                            $data["SEQ{$sCnt}"]                = $sCnt;
                            $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                            $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                            $data["DEVIATION{$sCnt}"]          = $tmp[$tmpCnt++];
                            $data["KRANK{$sCnt}"]              = $tmp[$tmpCnt++];
                        }
                        $data["DEVIATION"]      = $tmp[$tmpCnt++];
                        $data["RANK"]           = $tmp[$tmpCnt++];
                        $data["CNT"]            = $tmp[$tmpCnt++];
                        $this->csvHeadData["KYEAR"] = $data["YEAR"];
                        $this->csvHeadData["KMOSI_CD"] = $data["MOSI_CD"];
                    } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
                        $data["YEAR"]       = $tmp[$tmpCnt++];
                        $data["KYOUZAICD"]  = $this->field["GRADE"].$tmp[$tmpCnt++];
                        $data["MOCKCD"]     = $this->field["MOCKCD"];
                        $data["TYPE"]       = $tmp[$tmpCnt++];
                        $data["KYOUZAINAME"]= $tmp[$tmpCnt++];
                        $data["GAKKACD"]    = $tmp[$tmpCnt++];
                        $data["GAKKANAME"]  = $tmp[$tmpCnt++];
                        $data["BENEID"]     = $tmp[$tmpCnt++];
                        $data["HR_CLASS"]   = $tmp[$tmpCnt++];
                        $data["ATTENDNO"]   = $tmp[$tmpCnt++];
                        $data["NAME"]       = $tmp[$tmpCnt++];
                        $data["BUNRI_DIV"]  = $tmp[$tmpCnt++];
                        $data["BIRTHDAY"]   = $tmp[$tmpCnt++];
                        $data["SEX"]        = $tmp[$tmpCnt++];
                        for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                            $data["SEQ{$sCnt}"]                = $sCnt;
                            $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                            $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                            $data["GTZ{$sCnt}"]                = $tmp[$tmpCnt++];
                            $data["ALL_DEV{$sCnt}"]            = $tmp[$tmpCnt++];
                            $data["SCHOOL_DEV{$sCnt}"]         = $tmp[$tmpCnt++];
                            $data["ALL_RANK{$sCnt}"]           = $tmp[$tmpCnt++];
                            $data["SCHOOL_RANK{$sCnt}"]        = $tmp[$tmpCnt++];
                        }
                        $data["DEVIATION"]  = $tmp[$tmpCnt++];
                        $data["RANK"]       = $tmp[$tmpCnt++];
                        $data["CNT"]        = $tmp[$tmpCnt++];
                        $this->csvHeadData["KYEAR"] = $data["YEAR"];
                        $this->csvHeadData["KKYOUZAICD"] = $data["KYOUZAICD"];
                    } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
                        $data["YEAR"]       = $tmp[$tmpCnt++];
                        $data["KYOUZAICD"]  = $this->field["GRADE"].$tmp[$tmpCnt++];
                        $data["MOCKCD"]     = $this->field["MOCKCD"];
                        $data["TYPE"]       = $tmp[$tmpCnt++];
                        $data["KYOUZAINAME"]= $tmp[$tmpCnt++];
                        $data["GAKKACD"]    = $tmp[$tmpCnt++];
                        $data["GAKKANAME"]  = $tmp[$tmpCnt++];
                        $data["BENEID"]     = $tmp[$tmpCnt++];
                        $data["HR_CLASS"]   = $tmp[$tmpCnt++];
                        $data["ATTENDNO"]   = $tmp[$tmpCnt++];
                        $data["NAME"]       = $tmp[$tmpCnt++];
                        $data["BUNRI_DIV"]  = $tmp[$tmpCnt++];
                        $data["BIRTHDAY"]   = $tmp[$tmpCnt++];
                        $data["SEX"]        = $tmp[$tmpCnt++];
                        for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                            $data["SEQ{$hCnt}"]            = $hCnt;
                            $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["BOSYUTANNI{$hCnt}"]     = $tmp[$tmpCnt++];
                            $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                            $data["GAKUBU_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                            $data["GAKKA_NAME{$hCnt}"]     = $tmp[$tmpCnt++];
                            $data["NITTEI{$hCnt}"]         = $tmp[$tmpCnt++];
                            $data["HOUSIKI{$hCnt}"]        = $tmp[$tmpCnt++];
                            $data["ALL_JUDGE{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["JUDGE1{$hCnt}"]         = $tmp[$tmpCnt++];
                            $data["JUDGE2{$hCnt}"]         = $tmp[$tmpCnt++];
                            $data["ALL_JUDGE_SS{$hCnt}"]   = $tmp[$tmpCnt++];
                            $data["JUDGE1_SS{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["JUDGE2_SS{$hCnt}"]      = $tmp[$tmpCnt++];
                        }
                        $this->csvHeadData["KYEAR"] = $data["YEAR"];
                        $this->csvHeadData["KKYOUZAICD"] = $data["KYOUZAICD"];
                    } else if ($this->field["COMPANY"] == "00000003") {
                        $data["YEAR"]           = $tmp[$tmpCnt++];
                        $data["MOSI_CD"]        = $tmp[$tmpCnt++];
                        $data["MOCKCD"]         = $this->field["MOCKCD"];
                        $data["MOSI_NAME"]      = $tmp[$tmpCnt++];
                        $data["HR_CLASS"]       = $tmp[$tmpCnt++];
                        $data["ATTENDNO"]       = $tmp[$tmpCnt++];
                        $data["KANA"]           = $tmp[$tmpCnt++];
                        for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                            $data["SEQ{$sCnt}"]                = $sCnt;
                            $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                            $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                            $data["DEVIATION{$sCnt}"]          = $tmp[$tmpCnt++];
                        }
                        for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                            $data["SEQ{$hCnt}"]            = $hCnt;
                            $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                            $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["JUDGE_HYOUKA{$hCnt}"]   = $tmp[$tmpCnt++];
                            $data["RANK{$hCnt}"]           = $tmp[$tmpCnt++];
                            $data["JUDGE_SUUTI{$hCnt}"]    = $tmp[$tmpCnt++];
                        }
                        $data["DEVIATION"]  = $tmp[$tmpCnt++];
                        $data["RANK"]       = $tmp[$tmpCnt++];
                        $data["CNT"]        = $tmp[$tmpCnt++];
                        $this->csvHeadData["KYEAR"] = $data["YEAR"];
                        $this->csvHeadData["KMOSI_CD"] = $data["MOSI_CD"];
                    } else if ($this->field["COMPANY"] == "00000004") {
                        $data["YOUSHI_NO"]      = $tmp[$tmpCnt++];
                        $data["YEAR"]           = $tmp[$tmpCnt++];
                        $data["MOSI_CD"]        = $tmp[$tmpCnt++];
                        $data["MOCKCD"]         = $this->field["MOCKCD"];
                        $data["SCHOOL_CD"]      = $tmp[$tmpCnt++];
                        $data["GRADE"]          = $tmp[$tmpCnt++];
                        $data["HR_CLASS"]       = $tmp[$tmpCnt++];
                        $data["ATTENDNO"]       = $tmp[$tmpCnt++];
                        $data["KANA"]           = $tmp[$tmpCnt++];
                        $data["EXAM_TYPE"]      = $tmp[$tmpCnt++];
                        $data["BUN_RI_CD"]      = $tmp[$tmpCnt++];
                        for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                            $data["SEQ{$sCnt}"]               = $sCnt;
                            $data["MOCK_SUBCLASS_CD{$sCnt}"]  = $this->replaceSubclass[$sCnt];
                            $data["KAMOKU_CD{$sCnt}"]         = $tmp[$tmpCnt++];
                            $data["KAMOKU_NAME{$sCnt}"]       = $tmp[$tmpCnt++];
                            $data["HAITEN{$sCnt}"]            = $tmp[$tmpCnt++];
                            $data["SCORE{$sCnt}"]             = $tmp[$tmpCnt++];
                            $data["DEVIATION{$sCnt}"]         = $tmp[$tmpCnt++];
                            $data["LEVEL{$sCnt}"]             = $tmp[$tmpCnt++];
                        }
                        $data["REMARK_A"]  = $tmp[$tmpCnt++];
                        $data["REMARK_B"]  = $tmp[$tmpCnt++];
                        $data["REMARK_C"]  = $tmp[$tmpCnt++];
                        $data["REMARK_D"]  = $tmp[$tmpCnt++];
                        $data["REMARK_E"]  = $tmp[$tmpCnt++];
                        $data["REMARK_F"]  = $tmp[$tmpCnt++];
                        $data["REMARK_G"]  = $tmp[$tmpCnt++];
                        $data["REMARK_H"]  = $tmp[$tmpCnt++];
                        $data["REMARK_I"]  = $tmp[$tmpCnt++];
                        for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                            $data["SEQ{$hCnt}"]            = $hCnt;
                            $data["SCHOOL_CD_5{$hCnt}"]    = $tmp[$tmpCnt++];
                            $data["SCHOOL_CD_10{$hCnt}"]   = $tmp[$tmpCnt++];
                            $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                            $data["HYOUKA_SEISEKI{$hCnt}"] = $tmp[$tmpCnt++];
                            $data["SE_HYOUKA{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["NI_HYOUKA{$hCnt}"]      = $tmp[$tmpCnt++];
                            $data["SOUHYOU_PO{$hCnt}"]     = $tmp[$tmpCnt++];
                            $data["SOU_HYOUKA{$hCnt}"]     = $tmp[$tmpCnt++];
                        }
                        $this->csvHeadData["KYEAR"] = $data["YEAR"];
                        $this->csvHeadData["KMOSI_CD"] = $data["MOSI_CD"];
                    }

                    //学籍番号
                    $query = knjh436Query::getSchregNo($this, $data["HR_CLASS"], $data["ATTENDNO"]);
                    $setSchregNo = $db->getOne($query);
                    $data["SCHREGNO"] = $setSchregNo;

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey4 as $key => $val) {
                        $duplicateCsv4[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey5 as $key => $val) {
                        $duplicateCsv5[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey6 as $key => $val) {
                        $duplicateCsv6[$line][$val] = $data[$val];
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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $existenceSep    = "";
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
        $dupFlg4 = $this->duplicateCsvCheck($duplicateCsv4, $primaryKey4, $data);
        if ($dupFlg4[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg4[1] ."行目)");
        }
        $dupFlg5 = $this->duplicateCsvCheck($duplicateCsv5, $primaryKey5, $data);
        if ($dupFlg5[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複5(". $dupFlg5[1] ."行目)");
        }
        $dupFlg6 = $this->duplicateCsvCheck($duplicateCsv6, $primaryKey6, $data);
        if ($dupFlg6[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複6(". $dupFlg6[1] ."行目)");
        }
        $sonzaiarray = array("RELATIONSHIP"             => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "KYOUZAICD", "BENEID", "MOSI_CD", "EXAMNO", "HR_CLASS", "ATTENDNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("MOCKCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            } else {
            }
        }
        if ($data["SCHREGNO"] == "") {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh436Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /**
     * ヘッダ取得
     */
    function getCsvHeadData($tmp)
    {
        $tmpCnt = 0;
        $data = array();
        if ($this->field["COMPANY"] == "00000001") {
            $data["YEAR"]           = $tmp[$tmpCnt++];
            $data["MOSI_CD"]        = $tmp[$tmpCnt++];
            $data["MOCKCD"]         = $this->field["MOCKCD"];
            $data["MOSI_NAME"]      = $tmp[$tmpCnt++];
            $data["KANA"]           = $tmp[$tmpCnt++];
            $data["HR_CLASS"]       = $tmp[$tmpCnt++];
            $data["ATTENDNO"]       = $tmp[$tmpCnt++];
            $data["EXAMNO"]         = $tmp[$tmpCnt++];
            $data["SCHOOL_CD"]      = $tmp[$tmpCnt++];
            $data["SCHOOL_EDA"]     = $tmp[$tmpCnt++];
            $data["SCHOOL_NAME"]    = $tmp[$tmpCnt++];
            $data["BUNRI_DIV"]      = $tmp[$tmpCnt++];
            $data["SEX"]            = $tmp[$tmpCnt++];
            $data["GRADE"]          = $tmp[$tmpCnt++];
            $data["GEN_SOTU"]       = $tmp[$tmpCnt++];
            $data["KAMOKU_EIGO"]    = $tmp[$tmpCnt++];
            $data["KAMOKU_SUUGAKU"] = $tmp[$tmpCnt++];
            $data["KAMOKU_KOKUGO"]  = $tmp[$tmpCnt++];
            $data["KAMOKU_RI1"]     = $tmp[$tmpCnt++];
            $data["KAMOKU_RI2"]     = $tmp[$tmpCnt++];
            if($this->csv_type == "DI3" || $this->csv_type == "DM1" || $this->csv_type == "DM4" || $this->csv_type == "CC1"){
                $data["KAMOKU_RI3"]            = $tmp[$tmpCnt++];
            }
            $data["KAMOKU_REKIKOU1"]= $tmp[$tmpCnt++];
            $data["KAMOKU_REKIKOU2"]= $tmp[$tmpCnt++];
            if($this->csv_type == "DI3" || $this->csv_type == "DM1" || $this->csv_type == "DM4"){
                $data["RIKA_FIRST"]            = $tmp[$tmpCnt++];
                $data["REKIKOU_FIRST"]         = $tmp[$tmpCnt++];
            }
            for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                $data["SEQ{$hCnt}"]            = $hCnt;
                $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["NITTEI{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["TRANK{$hCnt}"]          = $tmp[$tmpCnt++];
                $data["CNT{$hCnt}"]            = $tmp[$tmpCnt++];
                $data["JUDGE_HYOUKA{$hCnt}"]   = $tmp[$tmpCnt++];
                $data["JUDGE_SUUTI{$hCnt}"]    = $tmp[$tmpCnt++];
            }
            //志望校数の設定とデータの数が違ってないかチェックしたい
            $dataCnt = preg_replace('/[^0-9]/', '', mb_convert_kana($data["SCHOOL_CD{$this->hopeCnt}"], "n"));
            if($dataCnt == "" || $dataCnt != $this->hopeCnt){
                $data = array();
                return $data;
            }else{
                $tmpNow = $tmpCnt;
                $nextData = $tmp[$tmpNow++];    //次に来るデータが志望データじゃないかチェック　"志"が入っていてかつ数字も含まれるとき
                if(preg_match('/志/', $nextData) && preg_replace('/[^0-9]/', '', mb_convert_kana($nextData, "n")) != ""){
                    $data = array();
                    return $data;
                }
            }
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]                = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                $data["DEVIATION{$sCnt}"]          = $tmp[$tmpCnt++];
                $data["RANK{$sCnt}"]               = $tmp[$tmpCnt++];
            }
            //科目数チェック
            $dataString = $data["SCORE{$this->scoreCnt}"];
            if($dataString != ""){
                if($tmp[$tmpCnt++] != ""){  //設定した科目数が少ないとき
                    $data = array();
                    return $data;
                }
            }else{  //設定した科目数が多いとき
                $data = array();
                return $data;
            }
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
            $data["YEAR"]       = $tmp[$tmpCnt++];
            $data["KYOUZAICD"]  = $this->field["GRADE"].$tmp[$tmpCnt++];
            $data["MOCKCD"]     = $this->field["MOCKCD"];
            $data["TYPE"]       = $tmp[$tmpCnt++];
            $data["KYOUZAINAME"]= $tmp[$tmpCnt++];
            $data["GAKKACD"]    = $tmp[$tmpCnt++];
            $data["GAKKANAME"]  = $tmp[$tmpCnt++];
            $data["BENEID"]     = $tmp[$tmpCnt++];
            $data["HR_CLASS"]   = $tmp[$tmpCnt++];
            $data["ATTENDNO"]   = $tmp[$tmpCnt++];
            $data["NAME"]       = $tmp[$tmpCnt++];
            $data["BUNRI_DIV"]  = $tmp[$tmpCnt++];
            $data["BIRTHDAY"]   = $tmp[$tmpCnt++];
            $data["SEX"]        = $tmp[$tmpCnt++];
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]                = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                $data["GTZ{$sCnt}"]                = $tmp[$tmpCnt++];
                $data["ALL_DEV{$sCnt}"]            = $tmp[$tmpCnt++];
                $data["SCHOOL_DEV{$sCnt}"]         = $tmp[$tmpCnt++];
                $data["ALL_RANK{$sCnt}"]           = $tmp[$tmpCnt++];
                $data["SCHOOL_RANK{$sCnt}"]        = $tmp[$tmpCnt++];
            }
            //科目数チェック
            $dataString = $data["SCORE{$this->scoreCnt}"];
            if($dataString != ""){
                if($tmp[$tmpCnt++] != ""){  //設定した科目数が少ないとき
                    $data = array();
                    return $data;
                }
            }else{  //設定した科目数が多いとき
                $data = array();
                return $data;
            }
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
            $data["YEAR"]       = $tmp[$tmpCnt++];
            $data["KYOUZAICD"]  = $this->field["GRADE"].$tmp[$tmpCnt++];
            $data["MOCKCD"]     = $this->field["MOCKCD"];
            $data["TYPE"]       = $tmp[$tmpCnt++];
            $data["KYOUZAINAME"]= $tmp[$tmpCnt++];
            $data["GAKKACD"]    = $tmp[$tmpCnt++];
            $data["GAKKANAME"]  = $tmp[$tmpCnt++];
            $data["BENEID"]     = $tmp[$tmpCnt++];
            $data["HR_CLASS"]   = $tmp[$tmpCnt++];
            $data["ATTENDNO"]   = $tmp[$tmpCnt++];
            $data["NAME"]       = $tmp[$tmpCnt++];
            $data["BUNRI_DIV"]  = $tmp[$tmpCnt++];
            $data["BIRTHDAY"]   = $tmp[$tmpCnt++];
            $data["SEX"]        = $tmp[$tmpCnt++];
            for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                $data["SEQ{$hCnt}"]            = $hCnt;
                $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["BOSYUTANNI{$hCnt}"]     = $tmp[$tmpCnt++];
                $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["GAKUBU_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["GAKKA_NAME{$hCnt}"]     = $tmp[$tmpCnt++];
                $data["NITTEI{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["HOUSIKI{$hCnt}"]        = $tmp[$tmpCnt++];
                $data["ALL_JUDGE{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["JUDGE1{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["JUDGE2{$hCnt}"]         = $tmp[$tmpCnt++];
                $data["ALL_JUDGE_SS{$hCnt}"]   = $tmp[$tmpCnt++];
                $data["JUDGE1_SS{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["JUDGE2_SS{$hCnt}"]      = $tmp[$tmpCnt++];
            }
            //志望校数の設定とデータの数が違ってないかチェックしたい
            $dataCnt = preg_replace('/[^0-9]/', '', mb_convert_kana($data["SCHOOL_CD{$this->hopeCnt}"], "n"));
            if($dataCnt == "" || $dataCnt != $this->hopeCnt){   //設定した数が多いとき
                $data = array();
                return $data;
            }else{
                if($tmp[$tmpCnt++] != ""){  //設定数が少ないとき
                    $data = array();
                    return $data;
                }
            }
        } else if ($this->field["COMPANY"] == "00000003") {
            $data["YEAR"]           = $tmp[$tmpCnt++];
            $data["MOSI_CD"]        = $tmp[$tmpCnt++];
            $data["MOCKCD"]         = $this->field["MOCKCD"];
            $data["MOSI_NAME"]      = $tmp[$tmpCnt++];
            $data["HR_CLASS"]       = $tmp[$tmpCnt++];
            $data["ATTENDNO"]       = $tmp[$tmpCnt++];
            $data["KANA"]           = $tmp[$tmpCnt++];
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]                = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"]   = $this->replaceSubclass[$sCnt];
                $data["SCORE{$sCnt}"]              = $tmp[$tmpCnt++];
                $data["DEVIATION{$sCnt}"]          = $tmp[$tmpCnt++];
            }
            for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                $data["SEQ{$hCnt}"]            = $hCnt;
                $data["SCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["SCHOOL_CD{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["JUDGE_HYOUKA{$hCnt}"]   = $tmp[$tmpCnt++];
                $data["TRANK{$hCnt}"]          = $tmp[$tmpCnt++];
                $data["JUDGE_SUUTI{$hCnt}"]    = $tmp[$tmpCnt++];
            }
            $data["DEVIATION"]  = $tmp[$tmpCnt++];
            $data["RANK"]       = $tmp[$tmpCnt++];
            $data["CNT"]        = $tmp[$tmpCnt++];
        } else if ($this->field["COMPANY"] == "00000004") {
            $data["YOUSHI_NO"]    = $tmp[$tmpCnt++];
            $data["YEAR"]         = $tmp[$tmpCnt++];
            $data["MOSI_CD"]      = $tmp[$tmpCnt++];
            $data["SCHOOL_CD"]    = $tmp[$tmpCnt++];
            $data["GRADE"]        = $tmp[$tmpCnt++];
            $data["HR_CLASS"]     = $tmp[$tmpCnt++];
            $data["ATTENDNO"]     = $tmp[$tmpCnt++];
            $data["KANA"]         = $tmp[$tmpCnt++];
            $data["EXAM_TYPE"]    = $tmp[$tmpCnt++];
            $data["BUN_RI_CD"]    = $tmp[$tmpCnt++];
            for ($sCnt = 1; $sCnt <= $this->scoreCnt; $sCnt++) {
                $data["SEQ{$sCnt}"]              = $sCnt;
                $data["MOCK_SUBCLASS_CD{$sCnt}"] = $this->replaceSubclass[$sCnt];
                $data["TKAMOKU_CD{$sCnt}"]       = $tmp[$tmpCnt++];
                $data["TKAMOKU_NAME{$sCnt}"]     = $tmp[$tmpCnt++];
                $data["THAITEN{$sCnt}"]          = $tmp[$tmpCnt++];
                $data["TSCORE{$sCnt}"]           = $tmp[$tmpCnt++];
                $data["TDEVIATION{$sCnt}"]       = $tmp[$tmpCnt++];
                $data["TLEVEL{$sCnt}"]           = $tmp[$tmpCnt++];
            }
            $data["TREMARK_A"]  = $tmp[$tmpCnt++];
            $data["TREMARK_B"]  = $tmp[$tmpCnt++];
            $data["TREMARK_C"]  = $tmp[$tmpCnt++];
            $data["TREMARK_D"]  = $tmp[$tmpCnt++];
            $data["TREMARK_E"]  = $tmp[$tmpCnt++];
            $data["TREMARK_F"]  = $tmp[$tmpCnt++];
            $data["TREMARK_G"]  = $tmp[$tmpCnt++];
            $data["TREMARK_H"]  = $tmp[$tmpCnt++];
            $data["TREMARK_I"]  = $tmp[$tmpCnt++];
            for ($hCnt = 1; $hCnt <= $this->hopeCnt; $hCnt++) {
                $data["SEQ{$hCnt}"]             = $hCnt;
                $data["TSCHOOL_CD_5{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["TSCHOOL_CD_10{$hCnt}"]   = $tmp[$tmpCnt++];
                $data["TSCHOOL_NAME{$hCnt}"]    = $tmp[$tmpCnt++];
                $data["THYOUKA_SEISEKI{$hCnt}"] = $tmp[$tmpCnt++];
                $data["TSE_HYOUKA{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["TNI_HYOUKA{$hCnt}"]      = $tmp[$tmpCnt++];
                $data["TSOUHYOU_PO{$hCnt}"]     = $tmp[$tmpCnt++];
                $data["TSOU_HYOUKA{$hCnt}"]     = $tmp[$tmpCnt++];
            }
        }

        return $data;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //ＣＳＶ出力
    function getDownloadModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = $this->getCsvHead($db);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        //エラー
        if ($this->field["OUTPUT"] == "2") {
            $headerDuplicate = array("YEAR"      => "実施年度",
                                     "MOSI_CD"   => "模試コード",
                                     "SEQ"       => "SEQ",
                                     "EXAMNO"    => "受験番号",
                                     "KYOUZAICD" => "教材CD",
                                     "BENEID"    => "受験番号",
                                     "HR_CLASS"  => "組",
                                     "ATTENDNO"  => "番号",
                                     "GRADE"     => "学年");
            $tables = array();
            if ($this->field["SHORI_MEI"] == "1") {
                if ($this->field["COMPANY"] == "00000001") {
                    $tables = array("1" => "MOCK_CSV_SUNDAI_HEAD_HDAT", "2" => "MOCK_CSV_SUNDAI_HOPE_HEAD_DAT", "3" => "MOCK_CSV_SUNDAI_SCORE_HEAD_DAT", "4" => "MOCK_CSV_SUNDAI_HDAT", "5" => "MOCK_CSV_SUNDAI_HOPE_DAT", "6" => "MOCK_CSV_SUNDAI_SCORE_DAT");
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
                    $tables = array("1" => "MOCK_CSV_BENE_SCORE_HEAD_HDAT", "2" => "MOCK_CSV_BENE_SCORE_HEAD_DAT", "3" => "MOCK_CSV_BENE_SCORE_HDAT", "4" => "MOCK_CSV_BENE_SCORE_DAT");
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
                    $tables = array("1" => "MOCK_CSV_BENE_HOPE_HEAD_HDAT", "2" => "MOCK_CSV_BENE_HOPE_HEAD_DAT", "3" => "MOCK_CSV_BENE_HOPE_HDAT", "4" => "MOCK_CSV_BENE_HOPE_DAT");
                } elseif ($this->field["COMPANY"] == "00000003") {
                    $tables = array("1" => "MOCK_CSV_ZKAI_HEAD_HDAT", "2" => "MOCK_CSV_ZKAI_SCORE_HEAD_DAT", "3" => "MOCK_CSV_ZKAI_HOPE_HEAD_DAT", "4" => "MOCK_CSV_ZKAI_HDAT", "5" => "MOCK_CSV_ZKAI_SCORE_DAT", "6" => "MOCK_CSV_ZKAI_HOPE_DAT");
                } elseif ($this->field["COMPANY"] == "00000004") {
                    $tables = array("1" => "MOCK_CSV_KAWAI_HEAD_HDAT", "2" => "MOCK_CSV_KAWAI_SCORE_HEAD_DAT", "3" => "MOCK_CSV_KAWAI_HOPE_HEAD_DAT", "4" => "MOCK_CSV_KAWAI_HDAT", "5" => "MOCK_CSV_KAWAI_SCORE_DAT", "6" => "MOCK_CSV_KAWAI_HOPE_DAT");
                }
            } else {
                if ($this->field["COMPANY"] == "00000001") {
                    $tables = array("1" => "MOCK_CSV_SUNDAI_HDAT", "2" => "MOCK_CSV_SUNDAI_HOPE_DAT", "3" => "MOCK_CSV_SUNDAI_SCORE_DAT");
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
                    $tables = array("1" => "MOCK_CSV_BENE_SCORE_HDAT", "2" => "MOCK_CSV_BENE_SCORE_DAT");
                } elseif ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
                    $tables = array("1" => "MOCK_CSV_BENE_HOPE_HDAT", "2" => "MOCK_CSV_BENE_HOPE_DAT");
                } elseif ($this->field["COMPANY"] == "00000003") {
                    $tables = array("1" => "MOCK_CSV_ZKAI_HDAT", "2" => "MOCK_CSV_ZKAI_SCORE_DAT", "3" => "MOCK_CSV_ZKAI_HOPE_DAT");
                } elseif ($this->field["COMPANY"] == "00000004") {
                    $tables = array("1" => "MOCK_CSV_KAWAI_HDAT", "2" => "MOCK_CSV_KAWAI_SCORE_DAT", "3" => "MOCK_CSV_KAWAI_HOPE_DAT");
                }
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        //ヘッダ出力
        } else if ($this->field["OUTPUT"] == "3") {
            $contents .= implode($header, ",");
            $header_data = true;
        } else {
            $contents .= implode($header, ",")."\n";
            $query = knjh436Query::selectCsvQuery($this);
        }

        if ($this->field["OUTPUT"] != "3") {
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        $setTitle = $this->field["YEAR"]."年度";
        $query = knjh436Query::getTitleGrade($this);
        $setTitle .= $db->getOne($query);
        $query = knjh436Query::getTitleMockCompany($this);
        $setTitle .= $db->getOne($query);
        $query = knjh436Query::getTitleMockMst($this);
        $setTitle .= $db->getOne($query);
        $query = knjh436Query::getTitleCsvDiv($this);
        $setTitle .= $db->getOne($query);

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "2") {
                $couse = "エラー出力";
            } else {
                $couse = $setTitle;
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    /**
     * ヘッダ取得
     */
    function getCsvHead($db)
    {
        //希望フィールド数
        $query = knjh436Query::getHopeField($this);
        $this->hopeCnt = $db->getOne($query);

        //得点フィールド数
        $query = knjh436Query::getScoreField($this);
        $this->scoreCnt = $db->getOne($query);

        $retHead = array();
        if ($this->field["COMPANY"] == "00000001") {
            $query = knjh436Query::getCsvTitle($this, "MOCK_CSV_SUNDAI_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]           = $this->setString($setRow["TYEAR"],            "実施年度");
            $retHead["MOSI_CD"]        = $this->setString($setRow["TMOSI_CD"],         "模試コード");
            $retHead["MOSI_NAME"]      = $this->setString($setRow["TMOSI_NAME"],       "模試名称");
            $retHead["KANA"]           = $this->setString($setRow["TKANA"],            "カナ氏名");
            $retHead["HR_CLASS"]       = $this->setString($setRow["THR_CLASS"],        "クラス");
            $retHead["ATTENDNO"]       = $this->setString($setRow["TATTENDNO"],        "出席番号");
            $retHead["EXAMNO"]         = $this->setString($setRow["TEXAMNO"],          "受験番号");
            $retHead["SCHOOL_CD"]      = $this->setString($setRow["TSCHOOL_CD"],       "学校コード");
            $retHead["SCHOOL_EDA"]     = $this->setString($setRow["TSCHOOL_EDA"],      "学校枝番");
            $retHead["SCHOOL_NAME"]    = $this->setString($setRow["TSCHOOL_NAME"],     "学校名");
            $retHead["BUNRI_DIV"]      = $this->setString($setRow["TBUNRI_DIV"],       "文理");
            $retHead["SEX"]            = $this->setString($setRow["TSEX"],             "性別");
            $retHead["GRADE"]          = $this->setString($setRow["TGRADE"],           "学年");
            $retHead["GEN_SOTU"]       = $this->setString($setRow["TGEN_SOTU"],        "既卒");
            $retHead["KAMOKU_EIGO"]    = $this->setString($setRow["TKAMOKU_EIGO"],     "英語");
            $retHead["KAMOKU_SUUGAKU"] = $this->setString($setRow["TKAMOKU_SUUGAKU"],  "数学");
            $retHead["KAMOKU_KOKUGO"]  = $this->setString($setRow["TKAMOKU_KOKUGO"],   "国語");
            $retHead["KAMOKU_RI1"]     = $this->setString($setRow["TKAMOKU_RI1"],      "理１");
            $retHead["KAMOKU_RI2"]     = $this->setString($setRow["TKAMOKU_RI2"],      "理２");
            if($this->csv_type == "DI3" || $this->csv_type == "DM1" || $this->csv_type == "DM4" || $this->csv_type == "CC1"){
                $retHead["KAMOKU_RI3"]     = $this->setString($setRow["TKAMOKU_RI2"],      "理３");
            }
            $retHead["KAMOKU_REKIKOU1"] = $this->setString($setRow["TKAMOKU_REKIKOU1"], "社１");
            $retHead["KAMOKU_REKIKOU2"] = $this->setString($setRow["TKAMOKU_REKIKOU2"], "社２");
            if($this->csv_type == "DI3" || $this->csv_type == "DM1" || $this->csv_type == "DM4"){
                $retHead["RIKA_FIRST"]    = $this->setString($setRow["TRIKA_FIRST"],    "理科第１解答科目");
                $retHead["REKIKOU_FIRST"] = $this->setString($setRow["TREKIKOU_FIRST"], "歴公第１解答科目");
            }

            //志望校情報セット
            $cnt = 1;
            $query = knjh436Query::getCsvSundaiTitle($this, "MOCK_CSV_SUNDAI_HOPE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCHOOL_CD{$row["SEQ"]}"]      = $this->setString($row["TSCHOOL_CD"],     "{$cnt}志大学CD");
                $retHead["SCHOOL_NAME{$row["SEQ"]}"]    = $this->setString($row["TSCHOOL_NAME"],   "{$cnt}志大学名称");
                $retHead["NITTEI{$row["SEQ"]}"]         = $this->setString($row["TNITTEI"],        "{$cnt}志日程");
                $retHead["RANK{$row["SEQ"]}"]           = $this->setString($row["TRANK"],          "{$cnt}志総志望席次");
                $retHead["CNT{$row["SEQ"]}"]            = $this->setString($row["TCNT"],           "{$cnt}志総志望人数");
                $retHead["JUDGE_HYOUKA{$row["SEQ"]}"]   = $this->setString($row["TJUDGE_HYOUKA"],  "{$cnt}志判定評価");
                $retHead["JUDGE_SUUTI{$row["SEQ"]}"]    = $this->setString($row["TJUDGE_SUUTI"],   "{$cnt}志判定数値");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_HOPE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCHOOL_CD{$cnt}"]      = "{$cnt}志大学CD";
                    $retHead["SCHOOL_NAME{$cnt}"]    = "{$cnt}志大学名称";
                    $retHead["NITTEI{$cnt}"]         = "{$cnt}志日程";
                    $retHead["RANK{$cnt}"]           = "{$cnt}志総志望席次";
                    $retHead["CNT{$cnt}"]            = "{$cnt}志総志望人数";
                    $retHead["JUDGE_HYOUKA{$cnt}"]   = "{$cnt}志判定評価";
                    $retHead["JUDGE_SUUTI{$cnt}"]    = "{$cnt}志判定数値";
                    $cnt++;
                }
            }

            //得点情報セット
            $cnt = 1;
            $query = knjh436Query::getCsvSundaiTitle($this, "MOCK_CSV_SUNDAI_SCORE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCORE{$row["SEQ"]}"]              = $this->setString($row["TSCORE"],     "科目{$cnt}_得点");
                $retHead["DEVIATION{$row["SEQ"]}"]          = $this->setString($row["TDEVIATION"], "科目{$cnt}_偏差値");
                $retHead["KRANK{$row["SEQ"]}"]              = $this->setString($row["TRANK"],      "科目{$cnt}_席次");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_SCORE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCORE{$cnt}"]              = "科目{$cnt}_得点";
                    $retHead["DEVIATION{$cnt}"]          = "科目{$cnt}_偏差値";
                    $retHead["KRANK{$cnt}"]              = "科目{$cnt}_席次";
                    $cnt++;
                }
            }
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "1") {
            $query = knjh436Query::getCsvTitle($this, "MOCK_CSV_BENE_SCORE_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]       = $this->setString($setRow["TYEAR"],        "年度");
            $retHead["KYOUZAICD"]  = ($setRow["TKYOUZAI"] != "") ? $this->field["GRADE"].$setRow["TKYOUZAI"]: "教材回";
            $retHead["TYPE"]       = $this->setString($setRow["TTYPE"],        "タイプ");
            $retHead["KYOUZAINAME"]= $this->setString($setRow["TKYOUZAINAME"], "教材名称");
            $retHead["GAKKACD"]    = $this->setString($setRow["TGAKKACD"],     "学科コード");
            $retHead["GAKKANAME"]  = $this->setString($setRow["TGAKKANAME"],   "高３学科");
            $retHead["BENEID"]     = $this->setString($setRow["TBENEID"],      "BeneID");
            $retHead["HR_CLASS"]   = $this->setString($setRow["THR_CLASS"],    "高３組");
            $retHead["ATTENDNO"]   = $this->setString($setRow["TATTENDNO"],    "高３番号");
            $retHead["NAME"]       = $this->setString($setRow["TNAME"],        "氏名");
            $retHead["BUNRI_DIV"]  = $this->setString($setRow["TBUNRI_DIV"],   "文理区分");
            $retHead["BIRTHDAY"]   = $this->setString($setRow["TBIRTHDAY"],    "生年月日");
            $retHead["SEX"]        = $this->setString($setRow["TSEX"],         "性別");

            //得点情報セット
            $cnt = 1;
            $query = knjh436Query::getCsvBeneTitle($this, "MOCK_CSV_BENE_SCORE_HEAD_DAT", $setRow["KYOUZAICD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $retHead["SCORE{$row["SEQ"]}"]              = $this->setString($row["TSCORE"],         "科目{$cnt}_得点");
                $retHead["GTZ{$row["SEQ"]}"]                = $this->setString($row["TGTZ"],           "科目{$cnt}_GTZ");
                $retHead["ALL_DEV{$row["SEQ"]}"]            = $this->setString($row["TALL_DEV"],       "科目{$cnt}_偏差値（全国）");
                $retHead["SCHOOL_DEV{$row["SEQ"]}"]         = $this->setString($row["TSCHOOL_DEV"],    "科目{$cnt}_偏差値（校内）");
                $retHead["ALL_RANK{$row["SEQ"]}"]           = $this->setString($row["TALL_RANK"],      "科目{$cnt}_順位（全国）");
                $retHead["SCHOOL_RANK{$row["SEQ"]}"]        = $this->setString($row["TSCHOOL_RANK"],   "科目{$cnt}_順位（校内）");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_SCORE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCORE{$cnt}"]              = "科目{$cnt}_得点";
                    $retHead["GTZ{$cnt}"]                = "科目{$cnt}_GTZ";
                    $retHead["ALL_DEV{$cnt}"]            = "科目{$cnt}_偏差値（全国）";
                    $retHead["SCHOOL_DEV{$cnt}"]         = "科目{$cnt}_偏差値（校内）";
                    $retHead["ALL_RANK{$cnt}"]           = "科目{$cnt}_順位（全国）";
                    $retHead["SCHOOL_RANK{$cnt}"]        = "科目{$cnt}_順位（校内）";
                    $cnt++;
                }
            }
        } else if ($this->field["COMPANY"] == "00000002" && $this->field["CSV_DIV"] == "2") {
            $query = knjh436Query::getCsvTitle($this, "MOCK_CSV_BENE_HOPE_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]       = $this->setString($setRow["TYEAR"],        "年度");
            $retHead["KYOUZAICD"]  = ($setRow["TKYOUZAI"] != "") ? $this->field["GRADE"].$setRow["TKYOUZAI"]: "教材回";
            $retHead["TYPE"]       = $this->setString($setRow["TTYPE"],        "タイプ");
            $retHead["KYOUZAINAME"]= $this->setString($setRow["TKYOUZAINAME"], "教材名称");
            $retHead["GAKKACD"]    = $this->setString($setRow["TGAKKACD"],     "学科コード");
            $retHead["GAKKANAME"]  = $this->setString($setRow["TGAKKANAME"],   "高３学科");
            $retHead["BENEID"]     = $this->setString($setRow["TBENEID"],      "BeneID");
            $retHead["HR_CLASS"]   = $this->setString($setRow["THR_CLASS"],    "高３組");
            $retHead["ATTENDNO"]   = $this->setString($setRow["TATTENDNO"],    "高３番号");
            $retHead["NAME"]       = $this->setString($setRow["TNAME"],        "氏名");
            $retHead["BUNRI_DIV"]  = $this->setString($setRow["TBUNRI_DIV"],   "文理区分");
            $retHead["BIRTHDAY"]   = $this->setString($setRow["TBIRTHDAY"],    "生年月日");
            $retHead["SEX"]        = $this->setString($setRow["TSEX"],         "性別");

            //志望校情報セット
            $cnt = 1;
            $query = knjh436Query::getCsvBeneTitle($this, "MOCK_CSV_BENE_HOPE_HEAD_DAT", $setRow["KYOUZAICD"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $retHead["SCHOOL_CD{$row["SEQ"]}"]      = $this->setString($row["TSCHOOL_CD"],     "志望{$cnt}_大学コード");
                $retHead["BOSYUTANNI{$row["SEQ"]}"]     = $this->setString($row["TBOSYUTANNI"],    "志望{$cnt}_募集単位名称");
                $retHead["SCHOOL_NAME{$row["SEQ"]}"]    = $this->setString($row["TSCHOOL_NAME"],   "志望{$cnt}_大学名称");
                $retHead["GAKUBU_NAME{$row["SEQ"]}"]    = $this->setString($row["TGAKUBU_NAME"],   "志望{$cnt}_学部名称");
                $retHead["GAKKA_NAME{$row["SEQ"]}"]     = $this->setString($row["TGAKKA_NAME"],    "志望{$cnt}_学科名称");
                $retHead["NITTEI{$row["SEQ"]}"]         = $this->setString($row["TNITTEI"],        "志望{$cnt}_日程名称");
                $retHead["HOUSIKI{$row["SEQ"]}"]        = $this->setString($row["THOUSIKI"],       "志望{$cnt}_方式名称");
                $retHead["ALL_JUDGE{$row["SEQ"]}"]      = $this->setString($row["TALL_JUDGE"],     "志望{$cnt}_総合判定");
                $retHead["JUDGE1{$row["SEQ"]}"]         = $this->setString($row["TJUDGE1"],        "志望{$cnt}_１次判定");
                $retHead["JUDGE2{$row["SEQ"]}"]         = $this->setString($row["TJUDGE2"],        "志望{$cnt}_２次判定");
                $retHead["ALL_JUDGE_SS{$row["SEQ"]}"]   = $this->setString($row["TALL_JUDGE_SS"],  "志望{$cnt}_総合判定SS");
                $retHead["JUDGE1_SS{$row["SEQ"]}"]      = $this->setString($row["TJUDGE1_SS"],     "志望{$cnt}_１次判定SS");
                $retHead["JUDGE2_SS{$row["SEQ"]}"]      = $this->setString($row["TJUDGE2_SS"],     "志望{$cnt}_２次判定SS");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_HOPE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCHOOL_CD{$cnt}"]      = "志望{$cnt}_大学コード";
                    $retHead["BOSYUTANNI{$cnt}"]     = "志望{$cnt}_募集単位名称";
                    $retHead["SCHOOL_NAME{$cnt}"]    = "志望{$cnt}_大学名称";
                    $retHead["GAKUBU_NAME{$cnt}"]    = "志望{$cnt}_学部名称";
                    $retHead["GAKKA_NAME{$cnt}"]     = "志望{$cnt}_学科名称";
                    $retHead["NITTEI{$cnt}"]         = "志望{$cnt}_日程名称";
                    $retHead["HOUSIKI{$cnt}"]        = "志望{$cnt}_方式名称";
                    $retHead["ALL_JUDGE{$cnt}"]      = "志望{$cnt}_総合判定";
                    $retHead["JUDGE1{$cnt}"]         = "志望{$cnt}_１次判定";
                    $retHead["JUDGE2{$cnt}"]         = "志望{$cnt}_２次判定";
                    $retHead["ALL_JUDGE_SS{$cnt}"]   = "志望{$cnt}_総合判定SS";
                    $retHead["JUDGE1_SS{$cnt}"]      = "志望{$cnt}_１次判定SS";
                    $retHead["JUDGE2_SS{$cnt}"]      = "志望{$cnt}_２次判定SS";
                    $cnt++;
                }
            }
        } else if ($this->field["COMPANY"] == "00000003") {
            $query = knjh436Query::getCsvTitle($this, "MOCK_CSV_ZKAI_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YEAR"]           = $this->setString($setRow["TYEAR"],        "実施年度");
            $retHead["MOSI_CD"]        = $this->setString($setRow["TMOSI_CD"],     "模試コード");
            $retHead["MOSI_NAME"]      = $this->setString($setRow["TMOSI_NAME"],   "模試名称");
            $retHead["HR_CLASS"]       = $this->setString($setRow["THR_CLASS"],    "組");
            $retHead["ATTENDNO"]       = $this->setString($setRow["TATTENDNO"],    "出席番号");
            $retHead["KANA"]           = $this->setString($setRow["TKANA"],        "氏名かな");

            //得点情報セット
            $query = knjh436Query::getCsvSundaiTitle($this, "MOCK_CSV_ZKAI_SCORE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            $cnt = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCORE{$row["SEQ"]}"]              = $this->setString($row["TSCORE"],     "科目{$cnt}_得点");
                $retHead["DEVIATION{$row["SEQ"]}"]          = $this->setString($row["TDEVIATION"], "科目{$cnt}_偏差値");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_SCORE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCORE{$cnt}"]              = "科目{$cnt}_得点";
                    $retHead["DEVIATION{$cnt}"]          = "科目{$cnt}_偏差値";
                    $cnt++;
                }
            }

            //志望校情報セット
            $query = knjh436Query::getCsvSundaiTitle($this, "MOCK_CSV_ZKAI_HOPE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            $cnt = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCHOOL_NAME{$row["SEQ"]}"]    = $this->setString($row["TSCHOOL_NAME"],  "志望校{$cnt}_名称");
                $retHead["SCHOOL_CD{$row["SEQ"]}"]      = $this->setString($row["TSCHOOL_CD"],    "志望校{$cnt}_コード");
                $retHead["JUDGE_HYOUKA{$row["SEQ"]}"]   = $this->setString($row["TJUDGE_HYOUKA"], "志望校{$cnt}_判定評価");
                $retHead["RANK{$row["SEQ"]}"]           = $this->setString($row["TRANK"],         "志望校{$cnt}_順位");
                $retHead["JUDGE_SUUTI{$row["SEQ"]}"]    = $this->setString($row["TJUDGE_SUUTI"],  "志望校{$cnt}_判定数値");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_HOPE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCHOOL_NAME{$cnt}"]    = "志望校{$cnt}_名称";
                    $retHead["SCHOOL_CD{$cnt}"]      = "志望校{$cnt}_コード";
                    $retHead["JUDGE_HYOUKA{$cnt}"]   = "志望校{$cnt}_判定評価";
                    $retHead["RANK{$cnt}"]           = "志望校{$cnt}_順位";
                    $retHead["JUDGE_SUUTI{$cnt}"]    = "志望校{$cnt}_判定数値";
                    $cnt++;
                }
            }

            $retHead["DEVIATION"]      = "追加偏差";
            $retHead["RANK"]           = "追加順位";
            $retHead["CNT"]            = "追加人数";
        } else if ($this->field["COMPANY"] == "00000004") {
            $query = knjh436Query::getCsvTitle($this, "MOCK_CSV_KAWAI_HEAD_HDAT");
            $setRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retHead["YOUSHI_NO"]    = $this->setString($setRow["TYOUSHI_NO"],   "用紙番号");
            $retHead["YEAR"]         = $this->setString($setRow["TYEAR"],   "年度");
            $retHead["MOSI_CD"]      = $this->setString($setRow["TMOSI_CD"],   "模試");
            $retHead["SCHOOL_CD"]    = $this->setString($setRow["TSCHOOL_CD"],   "高校");
            $retHead["GRADE"]        = $this->setString($setRow["TGRADE"],   "学年");
            $retHead["HR_CLASS"]     = $this->setString($setRow["THR_CLASS"],   "クラス");
            $retHead["ATTENDNO"]     = $this->setString($setRow["TATTENDNO"],   "番号");
            $retHead["KANA"]         = $this->setString($setRow["TKANA"],   "氏名");
            $retHead["EXAM_TYPE"]    = $this->setString($setRow["TEXAM_TYPE"],   "受験型");
            $retHead["BUN_RI_CD"]    = $this->setString($setRow["TBUN_RI_CD"],   "文理コード");


            //得点情報セット
            $query = knjh436Query::getCsvSundaiTitle($this, "MOCK_CSV_KAWAI_SCORE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            $cnt = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["KAMOKU_CD{$row["SEQ"]}"]      = $this->setString($row["TKAMOKU_CD"], "科目{$cnt}_コード");
                $retHead["KAMOKU_NAME{$row["SEQ"]}"]    = $this->setString($row["TKAMOKU_NAME"], $row["TKAMOKU_NAME"]);
                $retHead["HAITEN{$row["SEQ"]}"]         = $this->setString($row["THAITEN"],     "{$row["TKAMOKU_NAME"]}配点");
                $retHead["SCORE{$row["SEQ"]}"]          = $this->setString($row["TSCORE"], "{$row["TKAMOKU_NAME"]}得点");
                $retHead["DEVIATION{$row["SEQ"]}"]      = $this->setString($row["TDEVIATION"],     "{$row["TKAMOKU_NAME"]}偏差値");
                $retHead["LEVEL{$row["SEQ"]}"]          = $this->setString($row["TLEVEL"], "{$row["TKAMOKU_NAME"]}レベル");
                $cnt++;
            }
            $result->free();
            //登録されていない時は、H439でセットした項目数をセット
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_SCORE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["KAMOKU_CD{$cnt}"]      = "科目{$cnt}_コード";
                    $retHead["KAMOKU_NAME{$cnt}"]    = "科目名{$cnt}";
                    $retHead["HAITEN{$cnt}"]         = "科目{$cnt}配点";
                    $retHead["SCORE{$cnt}"]          = "科目{$cnt}得点";
                    $retHead["DEVIATION{$cnt}"]      = "科目{$cnt}偏差値";
                    $retHead["LEVEL{$cnt}"]          = "科目{$cnt}レベル";
                    $cnt++;
                }
            }

            $retHead["REMARK_A"]      = "未使用A";
            $retHead["REMARK_B"]      = "未使用B";
            $retHead["REMARK_C"]      = "未使用C";
            $retHead["REMARK_D"]      = "未使用D";
            $retHead["REMARK_E"]      = "未使用E";
            $retHead["REMARK_F"]      = "未使用F";
            $retHead["REMARK_G"]      = "未使用G";
            $retHead["REMARK_H"]      = "未使用H";
            $retHead["REMARK_I"]      = "未使用I";

            //志望校情報セット
            $query = knjh436Query::getCsvSundaiTitle($this, "MOCK_CSV_KAWAI_HOPE_HEAD_DAT", $setRow["MOSI_CD"]);
            $result = $db->query($query);
            $cnt = 1;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $retHead["SCHOOL_CD_5{$row["SEQ"]}"]    = $this->setString($row["TSCHOOL_CD_5"], "志望大{$cnt} コード5桁");
                $retHead["SCHOOL_CD_10{$row["SEQ"]}"]   = $this->setString($row["TSCHOOL_CD_10"], "志望大{$cnt} コード10桁");
                $retHead["SCHOOL_NAME{$row["SEQ"]}"]    = $this->setString($row["TSCHOOL_NAME"], "志望大{$cnt} 名称");
                $retHead["HYOUKA_SEISEKI{$row["SEQ"]}"] = $this->setString($row["THYOUKA_SEISEKI"], "志望大{$cnt} 評価成績");
                $retHead["SE_HYOUKA{$row["SEQ"]}"]      = $this->setString($row["TSE_HYOUKA"], "志望大{$cnt} セ評価");
                $retHead["NI_HYOUKA{$row["SEQ"]}"]      = $this->setString($row["TNI_HYOUKA"], "志望大{$cnt} ニ評価");
                $retHead["SOUHYOU_PO{$row["SEQ"]}"]     = $this->setString($row["TSOUHYOU_PO"], "志望大{$cnt} 総評ポ");
                $retHead["SOU_HYOUKA{$row["SEQ"]}"]     = $this->setString($row["TSOU_HYOUKA"], "志望大{$cnt} 総評価");
                $cnt++;
            }
            $result->free();
            if ($cnt == 1) {
                $query    = knjh436Query::getCsvTitleDefault($this, "MOCK_CSV_HOPE_FIELD_DAT");
                $fieldCnt = $db->getOne($query);
                for ($i = $cnt; $i <= $fieldCnt; $i++) {
                    $retHead["SCHOOL_CD_5{$cnt}}"]    = "志望大{$cnt} コード5桁";
                    $retHead["SCHOOL_CD_10{$cnt}"]   = "志望大{$cnt} コード10桁";
                    $retHead["SCHOOL_NAME{$cnt}"]    = "志望大{$cnt} 名称";
                    $retHead["HYOUKA_SEISEKI{$cnt}"] = "志望大{$cnt} 評価成績";
                    $retHead["SE_HYOUKA{$cnt}"]      = "志望大{$cnt} セ評価";
                    $retHead["NI_HYOUKA{$cnt}"]      = "志望大{$cnt} ニ評価";
                    $retHead["SOUHYOU_PO{$cnt}"]     = "志望大{$cnt} 総評ポ";
                    $retHead["SOU_HYOUKA{$cnt}"]     = "志望大{$cnt} 総評価";
                    $cnt++;
                }
            }
        }
        return $retHead;
    }

    //ブランク時、指定文言をセット
    function setString($strData, $setStr) {
        $retStr = ($strData == "") ? $setStr: $strData;

        return $retStr;
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

}
?>
