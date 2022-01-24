<?php

require_once('for_php7.php');

class knjd030Query extends Query {

    function selectQuery($model)
    {
        $query = "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.CHAIRCD, ";
        $query .= "    T1.HR_NAMEABBV, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.NAME_SHOW, ";
        $query .= "    T1.SEX, ";
        $query .= "    T2.SCORE, ";
        $query .= "    T2.ATTEND_FLG ";
        $query .= "FROM ";
        $query .= "    (SELECT ";
        $query .= "        ST1.SCHREGNO, ";
        $query .= "        MAX(ST1.CHAIRCD) AS CHAIRCD, ";
        $query .= "        ST4.HR_NAMEABBV, ";
        $query .= "        ST2.ATTENDNO, ";
        $query .= "        ST3.NAME_SHOW, ";
        $query .= "        MEISYOU_GET(ST3.SEX,'Z002',1) AS SEX ";
        $query .= "    FROM ";
        $query .= "        CHAIR_STD_DAT    ST1, ";
        $query .= "        SCHREG_REGD_DAT  ST2, ";
        $query .= "        SCHREG_BASE_MST  ST3, ";
        $query .= "        SCHREG_REGD_HDAT ST4, ";
        $query .= "        TESTSCORE_HDAT   ST5  ";
        $query .= "    WHERE ";
        $query .= "        ST1.YEAR     = '" .$model->field["YEAR"] ."' AND ";
        $query .= "        ST1.YEAR     = ST2.YEAR AND ";
        $query .= "        ST1.YEAR     = ST4.YEAR AND ";
        $query .= "        ST1.CHAIRCD  IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "        ST1.SCHREGNO = ST2.SCHREGNO AND ";
        $query .= "        ST1.SCHREGNO = ST3.SCHREGNO AND ";
        $query .= "        ST1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "        ST1.SEMESTER = ST2.SEMESTER AND ";
        $query .= "        ST1.SEMESTER = ST4.SEMESTER AND ";
        $query .= "        ST2.GRADE    = ST4.GRADE AND ";
        $query .= "        ST2.HR_CLASS = ST4.HR_CLASS AND ";
        $query .= "        ST1.YEAR     = ST5.YEAR AND ";
        $query .= "        ST1.SEMESTER = ST5.SEMESTER AND ";
        $query .= "        ST1.CHAIRCD  = ST5.CHAIRCD AND ";
        $query .= "        NOT (ST3.GRD_DIV IN ('1','2','3') AND ST3.GRD_DATE <= ST5.OPERATION_DATE)";   //1:´��/2:���/3:ž��
        $query .= "    GROUP BY ";  
        $query .= "        ST1.SCHREGNO, ";
        $query .= "        ST4.HR_NAMEABBV, ";
        $query .= "        ST2.ATTENDNO, ";
        $query .= "        ST3.NAME_SHOW, ";
        $query .= "        ST3.SEX ";
        $query .= "    ) T1  LEFT OUTER JOIN (SELECT DISTINCT ";
        $query .= "          ST1.SCHREGNO, ";
        $query .= "          COALESCE(ST1.SCORE,0) AS SCORE, ";
        $query .= "          ST1.UPDATED, ";
//        $query .= "          ST1.CHAIRCD, ";
        $query .= "          ST1.ATTEND_FLG ";
        $query .= "      FROM ";
        $query .= "          TESTSCORE_DAT ST1 ";
        $query .= "      WHERE ";
        $query .= "          ST1.YEAR        = '" .$model->field["YEAR"] ."' AND ";
        $query .= "          ST1.SEMESTER    = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "          ST1.SUBCLASSCD  = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "          ST1.TESTKINDCD  = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "          ST1.TESTITEMCD  = '" .$model->field["TESTITEMCD"] ."' ";
//        $query .= "      ) T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "      ) T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    NOT EXISTS (SELECT ";
        $query .= "                    w1.SCHREGNO ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_TRANSFER_DAT w1, ";
        $query .= "                    TESTSCORE_HDAT w2 ";
        $query .= "                WHERE ";
        $query .= "                    w1.TRANSFERCD IN ('1','2','3') AND ";      //1:α��/2:�ٳ�/3:����
        $query .= "                    w1.SCHREGNO        = T1.SCHREGNO AND ";
        $query .= "                    w2.CHAIRCD         = T1.CHAIRCD AND ";
//        $query .= "                    w2.OPERATION_DATE <= w1.TRANSFER_SDATE AND ";
        $query .= "                    w2.OPERATION_DATE  BETWEEN w1.TRANSFER_SDATE ";
        $query .= "                                           AND w1.TRANSFER_EDATE AND ";
        $query .= "                    w2.YEAR            = '" .$model->field["YEAR"] ."' AND ";
        $query .= "                    w2.SEMESTER        = '" .CTRL_SEMESTER."' AND ";
        $query .= "                    w2.TESTKINDCD      = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "                    w2.TESTITEMCD      = '" .$model->field["TESTITEMCD"] ."' ";
        $query .= "                ) ";
        if (is_array($model->sort)){
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order){
                switch($field){
                    case "ATTENDNO":    //�ֹ��
                        $query .= "T1.HR_NAMEABBV,T1.ATTENDNO ";
                        $query .= ($order > 0)? "ASC " : "DESC ";
                        break 2;
                    case "SCORE":       //�ʼ���
                        if ($order > 0){
                            $query .= "ATTEND_FLG DESC,SCORE DESC,T1.HR_NAMEABBV,T1.ATTENDNO ";
                        }else{
                            $query .= "ATTEND_FLG DESC,SCORE,T1.HR_NAMEABBV,T1.ATTENDNO ";
                        }
                        break 2;
                }
            }
        }else{
            $query .= "ORDER BY ";
            $query .= "    T1.HR_NAMEABBV, ";
            $query .= "    T1.ATTENDNO ASC ";
        }

