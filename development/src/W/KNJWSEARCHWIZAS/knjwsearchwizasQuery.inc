<?php

require_once('for_php7.php');

class knjwsearchwizasQuery extends Query {

    //担当所属とクラス
    function getStaff()
    {
        $query  =" SELECT ";
        $query .="     BELONGING_DIV ";
        $query .=" FROM ";
        $query .="     STAFF_MST ";
        $query .=" WHERE ";
        $query .="     STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //担当クラス
    function getHrclass()
    {
        $query  =" SELECT ";
        $query .="      GRADE || '-' || HR_CLASS AS VALUE ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_HDAT ";
        $query .=" WHERE ";
        $query .="         YEAR     = '".CTRL_YEAR."' ";
        $query .="    AND (TR_CD1   = '".STAFFCD."' ";
        $query .="    OR  TR_CD2    = '".STAFFCD."' ";
        $query .="    OR  TR_CD3    = '".STAFFCD."' ";
        $query .="    OR  SUBTR_CD1 = '".STAFFCD."' ";
        $query .="    OR  SUBTR_CD2 = '".STAFFCD."' ";
        $query .="    OR  SUBTR_CD3 = '".STAFFCD."') ";

        return $query;
    }

    //左リスト
    function GetStudents($model)
    {
        $where = knjwsearchwizasQuery::getStudentSearch($model);
        $and = $where == "" ? "" : "AND ";

        $query = " SELECT DISTINCT ";
        $query .= "   AP_B.APPLICANTNO, ";
        $query .= "   SCH_B.SCHREGNO, ";
        $query .= "   AP_B.NAME AS AP_B_NAME, ";
        $query .= "   SCH_B.NAME AS SCH_B_NAME, ";
        $query .= "   AP_B.SEX ";
        if ($model->dispData["serch1"]) {
            $query .= "   ,CL_D.SLIP_NO ";
        }
        $query .= " FROM ";
        $query .= "   APPLICANT_BASE_MST AP_B ";
        $query .= "   LEFT JOIN CLAIM_DAT CL_D ON CL_D.APPLICANTNO = AP_B.APPLICANTNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST SCH_B ON SCH_B.APPLICANTNO = AP_B.APPLICANTNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_DAT SCH_R ON SCH_B.SCHREGNO = SCH_R.SCHREGNO ";
        $query .= " WHERE ".$where;
        
        
        
        
        $query .= " ORDER BY ";
        $query .= "   AP_B.APPLICANTNO";
        if ($model->dispData["serch1"]) {
            $query .= "   ,CL_D.SLIP_NO ";
        }

//echo $query;
        return $query;
   }

    function getStudentSearch($model)
    {
        $and = "";
        $where = "";

        //制限付き
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= $and." AP_B.BELONGING_DIV = '".$model->belonging["BELONGING_DIV"]."' ";
                $and = "AND ";
            } else if ($model->search["SEARCH_DIV"] == "2") {
                $hr_class = join("','", $model->hr_class);
                $where .= $and." (SCH_R.GRADE || '-' || SCH_R.HR_CLASS IN ('".$hr_class."') ";
                $where .= " OR SCH_R.GRADE = '".$model->belonging["BELONGING_DIV"]."') ";
                $and = "AND ";
            }
            
        }

        if ($model->search["SEARCH_DIV"] == "2") {
            $where .= $and." AP_B.SCHREGNO = SCH_B.SCHREGNO ";
            $and = "AND ";
        } 
        
        //入学年度
        if ($model->search["YEAR"]) {
            $where .= "   ".$and."AP_B.YEAR = '".$model->search["YEAR"]."' ";
            $and = "AND ";
        }
        //処理年度
        if ($model->dispData["exe_year"]) {
            if ($model->search["EXE_YEAR"]) {
                $where .= "   ".$and."SCH_R.YEAR = '".$model->search["EXE_YEAR"]."' ";
                $and = "AND ";
            }
        } else {
            if ($model->search["SEARCH_DIV"] == "2" || $model->search["SEARCH_DIV"] == "4") {
                $where .= "   ".$and."SCH_R.YEAR = '" .CTRL_YEAR. "' ";
                $and = "AND ";
            }
        }
        //受験・入学区分
        if ($model->search["ENT_APP_DIV"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= $and." AP_B.APPLICANT_DIV = '".$model->search["ENT_APP_DIV"]."' ";
                $and = "AND ";
            } else if ($model->search["SEARCH_DIV"] == "2") {
                $where .= $and." SCH_B.ENT_DIV = '".$model->search["ENT_APP_DIV"]."' ";
                $and = "AND ";
            }
        } else {
            if ($model->search["SEARCH_DIV"] == "2") {
                //入学区分辞退以外
                $where .= $and." SCH_B.ENT_DIV NOT IN ('6') ";
                $and = "AND ";
            }
        }

