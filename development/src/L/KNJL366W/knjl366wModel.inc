<?php

require_once('for_php7.php');

class knjl366wModel extends Model {
    var $data   = array();
    var $field = array();
    var $cmd;
    var $ObjYear;

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->ObjYear);
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み

        $this->field = array("CSVDIV"      =>  VARS::post("CSVDIV")     //CSV種類
                            );

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
            //別紙様式
            $divName = ($this->field["CSVDIV"] == "1") ? "別紙様式９－１" :"別紙様式９－２";
            //学校情報
            $school_info = $db->getRow(knjl366wQuery::getSchoolInfo($this, $schoolCd), DB_FETCHMODE_ASSOC);
            //課程名
            $courseName = $db->getOne(knjl366wQuery::getCourseName($this, $schoolCd));

            /**********************/
            /*  タイトル部分出力  */
            /**********************/
            //1行目
            $contents .= $divName."　".$nendo."　スポーツ特別枠選抜の状況調べ\n";
            //2行目
            $contents .= "学区：".$school_info["DIST_NAME"].",,";
            $contents .= "学校コード：".$schoolCd.",";
            $contents .= "学校名：".$school_info["EDBOARD_SCHOOLNAME"]."\n";

            /**********************/
            /*  ヘッダー（一覧）  */
            /**********************/
            $header = array();
            $header["BOSHUU_SPORT"]         = "募集競技";
            $header["BOSHUU_MAJOR"]         = "募集学科";
            $header["SIGAN_CNT"]            = "志願者数";
            $header["GOUKAKU_CNT"]          = "合格内定者数";
            $header["ZENKI_GOUKAKU_CNT"]    = "前期合格内定者数";
            $header["FUGOUKAKU_CNT"]        = "不合格者数";

            /**********************/
            /*  ヘッダー部分出力  */
            /**********************/
            $contents .= implode($header, ",")."\n";

            /********************/
            /*  データ部分出力  */
            /********************/
            //出力しない項目
            $blankArray = ($this->field["CSVDIV"] == "2") ? array() : array("GOUKAKU_CNT","ZENKI_GOUKAKU_CNT","FUGOUKAKU_CNT");
            //集計する項目
            $calcArray = array("SIGAN_CNT","GOUKAKU_CNT","ZENKI_GOUKAKU_CNT","FUGOUKAKU_CNT");
            //初期化
            $calcData = array();
            foreach ($calcArray as $field) {
                $calcData[$field]["BOY"]     = 0;
                $calcData[$field]["GIRL"]    = 0;
                $calcData[$field]["TOTAL"]   = 0;
            }
            //SQL
            $query = knjl366wQuery::selectCsvQuery($this, $schoolCd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    if (in_array($field, $blankArray)) {
                        $contents .= $sep;
                    } else {
                        $contents .= $sep.$row[$field];
                    }
                    $sep = ",";
                }
                $contents .= "\n";

                //集計
                foreach ($calcArray as $field) {
                    $calcData[$field]["BOY"]     += $row[$field."1"];
                    $calcData[$field]["GIRL"]    += $row[$field."2"];
                    $calcData[$field]["TOTAL"]   += $row[$field];
                }

                $header_data = true;
            }
            $result->free();

            /**************/
            /*  集計出力  */
            /**************/
            $sumData = array();
            $sumData["BOSHUU_SPORT"]    = array();
            $sumData["BOSHUU_MAJOR"]    = array("男子", "女子", "合計");
            foreach ($calcArray as $field) {
                $sumData[$field] = array($calcData[$field]["BOY"], $calcData[$field]["GIRL"], $calcData[$field]["TOTAL"]);
            }

            for ($i = 0; $i < 3; $i++) {
                $sep = "";
                foreach ($header as $field => $label) {
                    if (in_array($field, $blankArray)) {
                        $contents .= $sep;
                    } else {
                        $contents .= $sep.$sumData[$field][$i];
                    }
                    $sep = ",";
                }
                $contents .= "\n";
            }
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //ファイル名
            $title = $divName."　".$nendo."　スポーツ特別枠選抜の状況調べ";

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
        knjl366wQuery::getDeleteReportFixed($this);
        $this->setMessage("MSG201");
    }
}
?>
