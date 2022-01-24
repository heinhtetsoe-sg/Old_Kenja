<?php

require_once('for_php7.php');

class knjs531Query extends Query {

    //年度取得
    function selectYearQuery($model) {
        $query  = " SELECT ";
        $query .= "     FISCALYEAR(EXECUTEDATE) AS LABEL, ";
        $query .= "     FISCALYEAR(EXECUTEDATE) AS VALUE ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " GROUP BY ";
        $query .= "     FISCALYEAR(EXECUTEDATE) ";
        $query .= " ORDER BY ";
        $query .= "     'FISCALYEAR(EXECUTEDATE)' DESC ";
        return $query;

    }

    //学校区分
    function getSchoolkind($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 || ':' || T1.ABBV1 AS LABEL, ";
        $query .= "     T1.NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.NAME1 IN (SELECT ";
                $query .= "                             REGD_G.SCHOOL_KIND ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.NAME1 IN (SELECT ";
            $query .= "                             REGD_G.SCHOOL_KIND ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.NAMECD2 ";
        return $query;
    }

    //年組取得
    function getGradeHrclass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || ':' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".$model->field["YEAR"]."' ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT E1 ";
        $query .= "                 WHERE ";
        $query .= "                     E1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "                     AND E1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "                     AND E1.GRADE = T1.GRADE ";
        if ($model->batch) {
            $query .= "                 AND E1.GRADE = '".$model->field["GRADE_HR"]."' ";
        }
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得(FI複式)
    function getGradeFiClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || ':' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR  = '".$model->field["YEAR"]."'";
        $query .= " AND T1.SEMESTER  = '".CTRL_SEMESTER."'";
        $query .= " AND EXISTS (SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_FI_HDAT E1 ";
        $query .= "                     LEFT JOIN SCHREG_REGD_GDAT E2 ON E2.YEAR = E1.YEAR ";
        $query .= "                                                  AND E2.GRADE = E1.GRADE ";
        $query .= "                 WHERE ";
        $query .= "                     E1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "                 AND E1.SEMESTER  = '".CTRL_SEMESTER."'";
        $query .= "                 AND E2.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "                 AND E1.GRADE = T1.GRADE ";
        if ($model->batch) {
            $query .= "                 AND E1.GRADE = '".$model->field["GRADE_HR"]."' ";
        }
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //訪問生取得
    function getVisitor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番　' || S2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST S1 ";
        $query .= "              ON T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "             AND T1.YEAR         = S1.YEAR ";
        $query .= "             AND S1.BASE_SEQ     = '004' ";
        $query .= "             AND S1.BASE_REMARK2 = '1' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S2 ";
        $query .= "              ON T1.SCHREGNO     = S2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || ':' || T1.HR_CLASS = '".$model->field["GRADE_HR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //EVENT_DATデータ確認
    function getEventcount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " WHERE ";
        if ($model->batch) {
            $query .= "     GRADE = '".$model->field["GRADE_HR"]."' ";
        } else {
            $query .= "     GRADE || ':' || HR_CLASS = '".$model->field["GRADE_HR"]."' ";
        }
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        $query .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }

