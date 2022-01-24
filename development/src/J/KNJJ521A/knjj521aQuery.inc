<?php
class knjj521aquery extends Query
{
    //年度取得
    public function getYear()
    {
        $query  = " SELECT DISTINCT";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //指定年度の最終学期を取得
    public function getLastSemester($model)
    {
        $query  = "     SELECT ";
        $query .= "         MAX(SEMESTER) AS MAX_SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND SEMESTER <> '9' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOLCD, ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "      PREF_CD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR        = '{$model->field["YEAR"]}' AND ";
        $query .= "      SCHOOLCD    = '".SCHOOLCD."' AND ";
        $query .= "      SCHOOL_KIND = '{$model->schoolkind}' ";

        return $query;
    }

    //文書番号
    public function getTuutatu($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.DOC_NUMBER = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJJ521A' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->field["YEAR"]}' AND ";
        $query .= "     GRADE   = '{$model->field["GRADE"]}'  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //課程学科コンボ
    public function getCourseMajor($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別コンボ
    public function getGender($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称取得
    public function getWeather($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' AND ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //日付取得
    public function getScoreDate($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR                = '{$model->field["YEAR"]}' AND ";
        $query .= "         SEMESTER            = '{$model->baseSemester}' AND ";
        $query .= "         GRADE || HR_CLASS   = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MIN(T1.DATE) AS DATE ";
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->field["YEAR"]}' AND ";
        $query .= "     T1.SCHREGNO IN (SELECT W1.SCHREGNO FROM SCHNO W1) ";

        return $query;
    }

    //体力種目マスタ
    public function getSportsItemMst()
    {
        $query  = " SELECT ";
        $query .= "     ITEMCD, ";
        $query .= "     ITEMABBV, ";
        $query .= "     UNIT ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD < '900' ";
        $query .= " ORDER BY ";
        $query .= "     ITEMCD ";

        return $query;
    }

    //県への報告履歴コンボ
    public function getReport($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     CHAR(T1.EXECUTE_DATE) || ':' || T1.COURSECD || ':' || T1.MAJORCD AS VALUE, ";
        $query .= "     RTRIM(CHAR(REPLACE(CHAR(T1.EXECUTE_DATE),'-','/'))) || '　' || L1.MAJORNAME  AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION521_DAT T1";
        $query .= "     INNER JOIN MAJOR_MST L1 ";
        $query .= "         ON  L1.COURSECD     = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD      = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

        return $query;
    }
   
    public function getAdditionReportInfoDat($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_ADDITION521_DAT T1 ";
        $query .= "     FULL JOIN ";
        $query .= "     SPORTS_SCORE_INQUIRY_ADDITION521_DAT T2 ";
        $query .= "     ON T2.EDBOARD_SCHOOLCD = T1.EDBOARD_SCHOOLCD ";
        $query .= "     AND T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "     AND T2.COURSECD = T1.COURSECD ";
        $query .= "     AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "     AND T2.SEX = T1.SEX ";
        $query .= "     AND T2.ROWNO = T1.ROWNO ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //リスト(再計算押下時)
    public function getRecalcList($model, $item_key)
    {
        $ctrlDate = str_replace("/", "-", CTRL_DATE);

        $query  = " WITH SPORTS_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        foreach ($item_key as $lenNo => $itemCd) {
            $query .= "    ,MAX(CASE WHEN ITEMCD = '{$itemCd}' THEN RECORD ELSE NULL END) AS RECORD".$lenNo;
        }
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN TOTAL ELSE NULL END) AS TOTAL ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN VALUE ELSE NULL END) AS VALUE ";
        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), SPORTS_INQUIRY_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $inquiryCdArray = range(1, $model->maxInquiryNum);
        foreach ($inquiryCdArray as $inquiryCd) {
            $query .= "    ,MAX(CASE WHEN INQUIRYCD = '{$inquiryCd}' THEN VALUE ELSE NULL END) AS INQUIRY".$inquiryCd;
        }
        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE_INQUIRY_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ), SPORTS_DATA  AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= "    ,T1.ATTENDNO ";
        $query .= "    ,L1.NAME_SHOW ";
        $query .= "    ,L1.GRD_DATE ";
        foreach ($item_key as $lenNo => $itemCd) {
            $query .= ",L2.RECORD".$lenNo;
        }
        $query .= "    ,L2.TOTAL ";
        $query .= "    ,L2.VALUE ";
        foreach ($inquiryCdArray as $inquiryCd) {
            $query .= ",L3.INQUIRY".$inquiryCd;
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SPORTS_SCORE L2 ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SPORTS_INQUIRY_SCORE L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR                 = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.SEMESTER             = '{$model->baseSemester}' ";
        $query .= "     AND T1.GRADE                = '{$model->field["GRADE"]}' ";
        $query .= " ), MEDEXAM_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         HEIGHT, ";
        $query .= "         WEIGHT, ";
        $query .= "         SITHEIGHT ";
        $query .= "     FROM ";
        $query .= "         MEDEXAM_DET_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     ROWNUMBER() OVER () AS ROWNO, ";
        $query .= "     SPD.* ,";
        $query .= "     MED.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SPORTS_DATA SPD ";
        $query .= "         ON SPD.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_DATA MED ";
        $query .= "         ON MED.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR                           = '{$model->field["YEAR"]}' ";
        $query .= "     AND REGD.SEMESTER                   = '{$model->baseSemester}' ";
        $query .= "     AND REGD.GRADE                      = '{$model->field["GRADE"]}' ";
        $query .= "     AND REGD.COURSECD || REGD.MAJORCD   = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= "     AND BASE.SEX                        = '{$model->field["SEX"]}' ";
        $query .= "     AND VALUE(BASE.GRD_DATE, '9999-12-31') > '{$ctrlDate}' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //リスト(通常)
    public function getList($model, $item_key)
    {
        $query  = " WITH SPORTS_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         ROWNO ";
        foreach ($item_key as $lenNo => $itemCd) {
            $query .= "    ,MAX(CASE WHEN ITEMCD = '{$itemCd}' THEN RECORD ELSE NULL END) AS RECORD".$lenNo;
        }
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN TOTAL ELSE NULL END) AS TOTAL ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN VALUE ELSE NULL END) AS VALUE ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN HEIGHT ELSE NULL END) AS HEIGHT ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN WEIGHT ELSE NULL END) AS WEIGHT ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN SITHEIGHT ELSE NULL END) AS SITHEIGHT ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN EXECDATE ELSE NULL END) AS SOKUTEI_DATE ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN WEATHER ELSE NULL END) AS WEATHER ";
        $query .= "        ,MAX(CASE WHEN ITEMCD = '999' THEN TEMPERATURE ELSE NULL END) AS TEMPERATURE ";
        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE_ADDITION521_DAT ";
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD        = '{$model->schoolcd}' ";
        $query .= "         AND YEAR                = '{$model->field["YEAR"]}' ";
        $query .= "         AND GRADE               = '{$model->field["GRADE"]}' ";
        $query .= "         AND COURSECD || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= "         AND SEX                 = '{$model->field["SEX"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         ROWNO ";
        $query .= " ), SPORTS_INQUIRY_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         ROWNO ";
        $inquiryCdArray = range(1, $model->maxInquiryNum);
        foreach ($inquiryCdArray as $inquiryCd) {
            $query .= "    ,MAX(CASE WHEN INQUIRYCD = '{$inquiryCd}' THEN VALUE ELSE NULL END) AS INQUIRY".$inquiryCd;
        }
        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE_INQUIRY_ADDITION521_DAT ";
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD        = '{$model->schoolcd}' ";
        $query .= "         AND YEAR                = '{$model->field["YEAR"]}' ";
        $query .= "         AND GRADE               = '{$model->field["GRADE"]}' ";
        $query .= "         AND COURSECD || MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= "         AND SEX                 = '{$model->field["SEX"]}' ";
        $query .= "     GROUP BY ";
        $query .= "         ROWNO ";
        $query .= " ) ";
        $query .= "     SELECT ";
        $query .= "        T1.ROWNO, ";
        $inquiryCdArray = range(1, $model->maxInquiryNum);
        foreach ($inquiryCdArray as $inquiryCd) {
            $query .= "        T2.INQUIRY{$inquiryCd},";
        }
        foreach ($item_key as $lenNo => $itemCd) {
            $itemCd = intval($itemCd);
            $query .= "        T1.RECORD{$lenNo},";
        }
        $query .= "        T1.TOTAL, ";
        $query .= "        T1.VALUE, ";
        $query .= "        T1.HEIGHT, ";
        $query .= "        T1.WEIGHT, ";
        $query .= "        T1.SITHEIGHT, ";
        $query .= "        T1.SOKUTEI_DATE, ";
        $query .= "        T1.WEATHER, ";
        $query .= "        T1.TEMPERATURE ";

        $query .= "     FROM ";
        $query .= "         SPORTS_SCORE T1 ";
        $query .= "         FULL JOIN SPORTS_INQUIRY_SCORE T2 ";
        $query .= "             ON T2.ROWNO  = T1.ROWNO ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;
        $souteiDate = ($model->field["SOKUTEI_DATE"]) ? str_replace("/", "-", $model->field["SOKUTEI_DATE"]) : "";
        $courseCd   = substr($model->field["COURSE_MAJOR"], 0, 1);
        $majorCd    = substr($model->field["COURSE_MAJOR"], 1, 3);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SPORTS_SCORE_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '{$model->schoolcd}' AND ";
        $query .= "     YEAR                = '{$model->field["YEAR"]}' AND ";
        $query .= "     GRADE               = '{$model->field["GRADE"]}' AND ";
        $query .= "     COURSECD            = '{$courseCd}' AND ";
        $query .= "     MAJORCD             = '{$majorCd }' AND ";
        $query .= "     SEX                 = '{$model->field["SEX"]}' ";
        $db->query($query);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SPORTS_SCORE_INQUIRY_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '{$model->schoolcd}' AND ";
        $query .= "     YEAR                = '{$model->field["YEAR"]}' AND ";
        $query .= "     GRADE               = '{$model->field["GRADE"]}' AND ";
        $query .= "     COURSECD            = '{$courseCd}' AND ";
        $query .= "     MAJORCD             = '{$majorCd }' AND ";
        $query .= "     SEX                 = '{$model->field["SEX"]}' ";
        $db->query($query);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '{$model->schoolcd}' AND ";
        $query .= "     YEAR                = '{$model->field["YEAR"]}' ";
        $db->query($query);

        //生徒でループ
        for ($gyoNo = 0; $gyoNo < count($model->data["ROWNO"]); $gyoNo++) {
            //変数
            $rowNo = $model->data["ROWNO"][$gyoNo];
            //種目でループ
            foreach ($model->data["RECORD"] as $lenNo => $itemCd) {
                $data = array();
                $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["COURSECD"][TEXT]         = $courseCd;
                $data["MAJORCD"][TEXT]          = $majorCd ;
                $data["SEX"][TEXT]              = $model->field["SEX"];
                $data["ROWNO"][TEXT]            = $rowNo;
                $data["ITEMCD"][TEXT]           = $itemCd;
                $data["RECORD"][NUMBER]         = $model->fields["RECORD".$lenNo][$gyoNo];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "SPORTS_SCORE_ADDITION521_DAT");
                $db->query($query);
            }
            //その他項目
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
            $data["YEAR"][TEXT]             = $model->field["YEAR"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["COURSECD"][TEXT]         = $courseCd;
            $data["MAJORCD"][TEXT]          = $majorCd ;
            $data["SEX"][TEXT]              = $model->field["SEX"];
            $data["ROWNO"][TEXT]            = $rowNo;
            $data["ITEMCD"][TEXT]           = "999";
            $data["VALUE"][TEXT]            = $model->fields["VALUE"][$gyoNo];
            $data["TOTAL"][TEXT]            = $model->fields["TOTAL"][$gyoNo];
            $data["HEIGHT"][TEXT]           = $model->fields["HEIGHT"][$gyoNo];
            $data["WEIGHT"][TEXT]           = $model->fields["WEIGHT"][$gyoNo];
            $data["SITHEIGHT"][TEXT]        = $model->fields["SITHEIGHT"][$gyoNo];
            $data["EXECDATE"][DATE]         = $model->field["SOKUTEI_DATE"];
            $data["WEATHER"][TEXT]          = $model->field["WEATHER"];
            $data["TEMPERATURE"][TEXT]      = $model->field["TEMPERATURE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]        = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "SPORTS_SCORE_ADDITION521_DAT");
            $db->query($query);

            //問診項目でループ
            $inquiryCdArray = range(1, $model->maxInquiryNum);
            foreach ($inquiryCdArray as $inquiryCd) {
                $data = array();
                $data["EDBOARD_SCHOOLCD"][TEXT] = $model->schoolcd;
                $data["YEAR"][TEXT]             = $model->field["YEAR"];
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["COURSECD"][TEXT]         = $courseCd;
                $data["MAJORCD"][TEXT]          = $majorCd ;
                $data["SEX"][TEXT]              = $model->field["SEX"];
                $data["ROWNO"][TEXT]            = $rowNo;
                $data["INQUIRYCD"][NUMBER]      = $inquiryCd;
                $data["VALUE"][NUMBER]          = $model->fields["INQUIRY".$inquiryCd][$gyoNo];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "SPORTS_SCORE_INQUIRY_ADDITION521_DAT");
                $db->query($query);
            }
        }
        //DB切断
        $db->commit();
        Query::dbCheckIn($db);
    }

    public function insertAddition521Query($row)
    {
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT] = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["GRADE"][TEXT]            = $row["GRADE"];
        $data["COURSECD"][TEXT]         = $row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
        $data["SEX"][TEXT]              = $row["SEX"];
        $data["ROWNO"][TEXT]            = $row["ROWNO"];
        $data["ITEMCD"][TEXT]           = $row["ITEMCD"];
        $data["RECORD"][NUMBER]         = $row["RECORD"];
        $data["SCORE"][NUMBER]          = $row["SCORE"];
        $data["VALUE"][TEXT]            = $row["VALUE"];
        $data["TOTAL"][TEXT]          = $row["TOTAL"];
        $data["HEIGHT"][NUMBER]         = $row["HEIGHT"];
        $data["WEIGHT"][NUMBER]         = $row["WEIGHT"];
        $data["SITHEIGHT"][NUMBER]      = $row["SITHEIGHT"];
        $data["EXECDATE"][DATE]         = $row["EXECDATE"];
        $data["WEATHER"][TEXT]          = $row["WEATHER"];
        $data["TEMPERATURE"][TEXT]      = $row["TEMPERATURE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "SPORTS_SCORE_ADDITION521_DAT");
        return $query;
    }
   
    public function insertAdditionInquiry521Query($row)
    {
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT] = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["GRADE"][TEXT]            = $row["GRADE"];
        $data["COURSECD"][TEXT]         = $row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
        $data["SEX"][TEXT]              = $row["SEX"];
        $data["ROWNO"][TEXT]            = $row["ROWNO"];
        $data["INQUIRYCD"][TEXT]        = $row["INQUIRYCD"];
        $data["VALUE"][NUMBER]          = $row["VALUE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "SPORTS_SCORE_INQUIRY_ADDITION521_DAT");
        return $query;
    }

    //UPDATE
    public function &getUpdateReport($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SPORTS_SCORE_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '{$model->schoolcd}' AND ";
        $query .= "     YEAR                = '{$model->field["YEAR"]}' ";
        // $query .= "     GRADE               = '{$model->field["GRADE"]}' AND ";
        // $query .= "     COURSECD            = '{$courseCd}' AND ";
        // $query .= "     MAJORCD             = '{$majorCd }' AND ";
        // $query .= "     SEX                 = '{$model->field["SEX"]}' ";

        $db2->query($query);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SPORTS_SCORE_INQUIRY_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD    = '{$model->schoolcd}' AND ";
        $query .= "     YEAR                = '{$model->field["YEAR"]}' ";
        // $query .= "     GRADE               = '{$model->field["GRADE"]}' AND ";
        // $query .= "     COURSECD            = '{$courseCd}' AND ";
        // $query .= "     MAJORCD             = '{$majorCd }' AND ";
        // $query .= "     SEX                 = '{$model->field["SEX"]}' ";

        $db2->query($query);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION521_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '{$model->schoolcd}'";
        $query .= "   AND YEAR              = '{$model->field["YEAR"]}'";
        $db->query($query);
        $db2->query($query);
    
        //INSERT
        //学校側から県側へコピ-
        $query = knjj521aQuery::getSchoolSideAdditionDatQuery($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjj521aQuery::insertAddition521Query($row2);
            $db2->query($query);
        }
        $result->free();
        $query = knjj521aQuery::getSchoolSideAdditionInquiryDatQuery($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjj521aQuery::insertAdditionInquiry521Query($row2);
            $db2->query($query);
        }
        $result->free();

        //報告用データ取得
        $query = knjj521aQuery::getAdditionReportInfoDat($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側、学校側・報告テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row2["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row2["YEAR"];
            $data["COURSECD"][TEXT]             = $row2["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row2["MAJORCD"];
            $data["EXECUTE_DATE"][TEXT]         = str_replace("/", "-", $model->field["EXECUTE_DATE"]);
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION521_DAT");
            $db->query($query);
            $db2->query($query);
        }
        $result->free();
        
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";
    
        $where  = " WHERE ";
        $where .= "     YEAR = '{$model->field["YEAR"]}' ";
        $where .= "     AND DOC_NUMBER = {$model->field["DOC_NUMBER"]} ";
        $where .= "     AND EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
    
        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);
    
        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //学校側から県側へコピーするデータ取得
    public function getSchoolSideAdditionDatQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";
    
        return $query;
    }
    //学校側から県側へコピーするデータ取得
    public function getSchoolSideAdditionInquiryDatQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_INQUIRY_ADDITION521_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";
    
        return $query;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            for ($i = 1; $i <= 6; $i++) {
                if ($data_arr["INQUIRYCD".$i] != "") {
                    $data_arr["INQUIRYCD"] = $data_arr["INQUIRYCD".$i];
                    $query = knjj521aQuery::insertAdditionInquiry521QuerySeq($data_arr);
                    $db->query($query);
                }
            }
            for ($i = 1; $i <= 9; $i++) {
                if ($data_arr["RECORD".$i] != "") {
                    $data_arr["RECORD"] = $data_arr["RECORD".$i];
                    $query = knjj521aQuery::insertAddition521QuerySeq($data_arr, $i);
                    $db->query($query);
                }
                $db->query($query);
            }

            $cnt++;
        }
        return $cnt;
    }
    public function insertAdditionInquiry521QuerySeq($row)
    {
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT] = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["GRADE"][TEXT]            = $row["GRADE"];
        $data["COURSECD"][TEXT]         = $row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
        $data["SEX"][TEXT]              = $row["SEX"];
        $data["ROWNO"][TEXT]            = $row["ROWNO"];
        $data["INQUIRYCD"][TEXT]        = $row["INQUIRYCD"];
        $data["VALUE"][NUMBER]          = $row["VALUE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "SPORTS_SCORE_INQUIRY_ADDITION521_DAT");
        return $query;
    }
    public function insertAddition521QuerySeq($row, $seq)
    {
        $data = array();
        $data["EDBOARD_SCHOOLCD"][TEXT] = $row["EDBOARD_SCHOOLCD"];
        $data["YEAR"][TEXT]             = $row["YEAR"];
        $data["GRADE"][TEXT]            = $row["GRADE"];
        $data["COURSECD"][TEXT]         = $row["COURSECD"];
        $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
        $data["SEX"][TEXT]              = $row["SEX"];
        $data["ROWNO"][TEXT]            = $row["ROWNO"];
        $data["INQUIRYCD"][TEXT]        = $row["INQUIRYCD"];
        $data["VALUE"][NUMBER]          = $row["VALUE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD ;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //追加
        $query = Query::insertSQL($data, "SPORTS_SCORE_ADDITION521_DAT");
        return $query;
    }
}
