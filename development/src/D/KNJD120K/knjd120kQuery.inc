<?php

require_once('for_php7.php');

class knjd120kquery extends Query
{

    //ABC評定科目
    public function getABCHyoutei()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= "     AND NAMECD1 = 'D065' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //編集可能学期の判別
    public function selectContolCodeQuery($model)
    {

        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '1' ";

        return $query;
    }

    //時間割講座テストより試験日を抽出
    public function selectExecuteDateQuery($chaircd, $semester, $testkindcd)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "    EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     TESTKINDCD = '".$testkindcd."' AND ";
        $query .= "     CHAIRCD = '".$chaircd."' ";
        $query .= " ORDER BY EXECUTEDATE ";
        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($model)
    {

        $query  = "   SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       t3.CLASSCD, ";
            $query .= "       t3.SCHOOL_KIND, ";
            $query .= "       t3.CURRICULUM_CD, ";
        }
        $query .= "       t3.SUBCLASSCD, ";
        $query .= "       t3.SUBCLASSABBV ";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2, ";
        $query .= "       SUBCLASS_MST t3 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       t1.YEAR = t2.YEAR AND ";
        $query .= "       t1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "       t1.SEMESTER = t2.SEMESTER AND ";
        $query .= "       t1.CHAIRCD = t2.CHAIRCD AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "       t2.STAFFCD = '".STAFFCD."' AND ";
        }
        $query .= "       SUBSTR(t3.SUBCLASSCD,1,2) NOT IN ('92','94') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       t1.CLASSCD       = t3.CLASSCD  AND ";
            $query .= "       t1.SCHOOL_KIND   = t3.SCHOOL_KIND  AND ";
            $query .= "       t1.CURRICULUM_CD = t3.CURRICULUM_CD  AND ";
        }
        $query .= "       t1.SUBCLASSCD = t3.SUBCLASSCD ";
        $query .= "   GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       t3.CLASSCD, ";
            $query .= "       t3.SCHOOL_KIND, ";
            $query .= "       t3.CURRICULUM_CD, ";
        }
        $query .= "       t3.SUBCLASSCD, ";
        $query .= "       t3.SUBCLASSABBV ";
        $query .= "   ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       t3.CLASSCD, ";
            $query .= "       t3.SCHOOL_KIND, ";
            $query .= "       t3.CURRICULUM_CD, ";
        }
        $query .= "       t3.SUBCLASSCD ";
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "   SELECT DISTINCT";
        $query .= "       t1.CHAIRCD, ";
        $query .= "       t1.CHAIRNAME ";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       t1.YEAR = t2.YEAR AND ";
        //$query .= "       t1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "       t1.SEMESTER = t2.SEMESTER AND ";
        $query .= "       t1.CHAIRCD = t2.CHAIRCD AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "       t2.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     t1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     t1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     t1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "       t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       t1.CHAIRCD ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        $query  = "";
        $query .= " WITH T_CHAIR AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "    C1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    C2.CLASSCD, ";
            $query .= "    C2.SCHOOL_KIND, ";
            $query .= "    C2.CURRICULUM_CD, ";
        }
        $query .= "    C2.SUBCLASSCD, ";
        $query .= "    C1.SCHREGNO, ";
        $query .= "    C1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "    CHAIR_STD_DAT C1, ";
        $query .= "    CHAIR_DAT C2 ";
        $query .= " WHERE ";
        $query .= "     C1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     C1.YEAR = C2.YEAR AND ";
        $query .= "     C1.SEMESTER = C2.SEMESTER AND ";
        $query .= "     C1.CHAIRCD = C2.CHAIRCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     C2.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     C2.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     C2.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     C2.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     C2.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     C1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " ) ";

        $query .= "     SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         w1.YEAR,w1.CLASSCD,w1.SCHOOL_KIND,w1.CURRICULUM_CD,w1.SUBCLASSCD,w1.SCHREGNO,w2.GRADE,w2.HR_NAMEABBV, ";
        } else {
            $query .= "         w1.YEAR,w1.SUBCLASSCD,w1.SCHREGNO,w2.GRADE,w2.HR_NAMEABBV, ";
        }
        $query .= "         w2.GRADE,w2.ATTENDNO,w1.NAME_SHOW,w1.CHAIRCD, ";
        $query .= "         w1.SEM1_INTER_REC,w1.SEM1_TERM_REC,w1.SEM1_REC, ";
        $query .= "         w1.SEM2_INTER_REC,w1.SEM2_TERM_REC,w1.SEM2_REC, ";
        $query .= "         w1.SEM3_TERM_REC, ";
        $query .= "         w1.SEM1_INTER_REC_FLG,w1.SEM1_TERM_REC_FLG,w1.SEM1_REC_FLG, ";
        $query .= "         w1.SEM2_INTER_REC_FLG,w1.SEM2_TERM_REC_FLG,w1.SEM2_REC_FLG, ";
        $query .= "         w1.SEM3_TERM_REC_FLG,w1.GRADE_RECORD, ";
        $query .= "         w1.A_PATTERN_ASSESS,w1.B_PATTERN_ASSESS,w1.C_PATTERN_ASSESS,w1.JUDGE_PATTERN, ";
