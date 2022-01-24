<?php

require_once('for_php7.php');

class knjx_hexam_remarkQuery extends Query {

    //学校名取得の取得(テストコンボで使う)
    function getSchoolName($JH="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        if($JH){
            $query .= "     AND NAMESPARE2 IS NOT NULL ";
        }

        return $query;
    }

    function selectQuery($model) {
        $query  =" SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->annual["YEAR"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = S1.YEAR AND ";
        $query .= "                         T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //備考一括更新画面用の生徒一覧
    function GetStudent($model)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T2.name_show ,";
        $query .= "      T1.schregno ,";
        $query .= "      T1.attendno";
        $query .= "  FROM";
        $query .= "      schreg_regd_dat T1";
        $query .= "      LEFT OUTER JOIN schreg_base_mst T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "      T1.year     = '".CTRL_YEAR."' AND";
        $query .= "      T1.semester = '".CTRL_SEMESTER."' AND";
        $query .= "      T1.grade    = '".$model->grade."' AND";
        $query .= "      T1.hr_class = '".$model->hrClass."' ";
        $query .= "  ORDER BY";
        $query .= "      T1.attendno";

        return $query;
    }
    
    //年組名取得
    function getHR_Name($model)
    {
        $query  = "SELECT hr_name FROM schreg_regd_hdat";
        $query .= " WHERE grade    = '".$model->grade."' ";
        $query .= "   AND hr_class = '".$model->hrClass."' ";
        $query .= "   AND year     = '".CTRL_YEAR."' ";
        $query .= "   AND semester = '".CTRL_SEMESTER."' ";
        return $query;
    }
    
    /* 備考一括更新処理 */
    function update(&$db, $model) {

        //対象テーブル
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_HDAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_HDAT";
        }

        $array = explode(",", $model->data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++)
        {
            //取得
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     {$tableName} ";
            $query .= " WHERE ";
            $query .= "       SCHREGNO  = '".$array[$i]."' ";
            $row_data = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if ($model->data["check"]["RCHECK0"] == "1"){
                $row_data["REMARK"]         = isset($model->data["field"]["NO_COMMENTS"]) ? $model->no_comments_label : $model->data["field"]["REMARK"];
            }
            if ($model->data["check"]["RCHECK1"] == "1"){
                if ($model->Properties["useHexamRemark2Flg"] == 1) {
                    $row_data["REMARK2"]    = $model->data["field"]["REMARK2"];
                }
            }
            if ($model->data["check"]["RCHECK2"] == "1"){
                $row_data["COMMENTEX_A_CD"] = $model->data["field"]["COMMENTEX_A_CD"];
            }

            //削除
            $query  = " DELETE FROM ";
            $query .= "     {$tableName} ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$array[$i]."' ";
            $db->query($query);

            //更新
            $data = array();
            $data["SCHREGNO"][TEXT]       = $array[$i];
            $data["DISEASE"][TEXT]        = $row_data["DISEASE"];
            $data["DOC_REMARK"][TEXT]     = $row_data["DOC_REMARK"];
            $data["TR_REMARK"][TEXT]      = $row_data["TR_REMARK"];
            $data["TOTALSTUDYACT"][TEXT]  = $row_data["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]  = $row_data["TOTALSTUDYVAL"];
            $data["REMARK"][TEXT]         = $row_data["REMARK"];
            $data["REMARK2"][TEXT]        = $row_data["REMARK2"];
            $data["COMMENTEX_A_CD"][TEXT] = $row_data["COMMENTEX_A_CD"];
            if ($model->Properties["useTotalstudySlashFlg"] == 1) {
                $data["TOTALSTUDYACT_SLASH_FLG"][TEXT]  = $row_data["TOTALSTUDYACT_SLASH_FLG"];
                $data["TOTALSTUDYVAL_SLASH_FLG"][TEXT]  = $row_data["TOTALSTUDYVAL_SLASH_FLG"];
            }
            $data["REGISTERCD"][TEXT]     = STAFFCD;

            $query = Query::insertSQL($data, $tableName);
            $db->query($query);
        }
    }
    
}
?>
