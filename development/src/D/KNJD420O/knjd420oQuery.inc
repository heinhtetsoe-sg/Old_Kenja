<?php

require_once('for_php7.php');

class knjd420oQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER as VALUE, ";
        $query .= "     SEMESTERNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //取得
    function getNameMstPattern($model, $name2, $fieldName) {
        $query  = " SELECT ";
        $query .= "     {$fieldName} ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->exp_year}' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMECD2 = '{$name2}' ";

        return $query;
    }

    //帳票パターン取得
    function getGuidancePattern($model) {
        $query  = " SELECT ";
        $query .= "     L1.YEAR, ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     L1.GAKUBU_SCHOOL_KIND, ";
        $query .= "     L1.GHR_CD, ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.CONDITION, ";
        $query .= "     L1.GROUPCD, ";
        $query .= "     VALUE(L1.GUIDANCE_PATTERN, '@') AS GUIDANCE_PATTERN ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_SCHREG_GROUP_DAT T1 ";
        $query .= "     LEFT JOIN GRADE_KIND_COMP_GROUP_DAT L3 ON T1.YEAR = L3.YEAR ";
        $query .= "          AND T1.SEMESTER    = L3.SEMESTER ";
        $query .= "          AND T1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND T1.GHR_CD      = L3.GHR_CD ";
        $query .= "          AND T1.GRADE       = L3.GRADE ";
        $query .= "          AND T1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "          AND T1.CONDITION   = L3.CONDITION ";
        $query .= "          AND T1.GROUPCD     = L3.GROUPCD ";
        $query .= "     LEFT JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = L3.YEAR ";
        $query .= "          AND L1.SEMESTER    = L3.SEMESTER ";
        $query .= "          AND L1.GAKUBU_SCHOOL_KIND = L3.GAKUBU_SCHOOL_KIND ";
        $query .= "          AND L1.GHR_CD      = L3.GHR_CD ";
        $query .= "          AND L1.GRADE       = L3.GRADE ";
        $query .= "          AND L1.HR_CLASS    = L3.HR_CLASS ";
        $query .= "          AND L1.CONDITION   = L3.CONDITION ";
        $query .= "          AND L1.GROUPCD     = L3.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //印刷学期取得
    function getD078($year, $namecd1, $ctrlSemester = '')
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     CASE WHEN ABBV1 = '".CTRL_SEMESTER."' THEN 1 END AS DEF_VALUE_FLG ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND NAMECD1 = '{$namecd1}' ";
        if ($ctrlSemester) {
            $query .= "     AND ABBV1 = '".CTRL_SEMESTER."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query   = " SELECT DISTINCT ";
        $query  .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query  .= "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query  .= "   T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ";
        $query  .= "   ABBV3 AS SORT  ";
        $query  .= " FROM ";
        $query  .= "   GRADE_KIND_COMP_GROUP_DAT T1 ";
        $query  .= "   LEFT JOIN SUBCLASS_MST T2 ";
        $query  .= "      ON T1.CLASSCD = T2.CLASSCD ";
        $query  .= "     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query  .= "     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query  .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query  .= "   LEFT JOIN V_NAME_MST T3 ";
        $query  .= "      ON T1.YEAR = T3.YEAR ";
        $query  .= "     AND T3.NAMECD1 = 'A023' ";
        $query  .= "     AND T1.SCHOOL_KIND = T3.NAME1 ";
        $query  .= " WHERE ";
        $query  .= "       T1.YEAR = '".CTRL_YEAR."' ";
        $query  .= "   AND T1.SEMESTER = '9' ";
        $query  .= " ORDER BY ";
        $query  .= "   SORT, ";
        $query  .= "   T1.CLASSCD, ";
        $query  .= "   T1.CURRICULUM_CD, ";
        $query  .= "   T1.SUBCLASSCD ";

        return $query;
    }

    //講座一覧取得
    function getChairCd($model) {
        $query   = " SELECT ";
        $query  .= "  CHAIRCD || ':' || CHAIRNAME AS LABEL,  ";
        $query  .= "  CHAIRCD AS VALUE  ";
        $query  .= " FROM ";
        $query  .= "   CHAIR_DAT ";
        $query  .= " WHERE ";
        $query  .= "   YEAR = '".CTRL_YEAR."' ";
        $query  .= "   AND SEMESTER = '{$model->semester}' ";
        $query  .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        $query  .= " ORDER BY ";
        $query  .= "    VALUE ";

        return $query;
    }

    //指定講座の生徒一覧取得
    function getChairSchreg($model) {
        $query   = " WITH CHAIR_STD AS ( ";
        $query  .= "   SELECT ";
        $query  .= "     YEAR, ";
        $query  .= "     SEMESTER, ";
        $query  .= "     CHAIRCD, ";
        $query  .= "     SCHREGNO ";
        $query  .= "   FROM ";
        $query  .= "     CHAIR_STD_DAT ";
        $query  .= "   GROUP BY ";
        $query  .= "     YEAR, ";
        $query  .= "     SEMESTER, ";
        $query  .= "     CHAIRCD, ";
        $query  .= "     SCHREGNO ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "   CHSTD.SCHREGNO, ";
        $query  .= "   BASE.NAME, ";
        $query  .= "   V_SCHGRP.GAKUBU_SCHOOL_KIND, ";
        $query  .= "   V_SCHGRP.GHR_CD, ";
        $query  .= "   V_SCHGRP.GRADE, ";
        $query  .= "   V_SCHGRP.HR_CLASS, ";
        $query  .= "   V_SCHGRP.CONDITION, ";
        $query  .= "   V_SCHGRP.GROUPCD, ";
        if (is_array($model->itemNameArr)) {
            foreach ($model->itemNameArr as $itemNo => $item_remark){
                $query .= "     TOKUSHI_SEQ{$itemNo}.REMARK AS REMARK_{$itemNo}, ";
            }
        }
        $query  .= "     VALUE(YMST.GUIDANCE_PATTERN, '@') AS GUIDANCE_PATTERN ";
        $query  .= " FROM ";
        $query  .= "   CHAIR_STD CHSTD ";
        $query  .= "   LEFT JOIN SCHREG_BASE_MST BASE ";
        $query  .= "     ON BASE.SCHREGNO = CHSTD.SCHREGNO ";
        $query  .= "   LEFT JOIN V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT V_SCHGRP ";
        $query  .= "     ON V_SCHGRP.YEAR = CHSTD.YEAR ";
        $query  .= "     AND V_SCHGRP.SEMESTER = '9' ";
        $query  .= "     AND V_SCHGRP.SCHREGNO = CHSTD.SCHREGNO ";
        $query  .= " 	AND V_SCHGRP.CLASSCD || '-' || V_SCHGRP.SCHOOL_KIND || '-' || V_SCHGRP.CURRICULUM_CD || '-' || V_SCHGRP.SUBCLASSCD = '{$model->subclasscd}' ";
        $query  .= "   LEFT JOIN GRADE_KIND_COMP_GROUP_YMST YMST ";
        $query  .= "     ON YMST.YEAR = V_SCHGRP.YEAR ";
        $query  .= "     AND YMST.SEMESTER = '9' ";
        $query  .= "     AND YMST.GAKUBU_SCHOOL_KIND = V_SCHGRP.GAKUBU_SCHOOL_KIND ";
        $query  .= "     AND YMST.GHR_CD = V_SCHGRP.GHR_CD ";
        $query  .= "     AND YMST.GRADE = V_SCHGRP.GRADE ";
        $query  .= "     AND YMST.HR_CLASS = V_SCHGRP.HR_CLASS ";
        $query  .= "     AND YMST.CONDITION = V_SCHGRP.CONDITION ";
        $query  .= "     AND YMST.GROUPCD = V_SCHGRP.GROUPCD ";
        if (is_array($model->itemNameArr)) {
            foreach ($model->itemNameArr as $itemNo => $item_remark){
                $query  .= "   LEFT JOIN HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT TOKUSHI_SEQ{$itemNo} ";
                $query  .= "      ON TOKUSHI_SEQ{$itemNo}.YEAR = CHSTD.YEAR ";
                $query  .= "     AND TOKUSHI_SEQ{$itemNo}.SEMESTER = CHSTD.SEMESTER ";
                $query  .= "     AND TOKUSHI_SEQ{$itemNo}.SCHREGNO = CHSTD.SCHREGNO ";
                $query  .= " 	 AND TOKUSHI_SEQ{$itemNo}.CLASSCD || '-' || TOKUSHI_SEQ{$itemNo}.SCHOOL_KIND || '-' || TOKUSHI_SEQ{$itemNo}.CURRICULUM_CD || '-' || TOKUSHI_SEQ{$itemNo}.SUBCLASSCD = '{$model->subclasscd}' ";
                $query  .= "     AND TOKUSHI_SEQ{$itemNo}.UNITCD = '00' ";
                $query  .= "     AND TOKUSHI_SEQ{$itemNo}.SEQ = '{$itemNo}' ";
            }
        }
        $query  .= "   WHERE ";
        $query  .= "     CHSTD.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND CHSTD.SEMESTER = '{$model->semester}' ";
        $query  .= "     AND CHSTD.CHAIRCD = '{$model->chaircd}' ";
        $query  .= "    ORDER BY SCHREGNO ";

        return $query;
    }

    //HREPORT_GUIDANCE_ITEM_NAME_DAT
    function getItemName($model) {
        $query   = " WITH TMP(ITEM_REMARK, ITEM_NO) AS ( ";
        $query  .= " VALUES ('指導内容', '1') ";
        $query  .= " UNION ";
        $query  .= " VALUES ('学習目標', '2') ";
        $query  .= " UNION ";
        $query  .= " VALUES ('支援の手立て', '3') ";
        $query  .= " UNION ";
        $query  .= " VALUES ('評価', '4') )";
        $query  .= " SELECT ITEM_REMARK, ITEM_NO FROM TMP ORDER BY ITEM_NO ";

        return $query;
    }

    //１レコード取得 -- HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT
    function getHreportTokushiSchregSubclassDat($model, $schregno, $seq) {

        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '{$model->semester}' ";
        $query .= "     AND SCHREGNO    = '{$schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        if ($model->cmd == 'list_set') {
            $query .= "     AND UNITCD      = '{$model->unitcd}' ";
        } else {
            if ($model->unit_aim_div == '1') {
                $query .= "     AND UNITCD      = '{$model->unitcd}' ";
            } else {
                $query .= "     AND UNITCD      = '{$model->unitcd00}' ";
            }
        }
        if ($seq != "") {
            $query .= "     AND SEQ              = {$seq} ";
        }
        return $query;
    }

    function getStaffName($model) {
        $query  = " SELECT ";
        $query .= "     REMARK1 AS STAFFNAME ";
        $query .= " FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '$model->semester' ";
        $query .= "     AND SCHREGNO   = '".$model->schregno."' ";
        $query .= "     AND SEQ        = '001' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/

    //INSERT -- HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);
        foreach ($model->schField as $schregno => $val) {
        

            //作成者 追加
            knjd420oQuery::updateStaffname($db, $model, $schregno);

            foreach ($model->itemNameArr as $itemNo => $item_remark) {

                //存在チェック
                $query = knjd420oQuery::getHreportTokushiSchregSubclassDat($model, $schregno, $itemNo);
                $check = $db->getOne($query);
                $data = array();
                $data["REMARK"][TEXT]           = $model->schField[$schregno]["REMARK"][$itemNo];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";
                if ($check == 0) {
                    $data["YEAR"][TEXT]             = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]         = $model->semester;
                    $data["SCHREGNO"][TEXT]         = $schregno;
                    $data["CLASSCD"][TEXT]          = $classCd;
                    $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                    $data["UNITCD"][TEXT]           = '00';
                    $data["GUIDANCE_PATTERN"][TEXT] = $model->schField[$schregno]["GUIDANCE_PATTERN"];
                    $data["SEQ"][NUMBER]            = $itemNo;
                    //追加
                    $query = Query::insertSQL($data, "HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT");
                    $db->query($query);
                } else {
                    $where  = " WHERE ";
                    $where .= "         YEAR         = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER     = '{$model->semester}'";
                    $where .= "     AND SCHREGNO     = '{$schregno}' ";
                    $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
                    $where .= "     AND UNITCD       = '00' ";
                    $where .= "     AND SEQ              = {$itemNo} ";
                    //更新
                    $query = Query::updateSQL($data, "HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT", $where);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

   //DELETE -- HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT
   function &getDeleteQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->schField as $schregno => $val) {
            if ($val["CHKBOX"]) {
                $query  = " DELETE FROM ";
                $query .= "     HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR        = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER    = '{$model->semester}' ";
                $query .= "     AND SCHREGNO    = '{$schregno}' ";
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
                $query .= "     AND UNITCD      = '00' ";
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //作成者 DELETE-INSERT
    function updateStaffname($db, $model, $schregno) {
        //作成者名 削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_GUIDANCE_SCHREG_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->semester."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     SEQ         = '001' ";
        $db->query($query);

        //作成者名 追加
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = $model->semester;
        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK1"][TEXT]      = $model->staffname;
        $query = Query::insertSQL($data, "HREPORT_GUIDANCE_SCHREG_DETAIL_DAT");
        $db->query($query);
    }
}
?>