//勤怠コードを追加
        $query .= "         w1.SEM1_INTER_REC_DI,w1.SEM1_TERM_REC_DI,w1.SEM2_INTER_REC_DI, ";
        $query .= "         w1.SEM2_TERM_REC_DI,w1.SEM3_TERM_REC_DI, ";
//2004/12/09 arakaki 近大-作業依頼書20041022-01
        $query .= "         w1.GRD_DATE, ";
//異動者情報を追加
        //$query .= "         w3.TRANSFERCD,w3.TRANSFER_SDATE,w3.TRANSFER_EDATE,w3.EDATE, ";
//該当講座の受講情報を追加
        $query .= "         w4.SEMESTER AS CHAIR_SEM1,w5.SEMESTER AS CHAIR_SEM2,w6.SEMESTER AS CHAIR_SEM3 ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "       ,W7.PROV_FLG ";
        }
        $query .= "     FROM ";
        $query .= "         (SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             t2.YEAR,t2.CLASSCD,t2.SCHOOL_KIND,t2.CURRICULUM_CD,t2.SUBCLASSCD,t2.SCHREGNO,t2.CHAIRCD, ";
        } else {
            $query .= "             t2.YEAR,t2.SUBCLASSCD,t2.SCHREGNO,t2.CHAIRCD, ";
        }
        $query .= "             t1.SEM1_INTER_REC,t1.SEM1_TERM_REC,t1.SEM1_REC, ";
        $query .= "             t1.SEM2_INTER_REC,t1.SEM2_TERM_REC,t1.SEM2_REC, ";
        $query .= "             t1.SEM3_TERM_REC, ";
        $query .= "             t1.SEM1_INTER_REC_FLG,t1.SEM1_TERM_REC_FLG,t1.SEM1_REC_FLG, ";
        $query .= "             t1.SEM2_INTER_REC_FLG,t1.SEM2_TERM_REC_FLG,t1.SEM2_REC_FLG, ";
        $query .= "             t1.SEM3_TERM_REC_FLG, ";
        $query .= "             t1.GRADE_RECORD, ";
        $query .= "             t1.A_PATTERN_ASSESS,t1.B_PATTERN_ASSESS,t1.C_PATTERN_ASSESS,t1.JUDGE_PATTERN,t3.NAME_SHOW ";
//勤怠コードを追加
        $query .= "            ,T1.SEM1_INTER_REC_DI,T1.SEM1_TERM_REC_DI,T1.SEM2_INTER_REC_DI,T1.SEM2_TERM_REC_DI,T1.SEM3_TERM_REC_DI";
