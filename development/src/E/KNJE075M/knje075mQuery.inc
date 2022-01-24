<?php

require_once('for_php7.php');

class knje075mQuery extends Query {

    //学年取得
    function getGrade()
    {
        $query .= " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."'";
            $query .= "AND SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "AND GRADE='".$model->field["GRADE"]."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "AND GRADE='".$model->field["GRADE"]."'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒一覧
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "     || ' ' ";
        $query .= "     || SCHREG_REGD_HDAT.HR_NAME ";
        $query .= "     || ATTENDNO ";
        $query .= "     || '番' ";
        $query .= "     || ' ' ";
        $query .= "     || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " INNER JOIN SCHREG_REGD_DAT ";
        $query .= "     ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT ";
        $query .= "     ON SCHREG_REGD_HDAT.YEAR     = SCHREG_REGD_DAT.YEAR ";
        $query .= "    AND SCHREG_REGD_HDAT.SEMESTER = SCHREG_REGD_DAT.SEMESTER ";
        $query .= "    AND SCHREG_REGD_HDAT.GRADE    = SCHREG_REGD_DAT.GRADE ";
        $query .= "    AND SCHREG_REGD_HDAT.HR_CLASS = SCHREG_REGD_DAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         (((SCHREG_REGD_DAT.YEAR)     = '{$model->control["年度"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.SEMESTER) = '{$model->control["学期"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS) = '{$model->field["GRADE_HR_CLASS"]}')) ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //出力対象一覧
    function getList2($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "     || ' ' ";
        $query .= "     || SCHREG_REGD_HDAT.HR_NAME ";
        $query .= "     || ATTENDNO ";
        $query .= "     || '番' ";
        $query .= "     || ' ' ";
        $query .= "     || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " INNER JOIN SCHREG_REGD_DAT ";
        $query .= "     ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT ";
        $query .= "     ON SCHREG_REGD_HDAT.YEAR     = SCHREG_REGD_DAT.YEAR ";
        $query .= "    AND SCHREG_REGD_HDAT.SEMESTER = SCHREG_REGD_DAT.SEMESTER ";
        $query .= "    AND SCHREG_REGD_HDAT.GRADE    = SCHREG_REGD_DAT.GRADE ";
        $query .= "    AND SCHREG_REGD_HDAT.HR_CLASS = SCHREG_REGD_DAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         (((SCHREG_REGD_DAT.YEAR)     = '{$model->control["年度"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.SEMESTER) = '{$model->control["学期"]}')) ";
        $query .= "     AND SCHREG_BASE_MST.SCHREGNO = '{$schregno}'  ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    function getSchoolName() {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //最高年次
    function getinfo_max_grade($schregno)
    {
        $query = "SELECT "
                 . "MAX(CASE WHEN W1.ANNUAL IS NULL THEN W2.ANNUAL ELSE W1.ANNUAL END) ANNUAL_MAX "
                 . "FROM(SELECT SCHREGNO,ANNUAL FROM SCHREG_REGD_DAT W1 WHERE W1.SCHREGNO='" . $schregno . "' AND W1.YEAR<='" . CTRL_YEAR . "')W1 "
                 . "FULL JOIN(SELECT SCHREGNO,ANNUAL FROM GRD_REGD_DAT W1 WHERE W1.SCHREGNO='" . $schregno . "' AND W1.YEAR<='" . CTRL_YEAR . "')W2 "
                 . "ON W2.SCHREGNO=W1.SCHREGNO";

        return $query;
    }


    //学習記録データ（進学用）    //2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
    function getinfo_ps1_1($schregno,$hyoutei)
    {
        $subject_D = "01";            //教科コード
        $subject_U = "89";            //教科コード
        $subject_T = "90";            //総合的な学習の時間

        //    評定１を２と判定
        $h_1_2 = "";
        $h_1_3 = "";
            if( $hyoutei=="on" ){                            //----->評定読み替えのON/OFF
                $h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END";
                $h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END";
            } else{
                $h_1_2 = "T1.GRADES";
                $h_1_3 = "T1.CREDIT";
            }

        //該当生徒の成績データ表
        $query  ="WITH STUDYREC AS(";
        $query .=        "SELECT ";
        $query .=            "SCHREGNO,YEAR,ANNUAL,CLASSCD,SUBCLASSCD,VALUATION AS GRADES,";
        $query .=            "GET_CREDIT AS CREDIT,ADD_CREDIT ";
        $query .=        "FROM ";
        $query .=            "SCHREG_STUDYREC_DAT T1 ";
        $query .=        "WHERE ";
        $query .=            "T1.SCHREGNO = '" . $schregno . "' AND YEAR <= '" . CTRL_YEAR . "' AND ";
        $query .=            "(CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' OR CLASSCD='".$subject_T."')) ";
        //該当生徒の科目評定、修得単位及び教科評定平均
        $query .=    "SELECT ";
        $query .=        "T1.ANNUAL,T1.CLASSCD,";
        $query .=    "T2.CLASSNAME,";
        $query .=        "T1.SUBCLASSCD,";
        $query .=    "T3.SUBCLASSNAME,";
        $query .=    $h_1_2 . " AS GRADES,";
        $query .=    "T5.AVG_GRADES,'' AS ASSESS_LEVEL,";
        $query .=        "T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ";
        $query .=    "FROM ";
        $query .=        "STUDYREC T1 ";
        $query .=        "INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        $query .=        "INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
                //    修得単位数の計
        $query .=        "INNER JOIN(SELECT ";
        $query .=                "CLASSCD,SUBCLASSCD,SUM(" . $h_1_3 . ") AS CREDIT ";
        $query .=            "FROM ";
        $query .=                "STUDYREC T1 ";
        $query .=            "WHERE ";
        $query .=                "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=            "GROUP BY ";
        $query .=                "CLASSCD,SUBCLASSCD ";
        $query .=        ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
                //    各教科の評定平均値
        $query .=    "INNER JOIN(SELECT ";
        $query .=            "CLASSCD,";
        $query .=            "DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1),5,1) AS AVG_GRADES ";
        $query .=        "FROM ";
        $query .=            "STUDYREC T1 ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=        "GROUP BY ";
        $query .=            "CLASSCD ";
        $query .=    ")T5 ON T5.CLASSCD = T1.CLASSCD ";
        $query .=    "WHERE ";
        $query .=        "T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        //    総合学習の修得単位数（合計）
        $query .=    "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'".$subject_T."' AS CLASSCD,";
        $query .=            "'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD = '".$subject_T."' ";
        //    留学中の修得単位数（合計）
        $query .=        "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "(SELECT ";
        $query .=                "SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
        $query .=            "FROM  ";
        $query .=                "SCHREG_TRANSFER_DAT  ";
        $query .=            "WHERE  ";
        $query .=                "SCHREGNO = '" . $schregno . "' AND TRANSFERCD = '1' ";
        $query .=            ")ST1 ";
        $query .=        "WHERE ";
        $query .=            "TRANSFER_YEAR <= " . CTRL_YEAR . " ";
        //    全体の修得単位数・全体の評定平均値
        $query .=        "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,";
        $query .=        "CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '特' ELSE '  ' END AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,";
        $query .=        "ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1) AS AVG_GRADES,";
        $query .=        "(SELECT     ST2.ASSESSMARK ";
        $query .=         "FROM         ASSESS_MST ST2 ";
        $query .=         "WHERE     ST2.ASSESSCD='4' ";
        $query .=                    "AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1),5,1) ";
        $query .=                            "BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
        $query .=            "0 AS GRADE_CREDIT,";
        $query .=            "SUM(" . $h_1_3 . ") AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC T1 ";
        $query .=        "LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=    "ORDER BY 2,4,1";

        return $query;
    }


    //学習記録データ（就職用）
    function getinfo_ps1_2($schregno,$hyoutei)
    {
        $subject_D = "01";   //教科コード
        $subject_U = "89";   //教科コード
        $subject_T = "90";   //総合的な学習の時間

        //    評定１を２と判定
        $h_1_2 = "";
        $h_1_3 = "";
            if( $hyoutei=="on" ){                            //----->評定読み替えのON/OFF
                $h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END";
                $h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END";
            } else{
                $h_1_2 = "T1.GRADES";
                $h_1_3 = "T1.CREDIT";
            }

        //該当生徒の成績データ表
        $query  ="WITH STUDYREC AS(";
        $query .=        "SELECT ";
        $query .=            "SCHREGNO,YEAR,ANNUAL,CLASSCD,SUBCLASSCD,VALUATION AS GRADES,";
        $query .=            "GET_CREDIT AS CREDIT,ADD_CREDIT ";
        $query .=        "FROM ";
        $query .=            "SCHREG_STUDYREC_DAT T1 ";
        $query .=        "WHERE ";
        $query .=            "T1.SCHREGNO = '" . $schregno . "' AND YEAR <= '" . CTRL_YEAR . "' AND ";
        $query .=            "(CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' OR CLASSCD='".$subject_T."')) ";

        //該当生徒の科目評定、修得単位及び教科評定平均
        $query .=    "SELECT ";
        $query .=        "T1.ANNUAL,T1.CLASSCD,";
        $query .=        "T2.CLASSNAME,";
        $query .=        "T1.SUBCLASSCD,";
        $query .=        "T3.SUBCLASSNAME,";
        $query .=        $h_1_2 . " AS GRADES,";
        $query .=        "T5.AVG_GRADES,'' AS ASSESS_LEVEL,";
        $query .=        "T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ";
        $query .=    "FROM ";
        $query .=        "STUDYREC T1 ";
        $query .=        "INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        $query .=        "INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //    修得単位数の計
        $query .=        "INNER JOIN(SELECT ";
        $query .=                "CLASSCD,SUBCLASSCD,SUM(" . $h_1_3 . ") AS CREDIT ";
        $query .=            "FROM ";
        $query .=                "STUDYREC T1 ";
        $query .=            "WHERE ";
        $query .=                "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=            "GROUP BY ";
        $query .=                "CLASSCD,SUBCLASSCD ";
        $query .=        ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //    各教科の評定平均値
        $query .=        "INNER JOIN(SELECT ";
        $query .=                "CLASSCD,";
        $query .=                "DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1),5,1) AS AVG_GRADES ";
        $query .=            "FROM ";
        $query .=                "STUDYREC T1 ";
        $query .=            "WHERE ";
        $query .=                "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=            "GROUP BY ";
        $query .=                "CLASSCD ";
        $query .=        ")T5 ON T5.CLASSCD = T1.CLASSCD ";
        $query .=    "WHERE ";
        $query .=        "T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";

        //    総合学習の修得単位数（学年別）
        $query .=    "UNION SELECT ";
        $query .=            "ANNUAL,'".$subject_T."' AS CLASSCD,'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD = '".$subject_T."' ";
        $query .=        "GROUP BY ";
        $query .=            "ANNUAL ";
        //    総合学習の修得単位数（合計）
        $query .=    "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'".$subject_T."' AS CLASSCD,";
        $query .=            "'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD = '".$subject_T."' ";

        //    留学中の修得単位数（学年別）
        $query .=        "UNION SELECT ";
        $query .=            "ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
        $query .=        "FROM ";
        $query .=                "(SELECT ";
        $query .=                    "ABROAD_CREDITS,";
        $query .=                    "INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
        $query .=                "FROM ";
        $query .=                    "SCHREG_TRANSFER_DAT ";
        $query .=                "WHERE ";
        $query .=                    "SCHREGNO = '" . $schregno . "' AND TRANSFERCD = '1' ";
        $query .=                ")ST1,";
        $query .=                "(SELECT ";
        $query .=                    "ANNUAL,MAX(YEAR) AS YEAR ";
        $query .=                 "FROM ";
        $query .=                    "SCHREG_REGD_DAT ";
        $query .=                "WHERE ";
        $query .=                    "SCHREGNO = '" . $schregno . "' AND YEAR <= '" . CTRL_YEAR . "' ";
        $query .=                "GROUP BY ";
        $query .=                    "ANNUAL ";
        $query .=                ")ST2 ";
        $query .=        "WHERE ";
        $query .=            "ST1.TRANSFER_YEAR <= " . CTRL_YEAR . " ";
        $query .=            "and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ";
        $query .=        "GROUP BY ";
        $query .=            "ANNUAL ";
        //    留学中の修得単位数（合計）
        $query .=        "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "(SELECT ";
        $query .=                "SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
        $query .=            "FROM  ";
        $query .=                "SCHREG_TRANSFER_DAT  ";
        $query .=            "WHERE  ";
        $query .=                "SCHREGNO = '" . $schregno . "' AND TRANSFERCD = '1' ";
        $query .=            ")ST1 ";
        $query .=        "WHERE ";
        $query .=            "TRANSFER_YEAR <= " . CTRL_YEAR . " ";

        //    修得単位数、評定平均（学年別）
        $query .=        "UNION SELECT ";
        $query .=            "ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,";
        $query .=            "'ZZZZ' AS SUBCLASSCD,'total' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,";
        $query .=            "0 AS AVG_GRADES,";
        $query .=            "'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,";
        $query .=            "SUM(" . $h_1_3 . ") AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC T1 ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=        "GROUP BY ";
        $query .=            "ANNUAL ";

        //    全体の修得単位数・全体の評定平均値
        $query .=        "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,";
        $query .=            "CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,";

        $query .=            "ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1) AS AVG_GRADES,";
        $query .=            "(SELECT     ST2.ASSESSMARK ";
        $query .=             "FROM         ASSESS_MST ST2 ";
        $query .=             "WHERE     ST2.ASSESSCD='4' ";
        $query .=                        "AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1),5,1) ";
        $query .=                                "BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
        $query .=            "0 AS GRADE_CREDIT,";
        $query .=            "SUM(" . $h_1_3 . ") AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC T1 ";
        $query .=            "LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=    "ORDER BY 2,4,1";

        return $query;
    }


    //出欠記録データ
    function getinfo_ps2($schregno)
    {
        $query = "SELECT DISTINCT "
                    . "YEAR,"
                    . "ANNUAL,"
                    . "VALUE(CLASSDAYS,0) AS CLASSDAYS,"                           //授業日数
                    . "VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) AS ATTEND_1,"         //授業日数-休学日数:1
                    . "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,"         //出停・忌引
                    . "VALUE(SUSPEND,0) AS SUSPEND,"                               //出停:2
                    . "VALUE(MOURNING,0) AS MOURNING,"                             //忌引:3
                    . "VALUE(ABROAD,0) AS ABROAD,"                                 //留学:4
                    . "VALUE(REQUIREPRESENT,0) AS REQUIREPRESENT,"                 //要出席日数:5
                    . "VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) "
                                . "+ VALUE(NOACCIDENTNOTICE,0) AS ATTEND_6,"       //病欠＋事故欠（届・無）:6
                    . "VALUE(PRESENT,0) AS PRESENT,"                               //出席日数:7
                    . "VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 "          //忌引＋出停:8
                . "FROM "
                    . "("
                        . "SELECT "
                            . "SCHREGNO,"
                            . "YEAR,"
                            . "ANNUAL,"
                            . "SUM(CLASSDAYS) AS CLASSDAYS,"
                            . "SUM(OFFDAYS) AS OFFDAYS,"
                            . "SUM(ABSENT) AS ABSENT,"
                            . "SUM(SUSPEND) AS SUSPEND,"
                            . "SUM(MOURNING) AS MOURNING,"
                            . "SUM(ABROAD) AS ABROAD,"
                            . "SUM(REQUIREPRESENT) AS REQUIREPRESENT,"
                            . "SUM(SICK) AS SICK,"
                            . "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,"
                            . "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
                            . "SUM(PRESENT) AS PRESENT "
                        . "FROM "
                            . "SCHREG_ATTENDREC_DAT "
                        . "WHERE "
                                . "SCHREGNO = '" . $schregno . "' "
                            . "AND YEAR <= '" . CTRL_YEAR . "' "
                        . "GROUP BY "
                            . "SCHREGNO,"
                            . "ANNUAL,"
                            . "YEAR "
                    . ")T1 "
                . "ORDER BY "
                    . "T1.ANNUAL";

        return $query;
    }


    //健康診断データ
    function getinfo_ps3($schregno)
    {
        $query = "SELECT "
                . "T2.DATE,"
                . "T1.HEIGHT,"
                . "T1.WEIGHT,"
                . "T1.R_BAREVISION,"
                . "T1.L_BAREVISION,"
                . "T1.R_VISION,"
                . "T1.L_VISION,"
                . "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = R_EAR) AS R_EAR,"
                . "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = L_EAR) AS L_EAR,"
                . "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F100' AND NAMECD2 = TB_REMARKCD) AS RB_REMARK "
            . "FROM "
                . "MEDEXAM_DET_DAT T1 "
                 . "INNER JOIN MEDEXAM_HDAT T2 ON T1.SCHREGNO = T2.SCHREGNO "
                                            . "AND T1.YEAR = T2.YEAR "
            . "WHERE  "
                    . "T1.SCHREGNO = '" . $schregno . "' "
                . "AND T1.YEAR <= '" . CTRL_YEAR . "' "
            . "ORDER BY "
                . "T1.YEAR DESC";

        return $query;
    }


    //所見データ（進学用）
    function getinfo_ps4_1($model, $schregno)
    {
        $query  = "SELECT ";
        $query .= "        W2.SCHREGNO, ";
        $query .= "        W1.COMMENTEX_A_CD, ";
        $query .= "        W1.DISEASE, ";
        $query .= "        W1.DOC_REMARK, ";
        $query .= "        W1.TR_REMARK, ";
        $query .= "        W1.TOTALSTUDYACT, ";
        $query .= "        W1.TOTALSTUDYVAL, ";
        $query .= "        W1.REMARK, ";
        $query .= "        W2.ANNUAL, ";
        $query .= "        W2.ATTENDREC_REMARK, ";
        $query .= "        W2.SPECIALACTREC, ";
        if ($model->useSyojikou3 == "0") {
            $query .= "        W2.TRAIN_REF ";
        } else {
            $query .= "        W2.TRAIN_REF, ";
            $query .= "        W2.TRAIN_REF2, ";
            $query .= "        W2.TRAIN_REF3 ";
        }
        $query .= "FROM ";
        $query .= "        HEXAM_ENTREMARK_DAT W2 ";
        $query .= "        LEFT JOIN HEXAM_ENTREMARK_HDAT W1 ON W2.SCHREGNO = W1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "     W2.YEAR <= '" . CTRL_YEAR . "' AND ";
        $query .= "        W2.SCHREGNO = '" . $schregno . "' ";
        $query .= "ORDER BY ";
        $query .= "        W2.ANNUAL ";

        return $query;
    }


    //所見データ（就職用）
    function getinfo_ps4_2($schregno)
    {
        $query = "SELECT "
                . "W1.SCHREGNO,"
                . "JOBHUNT_REC,"
                . "JOBHUNT_RECOMMEND,"
                . "JOBHUNT_ABSENCE,"
                . "JOBHUNT_HEALTHREMARK "
            . "FROM "
                . "HEXAM_EMPREMARK_DAT W1 "
            . "WHERE "
                . "W1.SCHREGNO = '" . $schregno . "' ";

        return $query;
    }


    //成績概評人数データ
    function getinfo_ps5($schregno)
    {
        $query = "SELECT "
                    . "A_MEMBER AS MEMBER5,"
                    . "B_MEMBER AS MEMBER4,"
                    . "C_MEMBER AS MEMBER3,"
                    . "D_MEMBER AS MEMBER2,"
                    . "E_MEMBER AS MEMBER1,"
                    . "COURSE_MEMBER AS MEMBER0,"
                    . "GRADE_MEMBER AS MEMBER6 "
                . "FROM "
                    . "GENEVIEWMBR_DAT T1,"
                    . "SCHREG_REGD_DAT T2 "
                . "WHERE "
                        . "T2.SCHREGNO = '" . $schregno . "' "
                    . "AND T2.YEAR = '" . CTRL_YEAR . "' "
                    . "AND T2.SEMESTER = '" . CTRL_SEMESTER . "' "
                    . "AND T1.YEAR = T2.YEAR "
                    . "AND T1.GRADE = T2.GRADE "
                    . "AND T1.COURSECD = T2.COURSECD "
                    . "AND T1.MAJORCD = T2.MAJORCD "
                    . "AND VALUE(T1.COURSECODE,'0000') = VALUE(T2.COURSECODE,'0000')";

        return $query;
    }


    //個人データ
    function getinfo_ps6($schregno)
    {
        $query  =    "SELECT ";
        $query .=        "T2.NAME,";
        $query .=        "T2.NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,";
        $query .=        "T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,";
            //課程・学科・コース
        $query .=        "T3.COURSENAME,T4.MAJORNAME,T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,";
            //卒業
        $query .=    "CASE WHEN T2.GRD_DATE IS NULL THEN ";
        $query .=    "RTRIM(CHAR(INT(T1.YEAR)+case t1.annual when '01' then 3 when '02' then 2 else 1 end)) || '-' || ";
        $query .=    "RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' ELSE VARCHAR(T2.GRD_DATE) END AS GRADU_DATE,";
        $query .=    "CASE WHEN T2.GRD_DATE IS NULL THEN '卒業見込み' ELSE ";
        $query .=        "(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' ";
        $query .=            "AND T2.GRD_DIV = ST2.NAMECD2) END AS GRADU_NAME,";
            //入学
        $query .=        "T2.ENT_DATE,T2.ENT_DIV,";
        $query .=        "(SELECT MIN(ANNUAL) FROM SCHREG_REGD_DAT ST1 WHERE ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,";
        $query .=        "(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND T2.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,";
            //住所
        $query .=        "VALUE(T8.ADDR1,'') || VALUE(T8.ADDR2,'') AS ADDR,";
        $query .=        "T8.ADDR1,T8.ADDR2,T8.TELNO,T8.ZIPCD,";
        $query .=        "T1.SCHREGNO ";
        $query .=    "FROM ";
            //学籍情報(??? or ????)
        $query .=        "(     SELECT        * ";
        $query .=            "FROM         SCHREG_REGD_DAT T1 ";
        $query .=            "WHERE         T1.SCHREGNO= '" . $schregno . "' AND T1.YEAR= '" . CTRL_YEAR . "' ";
        $query .=                    "AND T1.SEMESTER= '" . CTRL_SEMESTER . "' ";
        $query .=        ") T1 ";
        $query .=        "INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ";
        $query .=                                    "AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ";
            //卒業情報有りの場合
        $query .=        "INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ";
            //基礎情報
        $query .=        "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .=        "LEFT JOIN NAME_MST T7 ON NAMECD1='Z002' AND NAMECD2=T2.SEX ";
            //課程、学科、コース
        $query .=        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ";
        $query .=        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ";
        $query .=        "LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ";
        $query .=                            "AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')";
            //生徒住所(??)
        $query .=        "LEFT JOIN SCHREG_ADDRESS_DAT AS T8 ";
        $query .=        "INNER JOIN(";
        $query .=            "SELECT     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .=            "FROM         SCHREG_ADDRESS_DAT ";
        $query .=            "WHERE         SCHREGNO= '" . $schregno . "' AND FISCALYEAR(ISSUEDATE) <= '" . CTRL_YEAR . "' ";
        $query .=        ")T9 ON T9.ISSUEDATE = T8.ISSUEDATE ON T8.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }


    //学校データ
    function getinfo_ps7($year2,$staffcd)
    {
        $query  = "    SELECT ";
        $query .= "        T1.CLASSIFICATION,";
        $query .= "        T1.SCHOOLNAME1,";
        $query .= "        T1.SCHOOLZIPCD,T1.SCHOOLADDR1,T1.SCHOOLADDR2,";
        $query .= "        T1.SCHOOLDIV, ";
        $query .= "          T2.STAFFNAME AS PRINCIPAL_NAME,";
        $query .= "          T2.JOBNAME AS PRINCIPAL_JOBNAME, ";
        $query .= "          T3.STAFFNAME AS STAFF2_NAME,";
        $query .= "          T3.JOBNAME AS STAFF2_JOBNAME ";
        $query .= "    FROM ";
            //    学校
        $query .= "        (";
        $query .= "            SELECT ";
        $query .= "                'FRED' AS KEY,CLASSIFICATION,";
        $query .= "                SCHOOLNAME1,";
        $query .= "                SCHOOLZIPCD,SCHOOLADDR1,SCHOOLADDR2,";
        $query .= "                SCHOOLDIV ";
        $query .= "            FROM ";
        $query .= "                SCHOOL_MST W1 ";
        $query .= "            WHERE ";
        $query .= "                YEAR= '" . CTRL_YEAR . "' ";
        $query .= "        )T1 ";
            //    校長
        $query .= "    LEFT JOIN(";
        $query .= "        SELECT ";
        $query .= "            'FRED' AS KEY,STAFFNAME,JOBNAME ";
        $query .= "        FROM ";
        $query .= "            STAFF_YDAT W1 ";
        $query .= "            INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ";
        $query .= "            LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ";
        $query .= "        WHERE ";
        $query .= "            W1.YEAR= '" . $year2 . "' AND W2.JOBCD='0001' ";
        $query .= "    )T2 ON T1.KEY = T2.KEY ";
            //    担当教諭
        $query .= "    LEFT JOIN(";
        $query .= "        SELECT ";
        $query .= "            'FRED' AS KEY,STAFFNAME,JOBNAME ";
        $query .= "        FROM ";
        $query .= "            STAFF_MST W2 ";
        $query .= "            LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ";
        $query .= "        WHERE ";
        $query .= "            W2.STAFFCD = '" . $staffcd . "' ";
        $query .= "    )T3 ON T1.KEY = T3.KEY ";

        return $query;
    }
}
?>
