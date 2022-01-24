<?php

require_once('for_php7.php');


class knja130gQuery extends Query {

    //学年取得
    function getGrade()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND in ('J', 'H') ";

        return $query;
    }

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
        $query .= "                     SCHOOL_KIND in ('J', 'H') ) ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "      TR_CD2 = '".STAFFCD."' OR ";
            $query .= "      TR_CD3 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3 = '".STAFFCD."') ";
        }

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

        $array = $model->select_data["selectdata"];

        $db->autoCommit(false);

        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++){

                $query  = " DELETE FROM ATTEST_OPINIONS_UNMATCH WHERE SCHREGNO = '".$array[$i]."' ";
                $db->query($query);

                $year = $db->getCol(knja130gQuery::getOpinionsWk($array[$i]));

                foreach($year as $key => $val){

                    $remark = $db->getRow(knja130gQuery::getHtrainremarkDat($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    //学習記録データ
                    $result = $db->query(knja130gQuery::getStudyRec($val, $array[$i]));
                    $study = "";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        }
                    } else {
                        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                                      $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                        }
                    }

                    //出欠記録データ
                    $attend = $db->getRow(knja130gQuery::getAttendRec($val, $array[$i]), DB_FETCHMODE_ASSOC);

                    $hash_remark = $model->makeHash($remark, $study, $attend);

                    $opinion = $db->getRow(knja130gQuery::getOpinionsDat($val, $array[$i]), DB_FETCHMODE_ASSOC);
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
