<?php
class knjvexpQuery extends Query {
    //年度コンボフラグを取得（名称マスタ）
    function getComboFlg()
    {
        $db     = Query::dbCheckOut();
        $query  = "SELECT NAMECD2, ";
        $query .= "       NAME1 as PRGID, ";
        $query .= "       NAMESPARE1 as COMBO_FLG ";
        $query .= "FROM   NAME_MST ";
        $query .= "WHERE  NAMECD1 = 'A022' ";
        $ret_val = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $ret_val[$row["PRGID"]] = $row["COMBO_FLG"];
        }
        $result->free();
        Query::dbCheckIn($db);
        return $ret_val;
    }
    //年度コンボ（学期マスタ）
    function getYearSemester($exp_year)
    {
        $pre_year = CTRL_YEAR - 1;
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER, ";
        $query .= "   SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "       '$pre_year' <= YEAR AND SEMESTER <> '9' ";
        if ($exp_year != "off") 
            $query .= "   AND YEAR || '-' || SEMESTER = '" .$exp_year. "' ";
        $query .= " ORDER BY ";
        $query .= "   YEAR, SEMESTER ";

        return $query;
    }
   //学年と組
   function GetHr_Class($model)
   {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1 ";
        $query .= "  LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR  = T1.YEAR ";
        $query .= "                               AND L1.GRADE = T1.GRADE ";
        $query .= "  ,STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " ((T1.TR_CD1 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";           //2005/06/29
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";        //2005/06/29
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') ";      //2005/06/29
            $query .= " OR (L1.GRADE IN ";
            $query .= "       (SELECT ";
            $query .= "            FIELD2 ";
            $query .= "        FROM ";
            $query .= "            STAFF_DETAIL_MST ST ";
            $query .= "        WHERE ";
            $query .= "            T1.YEAR = ST.YEAR ";
            $query .= "            AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "            AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "            AND ST.FIELD1 = '0200')) ";
            $query .= "   ) AND ";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' ";
        
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "  AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        return $query;
    }
    //コース取得
    function GetCourseCode()
    {
        $query = " SELECT ";
        $query .= "   COURSECODE, ";
        $query .= "   COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }
    //卒業年度
    function GetGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";

        return $query;
    }
    function GetStudents($model)
    {
        if ($model->cmd == "list" && $model->grade){
            $grade = $model->grade;
        }else if ($model->cmd == "list" && !$model->grade){
            $grade = "";
        }else if ($model->cmd == "search" && $model->search["GRADE"]){
            $grade = $model->search["GRADE"];
        }

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   T2.GRD_SEMESTER, ";
            $query .= "   T2.GRD_GRADE, ";
            $query .= "   T2.GRD_HR_CLASS, ";
            $query .= "   T2.GRD_ATTENDNO, ";
        }
        $query .= "   FISCALYEAR(T2.ENT_DATE) AS ENT_YEAR, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2 ";
        $query .= " FROM ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   GRD_REGD_DAT T1, ";
            $query .= "   GRD_BASE_MST T2, ";
            $query .= "   GRD_REGD_HDAT T3 ";
        }else if($model->mode == "ungrd"){ //在籍
            $query .= "   SCHREG_REGD_DAT T1, ";
            $query .= "   SCHREG_BASE_MST T2,";
            $query .= "   SCHREG_REGD_HDAT T3 ";
        }
        if ($model->search["DATE"]){
            $query .= "   ,NURSEOFF_VISITREC_DAT T4 ";
        }
        $query .= " WHERE ";
        if (isset($grade) && $model->mode == "ungrd" ){
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '" .$grade."' AND ";
        }
        if ($model->mode == "grd" && $model->search["HR_CLASS"]){
            $query .= "   T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if($model->mode == "ungrd"){  //在籍生
            $query .= "   T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' AND ";
        }else if($model->mode == "grd" && $model->search["GRD_YEAR"]){  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]){
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["NAME"]){
            $query .= "   T2.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]){
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'","''",$model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]){
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]){
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'","''",$model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["SEX"]){
            $query .= "   T2.SEX = '" .$model->search["SEX"]. "' AND ";
        }
        if ($model->search["DATE"]){
            $query .= "   T4.VISIT_DATE = '" .$model->search["DATE"]. "' AND ";
            $query .= "   T1.SCHREGNO = T4.SCHREGNO AND ";
        }
        $query .= "   T1.YEAR     = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE    = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";

        if($model->mode == "grd"){  //卒業生
            $query .= "   T1.YEAR     = FISCALYEAR(T2.GRD_DATE) AND ";
            $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
            $query .= "   T1.GRADE    = T2.GRD_GRADE AND ";
            $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";
        }

        if ($model->mode == "ungrd" && $model->programid == KNJD105B) {
            $query .= "   T1.GRADE NOT IN (SELECT S1.GRADE ";
            $query .= "                    FROM SCHREG_REGD_HDAT S1, ";
            $query .= "                         NAME_MST S2 ";
            $query .= "                    WHERE S1.YEAR || '-' || S1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "                          S2.NAMECD1 = 'Z010' AND ";
            $query .= "                          S2.NAMECD2 = '00' AND ";
            $query .= "                          S2.NAME1 = 'CHIBEN' AND ";
            $query .= "                          S1.GRADE > '06') AND ";
        }

        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";

        //中等教育校フラグ NO001
        if ($model->ses_flg[$model->programid] == 1 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE <= '03' ";
        }
        if ($model->ses_flg[$model->programid] == 2 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE > '03' ";
        }

        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO") {
            $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) ";
        } else {
            $query .= "   T1.ATTENDNO ";
        }

        #echo $query;
        return $query;
   }
    //学校区分名称 NO001
    function GetSchooldiv()
    {
        $db     = Query::dbCheckOut();
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
    //検索結果
    function getStudent($model, $field, $mode="")
    {
        if($mode != ""){
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ( ";
        }
        $query .= "     SELECT DISTINCT ";
        $query .= "         KNJID, ";
        $query .= "         t1.SCHREGNO, ";
        $query .= "         t2.NAME, ";
        $query .= "         t2.NAME_KANA, ";
        $query .= "         t6.GRADE_NAME as GRADE, ";
        $query .= "         t6.HR_CLASS_NAME as HR_CLASS, ";
        $query .= "         t3.ATTENDNO ";
        $query .= "     FROM ";
        $query .= "         PV_SCHREG_MST t1  ";
        $query .= "         left join (SELECT ";
        $query .= "                         a1.*, ";
        $query .= "                         a2.GRADE_CD ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT a1  ";
        $query .= "                         left join SCHREG_REGD_GDAT a2 on a1.GRADE = a2.GRADE and a1.YEAR = a2.YEAR ";
        $query .= "                     WHERE ";
        if($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""){
            $query .= "                         a2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                     AND ";
        }
        $query .= "                         a1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     ) t3 on t1.SCHREGNO = t3.SCHREGNO ";
        $query .= "         left join SCHREG_BASE_MST t2 on t3.SCHREGNO = t2.SCHREGNO  ";
        $query .= "         left join (SELECT ";
        $query .= "                         YEAR, ";
        $query .= "                         GRADE, ";
        $query .= "                         SEMESTER, ";
        $query .= "                         HR_CLASS, ";
        $query .= "                         to_single_byte(GRADE_NAME) as GRADE_NAME, ";
        $query .= "                         to_single_byte(HR_CLASS_NAME1) as HR_CLASS_NAME ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_HDAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '".CTRL_YEAR."' ";
        $query .= "                     ) t6 on t3.YEAR = t6.YEAR and t3.GRADE = t6.GRADE and t3.HR_CLASS = t6.HR_CLASS and t3.SEMESTER = t6.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         t2.SCHREGNO IS NOT NULL ";
        $query .= "     AND ";
        $query .= "         t3.YEAR = '".CTRL_YEAR."'  ";
        $query .= "     AND ";
        $query .= "         t3.SEMESTER = '".CTRL_SEMESTER."'  ";
        if($field["GAKUSEKINO"] != ""){
            $query .= "     AND ";
            $query .= "         t1.SCHREGNO = '".$field["GAKUSEKINO"]."' ";
        }
        if($field["NAME"] != ""){
            $query .= "     AND ";
            $query .= "         t2.NAME like '%".$field["NAME"]."%' ";
        }
        if($field["NAME_KANA"] != ""){
            $query .= "     AND ";
            $query .= "         t2.NAME_KANA like '%".$field["NAME_KANA"]."%' ";
        }
        if($field["GRADE"] != ""){
            $grade = explode("-", $field["GRADE"]);
            $query .= "     AND ";
            $query .= "         t3.GRADE = '".$grade[0]."' ";
            $query .= "     AND ";
            $query .= "         t3.HR_CLASS = '".$grade[1]."' ";
        }
        if($field["COURSE"] != ""){
            $query .= "     AND ";
            $query .= "         t3.COURSECODE = '".$field["COURSE"]."' ";
        }
        $query .= "     ORDER BY ";
        $query .= "         t6.GRADE_NAME, ";
        $query .= "         t6.HR_CLASS_NAME, ";
        $query .= "         t3.ATTENDNO ";
        if($mode != ""){
            $query .= "     ) ";
        }
        
        return $query;
    }
}
?>
