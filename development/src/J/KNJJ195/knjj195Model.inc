<?php

require_once('for_php7.php');

class knjj195Model extends Model {
    var $cmd;

    function init() {
        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //支部一覧取得
        $branch_array = array();
        $query = knjj195Query::getBranchMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $branch_array[$row["VALUE"]] = $row["LABEL"];
        }

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["SCHOOL_KIND"]      = "中高";
        $header["HR_NAME"]          = "クラス";
        $header["CLASS_CNT"]        = "人数";
        foreach ($branch_array as $branch => $label) {
            $header["CNT".$branch]      = $label;
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
        //支部毎の件数取得
        $finData = array();
        $query = knjj195Query::getBranchCnt();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $finData[$row["GRADE"]][$row["HR_CLASS"]][$row["BRANCHCD"]] = $row["BRANCH_CNT"];
        }
        $result->free();

        //学年ごとの件数取得
        $gradeCnt = array();
        $query = knjj195Query::getHrclassList($this, "cntG");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gradeCnt[$row["GRADE"]] = $row["HR_CLASS_CNT"];
        }
        $result->free();

        //校種ごとの件数取得
        $schkindCnt = array();
        $query = knjj195Query::getHrclassList($this, "cntS");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schkindCnt[$row["SCHOOL_KIND"]] = $row["GHR_CNT"];
        }
        $result->free();

        //初期化
        $counterG = $counterS = 0;
        foreach ($branch_array as $branch => $label) {
            $sumGrade[$branch] = $sumSchKind[$branch] = $sumAll[$branch] = 0;;
        }

        $schoolkind = array("J" => "中学", "H" => "高校");

        //データ出力
        $query = knjj195Query::getHrclassList($this, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $counterG++;
            $counterS++;
            $dataTmp = array();
            $dataTmp["SCHOOL_KIND"] = $schoolkind[$row["SCHOOL_KIND"]];
            $dataTmp["HR_NAME"]     = $row["HR_NAME"];
            $dataTmp["CLASS_CNT"]   = $row["CLASS_CNT"];
            foreach ($branch_array as $branch => $label) {
                $cnt = $finData[$row["GRADE"]][$row["HR_CLASS"]][$branch];
                $dataTmp["CNT".$branch] = ($cnt > 0) ? $cnt : "";

                $sumGrade[$branch]      += ($cnt > 0) ? $cnt : 0;
                $sumSchKind[$branch]    += ($cnt > 0) ? $cnt : 0;
                $sumAll[$branch]        += ($cnt > 0) ? $cnt : 0;
            }

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$dataTmp[$field];
                $sep = ",";
            }
            $contents .= "\n";

            //学年ごとの小計出力
            if ($gradeCnt[$row["GRADE"]] == $counterG) {
                $dataTmp = array();
                $dataTmp["SCHOOL_KIND"] = $schoolkind[$row["SCHOOL_KIND"]];
                $dataTmp["HR_NAME"]     = $row["GRADE_NAME1"]."小計";
                $dataTmp["CLASS_CNT"]   = "";
                foreach ($branch_array as $branch => $label) {
                    $dataTmp["CNT".$branch] = $sumGrade[$branch];
                }

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$dataTmp[$field];
                    $sep = ",";
                }
                $contents .= "\n\n";
                $counterG = 0;
                foreach ($branch_array as $branch => $label) {
                    $sumGrade[$branch] = 0;
                }
            }

            //校種ごとの合計出力
            if ($schkindCnt[$row["SCHOOL_KIND"]] == $counterS) {
                $dataTmp = array();
                $dataTmp["SCHOOL_KIND"] = $schoolkind[$row["SCHOOL_KIND"]];
                $dataTmp["HR_NAME"]     = "合計";
                $dataTmp["CLASS_CNT"]   = "";
                foreach ($branch_array as $branch => $label) {
                    $dataTmp["CNT".$branch] = $sumSchKind[$branch];
                }

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$dataTmp[$field];
                    $sep = ",";
                }
                $contents .= "\n\n";
                $counterS = 0;
                foreach ($branch_array as $branch => $label) {
                    $sumSchKind[$branch] = 0;
                }
            }

            $header_data = true;
        }
        $result->free();

        //合計出力
        if ($header_data) {
            $dataTmp = array();
            $dataTmp["SCHOOL_KIND"] = "";
            $dataTmp["HR_NAME"]     = "中高合計";
            $dataTmp["CLASS_CNT"]   = "";
            foreach ($branch_array as $branch => $label) {
                $dataTmp["CNT".$branch] = $sumAll[$branch];
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
        $title = CTRL_YEAR.'年度 支部別人数';

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
