<?php

require_once('for_php7.php');
class knjx232query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX232' ";
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

    //年度一覧取得
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN  ";
        $query .= "     SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN  ";
        $query .= "     SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "		T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //科目一覧取得
    public function getSubclassList($model)
    {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= " LEFT JOIN  ";
        $query .= "     SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= "                    AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "                    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧取得
    public function getChairList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD AS VALUE, ";
        $query .= "     CHAIRCD || ' ' || CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教員一覧取得
    public function getStaffList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.STAFFCD AS VALUE, ";
        $query .= "     T2.STAFFCD || ' ' || T3.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2 ";
        $query .= " LEFT JOIN  ";
        $query .= "     STAFF_MST T3 ON T2.STAFFCD = T3.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = T2.YEAR || T2.SEMESTER AND ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $attenddate = str_replace("/", "-", $data_arr[$i]["ATTENDDATE"]);

            //データセット
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["ATTENDDATE"][TEXT]       = $attenddate;
            $data["PERIODCD"][TEXT]         = $data_arr[$i]["PERIODCD"];
            $data["CHAIRCD"][TEXT]          = $db->getOne(knjx232Query::getChaircd($model, $attenddate, $data_arr[$i]["PERIODCD"], $data_arr[$i]["SCHREGNO"]));
            $data["DI_CD"][TEXT]            = $data_arr[$i]["DI_CD"];
            $data["DI_REMARK_CD"][TEXT]     = $data_arr[$i]["DI_REMARK_CD"];
            $data["DI_REMARK"][TEXT]        = $data_arr[$i]["DI_REMARK"];
            $data["YEAR"][TEXT]             = substr($model->field["YEAR"], 0, 4);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM ATTEND_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND ATTENDDATE = '".$attenddate."' AND PERIODCD = '".$data_arr[$i]["PERIODCD"]."'")) {
                $query = Query::insertSQL($data, "ATTEND_DAT");
            } else {
                $where  = " WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "   AND ATTENDDATE    = '".$attenddate."' ";
                $where .= "   AND PERIODCD      = '".$data_arr[$i]["PERIODCD"]."' ";

                $query = Query::updateSQL($data, "ATTEND_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $attenddate = str_replace("/", "-", $data_arr[$i]["ATTENDDATE"]);

            $query  = " DELETE FROM ATTEND_DAT ";
            $query .= "	WHERE SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "	  AND ATTENDDATE    = '".$attenddate."' ";
            $query .= "	  AND PERIODCD      = '".$data_arr[$i]["PERIODCD"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //講座取得
    public function getChaircd($model, $attenddate, $periodcd, $schregno)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T2.EXECUTEDATE = '".$attenddate."' AND ";
        $query .= "     T2.PERIODCD = '".$periodcd."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "     '".$attenddate."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";

        return $query;
    }

    //名称マスタチェック
    public function checkNameMst($model, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = SUBSTR('".$model->field["YEAR"]."', 1, 4) AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //勤怠コードチェック
    public function checkAttendDiCdDat($model, $di_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = SUBSTR('".$model->field["YEAR"]."', 1, 4) AND ";
        $query .= "     DI_CD   = '".$di_cd."' ";

        return $query;
    }

    //時間割データチェック
    public function checkSchChrDat($model, $schregno, $attenddate, $periodcd)
    {
        $attenddate = str_replace("/", "-", $attenddate);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     '".$attenddate."' BETWEEN T1.APPDATE AND T1.APPENDDATE AND ";
        $query .= "     T2.EXECUTEDATE = '".$attenddate."' AND ";
        $query .= "     T2.PERIODCD = '".$periodcd."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         S1.NAME, ";
        $query .= "         S2.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN  ";
        $query .= "         SCHREG_BASE_MST S1  ON S1.SCHREGNO  = T1.SCHREGNO ";
        $query .= "     LEFT JOIN  ";
        $query .= "         SCHREG_REGD_HDAT S2 ON S2.YEAR      = T1.YEAR ";
        $query .= "                            AND S2.SEMESTER  = T1.SEMESTER ";
        $query .= "                            AND S2.GRADE     = T1.GRADE ";
        $query .= "                            AND S2.HR_CLASS  = T1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= " AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($model->field["STUDENT"] != "") {
            $query .= " AND T1.SCHREGNO = '".$model->field["STUDENT"]."' ";
        }

        $query .= " ), DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SDATE, ";
        $query .= "         EDATE ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR || SEMESTER = '".$model->field["YEAR"]."' ";

        $query .= " ), CHR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCH_INFO T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";

        $query .= " ), CHR_STF AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STF_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["STAFFCD"] != "") {
            $query .= " AND STAFFCD = '".$model->field["STAFFCD"]."' ";
        }

        $query .= " ), SUBCLASS AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "         T1.SUBCLASSCD, ";
        }
        $query .= "         S1.SUBCLASSNAME, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CHAIRNAME, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SUBCLASS_MST S1 ON T1.SUBCLASSCD = S1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T1.CLASSCD       = S1.CLASSCD ";
            $query .= "                        AND T1.SCHOOL_KIND   = S1.SCHOOL_KIND ";
            $query .= "                        AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "        ,CHR_STD T2, ";
        $query .= "         CHR_STF T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "         T1.CHAIRCD = T3.CHAIRCD ";
        if ($model->field["SUBCLASSCD"] != "") {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= " AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        if ($model->field["CHAIRCD"] != "") {
            $query .= " AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= " ), SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T1.ATTENDDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.DI_CD, ";
        $query .= "         T1.DI_REMARK_CD, ";
        $query .= "         T1.DI_REMARK, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T3.GRADE, ";
        $query .= "         T3.HR_CLASS, ";
        $query .= "         T3.ATTENDNO, ";
        $query .= "         T3.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT T1, ";
        $query .= "         SCH_CHR_DAT T2, ";
        $query .= "         SCH_INFO T3, ";
        $query .= "         DATE T4 ";
        $query .= "     WHERE ";
        $query .= "         T1.ATTENDDATE = T2.EXECUTEDATE AND ";
        $query .= "         T1.PERIODCD = T2.PERIODCD AND ";
        $query .= "         T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "         T1.ATTENDDATE BETWEEN T4.SDATE AND T4.EDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T2.CHAIRNAME, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.DI_CD, ";
        $query .= "     T1.DI_REMARK_CD, ";
        $query .= "     T1.DI_REMARK, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHEDULE T1, ";
        $query .= "     SUBCLASS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.ATTENDDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