        return $query;
    }
    //ʿ�����ʤɤ����
    function getAvgScoreQuery($model)
    {
        $query = " SELECT ";
        $query .= "   COUNT(*) AS SUBJECT, ";
        $query .= "   SUM(INTEGER(T2.ATTEND_FLG)) AS EXAMINEE, ";
        $query .= "   CASE FLOAT(SUM(INTEGER(T2.ATTEND_FLG))) ";
        $query .= "       WHEN 0 THEN 0 ";
        $query .= "       ELSE FLOAT(SUM(T2.SCORE)) / FLOAT(SUM(INTEGER(T2.ATTEND_FLG))) END AS AVG_SCORE, ";
        $query .= "   T1.PERFECT ";
        $query .= " FROM ";
        $query .= "   TESTSCORE_HDAT T1 LEFT OUTER JOIN ";
        $query .= "   TESTSCORE_DAT T2 ";
        $query .= " ON ";
        $query .= "   T1.YEAR       = T2.YEAR AND ";
        $query .= "   T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "   T2.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "   T1.TESTKINDCD = T2.TESTKINDCD AND ";
        $query .= "   T1.TESTITEMCD = T2.TESTITEMCD AND ";
        $query .= "   T1.CHAIRCD    = T2.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   T1.SEMESTER   = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "   T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "   T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "   T1.CHAIRCD    IN ('" .implode($model->field["CHAIRCD"],"','") ."') ";
        $query .= " GROUP BY ";
        $query .= "   T1.PERFECT ";

        return $query;
    }

