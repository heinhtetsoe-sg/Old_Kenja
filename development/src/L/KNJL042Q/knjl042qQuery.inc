<?php

require_once('for_php7.php');

class knjl042qQuery extends Query {
    //名称マスタより取得
    function getNameMst($model, $namecd1, $name3Flg, $ns2Flg=false) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        if ($name3Flg) {
            $query .= "     NAMECD2 || ':' || NAME3 AS LABEL, ";
        } else {
            if ($ns2Flg) {
                $query .= "     NAMECD2 || ':' || NAMESPARE2 AS LABEL, ";
            } else {
                $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            }
        }
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($name3Flg) {
            $query .= " AND NAME3 IS NOT NULL ";
        } else {
            if ($ns2Flg) {
                $query .= " AND NAMESPARE2 IS NOT NULL ";
            } else {
                $query .= " AND NAME1 IS NOT NULL ";
            }
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L051") {
            $query .= "     VALUE(VALUE, 0) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        if (SCHOOLKIND == "P") {
            $query .= "     L050.NAME3 AS TEST_ROOM, ";
            $query .= "     L051.NAME3 AS INTERVIEW_SETTIME, ";
            $query .= "     L052.NAME3 AS INTERVIEW_WAITINGROOM, ";
            $query .= "     L053.NAME3 AS INTERVIEW_ROOM, ";
            $query .= "     L054.NAME3 AS INTERVIEW_GROUP, ";
            $query .= "     RANK() OVER(PARTITION BY T2.INTERVIEW_GROUP ORDER BY T1.EXAMNO) AS GNO ";
        } else {
            $query .= "     L050.NAME1 AS TEST_ROOM, ";
            $query .= "     L051A.NAME1 AS INTERVIEW_SETTIME, ";
            $query .= "     L052.NAME1 AS INTERVIEW_ROOM, ";
            $query .= "     L051B.NAMESPARE2 AS INTERVIEW_ENDTIME, ";
            $query .= "     L054.NAME1 AS INTERVIEW_GROUP ";
        }
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_HALL_DAT T2 ";
        $query .= "              ON T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND T1.EXAMNO       = T2.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L050 ";
        $query .= "              ON T1.ENTEXAMYEAR  = L050.YEAR ";
        $query .= "             AND L050.NAMECD1    = 'L050' ";
        $query .= "             AND T2.TEST_ROOM    = L050.NAMECD2 ";
        if (SCHOOLKIND == "P") {
            $query .= "     LEFT JOIN V_NAME_MST L051 ";
            $query .= "              ON T1.ENTEXAMYEAR       = L051.YEAR ";
            $query .= "             AND L051.NAMECD1         = 'L051' ";
            $query .= "             AND T2.INTERVIEW_SETTIME = L051.NAMECD2 ";
        } else {
            $query .= "     LEFT JOIN V_NAME_MST L051A ";
            $query .= "              ON L051A.YEAR    = T1.ENTEXAMYEAR ";
            $query .= "             AND L051A.NAMECD1 = 'L051' ";
            $query .= "             AND L051A.NAMECD2 = T2.INTERVIEW_SETTIME ";
            $query .= "             AND L051A.NAME1 IS NOT NULL ";
            $query .= "     LEFT JOIN V_NAME_MST L051B ";
            $query .= "              ON L051B.YEAR    = T1.ENTEXAMYEAR ";
            $query .= "             AND L051B.NAMECD1 = 'L051' ";
            $query .= "             AND L051B.NAMECD2 = T2.INTERVIEW_ENDTIME ";
            $query .= "             AND L051B.NAMESPARE2 IS NOT NULL ";
        }
        $query .= "     LEFT JOIN V_NAME_MST L052 ";
        $query .= "              ON T1.ENTEXAMYEAR           = L052.YEAR ";
        $query .= "             AND L052.NAMECD1             = 'L052' ";
        if (SCHOOLKIND == "P") {
            $query .= "             AND T2.INTERVIEW_WAITINGROOM = L052.NAMECD2 ";
        } else {
            $query .= "             AND T2.INTERVIEW_ROOM = L052.NAMECD2 ";
        }
        $query .= "     LEFT JOIN V_NAME_MST L053 ";
        $query .= "              ON T1.ENTEXAMYEAR    = L053.YEAR ";
        $query .= "             AND L053.NAMECD1      = 'L053' ";
        $query .= "             AND T2.INTERVIEW_ROOM = L053.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST L054 ";
        $query .= "              ON T1.ENTEXAMYEAR     = L054.YEAR ";
        $query .= "             AND L054.NAMECD1       = 'L054' ";
        $query .= "             AND T2.INTERVIEW_GROUP = L054.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '0') <> '4' ";  //4:欠席は対象外
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //データチェック
    function checkHallDat($model, $examno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAMNO       = '{$examno}' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $examno) {
            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$examno] != "on") continue;

            $getCount = $db->getOne(knjl042qQuery::checkHallDat($model, $examno));
            $data = array();
            $data["TEST_ROOM"][TEXT]             = $model->test_room;
            $data["INTERVIEW_SETTIME"][TEXT]     = $model->interview_settime;
            //$data["INTERVIEW_WAITINGROOM"][TEXT] = $model->interview_waitingroom;
            $data["INTERVIEW_ROOM"][TEXT]        = $model->interview_room;
            $data["INTERVIEW_ENDTIME"][TEXT]     = $model->interview_endtime;
            $data["INTERVIEW_GROUP"][TEXT]       = $model->interview_group;
            $data["REGISTERCD"][TEXT]            = STAFFCD;
            $data["UPDATED"][FUNC]               = "sysdate()";
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["TESTDIV"][TEXT]      = $model->testdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_HALL_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
                $where .= "   AND EXAMNO        = '".$examno."' ";
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_HALL_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>