<?php

require_once('for_php7.php');
class knjl014wModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "DATADIV"          =>  VARS::post("DATADIV"),          //データ種類
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            //願書
            if ($this->field["DATADIV"] == "1") {
                $cnt = knjl014wQuery::updateQueryCsv($this, $this->data_arr);
            //調査書
            } elseif ($this->field["DATADIV"] == "2") {
                $cnt = knjl014wQuery::updateQueryCsv2($this, $this->data_arr);
            //得点
            } elseif ($this->field["DATADIV"] == "3") {
                $cnt = knjl014wQuery::updateQueryCsv3($this, $this->data_arr);
            //開示資料
            } elseif ($this->field["DATADIV"] == "4") {
                $cnt = knjl014wQuery::updateQueryCsv4($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            list($checkdata, $class_array) = $this->getCheckData($db);

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014wQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $data = $this->getData($tmp, $class_array);

                    //項目数によるファイルチェック
                    if ($checkcount !=get_count($data)) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    foreach ($checkdata as $key => $val) {
                        //ゼロ埋めしてセット
                        if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                            $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                        }
                    }
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

    public function getCheckData($db)
    {
        $checkdata = array();
        $class_array = array();
        //願書
        if ($this->field["DATADIV"] == "1") {
            $checkdata["ENTEXAMYEAR"]                       = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["APPLICANTDIV"]                      = array(1,    "HISSU",  "INT",   "入試制度");
            $checkdata["DATADIV"]                           = array(1,    "HISSU",  "INT",   "データ種類");
            $checkdata["EXAMNO"]                            = array(5,    "HISSU",  "INT2",  "受検番号");
            $checkdata["RECOM_EXAMNO"]                      = array(5,    "HISSU",  "INT2",  "受付番号");
            $checkdata["RECEPTDATE"]                        = array(10,   "HISSU",  "DATE",  "受付日付");
            $checkdata["TESTDIV"]                           = array(1,    "HISSU",  "INT",   "入試区分");
            $checkdata["DAI1_COURSECD"]                     = array(1,    "HISSU",  "INT",   "第１志望課程");
            $checkdata["DAI1_MAJORCD"]                      = array(3,    "HISSU",  "INT2",  "第１志望学科");
            $checkdata["DAI1_COURSECODE"]                   = array(4,    "HISSU",  "INT2",  "第１志望コース");
            $checkdata["DAI2_COURSECD"]                     = array(1,    "",       "INT",   "第２志望課程");
            $checkdata["DAI2_MAJORCD"]                      = array(3,    "",       "INT2",  "第２志望学科");
            $checkdata["DAI2_COURSECODE"]                   = array(4,    "",       "INT2",  "第２志望コース");
            $checkdata["GANHEN_DAI1_COURSECD"]              = array(1,    "",       "INT",   "願変前_第１志望課程");
            $checkdata["GANHEN_DAI1_MAJORCD"]               = array(3,    "",       "INT2",  "願変前_第１志望学科");
            $checkdata["GANHEN_DAI1_COURSECODE"]            = array(4,    "",       "INT2",  "願変前_第１志望コース");
            $checkdata["GANHEN_DAI2_COURSECD"]              = array(1,    "",       "INT",   "願変前_第２志望課程");
            $checkdata["GANHEN_DAI2_MAJORCD"]               = array(3,    "",       "INT2",  "願変前_第２志望学科");
            $checkdata["GANHEN_DAI2_COURSECODE"]            = array(4,    "",       "INT2",  "願変前_第２志望コース");
            $checkdata["BOSHUU_KYOUGI"]                     = array(5,    "",       "INT2",  "募集競技");
            $checkdata["TESTDIV2"]                          = array(1,    "",       "",      "追検査");
            $checkdata["OBOEGAKI_SIGANSYA"]                 = array(1,    "",       "",      "覚書志願者");
            $checkdata["KAIGAI_KIKOKUSEI_NADO"]             = array(1,    "",       "",      "海外帰国生等(様式16)");
            $checkdata["TUUGAKU_KUIKIGAI_KYOKA"]            = array(1,    "",       "",      "志願区域外許可(様式12)");
            $checkdata["HOSHOUNIN_TODOKE"]                  = array(1,    "",       "",      "保証人届(様式15)");
            $checkdata["KENGAI_CHUUGAKKOU_SHUSSHIN"]        = array(1,    "",       "",      "入学志願届(様式14ア)");
            $checkdata["KENGAI_CHUUGAKKOU_SHUSSHIN2"]       = array(1,    "",       "",      "入学志願届(様式14イ)");
            $checkdata["KENGAI_ZAIJUU"]                     = array(1,    "",       "",      "入学志願許可(様式13-1又は様式13-2)");
            $checkdata["NAME"]                              = array(120,  "HISSU",  "",      "氏名");
            $checkdata["NAME_KANA"]                         = array(240,  "HISSU",  "",      "ふりがな");
            $checkdata["BIRTHDAY"]                          = array(10,   "",       "DATE",  "生年月日");
            $checkdata["ZIPCD"]                             = array(8,    "",       "",      "郵便番号");
            $checkdata["ADDRESS1"]                          = array(90,   "",       "",      "住所");
            $checkdata["ADDRESS2"]                          = array(90,   "",       "",      "方書");
            $checkdata["TELNO"]                             = array(14,   "",       "",      "電話番号");
            $checkdata["FS_CD"]                             = array(7,    "",       "INT2",  "出身学校コード");
            $checkdata["FS_DAY"]                            = array(10,   "",       "DATE",  "卒業年月");
            $checkdata["FS_GRDDIV"]                         = array(1,    "",       "INT",   "卒業区分");
            $checkdata["GNAME"]                             = array(120,  "",       "",      "保護者氏名");
            $checkdata["GKANA"]                             = array(240,  "",       "",      "保護者ふりがな");
            $checkdata["GZIPCD"]                            = array(8,    "",       "",      "保護者郵便番号");
            $checkdata["GADDRESS1"]                         = array(90,   "",       "",      "保護者住所");
            $checkdata["GADDRESS2"]                         = array(90,   "",       "",      "保護者方書");
            $checkdata["GTELNO"]                            = array(14,   "",       "",      "保護者電話番号");
            $checkdata["RELATIONSHIP"]                      = array(2,    "",       "INT2",  "保護者続柄");
            $checkdata["REMARK1"]                           = array(60,   "",       "",      "備考１");
            $checkdata["REMARK2"]                           = array(60,   "",       "",      "備考２");
            $checkdata["JUDGEMENT"]                         = array(1,    "",       "",      "合否区分");
            $checkdata["ENTDIV"]                            = array(1,    "",       "",      "入学区分");
            $checkdata["SUC_COURSECD"]                      = array(1,    "",       "",      "合格課程");
            $checkdata["SUC_MAJORCD"]                       = array(3,    "",       "INT2",  "合格学科");
            $checkdata["SUC_COURSECODE"]                    = array(4,    "",       "INT2",  "合格コース");

        //調査書
        } elseif ($this->field["DATADIV"] == "2") {
            //教科
            $result = $db->query(knjl014wQuery::getNameMstAll($this->ObjYear, "L008"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["ABBV1"];
            }

            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
            $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
            $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT2",  "受検番号");
            $checkdata["SEX"]                   = array(1,    "HISSU",  "INT",   "性別");
            foreach ($class_array as $classcd => $label) {
                $checkdata["CONF1_RPT".$classcd]    = array(2,    "",       "INT",   "第1学年評定_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                $checkdata["CONF2_RPT".$classcd]    = array(2,    "",       "INT",   "第2学年評定_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                $checkdata["CONF3_RPT".$classcd]    = array(2,    "",       "INT",   "第3学年評定_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                $checkdata["KANTEN1_RPT".$classcd]  = array(2,    "",       "",      "第3学年観点①_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                $checkdata["KANTEN2_RPT".$classcd]  = array(2,    "",       "",      "第3学年観点②_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                $checkdata["KANTEN3_RPT".$classcd]  = array(2,    "",       "",      "第3学年観点③_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                $checkdata["KANTEN4_RPT".$classcd]  = array(2,    "",       "",      "第3学年観点④_".$label);
            }
            foreach ($class_array as $classcd => $label) {
                if ($classcd !== '01') {
                    continue;
                }
                $checkdata["KANTEN5_RPT".$classcd]  = array(2,    "",       "",      "第3学年観点⑤_".$label);
            }
            $checkdata["REMARK1"]               = array(240,  "",       "",      "備考");
            $checkdata["SHOKEN_SOUGAKU1"]       = array(750,  "",       "",      "学習活動");
            $checkdata["SHOKEN_SOUGAKU2"]       = array(750,  "",       "",      "観点");
            $checkdata["SHOKEN_SOUGAKU3"]       = array(750,  "",       "",      "評価");
            $checkdata["CHK_TOKUBETU1"]         = array(1,    "",       "",      "特別活動の記録_学級活動");
            $checkdata["CHK_TOKUBETU2"]         = array(1,    "",       "",      "特別活動の記録_生徒会活動");
            $checkdata["CHK_TOKUBETU3"]         = array(1,    "",       "",      "特別活動の記録_学校行事");
            $checkdata["CHK_KOUDOU1"]           = array(1,    "",       "",      "行動の記録_基本的な生活習慣");
            $checkdata["CHK_KOUDOU2"]           = array(1,    "",       "",      "行動の記録_健康・体力の向上");
            $checkdata["CHK_KOUDOU3"]           = array(1,    "",       "",      "行動の記録_自主・自律");
            $checkdata["CHK_KOUDOU4"]           = array(1,    "",       "",      "行動の記録_責任感");
            $checkdata["CHK_KOUDOU5"]           = array(1,    "",       "",      "行動の記録_創意工夫");
            $checkdata["CHK_KOUDOU6"]           = array(1,    "",       "",      "行動の記録_思いやり・協力");
            $checkdata["CHK_KOUDOU7"]           = array(1,    "",       "",      "行動の記録_生命尊重・自然愛護");
            $checkdata["CHK_KOUDOU8"]           = array(1,    "",       "",      "行動の記録_勤労・奉仕");
            $checkdata["CHK_KOUDOU9"]           = array(1,    "",       "",      "行動の記録_公正・公平");
            $checkdata["CHK_KOUDOU10"]          = array(1,    "",       "",      "行動の記録_公共心・公徳心");
            $checkdata["ABSENCE_DAYS1"]         = array(3,    "",       "INT",   "第1学年欠席日数");
            $checkdata["ABSENCE_REMARK1"]       = array(90,   "",       "",      "第1学年欠席理由");
            $checkdata["ABSENCE_DAYS2"]         = array(3,    "",       "INT",   "第2学年欠席日数");
            $checkdata["ABSENCE_REMARK2"]       = array(90,   "",       "",      "第2学年欠席理由");
            $checkdata["ABSENCE_DAYS3"]         = array(3,    "",       "INT",   "第3学年欠席日数");
            $checkdata["ABSENCE_REMARK3"]       = array(90,   "",       "",      "第3学年欠席理由");
            $checkdata["SHOKEN_KENKOU"]         = array(150,  "",       "",      "健康状況");
            $checkdata["SHOKEN_SONOTA"]         = array(1680,  "",       "",      "その他参考となる諸事項");
        } elseif ($this->field["DATADIV"] == "3") {
            //教科
            $result = $db->query(knjl014wQuery::getNameMstAll($this->ObjYear, "L009"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["NAME1"];
            }
            $checkdata["ENTEXAMYEAR"]               = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["APPLICANTDIV"]              = array(1,    "HISSU",  "INT",   "入試制度");
            $checkdata["DATADIV"]                   = array(1,    "HISSU",  "INT",   "データ種類");
            $checkdata["EXAMNO"]                    = array(5,    "HISSU",  "INT2",  "受検番号");
            $checkdata["TESTDIV"]                   = array(1,    "HISSU",  "INT",   "入試区分");

            foreach ($class_array as $classcd => $label) {
                $checkdata["SCORE".$classcd]  = array(3,    "",       "INT",      $label);
            }
        } elseif ($this->field["DATADIV"] == "4") {
            //教科
            $result = $db->query(knjl014wQuery::getNameMstAll($this->ObjYear, "L057"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["NAME1"];
            }
            $checkdata["ENTEXAMYEAR"]               = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["APPLICANTDIV"]              = array(1,    "HISSU",  "INT",   "入試制度");
            $checkdata["DATADIV"]                   = array(1,    "HISSU",  "INT",   "データ種類");
            $checkdata["EXAMNO"]                    = array(5,    "HISSU",  "INT2",  "受検番号");
            $checkdata["TESTDIV"]                   = array(1,    "HISSU",  "INT",   "入試区分");

            foreach ($class_array as $classcd => $label) {
                $checkdata["SCORE".$classcd]  = array(4,    "",       "",      $label);
            }
        }
        return array($checkdata, $class_array);
    }

    public function getData($tmp, $class_array)
    {
        $data = array();

        //願書
        if ($this->field["DATADIV"] == "1") {
            $data["ENTEXAMYEAR"]                = $tmp[0];
            $data["APPLICANTDIV"]               = $tmp[1];
            $data["DATADIV"]                    = $tmp[2];
            $data["EXAMNO"]                     = $tmp[3];

            $tmpCnt = 3;
            $data["RECOM_EXAMNO"]               = $tmp[++$tmpCnt];
            $data["RECEPTDATE"]                 = $tmp[++$tmpCnt];
            $data["TESTDIV"]                    = $tmp[++$tmpCnt];
            $data["DAI1_COURSECD"]              = $tmp[++$tmpCnt];
            $data["DAI1_MAJORCD"]               = $tmp[++$tmpCnt];
            $data["DAI1_COURSECODE"]            = $tmp[++$tmpCnt];
            $data["DAI2_COURSECD"]              = $tmp[++$tmpCnt];
            $data["DAI2_MAJORCD"]               = $tmp[++$tmpCnt];
            $data["DAI2_COURSECODE"]            = $tmp[++$tmpCnt];
            $data["GANHEN_DAI1_COURSECD"]       = $tmp[++$tmpCnt];
            $data["GANHEN_DAI1_MAJORCD"]        = $tmp[++$tmpCnt];
            $data["GANHEN_DAI1_COURSECODE"]     = $tmp[++$tmpCnt];
            $data["GANHEN_DAI2_COURSECD"]       = $tmp[++$tmpCnt];
            $data["GANHEN_DAI2_MAJORCD"]        = $tmp[++$tmpCnt];
            $data["GANHEN_DAI2_COURSECODE"]     = $tmp[++$tmpCnt];
            $data["BOSHUU_KYOUGI"]              = $tmp[++$tmpCnt];
            $data["TESTDIV2"]                   = $tmp[++$tmpCnt];
            $data["OBOEGAKI_SIGANSYA"]          = $tmp[++$tmpCnt];
            $data["KAIGAI_KIKOKUSEI_NADO"]      = $tmp[++$tmpCnt];
            $data["TUUGAKU_KUIKIGAI_KYOKA"]     = $tmp[++$tmpCnt];
            $data["HOSHOUNIN_TODOKE"]           = $tmp[++$tmpCnt];
            $data["KENGAI_CHUUGAKKOU_SHUSSHIN"] = $tmp[++$tmpCnt];
            $data["KENGAI_CHUUGAKKOU_SHUSSHIN2"]= $tmp[++$tmpCnt];
            $data["KENGAI_ZAIJUU"]              = $tmp[++$tmpCnt];
            $data["NAME"]                       = $tmp[++$tmpCnt];
            $data["NAME_KANA"]                  = $tmp[++$tmpCnt];
            $data["BIRTHDAY"]                   = $tmp[++$tmpCnt];
            $data["ZIPCD"]                      = $tmp[++$tmpCnt];
            $data["ADDRESS1"]                   = $tmp[++$tmpCnt];
            $data["ADDRESS2"]                   = $tmp[++$tmpCnt];
            $data["TELNO"]                      = $tmp[++$tmpCnt];
            $data["FS_CD"]                      = $tmp[++$tmpCnt];
            $data["FS_NAME"]                    = $tmp[++$tmpCnt];
            $data["FS_DAY"]                     = $tmp[++$tmpCnt];
            if (strlen($data["FS_DAY"])) {
                $data["FS_DAY"] .= "-01";
            }
            $data["FS_GRDDIV"]                  = $tmp[++$tmpCnt];
            $data["GNAME"]                      = $tmp[++$tmpCnt];
            $data["GKANA"]                      = $tmp[++$tmpCnt];
            $data["GZIPCD"]                     = $tmp[++$tmpCnt];
            $data["GADDRESS1"]                  = $tmp[++$tmpCnt];
            $data["GADDRESS2"]                  = $tmp[++$tmpCnt];
            $data["GTELNO"]                     = $tmp[++$tmpCnt];
            $data["RELATIONSHIP"]               = $tmp[++$tmpCnt];
            $data["REMARK1"]                    = $tmp[++$tmpCnt];
            $data["REMARK2"]                    = $tmp[++$tmpCnt];
            $data["JUDGEMENT"]                  = $tmp[++$tmpCnt];
            $data["ENTDIV"]                     = $tmp[++$tmpCnt];
            $data["SUC_COURSECD"]               = $tmp[++$tmpCnt];
            $data["SUC_MAJORCD"]                = $tmp[++$tmpCnt];
            $data["SUC_COURSECODE"]             = $tmp[++$tmpCnt];
            $data[$this->lastColumn]            = $tmp[++$tmpCnt];

        //調査書
        } elseif ($this->field["DATADIV"] == "2") {
            $data["ENTEXAMYEAR"]            = $tmp[0];
            $data["APPLICANTDIV"]           = $tmp[1];
            $data["DATADIV"]                = $tmp[2];
            $data["EXAMNO"]                 = $tmp[3];
            $data["SEX"]                    = $tmp[4];

            $tmpCnt = 4;
            foreach ($class_array as $classcd => $label) {
                $data["CONF1_RPT".$classcd]     = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                $data["CONF2_RPT".$classcd]     = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                $data["CONF3_RPT".$classcd]     = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                $data["KANTEN1_RPT".$classcd]   = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                $data["KANTEN2_RPT".$classcd]   = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                $data["KANTEN3_RPT".$classcd]   = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                $data["KANTEN4_RPT".$classcd]   = $tmp[++$tmpCnt];
            }
            foreach ($class_array as $classcd => $label) {
                if ($classcd !== '01') {
                    continue;
                }
                $data["KANTEN5_RPT".$classcd]   = $tmp[++$tmpCnt];
            }
            $data["REMARK1"]                = $tmp[++$tmpCnt];
            $data["SHOKEN_SOUGAKU1"]        = $tmp[++$tmpCnt];
            $data["SHOKEN_SOUGAKU2"]        = $tmp[++$tmpCnt];
            $data["SHOKEN_SOUGAKU3"]        = $tmp[++$tmpCnt];
            $data["CHK_TOKUBETU1"]          = $tmp[++$tmpCnt];
            $data["CHK_TOKUBETU2"]          = $tmp[++$tmpCnt];
            $data["CHK_TOKUBETU3"]          = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU1"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU2"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU3"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU4"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU5"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU6"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU7"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU8"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU9"]            = $tmp[++$tmpCnt];
            $data["CHK_KOUDOU10"]           = $tmp[++$tmpCnt];
            $data["ABSENCE_DAYS1"]          = $tmp[++$tmpCnt];
            $data["ABSENCE_REMARK1"]        = $tmp[++$tmpCnt];
            $data["ABSENCE_DAYS2"]          = $tmp[++$tmpCnt];
            $data["ABSENCE_REMARK2"]        = $tmp[++$tmpCnt];
            $data["ABSENCE_DAYS3"]          = $tmp[++$tmpCnt];
            $data["ABSENCE_REMARK3"]        = $tmp[++$tmpCnt];
            $data["SHOKEN_KENKOU"]          = $tmp[++$tmpCnt];
            $data["SHOKEN_SONOTA"]          = $tmp[++$tmpCnt];
            $data[$this->lastColumn]        = $tmp[++$tmpCnt];
        } elseif ($this->field["DATADIV"] == "3") {
            $data["ENTEXAMYEAR"]                = $tmp[0];
            $data["APPLICANTDIV"]               = $tmp[1];
            $data["DATADIV"]                    = $tmp[2];
            $data["EXAMNO"]                     = $tmp[3];
            $data["TESTDIV"]                    = $tmp[4];

            $tmpCnt = 4;
            foreach ($class_array as $classcd => $label) {
                $data["SCORE".$classcd]     = $tmp[++$tmpCnt];
            }
            $data[$this->lastColumn]        = $tmp[++$tmpCnt];
        } elseif ($this->field["DATADIV"] == "4") {
            $data["ENTEXAMYEAR"]                = $tmp[0];
            $data["APPLICANTDIV"]               = $tmp[1];
            $data["DATADIV"]                    = $tmp[2];
            $data["EXAMNO"]                     = $tmp[3];
            $data["TESTDIV"]                    = $tmp[4];

            $tmpCnt = 4;
            foreach ($class_array as $classcd => $label) {
                $data["SCORE".$classcd]     = $tmp[++$tmpCnt];
            }
            $data[$this->lastColumn]        = $tmp[++$tmpCnt];
        }
        return $data;
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $tyoufukuErrFlg     = false;    //重複エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $tyoufukuErrMsg     = "";       //重複エラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $tyoufukuSep        = "";
        $otherSep           = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2" || $val[2] == "FLOAT") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            //存在チェック
            $array = array("ENTEXAMYEAR","APPLICANTDIV","DATADIV");
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field["$key"] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if (in_array($key, array("DAI1_MAJORCD","DAI2_MAJORCD","GANHEN_DAI1_MAJORCD","GANHEN_DAI2_MAJORCD")) && strlen($data[$key])) {
                $daiArray = preg_split("{_}", $key);
                $dai = in_array($key, array("GANHEN_DAI1_MAJORCD","GANHEN_DAI2_MAJORCD")) ? "GANHEN_".$daiArray[1] : $daiArray[0];
                if (0 == $db->getOne(knjl014wQuery::examcoursecdCntSql($data["ENTEXAMYEAR"], $data["APPLICANTDIV"], $data["TESTDIV"], $data[$dai."_COURSECD"], $data[$dai."_MAJORCD"], $data[$dai."_COURSECODE"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == "BOSHUU_KYOUGI" && strlen($data[$key])) {
                if ($data["TESTDIV"] != '4') {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]."（スポーツ特枠選抜のみ入力可）");
                }
                if (0 == $db->getOne(knjl014wQuery::sportCdCntSql($data["ENTEXAMYEAR"], $data["BOSHUU_KYOUGI"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == "SEX" && strlen($data[$key]) && 0 == $db->getOne(knjl014wQuery::nameCntSql("Z002", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl014wQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_GRDDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl014wQuery::nameCntSql("L016", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array("RELATIONSHIP")) && strlen($data[$key]) && 0 == $db->getOne(knjl014wQuery::nameCntSql("H201", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array("TESTDIV2","OBOEGAKI_SIGANSYA","KAIGAI_KIKOKUSEI_NADO","TUUGAKU_KUIKIGAI_KYOKA")) && strlen($data[$key]) && $data[$key] != '1') {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array("CHK_TOKUBETU1","CHK_TOKUBETU2","CHK_TOKUBETU3")) && strlen($data[$key]) && $data[$key] != '1') {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array("CHK_KOUDOU1","CHK_KOUDOU2","CHK_KOUDOU3","CHK_KOUDOU4","CHK_KOUDOU5","CHK_KOUDOU6","CHK_KOUDOU7","CHK_KOUDOU8","CHK_KOUDOU9","CHK_KOUDOU10")) && strlen($data[$key]) && $data[$key] != '1') {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "") {
                $chkdata = $data[$key];
                if ($this->dateRep($chkdata) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //文字数チェック（整数部：2桁、小数部：1桁まで）
            if (($val[2] == "FLOAT") && $data[$key] != "" && is_numeric($data[$key])) {
                list($int, $decimal) = explode('.', $data[$key]);
                if (strlen($int) > 2) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]."の整数部");
                } elseif (strlen($decimal) > 1) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]."の小数部");
                }
            }
            //重複
            if ($key == "RECOM_EXAMNO") {
                $recome_cnt = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseRecome($this, $data));
                //存在チェック -- ENTEXAM_APPLICANTBASE_DAT
                if ($recome_cnt > 0) {
                    $returnFlg = $this->errSet($tyoufukuErrFlg, $tyoufukuErrMsg, "※重複:", $tyoufukuSep, "受付番号");
                }
            }
        }

        if ($this->field["DATADIV"] == "2") {
            $base_cnt = $db->getOne(knjl014wQuery::cntEntexamApplicantbaseDat($this, $data));
            //存在チェック -- ENTEXAM_APPLICANTBASE_DAT
            if ($base_cnt == 0) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "願書データ");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$tyoufukuErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl014wQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
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

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        list($header, $header_rei, $class_array) = $this->getHeaderAndRei($db);

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl014wQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field["DATADIV"] == "1") {
                    //願書
                    $query = knjl014wQuery::selectMainQuery1($this);
                } elseif ($this->field["DATADIV"] == "2") {
                    //調査書
                    $query = knjl014wQuery::selectMainQuery2($this, $class_array);
                } elseif ($this->field["DATADIV"] == "3") {
                    //調査書
                    $query = knjl014wQuery::selectMainQuery3($this, $class_array);
                } elseif ($this->field["DATADIV"] == "4") {
                    //調査書
                    $query = knjl014wQuery::selectMainQuery4($this, $class_array);
                }
            }

            if ($this->field["DATADIV"] == "1") {
                //日付変換項目一覧
                $date_array = array("RECEPTDATE","BIRTHDAY","FS_DAY");
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        if ($this->field["DATADIV"] == "1") {   //願書
                            //日付変換
                            foreach ($date_array as $key) {
                                $row[$key] = str_replace("-", "/", $row[$key]);
                                if ($key == "FS_DAY" && strlen($row[$key])) {
                                    list($year, $month, $day) = explode("/", $row[$key]);
                                    $row[$key] = $year."/".$month;
                                }
                            }
                        } elseif ($this->field["DATADIV"] == "2") {    //調査書
                        }
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    }
                    $contents .= implode(",", $row) ."\n";
                    $header_data = true;
                }
                $result->free();
            }
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl014wQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //データ種類
        $datadiv = array("1" => "願書", "2" => "調査書", "3" => "得点", "4" => "開示資料");
        $DataDivName = $datadiv[$this->field["DATADIV"]];
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$DataDivName.'データ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            if ($this->field["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv", "エラー出力");
            //エラー出力以外
            } else {
                $filename = sprintf("%s.csv", $title);
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function getHeaderAndRei($db)
    {
        $header = $header_rei = array();
        $class_array = array();

        /***********/
        /*  願 書  */
        /***********/
        if ($this->field["DATADIV"] == "1") {
            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]                  = "※入試年度";
            $header["APPLICANTDIV"]                 = "※入試制度";
            $header["DATADIV"]                      = "※データ種類";
            $header["EXAMNO"]                       = "※受検番号";
            $header["RECOM_EXAMNO"]                 = "※受付番号";
            $header["RECEPTDATE"]                   = "※受付日付";
            $header["TESTDIV"]                      = "※入試区分";
            $header["DAI1_COURSECD"]                = "※第１志望課程";
            $header["DAI1_MAJORCD"]                 = "※第１志望学科";
            $header["DAI1_COURSECODE"]              = "※第１志望コース";
            $header["DAI2_COURSECD"]                = "第２志望課程";
            $header["DAI2_MAJORCD"]                 = "第２志望学科";
            $header["DAI2_COURSECODE"]              = "第２志望コース";
            $header["GANHEN_DAI1_COURSECD"]         = "願変前_第１志望課程";
            $header["GANHEN_DAI1_MAJORCD"]          = "願変前_第１志望学科";
            $header["GANHEN_DAI1_COURSECODE"]       = "願変前_第１志望コース";
            $header["GANHEN_DAI2_COURSECD"]         = "願変前_第２志望課程";
            $header["GANHEN_DAI2_MAJORCD"]          = "願変前_第２志望学科";
            $header["GANHEN_DAI2_COURSECODE"]       = "願変前_第２志望コース";
            $header["BOSHUU_KYOUGI"]                = "募集競技";
            $header["TESTDIV2"]                     = "追検査";
            $header["OBOEGAKI_SIGANSYA"]            = "覚書志願者";
            $header["KAIGAI_KIKOKUSEI_NADO"]        = "海外帰国生等(様式16)";
            $header["TUUGAKU_KUIKIGAI_KYOKA"]       = "志願区域外許可(様式12)";
            $header["HOSHOUNIN_TODOKE"]             = "保証人届(様式15)";
            $header["KENGAI_CHUUGAKKOU_SHUSSHIN"]   = "入学志願届(様式14ア)";
            $header["KENGAI_CHUUGAKKOU_SHUSSHIN2"]  = "入学志願届(様式14イ)";
            $header["KENGAI_ZAIJUU"]                = "入学志願許可(様式13-1又は様式13-2)";
            $header["NAME"]                         = "※氏名";
            $header["NAME_KANA"]                    = "※ふりがな";
            $header["BIRTHDAY"]                     = "生年月日";
            $header["ZIPCD"]                        = "郵便番号";
            $header["ADDRESS1"]                     = "住所";
            $header["ADDRESS2"]                     = "方書";
            $header["TELNO"]                        = "電話番号";
            $header["FS_CD"]                        = "出身学校コード";
            $header["FS_NAME"]                      = "出身学校名";
            $header["FS_DAY"]                       = "卒業年月";
            $header["FS_GRDDIV"]                    = "卒業区分";
            $header["GNAME"]                        = "保護者氏名";
            $header["GKANA"]                        = "保護者ふりがな";
            $header["GZIPCD"]                       = "保護者郵便番号";
            $header["GADDRESS1"]                    = "保護者住所";
            $header["GADDRESS2"]                    = "保護者方書";
            $header["GTELNO"]                       = "保護者電話番号";
            $header["RELATIONSHIP"]                 = "保護者続柄";
            $header["REMARK1"]                      = "備考１";
            $header["REMARK2"]                      = "備考２";
            $header["JUDGEMENT"]                    = "合否区分";
            $header["ENTDIV"]                       = "入学区分";
            $header["SUC_COURSECD"]                 = "合格課程";
            $header["SUC_MAJORCD"]                  = "合格学科";
            $header["SUC_COURSECODE"]               = "合格コース";
            $header[$this->lastColumn]              = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            //卒業区分
            $fs_grddiv = implode(' ', $db->getCol(knjl014wQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));

            $header_rei["ENTEXAMYEAR"]                  = $this->ObjYear;
            $header_rei["APPLICANTDIV"]                 = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]                      = $this->field["DATADIV"];
            $header_rei["EXAMNO"]                       = "12345";
            $header_rei["RECOM_EXAMNO"]                 = "12345";
            $header_rei["RECEPTDATE"]                   = str_replace("-", "/", CTRL_DATE);
            $header_rei["TESTDIV"]                      = "1";
            $header_rei["DAI1_COURSECD"]                = "1";
            $header_rei["DAI1_MAJORCD"]                 = "123";
            $header_rei["DAI1_COURSECODE"]              = "1234";
            $header_rei["DAI2_COURSECD"]                = "1";
            $header_rei["DAI2_MAJORCD"]                 = "123";
            $header_rei["DAI2_COURSECODE"]              = "1234";
            $header_rei["GANHEN_DAI1_COURSECD"]         = "1";
            $header_rei["GANHEN_DAI1_MAJORCD"]          = "123";
            $header_rei["GANHEN_DAI1_COURSECODE"]       = "1234";
            $header_rei["GANHEN_DAI2_COURSECD"]         = "1";
            $header_rei["GANHEN_DAI2_MAJORCD"]          = "123";
            $header_rei["GANHEN_DAI2_COURSECODE"]       = "1234";
            $header_rei["BOSHUU_KYOUGI"]                = "12345";
            $header_rei["TESTDIV2"]                     = "1";
            $header_rei["OBOEGAKI_SIGANSYA"]            = "1";
            $header_rei["KAIGAI_KIKOKUSEI_NADO"]        = "1";
            $header_rei["TUUGAKU_KUIKIGAI_KYOKA"]       = "1";
            $header_rei["HOSHOUNIN_TODOKE"]             = "1";
            $header_rei["KENGAI_CHUUGAKKOU_SHUSSHIN"]   = "1";
            $header_rei["KENGAI_CHUUGAKKOU_SHUSSHIN2"]  = "1";
            $header_rei["KENGAI_ZAIJUU"]                = "1";
            $header_rei["NAME"]                         = "アルプ太郎";
            $header_rei["NAME_KANA"]                    = "あるぷたろう";
            $header_rei["BIRTHDAY"]                     = date('Y/m/d', strtotime(CTRL_DATE." -15 years"));
            $header_rei["ZIPCD"]                        = "123-4567";
            $header_rei["ADDRESS1"]                     = "全角30文字";
            $header_rei["ADDRESS2"]                     = "全角30文字";
            $header_rei["TELNO"]                        = "12-3456-7890";
            $header_rei["FS_CD"]                        = "1234567";
            $header_rei["FS_NAME"]                      = "入力の必要なし";     //表示のみ

            list($year, $month, $day) = explode("-", CTRL_DATE);
            $header_rei["FS_DAY"]                       = $year."/".$month;
            $header_rei["FS_GRDDIV"]                    = $fs_grddiv;
            $header_rei["GNAME"]                        = "アルプ父";
            $header_rei["GKANA"]                        = "あるぷちち";
            $header_rei["GZIPCD"]                       = "123-4567";
            $header_rei["GADDRESS1"]                    = "全角30文字";
            $header_rei["GADDRESS2"]                    = "全角30文字";
            $header_rei["GTELNO"]                       = "123-456-7890";
            $header_rei["RELATIONSHIP"]                 = "12";
            $header_rei["REMARK1"]                      = "全角20文字";
            $header_rei["REMARK2"]                      = "全角20文字";
            $header_rei["JUDGEMENT"]                    = "1";
            $header_rei["ENTDIV"]                       = "1";
            $header_rei["SUC_COURSECD"]                 = "1";
            $header_rei["SUC_MAJORCD"]                  = "123";
            $header_rei["SUC_COURSECODE"]               = "1234";
            $header_rei[$this->lastColumn]              = $this->lastColumn;
        }

        /**************/
        /*  調 査 書  */
        /**************/
        if ($this->field["DATADIV"] == "2") {
            //教科
            $result = $db->query(knjl014wQuery::getNameMstAll($this->ObjYear, "L008"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["ABBV1"];
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受検番号";
            $header["SEX"]                  = "※性別";
            foreach ($class_array as $classcd => $label) {
                $header["CONF1_RPT".$classcd]   = "第1学年評定_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                $header["CONF2_RPT".$classcd]   = "第2学年評定_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                $header["CONF3_RPT".$classcd]   = "第3学年評定_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                $header["KANTEN1_RPT".$classcd] = "第3学年観点①_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                $header["KANTEN2_RPT".$classcd] = "第3学年観点②_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                $header["KANTEN3_RPT".$classcd] = "第3学年観点③_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                $header["KANTEN4_RPT".$classcd] = "第3学年観点④_".$label;
            }
            foreach ($class_array as $classcd => $label) {
                if ($classcd !== '01') {
                    continue;
                }
                $header["KANTEN5_RPT".$classcd] = "第3学年観点⑤_".$label;
            }
            $header["REMARK1"]              = "備考";
            $header["SHOKEN_SOUGAKU1"]      = "学習活動";
            $header["SHOKEN_SOUGAKU2"]      = "観点";
            $header["SHOKEN_SOUGAKU3"]      = "評価";
            $header["CHK_TOKUBETU1"]        = "特別活動の記録_学級活動";
            $header["CHK_TOKUBETU2"]        = "特別活動の記録_生徒会活動";
            $header["CHK_TOKUBETU3"]        = "特別活動の記録_学校行事";
            $header["CHK_KOUDOU1"]          = "行動の記録_基本的な生活習慣";
            $header["CHK_KOUDOU2"]          = "行動の記録_健康・体力の向上";
            $header["CHK_KOUDOU3"]          = "行動の記録_自主・自律";
            $header["CHK_KOUDOU4"]          = "行動の記録_責任感";
            $header["CHK_KOUDOU5"]          = "行動の記録_創意工夫";
            $header["CHK_KOUDOU6"]          = "行動の記録_思いやり・協力";
            $header["CHK_KOUDOU7"]          = "行動の記録_生命尊重・自然愛護";
            $header["CHK_KOUDOU8"]          = "行動の記録_勤労・奉仕";
            $header["CHK_KOUDOU9"]          = "行動の記録_公正・公平";
            $header["CHK_KOUDOU10"]         = "行動の記録_公共心・公徳心";
            $header["ABSENCE_DAYS1"]        = "第1学年欠席日数";
            $header["ABSENCE_REMARK1"]      = "第1学年欠席理由";
            $header["ABSENCE_DAYS2"]        = "第2学年欠席日数";
            $header["ABSENCE_REMARK2"]      = "第2学年欠席理由";
            $header["ABSENCE_DAYS3"]        = "第3学年欠席日数";
            $header["ABSENCE_REMARK3"]      = "第3学年欠席理由";
            $header["SHOKEN_KENKOU"]        = "健康状況";
            $header["SHOKEN_SONOTA"]        = "その他参考となる諸事項";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            //性別
            $sex = implode(' ', $db->getCol(knjl014wQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));

            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "12345";
            $header_rei["SEX"]                  = $sex;
            foreach ($class_array as $classcd => $label) {
                $header_rei["CONF1_RPT".$classcd]   = "12";
            }
            foreach ($class_array as $classcd => $label) {
                $header_rei["CONF2_RPT".$classcd]   = "12";
            }
            foreach ($class_array as $classcd => $label) {
                $header_rei["CONF3_RPT".$classcd]   = "12";
            }
            foreach ($class_array as $classcd => $label) {
                $header_rei["KANTEN1_RPT".$classcd] = "A";
            }
            foreach ($class_array as $classcd => $label) {
                $header_rei["KANTEN2_RPT".$classcd] = "A";
            }
            foreach ($class_array as $classcd => $label) {
                $header_rei["KANTEN3_RPT".$classcd] = "A";
            }
            foreach ($class_array as $classcd => $label) {
                $header_rei["KANTEN4_RPT".$classcd] = "A";
            }
            foreach ($class_array as $classcd => $label) {
                if ($classcd !== '01') {
                    continue;
                }
                $header_rei["KANTEN5_RPT".$classcd] = "A";
            }
            $header_rei["REMARK1"]              = "全角80文字";
            $header_rei["SHOKEN_SOUGAKU1"]      = "全角250文字";
            $header_rei["SHOKEN_SOUGAKU2"]      = "全角250文字";
            $header_rei["SHOKEN_SOUGAKU3"]      = "全角250文字";
            $header_rei["CHK_TOKUBETU1"]        = "1";
            $header_rei["CHK_TOKUBETU2"]        = "1";
            $header_rei["CHK_TOKUBETU3"]        = "1";
            $header_rei["CHK_KOUDOU1"]          = "1";
            $header_rei["CHK_KOUDOU2"]          = "1";
            $header_rei["CHK_KOUDOU3"]          = "1";
            $header_rei["CHK_KOUDOU4"]          = "1";
            $header_rei["CHK_KOUDOU5"]          = "1";
            $header_rei["CHK_KOUDOU6"]          = "1";
            $header_rei["CHK_KOUDOU7"]          = "1";
            $header_rei["CHK_KOUDOU8"]          = "1";
            $header_rei["CHK_KOUDOU9"]          = "1";
            $header_rei["CHK_KOUDOU10"]         = "1";
            $header_rei["ABSENCE_DAYS1"]        = "123";
            $header_rei["ABSENCE_REMARK1"]      = "全角30文字";
            $header_rei["ABSENCE_DAYS2"]        = "123";
            $header_rei["ABSENCE_REMARK2"]      = "全角30文字";
            $header_rei["ABSENCE_DAYS3"]        = "123";
            $header_rei["ABSENCE_REMARK3"]      = "全角30文字";
            $header_rei["SHOKEN_KENKOU"]        = "全角50文字";
            $header_rei["SHOKEN_SONOTA"]        = "全角560文字";
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /***********/
        /*  得 点  */
        /***********/
        if ($this->field["DATADIV"] == "3") {
            //教科
            $result = $db->query(knjl014wQuery::getNameMstAll($this->ObjYear, "L009"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["NAME1"];
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受検番号";
            $header["TESTDIV"]              = "※入試区分";
            foreach ($class_array as $classcd => $label) {
                $header["SCORE".$classcd]   = $label;
            }
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "12345";
            $header_rei["TESTDIV"]              = "1";
            foreach ($class_array as $classcd => $label) {
                $header_rei["SCORE".$classcd]   = "123";
            }
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /*****************/
        /*  開 示 資 料  */
        /*****************/
        if ($this->field["DATADIV"] == "4") {
            //教科
            $result = $db->query(knjl014wQuery::getNameMstAll($this->ObjYear, "L057"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["NAME1"];
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受検番号";
            $header["TESTDIV"]              = "※入試区分";
            foreach ($class_array as $classcd => $label) {
                $header["SCORE".$classcd]   = $label;
            }
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "12345";
            $header_rei["TESTDIV"]              = "1";
            foreach ($class_array as $classcd => $label) {
                $header_rei["SCORE".$classcd]   = "1234";
            }
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }
        return array($header, $header_rei, $class_array);
    }
}
