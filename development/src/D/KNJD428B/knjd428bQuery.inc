<?php

require_once('for_php7.php');
class knjd428bQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER as VALUE, ";
        $query .= "     SEMESTERNAME as LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->exp_year}' ";
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

    //グループ、履修情報取得
    function getViewGradeKindSchreg($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == 'set') {
            $query .= " DISTINCT ";
            $query .= "     T1.GAKUBU_SCHOOL_KIND, ";
            $query .= "     T1.GHR_CD, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.CONDITION, ";
            $query .= "     T1.GROUPCD ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER = '9' ";
        $query .= " AND T1.SCHREGNO = '".$model->schregno."' ";
        if ($flg != 'set') {
            $query .= " ORDER BY ";
            $query .= "     T1.GROUPCD,  ";
            $query .= "     T1.CLASSCD,  ";
            $query .= "     T1.SCHOOL_KIND,  ";
            $query .= "     T1.CURRICULUM_CD,  ";
            $query .= "     T1.SUBCLASSCD,  ";
            $query .= "     T1.UNITCD  ";
        }

        return $query;
    }

    //グループコード取得
    function getGroupcd($model, $row) {
        $query  = " SELECT ";
        $query .= "     GROUPNAME ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND SEMESTER    = '9' ";
        $query .= " AND GAKUBU_SCHOOL_KIND = '".$row["GAKUBU_SCHOOL_KIND"]."' ";
        $query .= " AND GHR_CD      = '".$row["GHR_CD"]."' ";
        $query .= " AND GRADE       = '".$row["GRADE"]."' ";
        $query .= " AND HR_CLASS    = '".$row["HR_CLASS"]."' ";
        $query .= " AND CONDITION   = '".$row["CONDITION"]."' ";
        $query .= " AND GROUPCD     = '".$row["GROUPCD"]."' ";

        return $query;
    }

    //状態区分名称取得
    function getConditionName($model, $condition) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= " AND NAMECD1     = 'A033' ";
        $query .= " AND NAMECD2     = '".$condition."' ";

        return $query;
    }

   //履歴情報の表示
    function getList($model, $colName, $divArray) {
        $inDiv    = implode("','", $divArray);

        $query   = "  SELECT ";
        $query  .= "    MAIN.YEAR, ";
        $query  .= "    MAIN.SEMESTER, ";
        $query  .= "    SEMS.SEMESTERNAME, ";
        $query  .= "    MAIN.SCHREGNO, ";
        $query  .= "    MAIN.DIV, ";
        $query  .= "    MAIN.{$colName} AS REMARK ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_SCHREG_REMARK_DAT MAIN ";
        $query  .= "    LEFT JOIN SEMESTER_MST SEMS ";
        $query  .= "      ON SEMS.YEAR = MAIN.YEAR ";
        $query  .= "      AND SEMS.SEMESTER = MAIN.SEMESTER ";
        $query  .= "  WHERE ";
        $query  .= "    MAIN.YEAR = '".$model->exp_year."' ";
        if ($model->field["INPUT_DIV"] == 1) {
            $query  .= "   AND MAIN.SEMESTER = '9' ";
        } else {
            $query  .= "   AND MAIN.SEMESTER <> '9' ";
        }
        $query  .= "    AND MAIN.SCHREGNO = '".$model->schregno."' ";
        $query  .= "    AND DIV IN ('{$inDiv}') ";
        $query .= " ORDER BY ";
        if ($model->sort == "SUBCLASSCD") {
            $query .= "     MAIN.SEMESTER DESC, ";
        } else {
            $query .= "     MAIN.SEMESTER, ";
        }
        $query .= " MAIN.DIV ";

        return $query;
    }
   //履歴情報の表示
    function getList2($model, $colName, $seqArray) {
        $seqDiv    = implode("','", $seqArray);

        $query  = " SELECT ";
        $query .= "     MAIN.YEAR, ";
        $query .= "     MAIN.SEMESTER, ";
        $query .= "     SEMS.SEMESTERNAME, ";
        $query .= "     MAIN.SCHREGNO, ";
        $query .= "     MAIN.CLASSCD, ";
        $query .= "     MAIN.SCHOOL_KIND, ";
        $query .= "     MAIN.CURRICULUM_CD, ";
        $query .= "     MAIN.SUBCLASSCD, ";
        $query .= "     MAIN.CLASSCD || '-' || MAIN.SCHOOL_KIND || '-' || MAIN.CURRICULUM_CD || '-' || MAIN.SUBCLASSCD || ':' || SUBM.SUBCLASSNAME AS SET_SUBCLASSNAME, ";
        $query .= "     MAIN.CLASSCD || '-' || MAIN.SCHOOL_KIND || '-' || MAIN.CURRICULUM_CD || '-' || MAIN.SUBCLASSCD AS SET_SUBCLASSCD, ";
        $query .= "     MAIN.SEQ, ";
        $query .= "     MAIN.{$colName} AS REMARK ";
        $query .= " FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT MAIN ";
        $query .= "    INNER JOIN GRADE_KIND_SCHREG_GROUP_DAT SCH_GRP ";
        $query .= "      ON SCH_GRP.YEAR                   = MAIN.YEAR ";
        $query .= "      AND SCH_GRP.SEMESTER              = '9' ";
        $query .= "      AND SCH_GRP.GAKUBU_SCHOOL_KIND    = '{$model->groupInfo["GAKUBU_SCHOOL_KIND"]}' ";
        $query .= "      AND SCH_GRP.GHR_CD                = '{$model->groupInfo["GHR_CD"]}' ";
        $query .= "      AND SCH_GRP.GRADE                 = '{$model->groupInfo["GRADE"]}' ";
        $query .= "      AND SCH_GRP.HR_CLASS              = '{$model->groupInfo["HR_CLASS"]}' ";
        $query .= "      AND SCH_GRP.CONDITION             = '{$model->groupInfo["CONDITION"]}' ";
        $query .= "      AND SCH_GRP.GROUPCD               = '{$model->groupInfo["GROUPCD"]}' ";
        $query .= "      AND SCH_GRP.SCHREGNO              = MAIN.SCHREGNO ";
        $query .= "    INNER JOIN GRADE_KIND_COMP_GROUP_DAT KIND_GRP ";
        $query .= "      ON KIND_GRP.YEAR                  = SCH_GRP.YEAR ";
        $query .= "      AND KIND_GRP.SEMESTER             = SCH_GRP.SEMESTER ";
        $query .= "      AND KIND_GRP.GAKUBU_SCHOOL_KIND   = SCH_GRP.GAKUBU_SCHOOL_KIND ";
        $query .= "      AND KIND_GRP.GHR_CD               = SCH_GRP.GHR_CD ";
        $query .= "      AND KIND_GRP.GRADE                = SCH_GRP.GRADE ";
        $query .= "      AND KIND_GRP.HR_CLASS             = SCH_GRP.HR_CLASS ";
        $query .= "      AND KIND_GRP.CONDITION            = SCH_GRP.CONDITION ";
        $query .= "      AND KIND_GRP.GROUPCD              = SCH_GRP.GROUPCD ";
        $query .= "      AND KIND_GRP.CLASSCD              = MAIN.CLASSCD ";
        $query .= "      AND KIND_GRP.SCHOOL_KIND          = MAIN.SCHOOL_KIND ";
        $query .= "      AND KIND_GRP.CURRICULUM_CD        = MAIN.CURRICULUM_CD ";
        $query .= "      AND KIND_GRP.SUBCLASSCD           = MAIN.SUBCLASSCD ";
        $query .= "     LEFT JOIN SEMESTER_MST SEMS ON SEMS.YEAR     = MAIN.YEAR ";
        $query .= "                                AND SEMS.SEMESTER = MAIN.SEMESTER ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD       = MAIN.CLASSCD ";
        $query .= "                                AND SUBM.SCHOOL_KIND   = MAIN.SCHOOL_KIND ";
        $query .= "                                AND SUBM.CURRICULUM_CD = MAIN.CURRICULUM_CD ";
        $query .= "                                AND SUBM.SUBCLASSCD    = MAIN.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         MAIN.YEAR     = '{$model->exp_year}' ";
        $query .= "     AND MAIN.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND MAIN.UNITCD   = '00'";
        $query .= "     AND MAIN.SEQ IN ('{$seqDiv}') ";
        $query .= " ORDER BY ";
        if ($model->sort == "SUBCLASSCD") {
            $query .= "     MAIN.CLASSCD, ";
            $query .= "     MAIN.CURRICULUM_CD, ";
            $query .= "     MAIN.SUBCLASSCD, ";
            $query .= "     MAIN.SEMESTER, ";
            $query .= "     MAIN.SEQ ";
        } else {
            $query .= "     MAIN.SEMESTER, ";
            $query .= "     MAIN.CLASSCD, ";
            $query .= "     MAIN.CURRICULUM_CD, ";
            $query .= "     MAIN.SUBCLASSCD, ";
            $query .= "     MAIN.SEQ ";
        }

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD AS VALUE, ";
        $query .= "     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L3.CLASSCD = L1.CLASSCD ";
        $query .= "          AND L3.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= "          AND L3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "          AND L3.SUBCLASSCD  = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".$model->exp_year."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //HREPORT_GUIDANCE_ITEM_NAME_DAT
    function getItemName($model, $kindNo, $kindSeqArray) {
        $InKindSeq = implode("','", $kindSeqArray);

        //指定したキー分のレコードを取得するための固定データ
        $sep = "";
        $query   = "  WITH TMP(KIND_NO, KIND_SEQ) AS ( ";
        foreach($kindSeqArray as $seq) {
            $query  .= $sep."    VALUES ('{$kindNo}', '{$seq}') ";
            $sep = " UNION ";
        }
        $query  .= "  ) ";

        $query  .= "  SELECT ";
        $query  .= "    T1.KIND_NO, ";
        $query  .= "    T1.KIND_SEQ, ";
        $query  .= "    T2.KIND_REMARK ";
        $query  .= "  FROM ";
        $query  .= "    TMP T1 ";
        $query  .= "  LEFT JOIN ";
        $query  .= "    HREPORT_GUIDANCE_KIND_NAME_DAT T2 ";
        $query  .= "  ON ";
        $query  .= "    T2.KIND_NO     = T1.KIND_NO AND ";
        $query  .= "    T2.KIND_SEQ    = T1.KIND_SEQ AND ";
        $query  .= "    T2.YEAR        = '".CTRL_YEAR."' AND ";
        $query  .= "    T2.KIND_NO     = '{$kindNo}' AND ";
        $query  .= "    T2.KIND_SEQ IN ('{$InKindSeq}') ";
        $query  .= "  ORDER BY ";
        $query  .= "    T1.KIND_NO, ";
        $query  .= "    T1.KIND_SEQ ";

        return $query;
    }

    //１レコード取得 -- HREPORT_SCHREG_REMARK_DAT
    function getHreportSchregDat($model, $colName ,$divParam) {
        $semester = ($model->field["INPUT_DIV"] != 1) ? $model->selectSemes: '9';
        if (is_array($divParam)) {
            $inDiv    = implode("','", $divParam);
        }

        $query   = "  SELECT ";
        $query  .= "    DIV, {$colName} ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_SCHREG_REMARK_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR            = '{$model->exp_year}' ";
        $query  .= "    AND SEMESTER    = '{$semester}' ";
        $query  .= "    AND SCHREGNO    = '{$model->schregno}' ";
        if (is_array($divParam)) {
            $query  .= "    AND DIV IN ('{$inDiv}') ";
        } else {
            $query  .= "    AND DIV = '{$divParam}' ";
        }

        return $query;
    }

    //１レコード取得 -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function getHreportSchregSubclassDat($model, $seqParam) {
        $semester = ($model->field["INPUT_DIV"] != 1) ? $model->selectSemes: '9';
        if (is_array($seqParam)) {
            $inSeq    = implode("','", $seqParam);
        }

        $query   = "  SELECT ";
        $query  .= "    SEQ, REMARK ";
        $query  .= "  FROM ";
        $query  .= "    HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR            = '{$model->exp_year}' ";
        $query  .= "    AND SEMESTER    = '{$semester}' ";
        $query  .= "    AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        $query  .= "    AND UNITCD      = '00' ";
        if (is_array($seqParam)) {
            $query  .= "    AND SEQ IN ('{$inSeq}') ";
        } else {
            $query  .= "    AND SEQ = '{$seqParam}' ";
        }

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //COPY -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function &getCopySubclassQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 削除してからコピーする
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->selectSemes}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND SEQ         IN ('".implode("','", $model->keyData)."') ";
        // 指定科目でコピー
        if ($model->cmd == "sub_copy") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $db->query($query);

        $query  = " INSERT INTO HREPORT_SCHREG_SUBCLASS_REMARK_DAT( ";
        $query .= "       YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , SCHREGNO ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , UNITCD ";
        $query .= "     , SEQ ";
        $query .= "     , REMARK ";
        $query .= "     , REGISTERCD ";
        $query .= "     , UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "       YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , SCHREGNO ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "     , UNITCD ";
        $query .= "     , SEQ ";
        $query .= "     , REMARK ";
        $query .= "     , '".STAFFCD."' ";
        $query .= "     , sysdate() ";
        $query .= " FROM HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT MAIN";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->selectSemes}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND SEQ         IN ('".implode("','", $model->keyData)."') ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "           'X' ";
        $query .= "         FROM ";
        $query .= "           GRADE_KIND_SCHREG_GROUP_DAT SCH_GRP ";
        $query .= "         INNER JOIN GRADE_KIND_COMP_GROUP_DAT KIND_GRP ";
        $query .= "           ON KIND_GRP.YEAR                  = SCH_GRP.YEAR ";
        $query .= "           AND KIND_GRP.SEMESTER             = SCH_GRP.SEMESTER ";
        $query .= "           AND KIND_GRP.GAKUBU_SCHOOL_KIND   = SCH_GRP.GAKUBU_SCHOOL_KIND ";
        $query .= "           AND KIND_GRP.GHR_CD               = SCH_GRP.GHR_CD ";
        $query .= "           AND KIND_GRP.GRADE                = SCH_GRP.GRADE ";
        $query .= "           AND KIND_GRP.HR_CLASS             = SCH_GRP.HR_CLASS ";
        $query .= "           AND KIND_GRP.CONDITION            = SCH_GRP.CONDITION ";
        $query .= "           AND KIND_GRP.GROUPCD              = SCH_GRP.GROUPCD ";
        $query .= "           AND KIND_GRP.CLASSCD              = MAIN.CLASSCD ";
        $query .= "           AND KIND_GRP.SCHOOL_KIND          = MAIN.SCHOOL_KIND ";
        $query .= "           AND KIND_GRP.CURRICULUM_CD        = MAIN.CURRICULUM_CD ";
        $query .= "           AND KIND_GRP.SUBCLASSCD           = MAIN.SUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "           SCH_GRP.YEAR                      = MAIN.YEAR ";
        $query .= "           AND SCH_GRP.SEMESTER              = '9' ";
        $query .= "           AND SCH_GRP.GAKUBU_SCHOOL_KIND    = '{$model->groupInfo["GAKUBU_SCHOOL_KIND"]}' ";
        $query .= "           AND SCH_GRP.GHR_CD                = '{$model->groupInfo["GHR_CD"]}' ";
        $query .= "           AND SCH_GRP.GRADE                 = '{$model->groupInfo["GRADE"]}' ";
        $query .= "           AND SCH_GRP.HR_CLASS              = '{$model->groupInfo["HR_CLASS"]}' ";
        $query .= "           AND SCH_GRP.CONDITION             = '{$model->groupInfo["CONDITION"]}' ";
        $query .= "           AND SCH_GRP.GROUPCD               = '{$model->groupInfo["GROUPCD"]}' ";
        $query .= "           AND SCH_GRP.SCHREGNO              = MAIN.SCHREGNO ";
        $query .= "     ) ";
        // 指定科目でコピー
        if ($model->cmd == "sub_copy") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //COPY -- HREPORT_SCHREG_REMARK_DAT
    function &getCopyQuery($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $semester = ($model->field["INPUT_DIV"] != 1) ? $model->selectSemes : "9";
        $motoDiv = ($model->field["INPUT_DIV"] != 4) ? "01" : "03";

        //取込先テーブルのDIVと取込元テーブルのSEQの対応(DIV => SEQへの変換)
        if ($model->field["INPUT_DIV"] == 1) {
            $mapping = array("1" => "1");
        } else if ($model->field["INPUT_DIV"] == 2) {
            $mapping = array("1" => "3", 
                             "2" => "4",
                             "3" => "5");
        } else if ($model->field["INPUT_DIV"] == 4) {
            $mapping = array("1" => "4");
        }

        foreach($model->keyData as $sakiDiv) {
            $motoSeq = $mapping[$sakiDiv]; //取込先テーブルのDIVと取込元テーブルのSEQが対応

            //取込元テーブルのデータ取得
            $query  = " SELECT ";
            $query .= "     REMARK ";
            $query .= " FROM HREPORT_GUIDANCE_SCHREG_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR        = '{$model->exp_year}' ";
            $query .= "     AND SEMESTER    = '{$semester}' ";
            $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
            $query .= "     AND DIV         = '{$motoDiv}' ";
            $query .= "     AND SEQ         = '{$motoSeq}' ";
            $query .= "  ORDER BY  ";
            $query .= "     RECORD_DATE DESC LIMIT 1  ";
            $remark = $db->getOne($query);

            //取込先テーブルの存在チェック
            $query  = " SELECT COUNT(*) FROM ";
            $query .= "     HREPORT_SCHREG_REMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR        = '{$model->exp_year}' ";
            $query .= "     AND SEMESTER    = '{$semester}' ";
            $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
            $query .= "     AND DIV         = '{$sakiDiv}' ";
            $check = $db->getOne($query);

            $data = array();
            $data[$model->colName][TEXT]    = $remark;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            if ($check == 0) {
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = $sakiDiv;
                //追加
                $query = Query::insertSQL($data, "HREPORT_SCHREG_REMARK_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR        = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER    = '{$semester}' ";
                $where .= "     AND SCHREGNO    = '{$model->schregno}' ";
                $where .= "     AND DIV         = '{$sakiDiv}' ";
                //更新
                $query = Query::updateSQL($data, "HREPORT_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT -- HREPORT_SCHREG_REMARK_DAT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $semester = ($model->field["INPUT_DIV"] != 1) ? $model->selectSemes : "9";

        foreach($model->itemNameArr as $kindSeq => $itemName) {

            //存在チェック
            $itemNameToRemark = array_flip($model->remarkToItemName);
            $div = $itemNameToRemark[$kindSeq];
            $query = knjd428bQuery::getHreportSchregDat($model, $model->colName, $div);
            $check = $db->getOne($query);

            $data = array();
            $data[$model->colName][TEXT]    = $model->field["REMARK_{$kindSeq}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            if (!isset($check)) {
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["DIV"][TEXT]              = $div;
                //追加
                $query = Query::insertSQL($data, "HREPORT_SCHREG_REMARK_DAT");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR         = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER     = '{$semester}' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $where .= "     AND DIV          = '{$div}' ";
                //更新
                $query = Query::updateSQL($data, "HREPORT_SCHREG_REMARK_DAT", $where);
                $db->query($query);
            }

        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT -- HREPORT_SCHREG_SUBCLASS_REMARK_DAT
    function &getInsertSubclassQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

        foreach($model->itemNameArr as $kindSeq => $itemName) {

            //存在チェック
            $itemNameToRemark = array_flip($model->remarkToItemName);
            $seq = $itemNameToRemark[$kindSeq];
            $query = knjd428bQuery::getHreportSchregSubclassDat($model, $seq);
            $check = $db->getOne($query);

            $data = array();
            $data["REMARK"][TEXT]           = $this->field["REMARK_{$kindSeq}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            if (!isset($check)) {
                $data["YEAR"][TEXT]             = $model->exp_year;
                $data["SEMESTER"][TEXT]         = $model->selectSemes;
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["CLASSCD"][TEXT]          = $classCd;
                $data["SCHOOL_KIND"][TEXT]      = $schoolKind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculumCd;
                $data["SUBCLASSCD"][TEXT]       = $subclassCd;
                $data["UNITCD"][TEXT]           = '00';
                $data["SEQ"][NUMBER]            = $seq;
                //追加
                $query = Query::insertSQL($data, "HREPORT_SCHREG_SUBCLASS_REMARK_DAT ");
                $db->query($query);
            } else {
                $where  = " WHERE ";
                $where .= "         YEAR         = '{$model->exp_year}' ";
                $where .= "     AND SEMESTER     = '{$model->selectSemes}' ";
                $where .= "     AND SCHREGNO     = '{$model->schregno}' ";
                $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
                $where .= "     AND UNITCD       = '00' ";
                $where .= "     AND SEQ          = {$seq} ";
                //更新
                $query = Query::updateSQL($data, "HREPORT_SCHREG_SUBCLASS_REMARK_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

   //DELETE -- HREPORT_SCHREG_REMARK_DAT
   function &getDeleteQuery($model) {
        $semester = ($model->field["INPUT_DIV"] != 1) ? $model->selectSemes : "9";

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_SCHREG_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$semester}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND DIV         IN ('".implode("','", $model->keyData)."') ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
   }

   //DELETE -- HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT
   function &getDeleteSubclassQuery($model) {

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORT_SCHREG_SUBCLASS_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER    = '{$model->selectSemes}' ";
        $query .= "     AND SCHREGNO    = '{$model->schregno}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->subclasscd}' ";
        $query .= "     AND UNITCD      = '00' ";
        $query .= "     AND SEQ         IN ('".implode("','", $model->keyData)."') ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
   }

}
?>
