<?php

require_once('for_php7.php');

class knjb0090Query extends Query
{

    //FromToの年度・学期を取得
    public function getYearSemester($model)
    {
        $query  = "";
        $query .= "SELECT YEAR||SEMESTER AS VALUE,YEAR||'年度'||SEMESTERNAME||'　' AS LABEL ";
        $query .= "FROM   SEMESTER_MST ";
        $query .= "WHERE  SEMESTER NOT IN('9') ";
        $query .= "ORDER BY VALUE ";

        return $query;
    }

    //Fromの基本時間割一覧
    public function getTitle($year_semester)
    {
        $query  = "";
        $query .= "SELECT BSCSEQ,TITLE ";
        $query .= "FROM   SCH_PTRN_HDAT ";
        $query .= "WHERE  YEAR||SEMESTER = '". $year_semester ."' ";
        $query .= "ORDER BY BSCSEQ ";

        return $query;
    }

    //反映履歴
    public function getReflecthist($model)
    {
        $query  = "";
        $query .= "SELECT W1.REFLECTDATE,W1.REFLECTDIV,W1.SDATE,W1.EDATE,W1.DAYCD,W1.DAYS ";
        $query .= "      ,W2.BSCSEQ,W2.TITLE ";
        $query .= "FROM   PTRN_REFLECTHIST_DAT W1, ";
        $query .= "       SCH_PTRN_HDAT W2 ";
        $query .= "WHERE  W2.YEAR||W2.SEMESTER = '". $model->from_year_semester ."' AND ";
        $query .= "       W2.YEAR = W1.YEAR AND ";
        $query .= "       W2.BSCSEQ = W1.BSCSEQ ";
        $query .= "ORDER BY W1.REFLECTDATE DESC ";

        return $query;
    }

    //コピー時のＳＥＱを取得（To年度のMAX+1）
    public function getSeqMax($model)
    {
        $query  = "";
        $query .= "SELECT VALUE(MAX(BSCSEQ),0)+1 AS TO_BSCSEQ ";
        $query .= "FROM   SCH_PTRN_HDAT ";
        $query .= "WHERE  YEAR = '". substr($model->to_year_semester, 0, -1) ."' ";

        return $query;
    }

    //コピー処理
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //時間割パターンヘッダデータ------------------------------------------------------
        $query = knjb0090Query::insertPtrnHeaderQuery($model);
        $db->query($query);

        //時間割パターンデータ------------------------------------------------------
        $query = knjb0090Query::insertPtrnQuery($model);
        $db->query($query);

        //時間割パターン職員データ------------------------------------------------------
        //時間割担当チェックボックス
        if ($model->stf_chk == "on") {
            $query = knjb0090Query::insertPtrnStaffQuery($model);
            $db->query($query);
        }

        //時間割パターン集計フラグ------------------------------------------------------
        $result = $db->query(knjb0090Query::getPtrnClsQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjb0090Query::insertPtrnCountflgQuery($model, $row);
            $db->query($query);
        }
        $result->free();

        $db->commit();

        //コピー後のデータ件数を取得（表示用）
        $seq_cnt = $db->getOne(knjb0090Query::getSeqDataCount($model));

        Query::dbCheckIn($db);

