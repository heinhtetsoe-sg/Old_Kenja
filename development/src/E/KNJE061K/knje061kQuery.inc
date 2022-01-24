<?php

require_once('for_php7.php');

class knje061kquery extends Query
{

    //科目数の取得
    function getSubclasses($data, $model)
    {
        $query .= " SELECT SUBCLASSES ";
        $query .= "   FROM CLASS_MST ";
        //2004/08/19 arakaki $query .= "  WHERE CLASSCD = '".sprintf("%02d",$data["CLASSCD"]) ."'";
        $query .= "  WHERE CLASSCD = '".sprintf("%02s", $data["CLASSCD"]) ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
        }

        return $query;
    }

    //コースを取得
    function selectCourse($model)
    {
        $query  = " SELECT DISTINCT T1.coursecode, T2.coursecodename ";
        $query .= "   FROM schreg_regd_dat T1,";
        $query .= "         coursecode_mst T2 ";
        $query .= "  WHERE T1.coursecode = T2.coursecode";
        $query .= "    AND T1.grade      = '". $model->annual ."'";
        $query .= "    AND T1.hr_class   = '". $model->hr_class ."'";
        $query .= "    AND T1.year       = '".CTRL_YEAR ."'";
        $query .= "    AND T1.semester   = '".CTRL_SEMESTER ."'";
        $query .= "  ORDER BY T1.coursecode ";
        return $query;
    }

    //生徒取得
    function selectSchregno($model)
    {
        $query  = " SELECT DISTINCT T1.schregno, T2.name_show";
        $query .= "   FROM schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "  WHERE T1.coursecode = '". $model->coursecode ."'";
        $query .= "    AND T1.grade     = '". $model->annual ."'";
        $query .= "    AND T1.hr_class   = '". $model->hr_class ."'";
        $query .= "    AND T1.year       = '".CTRL_YEAR ."'";
        $query .= "    AND T1.semester   = '".CTRL_SEMESTER ."'";
        $query .= "    AND T1.schregno   = T2.schregno";
        $query .= "  ORDER BY T1.schregno";
        return $query;
    }

    //学年ごとの組を取得
    function selectQueryHRClass($grade)
    {
        $query  = "SELECT hr_class FROM schreg_regd_hdat";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= "   AND grade    = '".sprintf("%02d", $grade)."'";
        return $query;
    }

    //学年（年次）コンボボックス用
    function selectQueryAnnual($model)
    {
        $query  = "SELECT grade AS annual FROM schreg_regd_hdat";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= " GROUP BY grade";

        return $query;
    }

    //中高判別
    function GetJorH()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

    //成績データ新規作成と追加作成
    function addRecordSchregStudyrec($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        if ($model->field["CREATEDIV"] == 1) {
            $query = knje061kQuery::sqlDeleteStudyrec($model);
            $db->query($query);

            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $query = knje061kQuery::sqlDeleteStudyrecProvFlgALL($model);
                $db->query($query);
            }
        }

        //中高判別//alp 2006/03/24
        $getJorH = $db->getOne(knje061kQuery::GetJorH());
        //INSERT
        $query = knje061kQuery::sqlInsertStudyrec($getJorH, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //項目
            $data = array();
            $data = knje061kQuery::setData($row, $model);
            //追加
            $query = Query::insertSQL($data, "schreg_studyrec_dat");
            $db->query($query);

            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                //常に削除(1レコード)
                $query = knje061kQuery::sqlDeleteStudyrecProvFlg($row, $model);
                $db->query($query);
                //取得
                $query = knje061kQuery::getDataProvFlg($row, $model);
                $rowPF = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //フラグが１の時、追加(1レコード)
                if ($rowPF["PROV_FLG"] == "1") {
                    $query = knje061kQuery::setDataProvFlg($row, $model, $rowPF);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //学籍学習記録データの削除
    function sqlDeleteStudyrec($model)
    {
        //生成範囲の条件を分ける
        if ($model->field["RANGE"] == 1) {
            $OPERATOR = "=";
        } else {
            $OPERATOR = "<=";
        }

        $query = " DELETE FROM SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '0'";
        $query .= "   AND T1.YEAR ".$OPERATOR." '" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.SEMESTER = '" .CTRL_SEMESTER ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class)) {
            $query .= "  AND W.hr_class = '" .$model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= "  AND W.coursecode = '". $model->coursecode ."'";
        }
        if (strlen($model->schregno)) {
            $query .= "  AND W.schregno   = '". $model->schregno ."'";
        }
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        return $query;
    }

    //学籍学習記録データ作成
    function sqlInsertStudyrec($getJorH, $model)
    {
        //生成範囲の条件を分ける
        if ($model->field["RANGE"] == 1) {
            $OPERATOR = "=";
        } else {
            $OPERATOR = "<=";
        }
        $query .= " SELECT ";
        $query .= "     '0' as SCHOOLCD, ";
        $query .= "     T1.YEAR , ";
        $query .= "     T1.SCHREGNO , ";
        $query .= "     T1.ANNUAL , ";
        $query .= "     T2.CLASSCD , ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND, ";
            $query .= " T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD , ";
        $query .= "     T2.CLASSNAME , ";
        $query .= "     T2.CLASSABBV , ";
        $query .= "     T2.CLASSNAME_ENG , ";
        $query .= "     T2.CLASSABBV_ENG , ";
        $query .= "     T2.SUBCLASSES , ";
        $query .= "     T3.SUBCLASSNAME , ";
        $query .= "     T3.SUBCLASSABBV , ";
        $query .= "     T3.SUBCLASSNAME_ENG , ";
        $query .= "     T3.SUBCLASSABBV_ENG , ";
        $query .= "     CASE WHEN T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "               IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'D065') ";
        $query .= "          THEN T1.GRADE_RECORD ";
        $query .= "          ELSE T1.VALUATION ";
        $query .= "     END AS VALUATION,  ";
        $query .= "     CASE WHEN (DEC(T1.VALUATION ) > 1) OR (T1.SUBCLASSCD IN ('900100', '900200') AND DEC(T1.GRADE_RECORD) > 1)  ";
        $query .= "               OR (T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "                   IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'D065') AND DEC(T1.GRADE_RECORD) > 1) ";
        //中学---選択科目の時は、評定が '１' でも単位数は取込む
        if ($getJorH) {
            $query .= " OR (T2.ELECTDIV = '1' AND DEC(T1.VALUATION) > 0) ";
        }
        $query .= "     THEN T1.GET_CREDIT ELSE CAST(NULL AS SMALLINT) END AS GET_CREDIT, ";
        $query .= "     T1.ADD_CREDIT , ";
        $query .= "     CASE WHEN (DEC(T1.VALUATION ) > 0) OR (T1.SUBCLASSCD IN ('900100', '900200') AND DEC(T1.GRADE_RECORD) > 1)  ";
        $query .= "               OR (T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= "                   IN (SELECT IS1.NAME1 FROM V_NAME_MST IS1 WHERE IS1.YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'D065') AND DEC(T1.GRADE_RECORD) > 1) ";
        $query .= "          THEN T1.GET_CREDIT ELSE CAST(NULL AS SMALLINT) END AS COMP_CREDIT, ";
        $query .= "     '" .STAFFCD ."' ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         CASE WHEN T1.VALUATION IS NOT NULL THEN DEC(T1.VALUATION) ELSE CAST(NULL AS SMALLINT) END AS VALUATION, ";
        $query .= "         COALESCE(T1.GRADE_RECORD, 0) AS GRADE_RECORD, ";
        $query .= "         T1.CREDITS AS GET_CREDIT, ";
        $query .= "         CAST(NULL AS SMALLINT) AS ADD_CREDIT ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             ST1.YEAR, ";
        $query .= "             ST1.SEMESTER, ";
        $query .= "             ST1.SCHREGNO, ";
        $query .= "             ST2.CHAIRCD, ";
        $query .= "             ST1.ANNUAL, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         ST2.CLASSCD, ";
            $query .= "         ST2.SCHOOL_KIND, ";
            $query .= "         ST2.CURRICULUM_CD, ";
        }
        $query .= "             ST2.SUBCLASSCD, ";
        //中学//alp 2006/03/24
        if ($getJorH) {
            $query .= "         ST2.GRADE_ASSESS AS VALUATION, ";
//echo "中学";
        //高校//alp 2006/03/24
        } else {
            $query .= "         CASE WHEN ST2.JUDGE_PATTERN = 'A' THEN ST2.A_PATTERN_ASSESS WHEN ST2.JUDGE_PATTERN = 'B' THEN ST2.B_PATTERN_ASSESS WHEN ST2.JUDGE_PATTERN = 'C' THEN ST2.C_PATTERN_ASSESS END AS VALUATION, ";
//echo "高校";
        }
        $query .= "             ST2.GRADE_RECORD, ";
        $query .= "             ST3.CREDITS ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT AS ST1, ";
        $query .= "             KIN_RECORD_DAT AS ST2, ";
        $query .= "             CREDIT_MST AS ST3 ";
        $query .= "         WHERE ";
        $query .= "             ST1.YEAR $OPERATOR '" .CTRL_YEAR ."' AND ";
        $query .= "             ST1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "             ST2.YEAR = ST1.YEAR AND ";
        $query .= "             ST2.SCHREGNO = ST1.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         ST2.SCHOOL_KIND = ST3.SCHOOL_KIND AND ";
            $query .= "         ST2.CURRICULUM_CD = ST3.CURRICULUM_CD AND ";
        }
        $query .= "             ST2.SUBCLASSCD = ST3.SUBCLASSCD AND ";
        $query .= "             ST3.YEAR = ST1.YEAR AND ";
        $query .= "             ST3.COURSECD = ST1.COURSECD AND ";
        $query .= "             ST3.MAJORCD = ST1.MAJORCD AND ";
        $query .= "             ST3.GRADE = ST1.GRADE AND ";
        $query .= "             ST3.COURSECODE = ST1.COURSECODE AND ";
        $query .= "             ST3.CLASSCD = SUBSTR(ST2.SUBCLASSCD,1,2) ";
        $query .= "         ) T1 ";
        $query .= "     ) T1, ";
        $query .= "     class_mst T2, ";
        $query .= "     subclass_mst T3 ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(T1.subclasscd,1,2) = T2.classcd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= " T1.CLASSCD = T3.CLASSCD AND ";
            $query .= " T1.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
            $query .= " T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.subclasscd = T3.subclasscd AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_DAT W1 ";
        $query .= "             WHERE ";
        $query .= "                 W1.YEAR     = '" .CTRL_YEAR ."' AND ";
        $query .= "                 W1.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "                 W1.grade   = '" .$model->annual ."' AND ";
        if (strlen($model->hr_class)) {
            $query .= " W1.hr_class   = '" .$model->hr_class."' AND ";
        }
        if (strlen($model->coursecode)) {
            $query .= " W1.coursecode = '". $model->coursecode ."' AND ";
        }
        if (strlen($model->schregno)) {
            $query .= " W1.schregno   = '". $model->schregno ."' AND ";
        }
        $query .= "                 W1.SCHREGNO = T1.SCHREGNO ";
        $query .= "             ) AND ";
        $query .= "     NOT EXISTS(SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SUBCLASS_REPLACE_COMBINED_DAT W ";
        $query .= "                 WHERE ";
        $query .= "                     W.REPLACECD = '1' AND ";
        $query .= "                     W.YEAR = T1.YEAR AND ";
        $query .= "                     W.ATTEND_CLASSCD = T1.CLASSCD AND ";
        $query .= "                     W.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ";
        $query .= "                     W.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        $query .= "                     W.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
