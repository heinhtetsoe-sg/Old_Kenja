<?php

require_once('for_php7.php');
//定数定義
class knjz421bModel extends Model
{
    public $field      = array();
    public $cmd;
    public $getyear;
    public $year;
    public $senkou_no;
    //メンバ変数の初期化
    public function knjz421bModel()
    {
    }
    public function init()
    {
        $this->field = array("YEAR"                => VARS::post("YEAR"),
                             "SENKOU_NO"           => VARS::post("SENKOU_NO"),
                             "ACCEPTANCE_DATE"     => VARS::post("ACCEPTANCE_DATE"),
                             "COMPANY_CD"          => VARS::post("COMPANY_CD"),
                             "COMPANY_NAME"        => VARS::post("COMPANY_NAME"),
                             "COMPANY_NAMEKANA"    => VARS::post("COMPANY_NAMEKANA"),
                             "COMPANY_CONTENTS"    => VARS::post("COMPANY_CONTENTS"),
                             "SHIHONKIN"           => VARS::post("SHIHONKIN"),
                             "MAKECOMPANY_YEAR"    => VARS::post("MAKECOMPANY_YEAR"),
                             "COMPANY_ZIPCD"       => VARS::post("COMPANY_ZIPCD"),
                             "COMPANY_ADDR1"       => VARS::post("COMPANY_ADDR1"),
                             "COMPANY_ADDR2"       => VARS::post("COMPANY_ADDR2"),
                             "COMPANY_TELNO1"      => VARS::post("COMPANY_TELNO1"),
                             "COMPANY_TELNO2"      => VARS::post("COMPANY_TELNO2"),
                             "COMPANY_FAXNO"       => VARS::post("COMPANY_FAXNO"),
                             "INDUSTRY_SCD"        => VARS::post("INDUSTRY_SCD"),
                             "SONINZU"             => VARS::post("SONINZU"),
                             "SHUSHOKU_NAME"       => VARS::post("SHUSHOKU_NAME"),
                             "SHUSHOKU_ZIPCD"      => VARS::post("SHUSHOKU_ZIPCD"),
                             "SHUSHOKU_ADDR1"      => VARS::post("SHUSHOKU_ADDR1"),
                             "SHUSHOKU_ADDR2"      => VARS::post("SHUSHOKU_ADDR2"),
                             "SHUSHOKU_TELNO1"     => VARS::post("SHUSHOKU_TELNO1"),
                             "SHUSHOKU_TELNO2"     => VARS::post("SHUSHOKU_TELNO2"),
                             "SHUSHOKU_FAXNO"      => VARS::post("SHUSHOKU_FAXNO"),
                             "TONINZU"             => VARS::post("TONINZU"),
                             "COMPANY_SORT"        => VARS::post("COMPANY_SORT"),
                             "COMPANY_SORT_REMARK" => VARS::post("COMPANY_SORT_REMARK"),
                             "TARGET_NINZU"        => VARS::post("TARGET_NINZU"),
                             "TARGET_SEX"          => VARS::post("TARGET_SEX"),
                             "JUDGING_MEANS1"      => VARS::post("JUDGING_MEANS1"),
                             "JUDGING_MEANS2"      => VARS::post("JUDGING_MEANS2"),
                             "JUDGING_MEANS3"      => VARS::post("JUDGING_MEANS3"),
                             "JUDGING_MEANS4"      => VARS::post("JUDGING_MEANS4"),
                             "HOLIDAY1"            => VARS::post("HOLIDAY1"),
                             "HOLIDAY2"            => VARS::post("HOLIDAY2"),
                             "HOLIDAY3"            => VARS::post("HOLIDAY3"),
                             "HOLIDAY4"            => VARS::post("HOLIDAY4"),
                             "HOLIDAY_REMARK"      => VARS::post("HOLIDAY_REMARK"),
                             "SHIFT"               => VARS::post("SHIFT"),
                             "COMPANY_LODGING"     => VARS::post("COMPANY_LODGING"),
                             "BASIC_SALARY"        => VARS::post("BASIC_SALARY"),
                             "TAKE_SALARY"         => VARS::post("TAKE_SALARY"),
                             "KANKATSU"            => VARS::post("KANKATSU"),
                             "JOBTYPE_LCD"         => VARS::post("JOBTYPE_LCD"),
                             "JOBTYPE_MCD"         => VARS::post("JOBTYPE_MCD"),
                             "JOBTYPE_SCD"         => VARS::post("JOBTYPE_SCD"),
                             "JOBTYPE_SSCD"        => VARS::post("JOBTYPE_SSCD"),
                             "TSUKIN_NINZU"        => VARS::post("TSUKIN_NINZU"),
                             "SUMIKOMI_NINZU"      => VARS::post("SUMIKOMI_NINZU"),
                             "FUMON_NINZU"         => VARS::post("FUMON_NINZU"),
                             "SUISEN_NINZU"        => VARS::post("SUISEN_NINZU"),
                             "NIJI_BOSYU"          => VARS::post("NIJI_BOSYU"),
                             "TSUKIN_SALARY"       => VARS::post("TSUKIN_SALARY"),
                             "SUMIKOMI_SALARY"     => VARS::post("SUMIKOMI_SALARY"),
                             "SELECT_RECEPT_DATE"  => VARS::post("SELECT_RECEPT_DATE"),
                             "SELECT_DATE"         => VARS::post("SELECT_DATE"),
                             "KENGAKU_KAI"         => VARS::post("KENGAKU_KAI"),
                             "OTHER_HOLIDAY"       => VARS::post("OTHER_HOLIDAY"),
                             "REMARK"              => VARS::post("REMARK"),
                             "EMPLOYMENT_STATUS"   => VARS::post("EMPLOYMENT_STATUS"),
                             "APPLICATION_TARGET"  => VARS::post("APPLICATION_TARGET"),
                             "PERSONNEL_MANAGER"   => VARS::post("PERSONNEL_MANAGER"),
                             "DEPARTMENT_POSITION" => VARS::post("DEPARTMENT_POSITION"),
                             "OUTPUT"              => VARS::post("OUTPUT"),
                             "HEADER"              => VARS::post("HEADER")
        );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        $this->pdffile = VARS::file("PDF_FILE");

        if (VARS::get("PROGRAMID")) {
            $this->year = CTRL_YEAR;
            $this->getyear = "";
            $this->senkou_no = "";
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::post("YEAR");
        }
        if (VARS::get("YEAR") != "") {
            $this->getyear = VARS::get("YEAR");
        }
        if (VARS::get("SENKOU_NO") != "") {
            $this->senkou_no = VARS::get("SENKOU_NO");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //「useCompany_Sort」プロパティの値によって切り替わる「職種」のフィールド名とバイト数を保持(csv作成処理の中で使用)
        $this->altCompanySortName = ($this->Properties["useCompany_Sort"] == "1") ? "COMPANY_SORT_REMARK" : "COMPANY_SORT";
        $this->altCompanySortByte = ($this->Properties["useCompany_Sort"] == "1") ? 60 : 2;
    }

    // 学校・会社マスタ検索チェック
    public function chkCollegeOrCompanyMst($company_cd)
    {
        $db = Query::dbCheckOut();
        $row = $db->getRow(knjz421bQuery::getCollegeOrCompanyMst($company_cd), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        $Msg = "会社";
        if (!isset($row)) {
            $this->setWarning(MSG303, "該当する".$Msg."コードはありません。$company_cd");
            return false;
        }
        return true;
    }

    //PDFアップロード
    public function &getExecModel()
    {
        if ($this->senkou_no == "") {
            $this->setWarning("MSG304", "登録済みの求人データからしかアップロードできません。");
            return false;
        }
        //ファイル名取得
        $target = $_FILES['PDF_FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "pdf" && $kakutyousi != "PDF") {
            $this->warning = "PDFファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        $filename = DOCUMENTROOT ."/" .'pdf_download/' .$this->senkou_no .'.pdf';
        if (!is_uploaded_file($this->pdffile['tmp_name'])) {
            $this->warning = "ファイル名又は\\nサイズ(500KB以上)等が不正です。";
        } elseif ($this->senkou_no == "") {
            $this->warning = "求人番号を指定して下さい。";
        } else {
            if (move_uploaded_file($this->pdffile['tmp_name'], $filename) == false) {
                $this->warning = "PDFファイルの取込失敗です。";
            } else {
                $this->message = "PDFファイルの取込正常です。";
            }
        }
    }

    //PDFダウンロード
    public function getPdfModel()
    {
        $path_file = $this->field["SENKOU_NO"].'.pdf';
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->field["SENKOU_NO"]);

        $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;

        //ファイルの存在確認
        if (!file_exists($path_file)) {
            $this->setWarning("MSG303", "指定の求人番号のPDFデータは存在しません。");
            return false;
        }

        //オープンできるか確認
        if (!($fp = fopen($path_file, "r"))) {
            $this->setWarning("MSG203", "指定の求人番号のPDFデータは開くことができません。");
            return false;
        }
        fclose($fp);

        //ファイルサイズの確認
        if (($content_length = filesize($path_file)) == 0) {
            $this->setWarning("MSG203", "指定の求人番号のPDFデータのファイルサイズは 0 byteです。");
            return false;
        }

        ini_set('mbstring.http_output', 'pass');

        //ダウンロード用のHTTPヘッダ送信
        header('Content-Type: application/pdf');
        common::setDownloadHeader($path_file);
        header("Content-Length: ".$content_length);

        //ファイルを読んで出力
        if (!readfile($path_file)) {
            $this->setWarning("MSG203", "指定の求人番号のPDFデータは開くことができません。");
            return false;
        }

        return true;
    }

    //CSVファイル（ヘッダ出力 / 書出 / エラー出力）
    public function getDownloadCsvModel()
    {
        $data = false;
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ（エラー出力）
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $contents = implode(",", $header) . "\r\n";
            $query = knjz421bQuery::getErrorCsvData();
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ（ヘッダ出力 / 書出）   フィールド => ヘッダ名, ヘッダ例
            $csvItem = array("YEAR"                    => array("※年度",                "4バイト"),
                             "SENKOU_NO"               => array("※求人番号",            "5バイト"),
                             "ACCEPTANCE_DATE"         => array("※受付年月日",          "10バイト"),
                             "KANKATSU"                => array("※管轄",                "4バイト"),
                             "COMPANY_CD"              => array("※会社コード",          "8バイト"),
                             "COMPANY_NAME"            => array("※会社名",              "150バイト"),
                             "COMPANY_NAMEKANA"        => array("ふりがな",              "150バイト"),
                             "COMPANY_CONTENTS"        => array("事業内容",              "150バイト"),
                             "MAKECOMPANY_YEAR"        => array("設立年度",              "4バイト"),
                             "SHIHONKIN"               => array("資本金",                "17バイト"),
                             "SONINZU"                 => array("全体人数",              "8バイト"),
                             "TONINZU"                 => array("就業場所人数",          "8バイト"),
                             "COMPANY_ZIPCD"           => array("郵便番号",              "8バイト"),
                             "COMPANY_ADDR1"           => array("※住所1",               "150バイト"),
                             "COMPANY_ADDR2"           => array("※住所2",               "90バイト"),
                             "COMPANY_TELNO1"          => array("※電話番号1",           "14バイト"),
                             "COMPANY_TELNO2"          => array("電話番号2",             "14バイト"),
                             "COMPANY_FAXNO"           => array("FAX番号",               "14バイト"),
                             "INDUSTRY_SCD"            => array("※産業分類",            "3バイト"),
                             "SHUSHOKU_NAME"           => array("就業場所",              "150バイト"),
                             "SHUSHOKU_ZIPCD"          => array("※就業場所・郵便番号",  "8バイト"),
                             "SHUSHOKU_ADDR1"          => array("※就業場所・住所1",     "150バイト"),
                             "SHUSHOKU_ADDR2"          => array("※就業場所・住所2",     "90バイト"),
                             "SHUSHOKU_TELNO1"         => array("※就業場所・電話番号1", "14バイト"),
                             "SHUSHOKU_TELNO2"         => array("就業場所・電話番号2",   "14バイト"),
                             "SHUSHOKU_FAXNO"          => array("就業場所・FAX番号",     "14バイト"),
                             "JOBTYPE_LCD"             => array("※職業分類大",          "2バイト"),
                             "JOBTYPE_MCD"             => array("※職業分類中",          "2バイト"),
                             "JOBTYPE_SCD"             => array("※職業分類小",          "6バイト"),
                             "EMPLOYMENT_STATUS"       => array("雇用形態",              "2バイト"),
                             $this->altCompanySortName => array("職種",                  $this->altCompanySortByte."バイト"),
                             "APPLICATION_TARGET"      => array("募集対象",              "60バイト"),
                             "TSUKIN_NINZU"            => array("※通勤人数",            "3バイト"),
                             "SUMIKOMI_NINZU"          => array("※住込人数",            "3バイト"),
                             "FUMON_NINZU"             => array("※不問人数",            "3バイト"),
                             "SUISEN_NINZU"            => array("※推薦人数",            "3バイト"),
                             "NIJI_BOSYU"              => array("二次募集",              "1:有"),
                             "TSUKIN_SALARY"           => array("賃金通勤",              "8バイト"),
                             "SUMIKOMI_SALARY"         => array("賃金住込",              "8バイト"),
                             "SELECT_RECEPT_DATE"      => array("選考受付",              "10バイト"),
                             "SELECT_DATE"             => array("選考日",                "10バイト"),
                             "JUDGING_MEANS1"          => array("選考方法1",             "2バイト"),
                             "JUDGING_MEANS2"          => array("選考方法2",             "2バイト"),
                             "JUDGING_MEANS3"          => array("選考方法3",             "2バイト"),
                             "JUDGING_MEANS4"          => array("選考方法4",             "2バイト"),
                             "PERSONNEL_MANAGER"       => array("担当者名",              "30バイト"),
                             "DEPARTMENT_POSITION"     => array("担当者課係",            "40バイト"),
                             "KENGAKU_KAI"             => array("見学会",                "1:有"),
                             "HOLIDAY1"                => array("休日・土",              "1:有"),
                             "HOLIDAY2"                => array("休日・日",              "1:有"),
                             "HOLIDAY3"                => array("休日・祝",              "1:有"),
                             "HOLIDAY4"                => array("休日・他",              "1:有"),
                             "HOLIDAY_REMARK"          => array("休日・他入力",          "50バイト"),
                             "OTHER_HOLIDAY"           => array("休暇",                  "150バイト"),
                             "REMARK"                  => array("備考",                  "150バイト"),
                             "LASTCOLUMN"              => array("LASTCOLUMN",            "LASTCOLUMN")
            );

            $contents = "";
            //ヘッダ・ヘッダ例取得
            $header = $header_rei = "";
            foreach ($csvItem as $key => $val) {
                $header     .= ($header == "") ? $val[0] : ",".$val[0];
                $header_rei .= ($header_rei == "") ? $val[1] : ",".$val[1];
            }
            //ヘッダセット
            if ($this->field["HEADER"]) {
                $contents .= $header."\n";
            }
            $query = knjz421bQuery::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            //ヘッダ例セット
            $contents .= $header_rei."\n";
            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row['JOBTYPE_SCD'] = $row['JOBTYPE_SCD'] ."-". $row['JOBTYPE_SSCD'];
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                if ($this->field["OUTPUT"] == "2") {
                    $cnt = 0;
                    foreach ($csvItem as $key => $val) {
                        if ($key == "JOBTYPE_SSCD") {
                        } else {
                            $contents .= ($cnt == 0) ? $row[$key] : ",".$row[$key];
                            $cnt++;
                        }
                    }
                } else {
                    $contents .= implode($row, ",");
                }

                $contents .= "\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "求職票マスタエラー";
            } else {
                $couse = "求職票マスタ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVファイル（取込）
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $cnt = knjz421bQuery::insertQueryCsv($db, $this, $this->data_arr);
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);
            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"                    =>  array(4,    "年度"),
                               "SENKOU_NO"               =>  array(5,    "求人番号"),
                               "ACCEPTANCE_DATE"         =>  array(10,   "受付年月日"),
                               "KANKATSU"                =>  array(4,    "管轄"),
                               "COMPANY_CD"              =>  array(8,    "会社コード"),
                               "COMPANY_NAME"            =>  array(150,   "会社名"),
                               "COMPANY_NAMEKANA"        =>  array(150,  "ふりがな"),
                               "COMPANY_CONTENTS"        =>  array(150,  "事業内容"),
                               "MAKECOMPANY_YEAR"        =>  array(4,    "設立年度"),
                               "SHIHONKIN"               =>  array(17,   "資本金"),
                               "SONINZU"                 =>  array(8,    "全体人数"),
                               "TONINZU"                 =>  array(8,    "就業場所人数"),
                               "COMPANY_ZIPCD"           =>  array(8,    "郵便番号"),
                               "COMPANY_ADDR1"           =>  array(150,  "住所1"),
                               "COMPANY_ADDR2"           =>  array(90,   "住所2"),
                               "COMPANY_TELNO1"          =>  array(14,   "電話番号1"),
                               "COMPANY_TELNO2"          =>  array(14,   "電話番号2"),
                               "COMPANY_FAXNO"           =>  array(14,   "FAX番号"),
                               "INDUSTRY_SCD"            =>  array(3,    "産業分類"),
                               "SHUSHOKU_NAME"           =>  array(150,  "就業場所"),
                               "SHUSHOKU_ZIPCD"          =>  array(8,    "就業場所・郵便番号"),
                               "SHUSHOKU_ADDR1"          =>  array(150,  "就業場所・住所1"),
                               "SHUSHOKU_ADDR2"          =>  array(90,   "就業場所・住所2"),
                               "SHUSHOKU_TELNO1"         =>  array(14,   "就業場所・電話番号1"),
                               "SHUSHOKU_TELNO2"         =>  array(14,   "就業場所・電話番号2"),
                               "SHUSHOKU_FAXNO"          =>  array(14,   "就業場所・FAX番号"),
                               "JOBTYPE_LCD"             =>  array(2,    "職業分類大"),
                               "JOBTYPE_MCD"             =>  array(2,    "職業分類中"),
                               "JOBTYPE_SCD"             =>  array(6,    "職業分類小"),
                               "JOBTYPE_SSCD"            =>  array(2,    "職業分類細"),
                               "EMPLOYMENT_STATUS"       =>  array(2,    "雇用形態"),
                               $this->altCompanySortName =>  array($this->altCompanySortByte,    "職種"),
                               "APPLICATION_TARGET"      =>  array(60,   "募集対象"),
                               "TSUKIN_NINZU"            =>  array(3,    "通勤人数"),
                               "SUMIKOMI_NINZU"          =>  array(3,    "住込人数"),
                               "FUMON_NINZU"             =>  array(3,    "不問人数"),
                               "SUISEN_NINZU"            =>  array(3,    "推薦人数"),
                               "NIJI_BOSYU"              =>  array(1,    "二次募集"),
                               "TSUKIN_SALARY"           =>  array(8,    "賃金通勤"),
                               "SUMIKOMI_SALARY"         =>  array(8,    "賃金住込"),
                               "SELECT_RECEPT_DATE"      =>  array(10,   "選考受付"),
                               "SELECT_DATE"             =>  array(10,   "選考日"),
                               "JUDGING_MEANS1"          =>  array(2,    "選考方法1"),
                               "JUDGING_MEANS2"          =>  array(2,    "選考方法2"),
                               "JUDGING_MEANS3"          =>  array(2,    "選考方法3"),
                               "JUDGING_MEANS4"          =>  array(2,    "選考方法4"),
                               "PERSONNEL_MANAGER"       =>  array(30,   "担当者名"),
                               "DEPARTMENT_POSITION"     =>  array(40,   "担当者課係"),
                               "KENGAKU_KAI"             =>  array(1,    "見学会"),
                               "HOLIDAY1"                =>  array(1,    "休日・土"),
                               "HOLIDAY2"                =>  array(1,    "休日・日"),
                               "HOLIDAY3"                =>  array(1,    "休日・祝"),
                               "HOLIDAY4"                =>  array(1,    "休日・他"),
                               "HOLIDAY_REMARK"          =>  array(50,   "休日・他入力"),
                               "OTHER_HOLIDAY"           =>  array(150,  "休暇"),
                               "REMARK"                  =>  array(150,  "備考")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query  = knjz421bQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
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
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $jobtype = explode('-', $tmp[28]);
                    $data = array("YEAR"                    => $tmp[0],
                                  "SENKOU_NO"               => $tmp[1],
                                  "ACCEPTANCE_DATE"         => $tmp[2],
                                  "KANKATSU"                => $tmp[3],
                                  "COMPANY_CD"              => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]): $tmp[4],
                                  "COMPANY_NAME"            => $tmp[5],
                                  "COMPANY_NAMEKANA"        => $tmp[6],
                                  "COMPANY_CONTENTS"        => $tmp[7],
                                  "MAKECOMPANY_YEAR"        => $tmp[8],
                                  "SHIHONKIN"               => $tmp[9],
                                  "SONINZU"                 => $tmp[10],
                                  "TONINZU"                 => $tmp[11],
                                  "COMPANY_ZIPCD"           => $tmp[12],
                                  "COMPANY_ADDR1"           => $tmp[13],
                                  "COMPANY_ADDR2"           => $tmp[14],
                                  "COMPANY_TELNO1"          => $tmp[15],
                                  "COMPANY_TELNO2"          => $tmp[16],
                                  "COMPANY_FAXNO"           => $tmp[17],
                                  "INDUSTRY_SCD"            => $tmp[18],
                                  "SHUSHOKU_NAME"           => $tmp[19],
                                  "SHUSHOKU_ZIPCD"          => $tmp[20],
                                  "SHUSHOKU_ADDR1"          => $tmp[21],
                                  "SHUSHOKU_ADDR2"          => $tmp[22],
                                  "SHUSHOKU_TELNO1"         => $tmp[23],
                                  "SHUSHOKU_TELNO2"         => $tmp[24],
                                  "SHUSHOKU_FAXNO"          => $tmp[25],
                                  "JOBTYPE_LCD"             => $tmp[26],
                                  "JOBTYPE_MCD"             => (strlen(trim($tmp[27]))) ? sprintf("%02d", $tmp[27]): $tmp[27],
                                  "JOBTYPE_SCD"             => (strlen(trim($jobtype[0]))) ? $this->getZeroPaddingScd($jobtype[0], "S"): $jobtype[0],
                                  "JOBTYPE_SSCD"            => (strlen(trim($jobtype[1]))) ? $this->getZeroPaddingScd($jobtype[1], "SS"): $jobtype[1],
                                  "EMPLOYMENT_STATUS"       => (strlen(trim($tmp[29])) && is_numeric($tmp[30])) ? sprintf("%02d", $tmp[29]) : $tmp[29],
                                  $this->altCompanySortName => $tmp[30],
                                  "APPLICATION_TARGET"      => $tmp[31],
                                  "TSUKIN_NINZU"            => $tmp[32],
                                  "SUMIKOMI_NINZU"          => $tmp[33],
                                  "FUMON_NINZU"             => $tmp[34],
                                  "SUISEN_NINZU"            => $tmp[35],
                                  "NIJI_BOSYU"              => $tmp[36],
                                  "TSUKIN_SALARY"           => $tmp[37],
                                  "SUMIKOMI_SALARY"         => $tmp[38],
                                  "SELECT_RECEPT_DATE"      => $tmp[39],
                                  "SELECT_DATE"             => $tmp[40],
                                  "JUDGING_MEANS1"          => (strlen(trim($tmp[41])) && is_numeric($tmp[41])) ? sprintf("%02d", $tmp[41]) : $tmp[41],
                                  "JUDGING_MEANS2"          => (strlen(trim($tmp[42])) && is_numeric($tmp[42])) ? sprintf("%02d", $tmp[42]) : $tmp[42],
                                  "JUDGING_MEANS3"          => (strlen(trim($tmp[43])) && is_numeric($tmp[43])) ? sprintf("%02d", $tmp[43]) : $tmp[43],
                                  "JUDGING_MEANS4"          => (strlen(trim($tmp[44])) && is_numeric($tmp[44])) ? sprintf("%02d", $tmp[44]) : $tmp[44],
                                  "PERSONNEL_MANAGER"       => $tmp[45],
                                  "DEPARTMENT_POSITION"     => $tmp[46],
                                  "KENGAKU_KAI"             => $tmp[47],
                                  "HOLIDAY1"                => $tmp[48],
                                  "HOLIDAY2"                => $tmp[49],
                                  "HOLIDAY3"                => $tmp[50],
                                  "HOLIDAY4"                => $tmp[51],
                                  "HOLIDAY_REMARK"          => $tmp[52],
                                  "OTHER_HOLIDAY"           => $tmp[53],
                                  "REMARK"                  => $tmp[54]
                    );

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
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $existenceErrFlg1   = false;    //存在エラーフラグ
        $existenceErrFlg2   = false;    //存在エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $existenceErrMsg1   = "";       //存在エラーメッセージ
        $existenceErrMsg2   = "";       //存在エラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $existenceSep1      = "";
        $existenceSep2      = "";
        $otherSep           = "";

