<?php

require_once('for_php7.php');

class knja100Query extends Query {

    //学校区分を取得
    function getSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "     schooldiv ";
        $query .= " FROM ";
        $query .= "     school_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".CTRL_YEAR."' ";
        return $query;
    }

    //処理年度学期を取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= " SEMESTER, ";
        $query .= " SEMESTERNAME ";
        $query .= " FROM ";
        $query .= " SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".CTRL_YEAR."' AND ";
        $query .= " NOT SEMESTER ='9' ";
        return $query;
    }

    //処理年度のMAX学期とMIN学期を取得
    function getSemester_Range()
    {
        $query  = " SELECT ";
        $query .= " count(SEMESTER) as MAX_SEMESTER ";
        $query .= " FROM ";
        $query .= " SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".CTRL_YEAR."' AND ";
        $query .= " NOT SEMESTER = '9' ";
        return $query;
    }

    //学籍在籍データを削除
    function &getDeleteQuery($model)
    {
        $query  = " DELETE FROM SCHREG_REGD_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$model->semester."' ";
        return $query;
    }

    //クラス編成データから学籍在籍データへインサート
    function &getInsertQuery($model)
    {
/***
        $query  = " INSERT INTO SCHREG_REGD_DAT (SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO, ";
        $query .= " ANNUAL,SEAT_ROW,SEAT_COL,COURSECD,MAJORCD,COURSECODE,REGISTERCD,UPDATED) ";
        $query .= " SELECT ";
        $query .= " SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO,GRADE, ";
        $query .= " conv_col_row(ATTENDNO,2),conv_col_row(ATTENDNO,1),COURSECD,MAJORCD,COURSECODE, ";
        $query .= " '".STAFFCD."', ";
        $query .= " sysdate() ";
        $query .= " FROM CLASS_FORMATION_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$model->semester."' ";
***/
        $query  = " INSERT INTO SCHREG_REGD_DAT ( ";
        $query .= " SCHREGNO, ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " GRADE, ";
        $query .= " HR_CLASS, ";
        $query .= " ATTENDNO, ";
        $query .= " ANNUAL, ";
        $query .= " SEAT_ROW, ";
        $query .= " SEAT_COL, ";
        $query .= " COURSECD, ";
        $query .= " MAJORCD, ";
        $query .= " COURSECODE, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        //学年制：クラス編成データの「学年（GRADE）」
        if ($model->schooldiv == 0) {
            $query .= " T1.GRADE  as ANNUAL, ";
        //単位制：作成する学期（次学期）の前の学期（つまり処理学期）の学籍在籍データの「年次（ANNUAL）」
        } elseif ($model->schooldiv == 1) {
            $query .= " T2.ANNUAL as ANNUAL, ";
        }
        $query .= "     conv_col_row(T1.ATTENDNO,2), ";
        $query .= "     conv_col_row(T1.ATTENDNO,1), ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                 AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "                                 AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' ";
        return $query;

    }

   //コントロールマスタの学籍処理年度と処理学期を更新  
   function update_Control($model)
   {
        $query  = " UPDATE ";
        $query .= " CONTROL_MST ";
        $query .= " SET ";
        $query .= " CTRL_SEMESTER  = '".$model->semester."' ";
        $query .= " WHERE ";
        $query .= " CTRL_YEAR = '".CTRL_YEAR."' AND ";
        $query .= " CTRL_SEMESTER = '".CTRL_SEMESTER."' ";
        return $query;
   }

    function &getUpdateQuery($model) 
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
            //学籍在籍データを削除
            $db->query(knja100Query::getDeleteQuery($model));
            //クラス編成データから学籍在籍データへインサート
            $db->query(knja100Query::getInsertQuery($model));
            //指定した学期にコントロールマスタを更新	2005/10/06 m-yama 処理をコメント化
//            $db->query(knja100Query::update_Control($model));

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //クラス編成データのHR_CLASS,ATTENDNO,COURSECODEが一項目でもnullがあるデータを取得
    function getEx_Class_Formation($model)
    {
        $query = " SELECT ";
        $query .= " COUNT(*) ";
        $query .= " FROM ";
        $query .= " CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= " YEAR = '".CTRL_YEAR."' AND ";
        $query .= " SEMESTER = '".$model->next_semester."' AND ";
        $query .= " (HR_CLASS is null or ATTENDNO is null or COURSECODE is null ) ";
        //echo $query;
        return $query;
    }

    //クラス編成データの存在チェック
    function getExistsClassFormation($model)
    {
        $query = " SELECT ";
        $query .= " COUNT(*) ";
        $query .= " FROM ";
        $query .= " CLASS_FORMATION_DAT ";
        $query .= " WHERE ";
        $query .= " YEAR = '".CTRL_YEAR."' AND ";
        $query .= " SEMESTER = '".$model->semester."' ";
        return $query;
    }

}

?>