<?php

require_once('for_php7.php');

class knjz370Query extends Query {

    //学年コンボボックス
    function getGradeQuery()
    {
        $query  = "SELECT DISTINCT(grade) ";
        $query .= "  FROM schreg_regd_hdat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= " ORDER BY GRADE";
        return $query;
    }

    //リスト表示
    function getListQuery($select_grade, $mode="")
    {
        if($mode != "data"){
            $query  = "SELECT W1.grade,W1.course_seq,W1.hr_class,W2.hr_nameabbv ";
            $query .= "  FROM course_group_dat as W1 ";
            $query .= "  LEFT JOIN schreg_regd_hdat as w2 ";
            $query .= "         ON W1.year     = W2.year ";
            $query .= "        AND W1.grade    = W2.grade ";
            $query .= "        AND W1.hr_class = W2.hr_class ";
            $query .= " WHERE W1.year     = '".CTRL_YEAR."' ";
            $query .= "   AND W2.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "   AND W1.grade    = '".$select_grade."' ";
            $query .= " ORDER BY course_seq ";
        }else{
            $query  = "SELECT grade, course_seq, group_name ";
            $query .= "  FROM course_group_dat ";
            $query .= " WHERE year = '".CTRL_YEAR."' ";
            $query .= "   AND grade = '".$select_grade."' ";
            $query .= " GROUP BY grade, course_seq, group_name ";
            $query .= " ORDER BY course_seq ";
        }
        return $query;
    }

    //エディット表示
    function getEditQuery($select_grade, $course_seq)
    {
        $query  = "SELECT grade, course_seq, hr_class, group_name ";
        $query .= "  FROM course_group_dat ";
        $query .= " WHERE year       = '".CTRL_YEAR."' ";
        $query .= "   AND grade      = '".$select_grade."' ";
        $query .= "   AND course_seq = '".$course_seq."' ";
        $query .= " ORDER BY hr_class ";
        return $query;
    }

    //グループクラス(受講クラス)
    function getGroupQuery($select_grade, $course_seq)
    {
        $query  = "SELECT W1.grade, W1.hr_class, W2.hr_nameabbv ";
        $query .= "  FROM course_group_dat as W1 ";
        $query .= "  LEFT JOIN schreg_regd_hdat as w2 ";
        $query .= "         ON W1.year     = W2.year ";
        $query .= "        AND W1.grade    = W2.grade ";
        $query .= "        AND W1.hr_class = W2.hr_class ";
        $query .= " WHERE W1.year       = '".CTRL_YEAR."' ";
        $query .= "   AND W2.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "   AND W1.grade      = '".$select_grade."' ";
        $query .= "   AND W1.course_seq = '".$course_seq."' ";
        $query .= " ORDER BY course_seq ";
        return $query;
    }

    //グループクラス(クラス一覧)
    function getClassQuery($select_grade, $course_seq)
    {
        $query .= " SELECT grade, hr_class, hr_nameabbv ";
        $query .= "   FROM schreg_regd_hdat ";
        $query .= "  WHERE year     = '".CTRL_YEAR."' ";
        $query .= "    AND semester = '".CTRL_SEMESTER."' ";
        $query .= "    AND grade    = '".$select_grade."' ";
        $query .= "    AND hr_class NOT IN (SELECT hr_class ";
        $query .= "                           FROM course_group_dat ";
        $query .= "                          WHERE year       = '".CTRL_YEAR."' ";
        $query .= "                            AND grade      = '".$select_grade."' ) ";
        return $query;
    }

    //前年度コピー確認
    function selectYearQuery($year, $table)
    {
        return "SELECT COUNT(*) FROM $table WHERE year='$year'";
    }
    //前年度のコピー
    function &getCopyYearQuery($year)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO course_group_dat( ";
        $query .= "                             year, ";
        $query .= "                             grade, ";
        $query .= "                             hr_class, ";
        $query .= "                             coursecode, ";
        $query .= "                             course_seq, ";
        $query .= "                             group_name, ";
        $query .= "                             registercd, ";
        $query .= "                             updated ";
        $query .= "                             )";
        $query .= "            SELECT ";
        $query .= "                 '$year',";
        $query .= "                 grade, ";
        $query .= "                 hr_class, ";
        $query .= "                 coursecode, ";
        $query .= "                 course_seq, ";
        $query .= "                 group_name, ";
        $query .= "                 '".STAFFCD."', ";
        $query .= "                 sysdate() ";
        $query .= "              FROM course_group_dat ";
        $query .= "             WHERE year='" . ($year - 1) . "' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //追加処理
    function &getInsertQuery($fields)
    {
        $selectvalue = explode(",", $fields["SELECTVALUE"]);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($selectvalue); $i++)
        {
            $data["YEAR"][TEXT]         = $fields["YEAR"];
            $data["GRADE"][TEXT]        = $fields["GRADE"];
            $data["HR_CLASS"][TEXT]     = $selectvalue[$i];
            $data["COURSECODE"][TEXT]   = "0000";
            $data["COURSE_SEQ"][TEXT]   = $fields["COURSE_SEQ"];
            $data["GROUP_NAME"][TEXT]   = $fields["GROUP_NAME"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "course_group_dat");
            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);

        return;
    }

    //削除処理
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM course_group_dat ";
        $query .= "       WHERE year       = '".$fields["YEAR"]."' ";
        $query .= "         AND grade      = '".$fields["GRADE"]."' ";
        $query .= "         AND course_seq = '".$fields["COURSE_SEQ"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //更新時の重複チェック(グループコードの重複チェック)
    function IsExistingCd($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM course_group_dat ";
        $query .= "  WHERE year       = '".$fields["YEAR"]."' ";
        $query .= "    AND grade      = '".$fields["GRADE"]."' ";
        $query .= "    AND course_seq = '".$fields["COURSE_SEQ"]."' ";
        $flag = $db->getOne($query);

        Query::dbCheckIn($db);
        return $flag;
    }

    //更新時の重複チェック(グループクラスの重複チェック)
    function IsExistingCl($fields)
    {
        $selectvalue = explode(",", $fields["SELECTVALUE"]);

        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($selectvalue); $i++)
        {
            $query  = " SELECT COUNT(*) ";
            $query .= "   FROM course_group_dat ";
            $query .= "  WHERE year       = '".$fields["YEAR"]."' ";
            $query .= "    AND grade      = '".$fields["GRADE"]."' ";
            $query .= "    AND hr_class   = '".$selectvalue[$i]."' ";
            $flag = $db->getOne($query);
            if ($flag) {
                break;
            }
        }
        Query::dbCheckIn($db);
        return $flag;
    }

}
?>
