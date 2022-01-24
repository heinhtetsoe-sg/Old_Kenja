<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");
//ビュー作成用クラス
class knjd130hSubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjd130hindex.php", "", "subform1");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $db = Query::dbCheckOut();

        //校種、学校コード
        $schoolcd = $school_kind = "";
        if ($db->getOne(knjd130hQuery::checkSchoolMst()) > 0) {
            $schoolcd       = sprintf("%012d", SCHOOLCD);
            $school_kind    = $db->getOne(knjd130hQuery::getSchoolKind($model));
        }

        //SCHOOL_MSTの情報を取得。
        $knjSchoolMst = AttendAccumulate::getSchoolMstMap($db, CTRL_YEAR, $schoolcd, $school_kind);

        //出欠データ(出欠データをみる開始日/出欠集計テーブルをみる最終月)
        $isodate = CTRL_YEAR ."-04-01";
        $isomonth = array();
        $query = knjd130hQuery::getAppointedDay($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_isodate = sprintf("%04d-%02d-%02d", $row["YEAR"], $row["MONTH"], $row["APPOINTED_DAY"]);

            if (CTRL_DATE < $tmp_isodate) break;

            $isodate = $tmp_isodate;
            $isomonth[] = $row["MONTH"];
        }
        $result->free();
/***
echo $isodate .">>";
echo CTRL_DATE .">><br>";
echo implode("','", $isomonth) .">>";
echo get_count($isomonth);
***/

        //出欠データ
        $query = knjd130hQuery::getAttend($model, $isodate, implode("','", $isomonth), get_count($isomonth), $knjSchoolMst);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            array_walk($row, "htmlspecialchars_array");

            if (CTRL_SEMESTER < $row["SEMESTER"]) break;

            $arg["LESSON".$row["SEMESTER"]]             = $row["LESSON"];
            $arg["MOURNING_SUSPEND".$row["SEMESTER"]]   = $row["MOURNING_SUSPEND"];
            $arg["MLESSON".$row["SEMESTER"]]            = $row["MLESSON"];
            $arg["ABSENT".$row["SEMESTER"]]             = $row["ABSENT"];
            $arg["EARLY".$row["SEMESTER"]]              = $row["EARLY"];
        }
        $result->free();

        //出欠データ(遅刻回数)
        $query = knjd130hQuery::getAttendSHR($model, $isodate, implode("','", $isomonth), get_count($isomonth));
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            array_walk($row, "htmlspecialchars_array");

            if (CTRL_SEMESTER < $row["SEMESTER"]) break;

            $arg["SHR".$row["SEMESTER"]]        = $row["SHR"];
            $arg["NOT_SHR".$row["SEMESTER"]]    = $row["NOT_SHR"];
        }
        $result->free();

        //出欠データ(備考)
        $query = knjd130hQuery::getAttendRemark($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            array_walk($row, "htmlspecialchars_array");

            $arg["DI_REMARK".$row["SEMESTER"]]  = $row["DI_REMARK"];
        }
        $result->free();

        //学年・組を取得
        $grade = array();
        $grade = $db->getRow(knjd130hQuery::getGrade($model),DB_FETCHMODE_ASSOC);

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd130hQuery::getScAbsentCov(),DB_FETCHMODE_ASSOC);

        //成績データ
        $skekka1 = $skekka2 = $skekka9 = 0;
        $query = knjd130hQuery::getRecord($model, $isodate, implode("','", $isomonth), get_count($isomonth), $grade["GRADE"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"],$knjSchoolMst);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            array_walk($row, "htmlspecialchars_array");

            //学期評価・欠課時数は評価読替科目は非表示
            if( $row["REPLACEFLG"] != 9 ){
                if ( $row["ABSENT_SEM1"] != "" ) $skekka1 += $row["ABSENT_SEM1"];
                if ( $row["ABSENT_SEM2"] != "" ) $skekka2 += $row["ABSENT_SEM2"];
            } else {
                $row["SEM1_VALUE"] = "";
                $row["SEM2_VALUE"] = "";
            }
            //学年評定・欠課時数・履修単位数・修得単位数は評価読替元科目は非表示
            if( ( $row["REPLACEFLG"] == 9  &&  $row["GRAD_VALUE"] != "" )  ||
                ( $row["REPLACEFLG"] != 9  &&  $row["REPLACEFLG"] != 2 ) ){
                if( $row["REPLACEFLG"] == 9  &&  $row["REPLACE_ABSENT_TOTAL"] != "" ){
                    if ( $row["REPLACE_ABSENT_TOTAL"] != "" ) $skekka9 += $row["REPLACE_ABSENT_TOTAL"];
                } else
                if( ( $row["ABSENT_SEM1"] != "" ) ||
                    ( $row["ABSENT_SEM2"] != "" && 1 < CTRL_SEMESTER ) ||
                    ( $row["ABSENT_SEM3"] != "" && 2 < CTRL_SEMESTER ) ){
                    if ( $row["ABSENT_TOTAL"] != "" ) $skekka9 += $row["ABSENT_TOTAL"];
                }
            } else {
                $row["GRAD_VALUE"] = "";
            }

            $arg["data"][] = $row;
//break;
        }
        $result->free();

        $arg["ABSENT_SEM1"] = $skekka1;
        if (1 < CTRL_SEMESTER) $arg["ABSENT_SEM2"] = $skekka2;
        if (2 < CTRL_SEMESTER) $arg["ABSENT_SEM3"] = $skekka9 - $skekka1 - $skekka2;
//echo $skekka1 .">>";
//echo $skekka2 .">>";
//echo $skekka9 .">>";

        //成績データ(順位)
        $query = knjd130hQuery::getRecordRank($model, $grade["GRADE"], $grade["HR_CLASS"]);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            array_walk($row, "htmlspecialchars_array");

            //順位／人数
            if ( $row["SEM1_RNK"] != "" ) 
                $arg["PRECEDENCE1"] = $row["SEM1_RNK"];
//                $arg["PRECEDENCE1"] = $row["SEM1_RNK"] . "/" . $row["SEM1_CNT"];
            if ( $row["SEM2_RNK"] != "" && 1 < CTRL_SEMESTER ) 
                $arg["PRECEDENCE2"] = $row["SEM2_RNK"];
//                $arg["PRECEDENCE2"] = $row["SEM2_RNK"] . "/" . $row["SEM2_CNT"];
            if ( $row["GRAD_RNK"] != "" ) 
                $arg["PRECEDENCE3"] = $row["GRAD_RNK"];
//                $arg["PRECEDENCE3"] = $row["GRAD_RNK"] . "/" . $row["GRAD_CNT"];
        }
        $result->free();

        Query::dbCheckIn($db);


        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
                                                
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd130hSubForm1.html", $arg);
    }
}
?>