<?php

require_once('for_php7.php');

class knje373aquery extends Query
{
    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    public function getDataDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H508' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    public function getProName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.PROFICIENCYCD AS VALUE, ";
        $query .= "     T1.PROFICIENCYCD || ':' || T2.PROFICIENCYNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     PROFICIENCY_YMST T1";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT GRADES";
        $query .= " ON ";
        $query .= "     GRADES.YEAR     = T1.YEAR ";
        $query .= " AND GRADES.GRADE    = T1.GRADE ";
        $query .= " AND GRADES.SCHOOL_KIND = 'H' ";
        $query .= " AND GRADES.GRADE_CD = '03' "; //卒業年次
        $query .= " LEFT JOIN PROFICIENCY_MST T2 ON T1.PROFICIENCYDIV = T2.PROFICIENCYDIV";
        $query .= "                             AND T1.PROFICIENCYCD  = T2.PROFICIENCYCD";
        $query .= " WHERE ";
        $query .= "     T1.YEAR           = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER       = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     REGD.YEAR, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     NMZ002.ABBV1 AS SEX, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     AFT.*, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_INFO_DAT AFT ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "              ON REGD.YEAR      = AFT.YEAR ";
        $query .= "             AND REGD.SCHREGNO  = AFT.SCHREGNO ";
        $query .= "     INNER JOIN SCH_INFO SCH ";
        $query .= "              ON SCH.YEAR      = REGD.YEAR ";
        $query .= "             AND SCH.SCHREGNO  = REGD.SCHREGNO ";
        $query .= "             AND SCH.SEMESTER  = REGD.SEMESTER ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "             ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST NMZ002 ";
        $query .= "             ON NMZ002.NAMECD1 = 'Z002' ";
        $query .= "            AND NMZ002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "             ON FIN.FINSCHOOLCD = BASE.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         AFT.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //削除（ＣＳＶ取込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($idx = 0; $idx < get_count($data_arr); $idx++) {
            $query  = " DELETE FROM ";
            $query .= "     AFT_RECOMMENDATION_INFO_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".$data_arr[$idx]["YEAR"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$idx]["SCHREGNO"]."' ";

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($idx = 0; $idx < get_count($data_arr); $idx++) {
            //存在チェック
            $dataCnt = $db->getOne(knje373aQuery::getExistsAftRecmDatSql($data_arr[$idx]["YEAR"], $data_arr[$idx]["SCHREGNO"]));

            //データセット
            $data = array();
            $data["SCORE_JUDGE"][TEXT]                  = $data_arr[$idx]["SCORE_JUDGE"];
            $data["ATTEND_STATUS_JUDGE"][TEXT]          = $data_arr[$idx]["ATTEND_STATUS_JUDGE"];
            $data["INTERDISCIPLINARY_JUDGE"][TEXT]      = $data_arr[$idx]["INTERDISCIPLINARY_JUDGE"];
            $data["CLUB_OTHER_JUDGE"][TEXT]             = $data_arr[$idx]["CLUB_OTHER_JUDGE"];
            $data["SCHOOL_LIFE_JUDGE"][TEXT]            = $data_arr[$idx]["SCHOOL_LIFE_JUDGE"];
            $data["GRADE_GROUP_JUDGE"][TEXT]            = $data_arr[$idx]["GRADE_GROUP_JUDGE"];
            $data["BIBLE_JUDGE"][TEXT]                  = $data_arr[$idx]["BIBLE_JUDGE"];
            $data["READING_JUDGE"][TEXT]                = $data_arr[$idx]["READING_JUDGE"];
            $data["CLASS_STF_JUDGE1"][TEXT]             = $data_arr[$idx]["CLASS_STF_JUDGE1"];
            $data["CLASS_STF_JUDGE2"][TEXT]             = $data_arr[$idx]["CLASS_STF_JUDGE2"];
            $data["CLASS_STF_JUDGE_TOTAL"][TEXT]        = $data_arr[$idx]["CLASS_STF_JUDGE_TOTAL"];
            $data["JUDGE_TOTAL_POINT"][TEXT]            = $data_arr[$idx]["JUDGE_TOTAL_POINT"];
            $data["ALL_GRADE_TOTAL_SCORE"][TEXT]        = $data_arr[$idx]["ALL_GRADE_TOTAL_SCORE"];
            $data["ALL_GRADE_TOTAL_SCORE_ORDER"][TEXT]  = $data_arr[$idx]["ALL_GRADE_TOTAL_SCORE_ORDER"];
            $data["HOPE_CD1"][TEXT]                     = $data_arr[$idx]["HOPE_CD1"];
            $data["HOPE_CONFIRM_FLG1"][TEXT]            = $data_arr[$idx]["HOPE_CONFIRM_FLG1"];
            $data["HOPE_CD2"][TEXT]                     = $data_arr[$idx]["HOPE_CD2"];
            $data["HOPE_CONFIRM_FLG2"][TEXT]            = $data_arr[$idx]["HOPE_CONFIRM_FLG2"];
            $data["HOPE_CD3"][TEXT]                     = $data_arr[$idx]["HOPE_CD3"];
            $data["HOPE_CONFIRM_FLG3"][TEXT]            = $data_arr[$idx]["HOPE_CONFIRM_FLG3"];
            $data["DONT_RECOMMENDATION_FLG"][TEXT]      = $data_arr[$idx]["DONT_RECOMMENDATION_FLG"];
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE YEAR      = '{$data_arr[$idx]["YEAR"]}' ";
                $where .= "   AND SCHREGNO  = '{$data_arr[$idx]["SCHREGNO"]}' ";
                //更新
                $query = Query::updateSQL($data, "AFT_RECOMMENDATION_INFO_DAT", $where);
            } else {
                $data["YEAR"][TEXT]      = $data_arr[$idx]["YEAR"];
                $data["SCHREGNO"][TEXT]  = $data_arr[$idx]["SCHREGNO"];
                //追加
                $query = Query::insertSQL($data, "AFT_RECOMMENDATION_INFO_DAT");
            }
            $db->query($query);

