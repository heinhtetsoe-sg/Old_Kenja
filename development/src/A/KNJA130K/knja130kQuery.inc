<?php

require_once('for_php7.php');


class knja130kQuery extends Query
{
    //対象生徒
    function getStudent($model, $flg, $selectdata="")
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        if($flg == 'list' && $selectdata){
            $query .= "     AND T1.SCHREGNO NOT IN('".implode("','",$selectdata)."') ";
        }
        if($flg == 'select' && $selectdata){
            $query .= "     AND T1.SCHREGNO IN('".implode("','",$selectdata)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ( ";
                           //転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
                           //転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
        $query .= "     EXISTS( SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_BASE_MST S1 ";
        $query .= "             WHERE ";
        $query .= "                 S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 ((S1.GRD_DIV IN('2','3') AND ";
        $query .= "                   S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".CTRL_DATE."' THEN T2.EDATE ELSE '".CTRL_DATE."' END) OR ";
        $query .= "                  (S1.ENT_DIV IN('4','5') AND   ";
        $query .= "                   S1.ENT_DATE > ";
        $query .= "                         CASE WHEN T2.EDATE < '".CTRL_DATE."' THEN T2.EDATE ELSE '".CTRL_DATE."' END)) ";
        $query .= "     ) OR ";
                           //留学(1)・休学(2)者
        $query .= "     EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "            FROM ";
        $query .= "                 SCHREG_TRANSFER_DAT S1 ";
        $query .= "            WHERE ";
        $query .= "                 S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                 S1.TRANSFERCD IN ('1','2') AND  ";
        $query .= "                 CASE WHEN T2.EDATE < '".CTRL_DATE."' THEN T2.EDATE ELSE '".CTRL_DATE."' END ";
        $query .= "                     BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //データ取得
    function getHtrainremarkDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //データ取得
    function getOpinionsDat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.OPINION ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1, ";
        $query .= "     ATTEST_OPINIONS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.CHAGE_OPI_SEQ = T2.SEQ ";

        return $query;
    }

    //年度取得
    function getOpinionsWk($schregno)
    {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //INSERT
    function &getInsertSql($db, $model) {

        $array = explode (",", $model->selectdata);

        $db->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++){

                $query  = " DELETE FROM ATTEST_OPINIONS_UNMATCH WHERE SCHREGNO = '".$array[$i]."' ";
                $db->query($query);

                $year = $db->getCol(knja130kQuery::getOpinionsWk($array[$i]));

                foreach($year as $key => $val){

                    $remark = $db->getRow(knja130kQuery::getHtrainremarkDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
                    $hash_remark = $model->makeHash($remark);

                    $opinion = $db->getRow(knja130kQuery::getOpinionsDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
                    $hash_opinion = $opinion["OPINION"];

                    if($hash_remark != $hash_opinion){
                        $data["YEAR"][TEXT]                 = $val;
                        $data["SCHREGNO"][TEXT]             = $array[$i];
                        $data["FLG"][TEXT]                  = "1";
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "sysdate()";

                        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_UNMATCH"));
                    }
                }
            }
        }

        $db->commit(); //トランザクションをコミットする。
        return;
    }
}
?>