/***
        $query .= "                 ) AND ";

        #未受講データ(ゴミデータ)は作成しない。 2005/07/14
        $query .= "     EXISTS(SELECT ";
        $query .= "                 W1.SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 CHAIR_STD_DAT AS W1, CHAIR_DAT AS w2 ";
        $query .= "             WHERE ";
        $query .= "                 W1.YEAR       = W2.YEAR       AND ";
        $query .= "                 W1.SEMESTER   = W2.SEMESTER   AND ";
        $query .= "                 W1.CHAIRCD    = W2.CHAIRCD    AND ";
        $query .= "                 W1.YEAR       = T1.YEAR       AND ";
        $query .= "                 W1.SEMESTER   = T1.SEMESTER   AND ";
        $query .= "                 W1.SCHREGNO   = T1.SCHREGNO   AND ";
        $query .= "                 W2.SUBCLASSCD = T1.SUBCLASSCD ";
***/
        $query .= "             ) ";
        if ($model->field["CREATEDIV"] != 1) {
            //既存レコードは除く
            $query .= "    AND NOT EXISTS  (SELECT 'X' FROM schreg_studyrec_dat WK1 ";
            $query .= "                     WHERE WK1.schoolcd   = '0' ";
            $query .= "                       AND WK1.year       = T1.year ";
            $query .= "                       AND WK1.schregno   = T1.schregno ";
            $query .= "                       AND WK1.annual     = T1.annual ";
            $query .= "                       AND WK1.classcd    = SUBSTR(T1.subclasscd,1,2) ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   AND WK1.CLASSCD       = T1.CLASSCD ";
                $query .= "                   AND WK1.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                $query .= "                   AND WK1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                       AND WK1.subclasscd = T1.subclasscd ) ";
        }

