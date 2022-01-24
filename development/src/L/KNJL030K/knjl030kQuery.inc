<?php

require_once('for_php7.php');

class knjl030kQuery extends Query 
{

    //データ重複チェック
#    function CheckData($model)
#    {
#        $db = Query::dbCheckOut();
# 
#        $query  = " SELECT ";
#        $query .= "     applicantdiv, ";
#        $query .= "     testdiv, ";
#        $query .= "     examno ";
#        $query .= " FROM ";
#        $query .= "     entexam_applicantbase_dat ";
#        $query .= " WHERE ";
#        $query .= "     entexamyear = '".$model->year."' AND ";
#        $query .= "     examno      = '".$model->examno."' AND ";
#        $query .= "     testdiv    <> '".$model->testdiv."' ";
# #        $query .= "     (applicantdiv <> '".$model->applicantdiv."' or testdiv <> '".$model->testdiv."') ";
# 
#        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
#        Query::dbCheckIn($db);
# 
#        return $row;
#    }

    //名称マスタより取得
    function getNamecd($year, $cd, $notCd = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($notCd) {
            $query .= "     AND NAMECD2 <> '".$notCd."' ";
        }

        return $query;
    }

    //学校コード
    function getFscd(&$model)
#    function getFscd($year)    #2006/01/30
    {

        $query  = " SELECT FS_CD          AS FINSCHOOLCD,   ";
        $query .= "        FINSCHOOL_NAME AS FINSCHOOL_NAME ";
        $query .= " FROM ( ";
        $query .= "    SELECT FINSCHOOLCD    AS FS_CD, ";
        $query .= "           FINSCHOOL_NAME AS FINSCHOOL_NAME ";
        $query .= "      FROM V_FINSCHOOL_MST ";
        $query .= "     WHERE YEAR='".$model->year ."'  ";
        $query .= "   UNION  ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           t1.FS_CD          AS FS_CD,  ";
        $query .= "           t2.FINSCHOOL_NAME AS FINSCHOOL_NAME   ";
        $query .= "      FROM  ";
        $query .= "            ENTEXAM_APPLICANTBASE_DAT         t1  ";
        $query .= "            LEFT OUTER JOIN V_FINSCHOOL_MST   t2  ";
        $query .= "            ON   t2.YEAR        = t1.ENTEXAMYEAR  ";
        $query .= "            AND  t2.FINSCHOOLCD = t1.FS_CD  ";
        $query .= "     WHERE  ";
        $query .= "            t1.ENTEXAMYEAR      = '".$model->year ."'     AND  ";
        $query .= "            t1.TESTDIV          = '".$model->testdiv ."'  AND  ";
        $query .= "            t1.FS_CD            IS NOT NULL AND  ";
        $query .= "            t2.FINSCHOOL_NAME   IS NULL  ";
        $query .= "      ) T01 ";
        $query .= "ORDER BY FS_CD  ";
        return $query;

#        return " SELECT finschoolcd,finschool_name FROM v_finschool_mst WHERE year = '".$year."' ";    #2006/01/30
    }

    //志望学科名取得
    function getCourseMajor($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.coursecd, ";
        $query .= "     T1.majorcd, ";
        $query .= "     T2.coursename, ";
        $query .= "     T3.majorname ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst T1, ";
        $query .= "     v_course_mst T2, ";
        $query .= "     v_major_mst T3 ";
        $query .= " WHERE ";
        $query .= "     T1.coursecd    = T2.coursecd AND ";
        $query .= "     T1.majorcd     = T3.majorcd AND ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.testdiv     = '".$model->testdiv."' AND ";
        $query .= "     T1.desirediv   = '".$model->desirediv."' AND ";
        $query .= "     T1.wishno      = '1' ";
     
        return $query;
    }

    //志望区分コンボ
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
        $query .= "     T1.coursecd = T2.coursecd AND ";
        $query .= "     T1.majorcd = T2.majorcd AND ";
        $query .= "     T1.examcoursecd = T2.examcoursecd AND ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.testdiv = '".$model->testdiv."' AND ";
        $query .= "     T2.entexamyear = '".$model->year."' ";	//2006.01.15 alp m-yama
        $query .= " GROUP BY ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.examcoursecd, ";
        $query .= "     T2.examcourse_name ";
        $query .= " ORDER BY ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.wishno, ";
        $query .= "     T1.examcoursecd ";
#        echo $query;

