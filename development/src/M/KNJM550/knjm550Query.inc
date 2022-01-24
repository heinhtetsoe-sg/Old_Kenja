<?php

require_once('for_php7.php');


class knjm550Query extends Query
{

    //受講科目
    public function getChairStd($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.CHAIRCD || '-' || T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "           AND T1.CLASSCD = L1.CLASSCD ";
            $query .= "           AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ";
            $query .= "           AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        $query .= "     AND substr(T1.SUBCLASSCD, 1, 2) < '91' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             CHAIR_STD_DAT E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.SEMESTER ";
        $query .= "             AND T1.CHAIRCD = E1.CHAIRCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //回数
    public function getKaisu()
    {
        $query .= " WITH KAISU (LABEL, VALUE) AS ( ";
        $query .= " VALUES ";
        $sep = "";
        for ($i = 1; $i < 13; $i++) {
            $query .= $sep." (" .$i. ", " .$i. ") ";
            $sep = ",";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KAISU ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }
    //生徒一覧
    public function getSch($model)
    {
        $kamokuCnt = 0;
        $query  = " WITH SCHREG_CHAIR AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";

        $chairInState = "";
        $inSep = "";
        $withQuery = "";
        $joinQuery = "";
        $whereQuery = "(";
        $whereSep = "";
        $dataFlg = false;
        $kamokuCnt = 0;
        for ($i = 1; $i <= $model->panelCnt; $i++) {
            if ($model->searchField["SUBCLASS".$i]) {
                $chair = preg_split("/-/", $model->searchField["SUBCLASS".$i]);
                $chairInState .= $inSep."'".$chair[0]."'";
                $inSep = ",";

                list($withQ, $joinQ, $whereQ) = knjm550Query::getSearchData($model, $i, $chair[0], $chair[1]);
                $withQuery  .= $withQ;
                $joinQuery  .= $joinQ;
                $whereQuery .= $whereQ ? $whereSep.$whereQ : $whereQ;
                if ($whereQ) {
                    $whereSep = $model->mainSearch == "1" ? " AND " : " OR ";
                }

                $dataFlg = $whereQ ? true : $dataFlg;
                $kamokuCnt++;
            }
        }
        $whereQuery .= ")";

        if ($chairInState) {
            $query .= "     AND T1.CHAIRCD IN(".$chairInState.") ";
        }
        $query .= " ), SCHREG_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHAIR T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        if ($model->mainSearch == "1") {
            $query .= " HAVING ";
            $query .= "     COUNT(*) = ".$kamokuCnt." ";
        }

        $query .= $withQuery;
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T3.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' || T2.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "          AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T3.GRADE = T1.GRADE ";
        $query .= "          AND T3.HR_CLASS = T1.HR_CLASS ";

        $query .= $joinQuery;

        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        $query .= "     AND EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_T T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         ) ";

        if ($dataFlg) {
            $query .= "     AND ".$whereQuery;
        }

        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    public function getSearchData($model, $cnt, $chairCd, $subclassCd)
    {
        $withQ = "";
        $joinQ = "";
        $whereQ = "";
        $whereSep = "";
        $whereJouken = $model->searchField["SEARCH_S".$cnt] == "1" ? " AND " : " OR ";
        //スクーリング
        if (strlen($model->searchField["SCHOOLING".$cnt]) > 0) {
            $jouken = "";
            switch ($model->searchField["SCHOOLING_S".$cnt]) {
                case "1":
                    $jouken = " = ";
                    break;
                case "2":
                    $jouken = " >= ";
                    break;
                case "3":
                    $jouken = " <= ";
                    break;
                default:
                    break;
            }
            $withQ .= " ), KIND".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     T1.SCHREGNO, ";
            $withQ .= "     T1.SCHOOLINGKINDCD, ";
            $withQ .= "     T1.SCHOOLING_SEQ ";
            $withQ .= " FROM ";
            $withQ .= "     SCH_ATTEND_DAT T1, ";
            $withQ .= "     SCHREG_T T2 ";
            $withQ .= " WHERE ";
            $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
            $withQ .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
            $withQ .= "     AND T1.CHAIRCD = '" .$chairCd. "' ";
            $withQ .= "     AND T1.SCHOOLINGKINDCD = '1' ";
            $withQ .= " GROUP BY ";
            $withQ .= "     T1.SCHREGNO, ";
            $withQ .= "     T1.SCHOOLINGKINDCD, ";
            $withQ .= "     T1.SCHOOLING_SEQ ";
            $withQ .= " ), ETCKIND".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     T1.SCHREGNO, ";
            $withQ .= "     T1.SCHOOLINGKINDCD, ";
            $withQ .= "     T1.SCHOOLING_SEQ ";
            $withQ .= " FROM ";
            $withQ .= "     SCH_ATTEND_DAT T1, ";
            $withQ .= "     SCHREG_T T2 ";
            $withQ .= " WHERE ";
            $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
            $withQ .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
            $withQ .= "     AND T1.CHAIRCD = '" .$chairCd. "' ";
            $withQ .= "     AND T1.SCHOOLINGKINDCD <> '1' ";
            $withQ .= " ), KINDCNT".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     COUNT(*) AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     KIND".$cnt." T1 ";
            $withQ .= " GROUP BY ";
            $withQ .= "     SCHREGNO ";
            $withQ .= " UNION ALL ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     COUNT(*) AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     ETCKIND".$cnt." T1 ";
            $withQ .= " GROUP BY ";
            $withQ .= "     SCHREGNO ";
            $withQ .= " ), KINDCNT_2".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     0 AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     SCHREG_T T1 ";
            $withQ .= " WHERE ";
            $withQ .= "     T1.SCHREGNO NOT IN ( ";
            $withQ .= "         SELECT DISTINCT ";
            $withQ .= "             E1.SCHREGNO ";
            $withQ .= "         FROM ";
            $withQ .= "             KINDCNT".$cnt." E1 ";
            $withQ .= "     ) ";
            $withQ .= " GROUP BY ";
            $withQ .= "     T1.SCHREGNO ";
            $withQ .= " ), KINDCNT_3".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     CNT ";
            $withQ .= " FROM ";
            $withQ .= "     KINDCNT".$cnt." T1 ";
            $withQ .= " UNION ALL ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     CNT ";
            $withQ .= " FROM ";
            $withQ .= "     KINDCNT_2".$cnt." T1 ";
            $withQ .= " ), KIND_MAIN".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     SUM(CNT) AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     KINDCNT_3".$cnt." ";
            $withQ .= " GROUP BY ";
            $withQ .= "     SCHREGNO ";
            $withQ .= " HAVING ";
            $withQ .= "     SUM(CNT) " .$jouken.$model->searchField["SCHOOLING".$cnt]. " ";
            $withQ .= " ), SCHOOLING".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     COUNT(*) AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     KIND_MAIN".$cnt." ";
            $withQ .= " GROUP BY ";
            $withQ .= "     SCHREGNO ";

            $joinQ  .= "     LEFT JOIN SCHOOLING".$cnt." ON T1.SCHREGNO = SCHOOLING".$cnt.".SCHREGNO ";

            $whereQ .= $whereSep ? $whereSep : "(";
            $whereQ .= "VALUE(SCHOOLING".$cnt.".CNT, 0) > 0 ";

            $whereSep = $whereJouken;
        }
        //レポート
        if (strlen($model->searchField["REPORT".$cnt]) > 0) {
            if ($model->searchField["REPORT_S".$cnt] != "3") {
                $withQ .= " ), REP_DATA".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.STANDARD_SEQ, ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " FROM ";
                $withQ .= "     REP_PRESENT_DAT T1 ";
                $withQ .= " WHERE ";
                $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $withQ .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '".$subclassCd."' ";
                } else {
                    $withQ .= "     AND T1.SUBCLASSCD = '".$subclassCd."' ";
                }
                $withQ .= "     AND STANDARD_SEQ <= " .$model->searchField["REPORT".$cnt]. " ";
                if ($model->searchField["REPORT_S".$cnt] == "2") {
                    $withQ .= "     AND VALUE(GRAD_VALUE, '0') >= '2' ";
                }
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.STANDARD_SEQ, ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " ), REP_MAIN".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.SCHREGNO, ";
                $withQ .= "     COUNT(*) AS CNT ";
                $withQ .= " FROM ";
                $withQ .= "     REP_DATA".$cnt." T1 ";
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " HAVING ";
                $withQ .= "     COUNT(*) = " .$model->searchField["REPORT".$cnt]. " ";
            } else {
                //合格
                $withQ .= " ), REP_DATA_PASS".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.STANDARD_SEQ, ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " FROM ";
                $withQ .= "     REP_PRESENT_DAT T1 ";
                $withQ .= " WHERE ";
                $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $withQ .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '".$subclassCd."' ";
                } else {
                    $withQ .= "     AND T1.SUBCLASSCD = '".$subclassCd."' ";
                }
                $withQ .= "     AND STANDARD_SEQ <= " .$model->searchField["REPORT".$cnt]. " ";
                $withQ .= "     AND VALUE(GRAD_VALUE, '0') >= '2' ";
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.STANDARD_SEQ, ";
                $withQ .= "     T1.SCHREGNO ";
                //提出あり
                $withQ .= " ), REP_DATA_PUT".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.STANDARD_SEQ, ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " FROM ";
                $withQ .= "     REP_PRESENT_DAT T1 ";
                $withQ .= " WHERE ";
                $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $withQ .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '".$subclassCd."' ";
                } else {
                    $withQ .= "     AND T1.SUBCLASSCD = '".$subclassCd."' ";
                }
                $withQ .= "     AND STANDARD_SEQ <= " .$model->searchField["REPORT".$cnt]. " ";
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.STANDARD_SEQ, ";
                $withQ .= "     T1.SCHREGNO ";
                //合格
                $withQ .= " ), REP_PASS_SUB".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.SCHREGNO, ";
                $withQ .= "     COUNT(*) AS CNT ";
                $withQ .= " FROM ";
                $withQ .= "     REP_DATA_PASS".$cnt." T1 ";
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " HAVING ";
                $withQ .= "     COUNT(*) = " .$model->searchField["REPORT".$cnt]. " ";
                //提出あり
                $withQ .= " ), REP_PUT_SUB".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.SCHREGNO, ";
                $withQ .= "     COUNT(*) AS CNT ";
                $withQ .= " FROM ";
                $withQ .= "     REP_DATA_PUT".$cnt." T1 ";
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " HAVING ";
                $withQ .= "     COUNT(*) = " .$model->searchField["REPORT".$cnt]. " ";

                //不合格・未提出あり
                $withQ .= " ), REP_MAIN".$cnt." AS ( ";
                $withQ .= " SELECT ";
                $withQ .= "     T1.SCHREGNO ";
                $withQ .= " FROM ";
                $withQ .= "     CHAIR_STD_DAT T1 ";
                $withQ .= " WHERE ";
                $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
                $withQ .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
                $withQ .= "     AND T1.CHAIRCD = '".$chairCd."' ";
                $withQ .= "     AND ( ";
                $withQ .= "         NOT EXISTS( ";
                $withQ .= "                     SELECT ";
                $withQ .= "                         'x' ";
                $withQ .= "                     FROM ";
                $withQ .= "                         REP_PASS_SUB".$cnt." T2 ";
                $withQ .= "                     WHERE ";
                $withQ .= "                         T1.SCHREGNO = T2.SCHREGNO ";
                $withQ .= "                 ) ";
                $withQ .= "         OR ";
                $withQ .= "         NOT EXISTS( ";
                $withQ .= "                     SELECT ";
                $withQ .= "                         'x' ";
                $withQ .= "                     FROM ";
                $withQ .= "                         REP_PUT_SUB".$cnt." T2 ";
                $withQ .= "                     WHERE ";
                $withQ .= "                         T1.SCHREGNO = T2.SCHREGNO ";
                $withQ .= "                 ) ";
                $withQ .= "         ) ";
                $withQ .= " GROUP BY ";
                $withQ .= "     T1.SCHREGNO ";
            }

            $withQ .= " ), REPORT".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     SCHREGNO, ";
            $withQ .= "     COUNT(*) AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     REP_MAIN".$cnt." ";
            $withQ .= " GROUP BY ";
            $withQ .= "     SCHREGNO ";

            $joinQ  .= "     LEFT JOIN REPORT".$cnt." ON T1.SCHREGNO = REPORT".$cnt.".SCHREGNO ";

            $whereQ .= $whereSep ? $whereSep : "(";
            $whereQ .= "VALUE(REPORT".$cnt.".CNT, 0) > 0 ";

            $whereSep = $whereJouken;
        }
        //得点、評定
        if (strlen($model->searchField["SCORE".$cnt]) > 0 || strlen($model->searchField["HYOUTEI".$cnt]) > 0) {
            $jouken = "";
            switch ($model->searchField["SCORE_S".$cnt]) {
                case "1":
                    $jouken = " = ";
                    break;
                case "2":
                    $jouken = " >= ";
                    break;
                case "3":
                    $jouken = " <= ";
                    break;
                default:
                    break;
            }

            $joinQ  .= "     LEFT JOIN SCORE".$cnt." ON T1.SCHREGNO = SCORE".$cnt.".SCHREGNO ";

            $withQ .= " ), SCORE".$cnt." AS ( ";
            $withQ .= " SELECT ";
            $withQ .= "     T1.SCHREGNO, ";
            $withQ .= "     COUNT(*) AS CNT ";
            $withQ .= " FROM ";
            $withQ .= "     RECORD_DAT T1 ";
            $withQ .= " WHERE ";
            $withQ .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
            $withQ .= "     AND T1.TAKESEMES = '0' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $withQ .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = '".$subclassCd."' ";
            } else {
                $withQ .= "     AND T1.SUBCLASSCD = '".$subclassCd."' ";
            }
            if (strlen($model->searchField["SCORE".$cnt]) > 0) {
                $withQ .= "     AND VALUE(T1.SEM" .CTRL_SEMESTER. "_TERM_SCORE, 0) ".$jouken.$model->searchField["SCORE".$cnt]. " ";

                $whereQ .= $whereSep ? $whereSep : "(";
                $whereQ .= "VALUE(SCORE".$cnt.".CNT, 0) > 0 ";

                $whereSep = $whereJouken;
            }
            $jouken = "";
            switch ($model->searchField["HYOUTEI_S".$cnt]) {
                case "1":
                    $jouken = " = ";
                    break;
                case "2":
                    $jouken = " >= ";
                    break;
                case "3":
                    $jouken = " <= ";
                    break;
                default:
                    break;
            }
            if (strlen($model->searchField["HYOUTEI".$cnt]) > 0) {
                $hyouteiField = CTRL_SEMESTER == "1" ? " VALUE(T1.SEM1_VALUE, 0) " : " VALUE(T1.GRAD_VALUE, 0) ";
                $withQ .= "     AND ".$hyouteiField.$jouken.$model->searchField["HYOUTEI".$cnt]. " ";

                $whereQ .= $whereSep ? $whereSep : "(";
                $whereQ .= "VALUE(SCORE".$cnt.".CNT, 0) > 0 ";

                $whereSep = $whereJouken;
            }
            $withQ .= " GROUP BY ";
            $withQ .= "     T1.SCHREGNO ";
        }

        $whereQ .= $whereQ ? ")" : "";

        return array($withQ, $joinQ, $whereQ);
    }

    //コメント取得
    public function getComment()
    {
        $query  = " SELECT ";
        $query .= "     REMARKID,REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_T_DAT ";

        return $query;
    }
    
    //DELETE(更新処理)
    public function getDeleteQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM HREPORTREMARK_T_DAT ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return ;
    }
    
    //UPDATE(更新処理)
    public function getUpdateQuery(&$model, $cnt)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["REMARKID"][TEXT]   = $cnt;
        $data["REMARK"][TEXT]     = $model->field["COMMENT".$cnt];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "SYSDATE()";

        $query = Query::insertSQL($data, "HREPORTREMARK_T_DAT");

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //科目名称取得
    public function getSubclassName($model, $subclassCd)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '".$subclassCd."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$subclassCd."' ";
        }

        return $query;
    }

    //CSVデータ出力
    public function selectCsvQuery($model, $schregNo)
    {
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_KANA ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "          AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "          AND T3.GRADE = T1.GRADE ";
        $query .= "          AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND T1.SEMESTER = '" .CTRL_SEMESTER. "' ";
        $query .= "     AND T1.SCHREGNO = '" .$schregNo. "' ";

        return $query;
    }
}
