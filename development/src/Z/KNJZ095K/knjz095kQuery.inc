<?php

require_once('for_php7.php');

class knjz095kQuery extends Query {
    //データ取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T2.name1, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST T1 ";
        $query .= "     ,NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.year          = '".$model->year."' AND ";
        $query .= "     T1.prefecturescd = T2.namecd2 AND ";
        $query .= "     T2.namecd1       = 'G202' ";
        $query .= " ORDER BY ";
        $query .= "     T1.prefecturescd, ";
        $query .= "     T1.grade ";

        return $query;
    }

    //都道府県コード取得
    function getName($year) {
        $query  = " SELECT namecd2,name1 ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year    = '".$year."' ";
        $query .= "    AND namecd1 = 'G202' ";

        return $query;
    }

    //学年
    function getGrade($year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }
    
    //１レコード取得
    function getRow($model, $flg) {
        $db = Query::dbCheckOut();

        $query  = " SELECT * ";
        $query .= "   FROM REDUCTION_PREF_GRADE_MST ";
        $query .= "  WHERE year           = '".$model->year."' ";

        if ($flg == 1) {
            $query .= "    AND prefecturescd  = '".$model->prefecturescd."' ";
            $query .= "    AND grade          = '".$model->grade."' ";
        } elseif ($flg == 2) {
            $query .= "    AND prefecturescd = '".$model->field["PREFECTURESCD"]."' ";
            $query .= "    AND grade         = '".$model->field["GRADE"]."' ";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year) {
        $Lyear = $year - 1; //前年度チェック用

        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN year = '".$year."'  THEN 'X' ";
        $query .= "          WHEN year = '".$Lyear."' THEN 'Y' end  AS CNT";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST ";

        $result = $db->query($query);
        $opt_year = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    function &getCopyQuery($year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO REDUCTION_PREF_GRADE_MST ";
        $query .= "                ( ";
        $query .= "                 YEAR, ";
        $query .= "                 PREFECTURESCD, ";
        $query .= "                 GRADE, ";
        $query .= "                 CURRICULUM_FLG, ";
        $query .= "                 THIS_YEAR_FLG, ";
        $query .= "                 USE_RANK, ";
        $query .= "                 ZENKI_KAISI_YEAR, ";
        $query .= "                 KOUKI_KAISI_YEAR, ";
        $query .= "                 REGISTERCD, ";
        $query .= "                 UPDATED ";
        $query .= "                ) ";
        $query .= "    (SELECT ";
        $query .= "         '".$year."', ";
        $query .= "         PREFECTURESCD, ";
        $query .= "         GRADE, ";
        $query .= "         CURRICULUM_FLG, ";
        $query .= "         THIS_YEAR_FLG, ";
        $query .= "         USE_RANK, ";
        $query .= "         ZENKI_KAISI_YEAR, ";
        $query .= "         KOUKI_KAISI_YEAR, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_PREF_GRADE_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".($year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["YEAR"][TEXT]             = $fields["YEAR"];
        $data["PREFECTURESCD"][TEXT]    = $fields["PREFECTURESCD"];
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["CURRICULUM_FLG"][TEXT]   = $fields["CURRICULUM_FLG"];
        $data["THIS_YEAR_FLG"][TEXT]    = $fields["THIS_YEAR_FLG"];
        $data["USE_RANK"][TEXT]         = $fields["USE_RANK"];
        $data["ZENKI_KAISI_YEAR"][TEXT] = $fields["ZENKI_KAISI_YEAR"];
        $data["KOUKI_KAISI_YEAR"][TEXT] = $fields["KOUKI_KAISI_YEAR"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_PREF_GRADE_MST");
        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);
        return $reduction_seq;
    }

    //UPDATE
    function &getUpdateQuery($fields,$reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();
        $data["CURRICULUM_FLG"][TEXT]   = $fields["CURRICULUM_FLG"];
        $data["THIS_YEAR_FLG"][TEXT]    = $fields["THIS_YEAR_FLG"];
        $data["USE_RANK"][TEXT]         = $fields["USE_RANK"];
        $data["ZENKI_KAISI_YEAR"][TEXT] = $fields["ZENKI_KAISI_YEAR"];
        $data["KOUKI_KAISI_YEAR"][TEXT] = $fields["KOUKI_KAISI_YEAR"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE year          = '".$fields["YEAR"]."' ";
        $where .= "   AND prefecturescd = '".$fields["PREFECTURESCD"]."' ";
        $where .= "   AND grade         = '".$fields["GRADE"]."' ";

        $query = Query::updateSQL($data, "REDUCTION_PREF_GRADE_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields,$reduction_seq) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_PREF_GRADE_MST ";
        $query .= " WHERE year           = '".$fields["YEAR"]."' ";
        $query .= "   AND prefecturescd  = '".$fields["PREFECTURESCD"]."' ";
        $query .= "   AND grade          = '".$fields["GRADE"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