        return $query;
    }

    //出身塾名称
    function selectQueryPrischool(&$model)
    {
        $query  = " SELECT PS_CD, ";
        $query .= "       PRISCHOOL_NAME ";
        $query .= "  FROM (";
        $query .= "     SELECT PRISCHOOLCD    AS PS_CD, ";
        $query .= "            PRISCHOOL_NAME AS PRISCHOOL_NAME ";
        $query .= "       FROM V_PRISCHOOL_MST ";
        $query .= "      WHERE YEAR='".$model->year ."' ";
        $query .= "    UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "            t1.PS_CD          AS PS_CD, ";
        $query .= "            t2.PRISCHOOL_NAME AS PRISCHOOL_NAME  ";
        $query .= "       FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_DAT   t1 ";
        $query .= "             LEFT OUTER JOIN V_PRISCHOOL_MST   t2 ";
        $query .= "             ON   t2.YEAR        = t1.ENTEXAMYEAR ";
        $query .= "             AND  t2.PRISCHOOLCD = t1.PS_CD ";
        $query .= "      WHERE ";
        $query .= "             t1.ENTEXAMYEAR      = '". $model->year ."'    AND ";
        $query .= "             t1.TESTDIV          = '" .$model->testdiv ."' AND ";
        $query .= "             t1.PS_CD            IS NOT NULL AND ";
        $query .= "             t2.PRISCHOOL_NAME   IS NULL ";
        $query .= "       ) T01";
        $query .= " ORDER BY PS_CD ";
        return $query;

#2006/01/30
#        $query = " SELECT DISTINCT ";
#        $query .= "     t2.ps_cd, ";
#        $query .= "     t1.prischool_name ";
#        $query .= " FROM ";
#        $query .= "     prischool_mst             t1, ";
#        $query .= "     entexam_consultation_hdat t2 ";
#        $query .= " WHERE ";
#        $query .= "     t2.entexamyear  = '". $model->year ."' AND ";
#        $query .= "     t2.testdiv      = '" .$model->testdiv ."' AND ";
#        $query .= "     t1.prischoolcd  = t2.ps_cd ";
#        $query .= " ORDER BY ";
#        $query .= "     ps_cd ";
#        return $query;
    }

    //クラブ名称取得 2006.01.17 alp m-yama
    function getClubcd(&$model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "     CLUBCD, ";
        $query .= "     CLUBNAME ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        $query .= " ORDER BY ";
        $query .= "     CLUBCD ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.examno, ";
        $query .= "     T1.testdiv, ";
        $query .= "     T1.formno, ";
        $query .= "     T1.shdiv, ";
        $query .= "     T1.desirediv, ";
        $query .= "     T1.applicantdiv, ";
        $query .= "     T1.clubcd, ";	//2006.01.17 alp m-yama
        $query .= "     T1.SPECIAL_REASON_DIV, ";
        $query .= "     T1.name, ";
        $query .= "     T1.name_kana, ";
        $query .= "     T1.sex, ";
        $query .= "     T1.birthday, ";
        $query .= "     T1.addresscd, ";
        $query .= "     T1.telno, ";
        $query .= "     T1.locationcd, ";
        $query .= "     T1.natpubpridiv, ";
        $query .= "     T1.fs_cd, ";
        $query .= "     T1.ps_cd, ";
        $query .= "     T1.fs_grdyear, ";
        $query .= "     T1.gname, ";
        $query .= "     T1.gkana, ";
        $query .= "     T1.gtelno, ";
        $query .= "     T2.zipcd, ";
        $query .= "     T2.address, ";
        $query .= "     T2.gzipcd, ";
        $query .= "     T2.gaddress, ";
        $query .= "     T1.approval_flg ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat T1 ";
        $query .= "     LEFT OUTER JOIN entexam_applicantaddr_dat T2 ";
        $query .= "                  ON T2.entexamyear = T1.entexamyear ";
        $query .= "                 AND T2.testdiv = T1.testdiv ";
        $query .= "                 AND T2.examno = T1.examno ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";
        $query .= "     T1.testdiv     = '".$model->testdiv."' AND ";

        if($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "  T1.examno < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.examno DESC ";
        }elseif($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "  T1.examno > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.examno ASC ";
        }else{
            $query.= "  T1.examno = '".$model->examno."'";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        return $row;
    }

    //存在チェック
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     examno ";
        $query .= " FROM ";
        $query .= "     entexam_applicantbase_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year."' AND ";
        $query .= "     testdiv     = '".$model->testdiv."' AND ";
        $query .= "     examno      = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }


    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]           = $model->year;
        $data["TESTDIV"][TEXT]               = $model->field["TESTDIV"];
        $data["EXAMNO"][TEXT]                = $model->examno;
        $data["SHDIV"][TEXT]                 = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]             = $model->field["DESIREDIV"];
        $data["APPLICANTDIV"][TEXT]          = $model->field["APPLICANTDIV"];
        $data["CLUBCD"][TEXT]                = $model->field["CLUBCD"];	//2006.01.17 alp m-yama
        $data["SPECIAL_REASON_DIV"][TEXT]    = $model->field["SPECIAL_REASON_DIV"];
        $data["NAME"][TEXT]                  = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]             = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]                   = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]              = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["ADDRESSCD"][TEXT]             = $model->field["ADDRESSCD"];
        $data["TELNO"][TEXT]                 = $model->field["TELNO"];
        $data["LOCATIONCD"][TEXT]            = $model->field["LOCATIONCD"];
        $data["NATPUBPRIDIV"][TEXT]          = $model->field["NATPUBPRIDIV"];
        $data["FS_CD"][TEXT]                 = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_GRDYEAR"][TEXT]            = $model->field["FS_GRDYEAR"];
        $data["PS_CD"][TEXT]                 = $model->field["PS_CD"];
        $data["GNAME"][TEXT]                 = $model->field["GNAME"];
        $data["GKANA"][TEXT]                 = $model->field["GKANA"];
        $data["GTELNO"][TEXT]                = $model->field["GTELNO"];
        $data["FORMNO"][TEXT]                = (strlen($model->field["FORMNO"]) ? sprintf("%06d",$model->field["FORMNO"]) : "");
        /*** ADD 2005/11/15 by ameku ***/
        $data["APPROVAL_FLG"][TEXT]        = $model->field["APPROVAL_FLG"];
        /*** ADD 2005/11/15 by ameku ***/
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        if ($model->field["ZIPCD"] == "" && $model->field["ADDRESS"] == "" &&
            $model->field["GZIPCD"] == "" && $model->field["GADDRESS"] == "") {
            $query = " DELETE FROM entexam_applicantaddr_dat ";
            $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->field["TESTDIV"]."' AND examno = '".$model->examno."' ";
        } else {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $data["EXAMNO"][TEXT]        = $model->examno;
            $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
            $data["ADDRESS"][TEXT]       = $model->field["ADDRESS"];
            $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
            $data["GADDRESS"][TEXT]      = $model->field["GADDRESS"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["TESTDIV"][TEXT]               = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]                 = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]             = $model->field["DESIREDIV"];
        $data["APPLICANTDIV"][TEXT]          = $model->field["APPLICANTDIV"];
        $data["CLUBCD"][TEXT]                = $model->field["CLUBCD"];	//2006.01.17 alp m-yama
        $data["SPECIAL_REASON_DIV"][TEXT]    = $model->field["SPECIAL_REASON_DIV"];
        $data["NAME"][TEXT]                  = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]             = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]                   = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]              = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["ADDRESSCD"][TEXT]             = $model->field["ADDRESSCD"];
        $data["TELNO"][TEXT]                 = $model->field["TELNO"];
        $data["LOCATIONCD"][TEXT]            = $model->field["LOCATIONCD"];
        $data["NATPUBPRIDIV"][TEXT]          = $model->field["NATPUBPRIDIV"];
        $data["FS_CD"][TEXT]                 = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_GRDYEAR"][TEXT]            = $model->field["FS_GRDYEAR"];
        $data["PS_CD"][TEXT]                 = $model->field["PS_CD"];
        $data["GNAME"][TEXT]                 = $model->field["GNAME"];
        $data["GKANA"][TEXT]                 = $model->field["GKANA"];
        $data["GTELNO"][TEXT]                = $model->field["GTELNO"];
        $data["FORMNO"][TEXT]                = (strlen($model->field["FORMNO"]) ? sprintf("%06d",$model->field["FORMNO"]) : "");
        /*** ADD 2005/11/15 by ameku ***/
        $data["APPROVAL_FLG"][TEXT]        = $model->field["APPROVAL_FLG"];
        /*** ADD 2005/11/15 by ameku ***/
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE entexamyear = '".$model->year."' AND";
        $where .= "       testdiv     = '".$model->testdiv."' AND";
        $where .= "       examno      = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT --DELETE
        $query  = " DELETE FROM entexam_applicantaddr_dat ";
        $query .= "       WHERE entexamyear = '".$model->year."' ";
        $query .= "         AND testdiv     = '".$model->testdiv."' ";
        $query .= "         AND examno      = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT --INSERT
        if ($model->field["ZIPCD"] != "" || $model->field["ADDRESS"] != "" ||
            $model->field["GZIPCD"] != "" || $model->field["GADDRESS"] != "") {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $data["EXAMNO"][TEXT]        = $model->examno;
            $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
            $data["ADDRESS"][TEXT]       = $model->field["ADDRESS"];
            $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
            $data["GADDRESS"][TEXT]      = $model->field["GADDRESS"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query = " DELETE FROM entexam_applicantbase_dat ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->testdiv."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query = " DELETE FROM entexam_applicantaddr_dat ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->testdiv."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query = " DELETE FROM entexam_applicantconfrpt_dat ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->testdiv."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query = " DELETE FROM entexam_score_dat ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->testdiv."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_JUDGE_TMP
        $query = " DELETE FROM entexam_judge_tmp ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->testdiv."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_COURSE_HIST_DAT
        $query = " DELETE FROM ENTEXAM_COURSE_HIST_DAT ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND testdiv = '".$model->testdiv."' AND examno = '".$model->examno."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>