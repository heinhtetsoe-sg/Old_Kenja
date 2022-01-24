<?php

require_once('for_php7.php');

class knja080mForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        // $arg["Read"] = "start();";
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knja080mindex.php", "", "sel");
        $db = Query::dbCheckOut();

        $year_seme_arr = array();
        $result = $db->query(knja080mQuery::GetYearSeme($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $year_seme_arr[] = array("label" => $row["YEAR"]."年度　".$row["SEMESTERNAME"], 
                                       "value" => $row["YEAR"]."-".$row["SEMESTER"] );
        }
        $extra = "tabindex=\"1\" OnChange=\"return btn_submit('selectYear')\"";
        $arg["YEAR_SEME"] = knjCreateCombo($objForm, "YEAR_SEME", $model->year_seme, $year_seme_arr, $extra, 1);

        //ログイン校種のA023
        $query = knja080mQuery::getA023SchoolKind($model, SCHOOLKIND);
        $model->schoolKindA023 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //A023全て
        $query = knja080mQuery::getA023($model);
        $result = $db->query($query);
        $model->A023 = array();
        $model->A023GradeHr = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $setGradeHr = "00-00".$row["ABBV2"];
            $model->A023[$row["NAME1"]] = $row;
            $model->A023[$row["NAME1"]]["GRADEHR"] = $setGradeHr;

            $model->A023GradeHr[$setGradeHr] = $row;
        }
        $result->free();


        // 新入生の取得
        $model->freshManList = array();
        $query = knja080mQuery::getfreshMan($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->freshManList[$row['SCHREGNO']] = $row;
        }

        // 初期化
        $opt["RIGHT_CLASS"] = array();
        $opt["LEFT_CLASS"]  = array();

        // 右側のクラス取得
        if (get_count($model->freshManList) > 0) {
            $opt["RIGHT_CLASS"][] = array("label" => "新入生", "value" => "00-000");
        }
        // 右側のクラス取得
        $result = $db->query(knja080mQuery::getHr_Class($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $hrClass = $row["GRADE"] . "-" . $row["HR_CLASS"];
            if (!$model->leftclass) {
                $model->leftclass = $hrClass;
            }
            if ($model->leftclass != $hrClass) {
                $opt["RIGHT_CLASS"][] = array("label" => $row["HR_NAME"] ."　".$row["NAME"],
                                              "value" => $hrClass);
            }
        }
        $model->rightclass = $model->rightclass ? $model->rightclass : $opt["RIGHT_CLASS"][0]["value"];
        $extra = "onchange=\"btn_submit('selectclass')\"";
        $arg["RIGHT_CLASS"] = knjCreateCombo($objForm, "RIGHT_CLASS", $model->rightclass, $opt["RIGHT_CLASS"], $extra, 1);

        // 左側のクラス取得
        $result = $db->query(knja080mQuery::getHr_Class($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $hrClass = $row["GRADE"] . "-" . $row["HR_CLASS"];
            if ($model->rightclass != $hrClass) {
                $opt["LEFT_CLASS"][] = array("label" => $row["HR_NAME"] ."　".$row["NAME"], 
                                             "value" => $hrClass );
            }
        }
        $model->leftclass = $model->leftclass ? $model->leftclass : $opt["LEFT_CLASS"][0]["value"];
        $extra = "onchange=\"btn_submit('selectclass')\"";
        $arg["LEFT_CLASS"] = knjCreateCombo($objForm, "LEFT_CLASS", $model->leftclass, $opt["LEFT_CLASS"], $extra, 1);

        $opt["LEFT_CLASS_STU"] = array();
        $opt["RIGHT_CLASS_STU"] = array();

        //左クラス一覧
        $query = knja080mQuery::GetStudent($model, $model->leftclass);
        $result = $db->query($query);
        $model->schregnoL = array();
        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->rightclass == '00-000' && $row["FRESHMAN_FLG"] != "1") {
                continue;
            }
            $row["NAME"] = str_replace(" ","&nbsp;",$row["NAME"]);
            $freshMan = ($row["FRESHMAN_FLG"] == "1") ? " [新] " : "　　　";
            $opt["LEFT_CLASS_STU"][] = array("label"  => $freshMan."　".$row["HR_NAME"].$row["ATTENDNO"] ."番"."　".$row["SCHREGNO"]."　" .$row["NAME"],
                                             "value" => $row["SCHREGNO"]);
            $model->schregnoL[] = $row["SCHREGNO"];
            $i++;
        }
        $arg["LEFTNUM"] = $i;
        $extra = "multiple STYLE=\"WIDTH:100%; height:500px;\" ondblclick=\"moveStudent('right')\"";
        $leftVal = "left";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_CLASS_STU", $leftVal, $opt["LEFT_CLASS_STU"], $extra, 35);
        
        //右クラス一覧
        $query = knja080mQuery::GetStudent($model, $model->rightclass);
        $result = $db->query($query);
        $model->schregnoR = array();
        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row["NAME"] = str_replace(" ","&nbsp;",$row["NAME"]);
            $freshMan = ($row["FRESHMAN_FLG"] == "1") ? " [新] " : "　　　";
            $opt["RIGHT_CLASS_STU"][] = array("label"  => $freshMan."　".$row["HR_NAME"].$row["ATTENDNO"] ."番"."　".$row["SCHREGNO"]."　" .$row["NAME"],
                                              "value" => $row["SCHREGNO"]);
            $model->schregnoR[] = $row["SCHREGNO"];
            $i++;
        }
        $arg["RIGHTNUM"] = $i;
        $extra = "multiple STYLE=\"WIDTH:100%; height:500px;\"  ondblclick=\"moveStudent('left')\"";
        $leftVal = "left";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_CLASS_STU", $leftVal, $opt["RIGHT_CLASS_STU"], $extra, 35);

        //全員左ボタン
        $extra = "onclick=\"return moveStudent('sel_add_all');\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //左ボタン
        $extra = "onclick=\"return moveStudent('left');\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //右ボタン
        $extra = "onclick=\"return moveStudent('right');\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //全員右ボタン
        $extra = "onclick=\"return moveStudent('sel_del_all');\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);


        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_update", "更新", $extra);
        
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取消", $extra);
        
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);
        
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "MIN_SEMESTER", $model->min_semester);
        knjCreateHidden($objForm, "UPDATE_FLG");
        knjCreateHidden($objForm, "leftData");
        knjCreateHidden($objForm, "rightData");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja080mForm1.html", $arg); 
    }
}
?>