//2004/12/09 arakaki 近大-作業依頼書20041022-01｢卒業･退学･転学チェック｣
        $query .= "            ,t3.GRD_DATE";
        $query .= "         FROM ";
        $query .= "             T_CHAIR t2 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST t3 ON t3.SCHREGNO = t2.SCHREGNO ";
        $query .= "             LEFT JOIN KIN_RECORD_DAT t1 ON ";
        $query .= "                 t1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             t1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "             t1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "             t1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "             t1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "             t1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "                 t1.SCHREGNO = t2.SCHREGNO ";
        $query .= "         )w1  ";
        $query .= "         LEFT OUTER JOIN (SELECT ";
        $query .= "             t1.YEAR,t1.SEMESTER,t1.SCHREGNO,t1.GRADE,t1.ATTENDNO,t2.HR_NAMEABBV ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT t1, ";
        $query .= "             SCHREG_REGD_HDAT t2 ";
        $query .= "         WHERE ";
        $query .= "             t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "             t1.YEAR = t2.YEAR AND ";
        $query .= "             t1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "             t1.SEMESTER = t2.SEMESTER AND ";
        $query .= "             t1.GRADE = t2.GRADE AND ";
        $query .= "             t1.HR_CLASS = t2.HR_CLASS ";
        $query .= "         )w2  ";
        $query .= "         ON w2.YEAR = w1.YEAR AND w2.SCHREGNO = w1.SCHREGNO ";
