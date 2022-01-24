<?php

require_once('for_php7.php');

class knjl362wModel extends Model {
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
        $this->header_data = false;   //出力フラグ

        $selectdata = ($this->selectdata) ? explode(',', $this->selectdata) : array();
        foreach ($selectdata as $key => $schoolCd) {

            if ($contents) $contents .= "\n\n";

            /****************************/
            /*  タイトル部分データ取得  */
            /****************************/
            //年度（和暦）
            $nendo = common::DateConv1($this->ObjYear."/04/01",10);
            //学校情報
            $school_info = $db->getRow(knjl362wQuery::getSchoolInfo($this, $schoolCd), DB_FETCHMODE_ASSOC);
            //課程名
            $courseName = $db->getOne(knjl362wQuery::getCourseName($this, $schoolCd));

            /**********************/
            /*  タイトル部分出力  */
            /**********************/
            //1行目
            $contents .= $nendo."三重県立高等学校入学志願者学力検査結果集計表\n";

            /********************/
            /*  データ部分出力  */
            /********************/

            if ($this->field["CSVDIV"] == "1") {
                $headData = array(",学科・コース名"           => "CMNAME",
                                  ",学力検査等による受検者数" => "DAI1_CM_INT",
                                  ",学力検査等による合格者数" => "SUC_CM_INT",
                                  "全教科,受検者総得点"       => "DAI1_TOTAL_3",
                                  "全教科,総平均点"           => "DAI1_AVG_3",
                                  "全教科,合格者総得点"       => "SUC_TOTAL_3",
                                  "全教科,合格者平均点"       => "SUC_AVG_3",
                                  "国語,受検者総得点"         => "JAP_D1_TOTAL",
                                  "国語,総平均点"             => "JAP_D1_AVG",
                                  "国語,合格者総得点"         => "JAP_SUC_TOTAL",
                                  "国語,合格者平均点"         => "JAP_SUC_AVG",
                                  "数学,受検者総得点"         => "MATH_D1_TOTAL",
                                  "数学,総平均点"             => "MATH_D1_AVG",
                                  "数学,合格者総得点"         => "MATH_SUC_TOTAL",
                                  "数学,合格者平均点"         => "MATH_SUC_AVG",
                                  "英語,受検者総得点"         => "ENG_D1_TOTAL",
                                  "英語,総平均点"             => "ENG_D1_AVG",
                                  "英語,合格者総得点"         => "ENG_SUC_TOTAL",
                                  "英語,合格者平均点"         => "ENG_SUC_AVG"
                                  );
            } else {
                $headData = array(",学科・コース名"           => "CMNAME",
                                  ",学力検査等による受検者数" => "DAI1_CM_INT",
                                  ",学力検査等による合格者数" => "SUC_CM_INT",
                                  "５教科,受検者総得点"       => "DAI1_TOTAL_3",
                                  "５教科,総平均点"           => "DAI1_AVG_3",
                                  "５教科,合格者総得点"       => "SUC_TOTAL_3",
                                  "５教科,合格者平均点"       => "SUC_AVG_3",
                                  "国語,受検者総得点"         => "JAP_D1_TOTAL",
                                  "国語,総平均点"             => "JAP_D1_AVG",
                                  "国語,合格者総得点"         => "JAP_SUC_TOTAL",
                                  "国語,合格者平均点"         => "JAP_SUC_AVG",
                                  "数学,受検者総得点"         => "MATH_D1_TOTAL",
                                  "数学,総平均点"             => "MATH_D1_AVG",
                                  "数学,合格者総得点"         => "MATH_SUC_TOTAL",
                                  "数学,合格者平均点"         => "MATH_SUC_AVG",
                                  "社会,受検者総得点"         => "SOC_D1_TOTAL",
                                  "社会,総平均点"             => "SOC_D1_AVG",
                                  "社会,合格者総得点"         => "SOC_SUC_TOTAL",
                                  "社会,合格者平均点"         => "SOC_SUC_AVG",
                                  "英語,受検者総得点"         => "ENG_D1_TOTAL",
                                  "英語,総平均点"             => "ENG_D1_AVG",
                                  "英語,合格者総得点"         => "ENG_SUC_TOTAL",
                                  "英語,合格者平均点"         => "ENG_SUC_AVG",
                                  "理科,受検者総得点"         => "SCI_D1_TOTAL",
                                  "理科,総平均点"             => "SCI_D1_AVG",
                                  "理科,合格者総得点"         => "SCI_SUC_TOTAL",
                                  "理科,合格者平均点"         => "SCI_SUC_AVG",
                                  "実技,受検者総得点"         => "PRA_D1_TOTAL",
                                  "実技,総平均点"             => "PRA_D1_AVG",
                                  "実技,合格者総得点"         => "PRA_SUC_TOTAL",
                                  "実技,合格者平均点"         => "PRA_SUC_AVG"
                                  );
            }

            if ($this->field["CSVDIV"] == "1") {
                $testDiv = "1','2','3','4";
                $contents .= $this->getCsvData($db, $schoolCd, $headData, $school_info, $courseName, $testDiv, "前期選抜", "");
                $contents .= $this->getCsvData($db, $schoolCd, $headData, $school_info, $courseName, $testDiv, "前期選抜", "1");
            } else {
                $testDiv = "5";
                $contents .= $this->getCsvData($db, $schoolCd, $headData, $school_info, $courseName, $testDiv, "後期選抜", "");
                $contents .= $this->getCsvData($db, $schoolCd, $headData, $school_info, $courseName, $testDiv, "後期選抜", "1");
                $testDiv = "6";
                $contents .= $this->getCsvData($db, $schoolCd, $headData, $school_info, $courseName, $testDiv, "再募集", "");
            }

        }

        //ファイル名
        $title = $nendo.'　三重県立高等学校入学志願者学力検査結果集計表';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($this->header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //CSVデータセット
    function getCsvData($db, $schoolCd, $headData, $school_info, $courseName, $testDiv, $divName, $testDiv2) {

        $keepData = $keepCmCd = array();
        $query = knjl362wQuery::selectCsvQuery($this, $schoolCd, $testDiv, $testDiv2);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            foreach ($headData as $name => $engName) {
                if (strstr($engName, 'AVG')) {
                    $row["{$engName}"] = ltrim(sprintf("%5.1F", $row["{$engName}"]));
                }
            }
            $keepData["{$row["CMCD"]}"] = $row;
            $this->header_data = true;
        }
        $result->free();

        //2行目
        $testDiv2Name = $testDiv2 == "1" ? "(追検査)" : "";
        $retContents = "\n,,".$divName.$testDiv2Name."\n";
        //3行目
        $retContents .= "学区：".$school_info["DIST_NAME"].",,";
        $retContents .= "学校コード：".$schoolCd.",";
        $retContents .= "学校名：".$school_info["EDBOARD_SCHOOLNAME"].",,,";
        $retContents .= "課程：".$courseName."\n";

        foreach ($headData as $name => $engName) {
            $retContents .= $name;
            foreach ($keepData as $cmCD => $arrData) {
                foreach ($arrData as $komoku => $val) {
                    if ($engName == $komoku) {
                        $retContents .= ",".$val;
                    }
                }
            }
            $retContents .= "\n";
        }

        return $retContents;
    }

    //報告取り下げ処理
    function &getCancelModel() {
        knjl362wQuery::getDeleteReportFixed($this);
        $this->setMessage("MSG201");
    }
}
?>