    //EVENT_DATデータ確認　※指定月のみ
    function getEventcountMonth($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " WHERE ";
        if ($model->batch) {
            $query .= "     GRADE = '".$model->field["GRADE_HR"]."' ";
        } else {
            $query .= "     GRADE || ':' || HR_CLASS = '".$model->field["GRADE_HR"]."' ";
        }
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        $query .= "     AND MONTH(EXECUTEDATE)      =  ".$model->field["MONTH"]." ";
        $query .= "     AND HR_CLASS_DIV            = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }

    //EVENT_SCHREG_DATデータ確認　※指定月のみ
    function getEventSchregCountMonth($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO                = '".$model->field["VISITOR"]."' AND ";
        $query .= "     FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' AND ";
        $query .= "     MONTH(EXECUTEDATE)      =  ".$model->field["MONTH"]." ";

        return $query;
    }

    //画面情報有無確認
    function countEventMst($model, $setday) {
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["SHORI_DIV"] == "2") {
            $table  = "     EVENT_SCHREG_DAT ";
            $where  = "     SCHREGNO = '".$model->field["VISITOR"]."' ";
        } else {
            $table  = "     EVENT_DAT ";
            $where  = "     GRADE || ':' || HR_CLASS = '".$model->field["GRADE_HR"]."' ";
            $where .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .=       $where;
        $query .= "     AND EXECUTEDATE = '".$setday."'";

        return $query;
    }

    //画面情報を取得
    function setEventMst($model, $setday) {
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["SHORI_DIV"] == "2") {
            $table  = "     EVENT_SCHREG_DAT ";
            $where  = "     SCHREGNO = '".$model->field["VISITOR"]."' ";
        } else {
            $table  = "     EVENT_DAT ";
            $where  = "     GRADE || ':' || HR_CLASS = '".$model->field["GRADE_HR"]."' ";
            $where .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        }

        $query  = " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     HOLIDAY_FLG, ";
        if ($model->Properties["useEventAbbv"] == "1") {
            $query .= "     EVENT_ABBV, ";
        }
        $query .= "     REMARK1, ";
        $query .= "     REMARK2 ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .=       $where;
        $query .= "     AND EXECUTEDATE = '".$setday."'";

        return $query;
    }

    /***************************************
    更新用
    ****************************************/
    //対象年度の行事予定を登録する
    function &getInsertQuery($db, $model) {
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["SHORI_DIV"] == "2") {
            $table  = "EVENT_SCHREG_DAT";
            $where  = " SCHREGNO = '".$model->field["VISITOR"]."' ";
        } else {
            $table  = "EVENT_DAT";
            $where  = "     GRADE || ':' || HR_CLASS = '".$model->field["GRADE_HR"]."' ";
            $where .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
            list($grade, $hrClass) = preg_split("/:/", $model->field["GRADE_HR"]);
        }

        foreach ($model->updfield as $executeDate => $val) {
            $query  = " DELETE FROM ";
            $query .=       $table;
            $query .= " WHERE ";
            $query .=       $where;
            $query .= "     AND EXECUTEDATE = '".$executeDate."' ";
            $db->query($query);

            if ($val["HOLIDAY_FLG"] || $val["REMARK1"] || $val["REMARK2"] || $val["EVENT_ABBV"]) {
                $data = array();
                if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["SHORI_DIV"] == "2") {
                    $data["SCHREGNO"][TEXT]     = $model->field["VISITOR"];
                } else {
                    $data["GRADE"][TEXT]        = $grade;
                    $data["HR_CLASS"][TEXT]     = $hrClass;
                    $data["HR_CLASS_DIV"][TEXT] = $model->field["HR_CLASS_DIV"];
                }
                $data["EXECUTEDATE"][DATE]  = $executeDate;
                $data["HOLIDAY_FLG"][TEXT]  = $val["HOLIDAY_FLG"];
                if ($model->Properties["useEventAbbv"] == "1") {
                    $data["EVENT_ABBV"][TEXT]   = $val["EVENT_ABBV"];
                }
                $data["REMARK1"][TEXT]      = $val["REMARK1"];
                $data["REMARK2"][TEXT]      = $val["REMARK2"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::InsertSQL($data, $table);
                $db->query($query);
            }
        }

