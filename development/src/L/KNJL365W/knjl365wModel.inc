<?php

require_once('for_php7.php');

class knjl365wModel extends Model {
    var $data   = array();
    var $cmd;
    var $ObjYear;

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //初期化
        $contents  = "";
        $header_data = false;   //出力フラグ

        $selectdata = ($this->selectdata) ? explode(',', $this->selectdata) : array();
        foreach ($selectdata as $key => $schoolCd) {

            if ($contents) $contents .= "\n\n";

            /****************************/
            /*  タイトル部分データ取得  */
            /****************************/
            //年度（和暦）
            $nendo = common::DateConv1($this->ObjYear."/04/01",10);
            //作成日（和暦）
            $date = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
            //学校情報
            $school_info = $db->getRow(knjl365wQuery::getSchoolInfo($this, $schoolCd), DB_FETCHMODE_ASSOC);
            //課程名
            $courseName = $db->getOne(knjl365wQuery::getCourseName($this, $schoolCd));

            /**********************/
            /*  タイトル部分出力  */
            /**********************/
            //1行目
            $contents .= $nendo."　志願学区外高等学校入学志願許可者の受検・入学者の状況調べ\n";
            //2行目
            $contents .= ",,〔".$date."現在〕\n";
            //3行目
            $contents .= "学区：".$school_info["DIST_NAME"].",,";
            $contents .= "学校コード：".$schoolCd.",";
            $contents .= "学校名：".$school_info["EDBOARD_SCHOOLNAME"].",,,";
            $contents .= "課程：".$courseName.",,,";
            $query = knjl365wQuery::selectCsvQuery($this, $schoolCd);
            $gaitou = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $setGaitou = is_array($gaitou) ? "該当あり" : "該当なし";
            $contents .= "該当の有無：{$setGaitou}\n";

            /**********************/
            /*  ヘッダー（一覧）  */
            /**********************/
            $header = array();
            $header["NAME"]             = "生徒名";
            $header["SEX_NAME"]         = "性別";
            $header["GAITOUYOUSHIKI"]   = "該当様式";
            $header["FINSCHOOL_NAME"]   = "出身中学校";
            $header["PREF_NAME"]        = "都道府県名等";
            $header["SUC_MAJOR_NAME"]   = "学科・コース名";
            $header["TESTDIV_NAME"]     = "選抜の種類";
            $header["DAI2_SHIBOU"]      = "第２志望";
            $header["JUDGE"]            = "合否";
            $header["ENTDIV"]           = "入学の有無";
            $header["MOVE_DATE"]        = "転居の年月日";
            $header["MOVE_PLACE"]       = "転居地";

            /**********************/
            /*  ヘッダー部分出力  */
            /**********************/
            $contents .= implode($header, ",")."\n";

            /********************/
            /*  データ部分出力  */
            /********************/
            $boy = $girl = $total = 0;
            $pass = $fail = $un_exam = $enter = 0;
            $query = knjl365wQuery::selectCsvQuery($this, $schoolCd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                if ($row["HOSHOUNIN_TODOKE"] == "1") {
                    $row["GAITOUYOUSHIKI"] = "15";
                } else if ($row["KENGAI_CHUUGAKKOU_SHUSSHIN"] == "1") {
                    $row["GAITOUYOUSHIKI"] = "14";
                } else if ($row["NYUUGAKU_SIGAN_KYOKA"] == "1") {
                    $row["GAITOUYOUSHIKI"] = "13-2";
/*
                } else if ($row["KENGAI_HOSHOUNIN_ZAIJUU"] == "1" && $row["KENGAI_ZAIJUU"] == "1") { // 指示画面から「KENGAI_HOSHOUNIN_ZAIJUU」はカットになった
                    $row["GAITOUYOUSHIKI"] = "13-1(受保)";
                } else if ($row["KENGAI_HOSHOUNIN_ZAIJUU"] == "1") {
                    $row["GAITOUYOUSHIKI"] = "13-1(保)";
*/
                } else if ($row["KENGAI_ZAIJUU"] == "1") {
                    $row["GAITOUYOUSHIKI"] = "13-1";
                } else if ($row["TUUGAKU_KUIKIGAI_KYOKA"] == "1") {
                    $row["GAITOUYOUSHIKI"] = "12";
                }
                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                //集計
                $total++;
                if ($row["SEX"] == "1") $boy++;
                if ($row["SEX"] == "2") $girl++;
                if ($row["JUDGE"] == "○") $pass++;
                if ($row["JUDGE"] == "×") $fail++;
                if ($row["JUDGE"] == "－") $un_exam++;
                if ($row["ENTDIV"] == "○") $enter++;

                $header_data = true;
            }
            $result->free();

            /**************/
            /*  集計出力  */
            /**************/
            $sumData = array();
            $sumData["NAME"]                = array("合計","男","女");
            $sumData["SEX_NAME"]            = array($total, $boy, $girl);
            $sumData["GAITOUYOUSHIKI"]      = array();
            $sumData["FINSCHOOL_NAME"]      = array();
            $sumData["PREF_NAME"]           = array();
            $sumData["SUC_MAJOR_NAME"]      = array("合格数", "不合格数", "不受検数");
            $sumData["TESTDIV_NAME"]        = array($pass, $fail, $un_exam);
            $sumData["DAI2_SHIBOU"]         = array();
            $sumData["JUDGE"]               = array("入学者数", "", "");
            $sumData["ENTDIV"]              = array($enter, "", "");
            $sumData["CHANGE_ADDR_DATE"]    = array();
            $sumData["CHANGE_ADDR"]         = array();

            for ($i = 0; $i < 3; $i++) {
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$sumData[$field][$i];
                    $sep = ",";
                }
                $contents .= "\n";
            }
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //ファイル名
            $title = $nendo.'　志願学区外高等学校入学志願許可者の受検・入学者の状況調べ';

            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //報告取り下げ処理
    function &getCancelModel() {
        knjl365wQuery::getDeleteReportFixed($this);
        $this->setMessage("MSG201");
    }
}
?>