/*
        @(s)

     ��ǽ      ���������ϲ����Խ��оݥǡ����ν���߽���

     �����    ��True:������False:����

     ����      ��

     ��ǽ����  ���������ϲ��̤���Խ���������Τߤ�����ǥ����Ф��Ϥ��������롣

     ����      ���ʤ�

     ����      ��Ver1.0
     ����      ��
*/
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //���������̤Υ쥳���ɤ����
        $query = " INSERT INTO TESTSCORE_DAT( ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " SUBCLASSCD, ";
        $query .= " TESTKINDCD, ";
        $query .= " TESTITEMCD, ";
        $query .= " SCHREGNO ";
        $query .= " ) ";
        $query .= " (SELECT ";
        $query .= "   ST1.YEAR, ";
        $query .= "   ST1.SEMESTER, ";
        $query .= "   '" .$model->field["SUBCLASSCD"] ."' AS SUBCLASSCD, ";
        $query .= "   '" .$model->field["TESTKINDCD"] ."' AS TESTKINDCD, ";
        $query .= "   '" .$model->field["TESTITEMCD"] ."' AS TESTITEMCD, ";
        $query .= "   ST1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "   CHAIR_STD_DAT ST1 ";
        $query .= " WHERE ";
        $query .= "   ST1.YEAR     = '" .$model->field["YEAR"] ."' AND ";
        $query .= "   ST1.SEMESTER = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "   ST1.CHAIRCD  IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "   ST1.SCHREGNO IN ('" .implode($model->schregno,"','") ."') AND ";
        $query .= "   NOT EXISTS(SELECT 'X'";
        $query .= "                FROM TESTSCORE_DAT W ";
        $query .= "               WHERE ";
        $query .= "               W.YEAR       = ST1.YEAR AND ";
        $query .= "               W.SEMESTER   = ST1.SEMESTER AND ";
        $query .= "               W.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "               W.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "               W.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "               W.SCHREGNO = ST1.SCHREGNO ";
        $query .= "             ) ";
        $query .= " ) ";
        
        $db->query($query);

        //���ӹ����������ǡ�������Ͽ
        for ($i = 0,$sum=$exam=0; $i < get_count($model->field["SCORE"]); $i++){
            $score = $model->field["SCORE"][$i];
            if (is_numeric($score)){
                $attend_flg = 1;        //���������äƤ���м�����ʬ��"1"�Ȥ��롣
                //��פ����
                $sum += $score;
                //�оݿͿ�
                $exam++;
            }else{
                $attend_flg = 0;
                $score = 0;
            }
            $data = array();
            $data["SCORE"][NUMBER]      = $score;
            $data["ATTEND_FLG"][TEXT]   = $attend_flg;
            $data["CHAIRCD"][TEXT]      = $model->chaircd[$i];             //���֥��饹������
            $data["REGISTERCD"][TEXT]   = STAFFCD;                          //����������
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where  = " WHERE YEAR      = '" .$model->field["YEAR"] ."' ";
            $where .= " AND SEMESTER    = '" .$model->field["SEMESTER"] ."' ";
            $where .= " AND SUBCLASSCD  = '" .$model->field["SUBCLASSCD"] ."' ";
            $where .= " AND TESTKINDCD  = '" .$model->field["TESTKINDCD"] ."' ";
            $where .= " AND TESTITEMCD  = '" .$model->field["TESTITEMCD"] ."' ";
            $where .= " AND SCHREGNO    = '" .$model->schregno[$i] ."'";

            $query = Query::updateSQL($data, "TESTSCORE_DAT", $where);
            $db->query($query);
        }


        //���ߥǡ������
        $query = "DELETE FROM ";
        $query .= "    TESTSCORE_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "    T1.SEMESTER   = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "    T1.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "    T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "    T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "    T1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "    NOT EXISTS (SELECT 'X'";
        $query .= "                 FROM  CHAIR_STD_DAT w1 ";
        $query .= "                WHERE ";
        $query .= "                    w1.YEAR     = T1.YEAR AND ";
        $query .= "                    w1.CHAIRCD  = T1.CHAIRCD AND ";
        $query .= "                    w1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                ) ";

        $db->query($query);

        //��ư�������̤Υǡ�������(α��/�ٳ�/����)
        $query = "DELETE FROM ";
        $query .= "    TESTSCORE_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "    T1.SEMESTER   = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "    T1.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "    T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "    T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "    T1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "    EXISTS (SELECT 'X'";
        $query .= "            FROM "; 
        $query .= "                SCHREG_TRANSFER_DAT w1, ";
        $query .= "                TESTSCORE_HDAT w2 ";
        $query .= "            WHERE ";
        $query .= "                w1.TRANSFERCD     IN ('1','2','3') AND ";         //1:α��/2:�ٳ�/3:����
        $query .= "                w1.SCHREGNO       = T1.SCHREGNO AND ";
        $query .= "                w2.OPERATION_DATE BETWEEN w1.TRANSFER_SDATE ";
        $query .= "                                      AND w1.TRANSFER_EDATE AND ";
        $query .= "                w2.YEAR           = T1.YEAR AND ";
        $query .= "                w2.SEMESTER       = T1.SEMESTER AND ";
        $query .= "                w2.TESTKINDCD     = T1.TESTKINDCD AND ";
        $query .= "                w2.TESTITEMCD     = T1.TESTITEMCD AND ";
        $query .= "                w2.CHAIRCD        = T1.CHAIRCD ";
        $query .= "            ) ";

        $db->query($query);

        //��ư�������̤Υǡ�������(´��/���/ž��)
        $query = "DELETE FROM ";
        $query .= "    TESTSCORE_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR       = '" .$model->field["YEAR"] ."' AND ";
        $query .= "    T1.SEMESTER   = '" .$model->field["SEMESTER"] ."' AND ";
        $query .= "    T1.SUBCLASSCD = '" .$model->field["SUBCLASSCD"] ."' AND ";
        $query .= "    T1.TESTKINDCD = '" .$model->field["TESTKINDCD"] ."' AND ";
        $query .= "    T1.TESTITEMCD = '" .$model->field["TESTITEMCD"] ."' AND ";
        $query .= "    T1.CHAIRCD IN ('" .implode($model->field["CHAIRCD"],"','") ."') AND ";
        $query .= "    EXISTS (SELECT 'X'";
        $query .= "            FROM ";
        $query .= "                CHAIR_STD_DAT   w1, ";
        $query .= "                TESTSCORE_HDAT  w2, ";
        $query .= "                SCHREG_BASE_MST w3  ";
        $query .= "            WHERE ";
        $query .= "                w1.YEAR       = T1.YEAR AND ";
        $query .= "                w1.SEMESTER   = T1.SEMESTER AND ";
        $query .= "                w1.CHAIRCD    = T1.CHAIRCD ";
        $query .= "                w1.SCHREGNO   = T1.SCHREGNO AND ";
        $query .= "                w2.YEAR       = T1.YEAR AND ";
        $query .= "                w2.SEMESTER   = T1.SEMESTER AND ";
        $query .= "                w2.TESTKINDCD = T1.TESTKINDCD AND ";
        $query .= "                w2.TESTITEMCD = T1.TESTITEMCD AND ";
        $query .= "                w2.CHAIRCD    = T1.CHAIRCD AND ";
        $query .= "                w1.SCHREGNO   = w3.SCHREGNO AND ";
        $query .= "                (w3.GRD_DIV   IN ('1','2','3') AND w3.GRD_DATE <= w2.OPERATION_DATE) "; //1:´��/2:���/3:ž��
        $query .= "            ) ";

        $db->query($query);


        $db->commit(); // ���ߥåȤ��롣

        Query::dbCheckIn($db);
        return true;
    }
}
?>
