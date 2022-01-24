<?php

require_once('for_php7.php');

class knja062bQuery extends Query
{
    //Z010取得
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //最終学期取得
    public function getMaxSemester()
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //学期を取得
    public function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度 ' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA062B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //職員リストコンボ用取得
    public function getStaff($year, $staffcd, $model)
    {
        $db  = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD, ";
        $query .= "     STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' ";

        $staff_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        $ume = "" ;
        for ($umecnt = 1; $umecnt <= strlen($staff_mst["STAFFCD"]) - (int)$simo; $umecnt++) {
            $ume .= $fuseji;
        }
        if ($fuseji) {
            $staff_mst["FUSE_STAFFCD"] = $ume.substr($staff_mst["STAFFCD"], (strlen($staff_mst["STAFFCD"]) - (int)$simo), (int)$simo);
        } else {
            $staff_mst["FUSE_STAFFCD"] = $staff_mst["STAFFCD"];
        }
        Query::dbCheckIn($db);

        return "{$staff_mst['FUSE_STAFFCD']}　{$staff_mst['STAFFNAME_SHOW']}";
    }
    //ＨＲ施設リストコンボ用取得
    public function getFacility()
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT faccd, facilityname FROM v_facility_mst";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        $query .= " ORDER BY faccd";

        $result = $db->query($query);

        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["FACCD"]."  ".$row["FACILITYNAME"],
                           "value" => $row["FACCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }

    //１レコード取得
    public function getRow($model, $term, $grade, $lc_class)
    {
        $db  = Query::dbCheckOut();

        $query  = " SELECT  ";
        $query .= "     t1.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_HDAT t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     t1.year     = '".SUBSTR($term, 0, 4)."' ";
        $query .= " AND semester = '".SUBSTR($term, 5)."' ";
        $query .= " AND t1.grade    = '".$grade."' ";
        $query .= " AND lc_class = '".$lc_class."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //職員一覧
    public function selectList($model, $term)
    {
        $query  = " SELECT ";
        $query .= "     x.grade, ";
        $query .= "     x.lc_class, ";
        $query .= "     x.LC_NAME, ";
        $query .= "     x.LC_NAMEabbv, ";
        $query .= "     x.lc_class_name1, ";
        $query .= "     x.lc_class_name2, ";
        $query .= "     x.grade_name, ";
        $query .= "     x.tr_cd1, ";
        $query .= "     a.staffname_show name1, ";
        $query .= "     x.tr_cd2, ";
        $query .= "     b.staffname_show name2, ";
        $query .= "     x.tr_cd3, ";
        $query .= "     c.staffname_show name3, ";
        $query .= "     x.subtr_cd1, ";
        $query .= "     d.staffname_show name4, ";
        $query .= "     x.subtr_cd2, ";
        $query .= "     e.staffname_show name5, ";
        $query .= "     x.subtr_cd3, ";
        $query .= "     f.staffname_show name6, ";
        $query .= "     x.LC_FACCD, ";
        $query .= "     n.facilityname, ";
        $query .= "     x.classweeks, ";
        $query .= "     x.classdays ";
        $query .= " FROM ";
        $query .= " ((((((SCHREG_REGD_LC_HDAT x LEFT OUTER JOIN v_staff_mst a ON x.tr_cd1 = a.staffcd AND x.year = a.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    b ON x.tr_cd2    = b.staffcd AND x.year = b.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    c ON x.tr_cd3    = c.staffcd AND x.year = c.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    d ON x.subtr_cd1 = d.staffcd AND x.year = d.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    e ON x.subtr_cd2 = e.staffcd AND x.year = e.year)";
        $query .= " LEFT OUTER JOIN v_staff_mst    f ON x.subtr_cd3 = f.staffcd AND x.year = f.year)";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = x.YEAR AND T2.GRADE = x.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = x.YEAR AND T2.GRADE = x.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " LEFT OUTER JOIN v_facility_mst n ON x.LC_FACCD  = n.faccd   AND x.year = n.year";
        $query .= " WHERE x.year     = '".SUBSTR($term, 0, 4)."'";
        $query .= "   AND x.semester = '".SUBSTR($term, 5)."'";
        $query .= " ORDER BY x.grade, x.lc_class ";

        return $query;
    }

    //--------追加
    //学期マスタ取得
    public function getSemesterMst($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";

        return $query;
    }


    //----------

    //CSV 書き出し
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     SRH.YEAR, ";
        $query .= "     SRH.SEMESTER, ";
        $query .= "     SRH.GRADE, ";
        $query .= "     SRH.LC_CLASS, ";
        $query .= "     SRH.LC_NAME, ";
        $query .= "     SRH.LC_NAMEABBV, ";
        $query .= "     SRH.LC_CLASS_NAME1, ";
        $query .= "     SRH.LC_CLASS_NAME2, ";
        $query .= "     SRH.TR_CD1, ";
        $query .= "     MAX(T1.FROM_DATE) AS FROM_DATE_T1, ";
        $query .= "     MAX(T1.TO_DATE) AS TO_DATE_T1, ";
        $query .= "     SRH.TR_CD2, ";
        $query .= "     MAX(T2.FROM_DATE) AS FROM_DATE_T2, ";
        $query .= "     MAX(T2.TO_DATE) AS TO_DATE_T2, ";
        $query .= "     SRH.TR_CD3, ";
        $query .= "     MAX(T3.FROM_DATE) AS FROM_DATE_T3, ";
        $query .= "     MAX(T3.TO_DATE) AS TO_DATE_T3, ";
        $query .= "     SRH.SUBTR_CD1, ";
        $query .= "     MAX(S1.FROM_DATE) AS FROM_DATE_S1, ";
        $query .= "     MAX(S1.TO_DATE) AS TO_DATE_S1, ";
        $query .= "     SRH.SUBTR_CD2, ";
        $query .= "     MAX(S2.FROM_DATE) AS FROM_DATE_S2, ";
        $query .= "     MAX(S2.TO_DATE) AS TO_DATE_S2, ";
        $query .= "     SRH.SUBTR_CD3, ";
        $query .= "     MAX(S3.FROM_DATE) AS FROM_DATE_S3, ";
        $query .= "     MAX(S3.TO_DATE) AS TO_DATE_S3, ";
        $query .= "     SRH.LC_FACCD, ";
        $query .= "     SRH.CLASSWEEKS, ";
        $query .= "     SRH.CLASSDAYS, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_HDAT SRH ";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_LC_DAT T1 ON SRH.TR_CD1 = T1.STAFFCD AND SRH.YEAR = T1.YEAR AND SRH.SEMESTER = T1.SEMESTER AND SRH.GRADE = T1.GRADE AND SRH.LC_CLASS = T1.LC_CLASS AND T1.TR_DIV = '1'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_LC_DAT T2 ON SRH.TR_CD2 = T2.STAFFCD AND SRH.YEAR = T2.YEAR AND SRH.SEMESTER = T2.SEMESTER AND SRH.GRADE = T2.GRADE AND SRH.LC_CLASS = T2.LC_CLASS AND T2.TR_DIV = '2'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_LC_DAT T3 ON SRH.TR_CD3 = T3.STAFFCD AND SRH.YEAR = T3.YEAR AND SRH.SEMESTER = T3.SEMESTER AND SRH.GRADE = T3.GRADE AND SRH.LC_CLASS = T3.LC_CLASS AND T3.TR_DIV = '3'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_LC_DAT S1 ON SRH.SUBTR_CD1 = S1.STAFFCD AND SRH.YEAR = S1.YEAR AND SRH.SEMESTER = S1.SEMESTER AND SRH.GRADE = S1.GRADE AND SRH.LC_CLASS = S1.LC_CLASS AND S1.TR_DIV = '4'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_LC_DAT S2 ON SRH.SUBTR_CD2 = S2.STAFFCD AND SRH.YEAR = S2.YEAR AND SRH.SEMESTER = S2.SEMESTER AND SRH.GRADE = S2.GRADE AND SRH.LC_CLASS = S2.LC_CLASS AND S2.TR_DIV = '5'";
        $query .= " LEFT OUTER JOIN STAFF_CLASS_HIST_LC_DAT S3 ON SRH.SUBTR_CD3 = S3.STAFFCD AND SRH.YEAR = S3.YEAR AND SRH.SEMESTER = S3.SEMESTER AND SRH.GRADE = S3.GRADE AND SRH.LC_CLASS = S3.LC_CLASS AND S3.TR_DIV = '6'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = SRH.YEAR AND G1.GRADE = SRH.GRADE ";
                $query .= " AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = SRH.YEAR AND G1.GRADE = SRH.GRADE ";
            $query .= "     AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     SRH.YEAR || '-' || SRH.SEMESTER = '".$model->term."' ";
        $query .= " GROUP BY";
        $query .= "     SRH.YEAR, ";
        $query .= "     SRH.SEMESTER, ";
        $query .= "     SRH.GRADE,";
        $query .= "     SRH.LC_CLASS,";
        $query .= "     SRH.LC_NAME,";
        $query .= "     SRH.LC_NAMEABBV,";
        $query .= "     SRH.LC_CLASS_NAME1,";
        $query .= "     SRH.LC_CLASS_NAME2,";
        $query .= "     SRH.TR_CD1, ";
        $query .= "     SRH.TR_CD2, ";
        $query .= "     SRH.TR_CD3, ";
        $query .= "     SRH.SUBTR_CD1, ";
        $query .= "     SRH.SUBTR_CD2, ";
        $query .= "     SRH.SUBTR_CD3, ";
        $query .= "     SRH.LC_FACCD, ";
        $query .= "     SRH.CLASSWEEKS, ";
        $query .= "     SRH.CLASSDAYS ";
        $query .= " ORDER BY ";
        $query .= "     SRH.YEAR,SRH.SEMESTER";

        return $query;
    }

    //ここから取り込み時チェック
    //CSV取り込み時の入力チェック、職員マスタに登録されているコードなのか。
    public function checkStaffMst($staffcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffcd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、施設マスタに登録されているコードなのか。
    public function checkFaccd($faccd)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FACILITY_MST ";
        $query .= " WHERE ";
        $query .= "     FACCD = '{$faccd}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、前任者が終了後の日付なのか。




    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = 'KNJA062B';
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //ここまで


    //--- INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]       = substr($model->term, 5);
        $data["GRADE"][TEXT]          = $model->fields["GRADE"];
        $data["LC_CLASS"][TEXT]       = $model->fields["LC_CLASS"];
        $data["LC_NAME"][TEXT]        = $model->fields["LC_NAME"];
        $data["LC_NAMEABBV"][TEXT]    = $model->fields["LC_NAMEABBV"];
        $data["LC_CLASS_NAME1"][TEXT] = $model->fields["LC_CLASS_NAME1"];
        $data["LC_CLASS_NAME2"][TEXT] = $model->fields["LC_CLASS_NAME2"];
        $data["TR_CD1"][TEXT]         = $model->fields["TR_CD1"];
        $data["TR_CD2"][TEXT]         = $model->fields["TR_CD2"];
        $data["TR_CD3"][TEXT]         = $model->fields["TR_CD3"];
        $data["SUBTR_CD1"][TEXT]      = $model->fields["SUBTR_CD1"];
        $data["SUBTR_CD2"][TEXT]      = $model->fields["SUBTR_CD2"];
        $data["SUBTR_CD3"][TEXT]      = $model->fields["SUBTR_CD3"];
        $data["LC_FACCD"][TEXT]       = $model->fields["LC_FACCD"];
        $data["CLASSWEEKS"][NUMBER]   = $model->fields["CLASSWEEKS"];
        $data["CLASSDAYS"][NUMBER]    = $model->fields["CLASSDAYS"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "SYSDATE()";

        $query = Query::insertSQL($data, "SCHREG_REGD_LC_HDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //STAFF_CLASS_HIST_LC_DAT取得
    public function getStaffClassHistDat($model, $term, $grade, $lc_class)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_HIST_LC_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".SUBSTR($term, 0, 4)."' ";
        $query .= "     AND SEMESTER = '".SUBSTR($term, 5)."' ";
        $query .= "     AND GRADE    = '{$grade}' ";
        $query .= "     AND LC_CLASS = '{$lc_class}' ";

        $scHistD = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $scHistD;
    }

    //INSERT（STAFF_CLASS_HIST_LC_DAT）
    public function &getInsertQueryStaffHist($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO STAFF_CLASS_HIST_LC_DAT ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     '".$model->fields["GRADE"]."' AS GRADE, ";
        $query .= "     '".$model->fields["LC_CLASS"]."' AS LC_CLASS, ";
        $query .= "     TR_DIV, ";
        $query .= "     FROM_DATE, ";
        $query .= "     TO_DATE, ";
        $query .= "     STAFFCD, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_HIST_LC_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".substr($model->term, 0, 4)."' ";
        $query .= "     AND SEMESTER = '".substr($model->term, 5)."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= "     AND LC_CLASS = '".$model->lc_class."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //Staff用Insert-------------------------------------------------------
    public function &getInsert2Query($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->fields["YEAR"];
        $data["SEMESTER"][TEXT]     = $model->fields["SEMESTER"];
        $data["GRADE"][TEXT]        = $model->fields["GRADE"];
        $data["LC_CLASS"][TEXT]     = $model->fields["LC_CLASS"];
        $data["TR_DIV"][TEXT]       = $model->fields["TR_DIV1"];
        $data["FROM_DATE"][DATE]    = $model->fields["FROM_DATE1"];
        $data["TO_DATE"][DATE]      = $model->fields["TO_DATE1"];
        $data["STAFFCD"][TEXT]      = $model->fields["TR_CD1"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "STAFF_CLASS_HIST_LC_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //----------------------------------------------------------------------

    //--- UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]       = substr($model->term, 5);
        $data["GRADE"][TEXT]          = $model->fields["GRADE"];
        $data["LC_CLASS"][TEXT]       = $model->fields["LC_CLASS"];
        $data["LC_NAME"][TEXT]        = $model->fields["LC_NAME"];
        $data["LC_NAMEABBV"][TEXT]    = $model->fields["LC_NAMEABBV"];
        $data["LC_CLASS_NAME1"][TEXT] = $model->fields["LC_CLASS_NAME1"];
        $data["LC_CLASS_NAME2"][TEXT] = $model->fields["LC_CLASS_NAME2"];
        $data["TR_CD1"][TEXT]         = $model->fields["TR_CD1"];
        $data["TR_CD2"][TEXT]         = $model->fields["TR_CD2"];
        $data["TR_CD3"][TEXT]         = $model->fields["TR_CD3"];
        $data["SUBTR_CD1"][TEXT]      = $model->fields["SUBTR_CD1"];
        $data["SUBTR_CD2"][TEXT]      = $model->fields["SUBTR_CD2"];
        $data["SUBTR_CD3"][TEXT]      = $model->fields["SUBTR_CD3"];
        $data["LC_FACCD"][TEXT]       = $model->fields["LC_FACCD"];
        $data["CLASSWEEKS"][NUMBER]   = $model->fields["CLASSWEEKS"];
        $data["CLASSDAYS"][NUMBER]    = $model->fields["CLASSDAYS"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        $where  = " WHERE year   = '" .SUBSTR($model->term, 0, 4)."'";
        $where .= " AND semester = '".SUBSTR($model->term, 5)."'";
        $where .= " AND grade    = '".$model->grade."'";
        $where .= " AND lc_class = '".$model->lc_class."'";

        $query = Query::updateSQL($data, "SCHREG_REGD_LC_HDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //staff用UPDATE---------------------------------------------------
    public function &getUpdate2Query($model)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->fields["YEAR"];
        $data["SEMESTER"][TEXT]     = $model->fields["SEMESTER"];
        $data["GRADE"][TEXT]        = $model->fields["GRADE"];
        $data["LC_CLASS"][TEXT]     = $model->fields["LC_CLASS"];
        $data["TR_DIV"][TEXT]       = $model->fields["TR_DIV1"];
        $data["FROM_DATE"][DATE]    = $model->fields["FROM_DATE1"];
        $data["TO_DATE"][DATE]      = $model->fields["TO_DATE1"];
        $data["STAFFCD"][TEXT]      = $model->fields["TR_CD1"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$model->fields["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$model->fields["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$model->fields["GRADE"]."'";
        $where .= "   AND LC_CLASS  = '".$model->fields["LC_CLASS"]."'";
        $where .= "   AND TR_DIV    = '".$model->fields["TR_DIV1"]."'";
        $where .= "   AND FROM_DATE = '".str_replace("/", "-", $model->fields["FROM_DATE1"])."'";

        $query = Query::updateSQL($data, "STAFF_CLASS_HIST_LC_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //A070sから必要そうなもの--------------------------------------------

    //--- UPDATE SCHREG_REGD_LC_HDAT全て
    public function &getRegdHdatUpdAll($model)
    {
        $data["TR_CD1"][TEXT]       = null;
        $data["TR_CD2"][TEXT]       = null;
        $data["TR_CD3"][TEXT]       = null;
        $data["SUBTR_CD1"][TEXT]    = null;
        $data["SUBTR_CD2"][TEXT]    = null;
        $data["SUBTR_CD3"][TEXT]    = null;

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "       YEAR      = '".$model->fields["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$model->fields["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$model->fields["GRADE"]."'";
        $where .= "   AND LC_CLASS  = '".$model->fields["LC_CLASS"]."'";

        $query = Query::updateSQL($data, "SCHREG_REGD_LC_HDAT", $where);

        return $query;
    }

    //--- UPDATE SCHREG_REGD_LC_HDAT
    public function &getRegdHdatUpd($row)
    {
        $field = $row["TR_DIV1"] < 4 ? "TR_CD".$row["TR_DIV1"] : "SUBTR_CD".($row["TR_DIV1"] - 3);

        $data[$field][TEXT]         = $row["STAFFCD"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$row["YEAR"]."'";
        $where .= "   AND SEMESTER  = '".$row["SEMESTER"]."'";
        $where .= "   AND GRADE     = '".$row["GRADE"]."'";
        $where .= "   AND LC_CLASS  = '".$row["LC_CLASS"]."'";

        $query = Query::updateSQL($data, "SCHREG_REGD_LC_HDAT", $where);

        return $query;
    }

    //ここまで------------------------------------------

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        //生徒が在籍しているかチェック
        $query  = "SELECT COUNT(*) FROM SCHREG_REGD_LC_DAT t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE t1.YEAR  = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND t1.grade = '".$model->grade."'";
        $query .= "   AND lc_class = '".$model->lc_class."'";
        if ($db->getOne($query) > 0) {
            return "schreg_regd";
        }
        $query  = "DELETE FROM SCHREG_REGD_LC_HDAT t1 ";
        $query .= " WHERE t1.year  = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND t1.grade = '".$model->grade."'";
        $query .= "   AND lc_class = '".$model->lc_class."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        $db->query($query);
        Query::dbCheckIn($db);
        return "succeeded";
    }

    //DELETE（STAFF_CLASS_HIST_LC_DAT）
    public function &getDeleteQuery2($model)
    {
        $db = Query::dbCheckOut();
        $query  = " DELETE FROM STAFF_CLASS_HIST_LC_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "     AND SEMESTER = '".SUBSTR($model->term, 5)."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= "     AND LC_CLASS = '".$model->lc_class."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //「コピーボタン」押し時に,学籍在籍ヘッダデータが作られているかチェック
    public function count($model, $term)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_LC_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE T1.year ='".SUBSTR($term, 0, 4)."' AND semester='".SUBSTR($term, 5, 1)."'";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //「コピーボタン」押し時に,クラス編成データに生徒が割り振られているかチェック
    public function count2($model, $term)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_REGD_LC_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE T1.YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."'";

        $count = $db->getOne($query);
        return $count;
        Query::dbCheckIn($db);
    }

    //学籍ヘッダーデータの設定学期のデータを削除
    public function deleteHdat($model, $term)
    {
        $query  ="DELETE FROM SCHREG_REGD_LC_HDAT t1 ";
        $query .=" WHERE t1.year     ='".SUBSTR($term, 0, 4)."'";
        $query .="   AND semester ='".SUBSTR($term, 5, 1)."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }

        return $query;
    }
    public function deleteStaffClassHistLcDat($model, $term)
    {
        $query  ="DELETE FROM STAFF_CLASS_HIST_LC_DAT t1 ";
        $query .=" WHERE t1.year     ='".SUBSTR($term, 0, 4)."'";
        $query .="   AND semester ='".SUBSTR($term, 5, 1)."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT T2 WHERE T2.YEAR = t1.YEAR AND T2.GRADE = t1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }

        return $query;
    }

    //学籍在籍ヘッダデータの参照学期のデータを設定学期にインサート
    public function insertHdat($model, $term2, $term)
    {
        $query  =" INSERT INTO SCHREG_REGD_LC_HDAT ";
        $query .=" SELECT ";
        $query .="      '".SUBSTR($term, 0, 4)."', ";
        $query .="      '".SUBSTR($term, 5, 1)."', ";
        $query .="      t1.grade, ";
        $query .="      lc_class, ";
        $query .="      LC_NAME, ";
        $query .="      LC_NAMEabbv, ";
        $query .="      grade_name, ";
        $query .="      lc_class_name1, ";
        $query .="      lc_class_name2, ";
        $query .="      LC_FACCD,";
        $query .="      tr_cd1, ";
        $query .="      tr_cd2, ";
        $query .="      tr_cd3,";
        $query .="      subtr_cd1, ";
        $query .="      subtr_cd2, ";
        $query .="      subtr_cd3, ";
        $query .="      classweeks, ";
        $query .="      classdays, ";
        $query .="      '".STAFFCD."', ";
        $query .="      SYSDATE()";
        $query .="   FROM SCHREG_REGD_LC_HDAT t1";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .="  WHERE t1.year = '".SUBSTR($term2, 0, 4)."' AND semester = '".SUBSTR($term2, 5, 1)."'";

        return $query;
    }

    public function insertStaffClassHistLcDat($model, $term2, $term)
    {
        $term_year = SUBSTR($term, 0, 4);
        $term_year_add = (int)$term_year + 1;
        $term2_year = SUBSTR($term2, 0, 4);

        $query  = " INSERT INTO STAFF_CLASS_HIST_LC_DAT ";
        $query .= " SELECT ";
        $query .="      '".SUBSTR($term, 0, 4)."', ";
        $query .="      '".SUBSTR($term, 5, 1)."', ";
        $query .="      T1.GRADE, ";
        $query .="      T1.LC_CLASS, ";
        $query .="      T1.TR_DIV, ";
        if ($term_year == $term2_year) {
            $query .="      T1.FROM_DATE, ";
            $query .="      T1.TO_DATE, ";
        } else {
            $query .="      '".$term_year."-04-01' AS FROM_DATE, ";
            $query .="      '".$term_year_add."-03-31' AS TO_DATE, ";
        }
        $query .="      T1.STAFFCD, ";
        $query .="      '".STAFFCD."', ";
        $query .="      SYSDATE()";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_HIST_LC_DAT T1 ";
        $query .= " INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 YEAR, ";
        $query .= "                 SEMESTER, ";
        $query .= "                 GRADE, ";
        $query .= "                 LC_CLASS, ";
        $query .= "                 TR_DIV, ";
        $query .= "                 MAX(FROM_DATE) AS FROM_DATE ";
        $query .= "             FROM ";
        $query .= "                 STAFF_CLASS_HIST_LC_DAT ";
        $query .= "             GROUP BY ";
        $query .= "                 YEAR, ";
        $query .= "                 SEMESTER, ";
        $query .= "                 GRADE, ";
        $query .= "                 LC_CLASS, ";
        $query .= "                 TR_DIV ";
        $query .= "             ) L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                  AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                  AND L1.GRADE = T1.GRADE ";
        $query .= "                  AND L1.LC_CLASS = T1.LC_CLASS ";
        $query .= "                  AND L1.TR_DIV = T1.TR_DIV ";
        $query .= "                  AND L1.FROM_DATE = T1.FROM_DATE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".SUBSTR($term2, 0, 4)."' AND ";
        $query .= "     T1.SEMESTER = '".SUBSTR($term2, 5, 1)."' ";

        return $query;
    }

    /*クラス編成データの設定学期に参照学期のデータを追加。
      (設定学期の出席番号は参照学期の出席番号でインサート)*/
    public function insertCfd($model, $term2, $term, $grd_div)
    {
        $query  = " INSERT INTO SCHREG_REGD_LC_DAT ";
        $query .= "     (SCHREGNO,YEAR,SEMESTER,GRADE,LC_CLASS,ATTENDNO,ANNUAL,COURSECD,MAJORCD,COURSECODE,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO,";
        $query .= "     '".substr($term, 0, 4)."', ";
        $query .= "     '".substr($term, 5, 1)."', ";
        $query .= "     T1.GRADE, ";
        $query .= "     LC_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     COURSECD, ";       //課程
        $query .= "     MAJORCD, ";        //学科
        $query .= "     COURSECODE, ";     //コース
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_DAT T1";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".substr($term2, 0, 4)."' AND ";
        $query .= "     SEMESTER = '".substr($term2, 5, 1)."' ";
        if ($grd_div == "") {
            $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_BASE_MST WHERE GRD_DIV IS NULL) ";
        }

        return $query;
    }

    //「コピーボタン」押し時の処理
    public function getInsertCopyQuery($model, $term2, $term, $check, $grd_div)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knja062bQuery::deleteHdat($model, $term));
        $db->query(knja062bQuery::insertHdat($model, $term2, $term));

        $db->query(knja062bQuery::deleteStaffClassHistLcDat($model, $term));
        $db->query(knja062bQuery::insertStaffClassHistLcDat($model, $term2, $term));

        if ($check == 1) {
            $db->query(knja062bQuery::insertCfd($model, $term2, $term, $grd_div));
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["YEAR"][TEXT]           = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]       = $data_arr[$i]["SEMESTER"];
            $data["GRADE"][TEXT]          = $data_arr[$i]["GRADE"];
            $data["LC_CLASS"][TEXT]       = $data_arr[$i]["LC_CLASS"];
            $data["LC_NAME"][TEXT]        = $data_arr[$i]["LC_NAME"];
            $data["LC_NAMEABBV"][TEXT]    = $data_arr[$i]["LC_NAMEABBV"];
            $data["LC_CLASS_NAME1"][TEXT] = $data_arr[$i]["LC_CLASS_NAME1"];
            $data["LC_CLASS_NAME2"][TEXT] = $data_arr[$i]["LC_CLASS_NAME2"];
            $data["TR_CD1"][TEXT]         = $data_arr[$i]["TR_CD1"];
            $data["TR_CD2"][TEXT]         = $data_arr[$i]["TR_CD2"];
            $data["TR_CD3"][TEXT]         = $data_arr[$i]["TR_CD3"];
            $data["SUBTR_CD1"][TEXT]      = $data_arr[$i]["SUBTR_CD1"];
            $data["SUBTR_CD2"][TEXT]      = $data_arr[$i]["SUBTR_CD2"];
            $data["SUBTR_CD3"][TEXT]      = $data_arr[$i]["SUBTR_CD3"];
            $data["LC_FACCD"][TEXT]       = $data_arr[$i]["LC_FACCD"];
            $data["CLASSWEEKS"][NUMBER]   = $data_arr[$i]["CLASSWEEKS"];
            $data["CLASSDAYS"][NUMBER]    = $data_arr[$i]["CLASSDAYS"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";

            //データが一件もなければ、insertする
            if (1 > $db->getOne("SELECT COUNT(*) FROM SCHREG_REGD_LC_HDAT WHERE YEAR = '{$data_arr[$i]["YEAR"]}' AND SEMESTER = '{$data_arr[$i]["SEMESTER"]}' AND GRADE = '{$data_arr[$i]["GRADE"]}' AND LC_CLASS = '{$data_arr[$i]["LC_CLASS"]}'")) {
                $query = Query::insertSQL($data, "SCHREG_REGD_LC_HDAT");
            } else {
                $where  = "WHERE ";
                $where .= "     YEAR = '{$data_arr[$i]["YEAR"]}' AND SEMESTER = '{$data_arr[$i]["SEMESTER"]}' AND GRADE = '{$data_arr[$i]["GRADE"]}' AND LC_CLASS = '{$data_arr[$i]["LC_CLASS"]}' ";

                $query = Query::updateSQL($data, "SCHREG_REGD_LC_HDAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        return $cnt;
    }

    //CSVファイルよりDBへインサート(STAFF_CLASS_HIST_LC_DAT)
    public function insert2QueryCsv($db, $model, $data_arr)
    {
        $data = array();
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($staffcnt = 1; $staffcnt <= 6; $staffcnt++) {
                $soeji     = $staffcnt < 4 ? $staffcnt : $staffcnt - 3;
                $dateSoeji = $staffcnt < 4 ? "_T" : "_S";
                $trSoeji   = $staffcnt < 4 ? "" : "SUB";
                if (strlen($data_arr[$i]["FROM_DATE".$dateSoeji.$soeji]) > 0) {
                    //データセット
                    $data["YEAR"][TEXT]           = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]       = $data_arr[$i]["SEMESTER"];
                    $data["GRADE"][TEXT]          = $data_arr[$i]["GRADE"];
                    $data["LC_CLASS"][TEXT]       = $data_arr[$i]["LC_CLASS"];
                    $data["TR_DIV"][TEXT]         = $data_arr[$i]["TR_DIV".$staffcnt];
                    $data["FROM_DATE"][TEXT]      = $data_arr[$i]["FROM_DATE".$dateSoeji.$soeji];
                    $data["TO_DATE"][TEXT]        = $data_arr[$i]["TO_DATE".$dateSoeji.$soeji];
                    $data["STAFFCD"][TEXT]        = $data_arr[$i][$trSoeji."TR_CD".$soeji];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][NUMBER]      = "SYSDATE()";

                    //データが一件もなければ、insertする
                    $query  = " SELECT";
                    $query .= "   COUNT(*)";
                    $query .= " FROM";
                    $query .= "   STAFF_CLASS_HIST_LC_DAT";
                    $query .= " WHERE YEAR      = '".$data_arr[$i]["YEAR"]."'";
                    $query .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."'";
                    $query .= "   AND GRADE     = '".$data_arr[$i]["GRADE"]."'";
                    $query .= "   AND LC_CLASS  = '".$data_arr[$i]["LC_CLASS"]."'";
                    $query .= "   AND TR_DIV    = '".$data_arr[$i]["TR_DIV".$staffcnt]."'";
                    $query .= "   AND FROM_DATE = '".str_replace("/", "-", $data_arr[$i]["FROM_DATE".$dateSoeji.$soeji])."'";
                    if (1 > $db->getOne($query)) {
                        $query = Query::insertSQL($data, "STAFF_CLASS_HIST_LC_DAT");
                    } else {
                        $where  = " WHERE YEAR      = '".$data_arr[$i]["YEAR"]."'";
                        $where .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."'";
                        $where .= "   AND GRADE     = '".$data_arr[$i]["GRADE"]."'";
                        $where .= "   AND LC_CLASS  = '".$data_arr[$i]["LC_CLASS"]."'";
                        $where .= "   AND TR_DIV    = '".$data_arr[$i]["TR_DIV".$staffcnt]."'";
                        $where .= "   AND FROM_DATE = '".str_replace("/", "-", $data_arr[$i]["FROM_DATE".$dateSoeji.$soeji])."'";
                        $query = Query::updateSQL($data, "STAFF_CLASS_HIST_LC_DAT", $where);
                    }

                    $db->query($query);

                    //TO_DATEを、updateするSQL
                    $query  = " SELECT";
                    $query .= "   *";
                    $query .= " FROM";
                    $query .= "   STAFF_CLASS_HIST_LC_DAT";
                    $query .= " WHERE YEAR      = '".$data_arr[$i]["YEAR"]."'";
                    $query .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."'";
                    $query .= "   AND GRADE     = '".$data_arr[$i]["GRADE"]."'";
                    $query .= "   AND LC_CLASS  = '".$data_arr[$i]["LC_CLASS"]."'";
                    $query .= "   AND TR_DIV    = '".$data_arr[$i]["TR_DIV".$staffcnt]."'";
                    $query .= " ORDER BY";
                    $query .= "   FROM_DATE DESC";

                    //TO_DATEを、updateする
                    $setDate =  ((int)$data_arr[$i]["YEAR"] + 1)."-03-31";
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($row["TO_DATE"] <= $setDate) {
                        } else {
                            //データセット
                            $data = array();
                            $data["TO_DATE"][TEXT]        = $setDate;
                            $data["REGISTERCD"][TEXT]     = STAFFCD;
                            $data["UPDATED"][NUMBER]      = "SYSDATE()";
                            $where  = " WHERE YEAR      = '".$row["YEAR"]."'";
                            $where .= "   AND SEMESTER  = '".$row["SEMESTER"]."'";
                            $where .= "   AND GRADE     = '".$row["GRADE"]."'";
                            $where .= "   AND LC_CLASS  = '".$row["LC_CLASS"]."'";
                            $where .= "   AND TR_DIV    = '".$row["TR_DIV"]."'";
                            $where .= "   AND FROM_DATE = '".$row["FROM_DATE"]."'";
                            $query = Query::updateSQL($data, "STAFF_CLASS_HIST_LC_DAT", $where);
                            $db->query($query);
                        }

                        $setDate = date("Y-m-d", strtotime("-1 day", strtotime($row["FROM_DATE"])));
                    }
                    $result->free();
                }
            }
        }
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }
}
