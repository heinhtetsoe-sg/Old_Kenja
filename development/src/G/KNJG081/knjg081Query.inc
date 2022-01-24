<?php

require_once('for_php7.php');


class knjg081query extends Query {

    //年組一覧取得
    function getGradeHrclass()
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model)
    {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.PRINT_CNT ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN CERTIF_SCH_PRINT_CNT_DAT T2 ";
        $query .= "             ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "            AND T2.CERTIF_KINDCD = '008' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //削除
            $query  = " DELETE FROM CERTIF_SCH_PRINT_CNT_DAT";
            $query .= " WHERE ";
            $query .= "     SCHREGNO        = '".$schregno."' AND ";
            $query .= "     CERTIF_KINDCD   = '008' ";

            $db->query($query);

            if($model->fields["PRINT_CNT"][$i]){
                //追加
                $data = array();
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["CERTIF_KINDCD"][TEXT]    = "008";
                $data["PRINT_CNT"][NUMBER]      = $model->fields["PRINT_CNT"][$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "CERTIF_SCH_PRINT_CNT_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }
}
?>
