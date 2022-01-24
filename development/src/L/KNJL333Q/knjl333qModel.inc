<?php

require_once('for_php7.php');
class knjl333qModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl333qModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TAISYO"           =>  VARS::post("TAISYO"),           //出力対象
                             "ORDER"            =>  VARS::post("ORDER"),            //出力順

                             "ALL"              =>  VARS::post("ALL"),              //全員
                             "GOKAKU"           =>  VARS::post("GOKAKU"),           //合格
                             "NAIGOKAKU"        =>  VARS::post("NAIGOKAKU"),        //県内合格
                             "GAIGOKAKU"        =>  VARS::post("GAIGOKAKU"),        //県外合格
                             "FUGOKAKU"         =>  VARS::post("FUGOKAKU"),         //不合格
                             "KESSEKI"          =>  VARS::post("KESSEKI"),          //欠席
                             "SKSAIYO"          =>  VARS::post("SKSAIYO"),          //スカラー採用
                             "SKFUSAIYO"        =>  VARS::post("SKFUSAIYO"),        //スカラー不採用
                             "SKKIBO"           =>  VARS::post("SKKIBO"),           //スカラー希望者
                             "NYURYO"           =>  VARS::post("NYURYO"),           //入寮希望者
                             "CHUGAKUATE"       =>  VARS::post("CHUGAKUATE")        //中学校宛通知を送る中学校一覧
                     );

        $this->cmd = VARS::request("cmd");

        $this->PRINT_ALL = "1";
        $this->PRINT_PASS = "2";
        $this->PRINT_NAIPASS = "3";
        $this->PRINT_GAIPASS = "4";
        $this->PRINT_UNPASS = "5";
        $this->PRINT_KESSEKI = "6";
        $this->PRINT_SCHOLAR = "7";
        $this->PRINT_UNSCHOLAR = "8";
        $this->PRINT_SCHOLAR_HOPE = "9";
        $this->PRINT_NYUURYOU = "10";
        $this->PRINT_SCHOOL = "11";
        $this->PRINT_HASSOU_SCHOOL = "12";
        $this->PRINT_HASSOU_PASS = "13";

        $this->SUISEN = "2";
        $this->IPPAN = "3";

        $this->COURSE_FUTSU = "11";
        $this->COURSE_SPORT = "12";

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getDownloadModel()
    {
        $contents = ""; //CSVデータ出力用

        if ("1" == $this->field["ALL"]) {
            $contents .= $this->printSagyouList($this->PRINT_ALL);
        }
        if ("1" == $this->field["GOKAKU"]) {
            $contents .= $this->printSagyouList($this->PRINT_PASS);
        }
        if ("1" == $this->field["NAIGOKAKU"]) {
            $contents .= $this->printSagyouList($this->PRINT_NAIPASS);
        }
        if ("1" == $this->field["GAIGOKAKU"]) {
            $contents .= $this->printSagyouList($this->PRINT_GAIPASS);
        }
        if ("1" == $this->field["FUGOKAKU"]) {
            $contents .= $this->printSagyouList($this->PRINT_UNPASS);
        }
        if ("1" == $this->field["KESSEKI"]) {
            $contents .= $this->printSagyouList($this->PRINT_KESSEKI);
        }
        if ("1" == $this->field["SKSAIYO"]) {
            $contents .= $this->printSagyouList($this->PRINT_SCHOLAR);
        }
        if ("1" == $this->field["SKFUSAIYO"]) {
            $contents .= $this->printSagyouList($this->PRINT_UNSCHOLAR);
        }
        if ("1" == $this->field["SKKIBO"]) {
            $contents .= $this->printSagyouList($this->PRINT_SCHOLAR_HOPE);
        }
        if ("1" == $this->field["NYURYO"]) {
            $contents .= $this->printSagyouList($this->PRINT_NYUURYOU);
        }
        if ("1" == $this->field["CHUGAKUATE"]) {
            $contents .= $this->printSchool($this->PRINT_SCHOOL);
        }
        if ("1" == $this->field["ORDER"]) {
            $contents .= $this->printHassouSchool($this->PRINT_HASSOU_SCHOOL);
        }
        if ("2" == $this->field["ORDER"]) {
            $contents .= $this->printHassouPass($this->PRINT_HASSOU_PASS);
        }

        if (strlen($contents)) {
            if ("1" == $this->field["ORDER"] || "2" == $this->field["ORDER"]) {
                $couse = $this->ObjYear."年度　入学試験　書類発送用";
            } else {
                $couse = $this->ObjYear."年度　入試発送作業用リスト";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function printSagyouList($printDiv)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //入試区分略称取得
        $query = knjl333qQuery::getNameMst($this->ObjYear, "L045", $this->field["TESTDIV"]);
        $rowL045 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //件数取得
        $query = knjl333qQuery::sqlSagyouList($this, $printDiv, "CNT");
        $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印刷日
        $login_date = explode("-", str_replace("/", "-", CTRL_DATE));
        $print_date = $login_date[0] . "年" . $login_date[1] . "月" . $login_date[2] . "日";

        //ヘッダー1行目
        $header1 = $rowL045["ABBV1"] . "《" . $this->getSubTitle($printDiv) . "》" . "　件数：" . $cntRow["CNT"] . "件" . "　" . $print_date;

        //ヘッダーセット
        $header = array();
        $header["NO"]                       = "No";
        $header["EXAMNO"]                   = "受験番号";
        $header["TESTDIV_ABBV1"]            = "区分";
        $header["NAME"]                     = "氏名";
        $header["FINSCHOOL_NAME"]           = "中学校名";
        $header["JUDGEMENT_NAME1"]          = "合否";
        $header["SCHOLAR_KIBOU"]            = "スカラ希望";
        $header["SCHOLAR_SAIYOU_TOUKYU"]    = "スカラ採用"; //SCHOLAR_SAIYOU . " " . SCHOLAR_TOUKYU
        $header["DORMITORY_KIBOU"]          = "入寮";
        $header[$this->lastColumn]          = $this->lastColumn;

        //ヘッダーセット
        $contents = ""; //CSVデータ出力用
        $contents .= $header1."\n";
        $contents .= implode($header, ",")."\n";

        $lineNo = 1;
        $hasData = false;
        $query = knjl333qQuery::sqlSagyouList($this, $printDiv, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);

            $row["NO"] = $lineNo;
            $row["SCHOLAR_SAIYOU_TOUKYU"] = $row["SCHOLAR_SAIYOU"] . " " . $row["SCHOLAR_TOUKYU"];
            $row[$this->lastColumn] = $this->lastColumn;
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $lineNo++;
            $hasData = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($hasData) {
            return $contents;
        } else {
            return "";
        }
    }

    public function getSubTitle($printDiv)
    {
        if ($this->PRINT_ALL == $printDiv) {
            return "全員";
        }
        if ($this->PRINT_PASS == $printDiv) {
            return "合格";
        }
        if ($this->PRINT_NAIPASS == $printDiv) {
            return "県内合格";
        }
        if ($this->PRINT_GAIPASS == $printDiv) {
            return "県外合格";
        }
        if ($this->PRINT_UNPASS == $printDiv) {
            return "不合格";
        }
        if ($this->PRINT_KESSEKI == $printDiv) {
            return "欠席";
        }
        if ($this->PRINT_SCHOLAR == $printDiv) {
            return "スカラー採用";
        }
        if ($this->PRINT_UNSCHOLAR == $printDiv) {
            return "スカラー不採用";
        }
        if ($this->PRINT_SCHOLAR_HOPE == $printDiv) {
            return "スカラー希望者";
        }
        if ($this->PRINT_NYUURYOU == $printDiv) {
            return "入寮希望者";
        }
        if ($this->PRINT_SCHOOL == $printDiv) {
            return "中学校宛通知を送る中学校一覧";
        }

        if ($this->PRINT_HASSOU_SCHOOL == $printDiv) {
            return "中学校順";
        }
        if ($this->PRINT_HASSOU_PASS == $printDiv) {
            return "合否結果順";
        }

        return "";
    }

    public function printSchool($printDiv)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //入試区分L045取得
        $query = knjl333qQuery::getNameMst($this->ObjYear, "L045", $this->field["TESTDIV"]);
        $rowL045 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //件数取得
        $query = knjl333qQuery::sqlSchool($this, $printDiv, "CNT");
        $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印刷日
        $login_date = explode("-", str_replace("/", "-", CTRL_DATE));
        $print_date = $login_date[0] . "年" . $login_date[1] . "月" . $login_date[2] . "日";

        //ヘッダー1行目
        $header1 = $rowL045["ABBV1"] . "《" . $this->getSubTitle($printDiv) . "》" . "　件数：" . $cntRow["CNT"] . "件" . "　" . $print_date;

        //入試区分L004取得
        $rowL004 = array();
        $query = knjl333qQuery::getNameMst($this->ObjYear, "L004");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowL004[$row["VALUE"]] = $row["NAME1"];
        }

        //ヘッダーセット
        $header = array();
        $header["NO"]                       = "No";
        $header["FS_CD"]                    = "中学校コード";
        $header["FINSCHOOL_NAME"]           = "中学校名";
        if ($this->SUISEN == $this->field["TESTDIV"]) {
            $header["KOUTYOU_CNT"]              = $rowL004["3"]."志願者数";
            $header["JIKO_CNT"]                 = $rowL004["4"]."志願者数";
        } else {
            $header["IPPAN_CNT"]                = $rowL004["5"]."志願者数";
        }
        $header[$this->lastColumn]              = $this->lastColumn;

        //ヘッダーセット
        $contents = ""; //CSVデータ出力用
        $contents .= $header1."\n";
        $contents .= implode($header, ",")."\n";

        $lineNo = 1;
        $hasData = false;
        $query = knjl333qQuery::sqlSchool($this, $printDiv, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);

            $row["NO"] = $lineNo;
            $row[$this->lastColumn] = $this->lastColumn;
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $lineNo++;
            $hasData = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($hasData) {
            return $contents;
        } else {
            return "";
        }
    }

    public function printHassouSchool($printDiv)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //入試区分略称取得
        $query = knjl333qQuery::getNameMst($this->ObjYear, "L045", $this->field["TESTDIV"]);
        $rowL045 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //件数取得
        $query = knjl333qQuery::sqlHassouSchool($this, $printDiv, "CNT");
        $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印刷日
        $login_date = explode("-", str_replace("/", "-", CTRL_DATE));
        $print_date = $login_date[0] . "年" . $login_date[1] . "月" . $login_date[2] . "日";

        //ヘッダー1行目
        $header1 = $rowL045["ABBV1"] . "《" . $this->getSubTitle($printDiv) . "》" . "　件数：" . $cntRow["CNT"] . "件" . "　" . $print_date . "版";

        //ヘッダーセット
        $header = array();
        $header["NO"]                       = "No";
        $header["FINSCHOOL_NAME"]           = "中学校名";
        $header["EXAMNO"]                   = "受験番号";
        $header["TESTDIV_ABBV1"]            = "区分";
        $header["EXAMCOURSE_ABBV"]          = "コース";
        $header["NAME"]                     = "氏名";
        $header["SEX_ABBV1"]                = "性別";
        $header["JUDGEMENT_NAME1"]          = "合否";
        $header["SCHOLAR_KIBOU"]            = "スカラ希望";
        $header["SCHOLAR_SAIYOU_TOUKYU"]    = "スカラ採用"; //SCHOLAR_SAIYOU . " " . SCHOLAR_TOUKYU
        $header["DORMITORY_KIBOU"]          = "寮希望";
        $header["PREF_NAME"]                = "県名";
        $header[$this->lastColumn]          = $this->lastColumn;

        //ヘッダーセット
        $contents = ""; //CSVデータ出力用
        $contents .= $header1."\n";
        $contents .= implode($header, ",")."\n";

        $befSchool = "";
        $lineNo = 1;
        $hasData = false;
        $query = knjl333qQuery::sqlHassouSchool($this, $printDiv, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);

            if ($befSchool != $row["FS_CD"]) {
                $lineNo = 1;
            }

            $row["NO"] = $lineNo;
            $row["SCHOLAR_SAIYOU_TOUKYU"] = $row["SCHOLAR_SAIYOU"] . " " . $row["SCHOLAR_TOUKYU"];
            $row[$this->lastColumn] = $this->lastColumn;
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $befSchool = $row["FS_CD"];
            $lineNo++;
            $hasData = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($hasData) {
            return $contents;
        } else {
            return "";
        }
    }

    public function printHassouPass($printDiv)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //入試区分略称取得
        $query = knjl333qQuery::getNameMst($this->ObjYear, "L045", $this->field["TESTDIV"]);
        $rowL045 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //件数取得
        $query = knjl333qQuery::sqlHassouSchool($this, $printDiv, "CNT");
        $cntRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印刷日
        $login_date = explode("-", str_replace("/", "-", CTRL_DATE));
        $print_date = $login_date[0] . "年" . $login_date[1] . "月" . $login_date[2] . "日";

        //ヘッダー1行目
        $header1 = $rowL045["ABBV1"] . "《" . $this->getSubTitle($printDiv) . "》" . "　件数：" . $cntRow["CNT"] . "件" . "　" . $print_date . "版";

        //ヘッダーセット
        $header = array();
        $header["NO"]                       = "No";
        $header["JUDGEMENT_NAME1"]          = "合否";
        $header["EXAMNO"]                   = "受験番号";
        $header["NAME"]                     = "氏名";
        $header["SEX_ABBV1"]                = "性別";
        $header["TESTDIV_ABBV1"]            = "区分";
        $header["SCHOLAR_KIBOU"]            = "スカラ希望";
        $header["SCHOLAR_SAIYOU_TOUKYU"]    = "スカラ採用"; //SCHOLAR_SAIYOU . " " . SCHOLAR_TOUKYU
        $header["DORMITORY_KIBOU"]          = "寮希望";
        $header["EXAMCOURSE_ABBV"]          = "コース";
        $header["PREF_NAME"]                = "県名";
        $header["FINSCHOOL_NAME"]           = "中学校名";
        $header[$this->lastColumn]          = $this->lastColumn;

        //ヘッダーセット
        $contents = ""; //CSVデータ出力用
        $contents .= $header1."\n";
        $contents .= implode($header, ",")."\n";

        $lineNo = 1;
        $hasData = false;
        $query = knjl333qQuery::sqlHassouSchool($this, $printDiv, "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ","があればコロン"."に置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            $row = str_replace("\r\n", "", $row);

            $row["NO"] = $lineNo;
            $row["SCHOLAR_SAIYOU_TOUKYU"] = $row["SCHOLAR_SAIYOU"] . " " . $row["SCHOLAR_TOUKYU"];
            $row[$this->lastColumn] = $this->lastColumn;
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $lineNo++;
            $hasData = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($hasData) {
            return $contents;
        } else {
            return "";
        }
    }
}
