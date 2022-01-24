<?php

class knjl379iquery extends Query
{
    //名称マスタ取得
    public function getNameMst($nameCd1, $nameCd2)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getEntexamTestDivMst($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //連番 最小、最大取得
    public function getEntexamRowNoMinMax($applicantDiv, $testDiv)
    {
        $query  = "";
        $query .= " WITH RECEPTNUM AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(ORDER BY RECEPTNO) AS NUM, ";
        $query .= "   RECEPTNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV = '{$testDiv}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= " )  ";
        $query .= " SELECT ";
        $query .= "   MIN(NUM) AS MIN, ";
        $query .= "   MAX(NUM) AS MAX ";
        $query .= " FROM ";
        $query .= "   RECEPTNUM ";

        return $query;
    }

    //RECEPT作成の対象となる受験者
    public function getApplicantBaseSelectQuery($applicantDiv, $testDiv)
    {
        $rtnArr = array();

        $db = Query::dbCheckOut();

        $query  = "     SELECT ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "         AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "         AND TESTDIV = '{$testDiv}' ";
        $query .= "         AND (JUDGEMENT IS NULL OR JUDGEMENT NOT IN ('4', '5')) "; //4:欠席, 5:未受験でない受験者が対象

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtnArr[] = $row["EXAMNO"];
        }
        Query::dbCheckIn($db);

        return $rtnArr;
    }

    public function getCapaTotalCnt($applicantDiv, $testDiv)
    {
        $db = Query::dbCheckOut();

        $query  = "     SELECT ";
        $query .= "         VALUE(SUM(CAPA_CNT), 0) AS TOTAL_CAPACITY ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_YDAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "         AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "         AND TESTDIV = '{$testDiv}' ";
        $query .= "         AND EXAM_TYPE = '1' ";
        $query .= "         AND EXAMHALLCD LIKE '2%' ";

        $rtnVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnVal;
    }

    //会場コード、容量取得
    public function getEntexamHallCapa($applicantDiv, $testDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "   EXAMHALLCD,CAPA_CNT ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV = '{$testDiv}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= "   AND EXAMHALLCD LIKE '2%' ";
        $query .= " ORDER BY ";
        $query .= "   EXAMHALLCD ";
        
        return $query;
    }

    //受付番号取得
    public function getEntexamReceptMinMax($applicantDiv, $testDiv, $start, $end)
    {
        $query  = "";
        $query .= " WITH RECEPTNUM AS ( ";
        $query .= " SELECT ";
        $query .= "   ROW_NUMBER() OVER(ORDER BY RECEPTNO) AS NUM, ";
        $query .= "   RECEPTNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV = '{$testDiv}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= " )  ";
        $query .= " SELECT ";
        $query .= "   MIN(RECEPTNO) AS MIN, ";
        $query .= "   MAX(RECEPTNO) AS MAX ";
        $query .= " FROM ";
        $query .= "   RECEPTNUM ";
        $query .= " WHERE  ";
        $query .= "   NUM BETWEEN '{$start}' AND '{$end}' ";
        
        return $query;
    }

    //RECEPT存在
    public function existsCntEntexamReceptDat($applicantDiv, $testDiv, $examNo)
    {
        $receptNo = $examNo; //志願者番号と受験番号は同じ

        $query  = "   SELECT ";
        $query .= "       COUNT(*) AS CNT ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_RECEPT_DAT ";
        $query .= "   WHERE ";
        $query .= "           ENTEXAMYEAR   = " . (CTRL_YEAR + 1);
        $query .= "       AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "       AND TESTDIV       = '{$testDiv}' ";
        $query .= "       AND EXAM_TYPE     = '1' ";
        $query .= "       AND RECEPTNO      = '{$receptNo}' ";

        return $query;
    }

    //RECEPT削除
    public function insertEntexamReceptDat($applicantDiv, $testDiv, $examNo)
    {
        $receptNo = $examNo; //志願者番号と受験番号は同じ

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = CTRL_YEAR + 1;
        $data["APPLICANTDIV"][TEXT]     = $applicantDiv;
        $data["TESTDIV"][TEXT]          = $testDiv;
        $data["EXAM_TYPE"][TEXT]        = '1';
        $data["RECEPTNO"][TEXT]         = $receptNo;
        $data["EXAMNO"][TEXT]           = $examNo;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DAT");
        return $query;
    }

    //ENTEXAM_HALL_YDAT.RECEPTNO 更新
    public function update($applicantDiv, $testDiv, $examArr)
    {
        $start = 0;
        $end = 0;

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_RECEPT_DAT作成
        foreach ($examArr as $examNo) {
            $existsCnt = $db->getOne(knjl379iQuery::existsCntEntexamReceptDat($applicantDiv, $testDiv, $examNo));
            if ($existsCnt == 0) {
                $query = knjl379iQuery::insertEntexamReceptDat($applicantDiv, $testDiv, $examNo);
                $db->query($query);
            }
        }

        $query = knjl379iQuery::getEntexamRowNoMinMax($applicantDiv, $testDiv);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);

        $min = $row["MIN"];
        $max = $row["MAX"];

        $query = knjl379iQuery::getEntexamHallCapa($applicantDiv, $testDiv);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hallCd = $row["EXAMHALLCD"];
            $capaCnt = $row["CAPA_CNT"];

            if ($start == 0) {
                $start = $min;
                $end = $start + $capaCnt - 1;
            } else {
                $start =  $end + 1;
                $end = $end + $capaCnt;
            }

            $query = knjl379iQuery::getEntexamReceptMinMax($applicantDiv, $testDiv, $start, $end);

            $result2 = $db->query($query);
            $row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC);

            $data = array();
            $data["S_RECEPTNO"][TEXT]     = $row2["MIN"];
            $data["E_RECEPTNO"][TEXT]     = $row2["MAX"];
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][NUMBER]       = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
            $where .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
            $where .= "   AND TESTDIV = '{$testDiv}' ";
            $where .= "   AND EXAM_TYPE = '1' ";
            $where .= "   AND EXAMHALLCD = $hallCd ";
            $query2 = Query::updateSQL($data, "ENTEXAM_HALL_YDAT", $where);

            $db->query($query2);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return 'on';
    }
}
