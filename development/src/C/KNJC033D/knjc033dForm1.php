<?php

require_once('for_php7.php');

class knjc033dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc033dForm1", "POST", "knjc033dindex.php", "", "knjc033dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $model->cntl_dt_key = $model->cntl_dt_key ? $model->cntl_dt_key : CTRL_DATE;

        //事前チェック
        if ($db->getOne(knjc033dQuery::checkSemesterMst($model)) == 0) {
            $arg["jscript"] = "PreCheck();";
        }

        //出欠入力画面の対象年組を選択
        if ($model->cmd == "main" && !$model->grade_hr_class) {
//            $model->grade_hr_class = $model->grade. '-' .$model->hr_class;
        }

        //日付表示
        if ($model->useDateText == "1") {
            //カレンダーコントロール
            $model->cntl_dt_key = $model->cntl_dt_key ? $model->cntl_dt_key : CTRL_DATE;
            $arg["DATE"] = View::popUpCalendar2($objForm, "DATE",
                                               str_replace("-","/",$model->cntl_dt_key),"reload=true",""," style=\"background-color:lightgray\" readOnly");
        } else {
            $model->cntl_dt_key = CTRL_DATE;
            $weekday = array( "日", "月", "火", "水", "木", "金", "土" );
            $w = $weekday[date("w", strtotime(str_replace("/","-",$model->cntl_dt_key)))]."曜日";
            list ($y, $m ,$d) = explode('-', str_replace("/","-",$model->cntl_dt_key));
            $arg["DATE"] =  $y.'年 '.$m.'月 '.$d.'日（'.$w.'）';
        }

        //校種取得
        $tmp = array();
        $school_kind = "";
        $cnt1 = 1;
        $query = knjc033dQuery::getSchoolKind($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学年取得
            $grade = "";
            $query1 = knjc033dQuery::getGrade($row["NAME1"], $model);
            $result1 = $db->query($query1);
            while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

                $tmp["SCHOOL_KIND"] = ($school_kind == $row["NAME1"]) ? "" : $row["ABBV1"];

                //年組取得
                $tmp["GRADE_HR_CLASS"] = "";
                $cnt2 = 1;
                $query2 = knjc033dQuery::getGradeHrClass($row1["GRADE"], "", $model);
                $result2 = $db->query($query2);
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {

                    $sp = (!$tmp["GRADE_HR_CLASS"] || ($cnt2 > 8 && $cnt2 % 8 == 1)) ? "" : "　";
                    $br = ($cnt2 > 8 && $cnt2 % 8 == 1) ? "<br>" : "";

                    //文字色・背景色設定
                    $val = $row2["GRADE"] . '-' . $row2["HR_CLASS"];
                    $color = ($model->grade_hr_class == $val) ? "color:red;" : (($row2["EXECUTED"] == "1") ? "color:#ffffff;" : "");
                    $bgcolor = ($row2["EXECUTED"] == "1") ? "background:#3399ff;" : "";

                    //年組ボタン
                    $extra  = "onclick=\" setHrClass(this, '{$val}', '{$row2["HR_STAFF"]}');\"";
                    $extra .= " style=\"height:90px;width:175px;font-weight:bold;font-size:18pt;{$color}{$bgcolor}\"";
                    $tmp["GRADE_HR_CLASS"] .= $br.$sp.knjCreateBtn($objForm, "btn_form2".$val, $row2["HR_NAME"], $extra);

                    $tmp["line"] = ($tmp["SCHOOL_KIND"] && $cnt1 > 1) ? 1 : "";

                    $cnt2++;
                }
                $result2->free();
                $arg["data"][] = $tmp;

                $school_kind = $row["NAME1"];
            }
            $result1->free();
            $cnt1++;
        }
        $result->free();

        //選択クラス取消ボタン
        $extra  = (!$model->grade_hr_class) ? "disabled" : "";
        $extra .= " style=\"height:60px;font-weight:bold;font-size:15pt;\" onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "選択クラス取消", $extra);

        //全員出席ボタン
        $extra  = ($model->auth < DEF_UPDATE_RESTRICT || !$model->grade_hr_class) ? "disabled" : "";
        $extra .= " style=\"height:90px;width:220px;font-weight:bold;font-size:20pt;\" onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_all"] = knjCreateBtn($objForm, "btn_all", "全員出席", $extra);

        //出欠入力ボタン
        list($grade, $hr_class) = explode('-', $model->grade_hr_class);
        $link   = REQUESTROOT."/C/KNJC033D/knjc033dindex.php?cmd=form2&GRADE=".$grade."&HR_CLASS=".$hr_class."&DATE=".$model->cntl_dt_key;
        $extra  = (!$model->grade_hr_class) ? "disabled" : "";
        $extra .= " style=\"height:90px;width:220px;font-weight:bold;font-size:20pt;\" onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_attend"] = knjCreateBtn($objForm, "btn_attend", "出欠入力", $extra);

        //管理者コントロール
        $admin_control = $db->getOne(knjc033dQuery::checkAdminControlDat($model, $grade));

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "GRADE_HR_CLASS_SET", $model->grade_hr_class);
        knjCreateHidden($objForm, "ADMIN_CONTROL", $admin_control);
        knjCreateHidden($objForm, "useDateText", $model->useDateText);

        //権限（制限付き）
        $restrict = ($model->auth == DEF_UPDATE_RESTRICT || $model->auth == DEF_REFER_RESTRICT) ? "1" : "0";
        knjCreateHidden($objForm, "RESTRICT", $restrict);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc033dForm1.html", $arg);
    }
}
?>
