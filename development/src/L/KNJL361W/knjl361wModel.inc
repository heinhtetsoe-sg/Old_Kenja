<?php

require_once('for_php7.php');

class knjl361wModel extends Model {
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
            //学校情報
            $school_info = $db->getRow(knjl361wQuery::getSchoolInfo($this, $schoolCd), DB_FETCHMODE_ASSOC);
            //課程名
            $courseName = $db->getOne(knjl361wQuery::getCourseName($this, $schoolCd));

            /**********************/
            /*  タイトル部分出力  */
            /**********************/
            //1行目
            $contents .= $nendo."　三重県立高等学校入学者調べ\n";
            //2行目
            $contents .= "学区：".$school_info["DIST_NAME"].",,";
            $contents .= "学校コード：".$schoolCd.",";
            $contents .= "学校名：".$school_info["EDBOARD_SCHOOLNAME"].",,,";
            $contents .= "課程：".$courseName."\n";

            /**************/
            /*  ヘッダー  */
            /**************/
            $header = array();
            $header2 = array();
            $header3 = array();

            $header[] = "学科・コース名";
            $header[] = "";

            $header2[] = "市町名";
            $header2[] = "中学校名";

            $query = knjl361wQuery::getCouseMajor($this, $schoolCd);
            $result = $db->query($query);
            $this->cmCd = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->cmCd[] = $row["CM_CD"];
                $header[] = $row["CM_NAME"];
                $header[] = "";
                $header2[] = "入学者数";
                $header2[] = "（内）過年度";
            }

            $header[] = "合計";

            $header2[] = "総入学者数";
            $header2[] = "（内）過年度";

            $header3[] = "県外";

            /**********************/
            /*  ヘッダー部分出力  */
            /**********************/
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header2, ",")."\n";

            /********************/
            /*  データ部分出力  */
            /********************/
            $chekFlg = "DUMMY";
            $query = knjl361wQuery::selectCsvQuery($this, $schoolCd);//三重県内
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($chekFlg != $row["CITYNAME"]) {
                    $chekFlg = $row["CITYNAME"];
                    $row["CITYNAME"] = $chekFlg;
                } else {
                    $row["CITYNAME"] = "";
                }

                $contents .= implode($row, ",") ."\n";

                $header_data = true;
            }
            $result->free();

            $query = knjl361wQuery::selectCsvQuery2($this, $schoolCd);//三重県外
            $result = $db->query($query);
            $contents .= implode($header3, ",");
            $comma = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= ",";
                $contents .= implode($row, ",") ."\n";

                $header_data = true;

            }
            $result->free();

            $query = knjl361wQuery::selectCsvQuery3($this, $schoolCd);//合計
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $contents .= ",";
                $contents .= implode($row, ",") ."\n";
            }
            $result->free();
        }

        //ファイル名
        $title = $nendo.'　三重県立高等学校入学者調べ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
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
        knjl361wQuery::getDeleteReportFixed($this);
        $this->setMessage("MSG201");
    }
}
?>
