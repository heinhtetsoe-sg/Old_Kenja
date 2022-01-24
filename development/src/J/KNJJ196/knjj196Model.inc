<?php

require_once('for_php7.php');

class knjj196Model extends Model {
    var $field = array();
    var $cmd;

    function init() {

        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //出身学校一覧取得
        $finsch_array = array();
        $query = knjj196Query::getFinschoolList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $finsch_array[$row["VALUE"]] = $row["LABEL"];
        }

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["SCHOOL_KIND"]      = "中高";
        $header["HR_NAME"]          = "クラス";
        $header["CLASS_CNT"]        = "人数";
        foreach ($finsch_array as $finsch => $label) {
            $header["CNT".$finsch]      = $label;
        }

        /******************/
        /*  ヘッダー出力  */
        /******************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        /****************/
        /*  データ出力  */
        /****************/
        //出身学校毎の件数取得
        $finData = array();
        $query = knjj196Query::getFinschoolCnt($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $finData[$row["GRADE"]][$row["HR_CLASS"]][$row["FINSCHOOLCD"]] = $row["FINSCH_CNT"];
        }
        $result->free();

        //学年ごとのクラス件数取得
        $gradeCnt = array();
        $query = knjj196Query::getHrclassList($this, "cnt");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gradeCnt[$row["GRADE"]] = $row["HR_CLASS_CNT"];
        }
        $result->free();

        //初期化
        $counter = 0;
        foreach ($finsch_array as $finsch => $label) {
            $sumGrade[$finsch] = $sumAll[$finsch] = 0;;
        }

        $schoolkind = ($this->field["SCHOOL_KIND"] == "J") ? "中学" : "高校";

        //データ出力
        $query = knjj196Query::getHrclassList($this, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $counter++;
            $dataTmp = array();
            $dataTmp["SCHOOL_KIND"] = $schoolkind;
            $dataTmp["HR_NAME"]     = $row["HR_NAME"];
            $dataTmp["CLASS_CNT"]   = $row["CLASS_CNT"];
            foreach ($finsch_array as $finsch => $label) {
                $cnt = $finData[$row["GRADE"]][$row["HR_CLASS"]][$finsch];
                $dataTmp["CNT".$finsch] = ($cnt > 0) ? $cnt : "";

                $sumGrade[$finsch] += ($cnt > 0) ? $cnt : 0;
                $sumAll[$finsch] += ($cnt > 0) ? $cnt : 0;
            }

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$dataTmp[$field];
                $sep = ",";
            }
            $contents .= "\n";

            //学年ごとの小計出力
            if ($gradeCnt[$row["GRADE"]] == $counter) {
                $dataTmp = array();
                $dataTmp["SCHOOL_KIND"] = $schoolkind;
                $dataTmp["HR_NAME"]     = $row["GRADE_NAME1"]."小計";
                $dataTmp["CLASS_CNT"]   = "";
                foreach ($finsch_array as $finsch => $label) {
                    $dataTmp["CNT".$finsch] = $sumGrade[$finsch];
                }

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$dataTmp[$field];
                    $sep = ",";
                }
                $contents .= "\n\n";
                $counter = 0;
                foreach ($finsch_array as $finsch => $label) {
                    $sumGrade[$finsch] = 0;
                }
            }
            $header_data = true;
        }
        $result->free();

        //合計出力
        if ($header_data) {
            $dataTmp = array();
            $dataTmp["SCHOOL_KIND"] = $schoolkind;
            $dataTmp["HR_NAME"]     = "合計";
            $dataTmp["CLASS_CNT"]   = "";
            foreach ($finsch_array as $finsch => $label) {
                $dataTmp["CNT".$finsch] = $sumAll[$finsch];
            }

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$dataTmp[$field];
                $sep = ",";
            }
            $contents .= "\n";
        }

        //ファイル名
        $title = CTRL_YEAR.'年度 出身学校別人数';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
