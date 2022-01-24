<?php

require_once('for_php7.php');

class knjxattendQuery extends Query 
{
    function getZ010($nameCd2) {
        $query = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '{$nameCd2}' ";
        return $query;
    }

    //生徒情報取得
    function getStudentInfo($model)
    {
        if ($model->mode == "grd"){
            $year = $model->grd_year;
            $semester = $model->grd_semester;
        }else{
            $year = CTRL_YEAR;
            $semester = CTRL_SEMESTER;
        }
        
        $query = " SELECT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T2.NAME, ";
        $query .= "   T3.HR_NAME ";
        $query .= " FROM ";
        if ($model->mode == "grd"){
            $query .= "   GRD_REGD_DAT T1 LEFT JOIN GRD_BASE_MST T2  ";
            $query .= "   ON T1.SCHREGNO = T2.SCHREGNO  ";
            $query .= "   LEFT JOIN GRD_REGD_HDAT T3  ";
        }else{
            $query .= "   SCHREG_REGD_DAT T1 LEFT JOIN SCHREG_BASE_MST T2  ";
            $query .= "   ON T1.SCHREGNO = T2.SCHREGNO  ";
            $query .= "   LEFT JOIN SCHREG_REGD_HDAT T3  ";
        }
        $query .= "   ON T1.YEAR = T3.YEAR AND  ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND  ";
        $query .= "   T1.GRADE = T3.GRADE AND  ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "   T1.YEAR = '".$year."' AND ";
        $query .= "   T1.SEMESTER = '".$semester."' ";

        return $query;
    }