        return $seq_cnt;
    }

    //時間割パターンヘッダデータ
    public function insertPtrnHeaderQuery($model)
    {
        $data["YEAR"][TEXT]         = substr($model->to_year_semester, 0, -1);
        $data["SEMESTER"][TEXT]     = substr($model->to_year_semester, -1);
        $data["BSCSEQ"][NUMBER]     = $model->to_seq;
        $data["TITLE"][TEXT]        = $model->to_title;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = "";
        $query = Query::insertSQL($data, "sch_ptrn_hdat");

        return $query;
    }

    //時間割パターンデータ
    public function insertPtrnQuery($model)
    {
        $query  = "";
        $query .= "INSERT INTO SCH_PTRN_DAT ";
        $query .= "SELECT  '". substr($model->to_year_semester, 0, -1) ."' ";//To年度
        $query .= "       ,'". substr($model->to_year_semester, -1) ."' ";//To学期
        $query .= "       ,". $model->to_seq ." ";//To年度のMAX+1
        $query .= "       ,W1.DAYCD ";
        $query .= "       ,W1.PERIODCD ";
        $query .= "       ,W1.CHAIRCD ";
        $query .= "       ,W1.DECISIONDIV ";
        $query .= "       ,'". STAFFCD ."' ";
        $query .= "       ,SYSDATE() ";
        $query .= "FROM   SCH_PTRN_DAT W1, ";
        $query .= "       CHAIR_DAT W2 ";
        $query .= "WHERE  W1.YEAR||W1.SEMESTER = '". $model->from_year_semester ."' AND ";
        $query .= "       W1.BSCSEQ = ". $model->from_seq ." AND ";
        //Toに存在する講座コードの一致する講座のみコピー
        $query .= "       W2.YEAR||W2.SEMESTER = '". $model->to_year_semester ."' AND ";
        $query .= "       W2.CHAIRCD = W1.CHAIRCD ";

        return $query;
    }

    //時間割パターン職員データ
    public function insertPtrnStaffQuery($model)
    {
        $query  = "";
        $query .= "INSERT INTO SCH_PTRN_STF_DAT ";
        $query .= "SELECT  '". substr($model->to_year_semester, 0, -1) ."' ";//To年度
        $query .= "       ,'". substr($model->to_year_semester, -1) ."' ";//To学期
        $query .= "       ,". $model->to_seq ." ";//To年度のMAX+1
        $query .= "       ,W1.DAYCD ";
        $query .= "       ,W1.PERIODCD ";
        $query .= "       ,W1.CHAIRCD ";
        $query .= "       ,W1.STAFFCD ";
        $query .= "       ,'". STAFFCD ."' ";
        $query .= "       ,SYSDATE() ";
        $query .= "FROM   SCH_PTRN_STF_DAT W1, ";
        $query .= "       CHAIR_DAT W2 ";
        $query .= "WHERE  W1.YEAR||W1.SEMESTER = '". $model->from_year_semester ."' AND ";
        $query .= "       W1.BSCSEQ = ". $model->from_seq ." AND ";
        //Toに存在する講座コードの一致する講座のみコピー
        $query .= "       W2.YEAR||W2.SEMESTER = '". $model->to_year_semester ."' AND ";
        $query .= "       W2.CHAIRCD = W1.CHAIRCD ";
        //To年度に存在する職員のみコピー（年度跨りの時）
        if (substr($model->from_year_semester, 0, -1) != substr($model->to_year_semester, 0, -1)) {
            $query .= "   AND W1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT ";
            $query .= "                      WHERE  YEAR = '". substr($model->to_year_semester, 0, -1) ."') ";
        }

        return $query;
    }

    //時間割パターン・受講クラス取得
    public function getPtrnClsQuery($model)
    {
        //基本時間割
        $query  = " WITH PTRN_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCH_PTRN_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR || SEMESTER = '".$model->to_year_semester."' AND ";
        $query .= "         BSCSEQ = ".$model->to_seq." ";
        $query .= " ) ";

        //受講クラス＆集計フラグ
        $query .= " , CHAIR_CLS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T1.TRGTGRADE, ";
        $query .= "         T1.TRGTCLASS, ";
        $query .= "         T2.COUNTFLG ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CLS_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = T2.YEAR || T2.SEMESTER AND ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->to_year_semester."' AND ";
        $query .= "         T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "         T1.GROUPCD = '0000' ";
        $query .= " UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T1.TRGTGRADE, ";
        $query .= "         T1.TRGTCLASS, ";
        $query .= "         T2.COUNTFLG ";
        $query .= "     FROM ";
        $query .= "         CHAIR_CLS_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = T2.YEAR || T2.SEMESTER AND ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->to_year_semester."' AND ";
        $query .= "         T1.GROUPCD = T2.GROUPCD AND ";
        $query .= "         T1.CHAIRCD = '0000000' ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.BSCSEQ, ";
        $query .= "     T1.DAYCD, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T2.TRGTGRADE AS GRADE, ";
        $query .= "     T2.TRGTCLASS AS HR_CLASS, ";
        $query .= "     T2.COUNTFLG ";
        $query .= " FROM ";
        $query .= "     PTRN_DATA T1, ";
        $query .= "     CHAIR_CLS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD ";

        return $query;
    }

    //時間割パターンデータ集計フラグ
    public function insertPtrnCountflgQuery($model, $row)
    {
        $data["YEAR"][TEXT]         = $row["YEAR"];
        $data["SEMESTER"][TEXT]     = $row["SEMESTER"];
        $data["BSCSEQ"][NUMBER]     = $row["BSCSEQ"];
        $data["DAYCD"][TEXT]        = $row["DAYCD"];
        $data["PERIODCD"][TEXT]     = $row["PERIODCD"];
        $data["CHAIRCD"][TEXT]      = $row["CHAIRCD"];
        $data["GRADE"][TEXT]        = $row["GRADE"];
        $data["HR_CLASS"][TEXT]     = $row["HR_CLASS"];
        $data["COUNTFLG"][TEXT]     = $row["COUNTFLG"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "SCH_PTRN_COUNTFLG_DAT");

        return $query;
    }

    //コピー後のデータ件数を取得（表示用）
    public function getSeqDataCount($model)
    {
        $query  = "";
        $query .= "SELECT COUNT(*) AS SEQ_CNT ";
        $query .= "FROM   SCH_PTRN_DAT ";
        $query .= "WHERE  YEAR||SEMESTER = '". $model->to_year_semester ."' AND ";
        $query .= "       BSCSEQ = ". $model->to_seq ." ";

        return $query;
    }

    //一致する講座の存在チェック
    public function getExists($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT COUNT(*) AS CHAIR_CNT ";
        $query .= "FROM   SCH_PTRN_DAT W1, ";
        $query .= "       CHAIR_DAT W2 ";
        $query .= "WHERE  W1.YEAR||W1.SEMESTER = '". $model->from_year_semester ."' AND ";
        $query .= "       W1.BSCSEQ = ". $model->from_seq ." AND ";
        //Toに存在する講座コードの一致する講座のみコピー
        $query .= "       W2.YEAR||W2.SEMESTER = '". $model->to_year_semester ."' AND ";
        $query .= "       W2.CHAIRCD = W1.CHAIRCD ";

        $chair_cnt = $db->getOne($query);

        Query::dbCheckIn($db);

        return $chair_cnt;
    }
}