        //氏名
        if ($model->search["NAME"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.NAME LIKE '" .str_replace("'","''",$model->search["NAME"]). "%' ";
                $and = "AND ";
            }
            else if ($model->search["SEARCH_DIV"] == "2" || $model->search["SEARCH_DIV"] == "3" || $model->search["SEARCH_DIV"] == "4") {
                $where .= "   ".$and."SCH_B.NAME LIKE '" .str_replace("'","''",$model->search["NAME"]). "%' ";
                $and = "AND ";
            }
        }
        //氏名かな
        if ($model->search["NAME_KANA"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.NAME_KANA LIKE '" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
                $and = "AND ";
            }
            else if ($model->search["SEARCH_DIV"] == "2" || $model->search["SEARCH_DIV"] == "3" || $model->search["SEARCH_DIV"] == "4") {
                $where .= "   ".$and."SCH_B.NAME_KANA LIKE '" .str_replace("'","''",$model->search["NAME_KANA"]). "%' ";
                $and = "AND ";
            }
        }
        //性別
        if ($model->search["SEX"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.SEX = '{$model->search["SEX"]}' ";
                $and = "AND ";
            }
            else if ($model->search["SEARCH_DIV"] == "2" || $model->search["SEARCH_DIV"] == "3" || $model->search["SEARCH_DIV"] == "4") {
                $where .= "   ".$and."SCH_B.SEX = '{$model->search["SEX"]}' ";
                $and = "AND ";
            }
        }
        //伝票番号
        if ($model->search["SLIP_NO"]) {
            $where .= "   ".$and."CL_D.SLIP_NO = '" .$model->search["SLIP_NO"]. "' ";
            $and = "AND ";
        }
        //所属
        if ($model->search["BELONGING_DIV"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.BELONGING_DIV = '" .$model->search["BELONGING_DIV"]. "' ";
                $and = "AND ";
            }
            else {
                $where .= "   ".$and."SCH_R.GRADE = '" .$model->search["BELONGING_DIV"]. "' ";
                $and = "AND ";
                
            }
        }
        //学籍番号
        if ($model->search["SCHREGNO"]) {
            $where .= "   ".$and."SCH_B.SCHREGNO = '".$model->search["SCHREGNO"]."' ";
            $and = "AND ";
        }
        //志願者番号
        if ($model->search["APPLICANTNO"]) {
            $where .= "   ".$and."AP_B.APPLICANTNO = '".$model->search["APPLICANTNO"]."' ";
            $and = "AND ";
        }
        //コースコード
        if ($model->search["COURSE"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.COURSECODE = '" .$model->search["COURSE"]. "' ";
                $and = "AND ";
            }
            else {
                $where .= "   ".$and."SCH_R.COURSECODE = '" .$model->search["COURSE"]. "' ";
                $and = "AND ";
            }
        }
        //検索区分（卒業区分）
        if ($model->dispData["grddiv"]) {
            if ($model->search["GRD_DIV"]) {
                $where .= "   ".$and."SCH_B.GRD_DIV = '".$model->search["GRD_DIV"]."' ";
                $and = "AND ";
            }
            else if ($model->search["GRD_DIV"] == "0") {
                $where .= "   ".$and."VALUE(SCH_B.GRD_DIV, '0') = '".$model->search["GRD_DIV"]."' ";
                $and = "AND ";
            }
            $where .= " AND AP_B.SCHREGNO = SCH_B.SCHREGNO ";
        } else {
            //除籍区分なし
            if ($model->search["SEARCH_DIV"] == "2" && !$model->search["GRD_SHOW"] == "1") {
                $where .= $and." SCH_B.GRD_DIV IS NULL ";
                $and = "AND ";
            }
        }
        //支払区分
        if ($model->search["MANNER_PAYMENT"]) {
            $where .= "   ".$and."CL_D.MANNER_PAYMENT = '".$model->search["MANNER_PAYMENT"]."' ";
            $and = "AND ";
        }
        //年次
        if ($model->search["ANNUAL"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.ENT_ANNUAL = '" .$model->search["ANNUAL"]. "' ";
                $and = "AND ";
            }
            else {
                $where .= "   ".$and."SCH_R.ANNUAL = '" .$model->search["ANNUAL"]. "' ";
                $and = "AND ";
            }
        }
        //卒業予定月
        if ($model->search["GRD_MONTH"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."MONTH(AP_B.GRD_SCHEDULE_DATE) = " .$model->search["GRD_MONTH"]. " ";
                $and = "AND ";
            }
            else {
                $where .= "   ".$and."MONTH(SCH_R.GRD_SCHEDULE_DATE) = " .$model->search["GRD_MONTH"]. " ";
                $and = "AND ";
            }
        }
        //未収金(※計上した伝票で未だ入金が無い)
        if ($model->search["MISYUKIN"]) {
            switch ($model->search["MISYUKIN"]) {
                //あり
                case "1":
                    $where .= "   ".$and."EXISTS (".
                                         "    SELECT * ".
                                         "    FROM ".
                                         "         CLAIM_DAT ".
                                         "    WHERE ".
                                         "         APPLICANTNO = AP_B.APPLICANTNO ".
                                         "     AND SUMMING_UP_DATE IS NOT NULL".
                                         "     AND VALUE(TOTAL_MONEY,0) > VALUE(PAYMENT_MONEY,0)".
                                         "     AND VALUE(PAYMENT_MONEY,0) <= 0".
                                         ") ";
                    break;
                //なし
                case "2":
                    $where .= "   ".$and."EXISTS (".
                                         "    SELECT * ".
                                         "    FROM ".
                                         "         CLAIM_DAT ".
                                         "    WHERE ".
                                         "         APPLICANTNO = AP_B.APPLICANTNO ".
                                         "     AND SUMMING_UP_DATE IS NOT NULL".
                                         "     AND VALUE(TOTAL_MONEY,0) <= VALUE(PAYMENT_MONEY,0)".
                                         "     AND VALUE(PAYMENT_MONEY,0) > 0".
                                         ") ";
                    break;
            }
            $and = "AND ";
        }
        //学生区分
        if ($model->search["STUDENT_DIV"]) {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.STUDENT_DIV = '" .$model->search["STUDENT_DIV"]. "' ";
                $and = "AND ";
            }
            else {
                $where .= "   ".$and."SCH_R.STUDENT_DIV = '" .$model->search["STUDENT_DIV"]. "' ";
                $and = "AND ";
            }
        }
        //出席状況
        if ($model->search["ATTEND_INFO"]) {
        }
        //集中スクーリング
        if ($model->search["SCHOOLING_DIV"]) {
        }
        //学籍番号範囲指定
        if ($model->dispData["schno_range"] == "1") {
            if ($model->search["SCHREGNO1"]) {
                $where .= "   ".$and."SCH_B.SCHREGNO >= '".$model->search["SCHREGNO1"]."' ";
                $and = "AND ";
            }
            if ($model->search["SCHREGNO2"]) {
                $where .= "   ".$and."SCH_B.SCHREGNO <= '".$model->search["SCHREGNO2"]."' ";
                $and = "AND ";
            }
        }
        //指導要録
        if ($model->search["REC_GET_FLG"] != "" && $model->search["REC_GET_FLG"] != NULL) {
            $nullSet = $model->search["REC_GET_FLG"] == "1" ? " IS NULL " : " IS NOT NULL ";
            $where .= "   ".$and."SCH_B.EDUCATION_REC_GET_FLG".$nullSet;
            $and = "AND ";
        }
        //入金
        if ($model->search["PAYMENT_MONEY"]) {
            switch ($model->search["PAYMENT_MONEY"]) {
                //あり
                case "1":
                    $where .= "   ".$and."EXISTS (".
                                         "    SELECT * ".
                                         "    FROM ".
                                         "         PAYMENT_MONEY_HIST_DAT ".
                                         "    WHERE ".
                                         "         APPLICANTNO = AP_B.APPLICANTNO ".
                                         "     AND VALUE(PAYMENT_MONEY,0) > 0".
                                         ") ";
                    break;
                //なし
                case "2":
                    $where .= "   ".$and."NOT EXISTS (".
                                         "    SELECT * ".
                                         "    FROM ".
                                         "         PAYMENT_MONEY_HIST_DAT ".
                                         "    WHERE ".
                                         "         APPLICANTNO = AP_B.APPLICANTNO ".
                                         "     AND VALUE(PAYMENT_MONEY,0) > 0".
                                         ") ";
                    break;
            }
            $and = "AND ";
            
        }
        

        //プログラム固有 STR
        if ($model->programid == "KNJW043") {
            $where .= "   ".$and."AP_B.BELONGING_DIV = '010' ";
            $and = "AND ";
        }
        if ($model->programid == "KNJWL110") {
            if ($model->search["SEARCH_DIV"] == "1") {
                $where .= "   ".$and."AP_B.STUDENT_DIV NOT IN ('05', '99') ";
                $and = "AND ";
                $where .= "   ".$and."AP_B.PROCEDURE_DIV = '1' ";
                $and = "AND ";
            }
            else {
                $where .= "   ".$and."SCH_R.STUDENT_DIV NOT IN ('05', '99') ";
                $and = "AND ";
                
                $where .= "   ".$and."VALUE(SCH_B.ENT_DIV, '0') NOT IN ('7') ";
                $and = "AND ";
                if ($model->search["GRD_SHOW"] == "1") {
                    $where .= "   ".$and."VALUE(SCH_B.GRD_DIV, '0') IN ('0', '1', '2', '3', '4') ";
                    $and = "AND ";
                } else {
                    $where .= "   ".$and."(VALUE(SCH_B.GRD_DIV, '0') IN ('0', '4') OR (VALUE(SCH_B.GRD_DIV, '0') IN ('1', '2', '3') ";
                    $and = "AND ";
                    $where .= "   ".$and."SCH_B.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."' ) ) ";
                    $and = "AND ";
                }
            }
        }
        if ($model->programid == "KNJWA150") {
            $where .= "   ".$and."EXISTS (".
                                 "    SELECT * ".
                                 "    FROM ".
                                 "         CLAIM_DAT ".
                                 "    WHERE ".
                                 "         APPLICANTNO = AP_B.APPLICANTNO ".
                                 "     AND COMP_ENT_FLG = '1'".
                                 ") ";
            $and = "AND ";
        }
        if ($model->programid == "KNJWA184") {
            $where .= "   ".$and."VALUE(SCH_B.ENT_DIV, '0') NOT IN ('7') ";
            $and = "AND ";
            $where .= "   ".$and."(VALUE(SCH_B.GRD_DIV, '0') IN ('0', '4') OR VALUE(SCH_B.GRD_DIV, '0') IN ('1', '2', '3') ";
            $and = "AND ";
            $where .= "   ".$and."SCH_B.GRD_DATE > '".str_replace("/", "-", CTRL_DATE)."') ";
            $and = "AND ";
        }
        if ($model->programid == "KNJWD220") {
            $where .= "   ".$and."SCH_R.STUDENT_DIV IN ('01') ";
            $and = "AND ";
            $where .= "   ".$and."VALUE(SCH_B.GRD_DATE, '".str_replace("/", "-", $model->control["学期開始日付"][9])."') ";
            $where .= "         BETWEEN '".str_replace("/", "-", $model->control["学期開始日付"][9])."' ";
            $where .= "             AND '".str_replace("/", "-", $model->control["学期終了日付"][9])."' ";
            $and = "AND ";
        }
        //END
        return $where;
    }
    
    //年度
    function getYear()
    {
        $query  = " WITH MISYUKIN (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('".CTRL_YEAR."','".CTRL_YEAR."'), ('".(CTRL_YEAR + 1)."','".(CTRL_YEAR + 1)."') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM MISYUKIN ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //入学年度
    function getEntYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        /*
        $query .= " WHERE ";
        $query .= "     YEAR <= '".CTRL_YEAR."' ";
        */
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //処理年度
    function getExeYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //支払方法
    function getPayment()
    {
        $query  =" SELECT ";
        $query .="     NAMECD2 AS VALUE, ";
        $query .="     NAME1 AS LABEL ";
        $query .=" FROM ";
        $query .="     NAME_MST ";
        $query .=" WHERE ";
        $query .="     NAMECD1 = 'W001' ";
        $query .=" ORDER BY ";
        $query .="     NAMECD2 ";

        return $query;
    }

    //銀行・支店
    function getBranch()
    {
        $query  =" SELECT ";
        $query .="     VIRTUAL_BANK_CD AS VALUE, ";
        $query .="     VALUE(BANK_NAME, '') || VALUE(BRANCH_NAME, '') AS LABEL ";
        $query .=" FROM ";
        $query .="     VIRTUAL_BANK_MST ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //未収金
    function getMisyukin()
    {
        $query  = " WITH MISYUKIN (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('あり','1'), ('なし','2') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM MISYUKIN ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験区分
    function getGrdDiv()
    {
        $query  = " WITH KOTEI (VALUE, LABEL) AS ( ";
        $query .= " VALUES('0', '0:在籍者') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KOTEI ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験区分
    function getApplicantDiv()
    {
        $query  =" SELECT ";
        $query .="     NAMECD2 AS VALUE, ";
        $query .="     NAME1 AS LABEL ";
        $query .=" FROM ";
        $query .="     NAME_MST ";
        $query .=" WHERE ";
        $query .="     NAMECD1 = 'W005' ";
        $query .="     AND NAMECD2 != '6' ";
        $query .=" ORDER BY ";
        $query .="     NAMECD2 ";

        return $query;
    }

    //入学区分
    function getEntDiv()
    {
        $query  =" SELECT ";
        $query .="     NAMECD2 AS VALUE, ";
        $query .="     NAME1 AS LABEL ";
        $query .=" FROM ";
        $query .="     NAME_MST ";
        $query .=" WHERE ";
        $query .="     NAMECD1 = 'A002' ";
        $query .=" ORDER BY ";
        $query .="     NAMECD2 ";

        return $query;
    }

    //手続区分
    function getProcedure()
    {
        $query  =" SELECT ";
        $query .="     NAMECD2 AS VALUE, ";
        $query .="     NAME1 AS LABEL ";
        $query .=" FROM ";
        $query .="     NAME_MST ";
        $query .=" WHERE ";
        $query .="     NAMECD1 = 'W008' ";
        $query .=" ORDER BY ";
        $query .="     NAMECD2 ";

        return $query;
    }

    //性別
    function getSex() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z002' ";

        return $query;
    }

    //所属
    function getBelong($model)
    {
        $query  =" SELECT ";
        $query .="     BELONGING_DIV AS VALUE, ";
        $query .="     SCHOOLNAME3 AS LABEL ";
        $query .=" FROM ";
        $query .="     BELONGING_MST ";
        
        //権限設定
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $grade = join("','", $model->grade);
            $query .=" WHERE ";
            $query .="       BELONGING_DIV = '".$model->belonging["BELONGING_DIV"]."' ";
            $query .="    OR BELONGING_DIV IN ('".$grade."') ";
        }
        
        $query .=" ORDER BY ";
        $query .="     VALUE ";

        return $query;
    }

    //スクーリング
    function getSchooling()
    {
        $query  =" SELECT ";
        $query .="     SCHOOLING_TYPE AS VALUE, ";
        $query .="     SCHOOLING_TYPE || ':' || NAME AS LABEL ";
        $query .=" FROM ";
        $query .="     SCHOOLING_TYPE_MST ";
        $query .=" WHERE ";
        $query .="     SCHOOLING_DIV = '01' ";
        $query .=" ORDER BY ";
        $query .="     SCHOOLING_TYPE ";

        return $query;
    }

    //出席状況
    function getAttendInfo()
    {
        $query  = " WITH MISYUKIN (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('未出席','1'), ('出席済','2') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM MISYUKIN ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学生区分
    function getStuDiv()
    {
        $query  = " SELECT ";
        $query .= "     STUDENT_DIV || '　' || NAME AS LABEL, ";
        $query .= "     STUDENT_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     STUDENTDIV_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    function getCourse()
    {
        $query  = " SELECT ";
        $query .= "   COURSECODE || '　' || VALUE(COURSECODENAME, '') AS LABEL, ";
        $query .= "   COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年次
    function getAnnual()
    {
        $query  = " WITH ANNUAL (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('1年次','01'), ('2年次','02'), ('3年次','03') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM ANNUAL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //卒業予定月
    function getGrdMonth()
    {
        $query  = " WITH GRDDATE (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('3月','3'), ('9月','9') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM GRDDATE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //卒業予定月
    function getGrdShow()
    {
        $query  = " WITH GRDSHOW (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('する','1') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM GRDSHOW ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //提携先
    function getTeikei()
    {
        $query  = " WITH TEIKEI (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('日本文理大学','0001'), ('第一高等学院','0002'), ('セレッソ大阪','0003') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM TEIKEI ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目名
    function getSubclass()
    {
        $query  = " SELECT ";
        $query .= "   SUBCLASSCD || '　' || VALUE(SUBCLASSNAME, '') AS LABEL, ";
        $query .= "   SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "   SUBCLASS_MST ";
        $query .= " WHERE SUBCLASSCD not in('119999')";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //レポート回数
    function getReport()
    {
        $query  = " WITH REPORT (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('１回','1'), ('２回','2'), ('３回','3'), ";
        $query .= "            ('４回','4'), ('５回','5'), ('６回','6'), ";
        $query .= "            ('７回','7'), ('８回','8'), ('９回','9'), ";
        $query .= "            ('10回','10'), ('11回','11'), ('12回','12') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM REPORT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入力済／未入力
    function getInport()
    {
        $query  = " WITH INPORT (LABEL, VALUE) AS ( ";
        $query .= "     VALUES ('入力済','1'), ('未入力','2') ";
        $query .= "     ) ";
        $query .= " SELECT * FROM INPORT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
