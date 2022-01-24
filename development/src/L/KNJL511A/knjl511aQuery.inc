<?php

require_once('for_php7.php');

class knjl511aquery extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //データ件数取得
            $base_cnt   = $db->getOne(knjl511aQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt   = $db->getOne(knjl511aQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $detail_cnt = $db->getOne(knjl511aQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i]));

            //更新 -- ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $data_arr[$i]["TESTDIV"];
            $data["SHDIV"][TEXT]                = $model->field["SHDIV"];
            $data["DESIREDIV"][TEXT]            = $model->field["DESIREDIV"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = ($data_arr[$i]["SEX"]) ? $data_arr[$i]["SEX"] : $model->defaultVal["SEX"];
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_DAY"][TEXT]               = ($data_arr[$i]["FS_DAY"]) ? $data_arr[$i]["FS_DAY"] : $model->defaultVal["FS_DAY"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //更新 -- ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GZIPCD"][TEXT]           = ($data_arr[$i]["GZIPCD"]) ? $data_arr[$i]["GZIPCD"] : $data_arr[$i]["ZIPCD"];
            $data["GADDRESS1"][TEXT]        = ($data_arr[$i]["GADDRESS1"]) ? $data_arr[$i]["GADDRESS1"] : $data_arr[$i]["ADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = ($data_arr[$i]["GTELNO"]) ? $data_arr[$i]["GTELNO"] : $data_arr[$i]["TELNO"];
            $data["RELATIONSHIP"][TEXT]     = ($data_arr[$i]["RELATIONSHIP"]) ? $data_arr[$i]["RELATIONSHIP"] : $model->defaultVal["RELATIONSHIP"];;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //更新 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = '001';
            $data["REMARK10"][TEXT]         = $data_arr[$i]["HOPE_COURSECODE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $detail_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);


            //ENTEXAM_RECEPT_DAT --SELECT
            $query  = "  SELECT ";
            $query .= "  * ";
            $query .= "  FROM ";
            $query .= "    ENTEXAM_RECEPT_DAT  ";
            $query .= "        WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";
            $query .= "        AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "        AND EXAM_TYPE = '1' ";
            $query .= "        AND RECEPTNO = '".$data_arr[$i]["EXAMNO"]."' ";
            $receptRow = array();
            $receptRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //更新 -- ENTEXAM_RECEPT_DAT
            //DELETE
            $query  = "  DELETE ";
            $query .= "  FROM ";
            $query .= "    ENTEXAM_RECEPT_DAT ";
            $query .= "  WHERE ";
            $query .= "    ENTEXAMYEAR = '{$model->ObjYear}' AND ";
            $query .= "    APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND ";
            $query .= "    EXAM_TYPE = '1' AND ";
            $query .= "    RECEPTNO = '{$data_arr[$i]["EXAMNO"]}' ";
            $db->query($query);

            //INSERT
            $data = array(); 
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = "1";
            $data["RECEPTNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TOTAL4"][TEXT]           = $receptRow["TOTAL4"];
            $data["AVARAGE4"][TEXT]         = $receptRow["AVARAGE4"];
            $data["TOTAL_RANK4"][TEXT]      = $receptRow["TOTAL_RANK4"];
            $data["JUDGEDIV"][TEXT]         = $receptRow["JUDGEDIV"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function deleteQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //データ件数取得
            $base_cnt   = $db->getOne(knjl511aQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt   = $db->getOne(knjl511aQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $detail_cnt = $db->getOne(knjl511aQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i]));
            $score_cnt  = $db->getOne(knjl511aQuery::cntEntexamScoreDat($model, $data_arr[$i]));

            //ENTEXAM_APPLICANTBASE_DAT --DELETE
            if ($base_cnt != 0) {
                $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
                $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' ";
                $db->query($query);
            }

            //ENTEXAM_APPLICANTADDR_DAT --DELETE
            if ($addr_cnt != 0) {
                $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
                $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
                $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' ";
                $db->query($query);
            }

             //ENTEXAM_APPLICANTBASE_DETAIL_DAT --DELETE
            if ($detail_cnt != 0) {
                $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                $query .= "       WHERE ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
                $query .= "             APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
                $query .= "             EXAMNO          = '".$data_arr[$i]["EXAMNO"]."' AND ";
                $query .= "             SEQ             = '001' ";
                $db->query($query);
            }

            //ENTEXAM_RECEPT_DAT --DELETE
            if ($score_cnt == 0) {
                $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
                $query .= "       WHERE ENTEXAMYEAR      = '{$model->ObjYear}' AND ";
                $query .= "             APPLICANTDIV     = '{$model->field["APPLICANTDIV"]}' AND ";
                $query .= "             EXAM_TYPE        = '1' AND ";
                $query .= "             RECEPTNO         = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }


    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    function nameCntSql($namecd, $setcd, $where="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //出身学校コードチェック
    function fs_cdCntSql($examyear, $fs_cd) {
        $query .= "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_SCHOOL_MST ";
        $query .= "  WHERE ";
        $query .= "    ENTEXAMYEAR = '".$examyear."' ";
        $query .= "    AND ENTEXAM_SCHOOLCD = '".$fs_cd."' ";

        return $query;
    }

    //志望区分チェック
    function hopeCourseCdCntSql($examyear, $desirediv) {
        $query .= "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_HOPE_COURSE_YDAT ";
        $query .= "  WHERE ";
        $query .= "    ENTEXAMYEAR = '".$examyear."' AND ";
        $query .= "    HOPE_COURSECODE = '".$desirediv."' ";

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/

    //名称マスタより取得
    function getNameMstAll($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    function cntEntexamApplicantbaseDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    function cntEntexamApplicantaddrDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    function cntEntexamApplicantbaseDetailDat($model, $data_arr) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '001' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_SCORE_DAT
    function cntEntexamScoreDat($model, $data_arr) {
        $query  = "  SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_SCORE_DAT ";
        $query .= "  WHERE ";
        $query .= "    ENTEXAMYEAR      = '{$model->ObjYear}' AND ";
        $query .= "    APPLICANTDIV     = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "    TESTDIV          = '{$data_arr["TESTDIV"]}' AND ";
        $query .= "    EXAM_TYPE        = '1' AND ";
        $query .= "    RECEPTNO         = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //出身学校名取得
    function getFsName($data_arr) {
        $query  = " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$data_arr["FS_CD"]}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //ヘッダ例用データを名称マスタより取得
    function getHeaderReiNameMst($year, $namecd1, $name) {
        $query  = " SELECT DISTINCT ";
        $query .= "     {$name} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ヘッダ例用データを志望区分より取得
    function getHeaderReiHopeCourseCd($model, $testdiv) {
        $query  = "  SELECT ";
        $query .= "    YDAT.HOPE_COURSECODE || ' : ' || MST.HOPE_NAME AS LABEL ";
        $query .= "    , YDAT.HOPE_COURSECODE AS VALUE ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_HOPE_COURSE_YDAT YDAT ";
        $query .= "    INNER JOIN ENTEXAM_HOPE_COURSE_MST MST ";
        $query .= "      ON YDAT.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "      AND YDAT.HOPE_COURSECODE = MST.HOPE_COURSECODE ";
        $query .= "  ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {

        $query  = "  SELECT ";
        $query .= "    BASE.ENTEXAMYEAR, ";
        $query .= "    BASE.TESTDIV, ";
        $query .= "    BASE.EXAMNO, ";
        $query .= "    DETAIL.REMARK10 AS HOPE_COURSECODE, ";
        $query .= "    BASE.NAME, ";
        $query .= "    BASE.NAME_KANA, ";
        $query .= "    BASE.SEX, ";
        $query .= "    BASE.BIRTHDAY, ";
        $query .= "    BASE.FS_DAY, ";
        $query .= "    BASE.FS_CD, ";
        $query .= "    ADDR.ZIPCD, ";
        $query .= "    ADDR.ADDRESS1, ";
        $query .= "    ADDR.ADDRESS2, ";
        $query .= "    ADDR.TELNO, ";
        $query .= "    ADDR.GNAME, ";
        $query .= "    ADDR.GKANA, ";
        $query .= "    ADDR.RELATIONSHIP, ";
        $query .= "    ADDR.GZIPCD, ";
        $query .= "    ADDR.GADDRESS1, ";
        $query .= "    ADDR.GADDRESS2, ";
        $query .= "    ADDR.GTELNO ";
        $query .= "  FROM ";
        $query .= "    ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "      ON BASE.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = ADDR.APPLICANTDIV ";
        $query .= "      AND BASE.EXAMNO = ADDR.EXAMNO ";
        $query .= "    LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ";
        $query .= "      ON BASE.ENTEXAMYEAR = DETAIL.ENTEXAMYEAR ";
        $query .= "      AND BASE.APPLICANTDIV = DETAIL.APPLICANTDIV ";
        $query .= "      AND BASE.EXAMNO = DETAIL.EXAMNO ";
        $query .= "      AND DETAIL.SEQ = '001' ";
        $query .= "  WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '".$model->ObjYear."' ";

        return $query;
    }
}
?>
