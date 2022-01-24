<?php

require_once('for_php7.php');
class knjx_j040Query extends Query
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

    //年度・学期一覧
    public function getYearSemester($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     INNER JOIN SEMESTER_MST T2 ";
        $query .= "          ON T1.YEAR     = T2.YEAR ";
        $query .= "         AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部クラブ一覧
    public function getClubMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CLUBCD AS VALUE, ";
        $query .= "     CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " WHERE ";
            $query .= "  SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "  SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "  SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック -- SCHREG_REGD_DAT
    public function checkSchregRegdDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

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
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }

    //存在チェック -- NAME_MST
    public function checkNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //データの有無チェック
    public function checkSchregClubHistDat($model, $schregno, $clubcd, $sdate)
    {
        $sdate = ($sdate) ? date("Y-m-d", strtotime("{$sdate}")) : "";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     CLUBCD      = '".$clubcd."' AND ";
        $query .= "     SDATE       = '".$sdate."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
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
            $sdate = ($data_arr[$i]["SDATE"]) ? date("Y-m-d", strtotime("{$data_arr[$i]["SDATE"]}")) : "";
            $edate = ($data_arr[$i]["EDATE"]) ? date("Y-m-d", strtotime("{$data_arr[$i]["EDATE"]}")) : "";

            //データ件数取得
            $data_cnt = $db->getOne(knjx_j040Query::checkSchregClubHistDat($model, $data_arr[$i]["SCHREGNO"], $data_arr[$i]["CLUBCD"], $sdate));

            $data = array();
            if ($data_cnt < 1) {
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["CLUBCD"][TEXT]           = $data_arr[$i]["CLUBCD"];
                $data["SDATE"][TEXT]            = $sdate;
            }
            $data["EDATE"][TEXT]            = $edate;
            $data["EXECUTIVECD"][TEXT]      = $data_arr[$i]["EXECUTIVECD"];
            $data["REMARK"][TEXT]           = $data_arr[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT]  = $model->schKind;
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
                $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            }

            if ($data_cnt < 1) {
                //追加
                $query = Query::insertSQL($data, "SCHREG_CLUB_HIST_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                $where .= "     CLUBCD      = '".$data_arr[$i]["CLUBCD"]."' AND ";
                $where .= "     SDATE       = '".$sdate."' ";
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $where .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
                    $where .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                    $where .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
                    $where .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $where .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
                    $where .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
                //更新
                $query = Query::updateSQL($data, "SCHREG_CLUB_HIST_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶ取込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $sdate = ($data_arr[$i]["SDATE"]) ? date("Y-m-d", strtotime("{$data_arr[$i]["SDATE"]}")) : "";

            $query  = " DELETE FROM ";
            $query .= "     SCHREG_CLUB_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            $query .= "     CLUBCD      = '".$data_arr[$i]["CLUBCD"]."' AND ";
            $query .= "     SDATE       = '".$sdate."' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     L1.NAME_KANA, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     M1.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T1.EXECUTIVECD, ";
        $query .= "     N1.NAME1 AS EXECUTIVENAME, ";
        $query .= "     T1.REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST M1 ON M1.CLUBCD = T1.CLUBCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND M1.SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND M1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND M1.SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND M1.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND M1.SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND M1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'J001' AND N1.NAMECD2 = T1.EXECUTIVECD, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "     T2.YEAR || T2.SEMESTER = '".$model->field["YEAR"]."' AND ";
        if ($model->field["CLUBCD"] != "ALL") {
            $query .= "     T1.CLUBCD = '".$model->field["CLUBCD"]."' AND ";
        }
        $query .= "     '".CTRL_DATE."' BETWEEN T1.SDATE AND (CASE WHEN T1.EDATE IS NULL THEN '9999-12-31' ELSE T1.EDATE END) ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."'";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."'";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."'";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SDATE ";

        return $query;
    }
}
