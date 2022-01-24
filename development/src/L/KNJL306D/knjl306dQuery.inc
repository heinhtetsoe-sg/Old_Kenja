<?php

require_once('for_php7.php');

class knjl306dquery extends Query {
    //対象データの段階値数チェック
    function selectCountQuery($model) {
        $query  = " SELECT  ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND ASSESSCD    = '".$model->field["ASSESSCD"]."' ";

        return $query;
    }

    //評定説明取得
    function getAssessHdat($model) {
        $query  = " SELECT  ";
        $query .= "     ASSESSMEMO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_ASSESS_HDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND ASSESSCD    = '".$model->field["ASSESSCD"]."' ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {

        $query  = " WITH NAMEDATA AS ( ";
        for ($i = 1; $i <= $model->field["MAX_ASSESSLEVEL"]; $i++) {
            if ($i > 1) {
                $query .= "  UNION ";
            }
            $query .= "  SELECT ";
            $query .= "      '".$i."' AS ROW_NUM ";
            $query .= "  FROM ";
            $query .= "      SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ), ASSESSDATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         ROW_NUMBER() OVER(ORDER BY ASSESSLEVEL) AS ROW_NUM, ";
        $query .= "         AS_M.ASSESSLOW, ";
        $query .= "         AS_M.ASSESSHIGH, ";
        $query .= "         AS_M.ASSESSLEVEL, ";
        $query .= "         AS_M.ASSESSMARK ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_ASSESS_MST AS_M ";
        $query .= "         LEFT JOIN ENTEXAM_ASSESS_HDAT AS_H ON AS_M.ENTEXAMYEAR = AS_H.ENTEXAMYEAR ";
        $query .= "                                           AND AS_M.ASSESSCD    = AS_H.ASSESSCD ";
        $query .= "     WHERE ";
        $query .= "             AS_M.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "         AND AS_M.ASSESSCD    = '".$model->field["ASSESSCD"]."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT";
        $query .= "     INT(NAME.ROW_NUM) AS ROW_NUM, ";
        $query .= "     MAIN.ASSESSLEVEL, ";
        $query .= "     MAIN.ASSESSLOW, ";
        $query .= "     MAIN.ASSESSHIGH, ";
        $query .= "     MAIN.ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     NAMEDATA NAME ";
        $query .= "     LEFT JOIN ASSESSDATA MAIN ON MAIN.ROW_NUM = INT(NAME.ROW_NUM) ";
        $query .= " ORDER BY ";
        $query .= "     INT(NAME.ROW_NUM) ";

        return $query;
    }

/***** 次年度作成処理 *****/
    //ある年度にデータが存在するのかチェック
    function getCheckEntexamAssessMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //COPY(ENTEXAM_ASSESS_MST)
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_ASSESS_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     ASSESSCD, ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     ASSESSCD, ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSMARK, ";
        $query .= "     ASSESSLOW, ";
        $query .= "     ASSESSHIGH, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }

    //COPY(ENTEXAM_ASSESS_MST)
    function getCopyQuery2($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_ASSESS_HDAT ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     ASSESSCD, ";
        $query .= "     ASSESSMEMO, ";
        $query .= "     ASSESSLEVELCNT, ";
        $query .= "     MODIFY_FLG, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     ASSESSCD, ";
        $query .= "     ASSESSMEMO, ";
        $query .= "     ASSESSLEVELCNT, ";
        $query .= "     MODIFY_FLG, ";
        $query .= "     '". STAFFCD ."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_ASSESS_HDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }

/***** 更新処理 *****/
    //データ削除
    function delete($model, $db) {
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_ASSESS_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND ASSESSCD    = '".$model->field["ASSESSCD"]."' ";

        $db->query($query);

        return;
    }

    // データ更新処理
    function update($model, $db) {

        for ($i = 0; $i < get_count($model->data["ROW_NUM"]); $i++) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["ASSESSCD"][TEXT]         = $model->field["ASSESSCD"];
            $data["ASSESSLEVEL"][NUMBER]    = $i+1;
            $data["ASSESSLOW"][NUMBER]      = $model->fields["ASSESSLOW"][$i];
            if ($i+1 != get_count($model->data["ROW_NUM"])) {
                $data["ASSESSHIGH"][NUMBER]     = $model->fields["ASSESSHIGH"][$i];
            } else {
                $data["ASSESSHIGH"][NUMBER]     = $model->field["ASSESSHIGH_MAX"];
            }
            $data["ASSESSMARK"][TEXT]       = $model->fields["ASSESSMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "ENTEXAM_ASSESS_MST");
            $db->query($query);
        }

        return;
    }
}
?>