//異動者情報の取得
/*        $query .= "         LEFT OUTER JOIN (SELECT DISTINCT ";
        $query .= "             t1.YEAR,t1.SCHREGNO,t2.TRANSFERCD,t2.TRANSFER_SDATE,";
        $query .= "             t2.TRANSFER_EDATE,t3.EDATE";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT t1, ";
        $query .= "             SCHREG_TRANSFER_DAT t2, ";
        $query .= "             SEMESTER_MST t3 ";
        $query .= "         WHERE ";
        $query .= "             t1.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "             t2.TRANSFERCD IN ('1', '2', '3') AND ";
        $query .= "             t1.YEAR = T3.YEAR AND ";
        $query .= "             t3.SEMESTER = '9' ";
        $query .= "         )w3 ";
        $query .= "         ON w3.YEAR = w1.YEAR AND w3.SCHREGNO = w1.SCHREGNO ";
*/
//学期毎の該当講座の受講情報を取得
        $query .= "         LEFT OUTER JOIN ";
        $query .= "             CHAIR_STD_DAT w4 ";
        $query .= "         ON ";
        $query .= "             (w4.YEAR = w1.YEAR AND w4.SCHREGNO = w1.SCHREGNO AND ";
        $query .= "              w4.CHAIRCD = w1.CHAIRCD AND w4.SEMESTER = '1') ";
        $query .= "         LEFT OUTER JOIN ";
        $query .= "             CHAIR_STD_DAT w5 ";
        $query .= "         ON ";
        $query .= "             (w5.YEAR = w1.YEAR AND w5.SCHREGNO = w1.SCHREGNO AND ";
        $query .= "              w5.CHAIRCD = w1.CHAIRCD AND w5.SEMESTER = '2') ";
        $query .= "         LEFT OUTER JOIN ";
        $query .= "             CHAIR_STD_DAT w6 ";
        $query .= "         ON ";
        $query .= "             (w6.YEAR = w1.YEAR AND w6.SCHREGNO = w1.SCHREGNO AND ";
        $query .= "              w6.CHAIRCD = w1.CHAIRCD AND w6.SEMESTER = '3') ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " LEFT JOIN ";
            $query .= "    (SELECT  SCHREGNO, PROV_FLG ";
            $query .= "     FROM   RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
                $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
                $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
                $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
            } else {
                $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "    ) W7 ";
            $query .= "   ON W7.SCHREGNO = W1.SCHREGNO ";
        }
        $query .= "     ORDER BY ";
        $query .= "         w2.HR_NAMEABBV,w2.ATTENDNO ";
        return $query;
    }

    //異動者情報の取得
    public function getTransferData($year, $schregno)
    {

        $query .= " SELECT DISTINCT ";
        $query .= "     t2.TRANSFERCD, ";
        $query .= "     t2.TRANSFER_SDATE, ";
        $query .= "     t2.TRANSFER_EDATE, ";
        $query .= "     t3.EDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1, ";
        $query .= "     SCHREG_TRANSFER_DAT t2, ";
        $query .= "     SEMESTER_MST t3 ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = t2.SCHREGNO AND ";
        $query .= "     t2.TRANSFERCD IN ('1', '2', '3') AND ";
        $query .= "     t1.YEAR = T3.YEAR AND ";
        $query .= "     t3.SEMESTER = '9' AND ";
        $query .= "     t1.YEAR = '".$year."' AND ";
        $query .= "     t1.SCHREGNO = '".$schregno."'";

        return $query;
    }

    //文字評定の取得
    public function getAssess($grade, $subclasscd, $score, $row, $model)
    {

        $query .= " SELECT ";
        $query .= "     ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$row["CLASSCD"]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$row["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     ASSESSCD = '3' AND ";
        $query .= "     ASSESSLOW <= DECIMAL(".$score.") AND ";
        $query .= "     ASSESSHIGH >= DECIMAL(".$score.") ";

        return $query;
    }

    //相対評定の取得
    public function selectAssessLevel($grade, $subclasscd, $row, $model)
    {

        $query .= " SELECT ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$row["CLASSCD"]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$row["CURRICULUM_CD"]."' AND ";
        }
        $query .= "     SUBCLASSCD = '".$subclasscd."' AND ";
        $query .= "     ASSESSCD = '3' ";
        $query .= " ORDER BY ";
        $query .= "     ASSESSLEVEL ";

        return $query;
    }

    //テスト実施回数取得 NO001
    public function getSchTest($model)
    {

        $query  = " WITH CHR_TEST_TABLE AS ( ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     SEMESTER, ";
        $query .= "     SUM(SMALLINT(TESTKINDCD)) AS TESTSUM ";
        $query .= " FROM ";
        $query .= "     CHR_TEST_TABLE ";
        $query .= " GROUP BY ";
        $query .= "     CHAIRCD, ";
        $query .= "     SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        //0から100までの数値以外で有り得る値(空または出欠情報)
        $vals = array("","KK","KS");

        //学期成績データ項目名を配列にする。
        $term = array("SEM1_INTER_REC","SEM1_TERM_REC","SEM2_INTER_REC","SEM2_TERM_REC","SEM3_TERM_REC");

        $testArray = array("1","3");//中間テストがある
        $kkksArray = array("KK","KS");

        for ($i=0; $i<get_count($model->data["SCHREGNO"]); $i++) {
            $sem1InterRec   = $model->fields["SEM1_INTER_REC"][$i];//中間
            $sem1TermRec    = $model->fields["SEM1_TERM_REC"][$i];//期末
            $sem1InterRecDi = $model->fields["SEM1_INTER_REC_DI"][$i];//出欠(中間)
            //1学期：「中間または期末が編集可」かつ「学期平均が編集不可」の場合、学期平均を算出
            if ((in_array("0101", $model->control_cd) || in_array("0102", $model->control_cd)) && !in_array("0103", $model->control_cd)) {
                //期末に点数が入っている場合
                if (strlen($sem1TermRec)) {
                    //中間に点数が入っている場合、学期平均＝中間、期末の平均
                    if (strlen($sem1InterRec)) {
                        $model->fields["SEM1_REC"][$i] = round(($sem1InterRec + $sem1TermRec) / 2);
                    //中間がブランクまたはKK/KSの場合、学期平均＝ブランク。
                    } elseif (in_array($model->testkind["SEM1"], $testArray) || in_array($sem1InterRecDi, $kkksArray)) {
                        $model->fields["SEM1_REC"][$i] = "";
                    //中間テストが無い科目の場合、学期平均＝期末。
                    } else {
                        $model->fields["SEM1_REC"][$i] = $sem1TermRec;
                    }
                //期末に点数が入っていない場合、学期平均＝ブランク
                } else {
                    $model->fields["SEM1_REC"][$i] = "";
                }
            }

            $sem2InterRec   = $model->fields["SEM2_INTER_REC"][$i];//中間
            $sem2TermRec    = $model->fields["SEM2_TERM_REC"][$i];//期末
            $sem2InterRecDi = $model->fields["SEM2_INTER_REC_DI"][$i];//出欠(中間)
            //2学期：「中間または期末が編集可」かつ「学期平均が編集不可」の場合、学期平均を算出
            if ((in_array("0201", $model->control_cd) || in_array("0202", $model->control_cd)) && !in_array("0203", $model->control_cd)) {
                //期末に点数が入っている場合
                if (strlen($sem2TermRec)) {
                    //中間に点数が入っている場合、学期平均＝中間、期末の平均
                    if (strlen($sem2InterRec)) {
                        $model->fields["SEM2_REC"][$i] = round(($sem2InterRec + $sem2TermRec) / 2);
                    //中間がブランクまたはKK/KSの場合、学期平均＝ブランク。
                    } elseif (in_array($model->testkind["SEM2"], $testArray) || in_array($sem2InterRecDi, $kkksArray)) {
                        $model->fields["SEM2_REC"][$i] = "";
                    //中間テストが無い科目の場合、学期平均＝期末。
                    } else {
                        $model->fields["SEM2_REC"][$i] = $sem2TermRec;
                    }
                //期末に点数が入っていない場合、学期平均＝ブランク
                } else {
                    $model->fields["SEM2_REC"][$i] = "";
                }
            }

        //学年成績の算出
            if (!in_array("8003", $model->control_cd) && !$model->syncdFlg) {
              //生徒の学年
                $schreg_grade = $model->data["SCHREG_GRADE"][$model->data["SCHREGNO"][$i]];

              //１,２年生の場合
                if ($schreg_grade == "01" || $schreg_grade == "02") {
                //留学情報を持っている場合
                    if (in_array("0302", $model->control_cd) && $model->data["TRANSFERCD"][$model->data["SCHREGNO"][$i]] == "1") {
                        if ($model->fields["SEM1_REC"][$i] >="0" && $model->fields["SEM2_REC"][$i] >="0" && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                  //１,２,３学期の平均値をセット
                            $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM1_REC"][$i]+$model->fields["SEM2_REC"][$i]+$model->fields["SEM3_TERM_REC"][$i])/3);
                        } elseif ($model->fields["SEM1_REC"][$i] >="0" && $model->fields["SEM2_REC"][$i] >="0" && in_array($model->fields["SEM3_TERM_REC"][$i], $vals)) {
                //１,２学期のみの成績だけが入っている場合は１,２学期の平均値をセット
                            $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM1_REC"][$i]+$model->fields["SEM2_REC"][$i])/2);
                        } elseif (!strlen($model->fields["SEM1_REC"][$i]) && $model->fields["SEM2_REC"][$i] >="0" && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                //２,３学期のみの成績だけが入っている場合は２,３学期の平均値をセット
                            $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM2_REC"][$i]+$model->fields["SEM3_TERM_REC"][$i])/2);
                        } else {
                            $model->fields["GRADE_RECORD"][$i] = "";
                        }
                    } else {
                        //１,２,３学期末に点数が入っていれば学年成績算出
                        if ($model->fields["SEM1_REC"][$i] >="0" && $model->fields["SEM2_REC"][$i] >="0" && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                            $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM1_REC"][$i]+$model->fields["SEM2_REC"][$i]+$model->fields["SEM3_TERM_REC"][$i])/3);
                        } else {
                            $model->fields["GRADE_RECORD"][$i] = "";
                        }
                    }
              //３年生の場合
                } elseif ($schreg_grade == "03") {
                    if (in_array("0202", $model->control_cd) && $model->fields["SEM1_REC"][$i] >= "0" && !strlen($model->fields["SEM2_REC"][$i])) {
                      //「２学期期末が編集可能」かつ「１学期成績あり、２学期成績なし」は「学年成績なし」として学年成績、学年評定をNULLにする
                        $model->fields["GRADE_RECORD"][$i] = "";
                    } elseif ($model->fields["SEM1_REC"][$i] >="0" && $model->fields["SEM2_REC"][$i] >="0" && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                      //１,２,３学期の平均値をセット
                        $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM1_REC"][$i]+$model->fields["SEM2_REC"][$i]+$model->fields["SEM3_TERM_REC"][$i])/3);
                    } elseif ($model->fields["SEM1_REC"][$i] >="0" && !strlen($model->fields["SEM2_REC"][$i]) && in_array($model->fields["SEM3_TERM_REC"][$i], $vals) ) {
                      //１学期のみの成績だけが入っている場合は１学期の値をセット
                        $model->fields["GRADE_RECORD"][$i] = $model->fields["SEM1_REC"][$i];
                    } elseif (!strlen($model->fields["SEM1_REC"][$i]) && $model->fields["SEM2_REC"][$i] >="0" && in_array($model->fields["SEM3_TERM_REC"][$i], $vals)) {
                      //２学期のみの成績だけが入っている場合は２学期の値をセット
                        $model->fields["GRADE_RECORD"][$i] = $model->fields["SEM2_REC"][$i];
                    } elseif (!strlen($model->fields["SEM1_REC"][$i]) && !strlen($model->fields["SEM2_REC"][$i]) && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                      //３学期のみの成績だけが入っている場合は３学期の値をセット
                        $model->fields["GRADE_RECORD"][$i] = $model->fields["SEM3_TERM_REC"][$i];
                    } elseif ($model->fields["SEM1_REC"][$i] >="0" && $model->fields["SEM2_REC"][$i] >="0" && in_array($model->fields["SEM3_TERM_REC"][$i], $vals)) {
                      //１,２学期のみの成績だけが入っている場合は１,２学期の平均値をセット
                        $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM1_REC"][$i]+$model->fields["SEM2_REC"][$i])/2);
                    } elseif ($model->fields["SEM1_REC"][$i] >="0" && !strlen($model->fields["SEM2_REC"][$i]) && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                      //１,３学期のみの成績だけが入っている場合は１,３学期の平均値をセット
                        $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM1_REC"][$i]+$model->fields["SEM3_TERM_REC"][$i])/2);
                    } elseif (!strlen($model->fields["SEM1_REC"][$i]) && $model->fields["SEM2_REC"][$i] >="0" && $model->fields["SEM3_TERM_REC"][$i] >="0" ) {
                      //２,３学期のみの成績だけが入っている場合は２,３学期の平均値をセット
                        $model->fields["GRADE_RECORD"][$i] = round(($model->fields["SEM2_REC"][$i]+$model->fields["SEM3_TERM_REC"][$i])/2);
                    } else {
                        $model->fields["GRADE_RECORD"][$i] = "";
                    }
                } else {
                    $model->fields["GRADE_RECORD"][$i] = "";
                }
            }


            $pattern_asses=array();
            if ($model->fields["GRADE_RECORD"][$i]>="0" && !$model->syncdFlg) {
                $query  = "     SELECT ";
                $query .= "         TYPE_ASSES_CD, ";
                $query .= "         TYPE_ASSES_LEVEL ";
                $query .= "     FROM ";
                $query .= "         TYPE_ASSES_MST ";
                $query .= "     WHERE ";
                $query .= "         YEAR = '".CTRL_YEAR."' AND ";
                $query .= "         ".round($model->fields["GRADE_RECORD"][$i])." <= TYPE_ASSES_HIGH AND ";
                $query .= "         ".round($model->fields["GRADE_RECORD"][$i])." >= TYPE_ASSES_LOW ";

                $result = $db->query($query);
                while ($rows = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $pattern_asses[$rows["TYPE_ASSES_CD"]] = $rows["TYPE_ASSES_LEVEL"];
                }
            } else {
                   $pattern_asses=array();
            }
            $data = array();

            $data["YEAR"][TEXT]                 = CTRL_YEAR ;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]        = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]           = $model->field["SUBCLASSCD"];
            }
            $data["SCHREGNO"][TEXT]             = $model->data["SCHREGNO"][$i];

            $data["SEM1_INTER_REC_DI"][TEXT] = $model->fields["SEM1_INTER_REC_DI"][$i];      //出欠情報
            $data["SEM1_TERM_REC_DI"][TEXT]  = $model->fields["SEM1_TERM_REC_DI"][$i];
            $data["SEM2_INTER_REC_DI"][TEXT] = $model->fields["SEM2_INTER_REC_DI"][$i];
            $data["SEM2_TERM_REC_DI"][TEXT]  = $model->fields["SEM2_TERM_REC_DI"][$i];
            $data["SEM3_TERM_REC_DI"][TEXT]  = $model->fields["SEM3_TERM_REC_DI"][$i];

            $data["SEM1_INTER_REC"][NUMBER]  = $model->fields["SEM1_INTER_REC"][$i];          //1学期中間成績
            $data["SEM1_TERM_REC"][NUMBER]   = $model->fields["SEM1_TERM_REC"][$i];           //1学期期末成績
            $data["SEM1_REC"][NUMBER]        = $model->fields["SEM1_REC"][$i];                //1学期成績

            $data["SEM2_INTER_REC"][NUMBER]  = $model->fields["SEM2_INTER_REC"][$i];          //2学期中間成績
            $data["SEM2_TERM_REC"][NUMBER]   = $model->fields["SEM2_TERM_REC"][$i];           //2学期期末成績
            $data["SEM2_REC"][NUMBER]        = $model->fields["SEM2_REC"][$i];                //2学期成績

            $data["SEM3_TERM_REC"][NUMBER]   = $model->fields["SEM3_TERM_REC"][$i];           //3学期期末成績

            $data["SEM1_INTER_REC_FLG"][TEXT]   = $model->flg_data_ar["SEM1_INTER_REC_FLG"][$i];  //1学期中間成績_フラグ
            $data["SEM1_TERM_REC_FLG"][TEXT]    = $model->flg_data_ar["SEM1_TERM_REC_FLG"][$i];   //1学期期末成績_フラグ
            $data["SEM1_REC_FLG"][TEXT]         = $model->flg_data_ar["SEM1_REC_FLG"][$i];        //1学期成績_フラグ
            $data["SEM2_INTER_REC_FLG"][TEXT]   = $model->flg_data_ar["SEM2_INTER_REC_FLG"][$i];  //2学期中間成績_フラグ
            $data["SEM2_TERM_REC_FLG"][TEXT]    = $model->flg_data_ar["SEM2_TERM_REC_FLG"][$i];   //2学期期末成績_フラグ
            $data["SEM2_REC_FLG"][TEXT]         = $model->flg_data_ar["SEM2_REC_FLG"][$i];        //2学期成績_フラグ
            $data["SEM3_TERM_REC_FLG"][TEXT]    = $model->flg_data_ar["SEM3_TERM_REC_FLG"][$i];   //3学期期末成績_フラグ
            $data["SEM3_REC_FLG"][TEXT]         = $model->flg_data_ar["SEM3_REC_FLG"][$i];        //3学期成績_フラグ

            $data["GRADE_RECORD"][NUMBER]    = $model->fields["GRADE_RECORD"][$i];                //学年成績

            //類型別学年評定
            $data["A_PATTERN_ASSESS"][TEXT]  = $pattern_asses["A"];
            $data["B_PATTERN_ASSESS"][TEXT]  = $pattern_asses["B"];
            $data["C_PATTERN_ASSESS"][TEXT]  = $pattern_asses["C"];

            $data["REGISTERCD"][TEXT]        = STAFFCD ;                                           //登録者コード
            $data["UPDATED"][FUNC]           = "sysdate()";                                        //更新日付

            $where  = " WHERE YEAR           = '".CTRL_YEAR."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "       CLASSCD          = '".$subclass_array[0]."' AND ";
                $where .= "       SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
                $where .= "       CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
                $where .= "       SUBCLASSCD       = '".$subclass_array[3]."' AND ";
            } else {
                $where .= "       SUBCLASSCD     = '".$model->field["SUBCLASSCD"]."' AND ";
            }
            $where .= "       SCHREGNO       = '".$model->data["SCHREGNO"][$i]."' ";

            //追加・更新
            $query = " SELECT COUNT(*) FROM KIN_RECORD_DAT " . $where;
            if (0 < $db->getOne($query)) {
                $query = Query::updateSQL($data, "KIN_RECORD_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "KIN_RECORD_DAT");
            }
            $db->query($query);

            //評定フラグ・・・仮評定(1)、本評定(NULL)
            //学年評定を更新する場合、評定フラグは本評定(NULL)とする・・・レコードを削除またはNULLで更新(どちらでもよい)
            if ($model->fields["GRADE_RECORD"][$i]>="0" && !$model->syncdFlg) {
                //仮評定フラグ対応
                if ($model->Properties["useProvFlg"] == '1') {
                    $query  = "";
                    $query .= "DELETE ";
                    $query .= "FROM   RECORD_PROV_FLG_DAT ";
                    $query .= "WHERE  YEAR          = '".CTRL_YEAR."' ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $query .= "  AND  CLASSCD       = '".$subclass_array[0]."' ";
                        $query .= "  AND  SCHOOL_KIND   = '".$subclass_array[1]."' ";
                        $query .= "  AND  CURRICULUM_CD = '".$subclass_array[2]."' ";
                        $query .= "  AND  SUBCLASSCD    = '".$subclass_array[3]."' ";
                    } else {
                        $query .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
                    }
                    $query .= "  AND  SCHREGNO      = '{$model->data["SCHREGNO"][$i]}' ";

                    //$db->query($query);
                    if ($model->fields["PROV_FLG"][$i] == '1') {
                        $data = array();
                        $data["YEAR"][TEXT]         = CTRL_YEAR;
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $data["CLASSCD"][TEXT]       = $subclass_array[0];
                            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
                            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
                            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
                        } else {
                            $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
                        }
                        $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                        //評定フラグ・・・仮評定(1)、本評定(NULL)
                        $data["PROV_FLG"][TEXT]     = "1";
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";

                        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
                        //$db->query($query);
                    }
                }
            }
        }

