<?php

require_once('for_php7.php');

class knjl510aQuery extends Query 
{

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

    }

    //志望区分コンボ
    function getExamcourse($model)
    {
        $query  = "  SELECT ";
        $query .= "    YDAT.HOPE_COURSECODE || ' : ' || MST.HOPE_NAME AS LABEL ";
        $query .= "    , YDAT.HOPE_COURSECODE AS VALUE ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_HOPE_COURSE_YDAT YDAT ";
        $query .= "    INNER JOIN ENTEXAM_HOPE_COURSE_MST MST ";
        $query .= "      ON YDAT.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "      AND YDAT.HOPE_COURSECODE = MST.HOPE_COURSECODE ";
        $query .= "  ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //続柄コンボ
    function getRelationship($model)
    {
        $query  = "  SELECT ";
        $query .= "    NAME1 AS LABEL ,";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "  FROM ";
        $query .= "    NAME_MST ";
        $query .= "  WHERE ";
        $query .= "    NAMECD1 = 'H201' ";

        return $query;
    }

    //出身学校
    function getFinschoolName($model, $cd)
    {
        $query  = "  SELECT ";
        $query .= "    T2.FINSCHOOL_NAME ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_SCHOOL_MST T1 ";
        $query .= "    LEFT JOIN FINSCHOOL_MST T2 ";
        $query .= "      ON T1.FINSCHOOLCD = T2.FINSCHOOLCD ";
        $query .= "  WHERE ";
        $query .= "    T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "    AND T1.ENTEXAM_SCHOOLCD = '".$cd."' ";
        return $query;
    }

    //入試学校コードに対応する出身学校を取得
    function getFsCdSearch($model, $fsCd) {
        $query  = "  SELECT ";
        $query .= "    SM.FINSCHOOL_NAME ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_SCHOOL_MST ESM ";
        $query .= "    INNER JOIN FINSCHOOL_MST SM ";
        $query .= "      ON ESM.FINSCHOOLCD = SM.FINSCHOOLCD ";
        $query .= "  WHERE ";
        $query .= "    ESM.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "    ESM.ENTEXAM_SCHOOLCD = '".$fsCd."' ";

        return $query;

    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query .= "  SELECT ";
        $query .= "    T1.EXAMNO ";
        $query .= "    , T1.TESTDIV ";
        $query .= "    , T1.NAME ";
        $query .= "    , T1.NAME_KANA ";
        $query .= "    , T1.SEX ";
        $query .= "    , T1.BIRTHDAY ";
        $query .= "    , T1.FS_DAY ";
        $query .= "    , T1.FS_CD ";
        $query .= "    , T3.REMARK10 AS HOPE_COURSECODE ";
        $query .= "    , T2.ZIPCD ";
        $query .= "    , T2.ADDRESS1 ";
        $query .= "    , T2.ADDRESS2 ";
        $query .= "    , T2.TELNO ";
        $query .= "    , T2.GNAME ";
        $query .= "    , T2.GKANA ";
        $query .= "    , T2.RELATIONSHIP ";
        $query .= "    , T2.GZIPCD ";
        $query .= "    , T2.GADDRESS1 ";
        $query .= "    , T2.GADDRESS2 ";
        $query .= "    , T2.GTELNO ";
        $query .= " FROM ";
        $query .= "    ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "    LEFT OUTER JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "      ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "    LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3 ";
        $query .= "      ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "      AND T3.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.entexamyear = '".$model->year."' AND ";

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
        $query .= "     entexamyear  = '".$model->year."' AND ";
        $query .= "     applicantdiv = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     examno       = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //存在チェック(ENTEXAM_SCORE_DAT)
    function CntScoreData($model) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND ";
        $query .= "     applicantdiv = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     testdiv      = '".$model->testdiv."' AND ";
        $query .= "     exam_type    = '1' AND ";
        $query .= "     receptno     = '".$model->examno."' ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]           = $model->year;
        $data["APPLICANTDIV"][TEXT]          = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]                = $model->examno;
        $data["TESTDIV"][TEXT]               = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]                 = "1";  //not-null項目なので適当な値を入力
        $data["DESIREDIV"][TEXT]             = "1";  //not-null項目なので適当な値を入力
        $data["NAME"][TEXT]                  = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]             = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]                   = $model->field["SEX"];
        $data["BIRTHDAY"][TEXT]              = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_DAY"][TEXT]                = str_replace("/", "-", $model->field["FS_DAY"]);
        $data["FS_CD"][TEXT]                 = (strlen($model->field["FS_CD"]) ? $model->field["FS_CD"] : "");
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][FUNC]               = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //ENTEXAM_RECEPT_DAT挿入
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["RECEPTNO"][TEXT]         = $this->examno;
        $data["EXAMNO"][TEXT]           = $this->examno;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        $db->query($query);


        //ENTEXAM_APPLICANTBASE_DETAIL_DATにHOPE_COURSECODEを挿入
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]           = $model->year;
        $data["APPLICANTDIV"][TEXT]          = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]                = $model->examno;
        $data["SEQ"][TEXT]                   = "001";
        $data["REMARK10"][TEXT]              = $model->field["HOPE_COURSECODE"];
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][FUNC]               = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
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
        $data["NAME"][TEXT]                  = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]             = $model->field["NAME_KANA"];
        $data["BIRTHDAY"][TEXT]              = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_DAY"][TEXT]                = str_replace("/", "-", $model->field["FS_DAY"]);
        $data["FS_CD"][TEXT]                 = (strlen($model->field["FS_CD"]) ? $model->field["FS_CD"] : "");
        $data["REGISTERCD"][TEXT]            = STAFFCD;
        $data["UPDATED"][FUNC]               = "sysdate()";

        $where  = " WHERE entexamyear = '".$model->year."' AND";
        $where .= "       applicantdiv     = '".$model->field["APPLICANTDIV"]."' AND";
        $where .= "       examno      = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT --DELETE
        $query  = " DELETE FROM entexam_applicantbase_detail_dat ";
        $query .= "       WHERE entexamyear     = '".$model->year."' ";
        $query .= "         AND applicantdiv    = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "         AND examno          = '".$model->examno."' ";
        $query .= "         AND seq             = '001' ";
        $db->query($query);
        
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT --INSERT
        if ($model->field["HOPE_COURSECODE"] != "") {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $model->examno;
            $data["SEQ"][TEXT]           = '001';
            $data["REMARK10"][TEXT]      = $model->field["HOPE_COURSECODE"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT --DELETE
        $query  = " DELETE FROM entexam_applicantaddr_dat ";
        $query .= "       WHERE entexamyear     = '".$model->year."'    AND ";
        $query .= "             applicantdiv    = '1'       AND ";
        $query .= "             examno          = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT --INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);


        //ENTEXAM_RECEPT_DAT --SELECT
        $query  = "  SELECT ";
        $query .= "  * ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_RECEPT_DAT  ";
        $query .= "        WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "        AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "        AND EXAM_TYPE = '1' ";
        $query .= "        AND RECEPTNO = '".$model->examno."' ";
        $receptRow = array();
        $receptRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //ENTEXAM_RECEPT_DAT --DELETE
        $query  = "  DELETE ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_RECEPT_DAT  ";
        $query .= "        WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "        AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "        AND EXAM_TYPE = '1' ";
        $query .= "        AND RECEPTNO = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT --INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["RECEPTNO"][TEXT]         = $model->examno;
        $data["EXAMNO"][TEXT]           = $model->examno;
        $data["TOTAL4"][TEXT]           = $receptRow["TOTAL4"];
        $data["AVARAGE4"][TEXT]         = $receptRow["AVARAGE4"];
        $data["TOTAL_RANK4"][TEXT]      = $receptRow["TOTAL_RANK4"];
        $data["JUDGEDIV"][TEXT]         = $receptRow["JUDGEDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "SYSDATE()";
        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        $db->query($query);

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
        $query.= "       WHERE entexamyear = '".$model->year."' AND applicantdiv = '".$model->field["APPLICANTDIV"]."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query = " DELETE FROM entexam_applicantbase_detail_dat ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND applicantdiv = '".$model->field["APPLICANTDIV"]."' AND examno = '".$model->examno."' AND seq = '001' ";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query = " DELETE FROM entexam_applicantaddr_dat ";
        $query.= "       WHERE entexamyear = '".$model->year."' AND applicantdiv = '".$model->field["APPLICANTDIV"]."' AND examno = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $cnt = $db->getOne(knjl510aQuery::CntScoreData($model));
        if ($cnt == 0) {
            $query = " DELETE FROM entexam_recept_dat ";
            $query.= "       WHERE entexamyear = '".$model->year."' AND applicantdiv = '".$model->field["APPLICANTDIV"]."' AND testdiv = '".$model->field["TESTDIV"]."' AND exam_type = '1' AND receptno = '".$model->examno."' ";
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>