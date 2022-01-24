<?php

require_once('for_php7.php');

class knjz439Query extends Query {

    //教育課程
    function getCurriculumCD() {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z018' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト表示
    function getList($model) {
        $query  = " WITH T_CLASS AS ( ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     SELECT DISTINCT ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         CLASS_DETAIL_DAT ";
            $query .= "     WHERE ";
            $query .= "         CLASS_SEQ = '005' ";
        } else {
            $query .= "     SELECT DISTINCT ";
            $query .= "         NAME1 AS CLASSCD ";
            $query .= "     FROM ";
            $query .= "         NAME_MST ";
            $query .= "     WHERE ";
            $query .= "         NAMECD1 = 'D031' ";
        }
        $query .= "     ) ";
        $query .= " , T_STANDARD AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         CREDITS ";
        $query .= "     FROM ";
        $query .= "         STANDARD_CREDIT_MST ";
        $query .= "     WHERE ";
        $query .= "         CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        $query .= "     ) ";

        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     L2.CLASSCD || '-' || L2.SCHOOL_KIND AS CLASSCD, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
	        $query .= "     L2.CLASSCD , ";
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     L2.CLASSNAME, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS CHECK, ";
        $query .= "     L1.CREDITS ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= "     INNER JOIN T_CLASS T2 ON T2.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "                          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN T_STANDARD L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                      AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                      AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN CLASS_MST L2 ON L2.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= " WHERE ";
            $query .= "     T1.CURRICULUM_CD = '{$model->field["CURRICULUM_CD"]}' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SUBCLASSCD"]); $i++) {
            //標準科目チェックボックス
            if ($model->fields["UPDATE_DATA"][$i] != '1') continue;

            $curriculumCd = $model->field["CURRICULUM_CD"];
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $school_kind = substr($model->data["SUBCLASSCD"][$i], 3, 1);
                $subclassCd  = substr($model->data["SUBCLASSCD"][$i], 7, 6);
            } else {
                $subclassCd = $model->data["SUBCLASSCD"][$i];
            }
            $classCd = substr($subclassCd, 0, 2);
            $credits = $model->fields["CREDITS"][$i];
            //条件
            $where  = " WHERE CLASSCD       = '{$classCd}'";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $where .= "   AND SCHOOL_KIND = '{$school_kind}'";
            }
            $where .= "   AND CURRICULUM_CD = '{$curriculumCd}'";
            $where .= "   AND SUBCLASSCD    = '{$subclassCd}'";
            //削除
            $query  = " DELETE FROM STANDARD_CREDIT_MST " .$where;
            $db->query($query);
            //項目
            $data = array();
            $data["CLASSCD"][TEXT]          = $classCd;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["SCHOOL_KIND"][TEXT]    = $school_kind;
            }
            $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
            $data["SUBCLASSCD"][TEXT]       = $subclassCd;
            $data["CREDITS"][NUMBER]        = $credits;
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //追加
            if (strlen($credits)) {
                $query = Query::insertSQL($data, "STANDARD_CREDIT_MST");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }
}
?>