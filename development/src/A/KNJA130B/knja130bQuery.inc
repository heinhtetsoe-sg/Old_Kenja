<?php

require_once('for_php7.php');


class knja130bQuery extends Query
{
    //クラス取得
    function getHrClass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE IN (  SELECT ";
        $query .= "                     GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "                     SCHOOL_KIND = 'H' ) ";
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "	     TR_CD2 = '".STAFFCD."' OR ";
            $query .= "	     TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "	     SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "	     SUBTR_CD3 = '".STAFFCD."') ";
		}
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //名称マスタ
    function getNameMst($namecd1, $namecd2)
    {
        $query  = "SELECT * ";
        $query .= "FROM V_NAME_MST ";
        $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
        $query .= "AND NAMECD1='".$namecd1 ."'";
        $query .= "AND NAMECD2='".$namecd2 ."'";
        return $query;
    }

    //未履修科目出力　履修のみ科目出力
    function getRisyuMirsyu($model)
    {
        //参照・更新可
        $query  = "";
        $query .= " SELECT NAMESPARE1, NAMESPARE2, NAMESPARE3 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'A027' ";
        $query .= " AND NAMECD2 = '107' ";
        return $query;
    }

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

    //学習記録データ取得
    function getStudyRec($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    function getAttendRec($year, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
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

                $year = $db->getCol(knja130bQuery::getOpinionsWk($array[$i]));

                foreach($year as $key => $val){

                    $remark = $db->getRow(knja130bQuery::getHtrainremarkDat($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    //学習記録データ
                    $result = $db->query(knja130bQuery::getStudyRec($val, $array[$i]));
                    $study = "";
                    while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ("1" == $model->Properties["useCurriculumcd"]) {
                            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        } else {
                            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        }
                    }

                    //出欠記録データ
                    $attend = $db->getRow(knja130bQuery::getAttendRec($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    $hash_remark = $model->makeHash($remark, $study, $attend);

                    $opinion = $db->getRow(knja130bQuery::getOpinionsDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
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