            //非推薦フラグが空の時のみ登録
            if ($data_arr[$idx]["DONT_RECOMMENDATION_FLG"] == "") {
                $campusFaccd = substr($data_arr[$idx]["HOPE_CD1"], 0, 2);
                $campusDeptcd = substr($hopeCd, -2, 2);
                $gRow = $db->getRow(knje373aQuery::getCollegeFacultycdDepartmentcd($campusFaccd, $campusDeptcd), DB_FETCHMODE_ASSOC);

                $mSeq = $db->getOne("select VALUE(MAX(SEQ), 0) AS MSEQ FROM AFT_GRAD_COURSE_DAT WHERE YEAR = '".CTRL_YEAR."' ");

                $data = array();
                $data["SENKOU_KIND"][TEXT]  = "0";
                $data["STAT_CD"][TEXT]      = $gRow["SCHOOL_CD"];
                $data["SCHOOL_GROUP"][TEXT] = $gRow["SCHOOL_GROUP"];
                $data["FACULTYCD"][TEXT]    = $gRow["FACULTYCD"];
                $data["DEPARTMENTCD"][TEXT] = $gRow["DEPARTMENTCD"];
                $data["HOWTOEXAM"][TEXT]    = "05";
                $data["DECISION"][TEXT]     = "1";
                $data["PLANSTAT"][TEXT]     = "1";
                $data["TOROKU_DATE"][TEXT]  = str_replace("/", "-", CTRL_DATE);
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "SYSDATE()";

                $data["YEAR"][TEXT]     = $data_arr[$idx]["YEAR"];
                $data["SEQ"][TEXT]      = $mSeq + 1;
                $data["SCHREGNO"][TEXT] = $data_arr[$idx]["SCHREGNO"];
                //SEQがMAX+1固定なので、必ず新規(=insertSQL)になる。
                $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DAT");
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //学内推薦先データの存在チェック
    public function getExistsAftRecmDatSql($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_INFO_DAT ";
        $query .= " WHERE YEAR      = '{$year}' ";
        $query .= "   AND SCHREGNO  = '{$schregno}' ";

        return $query;
    }

    //存在チェック（在籍）
    public function checkSchData($model, $year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "          ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //学部学科コードの取得
    public function getCollegeFacultycdDepartmentcd($campusFaccd, $campusDeptcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CM.SCHOOL_GROUP, ";
        $query .= "     FAC.SCHOOL_CD, ";
        $query .= "     FAC.FACULTYCD, ";
        $query .= "     FAC.FACULTYNAME, ";
        $query .= "     DEPT.DEPARTMENTCD, ";
        $query .= "     DEPT.DEPARTMENTNAME, ";
        $query .= "     FAC.CAMPUS_FACULTYCD, ";
        $query .= "     DEPT.CAMPUS_DEPARTMENTCD ";
        $query .= " FROM ";
        $query .= "   V_NAME_MST NME075 ";
        $query .= "   INNER JOIN COLLEGE_MST CM ";
        $query .= "     ON CM.SCHOOL_CD = NME075.NAME1 ";
        $query .= "   INNER JOIN COLLEGE_FACULTY_MST FAC ";
        $query .= "     ON FAC.SCHOOL_CD = NME075.NAME1 ";
        $query .= "    AND FAC.CAMPUS_FACULTYCD = '".$campusFaccd."' ";
        $query .= "   INNER JOIN COLLEGE_DEPARTMENT_MST DEPT ";
        $query .= "     ON DEPT.SCHOOL_CD = NME075.NAME1 ";
        $query .= "    AND DEPT.FACULTYCD = FAC.FACULTYCD ";
        $query .= "    AND DEPT.CAMPUS_DEPARTMENTCD = '".$campusDeptcd."' ";
        $query .= " WHERE ";
        $query .= "   NME075.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND NME075.NAMECD1 = 'E075' ";
        $query .= "   AND NME075.NAMECD2 = '0001' ";

        return $query;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";
        $query .= " ORDER BY ";
        $query .= "     MSGROW ";

        return $query;
    }

    public function getYearGradeCdSet($model)
    {
        $query  = " WITH SCHREG_YEARLAST AS ( ";
        $query .= " SELECT ";
        $query .= "   T3.GRADE_CD, ";
        $query .= "   MAX(T1.YEAR || '-' || T1.SEMESTER) AS LAST_YEARSEME, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "   AND T1.SCHREGNO IN (SELECT ";
        $query .= "                         TW.SCHREGNO ";
        $query .= "                       FROM ";
        $query .= "                         SCHREG_REGD_DAT TW INNER JOIN SCHREG_REGD_GDAT TW2 ON TW2.YEAR = TW.YEAR AND TW2.GRADE = TW.GRADE ";
        $query .= "                       WHERE ";
        $query .= "                         TW.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                         TW.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                         TW2.SCHOOL_KIND = 'H' AND ";
        $query .= "                         TW2.GRADE_CD = '03' ";
        $query .= "                      ) ";
        $query .= " GROUP BY ";
        $query .= "   T3.GRADE_CD, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SUBSTR(LAST_YEARSEME, 1,4) AS YEAR , ";
        $query .= "     GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_YEARLAST ";
        $query .= " ORDER BY ";
        $query .= "   YEAR, GRADE_CD ";
        return $query;
    }

    public function getBaseInfo($model)
    {
        $cutYearLen = 4;
        $query  = " WITH SCHREG_YEARLAST AS ( ";
        $query .= " SELECT ";
        $query .= "   T3.GRADE_CD, ";
        $query .= "   MAX(T1.YEAR || '-' || T1.SEMESTER) AS LAST_YEARSEME, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "   T3.GRADE_CD, ";
        $query .= "   T1.SCHREGNO ";
        $query .= " ), SCHREG_YEARLAST_WITH_ATTREM AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.GRADE_CD, ";
        $query .= "   T1.LAST_YEARSEME, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T3.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_YEARLAST T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = SUBSTR(T1.LAST_YEARSEME, 1, ".$cutYearLen.") ";
        $query .= "    AND T2.SEMESTER = SUBSTR(T1.LAST_YEARSEME, 6, 1) ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN HTRAINREMARK_DAT T3 ";
        $query .= "     ON T3.YEAR =  T2.YEAR ";
        $query .= "    AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "    AND T3.ANNUAL = T2.ANNUAL ";
        $query .= " ), RRANK_SDIVS AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.TESTKINDCD, ";
        $query .= "   T1.TESTITEMCD, ";
        $query .= "   T1.SCORE_DIV, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T2.TOTAL_CREDIT, ";
        $query .= "   T1.AVG, ";
        $query .= "   T2.TOTAL_POINT, ";
        $query .= "   T2.TOTAL_POINT_IG_GS, ";
        $query .= "   T2.TOTAL_POINT_HISSU, ";
        $query .= "   T2.GRADE_RANK, ";
        $query .= "   T2.COURSE_RANK, ";
        $query .= "   T2.SUMMARY_CREDIT, ";
        $query .= "   T2.SUMMARY_POINT, ";
        $query .= "   T2.SUMMARY_GRADE_RANK, ";
        $query .= "   T2.SUMMARY_COURSE_RANK ";
        $query .= " FROM ";
        $query .= "   RECORD_RANK_SDIV_DAT T1 ";
        $query .= "   LEFT JOIN RECORD_RANK_SDIV_SOUTEN_DAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "    AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "    AND T2.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "    AND T2.CLASSCD = T1.CLASSCD ";
        $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "    AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER || '-' || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9-990008' ";
        $query .= "   AND T1.SCHOOL_KIND = 'H' ";
        $query .= "   AND T1.SUBCLASSCD = '999999' ";
        $query .= " ), PROFICIENCY_ONEDAT AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER, ";
        $query .= "   PROFICIENCYDIV, ";
        $query .= "   PROFICIENCYCD, ";
        $query .= "   SCHREGNO, ";
        $query .= "   MIN(PROFICIENCY_SUBCLASS_CD) AS PROFICIENCY_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "   PROFICIENCY_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR <= '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "   AND PROFICIENCYDIV = '".$model->field["PROFICIENCYDIV"]."' ";
        $query .= "   AND PROFICIENCYCD = '".$model->field["PROFICIENCYCD"]."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER, ";
        $query .= "   PROFICIENCYDIV, ";
        $query .= "   PROFICIENCYCD, ";
        $query .= "   SCHREGNO ";
        $query .= " ), PROFICIENCY_MAKEDAT AS ( ";
        $query .= " SELECT ";
        $query .= "   '".CTRL_YEAR."' AS YEAR, ";  //過年度分も混ぜて当年度分扱いとして、後のテーブルに結合する。
        $query .= "   T4.SCHREGNO, ";
        $query .= "   T4.SCORE ";
        $query .= " FROM ";
        $query .= "   PROFICIENCY_DAT T4 ";
        $query .= "   INNER JOIN PROFICIENCY_ONEDAT T6 ";
        $query .= "     ON T6.YEAR = T4.YEAR ";
        $query .= "    AND T6.SEMESTER = T4.SEMESTER ";
        $query .= "    AND T6.PROFICIENCYDIV = T4.PROFICIENCYDIV ";
        $query .= "    AND T6.PROFICIENCYCD = T4.PROFICIENCYCD ";
        $query .= "    AND T6.SCHREGNO = T4.SCHREGNO ";
        $query .= "    AND T6.PROFICIENCY_SUBCLASS_CD = T4.PROFICIENCY_SUBCLASS_CD ";
        $query .= " ), DIST_QUACD AS ( ";
        $query .= " SELECT ";
        $query .= "   MAX(REGDDATE || '@' || '".CTRL_YEAR."') AS REGY, ";  //過年度取得もまとめる
        $query .= "   SCHREGNO, ";
        $query .= "   QUALIFIED_CD ";
        $query .= " FROM ";
        $query .= "   SCHREG_QUALIFIED_HOBBY_DAT ";
        $query .= " WHERE ";
        $query .= "   QUALIFIED_CD IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "   SCHREGNO, ";
        $query .= "   QUALIFIED_CD ";
        $query .= " ), CNT_QUACD AS ( ";
        $query .= " SELECT ";
        $query .= "   SUBSTR(T2.REGY, 12,4) AS YEAR, ";
        $query .= "   T2.SCHREGNO, ";
        $query .= "   COUNT(T2.QUALIFIED_CD) AS QUA_CNT ";
        $query .= " FROM ";
        $query .= "   DIST_QUACD T2 ";
        $query .= " GROUP BY ";
        $query .= "   SUBSTR(T2.REGY, 12,4), ";
        $query .= "   T2.SCHREGNO ";
        $query .= " ), ENG_TESTTBL(RANGE_S, RANGE_E, RANK, JUDGE) AS ( ";
        $query .= "   VALUES(350, 400, 'A', 1) ";
        $query .= "   UNION ";
        $query .= "   VALUES(300, 349, 'B', 1) ";
        $query .= "   UNION ";
        $query .= "   VALUES(250, 299, 'C', 1) ";
        $query .= "   UNION ";
        $query .= "   VALUES(200, 249, 'D', 1) ";
        $query .= "   UNION ";
        $query .= "   VALUES(150, 199, 'E', 0) ";
        $query .= "   UNION ";
        $query .= "   VALUES(0, 149, 'F', 0) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   CASE WHEN T3.GRADE_CD IS NULL THEN '' ELSE VALUE(T3.GRADE_CD, 0) END AS GRADE, ";
        $query .= "   CASE WHEN T1.HR_CLASS IS NULL THEN '' ELSE VALUE(T1.HR_CLASS, 0) END AS HR_CLASS, ";
        $query .= "   CASE WHEN T1.ATTENDNO IS NULL THEN '' ELSE VALUE(T1.ATTENDNO, 0) END AS ATTENDNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   T2.NAME_KANA, ";
        $query .= "   Z002.ABBV1 AS SEX, ";
        $query .= "   T5.FINSCHOOL_NAME, ";
        $query .= "   SUBSTR(T6_1.LAST_YEARSEME, 1, ".$cutYearLen.") AS T71YEAR, ";  //出欠を取得するのに利用
        $query .= "   T7_1.TOTAL_POINT AS TOTAL_POINT1, ";
        $query .= "   T7_1.TOTAL_POINT_IG_GS AS TOTAL_POINT_IG_GS1, ";
        $query .= "   DECIMAL(INT((T7_1.AVG * 10.0) + 0.5) / 10.0, 5, 1) AS AVG1, ";
        $query .= "   T7_1.GRADE_RANK AS GRADE_RANK1, ";
        $query .= "   T6_1.ATTENDREC_REMARK AS ATTENDREC_REMARK1, ";
        $query .= "   SUBSTR(T6_2.LAST_YEARSEME, 1, ".$cutYearLen.") AS T72YEAR, ";  //出欠を取得するのに利用
        $query .= "   T7_2.TOTAL_POINT AS TOTAL_POINT2, ";
        $query .= "   DECIMAL(INT((T7_2.AVG * 10.0) + 0.5) / 10.0, 5, 1) AS AVG2, ";
        $query .= "   T7_2.GRADE_RANK AS GRADE_RANK2, ";
        $query .= "   T6_2.ATTENDREC_REMARK AS ATTENDREC_REMARK2, ";
        $query .= "   SUBSTR(T6_3.LAST_YEARSEME, 1, ".$cutYearLen.") AS T73YEAR, ";  //出欠を取得するのに利用
        $query .= "   T7_3.TOTAL_POINT AS TOTAL_POINT3, ";
        $query .= "   T7_3.TOTAL_POINT_HISSU AS TOTAL_POINT_HISSU3, ";
        $query .= "   DECIMAL((INT(T7_3.TOTAL_POINT * (CASE WHEN T7_3.TOTAL_CREDIT = 30 THEN 0.0 ELSE (30/(30-T7_3.TOTAL_CREDIT)) END) * 100.0) + 0.5) / 100.0, 6, 2) AS CALC_CREDIT3, ";
        $query .= "   DECIMAL(INT((T7_3.AVG * 10.0) + 0.5) / 10.0, 5, 1) AS AVG3, ";
        $query .= "   T7_3.GRADE_RANK AS GRADE_RANK3, ";
        $query .= "   T6_3.ATTENDREC_REMARK AS ATTENDREC_REMARK3, ";
        $query .= "   CASE WHEN T11.QUA_CNT > 0 THEN 'AA' ELSE '' END AS OUTER_ENG_TEST, ";
        $query .= "   CASE WHEN T9.SCORE IS NULL THEN NULL ELSE T10.RANK END AS ENG_RANK, ";
        $query .= "   T14.SCORE_JUDGE AS SCORE_JUDGE, ";
        $query .= "   T14.ATTEND_STATUS_JUDGE AS ATTEND_STATUS_JUDGE, ";
        $query .= "   T14.INTERDISCIPLINARY_JUDGE AS INTERDISCIPLINARY_JUDGE, ";
        $query .= "   T14.CLUB_OTHER_JUDGE AS CLUB_OTHER_JUDGE, ";
        $query .= "   T14.SCHOOL_LIFE_JUDGE AS SCHOOL_LIFE_JUDGE, ";
        $query .= "   T14.GRADE_GROUP_JUDGE AS GRADE_GROUP_JUDGE, ";
        $query .= "   T14.BIBLE_JUDGE AS BIBLE_JUDGE, ";
        $query .= "   T14.READING_JUDGE AS READING_JUDGE, ";
        $query .= "   T14.CLASS_STF_JUDGE1 AS CLASS_STF_JUDGE1, ";
        $query .= "   T14.CLASS_STF_JUDGE2 AS CLASS_STF_JUDGE2, ";
        $query .= "   T14.CLASS_STF_JUDGE_TOTAL AS CLASS_STF_JUDGE_TOTAL, ";
        $query .= "   T14.JUDGE_TOTAL_POINT AS JUDGE_TOTAL_POINT, ";
        $query .= "   T14.ALL_GRADE_TOTAL_SCORE AS ALL_GRADE_TOTAL_SCORE, ";
        $query .= "   T14.ALL_GRADE_TOTAL_SCORE_ORDER AS ALL_GRADE_TOTAL_SCORE_ORDER, ";
        $query .= "   T14.HOPE_CD1 AS HOPE_CD1, ";
        $query .= "   T14.HOPE_CONFIRM_FLG1 AS HOPE_CONFIRM_FLG1, ";
        $query .= "   T14.HOPE_CD2 AS HOPE_CD2, ";
        $query .= "   T14.HOPE_CONFIRM_FLG2 AS HOPE_CONFIRM_FLG2, ";
        $query .= "   T14.HOPE_CD3 AS HOPE_CD3, ";
        $query .= "   T14.HOPE_CONFIRM_FLG3 AS HOPE_CONFIRM_FLG3, ";
        $query .= "   T14.DONT_RECOMMENDATION_FLG AS DONT_RECOMMENDATION_FLG ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T2 ";
        $query .= "     ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "     ON T3.YEAR = T1.YEAR ";
        $query .= "    AND T3.GRADE = T1.GRADE ";
        $query .= "   LEFT JOIN V_NAME_MST Z002 ";
        $query .= "     ON Z002.YEAR = T1.YEAR ";
        $query .= "    AND Z002.NAMECD1 = 'Z002' ";
        $query .= "    AND Z002.NAMECD2 = T2.SEX ";
        $query .= "   LEFT JOIN FINSCHOOL_MST T5 ";
        $query .= "     ON T5.FINSCHOOLCD = T2.FINSCHOOLCD ";
        $query .= "   LEFT JOIN SCHREG_YEARLAST_WITH_ATTREM T6_1 ";
        $query .= "     ON T6_1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND VALUE(T6_1.GRADE_CD, 0) = '1' ";
        $query .= "   LEFT JOIN RRANK_SDIVS T7_1 ";
        $query .= "     ON T7_1.YEAR = SUBSTR(T6_1.LAST_YEARSEME, 1, ".$cutYearLen.") ";
        $query .= "    AND T7_1.SCHREGNO = T6_1.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_YEARLAST_WITH_ATTREM T6_2 ";
        $query .= "     ON T6_2.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND VALUE(T6_2.GRADE_CD, 0) = '2' ";
        $query .= "   LEFT JOIN RRANK_SDIVS T7_2 ";
        $query .= "     ON T7_2.YEAR = SUBSTR(T6_2.LAST_YEARSEME, 1, ".$cutYearLen.") ";
        $query .= "    AND T7_2.SCHREGNO = T6_2.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_YEARLAST_WITH_ATTREM T6_3 ";
        $query .= "     ON T6_3.SCHREGNO = T1.SCHREGNO ";
        $query .= "    AND VALUE(T6_3.GRADE_CD, 0) = '3' ";
        $query .= "   LEFT JOIN RRANK_SDIVS T7_3 ";
        $query .= "     ON T7_3.YEAR = SUBSTR(T6_3.LAST_YEARSEME, 1, ".$cutYearLen.") ";
        $query .= "    AND T7_3.SCHREGNO = T6_3.SCHREGNO ";
        $query .= "   LEFT JOIN AFT_RECOMMENDATION_INFO_DAT T8 ";
        $query .= "     ON T8.YEAR = T1.YEAR ";
        $query .= "    AND T8.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN PROFICIENCY_MAKEDAT T9 ";
        $query .= "     ON T9.YEAR = T1.YEAR ";
        $query .= "    AND T9.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN ENG_TESTTBL T10 ";
        $query .= "     ON VALUE(T9.SCORE, 0) BETWEEN T10.RANGE_S AND T10.RANGE_E ";
        $query .= "   LEFT JOIN CNT_QUACD T11 ";
        $query .= "     ON T11.YEAR = T1.YEAR ";
        $query .= "    AND T11.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN AFT_RECOMMENDATION_INFO_DAT T14 ";
        $query .= "     ON T14.YEAR = T1.YEAR ";
        $query .= "    AND T14.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEMESTER = (SELECT MAX(TW.SEMESTER) FROM SCHREG_REGD_DAT TW WHERE TW.YEAR = T1.YEAR AND TW.SCHREGNO = T1.SCHREGNO) ";
        $query .= "   AND VALUE(T3.GRADE_CD, 0) = '3' ";
        $query .= " ORDER BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SCHREGNO ";
        return $query;
    }

    public function getSemesDat($db, $model, $searchYear, $gradeCd)
    {
        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ"; // 沖縄確認用
        }
        $query = "SELECT T1.SDATE, T1.EDATE FROM SEMESTER_MST T1 WHERE T1.YEAR = '".$searchYear."' AND T1.SEMESTER = '9' ";
        $seme = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = " select T1.GRADE || T1.HR_CLASS AS GRHR FROM SCHREG_REGD_HDAT T1 LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE WHERE T1.YEAR = '".$searchYear."' AND T2.GRADE_CD = '".$gradeCd."' ";
        $hrcls = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hrcls[] = $row["GRHR"];
        }
        $result->free();

        $data = array();
        // 帳票のAttendAccumulateで欠課時数取得
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJZ.detail.dao.AttendAccumulateIO',
                      'METHOD_NAME'     => 'json',
                      'DBNAME'          => DB_DATABASE,
                      'SDATE'           => str_replace('/', '-', $seme["SDATE"]),
                      'DATE'            => str_replace('/', '-', $seme["EDATE"]),
                      'ATTEND_DIV'      => 'ATTEND_SEMES_DAT',
                      'CTRL_YEAR'       => $searchYear,
                      'CTRL_SEMESTER'   => '9',
                      'GRADE_HR_CLASS'  => implode(",", $hrcls),
                      'DOCUMENTROOT'    => DOCUMENTROOT
        );

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($data)
            )
        );
        $target = array();
        $debug = array();
        $context  = stream_context_create($options);
        $result = file_get_contents($url, false, $context);
        if ($result != false) {
            $arr = json_decode($result, true);
            foreach ($arr as $schregno => $records) {  //データは生徒毎にarray格納。
                foreach ($records as $record) {  //1データづつ処理
                    $keyStr = $record["SEMESTER"]."-".$record["SCHREGNO"];
                    $target[$keyStr] = $record;
                }
            }
        }
        //echo "[[debug]] <br>".implode("<br>", $debug);
        return $target;
    }

    public function getSubclsDat($db, $model, $searchYear, $gradeCd)
    {
        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ"; // 沖縄確認用
        }
        $query = "SELECT T1.SDATE, T1.EDATE FROM SEMESTER_MST T1 WHERE T1.YEAR = '".$searchYear."' AND T1.SEMESTER = '9' ";
        $seme = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = " select T1.GRADE || T1.HR_CLASS AS GRHR FROM SCHREG_REGD_HDAT T1 LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE WHERE T1.YEAR = '".$searchYear."' AND T2.GRADE_CD = '".$gradeCd."' ";
        $hrcls = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hrcls[] = $row["GRHR"];
        }
        $result->free();
        
        $data = array();
        // 帳票のAttendAccumulateで欠課時数取得
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJZ.detail.dao.AttendAccumulateIO',
                      'METHOD_NAME'     => 'json',
                      'DBNAME'          => DB_DATABASE,
                      'SDATE'           => str_replace('/', '-', $seme["SDATE"]),
                      'DATE'            => str_replace('/', '-', $seme["EDATE"]),
                      'ATTEND_DIV'      => 'ATTEND_SUBCLASS_DAT',
                      'CTRL_YEAR'       => $searchYear,
                      'CTRL_SEMESTER'   => 9,
                      'GRADE_HR_CLASS'  => implode(",", $hrcls),
                      'DOCUMENTROOT'    => DOCUMENTROOT
        );

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($data)
            )
        );
        $target = array();
        $debug = array();
        $context  = stream_context_create($options);
        $result = file_get_contents($url, false, $context);
        if ($result != false) {
            $arr = json_decode($result, true);
            foreach ($arr as $schregno => $records) {  //データは生徒毎にarray格納。
                foreach ($records as $record) {  //1データづつ処理
                    //以降は独自処理。
                    $keyStr = $record["SEMESTER"]."-".$record["SCHREGNO"];
                    $target[$keyStr][] = $record;
                }
            }
        }
        return $target;
    }
}