        return;
    }

    //対象年度の行事予定を削除する
    function getEventDelQuery($model, $ghr) {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     GRADE || ':' || HR_CLASS = '".$ghr."' ";
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        if ($model->cmd == 'shokitimonth') {
            $query .= "     AND MONTH(EXECUTEDATE) = ".$model->field["MONTH"]." ";
        }
        $query .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }

    //データ確認
    function getEventSchoolcount($model, $ghr, $div, $flg="") {

        list($grade, $hrClass) = preg_split("/:/", $ghr);
        $setGrade = $div == "1" ? "00" : $grade;
        if ($div == "2" && $flg == "all" || $div == "1") {
            $sethrClass = "000";
        } else {
            $sethrClass = $hrClass;
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVENT_MST ";
        $query .= " WHERE ";
        $query .= "     DATA_DIV = '".$div."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND GRADE = '".$setGrade."' ";
        $query .= "     AND FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        if ($model->cmd == 'shokitimonth') {
            $query .= " AND   MONTH(EXECUTEDATE) = ".$model->field["MONTH"]." ";
        }
        if ($div == "1") {
            $query .= "     AND COURSECD = '0' ";
            $query .= "     AND MAJORCD = '000' ";
        }
        $query .= "         AND HR_CLASS = '".$sethrClass."' ";
        if ($model->Properties["useFi_Hrclass"] == '1' && $div == "2" && $model->field["SCHOOL_KIND"] != "H") {
            $query .= "     AND HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     AND HR_CLASS_DIV = '2' ";
        } else {
            $query .= "     AND HR_CLASS_DIV = '1' ";
        }

        return $query;
    }

    //EVENT_DATコピー
    function getCopyQuery($model, $ghr, $div, $flg="") {
        list($grade, $hrClass) = preg_split("/:/", $ghr);
        $setGrade = $div == "1" ? "00" : $grade;
        if ($div == "2" && $flg == "all" || $div == "1") {
            $sethrClass = "000";
        } else {
            $sethrClass = $hrClass;
        }

        $query  = " INSERT INTO EVENT_DAT ( ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     HR_CLASS_DIV, ";
        $query .= "     HOLIDAY_FLG, ";
        if ($model->Properties["useEventAbbv"] == "1") {
            $query .= "     EVENT_ABBV, ";
        }
        $query .= "     EVENT_FLG, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     '".$grade."' AS GRADE, ";
        $query .= "     '".$hrClass."' AS HR_CLASS, ";
        $query .= "     T1.EXECUTEDATE, ";
        if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     '".$model->field["HR_CLASS_DIV"]."' AS HR_CLASS_DIV, ";
        } else {
            $query .= "     '1' AS HR_CLASS_DIV, ";
        }
        $query .= "     T1.HOLIDAY_FLG, ";
        if ($model->Properties["useEventAbbv"] == "1") {
            $query .= "     NULL AS EVENT_ABBV, ";
        }
        $query .= "     T1.EVENT_FLG, ";
        $query .= "     CASE WHEN LENGTH(T1.REMARK1) > 30 THEN LEFT(T1.REMARK1,30) ELSE T1.REMARK1 END AS REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     EVENT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.DATA_DIV = '".$div."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     AND T1.GRADE = '".$setGrade."' ";
        $query .= "     AND FISCALYEAR(T1.EXECUTEDATE) = '".$model->field["YEAR"]."' ";
        if ($model->cmd == 'shokitimonth') {
            $query .= " AND   MONTH(T1.EXECUTEDATE) = ".$model->field["MONTH"]." ";
        }
        $query .= "     AND T1.HR_CLASS = '".$sethrClass."' ";
        if ($model->Properties["useFi_Hrclass"] == '1' && $div == "2" && $model->field["SCHOOL_KIND"] != "H") {
            $query .= "     AND T1.HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     AND T1.HR_CLASS_DIV = '2' ";
        } else {
            $query .= "     AND T1.HR_CLASS_DIV = '1' ";
        }
        $query .= "     AND T1.COURSECD || T1.MAJORCD = (SELECT ";
        $query .= "                                         MIN(M1.COURSECD || M1.MAJORCD) AS COURSEMAJORCD ";
        $query .= "                                     FROM ";
        $query .= "                                         EVENT_MST M1 ";
        $query .= "                                     WHERE ";
        $query .= "                                         T1.DATA_DIV = M1.DATA_DIV  ";
        $query .= "                                     AND T1.SCHOOL_KIND = M1.SCHOOL_KIND ";
        $query .= "                                     AND T1.GRADE = M1.GRADE ";
        $query .= "                                     AND T1.HR_CLASS = M1.HR_CLASS ";
        $query .= "                                     AND T1.HR_CLASS_DIV = M1.HR_CLASS_DIV ";
        $query .= "                                     AND FISCALYEAR(T1.EXECUTEDATE) = FISCALYEAR(M1.EXECUTEDATE) ";
        $query .= "                                     AND MONTH(T1.EXECUTEDATE) = MONTH(M1.EXECUTEDATE) ";
        $query .= "                                     ) ";

        return $query;
    }

    //時間割削除
    function delSchChrDat($db, $model, $ghr) {
        $query  = " DELETE FROM ";
        $query .= "     SCH_CHR_DAT SCH_CDAT ";
        $query .= " WHERE ";
        $query .= "     SCH_CDAT.EXECUTEDATE IN ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             EV_D.EXECUTEDATE ";
        $query .= "         FROM ";
        $query .= "             EVENT_DAT EV_D ";
        $query .= "         WHERE ";
        $query .= "             EV_D.GRADE || ':' || EV_D.HR_CLASS = '{$ghr}' ";
        $query .= "             AND FISCALYEAR(EV_D.EXECUTEDATE) = '".$model->field["YEAR"]."'  ";
        $query .= "             AND EV_D.HR_CLASS_DIV = '".$model->field["HR_CLASS_DIV"]."' ";
        $query .= "             AND HOLIDAY_FLG = '1'  ";
        $query .= "     ) ";
        $query .= "     AND SCH_CDAT.CHAIRCD IN ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             CSTD.CHAIRCD ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT REGD, ";
        $query .= "             CHAIR_STD_DAT CSTD ";
        $query .= "         WHERE ";
        $query .= "             REGD.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "             AND REGD.GRADE || ':' || REGD.HR_CLASS = '{$ghr}' ";
        $query .= "             AND REGD.YEAR = CSTD.YEAR ";
        $query .= "             AND REGD.SEMESTER = CSTD.SEMESTER ";
        $query .= "             AND REGD.SCHREGNO = CSTD.SCHREGNO ";
        $query .= "     ) ";
        $db->query($query);

        //時間割ランク削除
        $query = "DELETE FROM UNIT_SCH_CHR_RANK_DAT WHERE YEAR = '".$model->field["YEAR"]."' ";
        $db->query($query);

        //時間割ランク作成
        $query  = " INSERT INTO UNIT_SCH_CHR_RANK_DAT ( ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     RANK, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";

        $query .= "     SELECT ";
        $query .= "          T1.EXECUTEDATE, ";
        $query .= "          T1.PERIODCD, ";
        $query .= "          T1.CHAIRCD, ";
        $query .= "          T1.YEAR, ";
        $query .= "          T1.SEMESTER, ";
        $query .= "          RANK() OVER(PARTITION BY T1.CHAIRCD ORDER BY T1.EXECUTEDATE, T1.PERIODCD) AS RANK, ";
        $query .= "          REGISTERCD, ";
        $query .= "          sysdate() ";
        $query .= "      FROM ";
        $query .= "          SCH_CHR_DAT T1 ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '".$model->field["YEAR"]."' ";

        $db->query($query);

        return;
    }

    //削除 -- EVENT_SCHREG_DAT
    function getEventSchregDelQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     EVENT_SCHREG_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO                = '".$model->field["VISITOR"]."' AND ";
        $query .= "     FISCALYEAR(EXECUTEDATE) = '".$model->field["YEAR"]."' AND ";
        $query .= "     MONTH(EXECUTEDATE)      = ".$model->field["MONTH"]." ";

        return $query;
    }

    //コピー -- EVENT_SCHREG_DAT
    function getEventSchregCopyQuery($model) {
        $query  = " INSERT INTO EVENT_SCHREG_DAT ( ";
        $query .= "     SCHREGNO, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     HOLIDAY_FLG, ";
        if ($model->Properties["useEventAbbv"] == "1") {
            $query .= "     EVENT_ABBV, ";
        }
        $query .= "     EVENT_FLG, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     '".$model->field["VISITOR"]."' AS SCHREGNO, ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     HOLIDAY_FLG, ";
        $query .= "     EVENT_FLG, ";
        if ($model->Properties["useEventAbbv"] == "1") {
            $query .= "     EVENT_ABBV, ";
        }
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     EVENT_DAT ";
        $query .= " WHERE ";
        $query .= "     GRADE || ':' || HR_CLASS    = '".$model->field["GRADE_HR"]."' AND ";
        $query .= "     FISCALYEAR(EXECUTEDATE)     = '".$model->field["YEAR"]."' AND ";
        $query .= "     MONTH(EXECUTEDATE)          =  ".$model->field["MONTH"]." AND ";
        $query .= "     HR_CLASS_DIV                = '".$model->field["HR_CLASS_DIV"]."' ";

        return $query;
    }
}
?>
