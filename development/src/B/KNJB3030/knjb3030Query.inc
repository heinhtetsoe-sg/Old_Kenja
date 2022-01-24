<?php

require_once('for_php7.php');

class knjb3030Query extends Query
{

    /*------------------------左フレーム-----------------------*/

    //学期リスト
    public function slectCombSeme()
    {
        $query = "select year,semester,semestername from semester_mst where semester<>'9' order by year,semester ";

        return $query;
    }

    //校種
    public function getSchoolKind($model)
    {
        list($year, $seme) = explode("-", $model->term);
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV2 ";

        return $query;
    }
    //状態区分（特別支援学校）
    public function getCondition()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A033' ";
        $query .= "     AND NAMESPARE3 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    // 通信制フラグ
    public function getZ001Namespare3()
    {
        $query  = " SELECT ";
        $query .= "     MAX(T1.NAMESPARE3) AS NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     INNER JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SCHOOLDIV = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.NAMECD1 = 'Z001' ";
        return $query;
    }
    //教科一覧
    public function getClassMst($model, $term)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CLASSNAME AS LABEL ";
        } else {
            $query .= "     CLASSCD AS VALUE, ";
            $query .= "     CLASSCD || '-' || CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".SUBSTR($term, 0, 4)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif (($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座一覧
    public function selectListTest($term, $group, $model, $asc_or_desc)
    {
        $query  = "SELECT ";
        $query .= "    t1.chaircd, ";
        $query .= "    t1.groupcd, ";
        $query .= "    w3.classcd, ";
        if ($group=="1") {
            $query .= "    w3.classname as groupname, ";
        } else {
            $query .= "    w1.groupname, ";
        }
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            $query .= "    t1.classcd, ";
            $query .= "    t1.school_kind, ";
            $query .= "    t1.curriculum_cd, ";
        }
        $query .= "    t1.subclasscd, ";
        $query .= "    w2.subclassname, ";
        $query .= "    t1.chairname ";
        $query .= "FROM ";
        $query .= "    chair_dat t1 left join v_electclass_mst w1 on w1.year=t1.year and w1.groupcd=t1.groupcd ";
        if ($model->Properties["knjb3030ExUserAuth"] == "1" && (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT)) {
            $query .= " left join chair_stf_dat t2 on t1.year = t2.year and t1.semester = t2.semester and t1.chaircd = t2.chaircd  ";
        }
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            $query .= "    left join v_subclass_mst w2 on w2.year=t1.year and w2.classcd=t1.classcd and w2.school_kind=t1.school_kind and ";
            $query .= "                                   w2.curriculum_cd=t1.curriculum_cd  and w2.subclasscd=t1.subclasscd ";
            $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=t1.classcd and ";
            $query .= "                                w3.school_kind=t1.school_kind ";
        } else {
            $query .= "    left join v_subclass_mst w2 on w2.year=t1.year and w2.subclasscd=t1.subclasscd ";
            $query .= "    left join v_class_mst w3 on w3.year=t1.year and w3.classcd=substr(t1.subclasscd,1,2) ";
        }
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            $query .= "    LEFT JOIN SUBCLASS_DETAIL_MST T3 ";
            $query .= "         ON T3.CLASSCD          = w2.CLASSCD ";
            $query .= "        AND T3.SCHOOL_KIND      = w2.SCHOOL_KIND ";
            $query .= "        AND T3.CURRICULUM_CD    = w2.CURRICULUM_CD ";
            $query .= "        AND T3.SUBCLASSCD       = w2.SUBCLASSCD ";
            $query .= "        AND T3.SUBCLASS_SEQ     = '001' ";
        }
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        if ($model->Properties["knjb3030ExUserAuth"] == "1" && (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT)) {
            $query .= " t2.staffcd = '".STAFFCD."' AND ";
        }
        if ($model->schoolKind != "999") {
            $query .= "    T1.SCHOOL_KIND ='{$model->schoolKind}' AND ";
        }
        if ($model->selectClass != "999") {
            if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND ='{$model->selectClass}' AND ";
            } else {
                $query .= "    T1.CLASSCD = '{$model->selectClass}' AND ";
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     t1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif (($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     t1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        if ($model->Properties["useSpecial_Support_School"] == "1") {
            if ($model->condition != "999") {
                $query .= "    T3.SUBCLASS_REMARK1 = '".$model->condition."' AND ";
            }
        }
        //絞込み（教科の場合、群コード：０）（群の場合、群コード：０以外）
        if ($group=="1") {
            $query .= "    t1.groupcd='0000' ";
            $query .= " ORDER BY ";
            if ($model->sort_name == "url_name") {
                $query .= " T1.CHAIRCD {$asc_or_desc}, ";
            }
            if ($model->sort_name == "subclass_name") {
                if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                    $query .= " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD {$asc_or_desc}, ";
                } else {
                    $query .= " T1.SUBCLASSCD {$asc_or_desc}, ";
                }
                $query .= " T1.CHAIRCD  ASC, ";
            }
            if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ASC, ";
            } else {
                $query .= "     T1.SUBCLASSCD ASC, ";
            }
            $query .= "     T1.GROUPCD ASC ";
        } else {
            $query .= "    T1.GROUPCD<>'0000' ";
            $query .= "ORDER BY ";
            if ($model->sort_name == "url_name") {
                $query .= " T1.CHAIRCD {$asc_or_desc}, ";
            }
            if ($model->sort_name == "subclass_name") {
                if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                    $query .= " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD {$asc_or_desc}, ";
                } else {
                    $query .= " T1.SUBCLASSCD {$asc_or_desc}, ";
                }
                $query .= " T1.CHAIRCD  ASC, ";
            }
            if ($model->sort_name == "group_name") {
                $query .= " T1.GROUPCD {$asc_or_desc}, ";
                if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                    $query .= " T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ASC, ";
                } else {
                    $query .= " T1.SUBCLASSCD ASC, ";
                }
                $query .= " T1.CHAIRCD ASC, ";
            }
            $query .= "     T1.GROUPCD ASC, ";
            if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ASC ";
            } else {
                $query .= "     T1.SUBCLASSCD ASC ";
            }
        }

        return $query;
    }

    //コピー先に講座データが存在するかをチェック----------
    public function getChairDatcount($model, $term)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat";
        $query .=" WHERE year ='".SUBSTR($term, 0, 4)."' AND semester='".SUBSTR($term, 5, 1)."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif (($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //コピー元の講座データが存在するかをチェック
    public function count($term2, $schoolKind)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat";
        $query .=" WHERE year ='".SUBSTR($term2, 0, 4)."' AND semester='".SUBSTR($term2, 5, 1)."'";
        if ($schoolKind != "999") {
            $query .="     AND school_kind='".$schoolKind."'";
        }

        return $query;
    }
    //対象年度の在籍ヘッダデータおよび在籍データの存在チェック
    public function countSchregRegd($term, $table)
    {
        $query  = "SELECT COUNT(*) FROM ".$table."";
        $query .=" WHERE year ='".SUBSTR($term, 0, 4)."' AND semester='".SUBSTR($term, 5, 1)."'";

        return $query;
    }

    //参照先の講座受講生徒データが妥当なデータか確かめる(DBエラー(重複)で落とさないため)
    public function checkChairStdDat($term2, $schoolKind)
    {
        $query  = " WITH COUNT_CHAIR_STD_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST L1 ON ";
        $query .= "         T1.YEAR       = L1.YEAR AND ";
        $query .= "         T1.SEMESTER   = L1.SEMESTER AND ";
        $query .= "         T1.APPENDDATE = L1.EDATE ";
        if ($schoolKind != "999") {
            $query .= " INNER JOIN ";
            $query .= "     CHAIR_DAT T2 ON ";
            $query .= "         T1.YEAR = T2.YEAR AND ";
            $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "         T1.CHAIRCD = T2.CHAIRCD AND";
            $query .= "         T2.SCHOOL_KIND = '".$schoolKind."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".SUBSTR($term2, 0, 4)."' AND ";
        $query .= "     T1.SEMESTER = '".SUBSTR($term2, 5, 1)."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " HAVING COUNT(*) > 1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COUNT_CHAIR_STD_DAT ";

        return $query;
    }

    public function getSemeSdateEdate($model)
    {
        $query = "select sdate,edate from semester_mst where year || '-' || semester = '".$model->term."'";
        return $query;
    }

    public function execute($db, $query)
    {
        $db->query($query);
    }

    //「コピーボタン」押し時の処理（メイン）
    public function getInsertCopyQuery($db, $model, $term2, $term, $check, $appdate, $appenddate, $checkGrddiv, $schoolKind)
    {
        $db->autoCommit(false);

        //前処理（対象の各データを全て削除）
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_cls_dat", $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_stf_dat", $schoolKind));
        if ($model->Properties["useChairStaffOrder"] == "1") {
            knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_stf_detail_dat", $schoolKind));
        }
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_textbook_dat", $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_fac_dat", $schoolKind));
        if ($model->Properties["useTestFacility"] == "1") {
            knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_test_fac_dat", $schoolKind));
        }
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_detail_dat", $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_std_dat", $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::deleteCopy($term, "chair_dat", $schoolKind, "1")); // 講座データを最後に消す
        //追加処理（参照から対象へ全てインサート）
        knjb3030Query::execute($db, knjb3030Query::insertChrDat($model, $term2, $term, $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::insertClsDat($term2, $term, $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::insertStfDat($term2, $term, $schoolKind));
        if ($model->Properties["useChairStaffOrder"] == "1") {
            knjb3030Query::execute($db, knjb3030Query::insertStfDetailDat($term2, $term, $schoolKind));
        }
        knjb3030Query::execute($db, knjb3030Query::insertTexDat($term2, $term, $schoolKind));
        knjb3030Query::execute($db, knjb3030Query::insertFacDat($term2, $term, $schoolKind));
        if ($model->Properties["useTestFacility"] == "1") {
            knjb3030Query::execute($db, knjb3030Query::insertTestFacDat($term2, $term, $schoolKind));
        }
        knjb3030Query::execute($db, knjb3030Query::insertDetailDat($term2, $term, $schoolKind));
        //生徒もコピー
        if ($check==1) {
            knjb3030Query::execute($db, knjb3030Query::insertStdDat($term2, $term, $appdate, $appenddate, $checkGrddiv, $schoolKind));
        }

        $db->commit();
    }

    //前処理（対象の各データを全て削除）
    public function deleteCopy($term, $table, $schoolKind, $where = "")
    {
        $query  = "DELETE FROM ".$table." ";
        $query .= " WHERE year     = '".SUBSTR($term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($term, 5)."'";
        if ($where) {
            $query .= "        AND school_kind = '".$schoolKind."'";
        } elseif ($schoolKind != "999") {
            $query .= "  AND CHAIRCD IN (SELECT CHAIRCD ";
            $query .= "      FROM CHAIR_DAT T2 ";
            $query .= "      WHERE T2.year     = '".SUBSTR($term, 0, 4)."'";
            $query .= "        AND T2.semester = '".SUBSTR($term, 5)."'";
            $query .= "        AND (T2.school_kind is null or T2.school_kind = '".$schoolKind."') ";
            $query .= "  ) ";
        }

        return $query;
    }

    //追加処理（参照から対象へ全てインサート）
    //講座データ
    public function insertChrDat($model, $term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO chair_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            $query .="        chaircd, groupcd, classcd, school_kind, curriculum_cd, subclasscd, ";
            $query .="        chairname, chairabbv, takesemes, lessoncnt, framecnt,";
        } else {
            $query .="        chaircd, groupcd, subclasscd, chairname, chairabbv, takesemes, lessoncnt, framecnt,";
        }
        $query .="        countflg,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_dat";
        $query .="  WHERE year = '".SUBSTR($term2, 0, 4)."' AND semester = '".SUBSTR($term2, 5, 1)."'";
        if ($schoolKind != "999") {
            $query .="      AND school_kind = '".$schoolKind."'";
        }
        $query .="      AND CHAIRCD NOT IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";
        return $query;
    }
    //講座受講クラスデータ
    public function insertClsDat($term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO chair_cls_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        T1.chaircd, T1.groupcd, T1.trgtgrade, T1.trgtclass,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_cls_dat T1";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."'";
        $query .="      AND (T1.CHAIRCD, T1.GROUPCD, T1.TRGTGRADE, T1.TRGTCLASS) NOT IN (SELECT CHAIRCD, GROUPCD, TRGTGRADE, TRGTCLASS FROM chair_cls_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }
    //講座担当職員データ
    public function insertStfDat($term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO chair_stf_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        T1.chaircd, T1.staffcd, T1.chargediv,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_stf_dat T1";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."'";
        $query .="      AND (T1.CHAIRCD, T1.STAFFCD) NOT IN (SELECT CHAIRCD, STAFFCD FROM chair_stf_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }
    //講座担当職員Detailデータ
    public function insertStfDetailDat($term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO CHAIR_STF_DETAIL_DAT";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        T1.chaircd, ";
        $query .="        T1.staffcd, ";
        $query .="        T1.SEQ,";
        $query .="        T1.REMARK_SINT1,";
        $query .="        T1.REMARK_SINT2,";
        $query .="        T1.REMARK_INT1,";
        $query .="        T1.REMARK_INT2,";
        $query .="        T1.REMARK_INT3,";
        $query .="        T1.REMARK_CHAR1,";
        $query .="        T1.REMARK_CHAR2,";
        $query .="        T1.REMARK_CHAR3,";
        $query .="        T1.REMARK_CHAR4,";
        $query .="        T1.REMARK_CHAR5,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM CHAIR_STF_DETAIL_DAT T1";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."'";
        $query .="      AND (T1.CHAIRCD, T1.STAFFCD, T1.SEQ) NOT IN (SELECT CHAIRCD, STAFFCD, SEQ FROM CHAIR_STF_DETAIL_DAT WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }
    //講座教科書データ
    public function insertTexDat($term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO chair_textbook_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        T1.chaircd, T1.textbookcd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_textbook_dat T1";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."'";
        $query .="      AND (T1.chaircd, T1.textbookcd) NOT IN (SELECT chaircd, textbookcd FROM chair_textbook_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }
    //講座使用施設データ
    public function insertFacDat($term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO chair_fac_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        T1.chaircd, T1.faccd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_fac_dat T1";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."'";
        $query .="      AND (T1.chaircd, T1.faccd) NOT IN (SELECT chaircd, faccd FROM chair_fac_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }
    //試験会場データ
    public function insertTestFacDat($term2, $term, $schoolKind)
    {
        $query  =" INSERT INTO chair_test_fac_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        T1.chaircd, T1.faccd,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_test_fac_dat T1";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."'";
        $query .="      AND (T1.chaircd, T1.faccd) NOT IN (SELECT chaircd, faccd FROM chair_test_fac_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }

    //講座使用施設データ
    public function insertDetailDat($term2, $term, $schoolKind)
    {
        $query  = " INSERT INTO chair_detail_dat (year,semester,chaircd,seq,remark1,remark2,remark3,registercd,updated) ";
        $query .= " SELECT '".SUBSTR($term, 0, 4)."', ";
        $query .= "        '".SUBSTR($term, 5, 1)."', ";
        $query .= "        T1.chaircd, T1.seq, T1.remark1, T1.remark2, T1.remark3, ";
        $query .= "        '".STAFFCD."', SYSDATE() ";
        $query .= " FROM chair_detail_dat T1 ";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat T2 ON";
            $query .= "          T1.year = T2.year AND";
            $query .= "          T1.semester = T2.semester AND";
            $query .= "          T1.chaircd = T2.chaircd AND";
            $query .= "          T2.school_kind = '".$schoolKind."'";
        }
        $query .= " WHERE T1.year = '".SUBSTR($term2, 0, 4)."' AND T1.semester = '".SUBSTR($term2, 5, 1)."' ";
        $query .="      AND (T1.chaircd, T1.seq) NOT IN (SELECT chaircd, seq FROM chair_detail_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";

        return $query;
    }

    //生徒もコピー
    //講座受講生徒データ
    public function insertStdDat($term2, $term, $appdate, $appenddate, $checkGrddiv, $schoolKind)
    {
        $query  =" INSERT INTO chair_std_dat";
        $query .=" SELECT '".SUBSTR($term, 0, 4)."',";
        $query .="        '".SUBSTR($term, 5, 1)."',";
        $query .="        w1.chaircd, w1.schregno, '".$appdate."', '".$appenddate."', w1.row, w1.column,";
        $query .="        '".STAFFCD."', SYSDATE()";
        $query .="   FROM chair_std_dat w1 ";
        $query .="   INNER JOIN semester_mst w2 on ";
        $query .="        w1.year = w2.year AND w1.semester = w2.semester AND w1.appenddate = w2.edate ";
        if ($schoolKind != "999") {
            $query .= "  INNER JOIN";
            $query .= "      chair_dat w3 ON";
            $query .= "          w1.year = w3.year AND";
            $query .= "          w1.semester = w3.semester AND";
            $query .= "          w1.chaircd = w3.chaircd AND";
            $query .= "          w3.school_kind = '".$schoolKind."'";
        }
        $query .="  WHERE w1.year = '".SUBSTR($term2, 0, 4)."' AND w1.semester = '".SUBSTR($term2, 5, 1)."' ";
        if ($checkGrddiv != 1) {
            $query .="        AND W1.SCHREGNO NOT IN ( ";
            $query .="                SELECT SCHREGNO ";
            $query .="                FROM   SCHREG_BASE_MST K1,SEMESTER_MST K2 ";
            $query .="                WHERE  YEAR='".SUBSTR($term2, 0, 4)."' AND SEMESTER='".SUBSTR($term2, 5, 1)."' AND ";
            $query .="                       GRD_DIV IN ('1','2','3') AND GRD_DATE <= EDATE ) ";
        }
        $query .="      AND (w1.chaircd, w1.schregno, w1.appdate) NOT IN (SELECT chaircd, schregno, appdate FROM chair_std_dat WHERE YEAR = '".SUBSTR($term, 0, 4)."' AND SEMESTER = '".SUBSTR($term, 5, 1)."') ";
        return $query;
    }

    /*------------------------右フレーム-----------------------*/

    //列名リスト
    public function getRetumei($model)
    {
        list($year, $seme) = explode("-", $model->term);
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'B023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //警告点区分
    public function getIsKeikokuten($model)
    {
        list($year)=explode('-', $model->term);
    
        $query .= " SELECT ";
        $query .= "     SCHOOL_REMARK1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "      YEAR='{$year}' ";
        $query .= "     AND SCHOOL_SEQ='009' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            if ($model->schoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->schoolKind), "','")."') ";
            } else {
                $query .= " AND SCHOOL_KIND = '' ";
            }
        } elseif (($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
    
        return $query;
    }

    //リストコンボ用取得
    //科目
    public function getSubclass($model, $year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query  = "SELECT DISTINCT ";
            $query .= "       classcd || '-' || school_kind || '-' || curriculum_cd || '-' || subclasscd as subclasscd, ";
            $query .= "       subclassname ";
            $query .= "  FROM v_subclass_mst ";
            $query .= " WHERE year   = '".$year."' ";
            if ($model->schoolKind != "999") {
                $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY subclasscd ";
        } else {
            $query  = "SELECT DISTINCT subclasscd, subclassname ";
            $query .= "  FROM v_subclass_mst ";
            $query .= " WHERE year   = '".$year."' ";
            if ($model->schoolKind != "999") {
                $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
            }
            $query .= " ORDER BY subclasscd";
        }

        $result = $db->query($query);//naka 05.12.23

        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"],
                           "value" => $row["SUBCLASSCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //科目（特別支援学校）
    public function getSubclassCondition($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '　' || T1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_DETAIL_MST T2 ";
        $query .= "          ON T1.CLASSCD          = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= "         AND T2.SUBCLASS_SEQ     = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".$year."' ";
        if ($model->schoolKind != "999") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode("','", explode(':', $model->selectSchoolKind))."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->condition != "999") {
            $query .= " AND T2.SUBCLASS_REMARK1 = '".$model->condition."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //履修期間区分
    public function getTakesemes($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT DISTINCT semester, semestername ";
        $query .= "  FROM semester_mst ";
        $query .= " WHERE year   = '".$year."' and semester<>'9' ";
        $query .= " ORDER BY semester";

        $result = $db->query($query);//naka 05.12.23

        $opt[] = array("label" => "0　通年", "value" => "0");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SEMESTER"]."　".$row["SEMESTERNAME"],
                           "value" => $row["SEMESTER"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //履修コース
    public function getRisyuuCourse($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = " SELECT DISTINCT namecd2, name1 ";
        $query .= " FROM v_name_mst ";
        $query .= " WHERE year = '".$year."' and namecd1 = 'B022' ";
        $query .= " ORDER BY namecd2";

        $result = $db->query($query);

        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."　".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }
    //群
    public function getGroup($year)
    {
        $db  = Query::dbCheckOut();
        $opt = array();
        $query  = "SELECT DISTINCT groupcd, groupname ";
        $query .= "  FROM v_electclass_mst ";
        $query .= " WHERE year   = '".$year."' ";
        $query .= " ORDER BY groupcd";

        $result = $db->query($query);//naka 05.12.23

        $opt[] = array("label" => "0000　なし（ＨＲ）", "value" => "0000");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GROUPCD"]."　".$row["GROUPNAME"],
                           "value" => $row["GROUPCD"]);
        }
        Query::dbCheckIn($db);
        return $opt;
    }

    //選択ダイアログ
    //HRクラス
    public function selectQuerySubFormHr($model, $term)
    {
        $query  = "SELECT ";
        $query .= "    grade || hr_class as grade_class, ";
        $query .= "    hr_name, ";
        $query .= "    hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    schreg_regd_hdat T1 ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    semester='".SUBSTR($term, 5)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //選択ダイアログ
    //授業クラス
    public function selectQuerySubFormLc($model, $term)
    {
        $query  = "SELECT ";
        $query .= "    GRADE || LC_CLASS AS GRADE_LC_CLASS, ";
        $query .= "    LC_NAME, ";
        $query .= "    LC_NAMEABBV ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_LC_HDAT T1 ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    SEMESTER = '".SUBSTR($term, 5)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE_LC_CLASS ";

        return $query;
    }
    //科目担任
    public function selectQuerySubFormStaff($model, $staffcd, $subclass)
    {
        $query  = "SELECT ";
        $query .= "    T1.SECTIONCD, ";
        $query .= "    T2.SECTIONABBV, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "    V_STAFF_MST T1 ";
        $query .= "    LEFT JOIN V_SECTION_MST T2 ON T2.YEAR =T1.YEAR AND T2.SECTIONCD = T1.SECTIONCD ";
        $query .= "    LEFT JOIN STAFF_CLASS_MST T3 ON T3.STAFFCD = T1.STAFFCD ";
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
            $query .= "     AND T3.CLASSCD         = '".$classcd."' ";
            $query .= "     AND T3.SCHOOL_KIND     = '".$school_kind."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND T3.CLASSCD = SUBSTR('".$subclass."', 1, 2) ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR='".SUBSTR($model->term, 0, 4)."' ";
        $query .= " ORDER BY ";
        if ($model->Properties["knjb3030SubFormStaffOrder"] == "1") {
            $query .= "    T1.SECTIONCD,";
            $query .= "    T1.STAFFNAME_KANA,";
            $query .= "    T1.STAFFCD";
        } else {
            $query .= "    CASE WHEN T3.STAFFCD IS NOT NULL ";
            $query .= "         THEN 0 ";
            $query .= "         ELSE 1 ";
            $query .= "    END, ";
            $query .= "    T1.STAFFCD, ";
            $query .= "    T1.SECTIONCD ";
        }
        return $query;
    }

    //資格科目
    public function getStaffClass($model, $subclass, $flg = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        if ($flg) {
            $query .= " WHERE ";
            if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $subclass);
                $query .= "     CLASSCD         = '".$classcd."' AND ";
                $query .= "     SCHOOL_KIND     = '".$school_kind."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            } else {
                $query .= "     CLASSCD = SUBSTR('".$subclass."',1,2) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //使用施設
    public function selectQuerySubFormFacility($term)
    {
        $query  = "SELECT ";
        $query .= "    faccd, ";
        $query .= "    facilityname, ";
        $query .= "    facilityabbv ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }
    //教科書
    public function selectQuerySubFormTextBook($term)
    {
        $query  = "SELECT ";
        $query .= "    textbookcd, ";
        $query .= "    textbookname, ";
        $query .= "    textbookabbv ";
        $query .= "FROM ";
        $query .= "    v_textbook_mst ";
        $query .= "WHERE ";
        $query .= "    year='".SUBSTR($term, 0, 4)."' ";
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //１レコード取得
    //（講座）
    public function getRowTest($model, $term, $chaircd)
    {
        $db  = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.REMARK1 AS STD_CNT, ";
        $query .= "     L1.REMARK2 AS NOT_DAY_ATTEND, ";
        $query .= "     L2.REMARK1 AS RISYUU_COURSE, ";
        $query .= "     L3.REMARK1 AS KEIKOKUTEN_SOTEN, ";
        $query .= "     L3.REMARK2 AS KEIKOKUTEN_HYOUKA, ";
        $query .= "     L3.REMARK3 AS KEIKOKUTEN_HYOUTEI, ";
        $query .= "     L4.REMARK1 AS SEQ004_REMARK1, ";
        $query .= "     L4.REMARK2 AS SEQ004_REMARK2, ";
        $query .= "     L4.REMARK3 AS SEQ004_REMARK3 ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L1 ";
        $query .= "          ON T1.YEAR     = L1.YEAR ";
        $query .= "         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L1.CHAIRCD ";
        $query .= "         AND L1.SEQ      = '001' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L2 ";
        $query .= "          ON T1.YEAR     = L2.YEAR ";
        $query .= "         AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L2.CHAIRCD ";
        $query .= "         AND L2.SEQ      = '002' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L3 ";
        $query .= "          ON T1.YEAR     = L3.YEAR ";
        $query .= "         AND T1.SEMESTER = L3.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L3.CHAIRCD ";
        $query .= "         AND L3.SEQ      = '003' ";
        $query .= "     LEFT JOIN CHAIR_DETAIL_DAT L4 ";
        $query .= "          ON T1.YEAR     = L4.YEAR ";
        $query .= "         AND T1.SEMESTER = L4.SEMESTER ";
        $query .= "         AND T1.CHAIRCD  = L4.CHAIRCD ";
        $query .= "         AND L4.SEQ      = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".SUBSTR($term, 0, 4)."' ";
        $query .= " AND T1.SEMESTER = '".SUBSTR($term, 5)."' ";
        $query .= " AND T1.CHAIRCD  = '".$chaircd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //（授業回数）
    public function getRowLesson($term, $groupcd)
    {
        $db  = Query::dbCheckOut();

        $query  = "SELECT distinct lessoncnt,framecnt FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($term, 0, 4)."' ";
        $query .= "AND semester = '".SUBSTR($term, 5)."' ";
        $query .= "AND groupcd  = '".$groupcd."' ";
        $query .= "order by lessoncnt desc ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //HRクラス取得
    public function getGradeClass($model, $term, $chaircd, $groupcd)
    {
        $query  = "SELECT ";
        $query .= "    t1.trgtgrade || t1.trgtclass as grade_class, ";
        $query .= "    t2.hr_nameabbv ";
        $query .= "FROM ";
        $query .= "    chair_cls_dat t1, ";
        $query .= "    schreg_regd_hdat t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        if ($groupcd > "0000") {
            $query .= "    t1.groupcd='".$groupcd."' AND ";
        } else {
            $query .= "    t1.chaircd='".$chaircd."' AND ";
        }
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.semester=t2.semester AND ";
        $query .= "    t1.trgtgrade=t2.grade AND ";
        $query .= "    t1.trgtclass=t2.hr_class ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND t1.trgtgrade IN (SELECT ";
                $query .= "                         G1.GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT G1 ";
                $query .= "                     WHERE ";
                $query .= "                         G1.YEAR         = T1.YEAR AND ";
                $query .= "                         G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND t1.trgtgrade IN (SELECT ";
            $query .= "                         G1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT G1 ";
            $query .= "                     WHERE ";
            $query .= "                         G1.YEAR         = T1.YEAR AND ";
            $query .= "                         G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY 1 ";

        return $query;
    }

    //授業クラス取得
    public function getGradeLcClass($model, $term, $chaircd, $groupcd)
    {
        $query  = "SELECT ";
        $query .= "    T1.LCGRADE || T1.LCCLASS AS GRADE_LC_CLASS, ";
        $query .= "    T2.LC_NAMEABBV ";
        $query .= "FROM ";
        $query .= "    CHAIR_LC_DAT T1, ";
        $query .= "    SCHREG_REGD_LC_HDAT T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    T1.SEMESTER = '".SUBSTR($term, 5)."' AND ";
        if ($groupcd > "0000") {
            $query .= "    T1.GROUPCD = '".$groupcd."' AND ";
        } else {
            $query .= "    T1.CHAIRCD = '".$chaircd."' AND ";
        }
        $query .= "    T1.YEAR = T2.YEAR AND ";
        $query .= "    T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "    T1.LCGRADE = T2.GRADE AND ";
        $query .= "    T1.LCCLASS = T2.LC_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.LCGRADE IN (SELECT ";
                $query .= "                         G1.GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT G1 ";
                $query .= "                     WHERE ";
                $query .= "                         G1.YEAR         = T1.YEAR AND ";
                $query .= "                         G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.LCGRADE IN (SELECT ";
            $query .= "                         G1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT G1 ";
            $query .= "                     WHERE ";
            $query .= "                         G1.YEAR         = T1.YEAR AND ";
            $query .= "                         G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE_LC_CLASS ";

        return $query;
    }
    //科目担任取得
    public function getStaff($model, $term, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     VALUE(T1.CHARGEDIV,0) AS CHARGEDIV, ";
        if ($model->Properties["useChairStaffOrder"] == "1") {
            $query .= "     CHR_STF_DETAIL.REMARK_SINT1 AS STF_ORDER, ";
        }
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        if ($model->Properties["useChairStaffOrder"] == "1") {
            $query .= "     LEFT JOIN CHAIR_STF_DETAIL_DAT CHR_STF_DETAIL ON T1.YEAR = CHR_STF_DETAIL.YEAR ";
            $query .= "          AND T1.SEMESTER = CHR_STF_DETAIL.SEMESTER ";
            $query .= "          AND T1.CHAIRCD = CHR_STF_DETAIL.CHAIRCD ";
            $query .= "          AND T1.STAFFCD = CHR_STF_DETAIL.STAFFCD ";
            $query .= "          AND CHR_STF_DETAIL.SEQ = '001' ";
        }
        $query .= "     , V_STAFF_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".SUBSTR($term, 0, 4)."' AND ";
        $query .= "     T1.SEMESTER = '".SUBSTR($term, 5)."' AND ";
        $query .= "     T1.CHAIRCD = '".$chaircd."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.STAFFCD = T2.STAFFCD ";
        $query .= "  ORDER BY ";
        $query .= "      T1.STAFFCD ";

        return $query;
    }
    //使用施設取得
    public function getFac($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.faccd, ";
        $query .= "    t2.facilityabbv ";
        $query .= "FROM ";
        $query .= "    chair_fac_dat t1, ";
        $query .= "    v_facility_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.faccd=t2.faccd ";
        $query .= " ORDER BY t1.faccd";

        return $query;
    }
    //使用施設取得
    public function getSikenKaizyou($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.faccd, ";
        $query .= "    t2.facilityabbv ";
        $query .= "FROM ";
        $query .= "    chair_test_fac_dat t1, ";
        $query .= "    v_facility_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.faccd=t2.faccd ";
        $query .= " ORDER BY t1.faccd";

        return $query;
    }
    //教科書取得
    public function getTextbook($term, $chaircd)
    {
        $query  = "SELECT ";
        $query .= "    t1.textbookcd, ";
        $query .= "    t2.textbookabbv ";
        $query .= "FROM ";
        $query .= "    chair_textbook_dat t1, ";
        $query .= "    v_textbook_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year='".SUBSTR($term, 0, 4)."' AND ";
        $query .= "    t1.semester='".SUBSTR($term, 5)."' AND ";
        $query .= "    t1.chaircd='".$chaircd."' AND ";
        $query .= "    t1.year=t2.year AND ";
        $query .= "    t1.textbookcd=t2.textbookcd ";
        $query .= " ORDER BY t1.textbookcd ";

        return $query;
    }
    //--- INSERT（講座データ）
    public function insertChair($model)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]      = $classcd;
            $data["SCHOOL_KIND"][TEXT]  = $school_kind;
            $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->fields["SUBCLASSCD"];
        }
        $data["CHAIRNAME"][TEXT]    = $model->fields["CHAIRNAME"];
        $data["CHAIRABBV"][TEXT]    = $model->fields["CHAIRABBV"];
        $data["TAKESEMES"][TEXT]    = $model->fields["TAKESEMES"];
        $data["LESSONCNT"][NUMBER]  = $model->fields["LESSONCNT"];
        $data["FRAMECNT"][NUMBER]   = $model->fields["FRAMECNT"];
        $data["COUNTFLG"][TEXT]     = ($model->fields["COUNTFLG"] == "1") ? $model->fields["COUNTFLG"] : "0";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_dat");

        return $query;
    }
    //--- UPDATE（講座データ）
    public function updateChair($model)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["useSpecial_Support_School"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->fields["SUBCLASSCD"]);
            $data["CLASSCD"][TEXT]      = $classcd;
            $data["SCHOOL_KIND"][TEXT]  = $school_kind;
            $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]   = $subclasscd;
        } else {
            $data["SUBCLASSCD"][TEXT]   = $model->fields["SUBCLASSCD"];
        }
        $data["CHAIRNAME"][TEXT]    = $model->fields["CHAIRNAME"];
        $data["CHAIRABBV"][TEXT]    = $model->fields["CHAIRABBV"];
        $data["TAKESEMES"][TEXT]    = $model->fields["TAKESEMES"];
        $data["LESSONCNT"][NUMBER]  = $model->fields["LESSONCNT"];
        $data["FRAMECNT"][NUMBER]   = $model->fields["FRAMECNT"];
        $data["COUNTFLG"][TEXT]     = ($model->fields["COUNTFLG"] == "1") ? $model->fields["COUNTFLG"] : "0";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        $where  = " WHERE year   = '".SUBSTR($model->term, 0, 4)."'";
        $where .= " AND semester = '".SUBSTR($model->term, 5)."'";
        $where .= " AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        $query = Query::updateSQL($data, "chair_dat", $where);

        return $query;
    }
    //--- CHEACK（講座データ）
    public function countChair($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";

        return $query;
    }
    //--- CHEACK（講座データ：群コード）
    public function checktoGroupcd($model)
    {
        $query  = "SELECT COUNT(*) FROM chair_dat ";
        $query .= "WHERE year   = '".SUBSTR($model->term, 0, 4)."' ";
        $query .= "AND semester = '".SUBSTR($model->term, 5)."' ";
        $query .= "AND chaircd  = '".$model->fields["CHAIRCD"]."' ";
        $query .= "AND groupcd  = '0000' ";

        return $query;
    }
    //--- INSERT（講座受講クラスデータ）
    public function insertChairCls($model, $grade_class)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        $data["TRGTGRADE"][TEXT]    = SUBSTR($grade_class, 0, 2);
        $data["TRGTCLASS"][TEXT]    = SUBSTR($grade_class, 2);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_cls_dat");
        return $query;
    }
    //--- INSERT（講座受講クラスデータ）
    public function insertChairLcCls($model, $grade_lc_class)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["GROUPCD"][TEXT]      = $model->fields["GROUPCD"];
        $data["LCGRADE"][TEXT]      = SUBSTR($grade_lc_class, 0, 2);
        $data["LCCLASS"][TEXT]      = SUBSTR($grade_lc_class, 2);
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_LC_DAT");

        return $query;
    }
    //--- DELETE（HRクラスデータ）
    public function deleteChairCls($model)
    {
        $query  = "DELETE FROM CHAIR_CLS_DAT ";
        $query .= " WHERE YEAR     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND SEMESTER = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND CHAIRCD  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- DELETE（授業クラスデータ）
    public function deleteChairLcCls($model)
    {
        $query  = "DELETE FROM CHAIR_LC_DAT ";
        $query .= " WHERE YEAR     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND SEMESTER = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND CHAIRCD  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座担当職員データ）
    public function insertChairStf($model, $staffcd, $chargediv)
    {
        $data = array();
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["STAFFCD"][TEXT]      = $staffcd;
        $data["CHARGEDIV"][NUMBER]  = $chargediv;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_stf_dat");

        return $query;
    }
    //--- DELETE（講座担当職員データ）
    public function deleteChairStf($model)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座担当職員データ）
    public function insertChairStfDetail($model, $staffcd, $order)
    {
        $data = array();
        $data["YEAR"][TEXT]            = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]        = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]         = $model->fields["CHAIRCD"];
        $data["STAFFCD"][TEXT]         = $staffcd;
        $data["SEQ"][TEXT]             = "001";
        $data["REMARK_SINT1"][NUMBER]  = $order;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][NUMBER]       = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_STF_DETAIL_DAT");

        return $query;
    }
    //--- DELETE（講座担当職員詳細データ）
    public function deleteChairStfDetail($model)
    {
        $query  = "DELETE FROM CHAIR_STF_DETAIL_DAT ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- INSERT（講座使用施設データ）
    public function insertChairFac($model, $faccd)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["FACCD"][TEXT]        = $faccd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_fac_dat");

        return $query;
    }
    //--- INSERT（試験会場データ）
    public function insertChairTestFac($model, $faccd)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["FACCD"][TEXT]        = $faccd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_test_fac_dat");

        return $query;
    }
    //--- DELETE（講座使用施設データ）
    public function deleteChairFac($model)
    {
        $query  = "DELETE FROM chair_fac_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- DELETE（試験会場データ）
    public function deleteChairTestFac($model)
    {
        $query  = "DELETE FROM chair_test_fac_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }
    //--- DELETE（講座受講生徒データ）
    public function deleteChairStd($model)
    {
        $query  = "DELETE FROM chair_std_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }

    //--- INSERT（講座詳細データ）
    public function insertChairDetail($model, $seq, $array)
    {
        $data = array();
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["SEQ"][TEXT]          = $seq;
        foreach ($array as $key => $val) {
            $data[$key][TEXT]           = $model->fields[$val];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_detail_dat");

        return $query;
    }

    //--- DELETE（講座詳細データ）
    public function deleteChairDetail($model, $seq = "")
    {
        $query  = " DELETE FROM chair_detail_dat ";
        $query .= " WHERE year      = '".substr($model->term, 0, 4)."' ";
        $query .= "   AND semester  = '".substr($model->term, 5)."' ";
        $query .= "   AND chaircd   = '".$model->fields["CHAIRCD"]."' ";
        if ($seq) {
            $query .= "   AND seq       = '".$seq."' ";
        }

        return $query;
    }
    //--- INSERT（講座教科書データ）
    public function insertChairTextbook($model, $textbookcd)
    {
        $data["YEAR"][TEXT]         = substr($model->term, 0, 4);
        $data["SEMESTER"][TEXT]     = substr($model->term, 5);
        $data["CHAIRCD"][TEXT]      = $model->fields["CHAIRCD"];
        $data["TEXTBOOKCD"][TEXT]   = $textbookcd;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "SYSDATE()";

        $query = Query::insertSQL($data, "chair_textbook_dat");

        return $query;
    }
    //--- DELETE（講座教科書データ）
    public function deleteChairTextbook($model)
    {
        $query  = "DELETE FROM chair_textbook_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";

        return $query;
    }

    //「更新ボタン」押し時の処理（メイン）・・・データがない場合、追加。ある場合、更新。
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //配列データ
        $grade_class = $grade_lc_class = $staffcd = $chargediv = $textbookcd = $faccd = array();
        $grade_class    = explode(",", $model->fields["GRADE_CLASS"]);
        $grade_lc_class = explode(",", $model->fields["GRADE_LC_CLASS"]);
        $staffcd        = explode(",", $model->fields["STAFFCD"]);
        $chargediv      = explode(",", $model->fields["CHARGEDIV"]);
        $order          = explode(",", $model->fields["ORDER"]);
        $textbookcd     = explode(",", $model->fields["TEXTBOOKCD"]);
        $faccd          = explode(",", $model->fields["FACCD"]);

        //講座データ
        $query = knjb3030Query::countChair($model);
        if ($db->getOne($query) > 0) {
            $db->query(knjb3030Query::updateChair($model));
            $ret_val = "Update";
        } else {
            $db->query(knjb3030Query::insertChair($model));
            $ret_val = "Insert";
        }
        //HRクラスデータ
        $db->query(knjb3030Query::deleteChairCls($model));
        if ($model->fields["GROUPCD"]=="0000") {
            if ($model->fields["GRADE_LC_CLASS"] == "") {
                if ($model->fields["GRADE_CLASS"] != "") {
                    for ($i = 0; $i < get_count($grade_class); $i++) {
                        $db->query(knjb3030Query::insertChairCls($model, $grade_class[$i]));
                    }
                }
            }
        }
        //授業クラスデータ
        $db->query(knjb3030Query::deleteChairLcCls($model));
        if ($model->fields["GROUPCD"]=="0000" && $model->Properties["useLc_Hrclass"] == "1") {
            if ($model->fields["GRADE_LC_CLASS"] != "") {
                for ($i = 0; $i < get_count($grade_lc_class); $i++) {
                    $db->query(knjb3030Query::insertChairLcCls($model, $grade_lc_class[$i]));
                }
            }
            if ($model->fields["GRADE_LC_CLASS"] != "") {
                $query = knjb3030Query::getLcToHrClass($model, $grade_lc_class);
                $result = $db->query($query);
                while ($lcRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $db->query(knjb3030Query::insertChairLcCls($model, $lcRow["GRADEHR"]));
                }
                $result->free();
            }
        }
        //講座担当職員データ
        $db->query(knjb3030Query::deleteChairStf($model));
        if ($model->Properties["useChairStaffOrder"] == "1") {
            $db->query(knjb3030Query::deleteChairStfDetail($model));
        }
        if ($model->fields["STAFFCD"] != "") {
            for ($i = 0; $i < get_count($staffcd); $i++) {
                $db->query(knjb3030Query::insertChairStf($model, $staffcd[$i], $chargediv[$i]));
                if ($model->Properties["useChairStaffOrder"] == "1") {
                    $db->query(knjb3030Query::insertChairStfDetail($model, $staffcd[$i], $order[$i]));
                }
            }
        }
        //講座教科書データ
        $db->query(knjb3030Query::deleteChairTextbook($model));
        if ($model->fields["TEXTBOOKCD"] != "") {
            for ($i = 0; $i < get_count($textbookcd); $i++) {
                $db->query(knjb3030Query::insertChairTextbook($model, $textbookcd[$i]));
            }
        }
        //講座使用施設データ
        $db->query(knjb3030Query::deleteChairFac($model));
        if ($model->fields["FACCD"] != "") {
            for ($i = 0; $i < get_count($faccd); $i++) {
                $db->query(knjb3030Query::insertChairFac($model, $faccd[$i]));
            }
        }
        if ($model->Properties["useTestFacility"] == "1") {
            $test_faccd     = explode(",", $model->fields["SIKENKAIZYOUFACCD"]);
            //試験会場データ
            $db->query(knjb3030Query::deleteChairTestFac($model));
            if ($model->fields["SIKENKAIZYOUFACCD"] != "") {
                for ($i = 0; $i < get_count($test_faccd); $i++) {
                    $db->query(knjb3030Query::insertChairTestFac($model, $test_faccd[$i]));
                }
            }
        }
        //講座詳細データ＋１日出欠対象外
        $db->query(knjb3030Query::deleteChairDetail($model, '001'));
        if ($model->fields["STD_CNT"] != "" || $model->fields["NOT_DAY_ATTEND"] == "1") {
            $array = array("REMARK1" => "STD_CNT", "REMARK2" => "NOT_DAY_ATTEND");
            $db->query(knjb3030Query::insertChairDetail($model, '001', $array));
        }
        //講座詳細データ＋履修コース
        $db->query(knjb3030Query::deleteChairDetail($model, '002'));
        if ($model->Properties["useChairDatRisyuuCourse"] == '1') {
            if ($model->fields["RISYUU_COURSE"] != "") {
                $array = array("REMARK1" => "RISYUU_COURSE");
                $db->query(knjb3030Query::insertChairDetail($model, '002', $array));
            }
        }
        if ($model->keikokutenFlag) {
            //講座詳細データ＋警告点
            $db->query(knjb3030Query::deleteChairDetail($model, '003'));
            $array = array("REMARK1" => "KEIKOKUTEN_SOTEN", "REMARK2" => "KEIKOKUTEN_HYOUKA", "REMARK3" => "KEIKOKUTEN_HYOUTEI");
            $db->query(knjb3030Query::insertChairDetail($model, '003', $array));
        }
        if ($model->Properties["chairRetsuMeisho_Hyouji"] == '1') {
            //列名称、スモールクラス名称、習熟度クラス名称
            $db->query(knjb3030Query::deleteChairDetail($model, '004'));
            $array = array("REMARK1" => "SEQ004_REMARK1", "REMARK2" => "SEQ004_REMARK2", "REMARK3" => "SEQ004_REMARK3");
            $db->query(knjb3030Query::insertChairDetail($model, '004', $array));
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //LC→HR更新対象
    public function getLcToHrClass($model, $grade_lc_class)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS AS GRADEHR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_DAT LCD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON LCD.YEAR = REGD.YEAR ";
        $query .= "           AND LCD.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND LCD.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     LCD.YEAR = '".(substr($model->term, 0, 4))."' ";
        $query .= "     AND LCD.SEMESTER = '".(substr($model->term, 5))."' ";
        $query .= "     AND LCD.GRADE || LCD.LC_CLASS IN ('".implode($grade_lc_class, "','")."') ";

        return $query;
    }

    //「削除ボタン」押し時の処理（メイン）
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM chair_dat ";
        $query .= " WHERE year     = '".SUBSTR($model->term, 0, 4)."'";
        $query .= "   AND semester = '".SUBSTR($model->term, 5)."'";
        $query .= "   AND chaircd  = '".$model->fields["CHAIRCD"]."'";
        $db->query($query);

        if ($model->fields["GROUPCD"]=="0000") {
            $db->query(knjb3030Query::deleteChairCls($model));
        }
        if ($model->fields["GROUPCD"]=="0000" && $model->Properties["useLc_Hrclass"] == "1") {
            $db->query(knjb3030Query::deleteChairLcCls($model));
        }
        $db->query(knjb3030Query::deleteChairStf($model));
        if ($model->Properties["useChairStaffOrder"] == "1") {
            $db->query(knjb3030Query::deleteChairStfDetail($model));
        }
        $db->query(knjb3030Query::deleteChairTextbook($model));
        $db->query(knjb3030Query::deleteChairFac($model));
        if ($model->Properties["useTestFacility"] == "1") {
            $db->query(knjb3030Query::deleteChairTestFac($model));
        }
        $db->query(knjb3030Query::deleteChairStd($model));
        $db->query(knjb3030Query::deleteChairDetail($model));

        $db->commit();
        Query::dbCheckIn($db);
        return "succeeded";
    }
}
