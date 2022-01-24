<?php

require_once('for_php7.php');

class knjxattend3Query extends Query 
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
        } else {
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
        } else {
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
        } else {
            $t = "SCHREG_ATTENDREC_DAT";
        }
        return " SELECT DISTINCT YEAR FROM ".$t." WHERE SCHREGNO = '".$model->schregno."' ";
    }

    //SCHREG_ATTENDREC_DAT テーブル値取得
    function getSchregAttendrecDat_Val($model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        } else {
            $t = "SCHREG_ATTENDREC_DAT";
        }
        return "SELECT * FROM ".$t." WHERE YEAR='".$model->grade_combo."' AND SCHREGNO='".$model->schregno."' ";
    }

    function getTrans_Annual($model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        } else {
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

    //SCHREG_ATTENDREC_DAT自身のデータを削除
    function schregAttendrecDat_Delete($a_schoolcd, $model)
    {
        if ($model->mode == "grd"){
            $t = "GRD_ATTENDREC_DAT";
        } else {
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
        } else {
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
        } else {
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

        //(実行履歴)実行日付・時間を取得
        $rirekiFlg = "";
        if ($model->prgid == 'KNJE041') {
            $rirekiFlg = "ON";
            $calcRow = $db->getRow(knjxattend3Query::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        }

        //前校(コード:1)と本校(コード:0)の２回ループ
        for($i=0; $i<2; $i++) {
            //(実行履歴)変更前データ追加
            if ($rirekiFlg == "ON") {
                $db->query(knjxattend3Query::getInsertRireki($calcRow, "BEF", $model, $i));
            }
            //SCHREG_ATTENDREC_DAT内の自身のデータを削除
            $db->query(knjxattend3Query::schregAttendrecDat_Delete($i,$model));

            //年次データが入力されている場合は更新処理
            if ( ( $model->field["ANNUAL"][$i] != null)  && (is_numeric($model->field["ANNUAL"][$i])) != "" )
            {
                //SCHREG_ATTENDREC_DATを更新
                $db->query(knjxattend3Query::schregAttendrecDat_Query($model,$i,"insert"));
                //(実行履歴)変更後データ追加
                if ($rirekiFlg == "ON") {
                    $db->query(knjxattend3Query::getInsertRireki($calcRow, "AFT", $model, $i));
                }
                //ANNUAL値を確認
                if(!isset($annual)) $annual = $model->field["ANNUAL"][$i];
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

    //履歴一覧
    function getListRireki($model) {
        if ($model->mode == "grd") {
            $t = "GRD_ATTENDREC_EXEC_HIST_DAT";
        } else {
            $t = "SCHREG_ATTENDREC_EXEC_HIST_DAT";
        }

        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "   {$t} T1 ";
        $query .= "   LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.REGISTERCD ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "   T1.CALC_DATE DESC, ";
        $query .= "   T1.CALC_TIME DESC, ";
        $query .= "   T1.YEAR ";

//        echo $query;
        return $query;
    }
    //(実行履歴)実行日付・時間を取得
    function getCalcDateTime() {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //(実行履歴)変更前・変更後データ追加・・・SQL条件はgetDeleteQuery()と同じにすること
    function getInsertRireki($calcRow, $befAftDiv, $model, $schoolcd) {
        if ($model->mode == "grd") {
            $t1 = "GRD_ATTENDREC_DAT";
            $t2 = "GRD_ATTENDREC_EXEC_HIST_DAT";
        } else {
            $t1 = "SCHREG_ATTENDREC_DAT";
            $t2 = "SCHREG_ATTENDREC_EXEC_HIST_DAT";
        }

        $query  = " INSERT INTO {$t2}( ";
        $query .= "     CALC_DATE, ";
        $query .= "     CALC_TIME, ";
        $query .= "     BEF_AFT_DIV, ";
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     SUMDATE, ";
        $query .= "     CLASSDAYS, ";
        $query .= "     OFFDAYS, ";
        $query .= "     ABSENT, ";
        $query .= "     SUSPEND, ";
        $query .= "     MOURNING, ";
        $query .= "     ABROAD, ";
        $query .= "     REQUIREPRESENT, ";
        $query .= "     SICK, ";
        $query .= "     ACCIDENTNOTICE, ";
        $query .= "     NOACCIDENTNOTICE, ";
        $query .= "     PRESENT, ";
        $query .= "     CMD_DIV, ";
        $query .= "     UPD_PRGID, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$calcRow["CALC_DATE"]."' AS CALC_DATE, ";//実行日付
        $query .= "     '".$calcRow["CALC_TIME"]."' AS CALC_TIME, ";//実行時間
        if ($befAftDiv == "BEF") {
            $query .= "     '1' AS BEF_AFT_DIV, ";                  //1:変更前データ 2:変更後データ
        } else if ($befAftDiv == "AFT") {
            $query .= "     '2' AS BEF_AFT_DIV, ";                  //1:変更前データ 2:変更後データ
        }
        $query .= "     SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL, ";
        $query .= "     SUMDATE, ";
        $query .= "     CLASSDAYS, ";
        $query .= "     OFFDAYS, ";
        $query .= "     ABSENT, ";
        $query .= "     SUSPEND, ";
        $query .= "     MOURNING, ";
        $query .= "     ABROAD, ";
        $query .= "     REQUIREPRESENT, ";
        $query .= "     SICK, ";
        $query .= "     ACCIDENTNOTICE, ";
        $query .= "     NOACCIDENTNOTICE, ";
        $query .= "     PRESENT, ";
        $query .= "     '1' AS CMD_DIV, ";                          //変更区分　1:更新ボタン 2:削除ボタン
        $query .= "     '".$model->prgid."' AS UPD_PRGID, ";        //更新PRGID
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";             //更新者
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     {$t1} ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".$schoolcd."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND YEAR     = '".$model->grade_combo."' ";

        return $query;
    }
}
?>