#        echo $query;
        return $query;
    }

    //学籍番号の取得（INSERT用）
    function setData($row, $model)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $row["SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["ANNUAL"][TEXT]           = $row["ANNUAL"];
        $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]   = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT] = $row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["CLASSNAME"][TEXT]        = $row["CLASSNAME"];
        $data["CLASSABBV"][TEXT]        = $row["CLASSABBV"];
        $data["CLASSNAME_ENG"][TEXT]    = $row["CLASSNAME_ENG"];
        $data["CLASSABBV_ENG"][TEXT]    = $row["CLASSABBV_ENG"];
        $data["SUBCLASSES"][TEXT]       = $row["SUBCLASSES"];
        $data["SUBCLASSNAME"][TEXT]     = $row["SUBCLASSNAME"];
        $data["SUBCLASSABBV"][TEXT]     = $row["SUBCLASSABBV"];
        $data["SUBCLASSNAME_ENG"][TEXT] = $row["SUBCLASSNAME_ENG"];
        $data["SUBCLASSABBV_ENG"][TEXT] = $row["SUBCLASSABBV_ENG"];
        $data["VALUATION"][NUMBER]      = $row["VALUATION"];
        $data["GET_CREDIT"][NUMBER]     = $row["GET_CREDIT"];
        $data["COMP_CREDIT"][NUMBER]    = $row["COMP_CREDIT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        return $data;
    }

    //仮評定フラグデータの削除(ALL)
    function sqlDeleteStudyrecProvFlgALL($model)
    {
        //生成範囲の条件を分ける
        if ($model->field["RANGE"] == 1) {
            $OPERATOR = "=";
        } else {
            $OPERATOR = "<=";
        }

        $query = " DELETE FROM STUDYREC_PROV_FLG_DAT T1 ";
        $query .= " WHERE T1.schoolcd = '0'";
        $query .= "   AND T1.YEAR ".$OPERATOR." '" .CTRL_YEAR ."'";
        $query .= "   AND EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT W ";
        $query .= "                WHERE W.YEAR     = '" .CTRL_YEAR ."'";
        $query .= "                  AND W.SEMESTER = '" .CTRL_SEMESTER ."'";
        $query .= "                  AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class)) {
            $query .= "  AND W.hr_class = '" .$model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= "  AND W.coursecode = '". $model->coursecode ."'";
        }
        if (strlen($model->schregno)) {
            $query .= "  AND W.schregno   = '". $model->schregno ."'";
        }
        $query .= "                  AND W.SCHREGNO = T1.SCHREGNO ) ";
        return $query;
    }

    //仮評定フラグデータを取得
    function getDataProvFlg($row, $model)
    {
        $query  = " SELECT * FROM RECORD_PROV_FLG_DAT ";
        $query .= " WHERE YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
            $query .= "   AND SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    //仮評定フラグデータを削除(1レコード)
    function sqlDeleteStudyrecProvFlg($row, $model)
    {
        $query  = " DELETE FROM STUDYREC_PROV_FLG_DAT ";
        $query .= " WHERE SCHOOLCD      = '".$row["SCHOOLCD"]."' ";
        $query .= "   AND YEAR          = '".$row["YEAR"]."' ";
        $query .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";
        $query .= "   AND CLASSCD       = '".$row["CLASSCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."' ";
            $query .= "   AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
        }
        $query .= "   AND SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        return $query;
    }

    //仮評定フラグデータのセット
    function setDataProvFlg($row, $model, $rowPF)
    {
        $data = array();
        $data["SCHOOLCD"][TEXT]         = $row["SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
        $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
        }
        $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
        $data["PROV_FLG"][TEXT]         = $rowPF["PROV_FLG"];
/***
        $data["PROV_SEMESTER"][TEXT]    = $rowPF["PROV_SEMESTER"];
        $data["PROV_TESTKINDCD"][TEXT]  = $rowPF["PROV_TESTKINDCD"];
        $data["PROV_TESTITEMCD"][TEXT]  = $rowPF["PROV_TESTITEMCD"];
        $data["PROV_SCORE_DIV"][TEXT]   = $rowPF["PROV_SCORE_DIV"];
***/
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($data, "STUDYREC_PROV_FLG_DAT");
        return $query;
    }

    //学籍出欠記録データ作成処理
    function addRecordSchregAttendrec(&$model)
    {
        //生成範囲の条件を分ける
        if ($model->field["RANGE"] == 1) {
            $OPERATOR = "=";
        } else {
            $OPERATOR = "<=";
        }

        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //学籍出欠記録データ削除
        $query  = " DELETE FROM schreg_attendrec_dat T1 ";
        $query .= "  WHERE T1.schoolcd = '0'";
        $query .= "    AND T1.year ".$OPERATOR." '" .CTRL_YEAR ."' ";
        $query .= "  AND EXISTS (SELECT 'X' FROM schreg_regd_dat W ";
        $query .= "                 WHERE W.year     = '" .CTRL_YEAR ."' ";
        $query .= "                   AND W.semester = '" .CTRL_SEMESTER ."' ";
        $query .= "                   AND W.grade   = '" .$model->annual  ."'";
        if (strlen($model->hr_class)) {
            $query .= " AND W.hr_class   = '". $model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W.coursecode = '". $model->coursecode."'";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W.schregno   = '". $model->schregno ."'";
        }
        $query .= "                AND W.schregno = T1.schregno) ";
        $db->query($query);

        //学籍出欠記録データ作成(各勤怠別集計)
        $query  = " WITH T_MAX_DATE AS ( ";
        $query .= "  SELECT ST1.YEAR, ";
        $query .= "      CASE WHEN DAY(MAX_DATE) = 1 THEN   ";
        $query .= "              COALESCE(EDATE, LAST_DAY(MAX_DATE) +   ";
        $query .= "              (CASE WHEN ST3.SCHOOLNAME1 IS NULL THEN 1 ELSE 0 END) DAYS)   ";
        $query .= "      ELSE (CASE WHEN ST1.MAX_DATE > ST2.EDATE   ";
        $query .= "            THEN ST2.EDATE ELSE ST1.MAX_DATE END) END   ";
        $query .= "      AS MAX_DATE  ";
        $query .= "  FROM  ";
        $query .= "      (SELECT YEAR, ";
        $query .= "          MAX(APP_DATE) AS MAX_DATE  ";
        $query .= "      FROM  ";
        $query .= "          (SELECT DISTINCT DATE(YEAR || '-' || MONTH || '-' || APPOINTED_DAY ) AS APP_DATE  ";
        $query .= "            FROM ATTEND_SEMES_DAT  ";
        $query .= "          WHERE MONTH IN ('04','05','06','07','08','09','10','11','12')  ";
        $query .= "          UNION  ";
        $query .= "          SELECT DISTINCT DATE(SUBSTR(CHAR(DEC(YEAR)+1),13,4) || '-' || MONTH || '-' || APPOINTED_DAY ) AS APP_DATE  ";
        $query .= "            FROM ATTEND_SEMES_DAT  ";
        $query .= "          WHERE MONTH IN ('01','02','03')  ";
        $query .= "          ) AS T01,  ";
        $query .= "          (SELECT YEAR, ";
        $query .= "              SDATE,  ";
        $query .= "              EDATE  ";
        $query .= "          FROM  ";
        $query .= "              SEMESTER_MST  ";
        $query .= "          WHERE  ";
        $query .= "              YEAR ".$OPERATOR." '" .CTRL_YEAR ."' AND  ";
        $query .= "              SEMESTER='9'  ";
        $query .= "          ) AS T02  ";
        $query .= "      WHERE  ";
        $query .= "          T01.APP_DATE BETWEEN T02.SDATE AND  ";
        $query .= "          T02.EDATE  ";
        $query .= "      GROUP BY YEAR ";
        $query .= "      ) ST1 LEFT OUTER JOIN SEMESTER_MST ST2 ON   ";
        $query .= "          ST2.YEAR = ST1.YEAR AND   ";
        $query .= "          ST2.SEMESTER <> '9' AND   ";
        $query .= "          MONTH(ST1.MAX_DATE) = MONTH (ST2.EDATE)  ";
        $query .= "          LEFT OUTER JOIN SCHOOL_MST ST3 ON   ";
        $query .= "          ST3.YEAR = ST1.YEAR AND   ";
        $query .= "          ST3.SCHOOLNAME1 LIKE '%中学%'  ";
        $query .= " ) ";

        $query .= " ,attend_semes as ( ";
        $query .= " SELECT ";
        $query .= "     year, ";
        $query .= "     schregno, ";
        $query .= "     SUM(lesson) AS lesson, ";
        $query .= "     SUM(offdays) AS offdays, ";
        $query .= "     SUM(absent) AS absent, ";
        $query .= "     SUM(suspend) AS suspend, ";
        $query .= "     SUM(mourning) AS mourning, ";
        $query .= "     SUM(abroad) AS abroad, ";
        $query .= "     SUM(sick) AS sick, ";
        $query .= "     SUM(notice) AS notice, ";
        $query .= "     SUM(nonotice) AS nonotice ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "     year ".$OPERATOR." '" .CTRL_YEAR ."' ";
        $query .= " GROUP BY ";
        $query .= "     year, ";
        $query .= "     schregno ";
        $query .= " ) ";

        $query .= " SELECT  ";
        $query .= "     '0' as SCHOOLCD,  ";
        $query .= "     T1.YEAR,  ";
        $query .= "     T1.SCHREGNO,  ";
        $query .= "     T1.ANNUAL,  ";
        $query .= "     T3.MAX_DATE as SUMDATE,  ";
        $query .= "     VALUE(T2.lesson, 0) AS CLASSDAYS,  ";
        $query .= "     VALUE(T2.offdays, 0) AS OFFDAYS,  ";
        $query .= "     VALUE(T2.absent, 0) AS ABSENT,  ";
        $query .= "     VALUE(T2.suspend, 0)  ";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= "     + VALUE(T2.VIRUS, 0)) ";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= "     + VALUE(T2.KOUDOME, 0)) ";
        }
        $query .= "     AS SUSPEND,  ";
        $query .= "     VALUE(T2.mourning, 0) AS MOURNING,  ";
        $query .= "     VALUE(T2.abroad, 0) AS ABROAD,  ";
        $query .= "     VALUE(T2.lesson,0) - VALUE(T2.offdays,0) - (VALUE(T2.suspend,0) + VALUE(T2.mourning,0) + VALUE(T2.abroad,0)) as REQUIREPRESENT,  ";
        $query .= "     VALUE(T2.sick, 0) AS SICK,  ";
        $query .= "     VALUE(T2.notice, 0) AS NOTICE,  ";
        $query .= "     VALUE(T2.nonotice, 0) AS NONOTICE,  ";
        $query .= "     VALUE(T2.lesson,0) - VALUE(T2.offdays,0) - (VALUE(T2.suspend,0) + VALUE(T2.mourning,0) + VALUE(T2.abroad,0)) - (VALUE(T2.sick,0) + VALUE(T2.notice,0) + VALUE(T2.nonotice,0)) as PRESENT  ";
        $query .= "   FROM  ";
        $query .= "     (  ";
        $query .= "     SELECT T1.year  ";
        $query .= "           ,T1.schregno  ";
        $query .= "           ,max(T1.annual) as annual ";
        $query .= "       FROM schreg_regd_dat T1  ";
        $query .= "      WHERE T1.year ".$OPERATOR." '" .CTRL_YEAR ."' ";
        $query .= "        AND EXISTS (SELECT 'X' FROM schreg_regd_dat W1  ";
        $query .= "                     WHERE W1.year     = '" .CTRL_YEAR ."' ";
        $query .= "                       AND W1.semester = '" .CTRL_SEMESTER ."' ";
        $query .= "                       AND W1.grade   = '" .$model->annual ."' ";
        if (strlen($model->hr_class)) {
            $query .= " AND W1.hr_class   = '". $model->hr_class."'";
        }
        if (strlen($model->coursecode)) {
            $query .= " AND W1.coursecode = '". $model->coursecode."'";
        }
        if (strlen($model->schregno)) {
            $query .= " AND W1.schregno   = '". $model->schregno ."'";
        }
        $query .= "                       AND W1.schregno = T1.schregno)  ";
        $query .= "     GROUP BY T1.year, T1.schregno ";
        $query .= "     ) T1  ";
        $query .= " INNER JOIN attend_semes T2 ON T1.schregno = T2.schregno AND T1.year = T2.year  ";
        $query .= " INNER JOIN T_MAX_DATE T3 ON T1.year = T3.year   ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //項目
            $data = array();
            $data["SCHOOLCD"][TEXT]         = $row["SCHOOLCD"];
            $data["YEAR"][TEXT]             = $row["YEAR"];
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["ANNUAL"][TEXT]           = $row["ANNUAL"];
            $data["SUMDATE"][TEXT]          = $row["SUMDATE"];
            $data["CLASSDAYS"][NUMBER]      = $row["CLASSDAYS"];
            $data["OFFDAYS"][NUMBER]        = $row["OFFDAYS"];
            $data["ABSENT"][NUMBER]         = $row["ABSENT"];
            $data["SUSPEND"][NUMBER]        = $row["SUSPEND"];
            $data["MOURNING"][NUMBER]       = $row["MOURNING"];
            $data["ABROAD"][NUMBER]         = $row["ABROAD"];
            $data["REQUIREPRESENT"][NUMBER] = $row["REQUIREPRESENT"];
            $data["SICK"][NUMBER]           = $row["SICK"];
            $data["ACCIDENTNOTICE"][NUMBER]     = $row["NOTICE"];
            $data["NOACCIDENTNOTICE"][NUMBER]   = $row["NONOTICE"];
            $data["PRESENT"][NUMBER]        = $row["PRESENT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            //追加
            $query = Query::insertSQL($data, "schreg_attendrec_dat");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //出欠累積データの存在チェック
    function existsRecordAttendSemesDat(&$model)
    {
        $db = Query::dbCheckOut();

        //事前処理チェック
        $query = " SELECT ";
        $query .= "     CASE WHEN DAY(MAX_DATE) = 1 THEN  ";
        $query .= "             COALESCE(EDATE, LAST_DAY(MAX_DATE) +  ";
        $query .= "             (CASE WHEN ST3.SCHOOLNAME1 IS NULL THEN 1 ELSE 0 END) DAYS)  ";
        $query .= "     ELSE (CASE WHEN ST1.MAX_DATE > ST2.EDATE  ";
        $query .= "           THEN ST2.EDATE ELSE ST1.MAX_DATE END) END  ";
        $query .= "     AS MAX_DATE ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         MAX(APP_DATE) AS MAX_DATE ";
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT DATE(YEAR || '-' || MONTH || '-' || APPOINTED_DAY ) AS APP_DATE ";
        $query .= "           FROM ATTEND_SEMES_DAT ";
        $query .= "         WHERE MONTH IN ('04','05','06','07','08','09','10','11','12') ";
        $query .= "         UNION ";
        $query .= "         SELECT DISTINCT DATE(SUBSTR(CHAR(DEC(YEAR)+1),13,4) || '-' || MONTH || '-' || APPOINTED_DAY ) AS APP_DATE ";
        $query .= "           FROM ATTEND_SEMES_DAT ";
        $query .= "         WHERE MONTH IN ('01','02','03') ";
        $query .= "         ) AS T01, ";
        $query .= "         (SELECT ";
        $query .= "             SDATE, ";
        $query .= "             EDATE ";
        $query .= "         FROM ";
        $query .= "             SEMESTER_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR='" .CTRL_YEAR ."' AND ";
        $query .= "             SEMESTER='9' ";
        $query .= "         ) AS T02 ";
        $query .= "     WHERE ";
        $query .= "         T01.APP_DATE BETWEEN T02.SDATE AND ";
        $query .= "         T02.EDATE ";
        $query .= "     ) ST1 LEFT OUTER JOIN SEMESTER_MST ST2 ON  ";
        $query .= "         ST2.YEAR = '" .CTRL_YEAR ."' AND  ";
        $query .= "         ST2.SEMESTER <> '9' AND  ";
        $query .= "         MONTH(ST1.MAX_DATE) = MONTH (ST2.EDATE) ";
        $query .= "         LEFT OUTER JOIN SCHOOL_MST ST3 ON  ";
        $query .= "         ST3.YEAR = '" .CTRL_YEAR ."' AND  ";
        $query .= "         ST3.SCHOOLNAME1 LIKE '%中学%' ";

        $maxdate = $db->getOne($query);
        Query::dbCheckIn($db);

        if (trim($maxdate) == "") {
            $model->setWarning("MSG305");
            return false;
        } else {
            return true;
        }
    }

    //学籍学習記録データの削除
    function deleteQuerySchreg_StudyRec_Dat($data, $model)
    {
        $query .= " DELETE FROM SCHREG_STUDYREC_DAT ";
        $query .= "  WHERE SCHOOLCD   = '" .$data["SCHOOLCD"] ."'";
        $query .= "    AND YEAR       = '" .$data["YEAR"] ."'";
        $query .= "    AND SCHREGNO   = '" .sprintf("%08d", $data["SCHREGNO"]) ."'";
        $query .= "    AND ANNUAL     = '" .sprintf("%02d", $data["ANNUAL"]) ."'";
        $query .= "    AND CLASSCD    = '" .sprintf("%02d", $data["CLASSCD"]) ."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
            $query .= "    AND CURRICULUM_CD    = '" .$data["CURRICULUM_CD"] ."'";
        }
        $query .= "    AND SUBCLASSCD = '" .sprintf("%06d", $data["SUBCLASSCD"]) ."'";
        return $query;
    }

    //学籍学習記録データの作成（ＣＳＶデータより読込）
    function addQuerySchreg_StudyRec_Dat($data, $model)
    {
        $datas = array();
        $datas["SCHOOLCD"][TEXT]         = $data["SCHOOLCD"];
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d", $data["SCHREGNO"]);
        $datas["ANNUAL"][TEXT]           = sprintf("%02d", $data["ANNUAL"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d", $data["CLASSCD"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]   = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT] = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d", $data["SUBCLASSCD"]);
        $datas["CLASSNAME"][TEXT]        = $data["CLASSNAME"];
        $datas["CLASSABBV"][TEXT]        = $data["CLASSABBV"];
        $datas["CLASSNAME_ENG"][TEXT]    = $data["CLASSNAME_ENG"];
        $datas["CLASSABBV_ENG"][TEXT]    = $data["CLASSABBV_ENG"];
        $datas["SUBCLASSES"][NUMBER]     = ((is_numeric($data["SUBCLASSES"]))? $data["SUBCLASSES"] : "NULL");
        $datas["SUBCLASSNAME"][TEXT]     = $data["SUBCLASSNAME"];
        $datas["SUBCLASSABBV"][TEXT]     = $data["SUBCLASSABBV"];
        $datas["SUBCLASSNAME_ENG"][TEXT] = $data["SUBCLASSNAME_ENG"];
        $datas["SUBCLASSABBV_ENG"][TEXT] = $data["SUBCLASSABBV_ENG"];
        $datas["VALUATION"][NUMBER]      = ((is_numeric($data["VALUATION"]))? $data["VALUATION"] : "NULL");
        $datas["GET_CREDIT"][NUMBER]     = ((is_numeric($data["GET_CREDIT"]))? $data["GET_CREDIT"] : "NULL");
        $datas["ADD_CREDIT"][NUMBER]     = ((is_numeric($data["ADD_CREDIT"]))? $data["ADD_CREDIT"] : "NULL");
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";

        $query = Query::insertSQL($datas, "SCHREG_STUDYREC_DAT");
        return $query;
    }

    function deleteStudyclassremark($data, $model)
    {
        $query  = " DELETE FROM STUDYCLASSREMARK_DAT ";
        $query .= " WHERE YEAR       = '".$data["YEAR"]."'";
        $query .= "   AND SCHREGNO   = '".sprintf("%08d", $data["SCHREGNO"])."'";
        $query .= "   AND CLASSCD    = '".sprintf("%02d", $data["CLASSCD"])."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND SCHOOL_KIND      = '" .$data["SCHOOL_KIND"] ."'";
            $query .= "    AND CURRICULUM_CD    = '" .$data["CURRICULUM_CD"] ."'";
        }
        $query .= "   AND SUBCLASSCD = '".sprintf("%06d", $data["SUBCLASSCD"])."'";
        return $query;
    }

    function insertStudyclassremark($data, $model)
    {
        $datas = array();
        $datas["YEAR"][TEXT]             = $data["YEAR"];
        $datas["SCHREGNO"][TEXT]         = sprintf("%08d", $data["SCHREGNO"]);
        $datas["CLASSCD"][TEXT]          = sprintf("%02d", $data["CLASSCD"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $datas["SCHOOL_KIND"][TEXT]   = $data["SCHOOL_KIND"];
            $datas["CURRICULUM_CD"][TEXT] = $data["CURRICULUM_CD"];
        }
        $datas["SUBCLASSCD"][TEXT]       = sprintf("%06d", $data["SUBCLASSCD"]);
        $datas["REMARK"][TEXT]           = $data["REMARK"];
        $datas["REGISTERCD"][TEXT]       = STAFFCD;
        $datas["UPDATED"][FUNC]          = "SYSDATE()";
        $query = Query::insertSQL($datas, "STUDYCLASSREMARK_DAT");
        return $query;
    }

    /****************************/
    /* ↓↓↓ 観点データ ↓↓↓ */
    /****************************/

    //生徒取得
    function getStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE = '".$model->annual."' ";
        if (strlen($model->hr_class)) {
            $query .= "     AND T1.HR_CLASS    = '".$model->hr_class."' ";
        }
        if (strlen($model->coursecode)) {
            $query .= "     AND T1.COURSECODE  = '".$model->coursecode."' ";
        }
        if (strlen($model->schregno)) {
            $query .= "     AND T1.SCHREGNO    = '".$model->schregno."' ";
        }

        //署名チェックに該当したデータを対象から除く。
        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM ATTEST_OPINIONS_WK WK ";
            $query .= "                     WHERE WK.YEAR = T1.YEAR ";
            $query .= "                       AND WK.SCHREGNO = T1.SCHREGNO ";
            $query .= "                       AND WK.CHAGE_OPI_SEQ IS NOT NULL) ";
        }

        return $query;
    }

    //削除
    function delDataJview($db, $schregNo, $model)
    {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUB_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$schregNo."' ";

        $db->query($query);
    }

    //追加
    function insDataJview($db, $schregNo, $model)
    {
        $query  = " INSERT INTO JVIEWSTAT_SUB_DAT ";
        //学年別観点マスタ
        $query .= " WITH SUBCLASS_VIEW AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T2.VIEWCD, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     FROM ";
        $query .= "         JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "         JVIEWNAME_GRADE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.GRADE = '".$model->annual."' ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T2.STUDYREC_CLASSCD IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_SCHOOL_KIND IS NOT NULL ";
            $query .= "     AND T2.STUDYREC_CURRICULUM_CD IS NOT NULL ";
        }
        $query .= "         AND T2.STUDYREC_SUBCLASSCD IS NOT NULL ";
        $query .= "         AND T2.STUDYREC_VIEWCD IS NOT NULL ";
        $query .= "     ) ";
        //成績観点
        $query .= " , JVIEWSTAT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD, ";
        // 観点の計算方法 1:平均、2:最高
        if ($model->keisanDiv == "2") {
            $query .= "     rtrim(char(max(smallint(L1.NAMESPARE2)))) AS STATUS_SUUJI ";
        } else {
            $query .= "     rtrim(char(smallint(round(avg(float(smallint(L1.NAMESPARE2))),0)))) AS STATUS_SUUJI ";
        }
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_RECORD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_VIEW T2 ";
        $query .= "              ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         LEFT JOIN NAME_MST L1 ";
        $query .= "              ON L1.NAMECD1 = 'D028' ";
        $query .= "             AND L1.ABBV1 = T1.STATUS ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '9' ";
        $query .= "         AND T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     GROUP BY ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "         T2.STUDYREC_SUBCLASSCD, ";
        $query .= "         T2.STUDYREC_VIEWCD ";
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' AS YEAR, ";
        $query .= "     '9' AS SEMESTER, ";
        $query .= "     '".$schregNo."' AS SCHREGNO, ";
        //教育課程対応・・・TODO:スクリプト修正要
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.STUDYREC_CLASSCD AS CLASSCD, ";
            $query .= "     T1.STUDYREC_SCHOOL_KIND AS SCHOOL_KIND, ";
            $query .= "     T1.STUDYREC_CURRICULUM_CD AS CURRICULUM_CD, ";
        }
        $query .= "     T1.STUDYREC_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     T1.STUDYREC_VIEWCD AS VIEWCD, ";
        $query .= "     L1.ABBV1 AS STATUS, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'D028' ";
        $query .= "         AND L1.NAMESPARE2 = T1.STATUS_SUUJI ";

        $db->query($query);
    }
}