## 2005/10/19 20:55 "成績入力終了"処理 追加修正 ##

        $chkDbValue = array( "1" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "2" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "3" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "2") ,
                             "4" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "2") ,
                             "5" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "3") );

        //管理者コントロール
        $query = knjd120kQuery::selectContolCodeQuery($model);
        $results = $db->query($query);
        while ( $row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sem = sprintf("%1d", substr($row_ct["CONTROL_CODE"], "0", "2"));
            $t_kind = substr($row_ct["CONTROL_CODE"], "3", "2");
            $admin_key[$sem][$t_kind]=$row_ct["CONTROL_CODE"];
        }
        $results->free();

        //disable flag
        $ableflg[1] = (isset($admin_key[1][1]))? "" : "disabled" ;
        $ableflg[2] = (isset($admin_key[1][2]))? "" : "disabled" ;
        $ableflg[3] = (isset($admin_key[2][1]))? "" : "disabled" ;
        $ableflg[4] = (isset($admin_key[2][2]))? "" : "disabled" ;
        $ableflg[5] = (isset($admin_key[3][2]))? "" : "disabled" ;

        for ($i=1; $i<=5; $i++) {
            if ($ableflg[$i] == "disabled") {
                continue;
            }

            $chkfg = ($model->field["CHK_COMP".$i] == "on")? "1" : "0" ;

            $data = array();
            $data["EXECUTED"][TEXT]     = $chkfg ;       //成績入力区分
            $data["REGISTERCD"][TEXT]   = STAFFCD ;      //登録者コード
            $data["UPDATED"][FUNC]      = "sysdate()";   //更新日付

            $where  = "";
            $where  = " WHERE YEAR       = '".CTRL_YEAR."' AND ";
            $where .= "       TESTKINDCD = '".$chkDbValue[$i]["TESTKINDCD"]."' AND ";
            $where .= "       TESTITEMCD = '".$chkDbValue[$i]["TESTITEMCD"]."' AND ";
            $where .= "       CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
            $where .= "       SEMESTER   = '".$chkDbValue[$i]["SEMETER"]."' ";

            $query = "";
            $query = Query::updateSQL($data, "SCH_CHR_TEST", $where);

            $db->query($query);
        }

## 2005/10/19 20:55 おわり ##


        Query::dbCheckIn($db);
    }


## 2005/10/19 20:55 "成績入力終了"処理 追加修正 ##

    //テーブルSchChrTestのデータ抽出SQL
    public function getSchChrTestData($charcd)
    {

        $query = "
            SELECT SEMESTER || TESTKINDCD || TESTITEMCD as TESTKIND, EXECUTED FROM SCH_CHR_TEST
               WHERE CHAIRCD = '".$charcd."'
               AND YEAR = '".CTRL_YEAR."'
               AND ((TESTKINDCD = '01' AND TESTITEMCD = '01') OR (TESTKINDCD = '02' AND TESTITEMCD = '01') )
        ";

        return $query;
    }

## 2005/10/19 20:55 おわり ##
}
