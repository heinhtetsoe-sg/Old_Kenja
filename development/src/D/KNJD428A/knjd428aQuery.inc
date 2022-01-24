<?php

require_once('for_php7.php');

class knjd428aQuery extends Query {

    //校種取得
    function getSchKindFromGrade($model) {
        $query  = " SELECT ";
        $query .= "   T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->exp_year."' ";
        $query .= "   AND T1.SEMESTER = '".$model->semester."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."'";
        return $query;
    }

    //学期制取得
    function getSemesterDiv($model, $schkind) {
        $query  = " SELECT ";
        $query .= "   SEMESTERDIV ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$model->exp_year."' ";
        $query .= "   AND SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND SCHOOL_KIND = '".$schkind."' ";
        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }
    
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "   T2.GRADE_CD ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->exp_year."' ";
        $query .= "   AND T1.SEMESTER = '".$model->semester."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."'";
        return $query;
    }

    //１レコード取得 -- HREPORTREMARK_DAT
    function getRow($model) {
        $query .= " SELECT ";
        $query .= "   T2.REMARK1 AS REMARK2_1, ";
        $query .= "   T2.REMARK2 AS REMARK2_2, ";
        $query .= "   T2.REMARK3 AS REMARK2_3, ";
        $query .= "   T2.FOREIGNLANGACT AS FOREIGNLANGACT2_1, ";
        $query .= "   T2.COMMUNICATION AS COMMUNICATION2_1, ";
        $query .= "   T2.TOTALSTUDYTIME AS TOTALSTUDYTIME2_1, ";
        $query .= "   T2.SPECIALACTREMARK AS SPECIALACTREMARK2_1, ";
        $query .= "   T2_9.TOTALSTUDYTIME AS TOTALSTUDYTIMEF9, ";
        $query .= "   T2_9.SPECIALACTREMARK AS SPECIALACTREMARKF9, ";
        $query .= "   T3_1.REMARK1 AS REMARKF3_1, ";
        $query .= "   T3_2.REMARK1 AS REMARKF3_2, ";
        $query .= "   T3_3.REMARK1 AS REMARKF3_3, ";
        $query .= "   T4_1.REMARK1 AS REMARK4_1, ";
        $query .= "   T4_1.REMARK2 AS REMARK4_2, ";
        $query .= "   T2.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "  SCHREG_REGD_DAT T1 ";
        $query .= "  LEFT JOIN  HREPORTREMARK_DAT T2 ";
        $query .= "    ON T2.YEAR = T1.YEAR ";
        $query .= "   AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "   AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "  LEFT JOIN  HREPORTREMARK_DAT T2_9 ";
        $query .= "    ON T2_9.YEAR = T1.YEAR ";
        $query .= "   AND T2_9.SEMESTER = '9' ";
        $query .= "   AND T2_9.SCHREGNO = T1.SCHREGNO ";
        $query .= "  LEFT JOIN  HREPORTREMARK_DETAIL_DAT T3_1 ";
        $query .= "    ON T3_1.YEAR = T1.YEAR ";
        $query .= "   AND T3_1.SEMESTER = '9' ";
        $query .= "   AND T3_1.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T3_1.DIV = '01' ";
        $query .= "   AND T3_1.CODE = '01' ";
        $query .= "  LEFT JOIN  HREPORTREMARK_DETAIL_DAT T3_2 ";
        $query .= "    ON T3_2.YEAR = T1.YEAR ";
        $query .= "   AND T3_2.SEMESTER = '9' ";
        $query .= "   AND T3_2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T3_2.DIV = '01' ";
        $query .= "   AND T3_2.CODE = '02' ";
        $query .= "  LEFT JOIN  HREPORTREMARK_DETAIL_DAT T3_3 ";
        $query .= "    ON T3_3.YEAR = T1.YEAR ";
        $query .= "   AND T3_3.SEMESTER = '9' ";
        $query .= "   AND T3_3.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T3_3.DIV = '01' ";
        $query .= "   AND T3_3.CODE = '03' ";
        $query .= "  LEFT JOIN  HREPORTREMARK_DETAIL_DAT T4_1 ";
        $query .= "    ON T4_1.YEAR = T1.YEAR ";
        $query .= "   AND T4_1.SEMESTER = T1.SEMESTER ";
        $query .= "   AND T4_1.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T4_1.DIV = '02' ";
        $query .= "   AND T4_1.CODE = '01' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->exp_year."' ";
        $query .= "   AND T1.SEMESTER = '".$model->semester."'";
        $query .= "   AND T1.SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    //校種 取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "       ON GDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND GDAT.GRADE    = REGD.GRADE ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ";
        $query .= "       ON HDAT.YEAR     = REGD.YEAR ";
        $query .= "      AND HDAT.SEMESTER = REGD.SEMESTER ";
        $query .= "      AND HDAT.GRADE    = REGD.GRADE ";
        $query .= "      AND HDAT.HR_CLASS = REGD.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR     = '{$model->exp_year}' ";
        $query .= " AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= " AND REGD.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND HDAT.GRADE    = '{$model->grade}' ";
        $query .= " AND HDAT.HR_CLASS = '{$model->hrClass}' ";

        return $query;
    }

    //HREPORT_CONDITION_DAT 取得
    function getReportCondition($model,$schoolKind,$seq) {
        $query  = " SELECT ";
        $query .= "   T1.REMARK1 ";
        $query .= " FROM ";
        $query .= "   HREPORT_CONDITION_DAT T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND T1.SCHOOL_KIND = '".$schoolKind."' ";
        $query .= "   AND T1.GRADE = '00' ";
        $query .= "   AND T1.COURSECD = '0' ";
        $query .= "   AND T1.MAJORCD = '000' ";
        $query .= "   AND T1.COURSECODE = '0000' ";
        $query .= "   AND T1.SEQ = '".$seq."' ";

        return $query;
    }

    //データチェック
    function getHreportRemarkCnt($model, $tblname, $semester, $div="", $code="")
    {
        //テーブル取得
        $table = "";
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     {$tblname} ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' ";
        $query .= " AND SEMESTER = '".$semester."' ";
        $query .= " AND SCHREGNO = '".$model->schregno."' ";
        if ($div != "") {
            $query .= " AND DIV = '".$div."' ";
        }
        if ($code != "") {
            $query .= " AND CODE = '".$code."' ";
        }

        return $query;
    }


    //UPDATE処理
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();

        //(今年度今学期の)選択生徒の校種を割り出す。
        $schkind = $db->getOne(knjd428aQuery::getSchKindFromGrade($model));
        //2学期制/3学期制をDBから取得。
        $semesdiv = $db->getOne(knjd428aQuery::getSemesterDiv($model, $schkind));

        //以下、3学期制/2学期制、校種により設定項目、取得元、学期('9'固定or学期指定)が変化するので注意。

        //HREPORTREMARK_DAT(各学期毎)
        $data = array();
        //// 1:P3/J3       P2/J2 道徳
        if ($schkind == "P" || $schkind == "J") {
            //H_REMARK1:各学期毎
            $data["REMARK1"][TEXT] = $model->field["REMARK1_1"];
        }
        //// 2:P3          P2 外国語活動
        if ($schkind == "P") {
            //H_FOREIGNLANGACT:各学期毎
            $data["FOREIGNLANGACT"][TEXT] = $model->field["REMARK2_1"];
        }
        //// 3:P3/J3/H3    P2(*)/J2(*)/H2(*) 自立活動 *DETAILに登録
        if ($semesdiv == "3") {
            //H_REMARK3:各学期毎
            $data["REMARK3"][TEXT] = $model->field["REMARK3_1"];
        }
        //// 4:P3/J3       P2/J2/H2 総合的な学習の時間 or 特別活動
        if ($semesdiv == "2") {
            //H_SPECIALACT:各学期毎
            $data["SPECIALACTREMARK"][TEXT] = $model->field["REMARK4_1"];
            //H_TOTALSTUDYTIME:各学期毎
            $data["TOTALSTUDYTIME"][TEXT] = $model->field["REMARK4_2"];
        }
        //// 6:P3/J3/H3    P2/J2/H2
        //H_COMMUNICATION:各学期毎
        $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
        $data["COMMUNICATION"][TEXT] = $model->field["REMARK6_1"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "sysdate()";
        $cnt = $db->getOne(knjd428aQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DAT", $model->semester));
        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' ";
            $where .= " AND SEMESTER = '".$model->semester."' ";
            $where .= " AND SCHREGNO = '".$model->schregno."' ";

            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            $db->query($query);
        } else {
            $data["YEAR"][TEXT] = $model->exp_year;
            $data["SEMESTER"][TEXT] = $model->semester;
            $data["SCHREGNO"][TEXT] = $model->schregno;

            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            $db->query($query);
        }


        //HREPORTREMARK_DAT('9'学期固定)
        $data = array();
        //// 4:P3/J3       P2/J2/H2 総合的な学習の時間 or 特別活動
        if ($schkind != "H" && $semesdiv == "3") {
            //H_SPECIALACT:通年入力('9'固定)
            $data["SPECIALACTREMARK"][TEXT] = $model->field["REMARK4_1"];
            //H_TOTALSTUDYTIME:通年入力('9'固定)
            $data["TOTALSTUDYTIME"][TEXT] = $model->field["REMARK4_2"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";

            $cnt = $db->getOne(knjd428aQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DAT", "9"));
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '9' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT] = $model->exp_year;
                $data["SEMESTER"][TEXT] = "9";
                $data["SCHREGNO"][TEXT] = $model->schregno;

                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
                $db->query($query);
            }
        }


        //HREPORTREMARK_DETAIL_DAT(DIV=2)
        $data = array();
        //// 3:P3/J3/H3    P2(*)/J2(*)/H2(*) 自立活動 *DETAILに登録
        if ($semesdiv == "2") {
            //HD_02_01_REMARK1:各学期毎
            $data["REMARK1"][TEXT] = $model->field["REMARK3_1"];
            //HD_02_01_REMARK2:各学期毎
            $data["REMARK2"][TEXT] = $model->field["REMARK3_2"];

            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";

            $cnt = $db->getOne(knjd428aQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", $model->semester, "02", "01"));
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '".$model->semester."' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";
                $where .= " AND DIV      = '02' ";
                $where .= " AND CODE     = '01' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT] = $model->exp_year;
                $data["SEMESTER"][TEXT] = $model->semester;
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["DIV"][TEXT]      = "02";
                $data["CODE"][TEXT]     = "01";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                $db->query($query);
            }
        }



        //HREPORTREMARK_DETAIL_DAT(DIV=1)
        //// 5:P3/J3/H3(*) 委員会・部活 '9固定'  *入力項目01_03もある。
        if ($semesdiv == "3") {
            //HD_01_01REMARK1:通年入力('9'固定)
            $data = array();
            $data["REMARK1"][TEXT] = $model->field["REMARK5_1"];

            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";

            $cnt = $db->getOne(knjd428aQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", "9", "01", "01"));
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '9' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";
                $where .= " AND DIV      = '01' ";
                $where .= " AND CODE     = '01' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT] = $model->exp_year;
                $data["SEMESTER"][TEXT] = "9";
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["DIV"][TEXT]      = "01";
                $data["CODE"][TEXT]     = "01";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                $db->query($query);
            }

            //HD_01_02REMARK1:通年入力('9'固定)
            $data = array();
            $data["REMARK1"][TEXT] = $model->field["REMARK5_2"];

            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][FUNC]        = "sysdate()";

            $cnt = $db->getOne(knjd428aQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", "9", "01", "02"));
            if ($cnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".$model->exp_year."' ";
                $where .= " AND SEMESTER = '9' ";
                $where .= " AND SCHREGNO = '".$model->schregno."' ";
                $where .= " AND DIV      = '01' ";
                $where .= " AND CODE     = '02' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT] = $model->exp_year;
                $data["SEMESTER"][TEXT] = "9";
                $data["SCHREGNO"][TEXT] = $model->schregno;
                $data["DIV"][TEXT]      = "01";
                $data["CODE"][TEXT]     = "02";

                $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                $db->query($query);
            }

            if ($schkind == "H") {
                //HD_01_03REMARK1:通年入力('9'固定)
                $data = array();
                $data["REMARK1"][TEXT] = $model->field["REMARK5_3"];

                $data["REGISTERCD"][TEXT]     = STAFFCD;
                $data["UPDATED"][FUNC]        = "sysdate()";

                $cnt = $db->getOne(knjd428aQuery::getHreportRemarkCnt($model, "HREPORTREMARK_DETAIL_DAT", "9", "01", "03"));
                if ($cnt > 0) {
                    $where  = " WHERE ";
                    $where .= "     YEAR     = '".$model->exp_year."' ";
                    $where .= " AND SEMESTER = '9' ";
                    $where .= " AND SCHREGNO = '".$model->schregno."' ";
                    $where .= " AND DIV      = '01' ";
                    $where .= " AND CODE     = '03' ";

                    $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                    $db->query($query);
                } else {
                    $data["YEAR"][TEXT] = $model->exp_year;
                    $data["SEMESTER"][TEXT] = "9";
                    $data["SCHREGNO"][TEXT] = $model->schregno;
                    $data["DIV"][TEXT]      = "01";
                    $data["CODE"][TEXT]     = "03";

                    $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                    $db->query($query);
                }
            }
        }

        Query::dbCheckIn($db);
        return;
    }
}
?>