    //年度情報取得
    function getYear($model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        }else{
            $t = "SCHREG_ATTENDREC_DAT";
        }
        return " SELECT DISTINCT YEAR FROM ".$t." WHERE SCHREGNO = '".$model->schregno."' ";
    }

    //SCHREG_ATTENDREC_DAT テーブル値取得
    function getSchregAttendrecDat_Val($model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        }else{
            $t = "SCHREG_ATTENDREC_DAT";
        }
        return "SELECT * FROM ".$t." WHERE YEAR='".$model->grade_combo."' AND SCHREGNO='".$model->schregno."' ";
    }

    //HEXAM_ENTREMARK_DAT テーブル値取得
    function getHexamEntremarkDat_Val($model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_HEXAM_ENTREMARK_DAT";
        }else{
            $t = "HEXAM_ENTREMARK_DAT";
        }
        return " SELECT ATTENDREC_REMARK,UPDATED
                 FROM ".$t." WHERE YEAR='".$model->grade_combo."' AND SCHREGNO='".$model->schregno."' ";
    }


    function getTrans_Annual($model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        }else{
            $t = "SCHREG_ATTENDREC_DAT";
        }
        $query  = " SELECT 
                       T1.ANNUAL, T2.MAX_ANNUAL
                    FROM 
                        SCHREG_REGD_DAT T1 LEFT OUTER JOIN(
                            SELECT 
                                MAX(S2.ANNUAL) AS MAX_ANNUAL, S2.SCHREGNO
                            FROM
                                ".$t." S2
                            WHERE
                                S2.SCHREGNO = '".$model->getValue["SCHREGNO"]."'
                            GROUP BY S2.SCHREGNO
                        )T2 ON T1.SCHREGNO = T2.SCHREGNO
                   WHERE
                   T1.SCHREGNO = '".$model->getValue["SCHREGNO"]."' ";

        return $query;
    }

    function getAttendrec_Hexamremark($model)
    {
        $query = "";
        $query .= "SELECT DISTINCT";
        $query .= "  T2.SCHOOLCD, ";
        $query .= "  T1.SCHREGNO, ";
        $query .= "  T1.YEAR, ";
        $query .= "  T1.GRADE, ";
        $query .= "  T2.CLASSDAYS, ";
        $query .= "  T2.OFFDAYS, ";
        $query .= "  T2.SUSPEND, ";
        $query .= "  T2.MOURNING, ";
        $query .= "  T2.ABROAD, ";
        $query .= "  T2.REQUIREPRESENT, ";
        $query .= "  T2.SICK, ";
        $query .= "  T2.ACCIDENTNOTICE, ";
        $query .= "  T2.NOACCIDENTNOTICE, ";
        $query .= "  T2.PRESENT, ";
        $query .= "  T2.ATTENDREC_UPDATED, ";
        $query .= "  T2.ATTENDREC_REMARK, ";
        $query .= "  T2.HEXAMREMARK_UPDATED ";
        $query .= "FROM ";
        if ($model->mode == "grd"){
            $query .= "  GRD_REGD_DAT T1 LEFT OUTER JOIN(SELECT ";
        }else{
            $query .= "  SCHREG_REGD_DAT T1 LEFT OUTER JOIN(SELECT ";
        }
        $query .= "                                      S1.SCHOOLCD, ";
        $query .= "                                      S1.SCHREGNO, ";
        $query .= "                                      S1.YEAR, ";
        $query .= "                                      S1.ANNUAL, ";
        $query .= "                                      COALESCE(S1.CLASSDAYS,0) AS CLASSDAYS, ";
        $query .= "                                      COALESCE(S1.OFFDAYS,0) AS OFFDAYS, ";
        $query .= "                                      COALESCE(S1.SUSPEND,0) AS SUSPEND, ";
        $query .= "                                      COALESCE(S1.MOURNING,0) AS MOURNING, ";
        $query .= "                                      COALESCE(S1.ABROAD,0) AS ABROAD, ";
        $query .= "                                      COALESCE(S1.REQUIREPRESENT,0) AS REQUIREPRESENT, ";
        $query .= "                                      COALESCE(S1.SICK,0) AS SICK, ";
        $query .= "                                      COALESCE(S1.ACCIDENTNOTICE,0) AS ACCIDENTNOTICE, ";
        $query .= "                                      COALESCE(S1.NOACCIDENTNOTICE,0) AS NOACCIDENTNOTICE, ";
        $query .= "                                      COALESCE(S1.PRESENT,0) AS PRESENT, ";
        $query .= "                                      S1.UPDATED AS ATTENDREC_UPDATED, ";
        $query .= "                                      H1.ATTENDREC_REMARK, ";
        $query .= "                                      H1.UPDATED AS HEXAMREMARK_UPDATED ";
        $query .= "                                    FROM ";
        if ($model->mode == "grd"){
            $query .= "                                      GRD_ATTENDREC_DAT S1 LEFT OUTER JOIN GRD_HEXAM_ENTREMARK_DAT H1 ON S1.SCHREGNO = H1.SCHREGNO AND S1.YEAR = H1.YEAR ";
        }else{
            $query .= "                                      SCHREG_ATTENDREC_DAT S1 LEFT OUTER JOIN HEXAM_ENTREMARK_DAT H1 ON S1.SCHREGNO = H1.SCHREGNO AND S1.YEAR = H1.YEAR ";
        }
        $query .= "                                    ) T2 ON T1.SCHREGNO = T2.SCHREGNO AND T1.YEAR = T2.YEAR  ";
        $query .= "WHERE ";
        $query .= "  T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "  T2.ANNUAL = '".$model->defalt_annual."' AND ";
        $query .= "  T1.SEMESTER = (SELECT ";
        $query .= "                  MAX(SEMESTER) ";
        $query .= "                FROM ";
        $query .= "                  SCHREG_REGD_DAT ";
        $query .= "                WHERE ";
        $query .= "                  T1.SCHREGNO = SCHREGNO AND ";
        $query .= "                  T1.YEAR = YEAR ";
        $query .= "                ) ";
        $query .= "ORDER BY ";
        $query .= "  GRADE ";

        //echo "this is probrem <BR>";

        return $query;
    }

    //SCHREG_ATTENDREC_DAT自身のデータを削除
    function schregAttendrecDat_Delete($a_schoolcd, $model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        }else{
            $t = "SCHREG_ATTENDREC_DAT";
        }
        $query = " DELETE 
                   FROM
                     ".$t."
                   WHERE
                     SCHOOLCD = '".$a_schoolcd."'
                     AND SCHREGNO = '".$model->schregno."'
                     AND YEAR     = '".$model->grade_combo."' ";

        return $query;
    }

    //SCHREG_ATTENDREC_DATのデータ更新
    function schregAttendrecDat_Query(&$model,$schoolcd, $type)
    {

        //出席しなければならない日数
        $requirepresent = ( $model->field["CLASSDAYS"][$schoolcd] - $model->field["OFFDAYS"][$schoolcd])  - 
               ( $model->field["SUSPEND"][$schoolcd] + $model->field["MOURNING"][$schoolcd] + 
                 $model->field["ABROAD"][$schoolcd]);

        //出席日数
        $present = $requirepresent - ($model->field["SICK"][$schoolcd] + 
               $model->field["ACCIDENTNOTICE"][$schoolcd] + $model->field["NOACCIDENTNOTICE"][$schoolcd]);

        $data = array();
        $data["SCHOOLCD"][TEXT]    = $schoolcd                 ;            //本校区分
        $data["YEAR"][TEXT]        = $model->grade_combo;                   //年度
        $data["SCHREGNO"][TEXT]    = $model->schregno;                      //学籍番号
        if(strlen($model->field["ANNUAL"][$schoolcd])<="1"){
                $data["ANNUAL"][TEXT]      = sprintf("%02d",$model->field["ANNUAL"][$schoolcd]);           //年次
        }else{
                $data["ANNUAL"][TEXT]      = $model->field["ANNUAL"][$schoolcd];           //年次
        }
        $data["CLASSDAYS"][NUMBER]   = $model->field["CLASSDAYS"][$schoolcd];      //授業日数
        $data["OFFDAYS"][NUMBER]     = $model->field["OFFDAYS"][$schoolcd];        //休学日数
        $data["SUSPEND"][NUMBER]     = $model->field["SUSPEND"][$schoolcd];        //出停日数
        $data["MOURNING"][NUMBER]    = $model->field["MOURNING"][$schoolcd];       //忌引日数
        $data["ABROAD"][NUMBER]      = $model->field["ABROAD"][$schoolcd];                 //留学日数
        $data["REQUIREPRESENT"][NUMBER]   = $requirepresent;                               //要出席日数
        $data["SICK"][NUMBER]             = $model->field["SICK"][$schoolcd];              //病欠日数
        $data["ACCIDENTNOTICE"][NUMBER]   = $model->field["ACCIDENTNOTICE"][$schoolcd];    //事故欠届日数
        $data["NOACCIDENTNOTICE"][NUMBER] = $model->field["NOACCIDENTNOTICE"][$schoolcd];  //事故欠無日数
        if ($model->isKyotoTushin == "1") {
            $data["PRESENT"][NUMBER]  = $model->field["PRESENT"][$schoolcd];               //出席日数(京都)
        } else {
            $data["PRESENT"][NUMBER]  = $present;                                          //出席日数
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        }else{
            $t = "SCHREG_ATTENDREC_DAT";
        }
        if( $type == "insert" ){
            $query = Query::insertSQL($data, $t);
        } else {
            $where = " WHERE 
                             YEAR = '".$model->grade_combo."' 
                         AND SCHOOLCD = '".($schoolcd)."' 
                         AND SCHREGNO = '".$model->schregno."' ";

            $query = Query::updateSQL($data, $t,$where);
        }

        return $query;
    }

    //HEXAM_ENTREMARK_DATの更新
    function hexamEntremarkDatQuery(&$model,$annual,$type)
    {
        $data = array();
        $data["YEAR"][TEXT]        = $model->grade_combo;                     //年度
        $data["SCHREGNO"][TEXT]    = $model->schregno;                        //学籍番号
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if ($model->mode == "grd"){
            $t = "GRD_HEXAM_ENTREMARK_DAT";
        }else{
            $t = "HEXAM_ENTREMARK_DAT";
        }

        if ( $type == "insert") 
        {
            $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];  //備考
            $data["ANNUAL"][TEXT] = sprintf("%02d",$annual);
            $query = Query::insertSQL($data, "HEXAM_ENTREMARK_DAT");
        } else {
            if($annual != ""){
                $data["ANNUAL"][TEXT] = sprintf("%02d",$annual);
                $data["ATTENDREC_REMARK"][TEXT] = $model->field["ATTENDREC_REMARK"];
            }else{
                $data["ATTENDREC_REMARK"][TEXT] = "" ;
            }
            $where = " WHERE YEAR     = '".$model->grade_combo ."' 
                         AND SCHREGNO = '".$model->schregno."' ";
            $query = Query::updateSQL($data, "HEXAM_ENTREMARK_DAT",  $where);
        }
        return $query;
    }

    // 処理内容 #######################
    #
    # 1.年次データ,その他のデータが入力された場合(Insert or Update)
    # 2.年次データのみ入力された場合(Insert or Update)
    # 3.その他のデータのみの入力の場合(エラーメッセージ表示)
    # 4.データが入力されていない場合(delete)
    #
    # 備考については本校のSchoolcdを優先的にHEXAM_ENTREMARK_DATに入力する。
    # (本校が未入力の場合は前校のSchoolcdを入力)
    #
    #####################################################################

    function &getUpdateQuery($model)
    {
        //初期化
        unset($annual);

        //DB OPEN
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //HEXAM_ENTREMARK_DATにデータが存在するか？
        $query = knjxattendQuery::getHexamEntremarkDat_Val($model);
        $row = array();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //前校(コード:1)と本校(コード:0)の２回ループ
        for($i=0; $i<2; $i++) {
            //SCHREG_ATTENDREC_DAT内の自身のデータを削除
            $db->query(knjxattendQuery::schregAttendrecDat_Delete($i,$model));

            //年次データが入力されている場合は更新処理
            if ( ( $model->field["ANNUAL"][$i] != null)  && (is_numeric($model->field["ANNUAL"][$i])) != "" )
            {
                //SCHREG_ATTENDREC_DATを更新
                $db->query(knjxattendQuery::schregAttendrecDat_Query($model,$i,"insert"));
                //ANNUAL値を確認
                if(!isset($annual)) $annual = $model->field["ANNUAL"][$i];
            }
        }

        if ($model->programid == "KNJE010A") {
            //備考項目
            if($model->memo_flg != "unset"){
                //ANNUAL値を確認
                if(isset($annual)){
                    //HEXAM_ENTREMARK_DAT更新分岐
                    $HexamEntremarkDat = (is_array($row))? "update" : "insert" ;
                    //echo "Hexam process is ".$HexamEntremarkDat."<BR>";
                    $query = knjxattendQuery::hexamEntremarkDatQuery($model,$annual,$HexamEntremarkDat);
                    $db->query($query);
                } else {
                    $query = knjxattendQuery::hexamEntremarkDatQuery($model,$annual,$HexamEntremarkDat);
                    $db->query($query);
                }
            }
        }

        $db->commit();            // コミット
        Query::dbCheckIn($db);    // DB CLOSE
        return true;
    }

    //署名チェック
    function getOpinionsWk($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->grade_combo."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        return $query;
    }
}
?>
