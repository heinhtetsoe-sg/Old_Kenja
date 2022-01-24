<?php

require_once('for_php7.php');

class knjx_j120aQuery extends Query
{
    //高セキュリティー
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID   = '{$model->getPrgId}' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //存在チェック -- SCHREG_REGD_DAT, SCHREG_CLUB_HIST_DAT
    public function checkRegdHistData($model, $schregno, $clubcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_CLUB_HIST_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T2.CLUBCD   = '".$clubcd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= "                 ) ";
        }

        return $query;
    }

    //存在チェック -- CLUB_MST
    public function checkClubMst($model, $clubcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        $query .= " WHERE ";
        $query .= "     CLUBCD = '".$clubcd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //存在チェック -- CLUB_HOST_MST
    public function checkClubHostMst($model, $hostcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        $query .= " WHERE ";
        $query .= "     HOSTCD = '".$hostcd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //存在チェック -- CLUB_ITEM_MST
    public function checkClubItemMst($model, $itemcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //存在チェック -- CLUB_ITEM_KIND_MST
    public function checkClubItemKindMst($model, $itemcd, $kindcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' AND ";
        $query .= "     KINDCD = '".$kindcd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //存在チェック -- CLUB_RECORD_MST
    public function checkClubRecordMst($model, $recordcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_RECORD_MST ";
        $query .= " WHERE ";
        $query .= "     RECORDCD = '".$recordcd."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //更新（ＣＳＶ取込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //日付変換
            $detail_date = ($data_arr[$i]["DETAIL_DATE"]) ? date("Y-m-d", strtotime("{$data_arr[$i]["DETAIL_DATE"]}")) : "";

            $data = array();
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
            }
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["CLUBCD"][TEXT]           = $data_arr[$i]["CLUBCD"];
            $data["DETAIL_DATE"][TEXT]      = $detail_date;
            $data["DETAIL_SEQ"][NUMBER]     = $cnt+1;
            $data["MEET_NAME"][TEXT]        = $data_arr[$i]["MEET_NAME"];
            $data["DIV"][TEXT]              = $data_arr[$i]["DIV"];
            $data["GROUPCD"][TEXT]          = ($data_arr[$i]["DIV"] == "2") ? $data_arr[$i]["GROUPCD"] : "";
            $data["HOSTCD"][TEXT]           = $data_arr[$i]["HOSTCD"];
            $data["ITEMCD"][TEXT]           = $data_arr[$i]["ITEMCD"];
            $data["KINDCD"][TEXT]           = $data_arr[$i]["KINDCD"];
            $data["RECORDCD"][TEXT]         = $data_arr[$i]["RECORDCD"];
            $data["DOCUMENT"][TEXT]         = $data_arr[$i]["DOCUMENT"];
            $data["DETAIL_REMARK"][TEXT]    = $data_arr[$i]["DETAIL_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_CSV_DAT");
            $db->query($query);
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ダミーデータ取得
    public function getSchregClubHdetailCsvDat($model)
    {
        $query  = " WITH MIN_GROUP_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLUBCD, ";
        $query .= "         DETAIL_DATE, ";
        $query .= "         DIV, ";
        $query .= "         GROUPCD, ";
        $query .= "         MAX(DETAIL_SEQ) AS DETAIL_SEQ ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HDETAIL_CSV_DAT ";
        $query .= "     WHERE ";
        $query .= "         DIV = '2' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         CLUBCD, ";
        $query .= "         DETAIL_DATE, ";
        $query .= "         DIV, ";
        $query .= "         GROUPCD ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T1.DETAIL_DATE, ";
        $query .= "         CASE WHEN T1.DIV = '2' THEN L1.DETAIL_SEQ ELSE T1.DETAIL_SEQ END AS DETAIL_SEQ, ";
        $query .= "         T1.DETAIL_SEQ AS CSV_ROWNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HDETAIL_CSV_DAT T1 ";
        $query .= "         LEFT JOIN MIN_GROUP_SEQ L1 ";
        $query .= "                  ON T1.CLUBCD       = L1.CLUBCD ";
        $query .= "                 AND T1.DETAIL_DATE  = L1.DETAIL_DATE ";
        $query .= "                 AND T1.DIV          = L1.DIV ";
        $query .= "                 AND T1.GROUPCD      = L1.GROUPCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     L1.MEET_NAME, ";
        $query .= "     L1.DIV, ";
        $query .= "     L1.GROUPCD, ";
        $query .= "     L1.HOSTCD, ";
        $query .= "     L1.ITEMCD, ";
        $query .= "     L1.KINDCD, ";
        $query .= "     L1.RECORDCD, ";
        $query .= "     L1.DOCUMENT, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN SCHREG_CLUB_HDETAIL_CSV_DAT L1 ";
        $query .= "              ON T1.CLUBCD       = L1.CLUBCD ";
        $query .= "             AND T1.DETAIL_DATE  = L1.DETAIL_DATE ";
        $query .= "             AND T1.DETAIL_SEQ   = L1.DETAIL_SEQ ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND L1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "         AND L1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     T1.CSV_ROWNO ";

        return $query;
    }

    //SEQ取得
    public function getSeq($model, $row)
    {
        $query  = " SELECT ";
        $query .= "     MAX(DETAIL_SEQ) AS DETAIL_SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$row["SCHREGNO"]."' AND ";
        $query .= "     CLUBCD      = '".$row["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$row["DETAIL_DATE"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //GROUPCD取得
    public function getGroupcd($model, $row)
    {
        $query  = " SELECT ";
        $query .= "     MAX(GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     GROUP_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     CLUBCD      = '".$row["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$row["DETAIL_DATE"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //更新（ＣＳＶ取込）
    public function updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjx_j120aQuery::getSchregClubHdetailCsvDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //SEQ取得
            $seq = $db->getOne(knjx_j120aQuery::getSeq($model, $row));
            $seq = ($seq) ? $seq+1 : 1;

            //個人
            if ($row["DIV"] == "1") {
                //追加 -- SCHREG_CLUB_HDETAIL_DAT
                $data = array();
                if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
                }
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["CLUBCD"][TEXT]           = $row["CLUBCD"];
                $data["DETAIL_DATE"][TEXT]      = $row["DETAIL_DATE"];
                $data["DETAIL_SEQ"][NUMBER]     = $seq;
                $data["MEET_NAME"][TEXT]        = $row["MEET_NAME"];
                $data["DIV"][TEXT]              = "1";
                $data["GROUPCD"][TEXT]          = "";
                $data["HOSTCD"][TEXT]           = $row["HOSTCD"];
                $data["ITEMCD"][TEXT]           = $row["ITEMCD"];
                $data["KINDCD"][TEXT]           = $row["KINDCD"];
                $data["RECORDCD"][TEXT]         = $row["RECORDCD"];
                $data["DOCUMENT"][TEXT]         = $row["DOCUMENT"];
                $data["DETAIL_REMARK"][TEXT]    = $row["DETAIL_REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_DAT");
                $db->query($query);
            }

            //団体
            if ($row["DIV"] == "2") {
                if ($detail_seq != $row["DETAIL_SEQ"]) {
                    //GROUPCD取得
                    $groupcd = $db->getOne(knjx_j120aQuery::getGroupcd($model, $row));
                    $groupcd = ($groupcd) ? $groupcd+1 : 1;
                }

                //追加 -- SCHREG_CLUB_HDETAIL_DAT
                $data = array();
                if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
                }
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["CLUBCD"][TEXT]           = $row["CLUBCD"];
                $data["DETAIL_DATE"][TEXT]      = $row["DETAIL_DATE"];
                $data["DETAIL_SEQ"][NUMBER]     = $seq;
                $data["DIV"][TEXT]              = "2";
                $data["GROUPCD"][TEXT]          = $groupcd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_DAT");
                $db->query($query);

                if ($detail_seq != $row["DETAIL_SEQ"]) {
                    //追加 -- GROUP_CLUB_HDETAIL_DAT
                    $data = array();
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                        $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
                    }
                    $data["CLUBCD"][TEXT]           = $row["CLUBCD"];
                    $data["DETAIL_DATE"][TEXT]      = $row["DETAIL_DATE"];
                    $data["GROUPCD"][TEXT]          = $groupcd;
                    $data["MEET_NAME"][TEXT]        = $row["MEET_NAME"];
                    $data["HOSTCD"][TEXT]           = $row["HOSTCD"];
                    $data["ITEMCD"][TEXT]           = $row["ITEMCD"];
                    $data["KINDCD"][TEXT]           = $row["KINDCD"];
                    $data["RECORDCD"][TEXT]         = $row["RECORDCD"];
                    $data["DOCUMENT"][TEXT]         = $row["DOCUMENT"];
                    $data["DETAIL_REMARK"][TEXT]    = $row["DETAIL_REMARK"];
                    $data["REGISTERCD"][TEXT]       = STAFFCD;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    $query = Query::insertSQL($data, "GROUP_CLUB_HDETAIL_DAT");
                    $db->query($query);
                }
                $detail_seq = $row["DETAIL_SEQ"];
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //ダミーデータの削除
    public function deleteQueryDummy()
    {
        $query  = " DELETE FROM SCHREG_CLUB_HDETAIL_CSV_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //ヘッダ例用部クラブデータ取得
    public function getClubMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CLUBCD ";

        return $query;
    }

    //ヘッダ例用データ取得
    public function getSchregRegdhdat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }
}
