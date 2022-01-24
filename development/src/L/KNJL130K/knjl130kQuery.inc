<?php

require_once('for_php7.php');

class knjl130kQuery extends Query 
{
    //名称マスタよりデータを取得
    function GetName($year, $namecd1)
    {
        $query = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year ."' AND ";
        $query .= "     NAMECD1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     CASE WHEN NAMECD1 = 'L010' ";
        $query .= "          THEN INT(NAMESPARE2 || NAMECD2) ";
        $query .= "          ELSE INT(NAMECD2) ";
        $query .= "     END ";

        return $query;
    }

    //中学・高校判定    2005/09/14 arakaki
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }

    
    //志望区分を取得
    function getExamcourse($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T2.examcourse_name ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst T1, ";
        $query .= "     entexam_course_mst T2 ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = T2.entexamyear AND ";      #2005/09/05
        $query .= "     T1.coursecd = T2.coursecd AND ";
        $query .= "     T1.majorcd = T2.majorcd AND ";
        $query .= "     T1.examcoursecd = T2.examcoursecd AND ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.testdiv = '".$model->testdiv."'";
        $query .= " GROUP BY ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T2.examcourse_name ";
        $query .= " ORDER BY ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.examcoursecd ";

        #echo $query;
        return $query;
    }

    //志望区分マスタより志望学科を取得
    function get_coursemajor($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD, ";
        $query .= "     rtrim(T2.COURSENAME) || "; 
        $query .= "     rtrim(T3.MAJORNAME) || ";
        $query .= "     rtrim(T4.EXAMCOURSE_NAME) AS COURSENAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_COURSE_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T2.YEAR AND ";
        $query .= "     T1.COURSECD = T2.COURSECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_MAJOR_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.MAJORCD = T3.MAJORCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_COURSE_MST T4 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T4.ENTEXAMYEAR AND ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD = T4.COURSECD || T4.MAJORCD || T4.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     T1.DESIREDIV = '".$model->desirediv."'";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD ";

        return $query;
    }

    //受験コース取得
    function get_examcourse($year)
    {
        $query  = " SELECT ";
        $query .= "     coursecd, ";
        $query .= "     majorcd, ";
        $query .= "     examcoursecd, ";
        $query .= "     examcourse_mark ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$year."'";
        $query .= " ORDER BY ";
        $query .= "     coursecd, majorcd, examcoursecd ";
        
        return $query;
    }

    //事前判定データ取得
    function getCantcons($model, $coursecd, $majorcd, $examcoursecd, $shdiv)
    {
        $query  = " SELECT ";
        $query .= "     T2.name1 ";
        $query .= " FROM ";
        $query .= "     entexam_applicantcons_dat T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T2 ";
        $query .= " ON ";
        $query .= "     T1.entexamyear = T2.year AND ";
        $query .= "     T1.judgement = T2.namecd2 AND ";
        $query .= "     T2.namecd1 = 'L002' ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.testdiv = '".$model->testdiv."' AND ";
        $query .= "     T1.examno = '".$model->examno."' AND ";
        $query .= "     T1.shdiv = '".$shdiv."' AND ";
        $query .= "     T1.coursecd = '".$coursecd."' AND ";
        $query .= "     T1.majorcd = '".$majorcd."' AND ";
        $query .= "     T1.examcoursecd = '".$examcoursecd."'";
        
        return $query;
    }

    //志願者の氏名を取得
    function get_appliname($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     TESTDIV = '".$model->testdiv."' AND";
        $query .= "     EXAMNO = '".$model->examno."'";  
        
        return $query;
    }

    //事前相談志望データ
    function get_consultation($model, $name, $name_kana,$jhflg)
    {    

        $query  = " SELECT ";
        $query .= "     T1.DATADIV, ";
        $query .= "     T1.WISHNO, ";
        $query .= "     T2.EXAMCOURSE_MARK, ";
        $query .= "     T3.ABBV1 AS SHDIV, ";
        $query .= "     T4.NAME1 AS JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     ENTEXAM_COURSE_MST T2 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.COURSECD = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD = T2.MAJORCD AND ";
        $query .= "     T1.EXAMCOURSECD = T2.EXAMCOURSECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST T3 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.SHDIV = T3.NAMECD2 AND ";
        $query .= "     T3.NAMECD1 = 'L006' ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     V_NAME_MST T4 ";
        $query .= " ON ";
        $query .= "     T1.ENTEXAMYEAR = T4.YEAR AND ";
        $query .= "     T1.JUDGEMENT = T4.NAMECD2 AND ";
        $query .= "     T4.NAMECD1 = 'L002' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "     T1.ACCEPTNO = (SELECT ";
        $query .= "                        WT2.ACCEPTNO ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_APPLICANTBASE_DAT WT1, ";
        $query .= "                        ENTEXAM_CONSULTATION_HDAT WT2 ";
        $query .= "                    WHERE ";
        $query .= "                        WT1.ENTEXAMYEAR = WT2.ENTEXAMYEAR AND ";
        $query .= "                        WT1.TESTDIV = WT2.TESTDIV AND ";
        $query .= "                        WT1.NAME = WT2.NAME AND ";
        $query .= "                        WT1.NAME_KANA = WT2.NAME_KANA AND ";
        //高校のみの条件（出身学校コード）#2005/09/14 arakaki
        if ($jhflg == 0) 
            $query .= "                    WT1.FS_CD = WT2.FS_CD AND ";
        $query .= "                        WT1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "                        WT1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "                        WT1.TESTDIV = '".$model->testdiv."' AND ";
        $query .= "                        WT1.EXAMNO = '".$model->examno."' )";            #2005/09/14 arakaki
#        $query .= "                        WT1.NAME = '".trim($name)."' AND ";
#        $query .= "                        WT1.NAME_KANA = '".trim($name_kana)."' AND ";
#        $query .= "                        WT1.FS_CD = '".$model->fs_cd."' )";
        $query .= " ORDER BY ";
        $query .= "     T1.DATADIV, T1.WISHNO";

        #echo $query;
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.entexamyear,T1.testdiv,T1.examno,T1.shdiv,T1.desirediv, ";
        $query .= "     T1.applicantdiv,T1.name,T1.name_kana,T1.sex,T1.birthday,T1.fs_cd, ";
        $query .= "     T1.fs_grdyear,T1.a_total,T1.a_average,T1.a_total_rank,T1.a_div_rank, ";
        $query .= "     T1.b_total,T1.b_average,T1.b_total_rank,T1.b_div_rank, ";
        $query .= "     T1.judgement,T1.judgement_group_no,T1.interview_attend_flg, ";
        $query .= "     T1.scalashipdiv,T1.suc_coursecd,T1.suc_majorcd, ";
        $query .= "     T1.suc_coursecode,T1.procedurediv,T1.entdiv, ";
        $query .= "     T1.formno,T1.success_noticeno,coalesce(T1.old_success_noticeno,'----') AS old_success_noticeno,T1.failure_noticeno, ";
        $query .= "     T4.confidential_rpt01,T4.confidential_rpt02,T4.confidential_rpt03,T4.confidential_rpt04,T4.confidential_rpt05,T4.confidential_rpt06, ";
        $query .= "     T4.confidential_rpt07,T4.confidential_rpt08,T4.confidential_rpt09,T4.confidential_rpt10,T4.confidential_rpt11,T4.confidential_rpt12, ";
        $query .= "     T4.total_report,T4.average5,T4.average_all, ";
        $query .= "     T5.examhall_name ";
        $query .= " FROM ";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= " LEFT OUTER JOIN entexam_applicantconfrpt_dat T4 ";
        $query .= "      ON T4.entexamyear = T1.entexamyear ";
        $query .= "     AND T4.testdiv = T1.testdiv ";
        $query .= "     AND T4.examno = T1.examno ";
        $query .= " LEFT OUTER JOIN entexam_hall_dat T5 ";
        $query .= "      ON T5.testdiv = T1.testdiv ";
        $query .= "     AND T5.examhallcd = T1.examhallno ";
        $query .= "     AND T1.examno between T5.s_examno AND T5.e_examno ";
        $query .= " WHERE ";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";
        $query .= "      T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "      T1.testdiv = '".$model->testdiv."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query.= " T1.examno < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.examno DESC ";
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query.= " T1.examno > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.examno ASC ";
        } else {
            $query.= " T1.examno = '".$model->examno."'";
        }

#        echo $query;
        return $query;
    }

    
    //得点データ取得
    function getScore($model)
    {
        $query  = " SELECT ";
        $query .= "     testsubclasscd,a_score,a_std_score,a_rank,";
        $query .= "     b_score,b_std_score,b_rank,attend_flg ";
        $query .= " FROM ";
        $query .= "     entexam_score_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year."' AND ";
        $query .= "     testdiv = '".$model->testdiv."' AND ";
        $query .= "     examno = '".$model->examno."'";
        $query .= " ORDER BY ";
        $query .= "     testsubclasscd ";

        return $query;
    }

    //存在チェック
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        TESTDIV = '".$model->testdiv."' AND 
                        EXAMNO = '".$model->examno."'";

        #echo $query;
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //合格・不合格通知No重複チェック  2005/08/16
    #2006/01/13 試験区分,合格通知№,旧合格通知№,不合格通知№全てで重複チェックを行うへ変更。

    function Existdata_Sno($model,$check_no)
    {
        $db = Query::dbCheckOut();

        $query = "
            WITH W_CHECK_NO AS (
                SELECT SUCCESS_NOTICENO  AS CHECK_NO
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$model->year."'
                   AND SUCCESS_NOTICENO IS NOT NULL
                   AND EXAMNO <> '".$model->examno."'
                UNION
                SELECT OLD_SUCCESS_NOTICENO  AS CHECK_NO
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$model->year."'
                   AND OLD_SUCCESS_NOTICENO IS NOT NULL
                   AND EXAMNO <> '".$model->examno."'
                UNION
                SELECT FAILURE_NOTICENO AS CHECK_NO
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$model->year."'
                   AND FAILURE_NOTICENO IS NOT NULL
                   AND EXAMNO <> '".$model->examno."'
            )
            SELECT COUNT(*) AS COUNT FROM W_CHECK_NO
               WHERE CHECK_NO = '".sprintf("%04d",$check_no)."'
        ";

#        $query = "  SELECT ";
#        $query .= "     EXAMNO";
#        $query .= " FROM";
#        $query .= "     ENTEXAM_APPLICANTBASE_DAT";
#        $query .= " WHERE";
#        $query .= "     ENTEXAMYEAR         = '".$model->year."'    AND ";
#        $query .= "     TESTDIV             = '".$model->testdiv."' AND ";
#        $query .= "     EXAMNO             <> '".$model->examno."'  AND ";
#        $query .= "     SUCCESS_NOTICENO    = '".sprintf("%04d",$model->field["SUCCESS_NOTICENO"])."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

#    //不合格通知No重複チェック  2005/08/16
#    function Existdata_Fno($model)
#    {
#        $db = Query::dbCheckOut();
# 
#        $query = "  SELECT ";
#        $query .= "     EXAMNO";
#        $query .= " FROM";
#        $query .= "     ENTEXAM_APPLICANTBASE_DAT";
#        $query .= " WHERE";
#        $query .= "     ENTEXAMYEAR         = '".$model->year."'    AND ";
#        $query .= "     TESTDIV             = '".$model->testdiv."' AND ";
#        $query .= "     EXAMNO             <> '".$model->examno."'  AND ";
#        $query .= "     FAILURE_NOTICENO    = '".sprintf("%04d",$model->field["FAILURE_NOTICENO"])."'";
#        # echo $query;
#        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
#        Query::dbCheckIn($db);
# 
#        return $row;
#    }


    //事前判定の名称を取得
    function get_judge_name($year, $cd)
    {
        $query  = " SELECT name1 ";
        $query .= " FROM v_name_mst ";
        $query .= " WHERE year = '".$year."' AND ";
        $query .= "       namecd1 = '".$cd."'";
        $query .= " ORDER BY ";
        $query .= "     name1 ";
        
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        #2006/01/13 6:繰上合格(コース変更合格)者、合格通知№,旧合格通知№ 制御処理
        #更新前の合格通知№,旧合格通知№を取得
        $query  = "SELECT SUCCESS_NOTICENO     AS SUCCESS_NOTICENO,";
        $query .= "       OLD_SUCCESS_NOTICENO AS OLD_SUCCESS_NOTICENO,";
        $query .= "       SUC_COURSECD,SUC_MAJORCD,SUC_COURSECODE, ";
        $query .= "       JUDGEMENT,JUDGEMENT_GROUP_NO,REGISTERCD,UPDATED ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."'    AND";
        $query .= "       TESTDIV     = '".$model->testdiv."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        $wk_suc_no_b  = (sprintf("%04d",$row["SUCCESS_NOTICENO"])     =='0000'? "" : sprintf("%04d",$row["SUCCESS_NOTICENO"]));
        $wk_osuc_no_b = (sprintf("%04d",$row["OLD_SUCCESS_NOTICENO"]) =='0000'? "" : sprintf("%04d",$row["OLD_SUCCESS_NOTICENO"]));

        #画面上の合格通知№,旧合格通知№を取得
        $wk_suc_no  = (sprintf("%04d",$model->field["SUCCESS_NOTICENO"])    =='0000'? "" : sprintf("%04d",$model->field["SUCCESS_NOTICENO"]));
        $wk_osuc_no = (sprintf("%04d",$model->field["OLD_SUCCESS_NOTICENO"])=='0000'? "" : sprintf("%04d",$model->field["OLD_SUCCESS_NOTICENO"]));

        if ($model->field["JUDGEMENT"] == 6 && $wk_osuc_no_b == "") {  #6:繰上合格(コース変更合格)者で旧合格通知№が空の人
            $wk_osuc_no = $wk_suc_no_b;                                #旧合格通知№作成
            if ($wk_suc_no == $wk_suc_no_b){                           #画面で合格通知№を入力変更していない場合
                $wk_suc_no = "";                                       #格通知№をクリア
            }
        }

        if ($model->field["JUDGEMENT"] != 6 && $wk_osuc_no_b != "") {  
            $wk_osuc_no = "";                                          #旧合格通知№クリア
            $wk_suc_no =$wk_osuc_no_b;                                 #旧合格通知№を合格通知に戻す
        }

        //合格コースが変更になった場合、変更前の合格コースを退避する。
        if ($model->field["SUC_COURSECD"] != $row["SUC_COURSECD"] ||
            $model->field["SUC_MAJORCD"] != $row["SUC_MAJORCD"] ||
            $model->field["SUC_COURSECODE"] != $row["SUC_COURSECODE"]) {

            $query  = "SELECT ";
            $query .= "    SEQ + 1 AS NEWSEQ ";
            $query .= "FROM ";
            $query .= "    ENTEXAM_COURSE_HIST_DAT ";
            $query .= "WHERE ";
            $query .= "    ENTEXAMYEAR = '".$model->year."' ";
            $query .= "    AND TESTDIV = '".$model->testdiv."' ";
            $query .= "    AND EXAMNO = '".$model->examno."' ";
            $query .= "    AND SEQ = ( SELECT ";
            $query .= "                    MAX(SEQ) ";
            $query .= "                FROM ";
            $query .= "                    ENTEXAM_COURSE_HIST_DAT ";
            $query .= "                WHERE ";
            $query .= "                    ENTEXAMYEAR = '".$model->year."' ";
            $query .= "                    AND TESTDIV = '".$model->testdiv."' ";
            $query .= "                    AND EXAMNO = '".$model->examno."') ";

            $newseq = $db->getOne($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAMNO"][TEXT]               = $model->examno;
            $data["SEQ"][NUMBER]                = ($newseq == "") ? 1 : $newseq;
            $data["JUDGEMENT"][TEXT]            = $row["JUDGEMENT"];
            $data["JUDGEMENT_GROUP_NO"][TEXT]   = $row["JUDGEMENT_GROUP_NO"];
            $data["SUC_COURSECD"][TEXT]         = $row["SUC_COURSECD"];
            $data["SUC_MAJORCD"][TEXT]          = $row["SUC_MAJORCD"];
            $data["SUC_COURSECODE"][TEXT]       = $row["SUC_COURSECODE"];
            $data["REGISTERCD"][TEXT]           = $row["REGISTERCD"];
            $data["UPDATED"][TEXT]              = $row["UPDATED"];
            $query = Query::insertSQL($data, "ENTEXAM_COURSE_HIST_DAT");
            $db->query($query);
        }

        //志願者基礎データ
        $data = array();
        $data["SHDIV"][TEXT]                = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]            = $model->field["DESIREDIV"];
        $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
        $data["SEX"][TEXT]                  = $model->field["SEX"];
        $data["JUDGEMENT"][TEXT]            = $model->field["JUDGEMENT"];
        $data["JUDGEMENT_GROUP_NO"][TEXT]   = (strlen($model->field["JUDGEMENT_GROUP_NO"])? sprintf("%02d",$model->field["JUDGEMENT_GROUP_NO"]) : "");
        $data["INTERVIEW_ATTEND_FLG"][TEXT] = $model->field["INTERVIEW_ATTEND_FLG"];
        $data["SCALASHIPDIV"][TEXT]         = $model->field["SCALASHIPDIV"];
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
#        $data["SUCCESS_NOTICENO"][TEXT]     = (strlen($model->field["SUCCESS_NOTICENO"])? sprintf("%04d",$model->field["SUCCESS_NOTICENO"]) : "");
        $data["SUCCESS_NOTICENO"][TEXT]     = $wk_suc_no;
        $data["OLD_SUCCESS_NOTICENO"][TEXT] = $wk_osuc_no;
        $data["FAILURE_NOTICENO"][TEXT]     = (strlen($model->field["FAILURE_NOTICENO"])? sprintf("%04d",$model->field["FAILURE_NOTICENO"]) : "");
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       TESTDIV = '".$model->testdiv."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //志願者事前相談データ
        $result = $db->query(knjl130kQuery::get_examcourse($model->year));
        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $query  = " DELETE ";
            $query .= " FROM ENTEXAM_APPLICANTCONS_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $query .= "       EXAMNO = '".$model->examno."' AND ";
            $query .= "       TESTDIV = '".$model->testdiv."' AND ";
            $query .= "       SHDIV = '1' AND ";
            $query .= "       COURSECD = '".$model->field["COURSECD".$i]."' AND ";
            $query .= "       MAJORCD = '".$model->field["MAJORCD".$i]."' AND ";
            $query .= "       EXAMCOURSECD = '".$model->field["EXAMCOURSECD".$i]."'";

           $db->query($query);

            if ((strlen($model->field["JUDGEMENT1_".$i]))) {
                
                $query   = " SELECT NAMECD2 ";
                $query  .= " FROM V_NAME_MST ";
                $query  .= " WHERE YEAR = '".$model->year."' AND ";
                $query  .= "       NAMECD1 = 'L002' AND ";
                $query .= "        NAME1 = '".$model->field["JUDGEMENT1_".$i]."'";
                $judgement = $db->getOne($query);
            
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                $data["TESTDIV"][TEXT]       = $model->testdiv;
                $data["EXAMNO"][TEXT]        = $model->examno;
                $data["SHDIV"][TEXT]         = "1";
                $data["COURSECD"][TEXT]      = $model->field["COURSECD".$i];
                $data["MAJORCD"][TEXT]       = $model->field["MAJORCD".$i];
                $data["EXAMCOURSECD"][TEXT]  = $model->field["EXAMCOURSECD".$i];
                $data["JUDGEMENT"][TEXT]     = $judgement;
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONS_DAT");
                $db->query($query);

            }

            $query  = " DELETE ";
            $query .= " FROM ENTEXAM_APPLICANTCONS_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
            $query .= "       EXAMNO = '".$model->examno."' AND ";
            $query .= "       TESTDIV = '".$model->testdiv."' AND ";
            $query .= "       SHDIV = '2' AND ";
            $query .= "       COURSECD = '".$model->field["COURSECD".$i]."' AND ";
            $query .= "       MAJORCD = '".$model->field["MAJORCD".$i]."' AND ";
            $query .= "       EXAMCOURSECD = '".$model->field["EXAMCOURSECD".$i]."'";

            $db->query($query);

            if ((strlen($model->field["JUDGEMENT2_".$i]))) {

                $query   = " SELECT NAMECD2 ";
                $query  .= " FROM V_NAME_MST ";
                $query  .= " WHERE YEAR = '".$model->year."' AND ";
                $query  .= "       NAMECD1 = 'L002' AND ";
                $query .= "       NAME1 = '".$model->field["JUDGEMENT2_".$i]."'";
                $judgement = $db->getOne($query);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                $data["TESTDIV"][TEXT]       = $model->testdiv;
                $data["EXAMNO"][TEXT]        = $model->examno;
                $data["SHDIV"][TEXT]         = "2";
                $data["COURSECD"][TEXT]      = $model->field["COURSECD".$i];
                $data["MAJORCD"][TEXT]       = $model->field["MAJORCD".$i];
                $data["EXAMCOURSECD"][TEXT]  = $model->field["EXAMCOURSECD".$i];
                $data["JUDGEMENT"][TEXT]     = $judgement;
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONS_DAT");
                $db->query($query);
            }

            $i++;
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>