        $checkNameMstCd = array("COMPANY_SORT"      => "E008",
                                "TARGET_SEX"        => "E007",
                                "JUDGING_MEANS1"    => "E029",
                                "JUDGING_MEANS2"    => "E029",
                                "JUDGING_MEANS3"    => "E029",
                                "JUDGING_MEANS4"    => "E029",
                                "EMPLOYMENT_STATUS" => "E053",
                                );

        $chkJobName = array("JOBTYPE_LCD"  => "大",
                            "JOBTYPE_MCD"  => "中",
                            "JOBTYPE_SCD"  => "小",
                            "JOBTYPE_SSCD" => "細",
                            );

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            $arychkflg = false;
            if ($this->Properties["joblabel_requiredinput"] == "1") {
                if (in_array($key, array("YEAR", "SENKOU_NO", "ACCEPTANCE_DATE", "KANKATSU", "COMPANY_CD", "COMPANY_NAME", "COMPANY_ADDR1", "COMPANY_ADDR2",
                                     "COMPANY_TELNO1", "INDUSTRY_SCD", "SHUSHOKU_ZIPCD", "SHUSHOKU_ADDR1", "SHUSHOKU_ADDR2", "SHUSHOKU_TELNO1",
                                     "JOBTYPE_LCD", "JOBTYPE_MCD", "JOBTYPE_SCD", "JOBTYPE_SSCD", "TSUKIN_NINZU", "SUMIKOMI_NINZU", "FUMON_NINZU", "SUISEN_NINZU"))) {
                    $arychkflg = true;
                }
            } else {
                if (in_array($key, array("YEAR", "SENKOU_NO", "ACCEPTANCE_DATE", "KANKATSU", "COMPANY_CD", "COMPANY_NAME", "COMPANY_ADDR1",
                                     "COMPANY_TELNO1", "INDUSTRY_SCD",
                                     "JOBTYPE_LCD", "JOBTYPE_MCD", "JOBTYPE_SCD", "JOBTYPE_SSCD", "TSUKIN_NINZU", "SUMIKOMI_NINZU", "FUMON_NINZU", "SUISEN_NINZU"))) {
                    $arychkflg = true;
                }
            }
            if ($arychkflg) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR", "SENKOU_NO", "COMPANY_CD", "MAKECOMPANY_YEAR", "INDUSTRY_SCD", "SONINZU", "TONINZU", "TARGET_NINZU", "BASIC_SALARY", "TAKE_SALARY"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //存在チェック（名称マスタ）
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjz421bQuery::checkNameMst($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※存在(名称マスタ):", $existenceSep1, $val[1]);
                }
            }
            //存在チェック（職業分類マスタ）
            if (in_array($key, array("JOBTYPE_LCD", "JOBTYPE_MCD", "JOBTYPE_SCD" , "JOBTYPE_SSCD"))) {
                $query = knjz421bQuery::checkJobtypeCd($key, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg1, $existenceErrMsg1, "※存在(職業分類マスタ):", $existenceSep1, "職業分類".$chkJobName[$key]);
                }
            }
            //存在チェック（値が1以外）
            if (in_array($key, array("HOLIDAY1", "HOLIDAY2", "HOLIDAY3", "HOLIDAY4", "SHIFT", "COMPANY_LODGING"))) {
                if ($data[$key] != "" && $data[$key] != "1") {
                    $returnFlg = $this->errSet($existenceErrFlg2, $existenceErrMsg2, "※存在(1以外):", $existenceSep2, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        //対象外チェック（重複）
        if ($data["YEAR"] != "" && $data["SENKOU_NO"] != "" && is_numeric($data["SENKOU_NO"])) {
            $query = knjz421bQuery::checkJobOfferDat($data["YEAR"], $data["SENKOU_NO"]);
            $rowCount = $db->getOne($query);
            if ($rowCount > "0") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※対象外:", $otherSep, "重複データ");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg1.$existenceErrMsg2.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjz421bQuery::insertQueryErr($db, $line, $check_error);
        }

        return $returnFlg;
    }

    //エラーメッセージをセット
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

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if (trim($this->field["SENKOU_NO"]) == "") {
            $this->setWarning("MSG301", "(求人番号)");
            return false;
        }

        if ($kind == "update" || $kind == "delete") {
            //リストより選択していない
            if ($this->senkou_no == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["SENKOU_NO"] != $this->senkou_no) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }

            //ゼロ埋め
            //会社コード
            $this->field["COMPANY_CD"] = sprintf("%08d", $this->field["COMPANY_CD"]);
            //職業分類小細コード
            $this->field["JOBTYPE_SCD"]  = $this->getZeroPaddingScd($this->field["JOBTYPE_SCD"], "S");
            $this->field["JOBTYPE_SSCD"] = $this->getZeroPaddingScd($this->field["JOBTYPE_SSCD"], "SS");

            //１レコード取得
            $row = knjz421bQuery::getRow($this->getyear, $this->senkou_no);

            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($kind == "update" || $kind == "insert") {
            if (trim($this->field["ACCEPTANCE_DATE"]) == "") {
                $this->setWarning("MSG301", "(受付年月日)");
                return false;
            }
            if (trim($this->field["KANKATSU"]) == "") {
                $this->setWarning("MSG301", "(管轄)");
                return false;
            }
            if (trim($this->field["COMPANY_CD"]) == "") {
                $this->setWarning("MSG301", "(会社コード)");
                return false;
            }
            if (trim($this->field["COMPANY_NAME"]) == "") {
                $this->setWarning("MSG301", "(会社名)");
                return false;
            }
            if (trim($this->field["COMPANY_ADDR1"]) == "") {
                $this->setWarning("MSG301", "(住所1)");
                return false;
            }
            if ($this->Properties["joblabel_requiredinput"] == "1") {
                if (trim($this->field["COMPANY_ADDR2"]) == "") {
                    $this->setWarning("MSG301", "(住所2)");
                    return false;
                }
            }
            if (trim($this->field["COMPANY_TELNO1"]) == "") {
                $this->setWarning("MSG301", "(電話番号1)");
                return false;
            }
            if (trim($this->field["INDUSTRY_SCD"]) == "") {
                $this->setWarning("MSG301", "(産業分類)");
                return false;
            }
            if (trim($this->field["JOBTYPE_SCD"]) == "" || trim($this->field["JOBTYPE_SSCD"]) == "") {
                $this->setWarning("MSG301", "(職業分類)");
                return false;
            }
            if ($this->Properties["joblabel_requiredinput"] == "1") {
                if (trim($this->field["SHUSHOKU_ZIPCD"]) == "") {
                    $this->setWarning("MSG301", "(郵便番号)");
                    return false;
                }
                if (trim($this->field["SHUSHOKU_ADDR1"]) == "") {
                    $this->setWarning("MSG301", "(住所1)");
                    return false;
                }
                if (trim($this->field["SHUSHOKU_ADDR2"]) == "") {
                    $this->setWarning("MSG301", "(住所2)");
                    return false;
                }
                if (trim($this->field["SHUSHOKU_TELNO1"]) == "") {
                    $this->setWarning("MSG301", "(電話番号1)");
                    return false;
                }
            }
            if (trim($this->field["TSUKIN_NINZU"]) == "" || trim($this->field["SUMIKOMI_NINZU"]) == "" || trim($this->field["FUMON_NINZU"]) == "") {
                $this->setWarning("MSG301", "(求人数)");
                return false;
            }
            if (trim($this->field["SUISEN_NINZU"]) == "") {
                $this->setWarning("MSG301", "(推薦人数)");
                return false;
            }

            //ゼロ埋め
            //会社コード
            $this->field["COMPANY_CD"] = sprintf("%08d", $this->field["COMPANY_CD"]);
            //職業分類小細コード
            $this->field["JOBTYPE_SCD"]  = $this->getZeroPaddingScd($this->field["JOBTYPE_SCD"], "S");
            $this->field["JOBTYPE_SSCD"] = $this->getZeroPaddingScd($this->field["JOBTYPE_SSCD"], "SS");
            //存在チェック
            $db = Query::dbCheckOut();
            $jobType_sName = $db->getOne(knjz421bQuery::getjobTypeSName($this, $this->field["JOBTYPE_LCD"], $this->field["JOBTYPE_MCD"], $this->field["JOBTYPE_SCD"], $this->field["JOBTYPE_SSCD"]));
            Query::dbCheckIn($db);
            if ($jobType_sName == "") {
                $this->setWarning("該当する職業分類コードはありません", "");
                return false;
            }

            //１レコード取得
            $row = knjz421bQuery::getRow(CTRL_YEAR, $this->field["SENKOU_NO"]);
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918", "(求人コード)");
                return false;
            }

            if (strlen($this->field["COMPANY_CD"]) > 8) {
                $this->setWarning("MSG915", "(会社コードは8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_NAME"]) > 150) {
                $this->setWarning("MSG915", "(会社名は150バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_NAMEKANA"]) > 150) {
                $this->setWarning("MSG915", "(会社名は150バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_CONTENTS"]) > 150) {
                $this->setWarning("MSG915", "(事業内容は150バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHIHONKIN"]) > 17) {
                $this->setWarning("MSG915", "(資本金は17バイトまでです。)");
                return false;
            } elseif (strlen($this->field["MAKECOMPANY_YEAR"]) > 4) {
                $this->setWarning("MSG915", "(設立年度は4バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_ZIPCD"]) > 8 || strlen($this->field["SHUSHOKU_ZIPCD"]) > 8) {
                $this->setWarning("MSG915", "(郵便番号は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_ADDR1"]) > 150 || strlen($this->field["SHUSHOKU_ADDR1"]) > 150) {
                $this->setWarning("MSG915", "(住所1は150バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_ADDR2"]) > 90 || strlen($this->field["SHUSHOKU_ADDR2"]) > 90) {
                $this->setWarning("MSG915", "(住所2は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_TELNO1"]) > 14 || strlen($this->field["SHUSHOKU_TELNO1"]) > 14) {
                $this->setWarning("MSG915", "(電話番号1は14バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_TELNO2"]) > 14 || strlen($this->field["SHUSHOKU_TELNO2"]) > 14) {
                $this->setWarning("MSG915", "(電話番号2は14バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_FAXNO"]) > 14 || strlen($this->field["SHUSHOKU_FAXNO"]) > 14) {
                $this->setWarning("MSG915", "(FAX番号は14バイトまでです。)");
                return false;
            } elseif (strlen($this->field["INDUSTRY_SCD"]) > 3) {
                $this->setWarning("MSG915", "(産業分類コードは3バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SONINZU"]) > 8) {
                $this->setWarning("MSG915", "(全体人数は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TONINZU"]) > 8) {
                $this->setWarning("MSG915", "(就業場所人数は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHUSHOKU_NAME"]) > 150) {
                $this->setWarning("MSG915", "(就業場所は150バイトまでです。)");
                return false;
            } elseif ($this->Properties["useCompany_Sort"] == "1" && strlen($this->field["COMPANY_SORT_REMARK"]) > 60) {
                $this->setWarning("MSG915", "(職種は60バイトまでです。)");
                return false;
            } elseif ($this->Properties["useCompany_Sort"] != "1" && strlen($this->field["COMPANY_SORT"]) > 2) {
                $this->setWarning("MSG915", "(職種は2バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TARGET_NINZU"]) > 2) {
                $this->setWarning("MSG915", "(募集人数は2バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TARGET_SEX"]) > 1) {
                $this->setWarning("MSG915", "(募集対象は1バイトまでです。)");
                return false;
            } elseif (strlen($this->field["JUDGING_MEANS1"]) > 2 || strlen($this->field["JUDGING_MEANS2"]) > 2 || strlen($this->field["JUDGING_MEANS3"]) > 2 || strlen($this->field["JUDGING_MEANS4"]) > 2) {
                $this->setWarning("MSG915", "(選考方法は2バイトまでです。)");
                return false;
            } elseif (strlen($this->field["PERSONNEL_MANAGER"]) > 30) {
                $this->setWarning("MSG915", "(担当者名は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["DEPARTMENT_POSITION"]) > 40) {
                $this->setWarning("MSG915", "(担当者課係は40バイトまでです。)");
                return false;
            } elseif (strlen($this->field["HOLIDAY1"]) > 1 || strlen($this->field["HOLIDAY2"]) > 1 || strlen($this->field["HOLIDAY3"]) > 1 || strlen($this->field["HOLIDAY4"]) > 1) {
                $this->setWarning("MSG915", "(休日は1バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHIFT"]) > 1) {
                $this->setWarning("MSG915", "(交代制は1バイトまでです。)");
                return false;
            } elseif (strlen($this->field["COMPANY_LODGING"]) > 1) {
                $this->setWarning("MSG915", "(宿舎は1バイトまでです。)");
                return false;
            } elseif (strlen($this->field["BASIC_SALARY"]) > 7) {
                $this->setWarning("MSG915", "(基本給は7バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TAKE_SALARY"]) > 7) {
                $this->setWarning("MSG915", "(手取額は7バイトまでです。)");
                return false;
            } elseif (strlen($this->field["APPLICATION_TARGET"]) > 60) {
                $this->setWarning("MSG915", "(募集対象は60バイトまでです。)");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->getyear   = CTRL_YEAR;
            $this->senkou_no = $this->field["SENKOU_NO"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    public function &delete()
    {
        if (!isset($this->senkou_no)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz421bQuery::getDeleteQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
            $this->getyear = "";
            $this->senkou_no = "";
        }
    }
    public function &insert()
    {
        $result = knjz421bQuery::getInsertQuery($this->field);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }

        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz421bQuery::getUpdateQuery($this->field, $this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }

        return $result;
    }
    //変数をクリアします
    public function clean()
    {
    }

    //小・細分類コードのゼロ埋め処理
    public function getZeroPaddingScd($jobtypescd, $cmd)
    {
        if (strlen($jobtypescd) == 0) {
            return '';
        }

        //頭ゼロ埋め
        if (strlen($jobtypescd) <= 2 && $cmd == 'S') {
            $jobtypescd = sprintf("%03d", $jobtypescd);
        }
        //頭ゼロ埋め
        if (strlen($jobtypescd) <= 1 && $cmd == 'SS') {
            $jobtypescd = sprintf("%02d", $jobtypescd);
        }

        return $jobtypescd;
    }
}
