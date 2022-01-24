<?php

require_once('for_php7.php');

class knjb0045Query extends Query {

	/* 年度コンボボックス。
     * 時間割パターンヘッダデータ(SCH_PTRN_HDAT)から年度(YEAR)を取得。
	 */
    function getYearQuery()
    {
		$query  = "SELECT ";
		$query .= "    t1.year, ";
		$query .= "    t1.semester, ";
		$query .= "    t2.semestername ";
		$query .= "FROM ";
		$query .= "    (SELECT ";
		$query .= "        year, ";
		$query .= "        semester ";
		$query .= "    FROM ";
		$query .= "        sch_ptrn_hdat ";
		$query .= "    GROUP BY ";
		$query .= "        year, ";
		$query .= "        semester ";
		$query .= "    ) t1, ";
		$query .= "    semester_mst t2 ";
		$query .= "WHERE ";
		$query .= "    t1.year=t2.year AND ";
		$query .= "    t1.semester=t2.semester ";
		$query .= "ORDER BY ";
		$query .= "    t1.year, ";
		$query .= "    t1.semester ";

        return $query;
    }
	/* タイトルコンボボックス。
     * 時間割パターンヘッダデータ(SCH_PTRN_HDAT)から学期(SEMESTER)パターン(BSCSEQ)タイトル(TITLE)を取得。
	 */
    function getTitleQuery($year_semester)
    {
		$query .= "SELECT ";
		$query .= "    bscseq, ";
		$query .= "    title ";
		$query .= "FROM ";
		$query .= "    sch_ptrn_hdat ";
		$query .= "WHERE ";
		$query .= "    year || '-' || semester = '".$year_semester."' ";
		$query .= "ORDER BY ";
		$query .= "    bscseq ";

        return $query;
    }

	//時間割チェックリストヘッダデータ（データ内容表示用）
    function getCheckListHdat($db)
    {
		$query  = "SELECT status,radio,year,semester,bscseq,date_from,date_to, ";
		$query .= "       operation,W1.registercd,date(W1.updated) date_create,TIME(W1.updated)  date_time,STAFFNAME ";
		$query .= "FROM   SCH_CHECKLIST_HDAT W1 ";
		$query .= "LEFT JOIN STAFF_MST W2 ON W1.REGISTERCD=W2.STAFFCD ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }
    function getTitle($db,$year,$semester,$bscseq)
    {
		$query  = "SELECT w2.semestername, w1.bscseq, w1.title ";
		$query .= "FROM sch_ptrn_hdat w1,semester_mst w2 ";
		$query .= "WHERE ";
		$query .= "    w1.year='".$year."' AND ";
		$query .= "    w1.semester='".$semester."' AND ";
		$query .= "    w1.bscseq=".$bscseq." AND ";
		$query .= "    w1.year=w2.year AND ";
		$query .= "    w1.semester=w2.semester ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

	//学期マスタチェック
    function getSemesterMst($date_check)
    {
        $db = Query::dbCheckOut();

		$query  = "select year,semester,sdate,edate from semester_mst ";
		$query .= "where semester<>'9' and '".str_replace("/","-",$date_check)."' between sdate and edate";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return $row;
    }

    //時間割チェックリストヘッダチェック
    function ChecktoStatus($status)
    {
        $db = Query::dbCheckOut();

        $count = $db->getOne("SELECT COUNT(*) FROM SCH_CHECKLIST_HDAT WHERE STATUS='".$status."'");

        Query::dbCheckIn($db);

        return $count;
    }


    //時間割チェックリストレコード数カウント
    function getRecordCnt($db)
    {
        return $db->getOne("SELECT COUNT(*) FROM SCH_CHECKLIST_DAT WHERE MSGTYPE = 3 ");//04/11/27 'MSGTYPE = 3'の条件を追加
    }


}
?>
