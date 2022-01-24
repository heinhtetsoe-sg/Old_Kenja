<?php
class knjh442dQuery extends Query
{

    //名称マスタの取得
    public function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";
        return $query;
    }

    //学校名取得の取得(
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getGradeHrClass($model, $div = "")
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "           AND T1.GRADE = T2.GRADE ";
        $query .= "           AND T2.SCHOOL_KIND = 'H' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($div != "GRADE") {
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象クラスリストを取得
    public function getTestType($model, $ghosyaCd, $searchCdStrt, $searchCdEnd)
    {
        // $mockCdArr[] = "1".$this->field["YEAR"].$ghosyaCd.substr($this->field["GRADE"], 1, 1).$mockNo; // '1' + 年度 + 業者コード + 学年 + 模試番号
        // 模試コード = '1' + 年度 + 業者コード + 学年 + 模試番号
        $query .= " SELECT DISTINCT ";
        $query .= "   MOCKCD, ";
        $query .= "   substr(MOCKCD,2,4) AS YEAR, ";
        $query .= "   substr(MOCKCD,6,1) AS GHOSYACD, ";
        $query .= "   substr(MOCKCD,7,1) AS GRADE, ";
        $query .= "   substr(MOCKCD,8,2) AS MCD, ";
        $query .= "   '1' || '-' || substr(MOCKCD,2,4) || '-' || substr(MOCKCD,6,1) || '-' || substr(MOCKCD,7,1) || '-' || substr(MOCKCD,8,2) AS VALUE, ";
        $query .= "   MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   MOCK_MST ";
        $query .= " WHERE ";
        $query .= "   substr(MOCKCD,1,1)  = '1' ";
        $query .= "   AND substr(MOCKCD,2,4) <= '".CTRL_YEAR."' ";
        $query .= "   AND substr(MOCKCD,6,1) = '{$ghosyaCd}' ";
        $query .= "   AND substr(MOCKCD,8,2) BETWEEN '{$searchCdStrt}' AND '{$searchCdEnd}' ";
        $query .= "   AND VALUE(substr(MOCKCD,7,1), '') <= INT('{$model->field["GRADE"]}') ";
        $query .= " ORDER BY ";
        $query .= "   YEAR, ";
        $query .= "   GHOSYACD, ";
        $query .= "   GRADE, ";
        $query .= "   MCD ";

        return $query;
    }

    public function getAftRecommendationRankHead($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_RANK_HEAD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".$model->field["YEAR"]."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    //更新 -- AFT_RECOMMENDATION_RANK_HEAD_DAT
    public function getUpdateHead($db, &$model)
    {
        $data = array();
        $data["TENTATIVE_FLG"][TEXT] = $model->field["TENTATIVE_FLG"];
        $data["PERCENTAGE"][TEXT]    = $model->field["PERCENTAGE"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE YEAR  = '".$model->field["YEAR"]."' ";
        $where .= "   AND GRADE = '".$model->field["GRADE"]."' ";

        //件数取得
        $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM AFT_RECOMMENDATION_RANK_HEAD_DAT ".$where);
        if ($cnt == 0) {
            //追加
            $data["YEAR"][TEXT]  = $model->field["YEAR"];
            $data["GRADE"][TEXT] = $model->field["GRADE"];
            $query = Query::insertSQL($data, "AFT_RECOMMENDATION_RANK_HEAD_DAT");
        } else {
            //更新
            $query = Query::updateSQL($data, "AFT_RECOMMENDATION_RANK_HEAD_DAT", $where);
        }
        $db->query($query);

        return;
    }

    //削除 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
    public function getDeleteRank($db, $model)
    {
        $query .= " DELETE FROM AFT_SCHREG_RECOMMENDATION_RANK_DAT WHERE YEAR = '".$model->field["YEAR"]."'  AND GRADE = '".$model->field["GRADE"]."' ";
        $db->query($query);
        return;
    }

    //更新 -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
    public function getUpdateRank($db, &$model, $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr)
    {

        //対象生徒の取得
        $query = knjh442dQuery::getSchno($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新内容の取得
            $query = knjh442dQuery::getUpdatedInfo($model, $row["SCHREGNO"], $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $data = array();
            $data["YEAR"][TEXT]                             = $model->field["YEAR"];
            $data["SCHREGNO"][TEXT]                         = $row["SCHREGNO"];
            $data["GRADE"][TEXT]                            = $model->field["GRADE"];
            $data["MOCK_NATIONAL_LANGUAGE_AVG"][NUMBER]     = $Row["MOCK_NATIONAL_LANGUAGE_AVG"]; // 1.実力テストの国語の100点満点に換算した平均点
            $data["MOCK_MATH_AVG"][NUMBER]                  = $Row["MOCK_MATH_AVG"]; // 2.実力テストの数学の100点満点に換算した平均点
            $data["MOCK_ENGLISH_AVG"][NUMBER]               = $Row["MOCK_ENGLISH_AVG"]; // 3.実力テストの英語の100点満点に換算した平均点
            $data["MOCK_TOTAL_SCORE"][NUMBER]               = $Row["MOCK_TOTAL_SCORE"]; // 4.実力テストの国語、数学、英語の100点満点に換算した合計点
            $data["MOCK_TOTAL_AVG"][NUMBER]                 = $Row["MOCK_TOTAL_AVG"]; // 5.実力テストの国語、数学、英語の100点満点に換算した平均点
            $data["MOCK_TOTAL_PERCENT_SCORE"][NUMBER]       = $Row["MOCK_TOTAL_PERCENT_SCORE"]; // 6.実力テストの総合点(4)に対する割合を計算した成績
            $data["TEST_VALUATION_AVG"][NUMBER]             = $Row["TEST_VALUATION_AVG"]; // 7.定期考査の学年評定平均値
            $data["TEST_VALUATION_AVG_PERCENT"][NUMBER]     = $Row["TEST_VALUATION_AVG_PERCENT"]; // 8.定期考査の学年評定平均値(7)に総合点に対する割合を計算した平均値  7 * (20 * 割合 / 100)
            $data["TEST_VALUATION_PERCENT_SCORE"][NUMBER]   = $Row["TEST_VALUATION_PERCENT_SCORE"]; // 9.学年評定平均値の総合点に対する割合を計算した成績
            $data["REGISTERCD"][TEXT]                       = STAFFCD;
            $data["UPDATED"][FUNC]                          = "sysdate()";

            $where  = " WHERE YEAR     = '".$model->field["YEAR"]."' ";
            $where .= "   AND SCHREGNO = '".$row["SCHREGNO"]."' ";

            //件数取得
            $cnt = $db->getOne(" SELECT COUNT(*) AS CNT FROM AFT_SCHREG_RECOMMENDATION_RANK_DAT ".$where);
            if ($cnt == 0) {
                //追加
                $query = Query::insertSQL($data, "AFT_SCHREG_RECOMMENDATION_RANK_DAT");
            } else {
                //更新
                $query = Query::updateSQL($data, "AFT_SCHREG_RECOMMENDATION_RANK_DAT", $where);
            }
            $db->query($query);
        }
        return;
    }

    //更新対象生徒
    public function getSchno($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "   SCHREGNO ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR  = '".$model->field["YEAR"]."' ";
        $query .= "   AND GRADE = '".$model->field["GRADE"]."' ";
        return $query;
    }

    //更新内容の取得
    public function getUpdatedInfo($model, $schregno, $mockCdArr, $kokugoArr, $sugakuArr, $eigoArr)
    {
        $mockDiv = "1";
        $query .= " WITH SCHNO_A AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "       REGD.YEAR, ";
        $query .= "       REGD.SCHREGNO, ";
        $query .= "       GDAT.GRADE_CD, ";
        $query .= "       REGD.GRADE ";
        $query .= "     FROM ";
        $query .= "       SCHREG_REGD_DAT REGD ";
        $query .= "       INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "               ON GDAT.YEAR  = REGD.YEAR ";
        $query .= "              AND GDAT.GRADE = REGD.GRADE ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR    <= '".$model->field["YEAR"]."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= " ), SCHNO_HIST AS( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "       T1.* ";
        $query .= "     FROM ";
        $query .= "       SCHNO_A T1 ";
        $query .= "     WHERE ";
        $query .= "       T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ";
        $query .= " ), SCHNO AS( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE ";
        $query .= "   FROM ";
        $query .= "     MOCK_RANK_DAT T1 ";
        $query .= "     INNER JOIN SCHNO_HIST T2 ";
        $query .= "             ON T2.YEAR     = T1.YEAR ";
        $query .= "            AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.MOCKCD IN ('" .implode($mockCdArr, "','") ."') ";
        $query .= " ), RECORD AS( ";
        $query .= "   SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     SUM(VALUE(T2.SCORE, 0)) AS SCORE, ";
        $query .= "     AVG(VALUE(T2.SCORE, 0)) AS SCORE_AVG ";
        $query .= "   FROM ";
        $query .= "     SCHNO_HIST T1 ";
        $query .= "     INNER JOIN RECORD_RANK_SDIV_DAT T2 ";
        $query .= "             ON T2.YEAR     = T1.YEAR ";
        $query .= "            AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990009' ";
        $query .= "            AND T2.SUBCLASSCD = '999999' ";
        $query .= "   GROUP BY ";
        $query .= "     T2.SCHREGNO ";
        $query .= " ), MOCK_RANK AS( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MOCK.MOCK_SUBCLASS_CD, ";
        $query .= "     SUM(VALUE(VALUE(MOCK.SCORE,0) / MOCK_P.PERFECT * 100, 0)) AS SCORE, ";
        $query .= "     AVG(VALUE(VALUE(MOCK.SCORE,0) / MOCK_P.PERFECT * 100, 0)) AS SCORE_AVG ";
        $query .= "   FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     LEFT JOIN MOCK_RANK_DAT MOCK ";
        $query .= "            ON MOCK.YEAR             = T1.YEAR ";
        $query .= "           AND MOCK.MOCKCD           = T1.MOCKCD ";
        $query .= "           AND MOCK.SCHREGNO         = T1.SCHREGNO ";
        $query .= "           AND MOCK.MOCKDIV          = '".$mockDiv."' ";
        $query .= "     LEFT JOIN MOCK_PERFECT_DAT MOCK_P ";
        $query .= "            ON MOCK_P.YEAR             = T1.YEAR ";
        $query .= "           AND MOCK_P.COURSE_DIV       = '0' ";
        $query .= "           AND MOCK_P.GRADE            = T1.GRADE ";
        $query .= "           AND MOCK_P.MOCK_SUBCLASS_CD = MOCK.MOCK_SUBCLASS_CD ";
        $query .= "   GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MOCK.MOCK_SUBCLASS_CD ";
        $query .= " ), MOCK_RANK_SCORE AS( ";
        $query .= "   SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(L1.SCORE) AS KOKUGO_SCORE, ";
        $query .= "     AVG(L1.SCORE) AS KOKUGO_SCORE_AVG, ";
        $query .= "     SUM(L2.SCORE) AS SUGAKU_SCORE, ";
        $query .= "     AVG(L2.SCORE) AS SUGAKU_SCORE_AVG, ";
        $query .= "     SUM(L3.SCORE) AS EIGO_SCORE, ";
        $query .= "     AVG(L3.SCORE) AS EIGO_SCORE_AVG, ";
        $query .= "     SUM(L1.SCORE + L2.SCORE + L3.SCORE) AS TOTAL_SCORE ";
        $query .= "   FROM ";
        $query .= "     SCHNO T1 ";
        $query .= "     LEFT JOIN MOCK_RANK L1 ";
        $query .= "            ON L1.SCHREGNO         = T1.SCHREGNO ";
        $query .= "           AND L1.MOCK_SUBCLASS_CD IN ('" .implode($kokugoArr, "','") ."') ";
        $query .= "     LEFT JOIN MOCK_RANK L2 ";
        $query .= "            ON L2.SCHREGNO         = T1.SCHREGNO ";
        $query .= "           AND L1.MOCK_SUBCLASS_CD IN ('" .implode($sugakuArr, "','") ."') ";
        $query .= "     LEFT JOIN MOCK_RANK L3 ";
        $query .= "            ON L3.SCHREGNO         = T1.SCHREGNO ";
        $query .= "           AND L1.MOCK_SUBCLASS_CD IN ('" .implode($eigoArr, "','") ."') ";
        $query .= "   GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   L1.KOKUGO_SCORE_AVG AS MOCK_NATIONAL_LANGUAGE_AVG, ";
        $query .= "   L1.SUGAKU_SCORE_AVG AS MOCK_MATH_AVG, ";
        $query .= "   L1.EIGO_SCORE_AVG AS MOCK_ENGLISH_AVG, ";
        $query .= "   TOTAL_SCORE AS MOCK_TOTAL_SCORE, ";
        $query .= "   TOTAL_SCORE / 3 AS MOCK_TOTAL_AVG, ";
        $query .= "   TOTAL_SCORE * ".$model->field["PERCENTAGE2"]." / 100 AS MOCK_TOTAL_PERCENT_SCORE, ";
        $query .= "   L5.SCORE_AVG AS TEST_VALUATION_AVG, ";
        $query .= "   L5.SCORE_AVG * (20 * ".$model->field["PERCENTAGE2"]." / 100 ) AS TEST_VALUATION_AVG_PERCENT, ";
        $query .= "   L5.SCORE_AVG * ".$model->field["PERCENTAGE"]." / 100 AS TEST_VALUATION_PERCENT_SCORE ";
        $query .= " FROM ";
        $query .= "   SCHNO T1 ";
        $query .= "   LEFT JOIN MOCK_RANK_SCORE L1 ";
        $query .= "          ON L1.SCHREGNO         = T1.SCHREGNO ";
        $query .= "   LEFT JOIN RECORD L5 ";
        $query .= "          ON L5.SCHREGNO         = T1.SCHREGNO ";
        
        return $query;
    }

    //更新(順位) -- AFT_SCHREG_RECOMMENDATION_RANK_DAT
    public function getUpdateRank2($db, &$model)
    {
        //順位の種類
        $kbnArr = ["GRADE", "CLASS", "COURSE"];
        foreach ($kbnArr as $key => $kbn) {
            //対象順位
            $field = "MOCK_TOTAL_SCORE_GRADE_RANK"; // 10.総合点の学年順位
            if ($kbn == "CLASS") {
                $field = "MOCK_TOTAL_SCORE_CLASS_RANK"; // 10.総合点のクラス順位
            } elseif ($kbn == "COURSE") {
                $field = "MOCK_TOTAL_SCORE_COURSE_RANK"; // 10.総合点のコース順位
            }

            //順位のキーを取得
            $query = knjh442dQuery::getRankKey($model, $kbn);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //更新内容の取得
                $rank = 1;
                $wkRank = 1;
                $score = 0;
                $query = knjh442dQuery::getRankInfo($model, $kbn, $row["KEY"]);
                $result = $db->query($query);
                while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($score == $Row["SCORE"]) {
                        $wkRank++;
                    } elseif ($score != 0) {
                        $rank += $wkRank;
                        $wkRank = 1;
                    }
                    $data = array();
                    $data[$field][NUMBER]     = $rank;
                    $data["REGISTERCD"][TEXT] = STAFFCD;
                    $data["UPDATED"][FUNC]    = "sysdate()";
    
                    $where  = " WHERE YEAR     = '".$model->field["YEAR"]."' ";
                    $where .= "   AND SCHREGNO = '".$Row["SCHREGNO"]."' ";

                    //更新
                    $query = Query::updateSQL($data, "AFT_SCHREG_RECOMMENDATION_RANK_DAT", $where);
                    $db->query($query);
                    $score == $Row["SCORE"];
                }
            }
        }
        return;
    }

    //順位のキーを取得
    public function getRankKey($model, $kbn)
    {
        $query .= " SELECT DISTINCT ";
        if ($kbn == "GRADE") {
            $query .= "   GRADE AS KEY ";
        } elseif ($kbn == "CLASS") {
            $query .= "   GRADE || HR_CLASS AS KEY ";
        } elseif ($kbn == "COURSE") {
            $query .= "   COURSECD || MAJORCD || COURSECODE AS KEY ";
        }
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR  = '".$model->field["YEAR"]."' ";
        return $query;
    }

    //点数順に更新対象生徒を取得
    public function getRankInfo($model, $kbn, $key)
    {
        $query .= " SELECT ";
        $query .= "   T2.SCHREGNO, ";
        $query .= "   T2.TEST_VALUATION_AVG + T2.TEST_VALUATION_AVG_PERCENT + T2.TEST_VALUATION_PERCENT_SCORE AS SCORE ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN AFT_SCHREG_RECOMMENDATION_RANK_DAT T2 ";
        $query .= "           ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR  = '".$model->field["YEAR"]."' ";
        if ($kbn == "GRADE") {
            $query .= "   AND T1.GRADE = '".$key."' ";
        } elseif ($kbn == "CLASS") {
            $query .= "   AND T1.GRADE || T1.HR_CLASS = '".$key."' ";
        } elseif ($kbn == "COURSE") {
            $query .= "   AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$key."' ";
        }
        $query .= " ORDER BY VALUE(SCORE, 0) DESC ";
        return $query;
    }
}
